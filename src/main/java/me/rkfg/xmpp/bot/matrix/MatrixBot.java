package me.rkfg.xmpp.bot.matrix;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jivesoftware.smack.chat.ChatManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import me.rkfg.xmpp.bot.BotBase;
import me.rkfg.xmpp.bot.exceptions.NotImplementedException;
import me.rkfg.xmpp.bot.message.BotMessage;
import me.rkfg.xmpp.bot.message.MatrixMessage;
import me.rkfg.xmpp.bot.plugins.MessagePlugin;
import me.rkfg.xmpp.bot.xmpp.ChatAdapter;
import me.rkfg.xmpp.bot.xmpp.MUCManager;
import ru.ppsrk.gwt.client.LogicException;

public class MatrixBot extends BotBase {

    private static final String RETRY_AFTER_MS = "retry_after_ms";
    private static final String EVENT_ID = "event_id";
    private static final String ROOMS = "rooms/";
    private static final String EVENTS = "events";
    private static final int TIMEOUT = 40000;
    private String token = null;
    private String apiServer = null;
    private String ownMXID = null;
    private HttpClient httpClient = HttpClientBuilder.create().build();
    private RequestConfig reqConfig = RequestConfig.custom().setSocketTimeout(TIMEOUT).setConnectTimeout(TIMEOUT)
            .setConnectionRequestTimeout(TIMEOUT).build();
    private StateManager stateManager = new StateManager();
    private RoomParticipantsManager roomParticipantsManager = null;
    private Map<String, Transaction> pendingEvents = new HashMap<>();
    private List<MatrixMessage> failedEvents = new LinkedList<>();

    @Override
    public int run() throws LogicException {
        try {
            init();
            token = sm.getStringSetting("matrixToken");
            ownMXID = sm.getStringSetting("mxid");
            if (ownMXID == null) {
                log.error("Set mxid property in settings.ini");
                return 1;
            }
            roomParticipantsManager = new RoomParticipantsManager(ownMXID);
            if (token == null) {
                log.error("Please set matrixToken in settings.ini to the access token");
                return 1;
            }
            apiServer = sm.getStringSetting("apiServer");
            if (apiServer == null) {
                log.error("Please set apiServer in settings.ini to the server you'd like to use (ex. matrix.org)");
                return 1;
            }
            JSONObject resp = get("sync", new BasicNameValuePair("timeout", "30000"));
            String next = resp.getString("next_batch");
            startRetryLoop();
            parseSync(resp, true);
            loopSync(next);
        } catch (IOException | URISyntaxException e) {
            log.warn("{}", e);
            return 2;
        }
        return 0;
    }

