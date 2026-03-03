package org.example.utilities;

@FunctionalInterface
public interface WebMethod {
    String handle(Request req, Response res);
}
