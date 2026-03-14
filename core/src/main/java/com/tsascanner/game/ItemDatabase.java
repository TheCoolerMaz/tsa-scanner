package com.tsascanner.game;

import com.tsascanner.game.Item.ItemCategory;
import com.tsascanner.game.Item.ShapePart;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Static registry of all items in the game.
 * Each item is defined with geometry-only shape parts.
 */
public class ItemDatabase {

    private static final List<Item> ALL_ITEMS = new ArrayList<>();
    private static final Random rng = new Random();

    static {
        // ============ HARMLESS ============

        // Laptop — wide flat rectangle
        ALL_ITEMS.add(new Item("Laptop", ItemCategory.HARMLESS, 1,
            ShapePart.rect(0, 0, 40, 6, 0),    // base
            ShapePart.rect(0, 6, 38, 1, 0)      // hinge line
        ));

        // Water Bottle — tall thin rectangle
        ALL_ITEMS.add(new Item("Water Bottle", ItemCategory.HARMLESS, 1,
            ShapePart.rect(0, 0, 8, 28, 0),
            ShapePart.rect(0, 28, 5, 4, 0)      // cap
        ));

        // Book — medium rectangle
        ALL_ITEMS.add(new Item("Book", ItemCategory.HARMLESS, 1,
            ShapePart.rect(0, 0, 22, 16, 0)
        ));

        // Shoes — two small rectangles
        ALL_ITEMS.add(new Item("Shoes", ItemCategory.HARMLESS, 1,
            ShapePart.rect(-8, 0, 12, 6, 0),
            ShapePart.rect(8, 0, 12, 6, 0)
        ));

        // Phone — small rectangle
        ALL_ITEMS.add(new Item("Phone", ItemCategory.HARMLESS, 1,
            ShapePart.rect(0, 0, 7, 14, 0)
        ));

        // Keys — small circle + small rectangle
        ALL_ITEMS.add(new Item("Keys", ItemCategory.HARMLESS, 1,
            ShapePart.circle(0, 0, 3),
            ShapePart.rect(3, 0, 8, 2, 0)
        ));

        // Headphones — circle + arc
        ALL_ITEMS.add(new Item("Headphones", ItemCategory.HARMLESS, 1,
            ShapePart.circle(-6, 0, 4),
            ShapePart.circle(6, 0, 4),
            ShapePart.rect(0, 5, 14, 2, 0)      // headband
        ));

        // Tablet — wide rectangle, smaller than laptop
        ALL_ITEMS.add(new Item("Tablet", ItemCategory.HARMLESS, 1,
            ShapePart.rect(0, 0, 18, 24, 0)
        ));

        // ============ WEAPONS ============

        // Knife — long thin rectangle angled
        ALL_ITEMS.add(new Item("Knife", ItemCategory.WEAPON, 1,
            ShapePart.rect(0, 0, 4, 26, 15),    // blade
            ShapePart.rect(0, -14, 6, 6, 0)     // handle
        ));

        // Gun — L-shape from two rectangles
        ALL_ITEMS.add(new Item("Gun", ItemCategory.WEAPON, 1,
            ShapePart.rect(0, 2, 28, 8, 0),     // barrel
            ShapePart.rect(-6, -8, 8, 12, 0)    // grip
        ));

        // Box Cutter — small angled rectangle
        ALL_ITEMS.add(new Item("Box Cutter", ItemCategory.WEAPON, 1,
            ShapePart.rect(0, 0, 3, 14, 20),
            ShapePart.rect(0, -8, 5, 4, 0)      // handle
        ));

        // Brass Knuckles — connected circles
        ALL_ITEMS.add(new Item("Brass Knuckles", ItemCategory.WEAPON, 2,
            ShapePart.circle(-6, 0, 4),
            ShapePart.circle(0, 0, 4),
            ShapePart.circle(6, 0, 4),
            ShapePart.rect(0, -4, 18, 3, 0)     // bar
        ));

        // ============ DISGUISED WEAPONS ============

        // Umbrella Knife — looks like umbrella (thin rect + small handle)
        ALL_ITEMS.add(new Item("Umbrella Knife", ItemCategory.DISGUISED_WEAPON, 3,
            ShapePart.rect(0, 0, 3, 34, 0),     // shaft (hides blade)
            ShapePart.rect(0, -18, 8, 3, 0)     // handle curve
        ));

        // Hairdryer Gun — circle (head) + rectangle (handle), like a gun shape
        ALL_ITEMS.add(new Item("Hairdryer Gun", ItemCategory.DISGUISED_WEAPON, 3,
            ShapePart.circle(8, 4, 7),           // dryer head
            ShapePart.rect(-4, 2, 18, 6, 0),    // body
            ShapePart.rect(-6, -8, 6, 12, 0)    // handle (looks like grip)
        ));

        // Pen Knife — looks like a pen
        ALL_ITEMS.add(new Item("Pen Knife", ItemCategory.DISGUISED_WEAPON, 4,
            ShapePart.rect(0, 0, 2, 20, 5)      // looks like pen, is a blade
        ));

        // Cane Sword — long thin rect
        ALL_ITEMS.add(new Item("Cane Sword", ItemCategory.DISGUISED_WEAPON, 4,
            ShapePart.rect(0, 0, 3, 40, 0),     // shaft
            ShapePart.circle(0, 20, 4)           // handle knob
        ));

        // ============ SUSPICIOUS HARMLESS ============

        // Wrench — looks like weapon but isn't
        ALL_ITEMS.add(new Item("Wrench", ItemCategory.SUSPICIOUS_HARMLESS, 2,
            ShapePart.rect(0, 0, 4, 28, 0),     // shaft
            ShapePart.circle(0, 15, 5)           // head
        ));

        // Scissors — X shape
        ALL_ITEMS.add(new Item("Scissors", ItemCategory.SUSPICIOUS_HARMLESS, 2,
            ShapePart.rect(0, 0, 3, 18, 20),    // blade 1
            ShapePart.rect(0, 0, 3, 18, -20),   // blade 2
            ShapePart.circle(0, 0, 3)            // pivot
        ));

        // Metal Water Bottle — tall cylinder (looks like pipe bomb)
        ALL_ITEMS.add(new Item("Metal Water Bottle", ItemCategory.SUSPICIOUS_HARMLESS, 2,
            ShapePart.rect(0, 0, 8, 30, 0),
            ShapePart.circle(0, 16, 4)           // cap
        ));

        // Curling Iron — rod + handle, looks vaguely weapon-like
        ALL_ITEMS.add(new Item("Curling Iron", ItemCategory.SUSPICIOUS_HARMLESS, 3,
            ShapePart.rect(0, 4, 4, 24, 0),     // barrel
            ShapePart.rect(0, -10, 6, 5, 0)     // handle
        ));
    }

