package me.rkfg.xmpp.bot.plugins;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;

import me.rkfg.xmpp.bot.Utils;
import me.rkfg.xmpp.bot.message.Message;
import ru.ppsrk.gwt.client.ClientAuthenticationException;
import ru.ppsrk.gwt.client.LogicException;

public class BehindComputerPlugin extends CommandPlugin {

    @Override
    public String processCommand(Message message, Matcher matcher) throws ClientAuthenticationException, LogicException {
        String from = message.getFrom();
        if (!from.matches("^.+?@behind\\.computer/.*$")) {
            return "с этого адреса нельзя общаться с Закомповьем.";
        }
        from = from.replaceAll("@behind\\.computer/.*$", "").replace("_", "-");
        String text = matcher.group(3);
        if (text.length() > 1024) {
            return "максимальная длина текста — 1024 символа.";
        }
        String passwd = getSettingsManager().getStringSetting("bcpasswd");
        try {
            HttpPost req = new HttpPost(new URIBuilder("http://behind.computer/post").build());
            List<NameValuePair> params = new LinkedList<NameValuePair>();
            params.add(new BasicNameValuePair("passwd", passwd));
            params.add(new BasicNameValuePair("from", from));
            params.add(new BasicNameValuePair("text", text));
            HttpEntity entity = new UrlEncodedFormEntity(params, "utf-8");
            req.setEntity(entity);
            HttpResponse resp = Utils.getHTTPClient().execute(req);
            if (resp.getStatusLine().getStatusCode() == 200) {
                return "отправлено.";
            }
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return "ошибка отправки.";
    }

    @Override
    public List<String> getCommand() {
        return Arrays.asList("bc", "zk", "ис", "ял");
    }

    @Override
    public String getManual() {
        return "отправить весточку в Закомповье. Возможно только с адресов @behind.computer, сообщение должно быть отправлено боту на JID\n"
                + "Формат: <текст>\n" + "Пример: " + PREFIX + "bc это сообщение будет на моей страничке в Закомповье.";
    }

}
