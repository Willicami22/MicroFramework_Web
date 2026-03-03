package org.example.utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class URLReader {
    public static void main(String[] args) {
        String site = (args != null && args.length > 0) ? args[0] : "http://www.google.com/";

        try {
            URL siteURL = URI.create(site).toURL();
            URLConnection urlConnection = siteURL.openConnection();

            Map<String, List<String>> headers = urlConnection.getHeaderFields();
            Set<Map.Entry<String, List<String>>> entrySet = headers.entrySet();

            for (Map.Entry<String, List<String>> entry : entrySet) {
                String headerName = entry.getKey();
                if (headerName != null) {
                    System.out.print(headerName + ": ");
                }
                List<String> headerValues = entry.getValue();
                for (String value : headerValues) {
                    System.out.print(value + " ");
                }
                System.out.println();
            }

            System.out.println("\n-------message-body------");
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(urlConnection.getInputStream()))) {
                String inputLine;
                while ((inputLine = reader.readLine()) != null) {
                    System.out.println(inputLine);
                }
            }
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("No se pudo leer la URL '" + site + "': " + e.getMessage());
        }
    }
}