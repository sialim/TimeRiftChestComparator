package me.sialim.timeriftchestcomparator;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class TimeRiftChestComparator extends JavaPlugin implements Listener, CommandExecutor {
    private Map<UUID, Boolean> toggleMap = new HashMap<>();

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        toggleMap.clear();
    }

    @Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("toggledupecheck")) {
            if (sender instanceof Player p) {
                UUID pUUID = p.getUniqueId();
                boolean newValue = !toggleMap.getOrDefault(pUUID, false);
                toggleMap.put(pUUID, newValue);
                p.sendMessage("Dupe checker " + (newValue ? "enabled" : "disabled") + "!");
                return true;
            }
        }
        return false;
    }

    @EventHandler public void onInventoryOpen(InventoryOpenEvent e) {
        if (!(e.getPlayer() instanceof Player p)) return;
        UUID pUUID = p.getUniqueId();
        if (!toggleMap.getOrDefault(pUUID, false)) return;

        ItemStack[] pInventory = p.getInventory().getContents();
        ItemStack[] cInventory = e.getInventory().getContents();

        Map<ItemStack, Integer> itemCount = new HashMap<>();

        for (ItemStack item : pInventory) {
            if (item != null) {
                itemCount.merge(item, item.getAmount(), Integer::sum);
            }
        }

        for (ItemStack item : cInventory) {
            if (item != null) {
                itemCount.merge(item, item.getAmount(), Integer::sum);
            }
        }

        for (Map.Entry<ItemStack, Integer> entry : itemCount.entrySet()) {
            if (entry.getValue() > 1) {
                ItemStack itemStack = entry.getKey();
                ItemMeta itemMeta = itemStack.getItemMeta();
                String itemName = itemMeta != null && itemMeta.hasDisplayName() ? itemMeta.getDisplayName() : itemStack.getType().name();
                p.sendMessage(ChatColor.RED + "Duplicate detected: " + itemName + " " + entry.getValue() + "x");
            }
        }
    }
}
