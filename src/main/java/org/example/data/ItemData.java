package org.example.data;

/**
 * Identifier of an item and its amount for storage.
 */
public class ItemData {
    public String itemId;
    public int amount;

    public ItemData() {}

    public ItemData(String itemId, int amount) {
        this.itemId = itemId;
        this.amount = amount;
    }
}
