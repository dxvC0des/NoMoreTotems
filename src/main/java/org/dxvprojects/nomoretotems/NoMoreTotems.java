package org.dxvprojects.nomoretotems;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class NoMoreTotems implements ModInitializer {

    private static final int SCAN_INTERVAL_TICKS = 2; // scan every 2 ticks (~0.1s)
    private final AtomicInteger tickCounter = new AtomicInteger(0);

    @Override
    public void onInitialize() {

        // Tick-based scan: inventories, hands, armor, ender chests
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (tickCounter.incrementAndGet() < SCAN_INTERVAL_TICKS) return;
            tickCounter.set(0);

            List<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList();
            for (ServerPlayerEntity player : players) {
                clearPlayerTotems(player);
            }
        });
    }

    private static void clearPlayerTotems(ServerPlayerEntity player) {
        try {
            Inventory inv = player.getInventory();
            if (inv != null) removeTotemsFromInventory(inv);

            Inventory ender = player.getEnderChestInventory();
            if (ender != null) removeTotemsFromInventory(ender);

            // Clear hands
            if (player.getMainHandStack().isOf(Items.TOTEM_OF_UNDYING))
                player.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
            if (player.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING))
                player.setStackInHand(Hand.OFF_HAND, ItemStack.EMPTY);

            // Clear armor
            EquipmentSlot[] armorSlots = {
                    EquipmentSlot.HEAD,
                    EquipmentSlot.CHEST,
                    EquipmentSlot.LEGS,
                    EquipmentSlot.FEET
            };

            for (EquipmentSlot slot : armorSlots) {
                ItemStack armor = player.getEquippedStack(slot);
                if (armor.isOf(Items.TOTEM_OF_UNDYING)) {
                    player.equipStack(slot, ItemStack.EMPTY);
                }
            }
        } catch (Throwable ignored) {}
    }

    private static void removeTotemsFromInventory(Inventory inv) {
        if (inv == null) return;
        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getStack(i);
            if (stack != null && !stack.isEmpty() && stack.isOf(Items.TOTEM_OF_UNDYING)) {
                inv.setStack(i, ItemStack.EMPTY);
            }
        }
    }
}