    private void startRetryLoop() {
        new Timer("message retry timer", true).scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                synchronized (failedEvents) {
                    failedEvents.stream().filter(m -> m.getResendTS() < System.currentTimeMillis() && m.getRetryCount() > 0).forEach(m -> {
                        log.info("Resent to {}, {}", m.getFromRoom(), m.getBody());
                        final JSONObject resp = doSendMessage(m.getBody(), m.getFromRoom());
                        if (resp != null && resp.has(EVENT_ID)) {
                            log.info("Successfully resent to {}", m.getFromRoom());
                            m.setRetryCount(0);
                        } else {
                            log.warn("Couldn't resend to {}, counter was {}", m.getFromRoom(), m.getRetryCount());
                            m.decRetry();
                            if (resp != null) {
                                m.addResendTS(resp.optLong(RETRY_AFTER_MS));
                            }
                        }
                    });
                    failedEvents.removeIf(m -> m.getRetryCount() == 0);
                }
            }
        }, 200, 200);
    }

    public void loopSync(String next) throws URISyntaxException, IOException {
        while (!Thread.currentThread().isInterrupted()) {
            JSONObject resp = new JSONObject();
            try {
                resp = get("sync", new BasicNameValuePair("timeout", "30000"), new BasicNameValuePair("since", next));
                next = resp.getString("next_batch");
                parseSync(resp, false);
            } catch (Exception e) {
                log.warn("Can't parse sync: {}, {}", resp.toString(2), e);
            }
        }
    }

    public void parseSync(JSONObject resp, boolean initialSync) {
        JSONObject rooms = resp.getJSONObject("rooms");
        JSONObject joinedRooms = rooms.getJSONObject("join");
        for (Object roomId : joinedRooms.keySet()) {
            String roomIdStr = (String) roomId;
            JSONObject room = joinedRooms.getJSONObject(roomIdStr);
            stateManager.joinRoom(roomIdStr);
            JSONArray events = room.getJSONObject("timeline").getJSONArray(EVENTS);
            processEvents(initialSync, roomIdStr, events);
            if (initialSync) {
                events = room.getJSONObject("state").getJSONArray(EVENTS);
                processEvents(initialSync, roomIdStr, events);
            }
        }
        JSONObject invitedRooms = rooms.getJSONObject("invite");
        for (Object roomId : invitedRooms.keySet()) {
            String roomIdStr = (String) roomId;
            try {
                log.info("Invited to {}, joining", roomIdStr);
                post(ROOMS + roomIdStr + "/join", new JSONObject());
            } catch (URISyntaxException | IOException e) {
                log.warn("{}", e);
            }
            roomParticipantsManager.addUser(roomIdStr, ownMXID);
            processEvents(initialSync, roomIdStr, invitedRooms.getJSONObject(roomIdStr).getJSONObject("invite_state").getJSONArray(EVENTS));
        }
        Set<String> emptyRooms = roomParticipantsManager.getEmptyRooms();
        emptyRooms.stream().forEach(roomId -> {
            try {
                log.info("No more users in {}, leaving", roomId);
                post(ROOMS + roomId + "/leave", new JSONObject());
            } catch (URISyntaxException | IOException e) {
                log.warn("{}", e);
            }
        });
        emptyRooms.stream().forEach(roomParticipantsManager::removeRoom);
    }

    public void processEvents(boolean initialSync, String roomId, JSONArray events) {
        for (int i = 0; i < events.length(); ++i) {
            JSONObject event = events.getJSONObject(i);
            String type = event.optString("type");
            String senderId = event.getString("sender");
            JSONObject content = event.getJSONObject("content");
            if (!initialSync && "m.room.message".equals(type)) {
                String body = content.getString("body");
                if (log.isDebugEnabled()) {
                    log.debug(event.toString(4));
                }
                log.debug("Content: {}", body);
                if (!fromMyself(event)) {
                    processMessage(body, roomId, senderId);
                } else {
                    log.debug("Received a message from myself, not processing");
                }
            }
            if ("m.room.member".equals(type)) {
                String membership = content.optString("membership");
                boolean join = "join".equals(membership);
                boolean leave = "leave".equals(membership);
                if (join) {
                    stateManager.addDisplayName(senderId, content.optString("displayname"));
                    roomParticipantsManager.addUser(roomId, senderId);
                }
                if (leave) {
                    stateManager.removeDisplayName(senderId);
                    roomParticipantsManager.removeUser(roomId, senderId);
                }
            }
        }
    }

    private boolean fromMyself(JSONObject event) {
        try {
            String eventId = event.getString(EVENT_ID);
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

    private JSONObject doSendMessage(String body, String roomId) {
        try {
            JSONObject resp = put(ROOMS + roomId + "/send/m.room.message/jbot" + System.currentTimeMillis(),
                    new JSONObject().put("msgtype", "m.text").put("formatted_body", body.replaceAll("\n", "<br/>"))
                            .put("format", "org.matrix.custom.html").put("body", body.replaceAll("<[^>]*>([^<]*)</[^>]*>", "$1")));
            String eventId = resp.optString(EVENT_ID);
            if (!eventId.isEmpty()) {
                pendingEvents.put(eventId, new Transaction(eventId, new MatrixMessage(stateManager, body, roomId, null)));
            }
            return resp;
        } catch (JSONException | URISyntaxException | IOException e) {
            log.warn("{}", e);
        }
        return null;
    }

    @Override
    public String sendMessage(String body, String roomId) {
        final JSONObject resp = doSendMessage(body, roomId);
        String eventId = null;
        long retry = 0;
        if (resp != null) {
            retry = resp.optLong(RETRY_AFTER_MS);
            eventId = resp.optString(EVENT_ID);
        }
        if (eventId != null && !eventId.isEmpty()) {
            return eventId;
        }
        long resendTS = System.currentTimeMillis() + retry + 100;
        synchronized (failedEvents) {
            log.warn("Message failed, resend at {}", resendTS);
            final MatrixMessage msg = new MatrixMessage(stateManager, body, roomId, null);
            msg.setResendTS(resendTS);
            failedEvents.add(msg);
        }
        return null;
    }

    public JSONObject fillAndExec(JSONObject body, HttpEntityEnclosingRequestBase req) throws IOException {
        req.setConfig(reqConfig);
        req.setHeader("Content-Type", "application/json");
        req.setEntity(new StringEntity(body.toString(), StandardCharsets.UTF_8));
        return exec(req);
    }

    private JSONObject put(String method, JSONObject body, NameValuePair... params) throws URISyntaxException, IOException {
        return fillAndExec(body, new HttpPut(buildURI(method, params)));
    }

    private JSONObject post(String method, JSONObject body, NameValuePair... params) throws URISyntaxException, IOException {
        return fillAndExec(body, new HttpPost(buildURI(method, params)));
    }

    private void processMessage(String body, String roomId, String sender) {
        for (MessagePlugin plugin : plugins) {
            Pattern pattern = plugin.getPattern();
            if (pattern != null) {
                Matcher matcher = pattern.matcher(body);
                if (matcher.find()) {
                    try {
                        String result = plugin.process(new MatrixMessage(stateManager, body, roomId, sender), matcher);
                        if (result != null && !result.isEmpty()) {
                            sendMessage(StringEscapeUtils.unescapeHtml4(result), roomId);
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
    public void processMessage(ChatAdapter mucAdapted, BotMessage message) {
        throw new NotImplementedException();
    }

    @Override
    public void sendMessage(String message) {
        for (String roomId : stateManager.listRooms()) {
            sendMessage(message, roomId);
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

    @Override
    public Set<String> getRoomsWithUser(String userId) {
        return stateManager.listRooms().stream().filter(r -> roomParticipantsManager.getUsers(r).contains(userId))
                .collect(Collectors.toSet());
    }

    @Override
    public boolean isDirectChat(String roomId) {
        return roomParticipantsManager.isDirectChat(roomId);
    }
}
