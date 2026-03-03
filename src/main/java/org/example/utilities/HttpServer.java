package org.example.utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpServer {
    static Map<String, WebMethod> endPoints = new HashMap<>();
    private static String staticFilesLocation = null;

    public static void get(String path, WebMethod handler) {
        endPoints.put(path, handler);
    }

    public static void staticfiles(String folder) {
        staticFilesLocation = folder;
    }

    // Returns raw bytes so binary files (images, fonts…) are never corrupted.
    private static byte[] serveStaticFile(String path, Response res) {
        if (staticFilesLocation == null) return null;
        if (path.equals("/") || path.isEmpty()) path = "/index.html";
        String resourcePath = (staticFilesLocation + path).replaceAll("^/+", "");
        try (InputStream is = HttpServer.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) return null;
            res.type(getContentType(path));
            return is.readAllBytes();
        } catch (IOException e) {
            return null;
        }
    }

    private static String getContentType(String path) {
        if (path.endsWith(".html"))                    return "text/html";
        if (path.endsWith(".css"))                     return "text/css";
        if (path.endsWith(".js"))                      return "application/javascript";
        if (path.endsWith(".json"))                    return "application/json";
        if (path.endsWith(".png"))                     return "image/png";
        if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
        if (path.endsWith(".gif"))                     return "image/gif";
        if (path.endsWith(".svg"))                     return "image/svg+xml";
        if (path.endsWith(".ico"))                     return "image/x-icon";
        if (path.endsWith(".webp"))                    return "image/webp";
        return "application/octet-stream";
    }

    // Builds an HTTP/1.1 response as raw bytes (headers + body).
    private static byte[] buildResponse(int status, String contentType, byte[] body) {
        String headers = "HTTP/1.1 " + status + " OK\r\n"
                + "Content-Type: " + contentType + "\r\n"
                + "Content-Length: " + body.length + "\r\n"
                + "\r\n";
        byte[] headerBytes = headers.getBytes(StandardCharsets.UTF_8);
        byte[] response = new byte[headerBytes.length + body.length];
        System.arraycopy(headerBytes, 0, response, 0, headerBytes.length);
        System.arraycopy(body, 0, response, headerBytes.length, body.length);
        return response;
    }

    public static void main(String[] args) throws IOException, URISyntaxException {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(35000);
        } catch (IOException e) {
            System.err.println("Could not listen on port: 35000.");
            System.exit(1);
        }

        boolean running = true;
        while (running) {
            Socket clientSocket = null;
            try {
                System.out.println("Listo para recibir ...");
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                System.err.println("Accept failed.");
                System.exit(1);
            }

            OutputStream out = clientSocket.getOutputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String inputLine;
            boolean firstLine = true;
            String httpMethod = "GET";
            String reqpath = "/";
            Map<String, String> queryParams = new HashMap<>();
            Map<String, String> headers = new HashMap<>();

            while ((inputLine = in.readLine()) != null) {
                System.out.println("Received: " + inputLine);
                if (firstLine) {
                    String[] tokens = inputLine.split(" ");
                    httpMethod = tokens[0];
                    URI requri = new URI(tokens[1]);
                    reqpath = requri.getPath();
                    queryParams = Request.parseQueryString(requri.getRawQuery());
                    System.out.println("Request path: " + reqpath);
                    firstLine = false;
                } else if (inputLine.contains(": ")) {
                    String[] headerParts = inputLine.split(": ", 2);
                    headers.put(headerParts[0], headerParts[1]);
                }
                if (!in.ready()) break;
            }

            Request req = new Request(httpMethod, reqpath, queryParams, headers);
            Response res = new Response();

            WebMethod wm = endPoints.get(reqpath);
            byte[] response;

            if (wm != null) {
                String body = wm.handle(req, res);
                String html = "<!DOCTYPE html><html>"
                        + "<head><meta charset=\"UTF-8\"><title>MicroFramework</title></head>"
                        + "<body>" + body + "</body></html>";
                response = buildResponse(res.getStatus(), res.getContentType(),
                        html.getBytes(StandardCharsets.UTF_8));
            } else {
                byte[] fileBytes = serveStaticFile(reqpath, res);
                if (fileBytes != null) {
                    response = buildResponse(res.getStatus(), res.getContentType(), fileBytes);
                } else {
                    String html = "<!DOCTYPE html><html>"
                            + "<head><meta charset=\"UTF-8\"><title>MicroFramework</title></head>"
                            + "<body>My Web Site</body></html>";
                    response = buildResponse(200, "text/html",
                            html.getBytes(StandardCharsets.UTF_8));
                }
            }

            out.write(response);
            out.flush();
            out.close();
            in.close();
            clientSocket.close();
        }
        serverSocket.close();
    }
}
