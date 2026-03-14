package com.tsascanner.game;

import java.util.List;
import java.util.Random;

/**
 * Generates bags with items appropriate for the current shift difficulty.
 */
public class BagGenerator {

    private static final Random rng = new Random();

    // Bag size ranges
    private static final float BAG_MIN_W = 70;
    private static final float BAG_MAX_W = 110;
    private static final float BAG_MIN_H = 50;
    private static final float BAG_MAX_H = 80;

    /**
     * Generate a single bag for the given shift.
     * Higher shifts = more items per bag and harder item mix.
     */
    public static Bag generate(int shiftNumber) {
        // Item count scales with shift
        int minItems, maxItems;
        switch (shiftNumber) {
            case 1:  minItems = 1; maxItems = 2; break;
            case 2:  minItems = 1; maxItems = 3; break;
            case 3:  minItems = 2; maxItems = 3; break;
            case 4:  minItems = 2; maxItems = 4; break;
            default: minItems = 2; maxItems = 4; break;
        }

        int itemCount = minItems + rng.nextInt(maxItems - minItems + 1);
        List<Item> items = ItemDatabase.getRandomItems(shiftNumber, itemCount);

        // Bag size — slightly random
        float w = BAG_MIN_W + rng.nextFloat() * (BAG_MAX_W - BAG_MIN_W);
        float h = BAG_MIN_H + rng.nextFloat() * (BAG_MAX_H - BAG_MIN_H);

        return new Bag(items, w, h);
    }

    /**
     * Ensure at least some weapon bags appear in a shift so it's not all harmless.
     * Returns a bag guaranteed to contain a weapon.
     */
    public static Bag generateWeaponBag(int shiftNumber) {
        // Keep trying until we get a bag with a weapon
        for (int attempt = 0; attempt < 50; attempt++) {
            Bag bag = generate(shiftNumber);
            if (bag.containsWeapon()) return bag;
        }
        // Fallback: force a weapon in
        Bag bag = generate(shiftNumber);
        List<Item> weapons = ItemDatabase.getRandomItems(shiftNumber, 1);
        // Replace first item with a forced weapon
        Item forcedWeapon = new Item("Knife", Item.ItemCategory.WEAPON, 1,
            Item.ShapePart.rect(0, 0, 4, 26, 15),
            Item.ShapePart.rect(0, -14, 6, 6, 0));
        bag.contents.set(0, forcedWeapon);
        return bag;
    }
}
