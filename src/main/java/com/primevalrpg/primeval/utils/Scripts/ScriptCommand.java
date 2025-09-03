package com.primevalrpg.primeval.utils.Scripts;

import java.util.List;
import java.util.Map;

/**
 * One parsed line of your DSL:
 * name = “fireball”, args = {direction→forward, speed→1.2}, targets = ["@entity"]
 */
public class ScriptCommand {
    public final String name;
    public final Map<String, String> args;
    public final List<String> targets;
    public final String triggerEvent;

    public ScriptCommand(String name,
                         Map<String, String> args,
                         List<String> targets,
                         String triggerEvent
    ) {
        this.name    = name;
        this.args    = args;
        this.targets = targets;
        this.triggerEvent = triggerEvent;

    }

    /**
     * The root token of the command line, e.g. for "flagSet name=foo…"
     * this returns "flagSet".
     */
    public String getName() {
        return name;
    }

    /** Check for presence of an argument key */
    public boolean hasArg(String key) {
        return args.containsKey(key);
    }

    /** Get raw argument or null if missing */
    public String getArg(String key) {
        return args.get(key);
    }

    /** Get argument or default if missing */
    public String getArg(String key, String defaultValue) {
        return args.getOrDefault(key, defaultValue);
    }

    /** Parse a double argument, falling back to default on error or missing */
    public double getDouble(String key, double defaultValue) {
        String val = args.get(key);
        if (val == null) return defaultValue;
        try {
            return Double.parseDouble(val);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    public int getInt(String key, int defaultValue) {
        String val = args.get(key);
        if (val == null) return defaultValue;
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException ex) {

        }
        return defaultValue;
    }

    /**
     * Any named arguments you parsed, e.g. "name"→"foo", etc.
     */
    public Map<String,String> getArgs() {
        return args;
    }
}

