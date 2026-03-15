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
    private static int bagCounter = 0;

    // Bag size ranges
    private static final float BAG_MIN_W = 70;
    private static final float BAG_MAX_W = 110;
    private static final float BAG_MIN_H = 50;
    private static final float BAG_MAX_H = 80;

    public static void resetCounter() {
        bagCounter = 0;
    }

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

        Bag bag = new Bag(items, w, h);

        // Assign color based on shift
        bag.color = pickColor(shiftNumber);

        return bag;
    }

    private static Bag.BagColor pickColor(int shiftNumber) {
        if (shiftNumber <= 2) {
            return Bag.BagColor.BROWN;
        }

        float roll = rng.nextFloat();
        if (shiftNumber == 3) {
            // 15% RED, 40% BLUE, 30% GREEN, 15% BROWN
            if (roll < 0.15f) return Bag.BagColor.RED;
            if (roll < 0.55f) return Bag.BagColor.BLUE;
            if (roll < 0.85f) return Bag.BagColor.GREEN;
            return Bag.BagColor.BROWN;
        } else if (shiftNumber == 4) {
            // 15% RED, 35% BLUE, 35% GREEN, 15% BROWN
            if (roll < 0.15f) return Bag.BagColor.RED;
            if (roll < 0.50f) return Bag.BagColor.BLUE;
            if (roll < 0.85f) return Bag.BagColor.GREEN;
            return Bag.BagColor.BROWN;
        } else {
            // Shift 5+: 15% RED, 25% BLUE, 30% GREEN, 30% BROWN
            if (roll < 0.15f) return Bag.BagColor.RED;
            if (roll < 0.40f) return Bag.BagColor.BLUE;
            if (roll < 0.70f) return Bag.BagColor.GREEN;
            return Bag.BagColor.BROWN;
        }
    }

    /**
     * Check if a bag's applicable rule has empty forbidden tags (VIP / everything allowed).
     */
    private static boolean bagHasEmptyRule(Bag bag, ShiftConfig config) {
        for (ShiftConfig.ForbiddenRule rule : config.rules) {
            if (rule.appliesTo(bag)) {
                return rule.forbiddenTags.length == 0;
            }
        }
        return false;
    }

    /**
     * Generate a bag guaranteed to contain at least one forbidden item.
     */
    public static Bag generateForbiddenBag(ShiftConfig config) {
        // Try generating until we get one with forbidden items in its context
        for (int attempt = 0; attempt < 50; attempt++) {
            Bag bag = generate(config);
            // Skip if this bag's rule has empty forbidden tags (VIP)
            if (bagHasEmptyRule(bag, config)) continue;
            if (bag.containsForbidden(config)) {
                bag.bagNumber = ++bagCounter;
                return bag;
            }
        }

        // Fallback: force a forbidden item in
        Bag bag = generate(config);
        // Make sure we don't pick a VIP bag for forcing
        int safetyCounter = 0;
        while (bagHasEmptyRule(bag, config) && safetyCounter < 20) {
            bag = generate(config);
            safetyCounter++;
        }

        // Get the forbidden tags for this bag's context
        String[] tagsForBag = getApplicableForbiddenTags(bag, config);
        if (tagsForBag.length > 0) {
            List<Item> forbidden = ItemDatabase.getForbiddenItems(config.shiftNumber, tagsForBag, 1);
            if (!forbidden.isEmpty()) {
                if (bag.contents.isEmpty()) {
                    bag.contents.add(forbidden.get(0));
                } else {
                    bag.contents.set(0, forbidden.get(0));
                }
            }
        }
        bag.bagNumber = ++bagCounter;
        return bag;
    }

    /**
     * Get the forbidden tags that apply to a specific bag.
     */
    private static String[] getApplicableForbiddenTags(Bag bag, ShiftConfig config) {
        for (ShiftConfig.ForbiddenRule rule : config.rules) {
            if (rule.appliesTo(bag)) {
                return rule.forbiddenTags;
            }
        }
        return new String[0];
    }

    /**
     * Generate a bag that is clean (no forbidden items for its context).
     */
    public static Bag generateCleanBag(ShiftConfig config) {
        for (int attempt = 0; attempt < 50; attempt++) {
            Bag bag = generate(config);
            if (!bag.containsForbidden(config)) {
                bag.bagNumber = ++bagCounter;
                return bag;
            }
        }
        // If we can't generate a clean bag after 50 attempts, just return whatever
        Bag bag = generate(config);
        bag.bagNumber = ++bagCounter;
        return bag;
    }
}
