package org.dxvprojects.nomoretotems;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.client.particle.TotemParticle;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.inventory.Inventory;
import net.minecraft.util.math.Box;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class NoMoreTotems implements ModInitializer {
    private static final int SCAN_INTERVAL_TICKS = 20 * 5; // every 5 seconds
    private final AtomicInteger tickCounter = new AtomicInteger(0);

    @Override
    public void onInitialize() {
        // Periodic scan: remove totems from player inventories (and ender chest)
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (tickCounter.incrementAndGet() < SCAN_INTERVAL_TICKS) return;
            tickCounter.set(0);

            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                try {
                    removeTotemsFromInventory(player.getInventory());
                    try {
                        Inventory ender = player.getEnderChestInventory();
                        if (ender != null) removeTotemsFromInventory(ender);
                    } catch (Throwable ignored) {}
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        });

        // After-death cleanup: remove totems dropped near the dead entity
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            try {
                if (!(entity.getWorld() instanceof ServerWorld world)) return;
                Box box = entity.getBoundingBox().expand(3.0);
                List<ItemEntity> items = world.getEntitiesByClass(
                        ItemEntity.class,
                        box,
                        ie -> {
                            ItemStack s = ie.getStack();
                            return s != null && !s.isEmpty() && s.isOf(Items.TOTEM_OF_UNDYING);
                        }
                );
                for (ItemEntity ie : items) {
                    ie.discard();
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private static void removeTotemsFromInventory(Inventory inv) {
        if (inv == null) return;
        for (int i = 0; i < inv.size(); i++) {
            try {
                ItemStack s = inv.getStack(i);
                if (s != null && !s.isEmpty() && s.isOf(Items.TOTEM_OF_UNDYING)) {
                    inv.setStack(i, ItemStack.EMPTY);
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }
}
