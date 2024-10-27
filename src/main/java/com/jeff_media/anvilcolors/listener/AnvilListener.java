package com.jeff_media.anvilcolors.listener;

import com.jeff_media.anvilcolors.AnvilColors;
import com.jeff_media.anvilcolors.data.RenameResult;
import com.jeff_media.anvilcolors.utils.Formatter;
import com.jeff_media.anvilcolors.utils.VersionUtils;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Objects;

public class AnvilListener implements Listener {

    private final AnvilColors plugin;
    private final Formatter formatter;

    public AnvilListener(AnvilColors plugin) {
        this.plugin = plugin;
        this.formatter = new Formatter(plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAnvilRename(PrepareAnvilEvent event) {

        List<HumanEntity> viewers = event.getViewers();
        if (viewers.size() != 1) return;
        HumanEntity humanEntity = viewers.get(0);
        if (!(humanEntity instanceof Player)) return;

        Player player = (Player) humanEntity;
        ItemStack item = event.getResult();

        if (item == null) return;
        if (!item.hasItemMeta()) return;
        ItemMeta meta = Objects.requireNonNull(item.getItemMeta());
        if (!meta.hasDisplayName()) return;

        final String originalName = meta.getDisplayName();
        String displayName = originalName;

        RenameResult result = formatter.colorize(player, displayName, plugin.getItalicsMode());
        if (result.getReplacedColorsCount() == 0) return;

        displayName = result.getColoredName();

        if(VersionUtils.hasAnvilRepairCostSupport()) {
            int cost = plugin.getConfig().getInt("level-cost");
            int costMultiplier = plugin.getConfig().getBoolean("cost-per-color") ? result.getReplacedColorsCount() : 1;
            int totalCost = cost * costMultiplier;

            plugin.debug("Cost: " + cost);
            plugin.debug("Cost multiplier: " + costMultiplier);
            plugin.debug("Total cost: " + totalCost);
            plugin.debug("Repair cost: " + event.getInventory().getRepairCost());
            plugin.debug("Colors: " + result.getReplacedColorsCount());

            if (totalCost > 0) {
                AnvilInventory inv = event.getInventory();
                inv.setRepairCost(totalCost + inv.getRepairCost());
            }
        }

        meta.setDisplayName(displayName);
        item.setItemMeta(meta);
        event.setResult(item);
    }
}
