package me.rkfg.xmpp.bot.plugins;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jivesoftware.smack.packet.Message;
import org.json.JSONObject;

public class VKRepostPlugin extends MessagePluginImpl {

    private static final Pattern PATTERN = Pattern.compile("(https?://([^.]*\\.userapi\\.com|[^.]*\\.vk\\.me)[^ ]+)", Pattern.DOTALL);
    private static final int MAX_IMAGE_SIZE = 5 * 1024 * 1024;

    @Override
    public Pattern getPattern() {
        return PATTERN;
    }

    @Override
    public String process(Message message, Matcher matcher) {
        String url = matcher.group(1);
        try {
            String apiKey = getSettingsManager().getStringSetting("imgurApiKey");
            if (apiKey == null || apiKey.isEmpty()) {
                log.warn("Set imgurApiKey option in settings.ini!");
                return null;
            }
            URLConnection imgConn = new URL(url).openConnection();
            if (imgConn.getContentLength() > MAX_IMAGE_SIZE) {
                return null;
            }
            imgConn.setConnectTimeout(5000);
            imgConn.setReadTimeout(5000);
            CloseableHttpClient httpClient = HttpClientBuilder.create().build();
            HttpPost req = new HttpPost("https://api.imgur.com/3/upload");
            req.setConfig(RequestConfig.custom().setConnectTimeout(5000).setSocketTimeout(5000).setConnectionRequestTimeout(5000).build());
            req.addHeader("Authorization", "Client-ID " + apiKey);
            req.setEntity(MultipartEntityBuilder.create().addBinaryBody("image", imgConn.getInputStream()).build());
            CloseableHttpResponse response = httpClient.execute(req);
            if (response.getStatusLine().getStatusCode() == 200) {
                StringBuilder sb = new StringBuilder();
                try (BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }
                }
                JSONObject obj = new JSONObject(sb.toString());
                if (!obj.isNull("data")) {
                    JSONObject data = obj.getJSONObject("data");
                    if (!data.isNull("link")) {
                        String link = data.getString("link");
                        log.info("Upload result: {}", link);
                        return url + " ⇒ " + link + " Слава Украине! \\o";
                    }
                }
                log.warn("Upload error: {}", sb);
            } else {
                log.warn("Bad response: {}", response.getStatusLine());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
