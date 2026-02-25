package org.example;

import java.net.MalformedURLException;
import java.net.URL;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) throws MalformedURLException {
        URL myurl =  new URL("http://is.escuelaing.edu.co:7654/respuestas/respuesta.txt?val=7&t=3#pubs");
        System.out.println("Protocol:" + myurl.getProtocol());
        System.out.println("Host:" + myurl.getHost());
        System.out.println("Authority:" + myurl.getAuthority());
        System.out.println("Port:" + myurl.getPort());
        System.out.println("Path:" + myurl.getPath());
        System.out.println("Query:" + myurl.getQuery());
        System.out.println("File:" + myurl.getFile());




    }
}