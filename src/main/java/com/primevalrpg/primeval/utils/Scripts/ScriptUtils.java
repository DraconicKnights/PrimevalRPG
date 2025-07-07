package com.primevalrpg.primeval.utils.Scripts;

import com.primevalrpg.primeval.PrimevalRPG;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ScriptUtils {
    private static DefaultExecutors executors = DefaultExecutors.getShared();

    /**
     * Parse & run a full script in one go.
     */
    public static void execScript(Collection<String> lines,
                                  LivingEntity self,
                                  Collection<LivingEntity> nearbyAll,
                                  Event event) {
        List<ScriptCommand> cmds = lines.stream()
                .map(ScriptParser::parseLine)
                .collect(Collectors.toList());

        ScriptContext ctx = new ScriptContext(
                cmds,
                self,
                nearbyAll,
                event,
                executors,
                PrimevalRPG.getInstance()
        );

        ctx.run();
    }

    /**
     * Varargs overload for convenience.
     */
    public static void execScript(LivingEntity self,
                                  Collection<LivingEntity> nearbyAll,
                                  Event event,
                                  String... lines) {
        execScript(Arrays.asList(lines), self, nearbyAll, event);
    }

    /**
     * Evaluate a boolean‐expression string (same syntax as globalEvents.yml).
     * Supports:
     *   - && via “and” (case‐insensitive)
     *   - ! (negation)
     *   - =, ==, !=, <, >, <=, >=
     *   - literal numbers & quoted strings
     *   - simple predicates (sneaking, onGround, event:<EventName>)
     *   - a few built‐in properties (self.health, self.maxHealth, player.name, event.action)
     */
    public static boolean evaluateCondition(String cond,
                                            LivingEntity self,
                                            Player player,
                                            Event event) {
        if (cond == null || cond.isBlank()) return true;

        // split on logical AND
        String[] clauses = cond.split("(?i)\\band\\b");
        for (String raw : clauses) {
            String clause = raw.trim();
            boolean neg = false;
            if (clause.startsWith("!")) {
                neg = true;
                clause = clause.substring(1).trim();
            }
            boolean result = evaluateSingle(clause, self, player, event);
            if (neg) result = !result;
            if (!result) return false;
        }
        return true;
    }

    private static boolean evaluateSingle(String clause,
                                          LivingEntity self,
                                          Player player,
                                          Event event) {
        String op = null;
        for (String cand : Arrays.asList("==", "!=", ">=", "<=", "=", ">", "<")) {
            if (clause.contains(cand)) { op = cand; break; }
        }

        if (op == null) {
            return evalPredicate(clause, self, player, event);
        }

        String[] parts = clause.split(op, 2);
        String left  = parts[0].trim();
        String right = parts[1].trim();

        String lval = resolve(left, self, player, event);
        String rval = resolve(right, self, player, event);

        Double ln = parseNum(lval), rn = parseNum(rval);
        if (ln != null && rn != null) {
            switch (op) {
                case "=": case "==": return ln.equals(rn);
                case "!=":          return !ln.equals(rn);
                case ">":  return ln > rn;
                case "<":  return ln < rn;
                case ">=": return ln >= rn;
                case "<=": return ln <= rn;
            }
        }

        switch (op) {
            case "=": case "==": return lval.equalsIgnoreCase(rval);
            case "!=":          return !lval.equalsIgnoreCase(rval);
        }
        return false;
    }

    private static boolean evalPredicate(String clause,
                                         LivingEntity self,
                                         Player player,
                                         Event event) {
        String c = clause.toLowerCase();

        if (c.toLowerCase().startsWith("action:") && event instanceof PlayerInteractEvent pie) {
            String want = c.substring("action:".length()).trim();
            return pie.getAction().name().equalsIgnoreCase(want);
        }

        switch (c) {
            case "sneaking":   return player.isSneaking();
            case "onground":   return player.isOnGround();
        }
        if (c.startsWith("event:")) {
            String want = clause.substring("event:".length()).trim();
            return event.getClass().getSimpleName()
                    .equalsIgnoreCase(want);
        }

        if (c.toLowerCase().startsWith("item:")
                && event instanceof PlayerInteractEvent pie)
        {
            String wantMat = c.substring("item:".length()).trim();
            ItemStack inHand = pie.getItem();
            return inHand != null
                    && inHand.getType().name().equalsIgnoreCase(wantMat);
        }

        if (c.toLowerCase().startsWith("itemmeta:")
                && event instanceof PlayerInteractEvent pie2)
        {
            String payload = c.substring("itemMeta:".length()).trim();
            String[] parts = payload.split("=",2);
            if (parts.length == 2) {
                String metaKey = parts[0].trim();
                String wantVal = parts[1].trim();
                ItemStack inHand = pie2.getItem();
                if (inHand != null && inHand.hasItemMeta()) {
                    ItemMeta im = inHand.getItemMeta();
                    // lookup your plugin by name:
                    NamespacedKey nk = new NamespacedKey(
                            PrimevalRPG.getInstance(),
                            metaKey);
                    String actual = im.getPersistentDataContainer()
                            .get(nk, PersistentDataType.STRING);
                    return wantVal.equals(actual);
                }
            }
            return false;
        }

        return false;
    }

    private static String resolve(String token,
                                  LivingEntity self,
                                  Player player,
                                  Event event) {
        String t = token.trim();
        if ("self.health".equalsIgnoreCase(t))    return String.valueOf(self.getHealth());
        if ("self.maxhealth".equalsIgnoreCase(t)) return String.valueOf(self.getMaxHealth());
        if ("player.name".equalsIgnoreCase(t)
                || "self.name".equalsIgnoreCase(t))      return player.getName();
        if ("event.action".equalsIgnoreCase(t)
                && event instanceof org.bukkit.event.player.PlayerInteractEvent) {
            return ((org.bukkit.event.player.PlayerInteractEvent) event)
                    .getAction().name();
        }

        if ((t.startsWith("\"") && t.endsWith("\""))
                || (t.startsWith("'") && t.endsWith("'"))) {
            return t.substring(1, t.length() - 1);
        }
        return t;
    }

    private static Double parseNum(String s) {
        try {
            return Double.valueOf(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }


}
