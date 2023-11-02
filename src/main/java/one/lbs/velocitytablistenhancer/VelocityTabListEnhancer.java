package one.lbs.velocitytablistenhancer;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import one.lbs.velocitytablistenhancer.config.Config;
import org.slf4j.Logger;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

@Plugin(id = "velocitytablistenhancer", name = "TabListEnhancer-Velocity", version = "0.1.0-SNAPSHOT",
        url = "https://github.com/Lazy-Bing-Server/TablistEnhancer-Velocity", description = "Add some tab list enhancement to velocity", authors = {"Ra1ny_Yuki"})
@Singleton
public class VelocityTabListEnhancer {

    @Inject
    public ProxyServer server;
    @Inject
    public Logger logger;
    public Config config = new Config();
    @Inject
    @DataDirectory
    public Path dataDirectory;
    @Inject
    public Injector injector;
    private static VelocityTabListEnhancer instance;

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        instance = this;
        if (!this.prepareConfig())
        {
            this.logger.error("Failed to prepare config, the plugin will not work");
            return;
        }
        if (config.isEnabled()) {
            TabListSyncHandler.init(this);
        }
    }

    private boolean prepareConfig() {
        if (!this.dataDirectory.toFile().exists() && !this.dataDirectory.toFile().mkdir()) {
            this.logger.error("Create data directory failed");
            return false;
        }

        File file = this.dataDirectory.resolve("config.yml").toFile();

        if (!file.exists()) {
            try (InputStream in = this.getClass().getClassLoader().getResourceAsStream("config.yml")) {
                Files.copy(Objects.requireNonNull(in), file.toPath());
            } catch (Exception e) {
                this.logger.error("Generate default config failed", e);
                return false;
            }
        }

        try {
            this.config.load(Files.readString(file.toPath()));
        } catch (Exception e) {
            this.logger.error("Read config failed", e);
            return false;
        }

        return true;
    }

    public static VelocityTabListEnhancer getInstance() {
        return Objects.requireNonNull(instance);
    }
}
