package com.primevalrpg.primeval.core.Inventory.Core;

import com.primevalrpg.primeval.core.Inventory.MobGUI.Core.BaseMenu;
import org.bukkit.entity.Player;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MenuHistory {
    private static final Map<UUID, Deque<BaseMenu>> HISTORY = new ConcurrentHashMap<>();

    /** Opens a menu and records it as the current top of stack. */
    public static void open(Player p, BaseMenu menu) {
        Deque<BaseMenu> stack = HISTORY.computeIfAbsent(p.getUniqueId(),
                id -> new ArrayDeque<>());
        stack.push(menu);
        menu.open(p);
    }

    /** Closes current menu and re-opens the previous one, or just closes if none. */
    public static void goBack(Player p) {
        Deque<BaseMenu> stack = HISTORY.get(p.getUniqueId());
        if (stack == null || stack.size() <= 1) {
            // no previous menu ⇒ just close
            HISTORY.remove(p.getUniqueId());
            p.closeInventory();
            return;
        }
        // pop current
        stack.pop();
        // reopen previous
        stack.peek().open(p);
    }

    public static void clear(Player p) {
        HISTORY.remove(p.getUniqueId());
    }

}
