package com.opentext.testutils.junit

import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.handler.AbstractHandler
import org.junit.rules.ExternalResource

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import static javax.servlet.http.HttpServletResponse.SC_OK

public class HttpServerRule extends ExternalResource {
    private Server generatedServer = new Server(0)
    private final Map<String, byte[]> pathMapping = new HashMap<>()
    private int invocationCounter = 0

    public URI getURI() {
        return generatedServer.getURI()
    }

    public int getInvocationCount() {
        return invocationCounter
    }

    @Override
    public void before() throws Exception {
        generatedServer.handler = new AbstractHandler() {
            @Override
            public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
                invocationCounter++
                String normalizedTarget = normalizePath "$target${request.queryString ? "?$request.queryString" : ''}"
                if (pathMapping.containsKey(normalizedTarget)) {
                    println normalizedTarget
                    response.status = SC_OK
                    response.contentType = 'application/json;charset=utf-8'
                    response.outputStream << pathMapping.get(normalizedTarget)
                    baseRequest.handled = true
                }
            }
        }
        generatedServer.start()
    }

    @Override
    public void after() {
        try {
            generatedServer.stop()
        }
        catch (Exception e) {
        }
    }

    private String normalizePath(String p) {
        p.dropWhile { it == '/' }
    }

    public void addResponseForPath(String path, byte[] response) {
        pathMapping.put(normalizePath(path), response)
    }

    public void addJSONResponseForPath(String path, groovy.json.JsonBuilder jsonBuilder) {
        new ByteArrayOutputStream().with { baos ->
            new OutputStreamWriter(baos).with { osw -> jsonBuilder.writeTo osw; flush() }
            addResponseForPath(path, baos.toByteArray())
        }
    }
}
