package http;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Query {

    public static Long getLong(String queryString, String key) {
        String v = get(queryString, key);
        if (v == null || v.isBlank()) return null;
        try { return Long.parseLong(v.trim()); }
        catch (Exception e) { return null; }
    }

    public static String get(String queryString, String key) {
        if (queryString == null || queryString.isBlank()) return null;
        Map<String, String> map = parse(queryString);
        return map.get(key);
    }

    private static Map<String, String> parse(String qs) {
        Map<String, String> out = new HashMap<>();
        for (String part : qs.split("&")) {
            String[] kv = part.split("=", 2);
            String k = decode(kv[0]);
            String v = kv.length > 1 ? decode(kv[1]) : "";
            out.put(k, v);
        }
        return out;
    }

    private static String decode(String s) {
        return URLDecoder.decode(s, StandardCharsets.UTF_8);
    }
}