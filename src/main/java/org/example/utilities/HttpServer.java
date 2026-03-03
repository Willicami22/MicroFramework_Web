package org.example.utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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

    private static String serveStaticFile(String path, Response res) {
        if (staticFilesLocation == null) return null;
        if (path.equals("/") || path.isEmpty()) path = "/index.html";
        String resourcePath = (staticFilesLocation + path).replaceAll("^/+", "");
        try (InputStream is = HttpServer.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) return null;
            res.type(getContentType(path));
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return null;
        }
    }

    private static String getContentType(String path) {
        if (path.endsWith(".html")) return "text/html";
        if (path.endsWith(".css"))  return "text/css";
        if (path.endsWith(".js"))   return "application/javascript";
        if (path.endsWith(".json")) return "application/json";
        return "text/plain";
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

            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
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
            String outputLine;

            if (wm != null) {
                String body = wm.handle(req, res);
                outputLine = "HTTP/1.1 " + res.getStatus() + " OK\r\n"
                        + "Content-Type: " + res.getContentType() + "\r\n"
                        + "\r\n"
                        + "<!DOCTYPE html>"
                        + "<html>"
                        + "<head><meta charset=\"UTF-8\"><title>MicroFramework</title></head>"
                        + "<body>" + body + "</body>"
                        + "</html>";
            } else {
                String fileContent = serveStaticFile(reqpath, res);
                if (fileContent != null) {
                    outputLine = "HTTP/1.1 " + res.getStatus() + " OK\r\n"
                            + "Content-Type: " + res.getContentType() + "\r\n"
                            + "\r\n"
                            + fileContent;
                } else {
                    outputLine = "HTTP/1.1 200 OK\r\n"
                            + "Content-Type: text/html\r\n"
                            + "\r\n"
                            + "<!DOCTYPE html>"
                            + "<html>"
                            + "<head><meta charset=\"UTF-8\"><title>MicroFramework</title></head>"
                            + "<body>My Web Site</body>"
                            + "</html>";
                }
            }

            out.println(outputLine);
            out.close();
            in.close();
            clientSocket.close();
        }
        serverSocket.close();
    }
}