package net.optifine.http;

import net.minecraft.src.Config;

import java.io.*;
import java.net.Proxy;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

public class HttpPipelineConnection {
    private static final Pattern patternFullUrl = Pattern.compile("^[a-zA-Z]+://.*");
    private String host;
    private int port;
    private Proxy proxy;
    private final List<HttpPipelineRequest> listRequests;
    private final List<HttpPipelineRequest> listRequestsSend;
    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private HttpPipelineSender httpPipelineSender;
    private HttpPipelineReceiver httpPipelineReceiver;
    private int countRequests;
    private boolean responseReceived;
    private long keepaliveTimeoutMs;
    private int keepaliveMaxCount;
    private long timeLastActivityMs;
    private boolean terminated;

    public HttpPipelineConnection(String host, int port, Proxy proxy) {
        this.host = null;
        this.port = 0;
        this.proxy = Proxy.NO_PROXY;
        listRequests = new LinkedList<>();
        listRequestsSend = new LinkedList<>();
        socket = null;
        inputStream = null;
        outputStream = null;
        httpPipelineSender = null;
        httpPipelineReceiver = null;
        countRequests = 0;
        responseReceived = false;
        keepaliveTimeoutMs = 5000L;
        keepaliveMaxCount = 1000;
        timeLastActivityMs = System.currentTimeMillis();
        terminated = false;
        this.host = host;
        this.port = port;
        this.proxy = proxy;
        httpPipelineSender = new HttpPipelineSender(this);
        httpPipelineSender.start();
        httpPipelineReceiver = new HttpPipelineReceiver(this);
        httpPipelineReceiver.start();
    }

    public synchronized boolean addRequest(HttpPipelineRequest pr) {
        if (isClosed()) {
            return false;
        } else {
            addRequest(pr, listRequests);
            addRequest(pr, listRequestsSend);
            ++countRequests;
            return true;
        }
    }

    private void addRequest(HttpPipelineRequest pr, List<HttpPipelineRequest> list) {
        list.add(pr);
        notifyAll();
    }

    public synchronized void setSocket(Socket s) throws IOException {
        if (!terminated) {
            if (socket != null) {
                throw new IllegalArgumentException("Already connected");
            } else {
                socket = s;
                socket.setTcpNoDelay(true);
                inputStream = socket.getInputStream();
                outputStream = new BufferedOutputStream(socket.getOutputStream());
                onActivity();
                notifyAll();
            }
        }
    }

    public synchronized OutputStream getOutputStream() throws InterruptedException {
        while (outputStream == null) {
            checkTimeout();
            wait(1000L);
        }

        return outputStream;
    }

    public synchronized InputStream getInputStream() throws InterruptedException {
        while (inputStream == null) {
            checkTimeout();
            wait(1000L);
        }

        return inputStream;
    }

    public synchronized HttpPipelineRequest getNextRequestSend() throws InterruptedException, IOException {
        if (listRequestsSend.isEmpty() && outputStream != null) {
            outputStream.flush();
        }

        return getNextRequest(listRequestsSend, true);
    }

    public synchronized HttpPipelineRequest getNextRequestReceive() throws InterruptedException {
        return getNextRequest(listRequests, false);
    }

    private HttpPipelineRequest getNextRequest(List<HttpPipelineRequest> list, boolean remove) throws InterruptedException {
        while (list.isEmpty()) {
            checkTimeout();
            wait(1000L);
        }

        onActivity();

        if (remove) {
            return list.removeFirst();
        } else {
            return list.getFirst();
        }
    }

    private void checkTimeout() {
        if (socket != null) {
            long i = keepaliveTimeoutMs;

            if (!listRequests.isEmpty()) {
                i = 5000L;
            }

            long j = System.currentTimeMillis();

            if (j > timeLastActivityMs + i) {
                terminate(new InterruptedException("Timeout " + i));
            }
        }
    }

    private void onActivity() {
        timeLastActivityMs = System.currentTimeMillis();
    }

    public synchronized void onRequestSent(HttpPipelineRequest pr) {
        if (!terminated) {
            onActivity();
        }
    }

