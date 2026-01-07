package net.optifine.http;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class HttpPipelineSender extends Thread {
    private static final Charset ASCII = StandardCharsets.US_ASCII;
    private final HttpPipelineConnection httpPipelineConnection;

    public HttpPipelineSender(HttpPipelineConnection httpPipelineConnection) {
        super("HttpPipelineSender");
        this.httpPipelineConnection = httpPipelineConnection;
    }

    public void run() {
        HttpPipelineRequest httppipelinerequest = null;

        try {
            connect();

            while (!Thread.interrupted()) {
                httppipelinerequest = httpPipelineConnection.getNextRequestSend();
                HttpRequest httprequest = httppipelinerequest.getHttpRequest();
                OutputStream outputstream = httpPipelineConnection.getOutputStream();
                writeRequest(httprequest, outputstream);
                httpPipelineConnection.onRequestSent(httppipelinerequest);
            }
        } catch (InterruptedException var4) {
        } catch (Exception exception) {
            httpPipelineConnection.onExceptionSend(httppipelinerequest, exception);
        }
    }

    private void connect() throws IOException {
        String s = httpPipelineConnection.getHost();
        int i = httpPipelineConnection.getPort();
        Proxy proxy = httpPipelineConnection.getProxy();
        Socket socket = new Socket(proxy);
        socket.connect(new InetSocketAddress(s, i), 5000);
        httpPipelineConnection.setSocket(socket);
    }

    private void writeRequest(HttpRequest req, OutputStream out) throws IOException {
        write(out, req.getMethod() + " " + req.getFile() + " " + req.getHttp() + "\r\n");
        Map<String, String> map = req.getHeaders();

        for (String s : map.keySet()) {
            String s1 = req.getHeaders().get(s);
            write(out, s + ": " + s1 + "\r\n");
        }

        write(out, "\r\n");
    }

    private void write(OutputStream out, String str) throws IOException {
        byte[] abyte = str.getBytes(ASCII);
        out.write(abyte);
    }
}
