package org.example.appexamples;
import org.example.utilities.HttpServer;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.example.utilities.HttpServer.get;
public class MathServices {
    public static void main(String[] args) throws IOException, URISyntaxException {
        get("/pi", () -> "PI=" + Math.PI);
        get("/Hello", () -> "Hello World");
        get("/euler",() -> euler());

        HttpServer.main(args);
    }

    private static String euler() {
        return  "e= " + Math.E;
    }
}
