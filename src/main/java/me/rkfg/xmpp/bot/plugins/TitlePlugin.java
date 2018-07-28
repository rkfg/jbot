package me.rkfg.xmpp.bot.plugins;

import static org.jsoup.helper.HttpConnection.*;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.UnsupportedEncodingException;
import java.net.IDN;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.ClientProtocolException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.rkfg.xmpp.bot.message.BotMessage;

public class TitlePlugin extends MessagePluginImpl {

    private static final int MAX_BODY_SIZE = 102400;
    private static final int TIMEOUT = 10000;
    private Logger log = LoggerFactory.getLogger(getClass());
    private Pattern hostnamePattern = Pattern.compile("https?://([^/]*)/?.*", Pattern.CASE_INSENSITIVE);
    private Pattern hostnameReplacePattern = Pattern.compile("(https?://)[^/]*(/?.*)", Pattern.CASE_INSENSITIVE);

    @Override
    public Pattern getPattern() {
        return Pattern.compile("(https?://[^ ]+)", Pattern.CASE_INSENSITIVE);
    }

    @Override
    public String process(BotMessage message, Matcher matcher) {
        String url;
        try {
            url = URLDecoder.decode(matcher.group(1), StandardCharsets.UTF_8.name());
            log.info("*** URL ***: {}", url);
            try {
                try {
                    String hostname = hostnamePattern.matcher(url).replaceAll("$1");
                    hostname = IDN.toASCII(hostname);
                    url = hostnameReplacePattern.matcher(url).replaceAll("$1" + hostname + "$2");
                } catch (IllegalArgumentException e) {
                    // this is not an IDN-encoded url
                }
                Document doc = Jsoup.connect(url).timeout(TIMEOUT).maxBodySize(MAX_BODY_SIZE).userAgent(DEFAULT_UA)
                        .header("Accept-Language", "ru,en").get();
                String titleStr = doc.title();
                return String.format("Title: %s", titleStr);
            } catch (InterruptedIOException e) {
                log.warn("Timed out loading URL: {}", url);
            } catch (ClientProtocolException e) {
                log.warn(e.getMessage());
            } catch (UnsupportedMimeTypeException e) {
                log.warn(e.getMessage());
            } catch (IOException e) {
                log.warn(e.getMessage());
            }
        } catch (UnsupportedEncodingException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        return null;
    }
}
