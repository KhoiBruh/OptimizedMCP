package net.optifine.http;

import java.net.Proxy;
import java.util.Map;

public class HttpRequest {
    private final String host;
    private final int port;
    private final Proxy proxy;
    private final String method;
    private final String file;
    private final String http;
    private final Map<String, String> headers;
    private int redirects = 0;

    public HttpRequest(String host, int port, Proxy proxy, String method, String file, String http, Map<String, String> headers, byte[] body) {
        this.host = host;
        this.port = port;
        this.proxy = proxy;
        this.method = method;
        this.file = file;
        this.http = http;
        this.headers = headers;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getMethod() {
        return method;
    }

    public String getFile() {
        return file;
    }

    public String getHttp() {
        return http;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public int getRedirects() {
        return redirects;
    }

    public void setRedirects(int redirects) {
        this.redirects = redirects;
    }

    public Proxy getProxy() {
        return proxy;
    }
}
