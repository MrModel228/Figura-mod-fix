package org.figuramc.figurafix.github;

import org.figuramc.figurafix.FiguraFix;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class SyncManager {
    private static final String AVATARS_DIR = "figura/avatars";
    private static final String CONFIG_FILE = "config/figurafix/config.properties";
    private static final String TOKEN_FILE = "assets/figurafix/token.dat";

    private GitHubAPI gitHubAPI;
    private ModelManager modelManager;
    private final Settings settings;

    public SyncManager(GitHubAPI gitHubAPI, ModelManager modelManager) {
        this.gitHubAPI = gitHubAPI;
        this.modelManager = modelManager;
        this.settings = loadSettings();
    }

    private Settings loadSettings() {
        Settings settings = new Settings();
        Path configPath = Paths.get(CONFIG_FILE);

        if (Files.exists(configPath)) {
            try (BufferedReader reader = new BufferedReader(new FileReader(configPath.toFile()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split("=", 2);
                    if (parts.length == 2) {
                        String key = parts[0].trim();
                        String value = parts[1].trim();
                        
                        switch (key) {
                            case "owner":
                                settings.owner = value;
                                break;
                            case "repo":
                                settings.repo = value;
                                break;
                            case "autoStart":
                                settings.autoStart = Boolean.parseBoolean(value);
                                break;
                            case "autoExit":
                                settings.autoExit = Boolean.parseBoolean(value);
                                break;
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (settings.owner.isEmpty() || settings.repo.isEmpty()) {
            settings.owner = "MrModel228";
            settings.repo = "figura-fix-models";
        }

        return settings;
    }

    public void saveSettings() {
        try {
            Files.createDirectories(Paths.get("config/figurafix"));
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(CONFIG_FILE))) {
                writer.write("owner=" + settings.owner);
                writer.newLine();
                writer.write("repo=" + settings.repo);
                writer.newLine();
                writer.write("autoStart=" + settings.autoStart);
                writer.newLine();
                writer.write("autoExit=" + settings.autoExit);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateGitHubAPI() {
        this.gitHubAPI = new GitHubAPI(getToken(), settings.owner, settings.repo);
        this.modelManager = new ModelManager(gitHubAPI);
    }

    private String getToken() {
        Path tokenPath = Paths.get(TOKEN_FILE);
        if (Files.exists(tokenPath)) {
            try {
                return Files.readString(tokenPath).trim();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "none";
    }

    public void syncAll() {
        FiguraFix.LOGGER.info("[Figura-Fix] Starting sync...");

        File avatarsFolder = modelManager.getAvatarsFolder();
        if (!avatarsFolder.exists()) {
            FiguraFix.LOGGER.info("[Figura-Fix] Avatars folder not found: " + avatarsFolder.getAbsolutePath());
            FiguraFix.LOGGER.info("[Figura-Fix] Sync complete!");
            return;
        }

        List<ModelManager.PlayerModels> localPlayers = modelManager.scanLocalAvatars();
        FiguraFix.LOGGER.info("[Figura-Fix] Found " + localPlayers.size() + " players with avatars");

        for (ModelManager.PlayerModels player : localPlayers) {
            String playerName = player.getPlayerName();
            List<ModelManager.AvatarModel> models = player.getModels();
            FiguraFix.LOGGER.info("[Figura-Fix] Processing player: " + playerName + " with " + models.size() + " avatars");

            for (ModelManager.AvatarModel model : models) {
                String avatarName = model.getName();
                String remotePath = "avatars/" + playerName + "/" + avatarName + "/model.json";

                try {
                    if (!gitHubAPI.fileExists(remotePath)) {
                        modelManager.uploadAvatar(playerName, avatarName, model.getPath());
                        FiguraFix.LOGGER.info("[Figura-Fix] Uploaded: " + playerName + "/" + avatarName);
                    } else {
                        FiguraFix.LOGGER.debug("[Figura-Fix] Already exists: " + playerName + "/" + avatarName);
                    }
                } catch (Exception e) {
                    FiguraFix.LOGGER.error("[Figura-Fix] Failed to check/upload " + playerName + "/" + avatarName + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }

        FiguraFix.LOGGER.info("[Figura-Fix] Sync complete!");
    }

    public void downloadFromGitHub() {
        FiguraFix.LOGGER.info("[Figura-Fix] Downloading avatars from GitHub...");

        try {
            Map<String, String> players = gitHubAPI.listDirectory("avatars");

            for (Map.Entry<String, String> playerEntry : players.entrySet()) {
                String playerName = playerEntry.getKey();
                String playerSha = playerEntry.getValue();

                File avatarsFolder = modelManager.getAvatarsFolder();
                Path playerDir = avatarsFolder.toPath().resolve(playerName);
                Files.createDirectories(playerDir);

                Map<String, String> avatars = gitHubAPI.listDirectory("avatars/" + playerName);

                for (Map.Entry<String, String> avatarEntry : avatars.entrySet()) {
                    String avatarName = avatarEntry.getKey();
                    String avatarSha = avatarEntry.getValue();

                    Path avatarDir = playerDir.resolve(avatarName);
                    Files.createDirectories(avatarDir);

                    String remotePath = "avatars/" + playerName + "/" + avatarName + "/model.json";
                    String content = gitHubAPI.getFileContent(remotePath);

                    if (content != null) {
                        String compressedContent = modelManager.compressJson(content);
                        Files.writeString(playerDir.resolve(avatarName).resolve("model.json"), compressedContent);
                        FiguraFix.LOGGER.info("[Figura-Fix] Downloaded: " + playerName + "/" + avatarName);
                    }
                }
            }
        } catch (IOException e) {
            FiguraFix.LOGGER.error("[Figura-Fix] Download failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean isAutoStart() {
        return settings.autoStart;
    }

    public boolean isAutoExit() {
        return settings.autoExit;
    }

    public void setAutoStart(boolean value) {
        settings.autoStart = value;
        saveSettings();
    }

    public void setAutoExit(boolean value) {
        settings.autoExit = value;
        saveSettings();
    }

    public String getOwner() {
        return settings.owner;
    }

    public String getRepo() {
        return settings.repo;
    }

    public void setOwner(String owner) {
        this.settings.owner = owner;
        saveSettings();
        updateGitHubAPI();
    }

    public void setRepo(String repo) {
        this.settings.repo = repo;
        saveSettings();
        updateGitHubAPI();
    }

    private static class Settings {
        String owner = "";
        String repo = "";
        boolean autoStart = true;
        boolean autoExit = true;
    }
}
