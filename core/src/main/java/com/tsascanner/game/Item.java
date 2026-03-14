package com.tsascanner.game;

/**
 * Represents an item inside a bag.
 * Contains shape data for geometry-only rendering.
 */
public class Item {

    public enum ItemCategory {
        HARMLESS,
        WEAPON,
        DISGUISED_WEAPON,
        SUSPICIOUS_HARMLESS
    }

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

    public final String name;
    public final ItemCategory category;
    public final ShapePart[] parts;
    public final int minShift; // earliest shift this item can appear

    // Position within a bag (set when placed into a bag)
    public float bagX, bagY;

    public Item(String name, ItemCategory category, int minShift, ShapePart... parts) {
        this.name = name;
        this.category = category;
        this.minShift = minShift;
        this.parts = parts;
    }

    public boolean isWeapon() {
        return category == ItemCategory.WEAPON || category == ItemCategory.DISGUISED_WEAPON;
    }

    /** Create a copy for placement in a bag. */
    public Item copy() {
        ShapePart[] copied = new ShapePart[parts.length];
        for (int i = 0; i < parts.length; i++) {
            ShapePart p = parts[i];
            copied[i] = new ShapePart(p.type, p.offsetX, p.offsetY, p.width, p.height, p.rotation);
        }
        return new Item(name, category, minShift, copied);
    }
}
