package com.primevalrpg.primeval.core.Inventory.PlayerMenu;

import com.primevalrpg.primeval.core.Inventory.MobGUI.Core.BaseMenu;
import com.primevalrpg.primeval.core.Player.PlayerData;
import com.primevalrpg.primeval.core.Player.PlayerDataManager;
import com.primevalrpg.primeval.core.enums.ElementType;
import com.primevalrpg.primeval.utils.Desing.ColourCode;
import com.primevalrpg.primeval.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class ElementMenu extends BaseMenu {
    private static final int ROWS = 6, COLS = 9, SIZE = ROWS * COLS;
    private static final int[] SNAKE = {
            4*COLS+1,3*COLS+1,2*COLS+1,1*COLS+1,
            1*COLS+3,2*COLS+3,3*COLS+3,4*COLS+3,
            4*COLS+5,3*COLS+5,2*COLS+5,1*COLS+5,
            1*COLS+7,2*COLS+7,3*COLS+7,4*COLS+7
    };
    private static final int PAGE_SIZE = SNAKE.length;
    private static final int MAX_LEVEL = PlayerData.MAX_LEVEL;
    private final int totalPages = (int)Math.ceil((double)MAX_LEVEL/PAGE_SIZE);
    private int page = 0;

    private final ElementType type;
    private PlayerData data;

    public ElementMenu(ElementType type) {
        super(SIZE, ColourCode.colour("&3&l" + type.name() + " Levels"));
        this.type = type;
    }

    @Override
    public void open(Player player) {
        this.data = PlayerDataManager.getInstance()
                .getPlayerData(player.getUniqueId());
        page = 0;
        render(player);
        super.open(player);
    }

    private void render(Player player) {
        int currentLvl = data.getElementLevel(type);
        int currentXp  = data.getElementXp(type);

        int startLevel = page * PAGE_SIZE;
        for (int i = 0; i < PAGE_SIZE; i++) {
            int slot     = SNAKE[i];
            int levelNum = startLevel + i + 1;

            if (levelNum > MAX_LEVEL) {
                getInventory().setItem(slot,
                        ItemBuilder.CreateCustomItem(Material.BLACK_STAINED_GLASS_PANE,false,"","")
                );
                continue;
            }

            boolean achieved = currentLvl >= levelNum;
            int needed  = data.getXpToNext(type, levelNum);
            int shownXp;

            if (levelNum < currentLvl) {
                shownXp = needed;
            } else if (levelNum == currentLvl) {
                shownXp = currentXp;
            } else {
                shownXp = 0;
            }

            getInventory().setItem(slot,
                    ItemBuilder.CreateMultiLoreItem(
                            achieved
                                    ? Material.LIME_STAINED_GLASS_PANE
                                    : Material.GRAY_STAINED_GLASS_PANE,
                            achieved,
                            ColourCode.colour((achieved ? "&a" : "&7") + "Level " + levelNum),
                            ColourCode.colour("&eXP: " + shownXp + "/" + needed)
                    )
            );
        }

        if (page > 0) {
            getInventory().setItem(51,
                    ItemBuilder.CreateCustomItem(
                            Material.ARROW, false,
                            ColourCode.colour("&ePrevious Page"),
                            ColourCode.colour("&7Page " + page + " of " + totalPages)
                    )
            );
        }

        if (page < totalPages - 1) {
            getInventory().setItem(52,
                    ItemBuilder.CreateCustomItem(
                            Material.ARROW, false,
                            ColourCode.colour("&eNext Page"),
                            ColourCode.colour("&7Page " + (page + 2) + " of " + totalPages)
                    )
            );
        }

        getInventory().setItem(SIZE - 1,
                ItemBuilder.CreateCustomItem(
                        Material.BARRIER, false,
                        ColourCode.colour("&cBack"),
                        ColourCode.colour("&7Return to element list")
                )
        );
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        e.setCancelled(true);
        Player p = (Player)e.getWhoClicked();
        int slot = e.getSlot();

        if (slot == SIZE - 1) {
            new PlayerCoreMenu().open(p);
            return;
        }
        if (slot == 51 && page > 0) {
            page--; render(p);
            return;
        }
        if (slot == 52 && page < totalPages - 1) {
            page++; render(p);
            return;
        }
        for (int i = 0; i < PAGE_SIZE; i++) {
            if (slot == SNAKE[i]) {
                int levelNum = page * PAGE_SIZE + i + 1;
                if (levelNum <= data.getElementLevel(type)) {
                    p.sendMessage(ColourCode.colour(
                            "&aSelected " + type.name() + " Level " + levelNum));
                }
                return;
            }
        }
    }
}