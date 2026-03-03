package org.example.utilities;

public class Response {
    private int status = 200;
    private String contentType = "text/html";

    public void status(int status) { this.status = status; }

    public void type(String contentType) { this.contentType = contentType; }

    public int getStatus() { return status; }

    public String getContentType() { return contentType; }
}
