package me.rkfg.xmpp.bot.plugins;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.ClientProtocolException;
import org.jivesoftware.smack.packet.Message;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import static org.jsoup.helper.HttpConnection.DEFAULT_UA;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TitlePlugin extends MessagePluginImpl {

    private static final int MAX_BODY_SIZE = 102400;
    private static final int TIMEOUT = 10000;
    private Logger log = LoggerFactory.getLogger(getClass());
    
    @Override
    public Pattern getPattern() {
        return Pattern.compile("(https?://[^ ]+)", Pattern.CASE_INSENSITIVE);
    }

    @Override
    public String process(Message message, Matcher matcher) {
        String url = matcher.group(1);
        log.info("*** URL ***: {}", url);
        try {
            Document doc = Jsoup.connect(url).timeout(TIMEOUT).maxBodySize(MAX_BODY_SIZE).userAgent(DEFAULT_UA)
                    .header("Accept-Language", "ru,en").get();
            String titleStr = doc.title();
            return String.format("Title: %s", URLDecoder.decode(titleStr, "UTF-8"));
        } catch (InterruptedIOException e) {
            log.warn("Timed out loading URL: {}", url);
        } catch (ClientProtocolException e) {
            log.warn(e.getMessage());
        } catch (UnsupportedMimeTypeException e) {
            log.warn(e.getMessage());
        } catch (IOException e) {
            log.warn(e.getMessage());
        }
        return null;
    }
}
