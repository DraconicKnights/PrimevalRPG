package com.primevalrpg.primeval.utils.API;

import com.primevalrpg.primeval.PrimevalRPG;
import com.primevalrpg.primeval.core.enums.LoggerLevel;
import com.primevalrpg.primeval.utils.Logger.RPGLogger;
import com.primevalrpg.primeval.utils.Scripts.DefaultExecutors;
import com.primevalrpg.primeval.utils.Scripts.ScriptCommand;
import com.primevalrpg.primeval.utils.Scripts.ScriptContext;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.function.BiConsumer;

/**
 * A utility class to build and register script commands using a fluent API.
 * ScriptCommandAPI provides an immutable interface for creating command batches.
 */
public final class ScriptCommandAPI {
    /**
     * Private constructor to prevent instantiation of the ScriptCommandAPI class.
     * This class acts as a utility class providing a fluent, immutable interface
     * for building and registering script commands through the nested {@code Builder}.
     */
    private ScriptCommandAPI() {}

    /**
     * Creates a new instance of the {@code Builder} class, allowing
     * the construction and registration of script commands using a
     * fluent API.
     *
     * @return a new {@code Builder} instance to configure and register commands
     */
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        /**
         * A map that associates command names with their corresponding command executors.
         * The keys are case-insensitive and represent the names of the commands, while
         * the values are instances of {@code DefaultExecutors.CommandExecutor} that define
         * the behavior of the respective commands.
         *
         * This map is used within the builder to manage command-to-executor mappings,
         * allowing commands to be added, aliased, removed, and previewed. It is
         * eventually registered to make the commands available for execution.
         */
        private final Map<String, DefaultExecutors.CommandExecutor> map = new HashMap<>();
        private String lastKey;

        /**
         * Start a new command entry, but don’t bind a handler yet.
         * Call execute(...) next to do so.
         */
        public Builder command(String name) {
            lastKey = name.toLowerCase(Locale.ROOT);
            map.put(lastKey, null);
            return this;
        }

        /**
         * As before: alias an existing command.
         * Also advances lastKey so execute() will apply here if you chain.
         */
        public Builder alias(String existing, String alias) {
            DefaultExecutors.CommandExecutor e =
                    map.get(existing.toLowerCase(Locale.ROOT));
            String a = alias.toLowerCase(Locale.ROOT);
            if (e != null) {
                map.put(a, e);
                lastKey = a;
            }
            return this;
        }

        /**
         * Bind your lambda to the most recently declared key (from command(...) or alias(...)).
         */
        public Builder execute(BiConsumer<ScriptCommand, ScriptContext> handler) {
            if (lastKey == null) {
                throw new IllegalStateException("No command() or alias() to attach execute() to");
            }
            DefaultExecutors.CommandExecutor exec = (cmd, ctx) -> handler.accept(cmd, ctx);
            map.put(lastKey, exec);
            return this;
        }

        /**
         * Removes the entry associated with the specified key from the map after converting the key
         * to lowercase using the rules of the default locale.
         *
         * @param name the key of the entry to be removed, where the key is case-insensitively matched.
         * @return the current Builder instance after the removal operation.
         */
        public Builder remove(String name) {
            map.remove(name.toLowerCase(Locale.ROOT));
            return this;
        }

        /**
         * Returns an unmodifiable set of all command names currently registered in the builder.
         *
         * @return a set of command names present in the builder, which cannot be modified.
         */
        public Set<String> preview() {
            return Collections.unmodifiableSet(map.keySet());
        }

        /**
         * Registers all command executors stored in the internal map to the DefaultExecutors.
         * <p>
         * This method iterates over the internal map of command names and their respective
         * command executors and registers each executor using the DefaultExecutors' registerExecutor method.
         */
        public void register() {

            PrimevalRPG plugin = JavaPlugin.getPlugin(PrimevalRPG.class);

            map.forEach((name, exec) -> {
                DefaultExecutors.registerExecutor(name, exec);
                RPGLogger.get().log(LoggerLevel.INFO, "Plugin '" + plugin.getName() + "' is registering command '" + name);
            });
        }
    }
}
