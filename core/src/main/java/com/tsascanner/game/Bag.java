package com.tsascanner.game;

import com.badlogic.gdx.graphics.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A bag on the conveyor belt containing items.
 */
public class Bag {

    public enum BagColor {
        BROWN(new Color(0.45f, 0.35f, 0.25f, 1f)),
        RED(new Color(0.7f, 0.2f, 0.2f, 1f)),
        BLUE(new Color(0.25f, 0.35f, 0.6f, 1f)),
        GREEN(new Color(0.25f, 0.5f, 0.3f, 1f));

        public final Color tint;
        BagColor(Color tint) { this.tint = tint; }
    }

    public enum BagState {
        ON_BELT,
        PULLED,       // pulled to inspection queue
        INSPECTING,   // currently being inspected
        PASSED,
        FLAGGED
    }

    public final List<Item> contents;
    public float x, y;
    public final float width, height;
    public BagState state;
    public BagColor color = BagColor.BROWN;
    public int bagNumber = 0;

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

    /** Check if the bag contains any item forbidden by the given shift config (context-aware). */
    public boolean containsForbidden(ShiftConfig config) {
        for (Item item : contents) {
            if (config.isForbidden(item, this)) return true;
        }
        return false;
    }
}
