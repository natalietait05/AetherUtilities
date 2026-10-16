
package com.aetheris.aetherutilities.autotool;

import com.aetheris.aetherutilities.client.AutoFeaturesConfig;
import com.aetheris.aetherutilities.client.mixin.MinecraftStartAttackInvoker;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public final class AutoTool {

    private static final int HOTBAR_SIZE = 9;
    private static final int INVENTORY_START = 9;
    private static final int INVENTORY_END = 36;
    private static final int HOTBAR_MENU_OFFSET = 36;
    private static BlockPos pendingBambooPos = null;

    private AutoTool() {
    }

    public static boolean selectTool(Minecraft client) {

        if (!AutoFeaturesConfig.isAutoToolEnabled()) {
            return false;
        }

        if (client == null
                || client.player == null
                || client.level == null
                || client.gameMode == null
                || client.gui.screen() != null) {
            return false;
        }

        Player player = client.player;

        if (!(client.hitResult instanceof BlockHitResult blockHit)
                || blockHit.getType() != HitResult.Type.BLOCK) {
            return false;
        }

        BlockPos blockPos = blockHit.getBlockPos();
        BlockState state = client.level.getBlockState(blockPos);

        if (state.isAir()) {
            return false;
        }

        boolean bamboo = isBamboo(state);
        Inventory inventory = player.getInventory();

        // Search hotbar first.
        int toolSlot = findTool(
                inventory,
                state,
                bamboo,
                0,
                HOTBAR_SIZE
        );

        if (toolSlot >= 0) {

            boolean changed = inventory.getSelectedSlot() != toolSlot;

            inventory.setSelectedSlot(toolSlot);

            if (bamboo && changed) {
                pendingBambooPos = new BlockPos(
                        blockPos.getX(),
                        blockPos.getY(),
                        blockPos.getZ()
                );

                return true;
            }

            return false;
        }

        // Search main inventory.
        toolSlot = findTool(
                inventory,
                state,
                bamboo,
                INVENTORY_START,
                INVENTORY_END
        );

        if (toolSlot < 0) {
            return false;
        }

        boolean moved = moveToolToHotbar(
                client,
                player,
                inventory,
                toolSlot
        );

        if (bamboo && moved) {
            pendingBambooPos = new BlockPos(
                    blockPos.getX(),
                    blockPos.getY(),
                    blockPos.getZ()
            );

            return true;
        }

        return false;
    }

    private static int findTool(
            Inventory inventory,
            BlockState state,
            boolean bamboo,
            int start,
            int end
    ) {

        for (int slot = start; slot < end; slot++) {

            ItemStack stack = inventory.getItem(slot);

            if (stack.isEmpty()) {
                continue;
            }

            if (isSuitableTool(stack, state, bamboo)) {
                return slot;
            }
        }

        return -1;
    }

    private static boolean isSuitableTool(
            ItemStack stack,
            BlockState state,
            boolean bamboo
    ) {

        String itemId = BuiltInRegistries.ITEM
                .getKey(stack.getItem())
                .getPath();

        if (bamboo) {
            return itemId.contains("sword");
        }

        return stack.getDestroySpeed(state) > 1.0F;
    }

    private static boolean isBamboo(BlockState state) {

        String blockId = BuiltInRegistries.BLOCK
                .getKey(state.getBlock())
                .getPath();

        return blockId.equals("bamboo");
    }

    private static boolean moveToolToHotbar(
            Minecraft client,
            Player player,
            Inventory inventory,
            int inventorySlot
    ) {

        AbstractContainerMenu menu = player.inventoryMenu;

        if (menu == null || player.containerMenu != menu) {
            return false;
        }

        // Find first empty hotbar slot.
        int targetHotbarSlot = -1;

        for (int slot = 0; slot < HOTBAR_SIZE; slot++) {

            if (inventory.getItem(slot).isEmpty()) {
                targetHotbarSlot = slot;
                break;
            }
        }

        // If full, replace the currently selected slot.
        if (targetHotbarSlot == -1) {
            targetHotbarSlot = inventory.getSelectedSlot();
        }

        int sourceMenuSlot = getMenuSlotIndex(inventorySlot);
        int targetMenuSlot = getMenuSlotIndex(targetHotbarSlot);

        if (sourceMenuSlot < 0
                || sourceMenuSlot >= menu.slots.size()
                || targetMenuSlot < 0
                || targetMenuSlot >= menu.slots.size()) {
            return false;
        }

        Slot source = menu.slots.get(sourceMenuSlot);
        Slot target = menu.slots.get(targetMenuSlot);

        if (!source.hasItem()
                || !target.mayPlace(source.getItem())) {
            return false;
        }

        swapSlots(client, menu, source, target);

        inventory.setSelectedSlot(targetHotbarSlot);
        return true;
    }

    private static int getMenuSlotIndex(int inventorySlot) {

        if (inventorySlot < HOTBAR_SIZE) {
            return inventorySlot + HOTBAR_MENU_OFFSET;
        }

        return inventorySlot;
    }

    // Same click system used by Auto Refill.

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

    public static void onClientTick(Minecraft client) {

        if (pendingBambooPos == null) {
            return;
        }

        BlockPos target = pendingBambooPos;
        pendingBambooPos = null;

        if (client == null
                || client.player == null
                || client.level == null
                || client.gameMode == null
                || client.gui.screen() != null) {
            return;
        }

        if (!(client.hitResult instanceof BlockHitResult blockHit)
                || blockHit.getType() != HitResult.Type.BLOCK) {
            return;
        }

        if (!target.equals(blockHit.getBlockPos())) {
            return;
        }

        BlockState state = client.level.getBlockState(target);

        if (state.isAir() || !isBamboo(state)) {
            return;
        }

        ((MinecraftStartAttackInvoker) (Object) client)
                .aetherutilities$invokeStartAttack();
    }
}
