package me.rkfg.xmpp.bot.plugins;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.rkfg.xmpp.bot.Utils;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.jivesoftware.smack.packet.Message;
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
        if (!isMessageFromUser(message)) {
            return null;
        }
        final String url = matcher.group(1);
        log.info("*** URL ***: {}", url);
        if (url.matches("(?iu).+\\.(jpg|jpeg|png|gif|tif|bmp)$")) {
            return null;
        }
        try {
            HttpClient client = Utils.getHTTPClient();
            HttpResponse response = client.execute(new HttpGet(url));
            InputStream inputStream = response.getEntity().getContent();
            byte[] buf = new byte[102400];
            int readCount = 0;
            int read = 1;
            while (readCount < 102400) {
                read = inputStream.read(buf, readCount, buf.length - readCount);
                if (read > 0) {
                    readCount += read;
                } else {
                    break;
                }
            }
            inputStream.close();
            String page = "";
            String charsetPage = new String(buf, 0, readCount, "latin1");
            Matcher charset = Pattern.compile("charset=\"?(.+?)\"").matcher(charsetPage);
            if (charset.find()) {
                if ("windows-1251".equalsIgnoreCase(charset.group(1))) {
                    page = new String(buf, 0, readCount, "cp1251");
                }
                if ("koi8-r".equalsIgnoreCase(charset.group(1))) {
                    page = new String(buf, 0, readCount, "koi8");
                }
            }
            if (page.isEmpty()) {
                page = new String(buf, 0, readCount, "utf-8");
            }
            Matcher title = Pattern.compile("<title>(.+?)</title>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL).matcher(page.toString());
            if (title.find()) {
                String titleStr = title.group(1).replaceAll("\r|\n", "");
                if (titleStr.length() > 200) {
                    titleStr = titleStr.substring(0, 200) + "...";
                }
                return String.format("Title: %s", titleStr);
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
