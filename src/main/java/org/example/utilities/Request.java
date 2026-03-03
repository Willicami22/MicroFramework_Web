package org.example.utilities;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Request {
    private final String method;
    private final String path;
    private final Map<String, String> queryParams;
    private final Map<String, String> headers;

    public Request(String method, String path, Map<String, String> queryParams, Map<String, String> headers) {
        this.method = method;
        this.path = path;
        this.queryParams = Collections.unmodifiableMap(queryParams);
        this.headers = Collections.unmodifiableMap(headers);
    }

    public String getMethod() { return method; }

    public String getPath() { return path; }

    public String getQueryParam(String key) { return queryParams.getOrDefault(key, ""); }

    public String getValues(String key) { return queryParams.getOrDefault(key, ""); }

    public Map<String, String> getQueryParams() { return queryParams; }

    public String getHeader(String name) { return headers.getOrDefault(name, ""); }

    static Map<String, String> parseQueryString(String query) {
        Map<String, String> params = new HashMap<>();
        if (query == null || query.isEmpty()) return params;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) params.put(kv[0], kv[1]);
            else if (kv.length == 1) params.put(kv[0], "");
        }
        return params;
    }
}
