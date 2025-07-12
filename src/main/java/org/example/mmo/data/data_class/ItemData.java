package org.example.mmo.data.data_class;

/**
 * Identifier of an item and its amount for storage.
 */
public class ItemData {
    public String itemId;
    public int amount;
    /** Inventory slot, or -1 if unspecified (legacy data). */
    public int slot = -1;

    public ItemData() {}

    public ItemData(String itemId, int amount) {
        this(itemId, amount, -1);
    }

    public ItemData(String itemId, int amount, int slot) {
        this.itemId = itemId;
        this.amount = amount;
        this.slot = slot;
    }
}
