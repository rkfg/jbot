package me.rkfg.xmpp.bot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;

public class Utils {
    private static int TIMEOUT = 10000;

    public static HttpClient getHTTPClient(int timeout) {
        RequestConfig config = RequestConfig.custom().setConnectionRequestTimeout(timeout).setConnectTimeout(timeout)
                .setSocketTimeout(timeout).build();
        return HttpClientBuilder.create().setDefaultRequestConfig(config)
                .setUserAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.63 Safari/537.36")
                .build();
    }

    public static HttpClient getHTTPClient() {
        return getHTTPClient(TIMEOUT);
    }

    public static String readHttpResponse(HttpResponse response) throws IOException {
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }
        reader.close();
        return builder.toString();
    }

}
