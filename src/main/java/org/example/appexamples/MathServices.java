package org.example.appexamples;
import org.example.utilities.HttpServer;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.example.utilities.HttpServer.get;
import static org.example.utilities.HttpServer.staticfiles;
public class MathServices {
    public static void main(String[] args) throws IOException, URISyntaxException {
        staticfiles("/webroot/public");
        get("/pi", (req, res) -> "PI=" + Math.PI);
        get("/Hello", (req, res) -> "Hello " + req.getValues("name"));
        get("/euler", (req, res) -> euler());

        HttpServer.main(args);
    }

    private static String euler() {
        return  "e= " + Math.E;
    }
}
