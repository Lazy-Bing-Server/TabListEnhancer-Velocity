package one.lbs.velocitytablistenhancer.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class TextUtil {
    public static Component getServerNameComponent(String serverName) {
        HoverEvent<Component> hoverEvent = HoverEvent.showText(getServerNameComponentWithoutEvent(serverName));
        return getServerNameComponentWithoutEvent(serverName).hoverEvent(hoverEvent)
                .clickEvent(ClickEvent.suggestCommand("/server " + serverName));
    }

    public static Component getServerNameComponentWithoutEvent(String serverName) {
        return Component.text(serverName).color(NamedTextColor.GOLD).decoration(TextDecoration.BOLD, true);
    }

    public static Component getUsernameComponentWithoutEvent(String username) {
        return Component.text(username).color(NamedTextColor.GREEN);
    }
}

