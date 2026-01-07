package net.optifine.http;

import java.util.Map;

public class HttpResponse {
    private final int status;
    private final Map<String, String> headers;
    private final byte[] body;

    public HttpResponse(int status, String statusLine, Map<String, String> headers, byte[] body) {
        this.status = status;
        this.headers = headers;
        this.body = body;
    }

    public int getStatus() {
        return status;
    }

    public String getHeader(String key) {
        return headers.get(key);
    }

    public byte[] getBody() {
        return body;
    }
}
