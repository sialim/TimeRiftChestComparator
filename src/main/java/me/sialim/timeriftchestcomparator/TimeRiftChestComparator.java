package me.sialim.timeriftchestcomparator;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class TimeRiftChestComparator extends JavaPlugin implements Listener, CommandExecutor {
    private Map<UUID, ToggleSettings> toggleMap = new HashMap<>();

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        toggleMap.clear();
    }

    @Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("itemchecker")) {
            if (sender instanceof Player p) {
                UUID pUUID = p.getUniqueId();
                if (!p.hasPermission("itemchecker.toggle")) {
                    p.sendMessage(ChatColor.RED + "You do not have permission to use itemchecker.toggle");
                    return true;
                }
                ToggleSettings settings = toggleMap.getOrDefault(pUUID, new ToggleSettings());

                if (args.length > 0 && args[0].equalsIgnoreCase("inventory")) {
                    settings.setCheckInventory(!settings.isCheckInventory());
                    p.sendMessage(ChatColor.GREEN + "Inventory dupe check: " + (settings.isCheckInventory() ? "enabled" : "disabled") + "!");
                } else {
                    settings.setCheckContainer(!settings.isCheckContainer());
                    p.sendMessage(ChatColor.GREEN + "Inventory dupe check: " + (settings.isCheckInventory() ? "enabled" : "disabled") + "!");
                }

                toggleMap.put(pUUID, settings);
                return true;
            }
        }
        return false;
    }

    @EventHandler public void onInventoryOpen(InventoryOpenEvent e) {
        if (!(e.getPlayer() instanceof Player p)) return;
        UUID pUUID = p.getUniqueId();
        ToggleSettings settings = toggleMap.getOrDefault(pUUID, new ToggleSettings());

        if (!settings.isCheckContainer() || !settings.isCheckInventory()) return;

        Inventory openedInventory = e.getInventory();
        Map<ItemStack, Integer> itemCount = new HashMap<>();

        if (settings.isCheckInventory() && openedInventory.equals(p.getInventory())) {
            checkInventoryForDuplicates(p.getInventory().getContents(), itemCount);
        } else if (settings.isCheckContainer() && !openedInventory.equals(p.getInventory())) {
            checkInventoryForDuplicates(openedInventory.getContents(), itemCount);
        }

        for (Map.Entry<ItemStack, Integer> entry : itemCount.entrySet()) {
            if (entry.getValue() > 1) {
                ItemStack itemStack = entry.getKey();
                ItemMeta itemMeta = itemStack.getItemMeta();
                String itemName = itemMeta != null && itemMeta.hasDisplayName() ? itemMeta.getDisplayName() : itemStack.getType().name();
                p.sendMessage(ChatColor.RED + "Duplicate Relic: '" + itemName + "' (" + entry.getValue() + "x Copies Found)");
            }
        }
    }

    private void addItemToCountMap(Map<ItemStack, Integer> itemCount, ItemStack item) {
        boolean found = false;
        for (ItemStack key : itemCount.keySet()) {
            if (key.isSimilar(item)) {
                itemCount.put(key, itemCount.get(key) + item.getAmount());
                found = true;
                break;
            }
        }
        if (!found) {
            itemCount.put(item, item.getAmount());
        }
    }

    private void checkInventoryForDuplicates(ItemStack[] inventory, Map<ItemStack, Integer> itemCount) {
        for (ItemStack item : inventory) {
            if (item != null) {
                addItemToCountMap(itemCount, item);
                if (item.getType() == Material.SHULKER_BOX || item.getType() == Material.WHITE_SHULKER_BOX || item.getType() == Material.ORANGE_SHULKER_BOX ||
                        item.getType() == Material.MAGENTA_SHULKER_BOX || item.getType() == Material.LIGHT_BLUE_SHULKER_BOX || item.getType() == Material.YELLOW_SHULKER_BOX ||
                        item.getType() == Material.LIME_SHULKER_BOX || item.getType() == Material.PINK_SHULKER_BOX || item.getType() == Material.GRAY_SHULKER_BOX ||
                        item.getType() == Material.LIGHT_GRAY_SHULKER_BOX || item.getType() == Material.CYAN_SHULKER_BOX || item.getType() == Material.PURPLE_SHULKER_BOX ||
                        item.getType() == Material.BLUE_SHULKER_BOX || item.getType() == Material.BROWN_SHULKER_BOX || item.getType() == Material.GREEN_SHULKER_BOX ||
                        item.getType() == Material.RED_SHULKER_BOX || item.getType() == Material.BLACK_SHULKER_BOX) {
                    BlockStateMeta meta = (BlockStateMeta) item.getItemMeta();
                    if (meta != null && meta.getBlockState() instanceof ShulkerBox) {
                        ShulkerBox shulker = (ShulkerBox) meta.getBlockState();
                        checkInventoryForDuplicates(shulker.getInventory().getContents(), itemCount);
                    }
                }

            }
        }
    }

    private static class ToggleSettings {
        private boolean checkContainer = false;
        private boolean checkInventory = false;

        public boolean isCheckContainer() {
            return checkContainer;
        }

        public void setCheckContainer(boolean checkContainer) {
            this.checkContainer = checkContainer;
        }

        public boolean isCheckInventory() {
            return checkInventory;
        }

        public void setCheckInventory(boolean checkInventory) {
            this.checkInventory = checkInventory;
        }
    }
}
