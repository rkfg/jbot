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
            Document doc = Jsoup.connect(url).maxBodySize(102400).userAgent("Mozilla").cookie("auth", "token").get();
            String titleStr = doc.title();
            return String.format("Title: %s", titleStr);
        } catch (InterruptedIOException e) {
            // timeout, doing nothing
        } catch (ClientProtocolException e) {
            //
        } catch (IOException e) {
            //
        }
        return null;
    }
}