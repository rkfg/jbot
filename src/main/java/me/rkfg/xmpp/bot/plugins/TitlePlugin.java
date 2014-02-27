package me.rkfg.xmpp.bot.plugins;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.IDN;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.ClientProtocolException;
import org.jivesoftware.smack.packet.Message;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TitlePlugin extends MessagePluginImpl {

    private static final String USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.117 Safari/537.36";
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
            //
        } catch (IOException e) {
            //
        }
        return null;
    }
}