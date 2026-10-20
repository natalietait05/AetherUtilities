
package com.aetheris.aetherutilities.autotool;

import com.aetheris.aetherutilities.client.AutoFeaturesConfig;
import com.aetheris.aetherutilities.client.mixin.MinecraftStartAttackInvoker;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public final class AutoTool {

    private static final int HOTBAR_SIZE = 9;
    private static final int INVENTORY_START = 9;
    private static final int INVENTORY_END = 36;
    private static final int HOTBAR_MENU_OFFSET = 36;

    private static BlockPos pendingBambooPos = null;

    private AutoTool() {
    }

    // ============================================================
    // MAIN SELECTION
    // ============================================================

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

        Inventory inventory = player.getInventory();

        // Bamboo retains its original sword behavior.
        if (isBamboo(state)) {
            return selectBambooTool(
                    client,
                    player,
                    inventory,
                    blockPos
            );
        }

        // Leaves use a specific tool priority.
        if (state.is(BlockTags.LEAVES)) {
            return selectLeafTool(
                    client,
                    player,
                    inventory
            );
        }

        // All other blocks use the fastest available tool.
        return selectBestTool(
                client,
                player,
                inventory,
                state
        );
    }

    // ============================================================
    // NORMAL BLOCKS
    // ============================================================

    private static boolean selectBestTool(
            Minecraft client,
            Player player,
            Inventory inventory,
            BlockState state
    ) {

        int selectedSlot = inventory.getSelectedSlot();

        ItemStack selectedStack = inventory.getItem(selectedSlot);

        float selectedSpeed = getDestroySpeed(
                selectedStack,
                state
        );

        int bestSlot = -1;
        float bestSpeed = 1.0F;

        // Search hotbar.
        for (int slot = 0; slot < HOTBAR_SIZE; slot++) {

            float speed = getDestroySpeed(
                    inventory.getItem(slot),
                    state
            );

            if (speed > bestSpeed) {
                bestSpeed = speed;
                bestSlot = slot;
            }
        }

        // Search main inventory.
        for (int slot = INVENTORY_START;
             slot < INVENTORY_END;
             slot++) {

            float speed = getDestroySpeed(
                    inventory.getItem(slot),
                    state
            );

            if (speed > bestSpeed) {
                bestSpeed = speed;
                bestSlot = slot;
            }
        }

        if (bestSlot < 0) {
            return false;
        }

        // Keep the selected tool if it is equally good
        // or better than the best available tool.
        if (selectedSpeed >= bestSpeed) {
            return false;
        }

        // Tool already in hotbar.
        if (bestSlot < HOTBAR_SIZE) {

            inventory.setSelectedSlot(bestSlot);

            return false;
        }

        // Tool in main inventory: swap with selected slot.
        moveToolToSelectedSlot(
                client,
                player,
                inventory,
                bestSlot
        );

        return false;
    }

    private static float getDestroySpeed(
            ItemStack stack,
            BlockState state
    ) {

        if (stack == null || stack.isEmpty()) {
            return 0.0F;
        }

        return stack.getDestroySpeed(state);
    }

    // ============================================================
    // LEAVES
    // ============================================================

    private static boolean selectLeafTool(
            Minecraft client,
            Player player,
            Inventory inventory
    ) {

        int selectedSlot = inventory.getSelectedSlot();

        ItemStack selectedStack = inventory.getItem(selectedSlot);

        int selectedPriority = getLeafToolPriority(selectedStack);

        int bestSlot = -1;
        int bestPriority = Integer.MAX_VALUE;

        // Search hotbar.
        for (int slot = 0; slot < HOTBAR_SIZE; slot++) {

            int priority = getLeafToolPriority(
                    inventory.getItem(slot)
            );

            if (priority >= 0 && priority < bestPriority) {

                bestPriority = priority;
                bestSlot = slot;
            }
        }

        // Search main inventory.
        for (int slot = INVENTORY_START;
             slot < INVENTORY_END;
             slot++) {

            int priority = getLeafToolPriority(
                    inventory.getItem(slot)
            );

            if (priority >= 0 && priority < bestPriority) {

                bestPriority = priority;
                bestSlot = slot;
            }
        }

        if (bestSlot < 0) {
            return false;
        }

        // Keep the currently selected tool if it already
        // has the highest available priority.
        if (selectedPriority >= 0
                && selectedPriority <= bestPriority) {
            return false;
        }

        // Tool already in hotbar.
        if (bestSlot < HOTBAR_SIZE) {

            inventory.setSelectedSlot(bestSlot);

            return false;
        }

        // Tool in main inventory:
        // replace the currently selected hotbar slot.
        moveToolToSelectedSlot(
                client,
                player,
                inventory,
                bestSlot
        );

        return false;
    }

    /**
     * Lower values mean higher priority:
     *
     * 0 = Hoe
     * 1 = Shears
     * 2 = Sword
     * -1 = Not suitable
     *
     * Uses registry identifiers, just like the original
     * sword detection in this project.
     */
    private static int getLeafToolPriority(ItemStack stack) {

        if (stack == null || stack.isEmpty()) {
            return -1;
        }

        String itemId = BuiltInRegistries.ITEM
                .getKey(stack.getItem())
                .getPath();

        if (itemId.equals("hoe")
                || itemId.endsWith("_hoe")) {
            return 0;
        }

        if (itemId.equals("shears")
                || itemId.endsWith("_shears")) {
            return 1;
        }

        if (itemId.contains("sword")) {
            return 2;
        }

        return -1;
    }

    // ============================================================
    // BAMBOO
    // ============================================================

    private static boolean selectBambooTool(
            Minecraft client,
            Player player,
            Inventory inventory,
            BlockPos blockPos
    ) {

        // Search hotbar first.
        int swordSlot = findSword(
                inventory,
                0,
                HOTBAR_SIZE
        );

        if (swordSlot >= 0) {

            boolean changed =
                    inventory.getSelectedSlot() != swordSlot;

            inventory.setSelectedSlot(swordSlot);

            if (changed) {
                scheduleBambooBreak(blockPos);
                return true;
            }

            return false;
        }

        // Search main inventory.
        swordSlot = findSword(
                inventory,
                INVENTORY_START,
                INVENTORY_END
        );

        if (swordSlot < 0) {
            return false;
        }

        boolean moved = moveToolToHotbar(
                client,
                player,
                inventory,
                swordSlot
        );

        if (moved) {
            scheduleBambooBreak(blockPos);
            return true;
        }

        return false;
    }

    private static int findSword(
            Inventory inventory,
            int start,
            int end
    ) {

        for (int slot = start; slot < end; slot++) {

            if (isSword(inventory.getItem(slot))) {
                return slot;
            }
        }

        return -1;
    }

    private static boolean isSword(ItemStack stack) {

        if (stack == null || stack.isEmpty()) {
            return false;
        }

        String itemId = BuiltInRegistries.ITEM
                .getKey(stack.getItem())
                .getPath();

        return itemId.contains("sword");
    }

    private static boolean isBamboo(BlockState state) {

        String blockId = BuiltInRegistries.BLOCK
                .getKey(state.getBlock())
                .getPath();

        return blockId.equals("bamboo");
    }

    // ============================================================
    // INVENTORY MOVEMENT
    // ============================================================

    /**
     * Swaps a main inventory tool with the currently selected
     * hotbar slot.
     */
    private static void moveToolToSelectedSlot(
            Minecraft client,
            Player player,
            Inventory inventory,
            int inventorySlot
    ) {

        AbstractContainerMenu menu = player.inventoryMenu;

        if (player.containerMenu != menu) {
            return;
        }

        int selectedHotbarSlot = inventory.getSelectedSlot();

        int sourceMenuSlot = getMenuSlotIndex(inventorySlot);
        int targetMenuSlot = getMenuSlotIndex(selectedHotbarSlot);

        if (sourceMenuSlot < 0
                || sourceMenuSlot >= menu.slots.size()
                || targetMenuSlot < 0
                || targetMenuSlot >= menu.slots.size()) {
            return;
        }

        Slot source = menu.slots.get(sourceMenuSlot);
        Slot target = menu.slots.get(targetMenuSlot);

        if (!source.hasItem()
                || !target.mayPlace(source.getItem())) {
            return;
        }

        swapSlots(
                client,
                menu,
                source,
                target
        );

    }

    /**
     * Original bamboo behavior:
     * use an empty hotbar slot first, or selected slot if full.
     */
    private static boolean moveToolToHotbar(
            Minecraft client,
            Player player,
            Inventory inventory,
            int inventorySlot
    ) {

        int targetHotbarSlot = -1;

        for (int slot = 0; slot < HOTBAR_SIZE; slot++) {

            if (inventory.getItem(slot).isEmpty()) {
                targetHotbarSlot = slot;
                break;
            }
        }

        if (targetHotbarSlot == -1) {
            targetHotbarSlot = inventory.getSelectedSlot();
        }

        AbstractContainerMenu menu = player.inventoryMenu;

        if (player.containerMenu != menu) {
            return false;
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

        swapSlots(
                client,
                menu,
                source,
                target
        );

        inventory.setSelectedSlot(targetHotbarSlot);

        return true;
    }

    private static int getMenuSlotIndex(int inventorySlot) {

        if (inventorySlot < HOTBAR_SIZE) {
            return inventorySlot + HOTBAR_MENU_OFFSET;
        }

        return inventorySlot;
    }

    // ============================================================
    // INVENTORY SWAP
    // ============================================================

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

    // ============================================================
    // BAMBOO BREAK SCHEDULING
    // ============================================================

    private static void scheduleBambooBreak(BlockPos blockPos) {

        pendingBambooPos = new BlockPos(
                blockPos.getX(),
                blockPos.getY(),
                blockPos.getZ()
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
