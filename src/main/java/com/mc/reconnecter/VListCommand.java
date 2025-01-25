package com.mc.reconnecter;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class VListCommand implements SimpleCommand {
    private final ProxyServer proxyServer;

    public VListCommand(ProxyServer proxyServer) {
        this.proxyServer = proxyServer;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        Component message = Component.text("Online players:\n", NamedTextColor.GRAY);
        int playerCount = 0;
        for (Player player : proxyServer.getAllPlayers()) {
            String serverName = player.getCurrentServer().map(serverConnection -> serverConnection.getServerInfo().getName()).orElse("Unknown");
            message = message.append(Component.text(player.getUsername(), NamedTextColor.YELLOW))
                    .append(Component.text(" - ", NamedTextColor.WHITE))
                    .append(Component.text(serverName, NamedTextColor.GREEN))
                    .append(Component.text("\n", NamedTextColor.WHITE));
            playerCount++;
        }
        message = message.append(Component.text("Total players: " + playerCount, NamedTextColor.GRAY));
        source.sendMessage(message);
    }
}