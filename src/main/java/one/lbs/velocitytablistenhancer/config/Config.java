package one.lbs.velocitytablistenhancer.config;

import com.google.common.collect.Maps;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.DumperOptions;

import java.util.Map;

public class Config {
    private final Map<String, Object> options = Maps.newHashMap();

    @SuppressWarnings("unchecked")
    public void load(String yamlContent)
    {
        this.options.clear();
        this.options.putAll(new Yaml().loadAs(yamlContent, this.options.getClass()));
    }

    public boolean isEnabled()
    {
        Object playerCount = this.options.get("enabled");
        if (playerCount instanceof Boolean)
        {
            return (Boolean)playerCount;
        }
        return false;
    }

    public long getTabListUpdateInterval() {
        return (long) this.options.get("tablist-update-interval");
    }
}
