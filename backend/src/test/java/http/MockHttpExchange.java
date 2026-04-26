package http;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;

public abstract class MockHttpExchange extends HttpExchange {

    private final Headers requestHeaders = new Headers();
    private final Headers responseHeaders = new Headers();
    private final String method;
    private final URI uri;

    private InputStream requestBody = InputStream.nullInputStream();
    private final ByteArrayOutputStream responseBody = new ByteArrayOutputStream();

    private int responseCode = -1;

    public MockHttpExchange(String method, String path) {
        this.method = method;
        this.uri = URI.create("http://localhost" + path);
    }

    // Allow setting request body
    public void setRequestBody(InputStream body) {
        this.requestBody = body;
    }

    // Allow adding headers
    public void addRequestHeader(String key, String value) {
        requestHeaders.add(key, value);
    }

    // Allow asserting response code
    public void assertResponseCode(int expected) {
        if (expected != responseCode) {
            throw new AssertionError("Expected " + expected + " but got " + responseCode);
        }
    }

    // Allow reading response body
    public String getResponseBodyString() {
        return responseBody.toString(StandardCharsets.UTF_8);
    }

    @Override
    public Headers getRequestHeaders() {
        return requestHeaders;
    }

    @Override
    public Headers getResponseHeaders() {
        return responseHeaders;
    }

    @Override
    public URI getRequestURI() {
        return uri;
    }

    @Override
    public String getRequestMethod() {
        return method;
    }

    @Override
    public InputStream getRequestBody() {
        return requestBody;
    }

    @Override
    public OutputStream getResponseBody() {
        return responseBody;
    }

    @Override
    public void sendResponseHeaders(int rCode, long responseLength) {
        this.responseCode = rCode;
    }

    @Override
    public void close() {}

    @Override
    public InetSocketAddress getRemoteAddress() {
        return new InetSocketAddress(0);
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return new InetSocketAddress(0);
    }

    @Override
    public String getProtocol() {
        return "HTTP/1.1";
    }

    @Override
    public Object getAttribute(String name) { return null; }

    @Override
    public void setAttribute(String name, Object value) {}

    @Override
    public void setStreams(InputStream i, OutputStream o) {}

    @Override
    public com.sun.net.httpserver.HttpContext getHttpContext() {
        return null;
    }
}
