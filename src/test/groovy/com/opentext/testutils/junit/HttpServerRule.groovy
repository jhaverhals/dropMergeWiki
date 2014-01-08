package com.opentext.testutils.junit

import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.handler.AbstractHandler
import org.junit.rules.ExternalResource

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import static javax.servlet.http.HttpServletResponse.SC_OK

public class HttpServerRule extends ExternalResource {
    private Server generatedServer
    private final Map<String, byte[]> pathMapping = new HashMap<>()

    public URI getURI() {
        return generatedServer.getURI()
    }

    @Override
    public void before() throws Exception {
        generatedServer = new Server(0)
        generatedServer.handler = new AbstractHandler() {
            @Override
            public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
                if (pathMapping.containsKey(target)) {
                    response.status = SC_OK
                    response.contentType = "text/xml;charset=utf-8"
                    response.outputStream << pathMapping.get(target)
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

    public void addResponseForPath(String path, byte[] response) {
        pathMapping.put(path, response)
    }
}
