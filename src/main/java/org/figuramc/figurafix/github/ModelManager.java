package org.figuramc.figurafix.github;

import java.io.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.*;

public class ModelManager {
    private static final String AVATARS_DIR = "config/figura/avatars";
    private final GitHubAPI gitHubAPI;
    private final ConcurrentHashMap<String, String> fileHashes = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private volatile boolean watchRunning = true;

    public ModelManager(GitHubAPI gitHubAPI) {
        this.gitHubAPI = gitHubAPI;
        startFileWatcher();
    }

    public void stopWatcher() {
        watchRunning = false;
        scheduler.shutdown();
    }

    private void startFileWatcher() {
        scheduler.scheduleAtFixedRate(() -> {
            if (!watchRunning) return;
            try {
                checkAndSyncChanges();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 5, 5, TimeUnit.SECONDS);
    }

    private void checkAndSyncChanges() {
        Path avatarsPath = Paths.get(AVATARS_DIR);
        if (!Files.exists(avatarsPath)) return;

        try {
            Files.walk(avatarsPath)
                    .filter(p -> p.toString().endsWith("model.json"))
                    .forEach(path -> {
                        try {
                            String content = Files.readString(path);
                            String currentHash = calculateHash(content);
                            String pathStr = path.toString();
                            String oldHash = fileHashes.get(pathStr);

                            if (oldHash == null) {
                                fileHashes.put(pathStr, currentHash);
                                uploadNewAvatar(path);
                            } else if (!oldHash.equals(currentHash)) {
                                fileHashes.put(pathStr, currentHash);
                                uploadModifiedAvatar(path);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void uploadNewAvatar(Path path) {
        try {
            String content = Files.readString(path);
            String compressedContent = compressJson(content);
            
            Path playerPath = path.getParent().getParent();
            String playerName = playerPath.getFileName().toString();
            String avatarName = path.getParent().getFileName().toString();
            String remotePath = "avatars/" + playerName + "/" + avatarName + "/model.json";

            gitHubAPI.uploadFile(remotePath, compressedContent, 
                    "Upload new avatar " + avatarName + " for " + playerName);
            
            updateLocalHash(path, compressedContent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void uploadModifiedAvatar(Path path) {
        try {
            String content = Files.readString(path);
            String compressedContent = compressJson(content);
            
            Path playerPath = path.getParent().getParent();
            String playerName = playerPath.getFileName().toString();
            String avatarName = path.getParent().getFileName().toString();
            String remotePath = "avatars/" + playerName + "/" + avatarName + "/model.json";

            gitHubAPI.uploadFile(remotePath, compressedContent, 
                    "Update avatar " + avatarName + " for " + playerName);
            
            updateLocalHash(path, compressedContent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateLocalHash(Path path, String content) {
        String pathStr = path.toString();
        String newHash = calculateHash(content);
        fileHashes.put(pathStr, newHash);
    }

    public List<PlayerModels> scanLocalAvatars() {
        List<PlayerModels> players = new ArrayList<>();
        Path avatarsPath = Paths.get(AVATARS_DIR);

        if (!Files.exists(avatarsPath)) {
            return players;
        }

        try {
            Files.list(avatarsPath)
                    .filter(Files::isDirectory)
                    .forEach(playerDir -> {
                        String playerName = playerDir.getFileName().toString();
                        List<AvatarModel> models = new ArrayList<>();

                        try {
                            Files.list(playerDir)
                                    .filter(Files::isDirectory)
                                    .forEach(avatarDir -> {
                                        String avatarName = avatarDir.getFileName().toString();
                                        String modelPath = playerDir + "/" + avatarName + "/model.json";
                                        
                                        if (Files.exists(Paths.get(modelPath))) {
                                            try {
                                                String content = Files.readString(Paths.get(modelPath));
                                                String hash = calculateHash(content);
                                                long size = Files.size(Paths.get(modelPath));
                                                long lastModified = Files.getLastModifiedTime(Paths.get(modelPath)).toMillis();

                                                models.add(new AvatarModel(
                                                        avatarName,
                                                        modelPath,
                                                        hash,
                                                        size,
                                                        lastModified
                                                ));
                                                fileHashes.put(modelPath, hash);
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        players.add(new PlayerModels(playerName, models));
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return players;
    }

    public void uploadAvatar(String playerName, String avatarName, String modelPath) {
        try {
            String content = Files.readString(Paths.get(modelPath));
            String compressedContent = compressJson(content);
            String remotePath = "avatars/" + playerName + "/" + avatarName + "/model.json";

            gitHubAPI.uploadFile(remotePath, compressedContent, 
                    "Upload avatar " + avatarName + " for " + playerName);
            
            updateLocalHash(Paths.get(modelPath), compressedContent);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void downloadAvatar(String playerName, String avatarName) {
        try {
            String remotePath = "avatars/" + playerName + "/" + avatarName + "/model.json";
            String content = gitHubAPI.getFileContent(remotePath);

            if (content != null) {
                Path localDir = Paths.get(AVATARS_DIR, playerName, avatarName);
                Files.createDirectories(localDir);

                String compressedContent = compressJson(content);
                Files.writeString(localDir.resolve("model.json"), compressedContent);
                
                updateLocalHash(localDir.resolve("model.json"), compressedContent);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String compressJson(String json) {
        StringBuilder compressed = new StringBuilder();
        boolean inString = false;
        boolean skipSpace = true;

        for (char c : json.toCharArray()) {
            switch (c) {
                case ' ':
                    if (inString) {
                        compressed.append(c);
                        skipSpace = false;
                    }
                    break;
                case '\n':
                case '\r':
                case '\t':
                    if (inString) {
                        compressed.append(c);
                    }
                    break;
                case '"':
                    inString = !inString;
                    compressed.append(c);
                    skipSpace = true;
                    break;
                default:
                    if (!skipSpace || inString) {
                        compressed.append(c);
                    }
                    skipSpace = false;
            }
        }

        return compressed.toString();
    }

    private String calculateHash(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return "";
        }
    }

    public static class PlayerModels {
        private final String playerName;
        private final List<AvatarModel> models;

        public PlayerModels(String playerName, List<AvatarModel> models) {
            this.playerName = playerName;
            this.models = models;
        }

        public String getPlayerName() { return playerName; }
        public List<AvatarModel> getModels() { return models; }
    }

    public static class AvatarModel {
        private final String name;
        private final String path;
        private final String hash;
        private final long size;
        private final long lastModified;

        public AvatarModel(String name, String path, String hash, long size, long lastModified) {
            this.name = name;
            this.path = path;
            this.hash = hash;
            this.size = size;
            this.lastModified = lastModified;
        }

        public String getName() { return name; }
        public String getPath() { return path; }
        public String getHash() { return hash; }
        public long getSize() { return size; }
        public long getLastModified() { return lastModified; }
    }
}
