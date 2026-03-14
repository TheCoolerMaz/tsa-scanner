package com.tsascanner.game;

/**
 * Represents an item inside a bag.
 * Contains shape data for geometry-only rendering.
 * Uses a tag-based system for classification (e.g., "blade", "gun", "liquid").
 */
public class Item {

    public enum ShapeType {
        RECT,
        CIRCLE,
        TRIANGLE
    }

    /** A single shape primitive for rendering. */
    public static class ShapePart {
        public ShapeType type;
        public float offsetX, offsetY; // offset from item center
        public float width, height;    // for RECT; width = radius for CIRCLE
        public float rotation;         // degrees

        public ShapePart(ShapeType type, float offsetX, float offsetY,
                         float width, float height, float rotation) {
            this.type = type;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.width = width;
            this.height = height;
            this.rotation = rotation;
        }

        /** Convenience for a simple rectangle at origin. */
        public static ShapePart rect(float w, float h) {
            return new ShapePart(ShapeType.RECT, 0, 0, w, h, 0);
        }

        public static ShapePart rect(float ox, float oy, float w, float h) {
            return new ShapePart(ShapeType.RECT, ox, oy, w, h, 0);
        }

        public static ShapePart rect(float ox, float oy, float w, float h, float rot) {
            return new ShapePart(ShapeType.RECT, ox, oy, w, h, rot);
        }

        public static ShapePart circle(float ox, float oy, float radius) {
            return new ShapePart(ShapeType.CIRCLE, ox, oy, radius, radius, 0);
        }

        public static ShapePart circle(float radius) {
            return new ShapePart(ShapeType.CIRCLE, 0, 0, radius, radius, 0);
        }
    }

    /** Classification state for inspection. */
    public enum InspectionMark {
        UNMARKED,
        MARKED_CLEAR,
        MARKED_FORBIDDEN
    }

    public final String name;
    public final String[] tags;
    public final ShapePart[] parts;
    public final int minShift; // earliest shift this item can appear

    // Position within a bag (set when placed into a bag)
    public float bagX, bagY;

    // Inspection state
    public InspectionMark mark = InspectionMark.UNMARKED;

    public Item(String name, String[] tags, int minShift, ShapePart... parts) {
        this.name = name;
        this.tags = tags;
        this.minShift = minShift;
        this.parts = parts;
    }

    /** Check if the item has a specific tag. */
    public boolean hasTag(String tag) {
        for (String t : tags) {
            if (t.equals(tag)) return true;
        }
        return false;
    }

    /** Check if the item has any of the given tags. */
    public boolean hasAnyTag(String... checkTags) {
        for (String ct : checkTags) {
            for (String t : tags) {
                if (t.equals(ct)) return true;
            }
        }
        return false;
    }

    /** Create a copy for placement in a bag. */
    public Item copy() {
        ShapePart[] copied = new ShapePart[parts.length];
        for (int i = 0; i < parts.length; i++) {
            ShapePart p = parts[i];
            copied[i] = new ShapePart(p.type, p.offsetX, p.offsetY, p.width, p.height, p.rotation);
        }
        String[] tagsCopy = new String[tags.length];
        System.arraycopy(tags, 0, tagsCopy, 0, tags.length);
        return new Item(name, tagsCopy, minShift, copied);
    }
}
