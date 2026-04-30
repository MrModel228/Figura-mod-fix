package org.figuramc.figurafix.github;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class GitHubAPI {
    private static final String API_BASE = "https://api.github.com";
    private final String authToken;
    private final String owner;
    private final String repo;

    public GitHubAPI(String authToken, String owner, String repo) {
        this.authToken = authToken;
        this.owner = owner;
        this.repo = repo;
    }

    private HttpURLConnection createConnection(String endpoint, String method) throws IOException {
        URL url = new URL(API_BASE + endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setRequestProperty("Authorization", "Bearer " + authToken);
        conn.setRequestProperty("Accept", "application/vnd.github.v3+json");
        conn.setRequestProperty("Content-Type", "application/json");
        return conn;
    }

    private String sendRequest(String endpoint, String method, String body) throws IOException {
        HttpURLConnection conn = createConnection(endpoint, method);
        conn.setDoOutput(body != null);

        if (body != null) {
            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes());
            }
        }

        int responseCode = conn.getResponseCode();
        if (responseCode == 200 || responseCode == 201 || responseCode == 204) {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                return response.toString();
            }
        } else {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getErrorStream()))) {
                StringBuilder error = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    error.append(line);
                }
                throw new IOException("GitHub API error: " + error.toString());
            }
        }
    }

    public String getFileContent(String path) throws IOException {
        String encodedPath = path.replace("/", "%2F");
        String response = sendRequest(
                "/repos/" + owner + "/" + repo + "/contents/" + encodedPath,
                "GET",
                null
        );

        if (response.contains("\"message\"")) {
            return null;
        }

        Map<String, Object> data = parseJson(response);
        String content = (String) data.get("content");
        String encoding = (String) data.get("encoding");

        if ("base64".equals(encoding)) {
            return new String(Base64.getDecoder().decode(content));
        }
        return content;
    }

    public void uploadFile(String path, String content, String commitMessage) throws IOException {
        String encodedPath = path.replace("/", "%2F");
        String response = sendRequest(
                "/repos/" + owner + "/" + repo + "/contents/" + encodedPath,
                "GET",
                null
        );

        String sha = null;
        if (!response.contains("\"message\"")) {
            Map<String, Object> data = parseJson(response);
            sha = (String) data.get("sha");
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("content", Base64.getEncoder().encodeToString(content.getBytes()));
        payload.put("message", commitMessage);

        if (sha != null) {
            payload.put("sha", sha);
        }

        sendRequest(
                "/repos/" + owner + "/" + repo + "/contents/" + encodedPath,
                sha == null ? "PUT" : "PUT",
                serializeJson(payload)
        );
    }

    public boolean fileExists(String path) throws IOException {
        String encodedPath = path.replace("/", "%2F");
        String response = sendRequest(
                "/repos/" + owner + "/" + repo + "/contents/" + encodedPath,
                "GET",
                null
        );
        return !response.contains("\"message\"");
    }

    public Map<String, String> listDirectory(String path) throws IOException {
        String encodedPath = path.isEmpty() ? "" : path.replace("/", "%2F");
        String response = sendRequest(
                "/repos/" + owner + "/" + repo + "/contents/" + encodedPath,
                "GET",
                null
        );

        Map<String, String> files = new HashMap<>();
        String[] items = response.substring(1, response.length() - 1).split("\\},\\{");

        for (String item : items) {
            item = item.trim();
            if (item.startsWith("{")) item = item.substring(1);
            if (item.endsWith("}")) item = item.substring(0, item.length() - 1);

            String name = extractValue(item, "name");
            String type = extractValue(item, "type");

            if ("file".equals(type)) {
                files.put(name, extractValue(item, "sha"));
            }
        }

        return files;
    }

    private Map<String, Object> parseJson(String json) {
        Map<String, Object> map = new HashMap<>();
        String[] pairs = json.substring(1, json.length() - 1).split(",(?=([^\"']*[\"'][^\"']*[\"'])*[^\"']*$)");
        
        for (String pair : pairs) {
            String[] keyValue = pair.split(":", 2);
            if (keyValue.length == 2) {
                String key = keyValue[0].trim().replaceAll("^\"|\"$", "");
                String value = keyValue[1].trim();
                
                if (value.startsWith("\"")) {
                    value = value.replaceAll("^\"|\"$", "");
                }
                
                map.put(key, value);
            }
        }
        
        return map;
    }

    private String serializeJson(Map<String, Object> map) {
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) json.append(",");
            first = false;
            
            json.append("\"").append(entry.getKey()).append("\":\"");
            json.append(entry.getValue().toString().replace("\"", "\\\"")).append("\"");
        }
        
        json.append("}");
        return json.toString();
    }

    private String extractValue(String json, String key) {
        String pattern = "\"" + key + "\":\"";
        int start = json.indexOf(pattern);
        if (start == -1) return "";
        
        start += pattern.length();
        int end = json.indexOf("\"", start);
        return end > start ? json.substring(start, end) : "";
    }

    public String getOwner() {
        return owner;
    }

    public String getRepo() {
        return repo;
    }
}
