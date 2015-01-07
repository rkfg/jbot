package me.rkfg.xmpp.bot;

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
}