    /** Get all items available for a given shift. */
    public static List<Item> getItemsForShift(int shiftNumber) {
        List<Item> available = new ArrayList<>();
        for (Item item : ALL_ITEMS) {
            if (item.minShift <= shiftNumber) {
                available.add(item);
            }
        }
        return available;
    }

    /** Get random items appropriate for the shift, with weighted category selection. */
    public static List<Item> getRandomItems(int shiftNumber, int count) {
        List<Item> available = getItemsForShift(shiftNumber);
        List<Item> result = new ArrayList<>();

        // Category pool by shift
        List<Item> harmless = new ArrayList<>();
        List<Item> weapons = new ArrayList<>();
        List<Item> disguised = new ArrayList<>();
        List<Item> suspicious = new ArrayList<>();

        for (Item item : available) {
            switch (item.category) {
                case HARMLESS: harmless.add(item); break;
                case WEAPON: weapons.add(item); break;
                case DISGUISED_WEAPON: disguised.add(item); break;
                case SUSPICIOUS_HARMLESS: suspicious.add(item); break;
            }
        }

        for (int i = 0; i < count; i++) {
            float roll = rng.nextFloat();
            List<Item> pool;

            if (shiftNumber <= 2) {
                // Early: mostly harmless, some obvious weapons
                if (roll < 0.65f) pool = harmless;
                else if (roll < 0.90f) pool = weapons;
                else pool = suspicious.isEmpty() ? harmless : suspicious;
            } else if (shiftNumber <= 3) {
                // Mid: disguised weapons appear
                if (roll < 0.45f) pool = harmless;
                else if (roll < 0.65f) pool = weapons;
                else if (roll < 0.80f) pool = disguised.isEmpty() ? weapons : disguised;
                else pool = suspicious.isEmpty() ? harmless : suspicious;
            } else {
                // Late: full spread
                if (roll < 0.30f) pool = harmless;
                else if (roll < 0.50f) pool = weapons;
                else if (roll < 0.70f) pool = disguised.isEmpty() ? weapons : disguised;
                else pool = suspicious.isEmpty() ? harmless : suspicious;
            }

            if (pool.isEmpty()) pool = harmless;
            Item template = pool.get(rng.nextInt(pool.size()));
            result.add(template.copy());
        }

        return result;
    }
}
