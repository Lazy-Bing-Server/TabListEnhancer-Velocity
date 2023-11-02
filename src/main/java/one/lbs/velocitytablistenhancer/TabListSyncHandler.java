package one.lbs.velocitytablistenhancer;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.proxy.Player;
import one.lbs.velocitytablistenhancer.utils.TabListUtil;

import java.util.concurrent.TimeUnit;

@Singleton
public class TabListSyncHandler {
    @Inject
    public VelocityTabListEnhancer pluginInstance;

    public static void init(VelocityTabListEnhancer pluginInstance) {
        pluginInstance.server.getEventManager().register(pluginInstance, pluginInstance.injector.getInstance(TabListSyncHandler.class));
        pluginInstance.server.getScheduler().buildTask(pluginInstance, pluginInstance.injector.getInstance(TabListSyncHandler.class)::updateTabList)
                .repeat(pluginInstance.config.getTabListUpdateInterval(), TimeUnit.MILLISECONDS).schedule();
    }

    public void updateTabList() {
        TabListUtil.updateTabList(pluginInstance);
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent disconnectEvent) {
        Player targetPlayer = disconnectEvent.getPlayer();
        TabListUtil.updateLastLeftPlayer(targetPlayer);
    }
}
