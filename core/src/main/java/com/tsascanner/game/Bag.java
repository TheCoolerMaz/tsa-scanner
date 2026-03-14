package com.tsascanner.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A bag on the conveyor belt containing items.
 */
public class Bag {

    public enum BagState {
        ON_BELT,
        INSPECTING,
        PASSED,
        FLAGGED
    }

    public final List<Item> contents;
    public float x, y;
    public final float width, height;
    public BagState state;

    private static final Random rng = new Random();

    public Bag(List<Item> items, float width, float height) {
        this.contents = new ArrayList<>(items);
        this.width = width;
        this.height = height;
        this.state = BagState.ON_BELT;

        // Position items randomly within the bag bounds
        placeItems();
    }

    private void placeItems() {
        float padding = 6;
        for (Item item : contents) {
            // Find max extent of item parts for bounding
            float maxExtent = 0;
            for (Item.ShapePart part : item.parts) {
                float ex = Math.abs(part.offsetX) + part.width;
                float ey = Math.abs(part.offsetY) + part.height;
                maxExtent = Math.max(maxExtent, Math.max(ex, ey));
            }
            // Place with some margin from bag edges
            float margin = maxExtent / 2 + padding;
            float rangeX = width - margin * 2;
            float rangeY = height - margin * 2;
            if (rangeX < 1) rangeX = 1;
            if (rangeY < 1) rangeY = 1;

            item.bagX = -width / 2 + margin + rng.nextFloat() * rangeX;
            item.bagY = -height / 2 + margin + rng.nextFloat() * rangeY;
        }
    }

    public boolean containsWeapon() {
        for (Item item : contents) {
            if (item.isWeapon()) return true;
        }
        return false;
    }
}
