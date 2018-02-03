package me.rkfg.xmpp.bot.plugins;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import me.rkfg.xmpp.bot.Utils;
import me.rkfg.xmpp.bot.message.Message;

public class GoogleCommandPlugin extends CommandPlugin {

    @Override
    public String processCommand(Message message, Matcher matcher) {
        return searchString(matcher.group(COMMAND_GROUP));
    }

    protected String searchString(String str) {
        HttpClient client = Utils.getHTTPClient();
        try {
            HttpResponse response = client.execute(new HttpGet(String.format(
                    "http://ajax.googleapis.com/ajax/services/search/web?v=1.0&q=%s&hl=ru", URLEncoder.encode(str, "utf-8"))));
            JSONObject jsonObject = new JSONObject(Utils.readHttpResponse(response));
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
                    .replaceAll("<b>|</b>", ""), URLDecoder.decode(URLDecoder.decode(result.getString("url"), "utf-8"), "utf-8"));
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
