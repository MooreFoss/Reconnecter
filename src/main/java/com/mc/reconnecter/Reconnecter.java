package com.mc.reconnecter;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.*;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Plugin(id = "reconnecter", name = "Reconnecter", version = BuildConstants.VERSION, description = "Allow players to join the server they last disconnected from.")
public class Reconnecter {
    private final Logger logger;
    private final ProxyServer proxyServer;
    private final Path dataFile;
    private final Map<UUID, String> lastServerMap = new HashMap<>();

    @Inject
    public Reconnecter(Logger logger, ProxyServer proxyServer, @DataDirectory Path dataDirectory) {
        this.logger = logger;
        this.proxyServer = proxyServer;
        this.dataFile = dataDirectory.resolve("lastserver.dat");
        loadLastServerData();
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        logger.info("Reconnecter plugin has been initialized!");
    }

    @Subscribe
    public void onPlayerLogin(PostLoginEvent event) {
        UUID playerUUID = event.getPlayer().getUniqueId();
        String lastServer = lastServerMap.get(playerUUID);

        if (lastServer != null) {
            proxyServer.getServer(lastServer).ifPresent(registeredServer -> {
                logger.info("Reconnecting player {} to their last server: {}", playerUUID, lastServer);
                proxyServer.getPlayer(playerUUID).ifPresent(player -> player.createConnectionRequest(registeredServer).fireAndForget());
            });
        }
    }

    @Subscribe
    public void onPlayerDisconnect(DisconnectEvent event) {
        Player player = event.getPlayer();
        String currentServer = player.getCurrentServer().map(connection -> connection.getServerInfo().getName()).orElse(null);

        if (currentServer != null) {
            lastServerMap.put(player.getUniqueId(), currentServer);
            saveLastServerData();
        }
    }

    private void loadLastServerData() {
        if (Files.exists(dataFile)) {
            try (BufferedReader reader = new BufferedReader(new FileReader(dataFile.toFile()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length == 2) {
                        lastServerMap.put(UUID.fromString(parts[0]), parts[1]);
                    }
                }
            } catch (Exception e) {
                logger.error("Failed to load last server data", e);
            }
        }
    }

    private void saveLastServerData() {
        try {
            // 确保目录存在
            Files.createDirectories(dataFile.getParent());

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(dataFile.toFile()))) {
                for (Map.Entry<UUID, String> entry : lastServerMap.entrySet()) {
                    writer.write(entry.getKey() + "," + entry.getValue());
                    writer.newLine();
                }
            }
        } catch (Exception e) {
            logger.error("Failed to save last server data", e);
        }
    }
}