    public synchronized void onResponseReceived(HttpPipelineRequest pr, HttpResponse resp) {
        if (!terminated) {
            responseReceived = true;
            onActivity();

            if (!listRequests.isEmpty() && listRequests.getFirst() == pr) {
                listRequests.removeFirst();
                pr.setClosed(true);
                String s = resp.getHeader("Location");

                if (resp.getStatus() / 100 == 3 && s != null && pr.getHttpRequest().getRedirects() < 5) {
                    try {
                        s = normalizeUrl(s, pr.getHttpRequest());
                        HttpRequest httprequest = HttpPipeline.makeRequest(s, pr.getHttpRequest().getProxy());
                        httprequest.setRedirects(pr.getHttpRequest().getRedirects() + 1);
                        HttpPipelineRequest httppipelinerequest = new HttpPipelineRequest(httprequest, pr.getHttpListener());
                        HttpPipeline.addRequest(httppipelinerequest);
                    } catch (IOException ioexception) {
                        pr.getHttpListener().failed(pr.getHttpRequest(), ioexception);
                    }
                } else {
                    HttpListener httplistener = pr.getHttpListener();
                    httplistener.finished(pr.getHttpRequest(), resp);
                }

                checkResponseHeader(resp);
            } else {
                throw new IllegalArgumentException("Response out of order: " + pr);
            }
        }
    }

    private String normalizeUrl(String url, HttpRequest hr) {
        if (patternFullUrl.matcher(url).matches()) {
            return url;
        } else if (url.startsWith("//")) {
            return "http:" + url;
        } else {
            String s = hr.getHost();

            if (hr.getPort() != 80) {
                s = s + ":" + hr.getPort();
            }

            if (url.startsWith("/")) {
                return "http://" + s + url;
            } else {
                String s1 = hr.getFile();
                int i = s1.lastIndexOf("/");
                return i >= 0 ? "http://" + s + s1.substring(0, i + 1) + url : "http://" + s + "/" + url;
            }
        }
    }

    private void checkResponseHeader(HttpResponse resp) {
        String s = resp.getHeader("Connection");

        if (s != null && !s.equalsIgnoreCase("keep-alive")) {
            terminate(new EOFException("Connection not keep-alive"));
        }

        String s1 = resp.getHeader("Keep-Alive");

        if (s1 != null) {
            String[] astring = Config.tokenize(s1, ",;");

            for (String s2 : astring) {
                String[] astring1 = split(s2, '=');

                if (astring1.length >= 2) {
                    if (astring1[0].equals("timeout")) {
                        int j = Config.parseInt(astring1[1], -1);

                        if (j > 0) {
                            keepaliveTimeoutMs = j * 1000L;
                        }
                    }

                    if (astring1[0].equals("max")) {
                        int k = Config.parseInt(astring1[1], -1);

                        if (k > 0) {
                            keepaliveMaxCount = k;
                        }
                    }
                }
            }
        }
    }

    private String[] split(String str, char separator) {
        int i = str.indexOf(separator);

        if (i < 0) {
            return new String[]{str};
        } else {
            String s = str.substring(0, i);
            String s1 = str.substring(i + 1);
            return new String[]{s, s1};
        }
    }

    public synchronized void onExceptionSend(HttpPipelineRequest pr, Exception e) {
        terminate(e);
    }

    public synchronized void onExceptionReceive(HttpPipelineRequest pr, Exception e) {
        terminate(e);
    }

    private synchronized void terminate(Exception e) {
        if (!terminated) {
            terminated = true;
            terminateRequests(e);

            if (httpPipelineSender != null) {
                httpPipelineSender.interrupt();
            }

            if (httpPipelineReceiver != null) {
                httpPipelineReceiver.interrupt();
            }

            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException var3) {
            }

            socket = null;
            inputStream = null;
            outputStream = null;
        }
    }

    private void terminateRequests(Exception e) {
        if (!listRequests.isEmpty()) {
            if (!responseReceived) {
                HttpPipelineRequest httppipelinerequest = listRequests.removeFirst();
                httppipelinerequest.getHttpListener().failed(httppipelinerequest.getHttpRequest(), e);
                httppipelinerequest.setClosed(true);
            }

            while (!listRequests.isEmpty()) {
                HttpPipelineRequest httppipelinerequest1 = listRequests.removeFirst();
                HttpPipeline.addRequest(httppipelinerequest1);
            }
        }
    }

    public synchronized boolean isClosed() {
        return terminated || countRequests >= keepaliveMaxCount;
    }

    public synchronized boolean hasActiveRequests() {
        return !listRequests.isEmpty();
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public Proxy getProxy() {
        return proxy;
    }
}
