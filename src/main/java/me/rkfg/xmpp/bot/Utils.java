package me.rkfg.xmpp.bot;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

public class Utils {
    public static HttpClient getHTTPClient() {
        return HttpClientBuilder.create()
                .setUserAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.63 Safari/537.36")
                .build();
    }
}
