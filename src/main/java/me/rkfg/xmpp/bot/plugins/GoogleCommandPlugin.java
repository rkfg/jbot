package me.rkfg.xmpp.bot.plugins;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;

import me.rkfg.xmpp.bot.Utils;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.jivesoftware.smack.packet.Message;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GoogleCommandPlugin extends CommandPlugin {

    @Override
    public String processCommand(Message message, Matcher matcher) {
        HttpClient client = Utils.getHTTPClient();
        try {
            HttpResponse response = client.execute(new HttpGet(String.format(
                    "http://ajax.googleapis.com/ajax/services/search/web?v=1.0&q=%s&hl=ru", URLEncoder.encode(matcher.group(2), "utf-8"))));
            StringBuilder builder = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            JSONObject jsonObject = new JSONObject(builder.toString());
            if (jsonObject.getInt("responseStatus") != 200) {
                String details = jsonObject.getString("responseDetails");
                if (details != null) {
                    return "ошибка запроса: \"" + details + "\"";
                }
            }
            JSONArray results = jsonObject.getJSONObject("responseData").getJSONArray("results");
            if (results.length() == 0) {
                return "ничего не найдено!";
            }
            JSONObject result = results.getJSONObject(0);
            return String.format("%s\n%s\n%s", result.getString("titleNoFormatting"), result.getString("content")
                    .replaceAll("<b>|</b>", ""), URLDecoder.decode(result.getString("url"), "utf-8"));
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JSONException e) {
            return "некорректные данные от гугла (заабузили сервис?)";
        }
        return null;
    }

    @Override
    public List<String> getCommand() {
        return Arrays.asList("g", "п");
    }

    @Override
    public String getManual() {
        return "выдать первый результат из гугла.\n" + "Формат: <текст запроса>\n" + "Пример: " + PREFIX + "g тест";
    }
}
