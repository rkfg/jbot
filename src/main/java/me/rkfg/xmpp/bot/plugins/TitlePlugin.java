package me.rkfg.xmpp.bot.plugins;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.net.IDN;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.ClientProtocolException;
import org.jivesoftware.smack.packet.Message;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TitlePlugin extends MessagePluginImpl {

    private static final String USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.117 Safari/537.36";
    private static final int MAX_BODY_SIZE = 102400;
    private static final int TIMEOUT = 10000;
    private Logger log = LoggerFactory.getLogger(getClass());

    private Set<String> imageMime = new HashSet<>(Arrays.asList("image/png", "image/jpg", "image/jpeg", "image/gif", "image/bmp"));

    @Override
    public Pattern getPattern() {
        return Pattern.compile("(https?://[^ ]+)", Pattern.CASE_INSENSITIVE);
    }

    @Override
    public String process(Message message, Matcher matcher) {
        String url = matcher.group(1);
        log.info("*** URL ***: {}", url);
        // punycode my ass
        // need to make it more versatile and not bounded to .рф/.ру only
        if (url.matches("(?iu).+\\.(рф|ру)")) {
            url = "http://" + IDN.toASCII(url.replaceAll("http://", ""));
        }
        try {
            Document doc = Jsoup.connect(url).timeout(TIMEOUT).maxBodySize(MAX_BODY_SIZE).userAgent(USER_AGENT).get();
            String titleStr = doc.title();
            return String.format("Title: %s", titleStr);
        } catch (InterruptedIOException e) {
            log.warn("Timed out loading URL: {}", url);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (UnsupportedMimeTypeException e) {
            if (imageMime.contains(e.getMimeType())) {
                log.info("Image found in URL: {}", e.getUrl());
                try {
                    URLConnection connection = new URL(e.getUrl()).openConnection();
                    connection.setConnectTimeout(5000);
                    connection.setReadTimeout(20000);
                    int contentLength = connection.getContentLength();
                    if (contentLength > 5120000) {
                        log.warn("Content is too big ({})!", contentLength);
                        return null;
                    }
                    URLConnection conn = new URL("http://127.0.0.1:5010/classify_url?imageurl=" + URLEncoder.encode(e.getUrl(), "UTF-8"))
                            .openConnection();
                    conn.setConnectTimeout(5000);
                    conn.setReadTimeout(20000);
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) {
                        sb.append(line);
                    }
                    JSONObject betsObj = new JSONObject(sb.toString());
                    if (betsObj.has("bets")) {
                        StringBuilder result = new StringBuilder();
                        JSONArray betsArr = betsObj.getJSONArray("bets");
                        for (int i = 0; i < betsArr.length(); i++) {
                            if (result.length() > 0) {
                                result.append(", ");
                            }
                            result.append(betsArr.get(i));
                        }
                        return "На картинке, возможно: " + result.toString();
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}