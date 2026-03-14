package com.tsascanner.game;

import com.tsascanner.game.Item.ShapePart;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Static registry of all items in the game.
 * Each item is defined with geometry-only shape parts and a tag-based classification.
 */
public class ItemDatabase {

    private static final List<Item> ALL_ITEMS = new ArrayList<>();
    private static final Random rng = new Random();

    static {
        // ============ BLADES ============

        // Knife — long thin blade + handle
        ALL_ITEMS.add(new Item("Knife", new String[]{"blade", "knife", "metal"}, 1,
            ShapePart.rect(0, 0, 4, 26, 15),    // blade
            ShapePart.rect(0, -14, 6, 6, 0)     // handle
        ));

        // Box Cutter — small angled blade + handle
        ALL_ITEMS.add(new Item("Box Cutter", new String[]{"blade", "metal", "tool"}, 1,
            ShapePart.rect(0, 0, 3, 14, 20),
            ShapePart.rect(0, -8, 5, 4, 0)      // handle
        ));

        // Scissors — X shape with pivot
        ALL_ITEMS.add(new Item("Scissors", new String[]{"blade", "metal", "tool"}, 1,
            ShapePart.rect(0, 0, 3, 18, 20),    // blade 1
            ShapePart.rect(0, 0, 3, 18, -20),   // blade 2
            ShapePart.circle(0, 0, 3)            // pivot
        ));

        // Letter Opener — thin tapered blade + round handle
        ALL_ITEMS.add(new Item("Letter Opener", new String[]{"blade", "metal"}, 2,
            ShapePart.rect(0, 4, 3, 22, 0),     // blade
            ShapePart.circle(0, -8, 4)           // round handle
        ));

        // Razor — very small rectangular blade
        ALL_ITEMS.add(new Item("Razor", new String[]{"blade", "metal"}, 2,
            ShapePart.rect(0, 0, 12, 3, 0),     // thin blade
            ShapePart.rect(0, -3, 8, 2, 0)      // guard
        ));

        // ============ GUNS ============

        // Handgun — L-shape from two rectangles
        ALL_ITEMS.add(new Item("Handgun", new String[]{"gun", "firearm", "metal"}, 1,
            ShapePart.rect(0, 2, 28, 8, 0),     // barrel
            ShapePart.rect(-6, -8, 8, 12, 0)    // grip
        ));

        // Water Pistol — similar shape but tagged toy
        ALL_ITEMS.add(new Item("Water Pistol", new String[]{"toy", "plastic"}, 2,
            ShapePart.rect(0, 2, 24, 7, 0),     // barrel
            ShapePart.rect(-5, -7, 7, 10, 0),   // grip
            ShapePart.circle(12, 3, 5)           // water tank bulge
        ));

        // ============ FOOD ============

        // Sandwich — wide flat rectangle with layers
        ALL_ITEMS.add(new Item("Sandwich", new String[]{"food"}, 1,
            ShapePart.rect(0, 0, 20, 12, 0),    // bread
            ShapePart.rect(0, 1, 18, 3, 0)      // filling line
        ));

        // Banana — curved shape via angled rectangle
        ALL_ITEMS.add(new Item("Banana", new String[]{"food", "fruit"}, 1,
            ShapePart.rect(0, 0, 5, 22, 10),    // body
            ShapePart.rect(0, 12, 3, 4, -5)     // stem
        ));

        // Raw Steak — irregular blob via overlapping shapes
        ALL_ITEMS.add(new Item("Raw Steak", new String[]{"food", "meat"}, 2,
            ShapePart.rect(0, 0, 20, 10, 5),    // main mass
            ShapePart.circle(8, 2, 5),           // fat lobe
            ShapePart.rect(-6, -2, 8, 5, -10)   // bone nub
        ));

        // Apple — circle with stem
        ALL_ITEMS.add(new Item("Apple", new String[]{"food", "fruit"}, 1,
            ShapePart.circle(0, 0, 7),           // body
            ShapePart.rect(0, 8, 2, 4, 0)       // stem
        ));

        // Cheese Wedge — triangle shape
        ALL_ITEMS.add(new Item("Cheese Wedge", new String[]{"food"}, 2,
            new ShapePart(Item.ShapeType.TRIANGLE, 0, 0, 18, 12, 0)
        ));

        // ============ LIQUIDS ============

        // Water Bottle — tall with cap
        ALL_ITEMS.add(new Item("Water Bottle", new String[]{"liquid", "bottle", "plastic"}, 1,
            ShapePart.rect(0, 0, 8, 28, 0),
            ShapePart.rect(0, 15, 5, 4, 0)      // cap
        ));

        // Soda Can — short cylinder
        ALL_ITEMS.add(new Item("Soda Can", new String[]{"liquid", "metal"}, 1,
            ShapePart.rect(0, 0, 10, 16, 0),    // can body
            ShapePart.rect(0, 9, 6, 2, 0)       // pull tab
        ));

        // Shampoo Bottle — wide bottom, narrow neck
        ALL_ITEMS.add(new Item("Shampoo Bottle", new String[]{"liquid", "bottle", "plastic"}, 2,
            ShapePart.rect(0, -4, 12, 18, 0),   // body
            ShapePart.rect(0, 10, 6, 8, 0),     // neck
            ShapePart.rect(0, 15, 8, 3, 0)      // cap
        ));

        // Thermos — tall metal cylinder
        ALL_ITEMS.add(new Item("Thermos", new String[]{"liquid", "metal"}, 2,
            ShapePart.rect(0, 0, 10, 30, 0),    // body
            ShapePart.circle(0, 16, 5)           // cap/lid
        ));

        // ============ ELECTRONICS ============

        // Laptop — wide flat rectangle
        ALL_ITEMS.add(new Item("Laptop", new String[]{"electronic", "metal"}, 1,
            ShapePart.rect(0, 0, 40, 6, 0),     // base
            ShapePart.rect(0, 6, 38, 1, 0)      // hinge line
        ));

        // Phone — small rectangle
        ALL_ITEMS.add(new Item("Phone", new String[]{"electronic", "metal"}, 1,
            ShapePart.rect(0, 0, 7, 14, 0),
            ShapePart.circle(0, -5, 2)           // home button
        ));

        // Tablet — medium rectangle
        ALL_ITEMS.add(new Item("Tablet", new String[]{"electronic", "metal"}, 1,
            ShapePart.rect(0, 0, 18, 24, 0),
            ShapePart.circle(0, -10, 2)          // home button
        ));

        // Game Console — rectangle with bumps
        ALL_ITEMS.add(new Item("Game Console", new String[]{"electronic", "plastic"}, 2,
            ShapePart.rect(0, 0, 26, 10, 0),    // body
            ShapePart.circle(-10, 2, 3),         // left stick
            ShapePart.circle(10, 2, 3)           // right stick
        ));

        // Power Bank — thick small rectangle
        ALL_ITEMS.add(new Item("Power Bank", new String[]{"electronic", "metal"}, 2,
            ShapePart.rect(0, 0, 12, 7, 0),     // body
            ShapePart.rect(6, 0, 2, 3, 0)       // USB port
        ));

        // ============ TOOLS ============

        // Wrench — shaft with circular head
        ALL_ITEMS.add(new Item("Wrench", new String[]{"tool", "metal"}, 1,
            ShapePart.rect(0, 0, 4, 28, 0),     // shaft
            ShapePart.circle(0, 15, 5)           // head
        ));

        // Screwdriver — long shaft with handle
        ALL_ITEMS.add(new Item("Screwdriver", new String[]{"tool", "metal"}, 2,
            ShapePart.rect(0, 4, 3, 22, 0),     // shaft
            ShapePart.rect(0, -8, 7, 8, 0)      // handle
        ));

        // Pliers — two arms meeting at pivot
        ALL_ITEMS.add(new Item("Pliers", new String[]{"tool", "metal"}, 2,
            ShapePart.rect(-2, 4, 3, 16, 5),    // arm 1
            ShapePart.rect(2, 4, 3, 16, -5),    // arm 2
            ShapePart.circle(0, 0, 3),           // pivot
            ShapePart.rect(-3, -8, 5, 8, 0),    // handle 1
            ShapePart.rect(3, -8, 5, 8, 0)      // handle 2
        ));

        // Hammer — T-shape
        ALL_ITEMS.add(new Item("Hammer", new String[]{"tool", "metal"}, 2,
            ShapePart.rect(0, 0, 4, 26, 0),     // handle
            ShapePart.rect(0, 14, 16, 8, 0)     // head
        ));

        // ============ PERSONAL ============

        // Umbrella — long thin shaft + curved handle
        ALL_ITEMS.add(new Item("Umbrella", new String[]{"personal"}, 1,
            ShapePart.rect(0, 0, 3, 34, 0),     // shaft
            ShapePart.rect(0, -18, 8, 3, 0)     // handle curve
        ));

        // Hairdryer — circle head + rectangle handle (L-shape)
        ALL_ITEMS.add(new Item("Hairdryer", new String[]{"personal", "electronic"}, 2,
            ShapePart.circle(8, 4, 7),           // dryer head
            ShapePart.rect(-4, 2, 18, 6, 0),    // body
            ShapePart.rect(-6, -8, 6, 12, 0)    // handle
        ));

        // Curling Iron — rod with handle
        ALL_ITEMS.add(new Item("Curling Iron", new String[]{"personal", "electronic"}, 2,
            ShapePart.rect(0, 4, 4, 24, 0),     // barrel
            ShapePart.rect(0, -10, 6, 5, 0)     // handle
        ));

        // Keys — circle + small rectangle
        ALL_ITEMS.add(new Item("Keys", new String[]{"personal", "metal"}, 1,
            ShapePart.circle(0, 0, 3),
            ShapePart.rect(3, 0, 8, 2, 0)
        ));

        // Headphones — two circles + headband
        ALL_ITEMS.add(new Item("Headphones", new String[]{"personal", "electronic"}, 1,
            ShapePart.circle(-6, 0, 4),
            ShapePart.circle(6, 0, 4),
            ShapePart.rect(0, 5, 14, 2, 0)      // headband
        ));

        // Sunglasses — two circles + bridge
        ALL_ITEMS.add(new Item("Sunglasses", new String[]{"personal"}, 1,
            ShapePart.circle(-7, 0, 5),          // left lens
            ShapePart.circle(7, 0, 5),           // right lens
            ShapePart.rect(0, 0, 4, 2, 0)       // bridge
        ));

        // ============ WEIRD ============

        // Rubber Duck — circle body + smaller head + beak triangle
        ALL_ITEMS.add(new Item("Rubber Duck", new String[]{"toy", "plastic"}, 1,
            ShapePart.circle(0, 0, 8),           // body
            ShapePart.circle(6, 7, 5),           // head
            new ShapePart(Item.ShapeType.TRIANGLE, 11, 8, 6, 4, 0) // beak
        ));

        // Cactus (in pot) — rectangle pot + circle body + small spikes
        ALL_ITEMS.add(new Item("Cactus", new String[]{"plant", "weird"}, 2,
            ShapePart.rect(0, -6, 14, 10, 0),   // pot
            ShapePart.rect(0, 6, 8, 16, 0),     // cactus body
            ShapePart.rect(-6, 10, 4, 6, 30),   // left arm
            ShapePart.rect(6, 8, 4, 6, -30)     // right arm
        ));

        // Bowling Pin — narrow top, wide bottom
        ALL_ITEMS.add(new Item("Bowling Pin", new String[]{"toy", "plastic"}, 2,
            ShapePart.rect(0, -4, 10, 14, 0),   // base
            ShapePart.rect(0, 6, 6, 8, 0),      // neck
            ShapePart.circle(0, 12, 5)           // head
        ));

        // Trophy — cup shape
        ALL_ITEMS.add(new Item("Trophy", new String[]{"metal", "weird"}, 2,
            ShapePart.rect(0, -8, 14, 4, 0),    // base
            ShapePart.rect(0, -4, 4, 10, 0),    // stem
            ShapePart.rect(0, 6, 16, 10, 0),    // cup body
            ShapePart.rect(-10, 2, 4, 6, 0),    // left handle
            ShapePart.rect(10, 2, 4, 6, 0)      // right handle
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

    /** Get a random item from the available pool for a shift. */
    public static Item getRandomItem(int shiftNumber) {
        List<Item> available = getItemsForShift(shiftNumber);
        if (available.isEmpty()) return ALL_ITEMS.get(0).copy();
        return available.get(rng.nextInt(available.size())).copy();
    }

    /** Get random items appropriate for the shift. */
    public static List<Item> getRandomItems(int shiftNumber, int count) {
        List<Item> available = getItemsForShift(shiftNumber);
        List<Item> result = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            if (available.isEmpty()) break;
            Item template = available.get(rng.nextInt(available.size()));
            result.add(template.copy());
        }

        return result;
    }

    /** Get random items that have at least one of the given forbidden tags. */
    public static List<Item> getForbiddenItems(int shiftNumber, String[] forbiddenTags, int count) {
        List<Item> available = getItemsForShift(shiftNumber);
        List<Item> forbidden = new ArrayList<>();
        for (Item item : available) {
            if (item.hasAnyTag(forbiddenTags)) {
                forbidden.add(item);
            }
        }
        if (forbidden.isEmpty()) return new ArrayList<>();

        List<Item> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Item template = forbidden.get(rng.nextInt(forbidden.size()));
            result.add(template.copy());
        }
        return result;
    }
}
