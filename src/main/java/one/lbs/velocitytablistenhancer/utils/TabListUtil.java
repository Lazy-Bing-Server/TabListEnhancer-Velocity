package one.lbs.velocitytablistenhancer.utils;

import one.lbs.velocitytablistenhancer.VelocityTabListEnhancer;
import one.lbs.velocitytablistenhancer.utils.TextUtil;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.player.TabList;
import com.velocitypowered.api.proxy.player.TabListEntry;
import net.kyori.adventure.text.Component;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;

public class TabListUtil {
    public static final HashSet<Player> lastLeftPlayers = new HashSet<>();

    public static void updateLastLeftPlayer(Player player) {
        synchronized (lastLeftPlayers){
            lastLeftPlayers.add(player);
        }
    }

    public static TabListEntry getTabListEntry(TabList tabList, Player player) {
        return TabListEntry.builder()
                .tabList(tabList)
                .profile(player.getGameProfile())
                .displayName(getRegularDisplayName(player))
                .latency(Long.valueOf(player.getPing()).intValue())
                .gameMode(0)
                .build();
    }

    public static Component getServerDisplayName(Player player, ServerConnection currentServer) {
        return TextUtil.getUsernameComponentWithoutEvent(player.getGameProfile().getName())
                .append(Component.text(" ["))
                .append(TextUtil.getServerNameComponent(currentServer.getServerInfo().getName()))
                .append(Component.text(']'));
    }

    public static Component getRegularDisplayName(Player player) {
        return Component.text(player.getGameProfile().getName());
    }

    public static void updateTabListEntry(TabListEntry tabListEntry, Player player, Player itemPlayer) {
        Component component = null;
        if (tabListEntry.getDisplayNameComponent().isPresent()) {
            component = tabListEntry.getDisplayNameComponent().get();
        }

        if (inSameServer(player, itemPlayer)) {
            if (!Objects.equals(component, getRegularDisplayName(itemPlayer))) {
                tabListEntry.setDisplayName(getRegularDisplayName(itemPlayer));
            }
        } else {
            itemPlayer.getCurrentServer().ifPresent(
                    serverConnection -> tabListEntry.setDisplayName(getServerDisplayName(itemPlayer, serverConnection))
            );
        }

        int latency = Long.valueOf(itemPlayer.getPing()).intValue();
        if (tabListEntry.getLatency() != latency && latency != -1) {
            tabListEntry.setLatency(latency);
        }

        if (!inSameServer(player, itemPlayer)) {
            if (tabListEntry.getGameMode() != 0) {
                tabListEntry.setGameMode(0);
            }
        }
    }

    private static boolean inSameServer(Player player1, Player player2) {
        if (player1.getCurrentServer().isEmpty()) {
            return false;
        }
        String server1 = player1.getCurrentServer().get().getServerInfo().getName();
        if (player2.getCurrentServer().isEmpty()) {
            return false;
        }
        String server2 = player2.getCurrentServer().get().getServerInfo().getName();
        return server1.equals(server2);
    }

    // Yuki note: execute from plugin event instead of packet 2023.2.18
    public static void updateTabList(VelocityTabListEnhancer instance) {
        for (Player toPlayer : instance.server.getAllPlayers()) {
            TabList tabList = toPlayer.getTabList();
            // add missing tabListEntry
            for (Player entryPlayer : instance.server.getAllPlayers()) {
                // ignore player themselves
                if (!entryPlayer.getGameProfile().getName().equals(toPlayer.getGameProfile().getName())) {

                    boolean shouldAdd = true;
                    for (TabListEntry tabListEntry : tabList.getEntries()) {
                        if (tabListEntry.getProfile().getName().equals(entryPlayer.getGameProfile().getName())) {
                            shouldAdd = false;
                            break;
                        }
                    }
                    if (shouldAdd) {
                        tabList.addEntry(TabListUtil.getTabListEntry(tabList, entryPlayer));
                    }
                }
            }
            synchronized (lastLeftPlayers){    // 更新 tabListEntry
                for (TabListEntry tabListEntry : tabList.getEntries()) {
                    String itemPlayerName = tabListEntry.getProfile().getName();
                    Optional<Player> optionalPlayer = instance.server.getPlayer(itemPlayerName);
                    boolean itemShouldBeRemoved = false;
                    for (Player leftPlayer : lastLeftPlayers) {
                        if (Objects.equals(leftPlayer.getGameProfile().getName(), itemPlayerName)) {
                            itemShouldBeRemoved = true;
                            lastLeftPlayers.remove(leftPlayer);
                        }
                    }
                    if (optionalPlayer.isPresent()) {
                        Player itemPlayer = optionalPlayer.get();
                        updateTabListEntry(tabListEntry, toPlayer, itemPlayer);
                    } else if (itemShouldBeRemoved) {
                        tabList.removeEntry(tabListEntry.getProfile().getId());
                    }
                    lastLeftPlayers.clear();
                }
            }
        }
    }
}
