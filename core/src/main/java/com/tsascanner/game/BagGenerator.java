package com.tsascanner.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Generates bags with items appropriate for the current shift difficulty.
 * Uses the tag-based system and ShiftConfig to determine forbidden items.
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
     * Higher shifts = more items per bag.
     */
    public static Bag generate(ShiftConfig config) {
        int shiftNumber = config.shiftNumber;

        // Item count scales with shift
        int minItems, maxItems;
        switch (shiftNumber) {
            case 1:  minItems = 1; maxItems = 3; break;
            case 2:  minItems = 2; maxItems = 3; break;
            case 3:  minItems = 2; maxItems = 4; break;
            case 4:  minItems = 2; maxItems = 4; break;
            default: minItems = 3; maxItems = 5; break;
        }

        int itemCount = minItems + rng.nextInt(maxItems - minItems + 1);
        List<Item> items = ItemDatabase.getRandomItems(shiftNumber, itemCount);

        // Bag size — slightly random
        float w = BAG_MIN_W + rng.nextFloat() * (BAG_MAX_W - BAG_MIN_W);
        float h = BAG_MIN_H + rng.nextFloat() * (BAG_MAX_H - BAG_MIN_H);

        return new Bag(items, w, h);
    }

    /**
     * Generate a bag guaranteed to contain at least one forbidden item.
     */
    public static Bag generateForbiddenBag(ShiftConfig config) {
        int shiftNumber = config.shiftNumber;

        // Try generating until we get one with a forbidden item
        for (int attempt = 0; attempt < 50; attempt++) {
            Bag bag = generate(config);
            if (bag.containsForbidden(config)) return bag;
        }

        // Fallback: force a forbidden item in
        Bag bag = generate(config);
        List<Item> forbidden = ItemDatabase.getForbiddenItems(shiftNumber, config.forbiddenTags, 1);
        if (!forbidden.isEmpty()) {
            if (bag.contents.isEmpty()) {
                bag.contents.add(forbidden.get(0));
            } else {
                bag.contents.set(0, forbidden.get(0));
            }
        }
        return bag;
    }

    /**
     * Generate a bag that is clean (no forbidden items).
     */
    public static Bag generateCleanBag(ShiftConfig config) {
        for (int attempt = 0; attempt < 50; attempt++) {
            Bag bag = generate(config);
            if (!bag.containsForbidden(config)) return bag;
        }
        // If we can't generate a clean bag after 50 attempts, just return whatever
        return generate(config);
    }
}
