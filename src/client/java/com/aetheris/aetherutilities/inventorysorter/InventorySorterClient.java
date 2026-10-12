package com.aetheris.aetherutilities.inventorysorter;

public final class InventorySorterClient {

    private InventorySorterClient() {
    }

    public static void initialize() {

        InventorySorterConfig.load();
    }
}