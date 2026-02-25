package org.example.utilities;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class HttpServer {
    static Map<String, WebMethod> endPoints = new HashMap<>();
    public static void main(String[] args) throws IOException, URISyntaxException {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(35000);
        } catch (IOException e) {
            System.err.println("Could not listen on port: 35000.");
            System.exit(1); }
        Socket clientSocket = null;

        boolean running = true;
        while(running) {
            try {
                System.out.println("Listo para recibir ...");
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                System.err.println("Accept failed.");
                System.exit(1);
            }

            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            clientSocket.getInputStream()));
            String inputLine, outputLine;

            boolean firstLine = true;
            String reqpath = " ";

            while ((inputLine = in.readLine()) != null) {
                System.out.println("Received: " + inputLine);
                if(firstLine){
                    String[] reqTokens = inputLine.split(" ");
                    String method = reqTokens[0];
                    String struri = reqTokens[1];
                    String protocol = reqTokens[2];

                    URI requri = new URI(struri);

                    reqpath = requri.getPath();

                    System.out.println("Request path: " + reqpath);

                    firstLine = false;
                }
                if (!in.ready()) {
                    break;
                }
            }

            WebMethod wm = endPoints.get(reqpath);
            System.out.println(wm);

            if(wm != null){
                outputLine =
                        "HTTP/1.1 200 OK\n"
                                + "content-Type: text/html\n\r"
                                + "\n\r"
                                + "<!DOCTYPE html>"
                                + "<html>"
                                + "<head>"
                                + "<meta charset=\"UTF-8\">"
                                + "<title>Title of the document</title>\n"
                                + "</head>"
                                + "<body>"
                                + wm.execute()
                                + "</body>"
                                + "</html>" + inputLine;
                out.println(outputLine);
            }
            else {
                outputLine =
                        "HTTP/1.1 200 OK\n"
                                + "content-Type: text/html\n\r"
                                + "\n\r"
                                + "<!DOCTYPE html>"
                                + "<html>"
                                + "<head>"
                                + "<meta charset=\"UTF-8\">"
                                + "<title>Title of the document</title>\n"
                                + "</head>"
                                + "<body>"
                                + "My Web Site"
                                + "</body>"
                                + "</html>" + inputLine;
                out.println(outputLine);
            }

            out.close();
            in.close();
            clientSocket.close();
        }
        serverSocket.close();
    }
    public static void get(String path, WebMethod web){
        endPoints.put(path, web);
    }
}