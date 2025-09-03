package com.primevalrpg.primeval.utils.API;

import com.primevalrpg.primeval.utils.API.Interface.MobAbility;
import com.primevalrpg.primeval.utils.Data.MobDataHandler;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class AbilityFactory {
    private final Map<String, Template> templates = new HashMap<>();

    public void load() {
        templates.clear();
        ConfigurationSection root = MobDataHandler.getAbilitiesConfig().getConfigurationSection("abilities");
        if (root == null) return;
        for (String key : root.getKeys(false)) {
            ConfigurationSection sec = root.getConfigurationSection(key);
            String clazz     = sec.getString("class");
            long   cd        = sec.getLong("cooldown", 0);
            Map<String,Object> params = new HashMap<>();
            for (String p : sec.getKeys(false)) {
                if (!p.equals("class") && !p.equals("cooldown")) {
                    params.put(p, sec.get(p));
                }
            }
            templates.put(key, new Template(clazz, cd, params));
        }
    }

    public MobAbility create(String key, LivingEntity mob) {
        Template t = templates.get(key);
        if (t == null) throw new IllegalArgumentException("No such ability: "+key);
        try {
            Class<?> c = Class.forName(t.className);
            Constructor<?> ctor = c.getConstructor(LivingEntity.class, Map.class);
            return (MobAbility) ctor.newInstance(mob, t.parameters);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** Return an unmodifiable set of all registered ability keys */
    public Set<String> allKeys() {
        return Collections.unmodifiableSet(templates.keySet());
    }

    public long getCooldown(String key) {
        Template t = templates.get(key);
        return t != null ? t.cooldown : 0;
    }

    private static class Template {
        final String className;
        final long cooldown;
        final Map<String,Object> parameters;
        Template(String c, long cd, Map<String,Object> p) {
            this.className  = c;
            this.cooldown   = cd;
            this.parameters = p;
        }
    }
}