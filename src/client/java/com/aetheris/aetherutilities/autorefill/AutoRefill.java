
package com.aetheris.aetherutilities.autorefill;

import com.aetheris.aetherutilities.client.AutoFeaturesConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

public final class AutoRefill {

    private static final int HOTBAR_SIZE = 9;
    private static final int INVENTORY_START = 9;
    private static final int INVENTORY_END = 36;
    private static final int HOTBAR_MENU_OFFSET = 36;

    private static final ItemStack[] previousHotbar =
            new ItemStack[HOTBAR_SIZE];

    private static Player trackedPlayer;
    private static boolean initialized;

    private AutoRefill() {
    }

    public static void initialize() {
        ClientTickEvents.END_CLIENT_TICK.register(
                AutoRefill::onClientTick
        );
    }

    private static void onClientTick(Minecraft client) {

        Player player = client.player;

        if (player == null || client.level == null) {
            reset();
            return;
        }

        Inventory inventory = player.getInventory();

        if (!initialized || trackedPlayer != player) {
            initializeSnapshot(inventory, player);
            return;
        }

        if (client.gui.screen() != null
                || client.gameMode == null
                || !AutoFeaturesConfig.isAutoRefillEnabled()) {

            updateSnapshot(inventory);
            return;
        }

        for (int hotbarSlot = 0;
             hotbarSlot < HOTBAR_SIZE;
             hotbarSlot++) {

            ItemStack previousStack = previousHotbar[hotbarSlot];
            ItemStack currentStack = inventory.getItem(hotbarSlot);

            if (previousStack != null
                    && !previousStack.isEmpty()
                    && currentStack.isEmpty()
                    && previousStack.getItem() instanceof BlockItem) {

                int sourceSlot = refillSlot(
                        client,
                        player,
                        inventory,
                        hotbarSlot,
                        previousStack
                );

                // Update the destination snapshot.
                previousHotbar[hotbarSlot] =
                        inventory.getItem(hotbarSlot).copy();

                // Prevent the source hotbar slot from being
                // interpreted as another depleted stack.
                if (sourceSlot >= 0 && sourceSlot < HOTBAR_SIZE) {
                    previousHotbar[sourceSlot] =
                            inventory.getItem(sourceSlot).copy();
                }

            } else {
                previousHotbar[hotbarSlot] =
                        currentStack.copy();
            }
        }
    }

    private static int refillSlot(
            Minecraft client,
            Player player,
            Inventory inventory,
            int hotbarSlot,
            ItemStack depletedStack
    ) {

        int inventorySlot = findMatchingStack(
                inventory,
                depletedStack,
                hotbarSlot
        );

        if (inventorySlot == -1) {
            return -1;
        }

        AbstractContainerMenu menu = player.inventoryMenu;

        if (menu == null || player.containerMenu != menu) {
            return -1;
        }

        int sourceMenuSlot = getMenuSlotIndex(inventorySlot);
        int targetMenuSlot = hotbarSlot + HOTBAR_MENU_OFFSET;

        if (sourceMenuSlot < 0
                || sourceMenuSlot >= menu.slots.size()
                || targetMenuSlot >= menu.slots.size()) {
            return -1;
        }

        Slot sourceSlot = menu.slots.get(sourceMenuSlot);
        Slot targetSlot = menu.slots.get(targetMenuSlot);

        if (!sourceSlot.hasItem()
                || !targetSlot.getItem().isEmpty()) {
            return -1;
        }

        if (!ItemStack.isSameItemSameComponents(
                sourceSlot.getItem(),
                depletedStack
        )) {
            return -1;
        }

        swapSlots(client, menu, sourceSlot, targetSlot);

        return inventorySlot;
    }

    private static int findMatchingStack(
            Inventory inventory,
            ItemStack reference,
            int excludedHotbarSlot
    ) {

        // First: search the other hotbar slots.
        for (int slot = 0; slot < HOTBAR_SIZE; slot++) {

            if (slot == excludedHotbarSlot) {
                continue;
            }

            if (matches(inventory.getItem(slot), reference)) {
                return slot;
            }
        }

        // Second: search the main inventory.
        for (int slot = INVENTORY_START;
             slot < INVENTORY_END;
             slot++) {

            if (matches(inventory.getItem(slot), reference)) {
                return slot;
            }
        }

        return -1;
    }

    private static boolean matches(
            ItemStack candidate,
            ItemStack reference
    ) {
        return !candidate.isEmpty()
                && ItemStack.isSameItemSameComponents(
                candidate,
                reference
        );
    }

    private static int getMenuSlotIndex(int inventorySlot) {

        if (inventorySlot < HOTBAR_SIZE) {
            return inventorySlot + HOTBAR_MENU_OFFSET;
        }

        return inventorySlot;
    }

    private static void swapSlots(
            Minecraft client,
            AbstractContainerMenu menu,
            Slot first,
            Slot second
    ) {
        if (first == null || second == null || first == second) {
            return;
        }

        click(client, menu, first);
        click(client, menu, second);
        click(client, menu, first);
    }

    private static void click(
            Minecraft client,
            AbstractContainerMenu menu,
            Slot slot
    ) {
        if (client == null
                || client.gameMode == null
                || client.player == null
                || menu == null
                || slot == null) {
            return;
        }

        client.gameMode.handleContainerInput(
                menu.containerId,
                slot.index,
                0,
                ContainerInput.PICKUP,
                client.player
        );
    }

    private static void initializeSnapshot(
            Inventory inventory,
            Player player
    ) {
        trackedPlayer = player;
        updateSnapshot(inventory);
        initialized = true;
    }

    private static void updateSnapshot(Inventory inventory) {

        for (int slot = 0; slot < HOTBAR_SIZE; slot++) {
            previousHotbar[slot] =
                    inventory.getItem(slot).copy();
        }
    }

    private static void reset() {

        trackedPlayer = null;
        initialized = false;

        for (int slot = 0; slot < HOTBAR_SIZE; slot++) {
            previousHotbar[slot] = ItemStack.EMPTY;
        }
    }
}
