package me.rkfg.xmpp.bot.matrix;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.packet.Message;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.rkfg.xmpp.bot.BotBase;
import me.rkfg.xmpp.bot.exceptions.NotImplementedException;
import me.rkfg.xmpp.bot.message.MatrixMessage;
import me.rkfg.xmpp.bot.plugins.MessagePlugin;
import me.rkfg.xmpp.bot.xmpp.ChatAdapter;
import me.rkfg.xmpp.bot.xmpp.MUCManager;

public class MatrixBot extends BotBase {

    private static final int TIMEOUT = 40000;
    private String token = null;
    private String apiServer = null;
    private HttpClient httpClient = HttpClientBuilder.create().build();
    private RequestConfig reqConfig = RequestConfig.custom().setSocketTimeout(TIMEOUT).setConnectTimeout(TIMEOUT)
            .setConnectionRequestTimeout(TIMEOUT).build();
    private Logger log = LoggerFactory.getLogger(getClass());
    private StateManager stateManager = new StateManager();
    private Map<String, Transaction> pendingEvents = new HashMap<>();

    @Override
    public void run() {
        try {
            init();
            token = sm.getStringSetting("matrixToken");
            if (token == null) {
                log.error("Please set matrixToken in settings.ini to the access token");
                System.exit(1);
            }
            apiServer = sm.getStringSetting("apiServer");
            if (apiServer == null) {
                log.error("Please set apiServer in settings.ini to the server you'd like to use (ex. matrix.org)");
                System.exit(1);
            }
            JSONObject resp = get("sync", new BasicNameValuePair("timeout", "30000"));
            String next = resp.getString("next_batch");
            parseSync(resp, true);
            loopSync(next);
        } catch (IOException | URISyntaxException e) {
            log.warn("{}", e);
        }
    }

    public void loopSync(String next) throws URISyntaxException, IOException {
        while (!Thread.currentThread().isInterrupted()) {
            JSONObject resp = get("sync", new BasicNameValuePair("timeout", "30000"), new BasicNameValuePair("since", next));
            next = resp.getString("next_batch");
            try {
                parseSync(resp, false);
            } catch (JSONException e) {
                log.warn("Can't parse sync: {}", resp.toString(2));
            }
        }
    }

    public void parseSync(JSONObject resp, boolean initialSync) {
        JSONObject rooms = resp.getJSONObject("rooms").getJSONObject("join");
        for (Object roomName : rooms.keySet()) {
            String roomNameStr = (String) roomName;
            JSONObject room = rooms.getJSONObject(roomNameStr);
            stateManager.joinRoom(roomNameStr);
            JSONArray events = room.getJSONObject("timeline").getJSONArray("events");
            processEvents(initialSync, roomNameStr, events);
            if (initialSync) {
                events = room.getJSONObject("state").getJSONArray("events");
                processEvents(initialSync, roomNameStr, events);
            }
        }
    }

    public void processEvents(boolean initialSync, String roomName, JSONArray events) {
        for (int i = 0; i < events.length(); ++i) {
            JSONObject event = events.getJSONObject(i);
            String type = event.optString("type");
            String sender = event.getString("sender");
            JSONObject content = event.getJSONObject("content");
            if (!initialSync && "m.room.message".equals(type)) {
                String body = content.getString("body");
                log.debug(event.toString(4));
                log.debug("Content: {}", body);
                if (!fromMyself(event)) {
                    processMessage(body, roomName, sender);
                } else {
                    log.debug("Received a message from myself, not processing");
                }
            }
            if ("m.room.member".equals(type)) {
                boolean join = "join".equals(content.optString("membership"));
                if (join) {
                    stateManager.addDisplayName(sender, content.optString("displayname"));
                } else {
                    stateManager.removeDisplayName(sender);
                }
            }
        }
    }

    private boolean fromMyself(JSONObject event) {
        try {
            String eventId = event.getString("event_id");
            return pendingEvents.remove(eventId) != null;
        } catch (JSONException e) {
            // not found
        }
        return false;
    }

    private JSONObject get(String method, NameValuePair... params) throws URISyntaxException, IOException {
        URI uri = buildURI(method, params);
        HttpGet req = new HttpGet(uri);
        req.setConfig(reqConfig);
        return exec(req);
    }

    public JSONObject exec(HttpUriRequest req) throws IOException {
        HttpResponse response = httpClient.execute(req);
        String entity = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
        return new JSONObject(entity);
    }

    public URI buildURI(String method, NameValuePair... params) throws URISyntaxException {
        return new URIBuilder("https://" + apiServer + "/_matrix/client/r0/" + method).addParameters(Arrays.asList(params))
                .addParameter("access_token", token).build();
    }

    @Override
    public String sendMessage(String body, String room) {
        try {
            JSONObject resp = put("rooms/" + room + "/send/m.room.message/jbot" + System.currentTimeMillis(),
                    new JSONObject().put("msgtype", "m.text").put("formatted_body", body).put("format", "org.matrix.custom.html")
                            .put("body", body.replaceAll("<a[^>]*>([^<]*)</a>", "$1")));
            String eventId = resp.optString("event_id");
            pendingEvents.put(eventId, new Transaction(eventId, new MatrixMessage(stateManager, body, room, null)));
            return eventId;
        } catch (JSONException | URISyntaxException | IOException e) {
            log.warn("{}", e);
        }
        return null;
    }

    private JSONObject put(String method, JSONObject body, NameValuePair... params) throws URISyntaxException, IOException {
        URI uri = buildURI(method, params);
        HttpPut req = new HttpPut(uri);
        req.setConfig(reqConfig);
        req.setHeader("Content-Type", "application/json");
        req.setEntity(new StringEntity(body.toString(), StandardCharsets.UTF_8));
        return exec(req);
    }

    private void processMessage(String body, String room, String sender) {
        for (MessagePlugin plugin : plugins) {
            Pattern pattern = plugin.getPattern();
            if (pattern != null) {
                Matcher matcher = pattern.matcher(body);
                if (matcher.find()) {
                    try {
                        String result = plugin.process(new MatrixMessage(stateManager, body, room, sender), matcher);
                        if (result != null && !result.isEmpty()) {
                            sendMessage(StringEscapeUtils.unescapeHtml4(result), room);
                            break;
                        }
                    } catch (Exception e) {
                        log.warn("{}", e);
                    }
                }
            }
        }
    }

    @Override
    public void processMessage(ChatAdapter mucAdapted, Message message) {
        throw new NotImplementedException();
    }

    @Override
    public void sendMessage(String message) {
        for (String roomName : stateManager.listRooms()) {
            sendMessage(message, roomName);
        }
    }

    @Override
    public ChatManager getChatManagerInstance() {
        throw new NotImplementedException();
    }

    @Override
    public MUCManager getMUCManager() {
        throw new NotImplementedException();
    }

}
