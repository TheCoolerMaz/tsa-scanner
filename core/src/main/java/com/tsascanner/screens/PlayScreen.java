package com.tsascanner.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.tsascanner.TsaGame;
import com.tsascanner.game.Bag;
import com.tsascanner.game.BagGenerator;
import com.tsascanner.game.GameState;
import com.tsascanner.game.Item;
import com.tsascanner.game.ShiftConfig;
import com.tsascanner.screens.transitions.FadeTransition;

/**
 * Main gameplay screen — TSA checkpoint scanner.
 *
 * Split-screen layout:
 * - Top half: conveyor belt with bags, x-ray scan zone
 * - Bottom half: inspection zone for pulled bags
 */
public class PlayScreen extends GameScreen {

    // ===== Virtual resolution =====
    private static final float WORLD_W = 480;
    private static final float WORLD_H = 270;

    // ===== Colors =====
    private static final Color COL_BG            = new Color(0.08f, 0.08f, 0.14f, 1f);
    private static final Color COL_TOP_BAR       = new Color(0.12f, 0.12f, 0.20f, 1f);
    private static final Color COL_BELT          = new Color(0.25f, 0.25f, 0.28f, 1f);
    private static final Color COL_BELT_MARKS    = new Color(0.35f, 0.35f, 0.38f, 1f);
    private static final Color COL_BAG_NORMAL    = new Color(0.45f, 0.35f, 0.25f, 1f);
    private static final Color COL_BAG_OUTLINE   = new Color(0.55f, 0.45f, 0.35f, 1f);
    private static final Color COL_XRAY_BG       = new Color(0.05f, 0.10f, 0.25f, 0.85f);
    private static final Color COL_XRAY_OUTLINE  = new Color(0.15f, 0.30f, 0.60f, 1f);
    private static final Color COL_XRAY_ITEM     = new Color(1.0f, 0.6f, 0.1f, 1f);
    private static final Color COL_XRAY_GLOW     = new Color(1.0f, 0.7f, 0.2f, 0.3f);
    private static final Color COL_SCAN_ZONE     = new Color(0.05f, 0.12f, 0.30f, 0.25f);
    private static final Color COL_SCAN_BORDER   = new Color(0.2f, 0.4f, 0.8f, 0.6f);
    private static final Color COL_CORRECT       = new Color(0.2f, 0.9f, 0.3f, 1f);
    private static final Color COL_WRONG         = new Color(0.9f, 0.2f, 0.2f, 1f);
    private static final Color COL_HUD_TEXT      = new Color(0.85f, 0.85f, 0.90f, 1f);
    private static final Color COL_OVERLAY       = new Color(0f, 0f, 0f, 0.75f);
    private static final Color COL_DIVIDER       = new Color(0.3f, 0.3f, 0.4f, 1f);
    private static final Color COL_INSPECT_BG    = new Color(0.06f, 0.06f, 0.12f, 1f);
    private static final Color COL_REF_CARD      = new Color(0.10f, 0.10f, 0.18f, 1f);
    private static final Color COL_SELECTED      = new Color(0.3f, 0.7f, 1.0f, 1f);
    private static final Color COL_MARK_CLEAR    = new Color(0.2f, 0.9f, 0.3f, 0.8f);
    private static final Color COL_MARK_FORBID   = new Color(0.9f, 0.2f, 0.2f, 0.8f);
    private static final Color COL_DIM_TEXT      = new Color(0.5f, 0.5f, 0.6f, 1f);

    // ===== Layout — Conveyor Zone (top half) =====
    private static final float TOP_BAR_Y = 250;
    private static final float TOP_BAR_H = 20;
    private static final float CONVEYOR_BOTTOM = 150;
    private static final float BELT_Y = 160;
    private static final float BELT_H = 25;
    private static final float SCAN_ZONE_X = 140;
    private static final float SCAN_ZONE_W = 200;
    private static final float SCAN_ZONE_TOP = 240;

    // ===== Layout — Inspection Zone (bottom half) =====
    private static final float INSPECT_TOP = 145;
    private static final float INSPECT_BOTTOM = 0;
    private static final float INSPECT_ITEMS_Y = 40;
    private static final float INSPECT_ITEMS_H = 80;
    private static final float INSPECT_ITEM_SPACING = 65;
    private static final float INSPECT_ITEM_SIZE = 50;
    private static final float REF_CARD_X = 320;
    private static final float REF_CARD_W = 155;

    // ===== Rendering =====
    private OrthographicCamera camera;
    private FitViewport viewport;
    private SpriteBatch batch;
    private ShapeRenderer shapes;
    private BitmapFont font;
    private GlyphLayout layout;

    // ===== Game State =====
    private GameState state;
    private Bag currentBeltBag;
    private float beltAnimOffset;
    private boolean waitingForNextBag;
    private float nextBagDelay;
    private boolean showResults;
    private boolean speedBoost;

    // ===== Feedback flash =====
    private float flashTimer;
    private Color flashColor;

    // Mouse unprojection helper
    private final Vector3 mouseWorld = new Vector3();

    @Override
    public void show() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_W, WORLD_H, camera);
        viewport.apply(true);

        batch = new SpriteBatch();
        shapes = new ShapeRenderer();
        layout = new GlyphLayout();

        // Get font — fallback to default BitmapFont if not loaded
        BitmapFont assetFont = TsaGame.INSTANCE.assets.getFont();
        font = (assetFont != null) ? assetFont : new BitmapFont();

        // Init game state
        state = new GameState();
        state.reset(1);

        showResults = false;
        flashTimer = 0;
        beltAnimOffset = 0;
        waitingForNextBag = false;
        currentBeltBag = null;
    }

    @Override
    public void render(float delta) {
        delta = Math.min(delta, 0.05f);

        if (showResults) {
            handleResultsInput();
        } else if (state.showBriefing) {
            handleBriefingInput();
        } else {
            handleInput(delta);
            update(delta);
        }

        // Clear
        Gdx.gl.glClearColor(COL_BG.r, COL_BG.g, COL_BG.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        camera.update();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapes.setProjectionMatrix(camera.combined);

        drawBackground();
        drawConveyorBelt();
        drawScanZone();
        if (currentBeltBag != null) {
            drawBeltBag();
        }
        drawDivider();
        drawInspectionZone();
        drawFlash();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        drawHUD();
        drawFeedback();
        drawInspectionText();
        batch.end();

        if (state.showBriefing) {
            drawBriefing();
        }

        if (showResults) {
            drawResults();
        }
    }

    // ==================== UPDATE ====================

    private void update(float delta) {
        if (state.shiftOver) {
            showResults = true;
            return;
        }

        // Update shift timer
        state.shiftTimer += delta;
        if (state.shiftTimer >= state.shiftConfig.shiftDuration) {
            state.shiftOver = true;
            showResults = true;
            return;
        }

        // Belt speed with optional 2x boost
        float speed = state.shiftConfig.beltSpeed * (speedBoost ? 2f : 1f);

        // Animate belt
        beltAnimOffset += speed * delta;
        if (beltAnimOffset > 20) beltAnimOffset -= 20;

        // Feedback timer
        if (state.feedbackTimer > 0) {
            state.feedbackTimer -= delta;
        }

        // Flash timer
        if (flashTimer > 0) {
            flashTimer -= delta;
        }

        // Waiting for next bag delay
        if (waitingForNextBag) {
            nextBagDelay -= delta;
            if (nextBagDelay <= 0) {
                waitingForNextBag = false;
                spawnNextBeltBag();
            }
            return;
        }

        // Move current belt bag
        if (currentBeltBag != null) {
            currentBeltBag.x += speed * delta;

            // Check if bag has left the screen — auto-pass
            if (currentBeltBag.x > WORLD_W + 20) {
                state.autoPassed(currentBeltBag);
                if (currentBeltBag.containsForbidden(state.shiftConfig)) {
                    triggerFlash(false);
                }
                currentBeltBag = null;
                waitingForNextBag = true;
                nextBagDelay = 0.3f;
            }
        }
    }

    /** Is the belt bag currently overlapping the scan zone? */
    private boolean bagInScanZone() {
        if (currentBeltBag == null) return false;
        float bagRight = currentBeltBag.x + currentBeltBag.width;
        return bagRight > SCAN_ZONE_X && currentBeltBag.x < SCAN_ZONE_X + SCAN_ZONE_W;
    }

    // ==================== INPUT ====================

    private void handleInput(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            manager.pop(new FadeTransition(0.3f));
            return;
        }

        // Hold SPACE for 2x belt speed
        speedBoost = Gdx.input.isKeyPressed(Input.Keys.SPACE);

        // D — pull bag to inspection (only while in scan zone)
        if (currentBeltBag != null && bagInScanZone()) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.D)) {
                state.pullBag(currentBeltBag);
                currentBeltBag = null;
                waitingForNextBag = true;
                nextBagDelay = 0.3f;
            }
        }

        // Inspection controls — only when a bag is being inspected
        if (state.currentInspectionBag != null) {
            // Mouse click to select item
            if (Gdx.input.justTouched()) {
                mouseWorld.set(Gdx.input.getX(), Gdx.input.getY(), 0);
                camera.unproject(mouseWorld);

                // Check if click is in the inspection items area
                if (mouseWorld.y >= INSPECT_BOTTOM && mouseWorld.y <= INSPECT_TOP) {
                    int count = state.currentInspectionBag.contents.size();
                    float startX = 10;
                    for (int i = 0; i < count; i++) {
                        float ix = startX + i * INSPECT_ITEM_SPACING + INSPECT_ITEM_SIZE / 2;
                        float iy = INSPECT_ITEMS_Y + INSPECT_ITEMS_H / 2;
                        float halfSize = INSPECT_ITEM_SIZE / 2 + 4;
                        if (mouseWorld.x >= ix - halfSize && mouseWorld.x <= ix + halfSize
                            && mouseWorld.y >= iy - halfSize && mouseWorld.y <= iy + halfSize) {
                            state.selectedItemIndex = i;
                            break;
                        }
                    }
                }
            }

            // A — mark selected as CLEAR
            if (Gdx.input.isKeyJustPressed(Input.Keys.A)) {
                state.markSelectedClear();
            }

            // S — mark selected as FORBIDDEN
            if (Gdx.input.isKeyJustPressed(Input.Keys.S)) {
                state.markSelectedForbidden();
            }

            // ENTER — submit inspected bag
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                boolean allCorrect = state.submitInspection();
                triggerFlash(allCorrect);
            }
        }
    }

    private void handleBriefingInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            state.showBriefing = false;
            spawnNextBeltBag();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            manager.pop(new FadeTransition(0.3f));
        }
    }

    private void handleResultsInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) ||
            Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {

            String rating = state.getRating();
            if (rating.equals("FIRED") || state.shiftNumber >= 5) {
                manager.pop(new FadeTransition(0.3f));
            } else {
                state.nextShift();
                showResults = false;
                currentBeltBag = null;
                waitingForNextBag = false;
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            manager.pop(new FadeTransition(0.3f));
        }
    }

    // ==================== HELPERS ====================

    private void spawnNextBeltBag() {
        ShiftConfig config = state.shiftConfig;
        float forbiddenChance = 0.38f;
        if (MathUtils.random() < forbiddenChance) {
            currentBeltBag = BagGenerator.generateForbiddenBag(config);
        } else {
            currentBeltBag = BagGenerator.generateCleanBag(config);
        }
        currentBeltBag.x = -currentBeltBag.width - MathUtils.random(10f, 40f);
        currentBeltBag.y = BELT_Y + BELT_H;
    }

    private void triggerFlash(boolean correct) {
        flashTimer = 0.3f;
        flashColor = correct ? COL_CORRECT : COL_WRONG;
    }

    // ==================== DRAWING ====================

    private void drawBackground() {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        // Top bar
        shapes.setColor(COL_TOP_BAR);
        shapes.rect(0, TOP_BAR_Y, WORLD_W, TOP_BAR_H);
        // Inspection zone background
        shapes.setColor(COL_INSPECT_BG);
        shapes.rect(0, INSPECT_BOTTOM, WORLD_W, INSPECT_TOP);
        shapes.end();
    }

    private void drawConveyorBelt() {
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        // Belt body
        shapes.setColor(COL_BELT);
        shapes.rect(0, BELT_Y, WORLD_W, BELT_H);

        // Animated hash marks
        shapes.setColor(COL_BELT_MARKS);
        for (float mx = -20 + beltAnimOffset; mx < WORLD_W + 20; mx += 20) {
            shapes.rect(mx, BELT_Y + 2, 8, 2);
            shapes.rect(mx, BELT_Y + BELT_H - 4, 8, 2);
        }

        shapes.end();

        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(COL_BELT_MARKS);
        shapes.line(0, BELT_Y, WORLD_W, BELT_Y);
        shapes.line(0, BELT_Y + BELT_H, WORLD_W, BELT_Y + BELT_H);
        shapes.end();
    }

    private void drawScanZone() {
        float zoneH = SCAN_ZONE_TOP - BELT_Y;

        // Semi-transparent scan zone fill
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(COL_SCAN_ZONE);
        shapes.rect(SCAN_ZONE_X, BELT_Y, SCAN_ZONE_W, zoneH);
        shapes.end();

        // Scan zone border
        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(COL_SCAN_BORDER);
        shapes.rect(SCAN_ZONE_X, BELT_Y, SCAN_ZONE_W, zoneH);

        // Scan line accents
        shapes.setColor(COL_SCAN_BORDER.r, COL_SCAN_BORDER.g, COL_SCAN_BORDER.b, 0.3f);
        shapes.line(SCAN_ZONE_X, BELT_Y + zoneH / 3, SCAN_ZONE_X + SCAN_ZONE_W, BELT_Y + zoneH / 3);
        shapes.line(SCAN_ZONE_X, BELT_Y + zoneH * 2 / 3, SCAN_ZONE_X + SCAN_ZONE_W, BELT_Y + zoneH * 2 / 3);
        shapes.end();
    }

    private void drawBeltBag() {
        float bx = currentBeltBag.x;
        float by = currentBeltBag.y;
        float bw = currentBeltBag.width;
        float bh = currentBeltBag.height;

        boolean inZone = bagInScanZone();

        if (inZone) {
            // X-ray view — all items in same neutral orange color
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            shapes.setColor(COL_XRAY_BG);
            shapes.rect(bx, by, bw, bh);
            shapes.end();

            // Draw items — all in same orange (no debug colors)
            drawBeltItems(bx + bw / 2, by + bh / 2);

            // Bag outline
            shapes.begin(ShapeRenderer.ShapeType.Line);
            shapes.setColor(COL_XRAY_OUTLINE);
            shapes.rect(bx, by, bw, bh);
            shapes.end();
        } else {
            // Normal view — opaque bag
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            shapes.setColor(COL_BAG_NORMAL);
            shapes.rect(bx, by, bw, bh);

            // Handle straps
            shapes.setColor(COL_BAG_OUTLINE);
            shapes.rect(bx + bw * 0.3f, by + bh, bw * 0.1f, 6);
            shapes.rect(bx + bw * 0.6f, by + bh, bw * 0.1f, 6);
            shapes.end();

            // Bag outline
            shapes.begin(ShapeRenderer.ShapeType.Line);
            shapes.setColor(COL_BAG_OUTLINE);
            shapes.rect(bx, by, bw, bh);
            shapes.end();
        }
    }

    /** Draw items on the belt — all in the same neutral orange color. */
    private void drawBeltItems(float centerX, float centerY) {
        for (Item item : currentBeltBag.contents) {
            float ix = centerX + item.bagX;
            float iy = centerY + item.bagY;

            for (Item.ShapePart part : item.parts) {
                float px = ix + part.offsetX;
                float py = iy + part.offsetY;

                // Glow
                drawShapePart(part, px, py, COL_XRAY_GLOW, 2f, true);
                // Solid
                drawShapePart(part, px, py, COL_XRAY_ITEM, 0f, true);
                // Outline
                drawShapePart(part, px, py, COL_XRAY_ITEM, 0f, false);
            }
        }
    }

    private void drawDivider() {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(COL_DIVIDER);
        shapes.rect(0, INSPECT_TOP, WORLD_W, 2);
        shapes.end();
    }

    private void drawInspectionZone() {
        // Reference card background (right side)
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(COL_REF_CARD);
        shapes.rect(REF_CARD_X, INSPECT_BOTTOM, REF_CARD_W, INSPECT_TOP);
        shapes.end();

        // Reference card border
        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(COL_DIVIDER);
        shapes.rect(REF_CARD_X, INSPECT_BOTTOM, REF_CARD_W, INSPECT_TOP);
        shapes.end();

        // Draw inspection items if a bag is being inspected
        if (state.currentInspectionBag != null) {
            drawInspectionItems();
        }
    }

    /** Draw inspection items laid out in a row. */
    private void drawInspectionItems() {
        Bag bag = state.currentInspectionBag;
        int count = bag.contents.size();
        float startX = 10;

        for (int i = 0; i < count; i++) {
            Item item = bag.contents.get(i);
            float ix = startX + i * INSPECT_ITEM_SPACING + INSPECT_ITEM_SIZE / 2;
            float iy = INSPECT_ITEMS_Y + INSPECT_ITEMS_H / 2;

            // Item box background
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            shapes.setColor(COL_XRAY_BG);
            shapes.rect(ix - INSPECT_ITEM_SIZE / 2, iy - INSPECT_ITEM_SIZE / 2,
                        INSPECT_ITEM_SIZE, INSPECT_ITEM_SIZE);
            shapes.end();

            // Draw the item parts (scaled to fit in the box)
            float scale = getItemScale(item);
            for (Item.ShapePart part : item.parts) {
                float px = ix + part.offsetX * scale;
                float py = iy + part.offsetY * scale;

                Item.ShapePart scaled = new Item.ShapePart(part.type, 0, 0,
                    part.width * scale, part.height * scale, part.rotation);
                drawShapePart(scaled, px, py, COL_XRAY_ITEM, 0f, true);
            }

            // Selection highlight
            if (i == state.selectedItemIndex) {
                shapes.begin(ShapeRenderer.ShapeType.Line);
                shapes.setColor(COL_SELECTED);
                float pad = 3;
                shapes.rect(ix - INSPECT_ITEM_SIZE / 2 - pad, iy - INSPECT_ITEM_SIZE / 2 - pad,
                            INSPECT_ITEM_SIZE + pad * 2, INSPECT_ITEM_SIZE + pad * 2);
                shapes.rect(ix - INSPECT_ITEM_SIZE / 2 - pad + 1, iy - INSPECT_ITEM_SIZE / 2 - pad + 1,
                            INSPECT_ITEM_SIZE + pad * 2 - 2, INSPECT_ITEM_SIZE + pad * 2 - 2);
                shapes.end();
            }

            // Mark overlay
            if (item.mark == Item.InspectionMark.MARKED_CLEAR) {
                drawCheckMark(ix, iy);
            } else if (item.mark == Item.InspectionMark.MARKED_FORBIDDEN) {
                drawXMark(ix, iy);
            }

            // Item box border
            shapes.begin(ShapeRenderer.ShapeType.Line);
            shapes.setColor(COL_DIVIDER);
            shapes.rect(ix - INSPECT_ITEM_SIZE / 2, iy - INSPECT_ITEM_SIZE / 2,
                        INSPECT_ITEM_SIZE, INSPECT_ITEM_SIZE);
            shapes.end();
        }
    }

    /** Calculate scale factor to fit item parts within inspection box. */
    private float getItemScale(Item item) {
        float maxExtent = 1;
        for (Item.ShapePart part : item.parts) {
            float ex = Math.abs(part.offsetX) + part.width / 2;
            float ey = Math.abs(part.offsetY) + part.height / 2;
            maxExtent = Math.max(maxExtent, Math.max(ex, ey));
        }
        float targetSize = INSPECT_ITEM_SIZE / 2 - 4;
        return Math.min(1.0f, targetSize / maxExtent);
    }

    /** Draw a green check mark overlay. */
    private void drawCheckMark(float cx, float cy) {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(COL_MARK_CLEAR);
        // Simple check: two rectangles forming a V shape
        shapes.rect(cx - 8, cy - 2, 8, 3, 8, 3, 1, 1, -45);
        shapes.rect(cx - 2, cy - 2, 14, 3, 14, 3, 1, 1, 30);
        shapes.end();
    }

    /** Draw a red X mark overlay. */
    private void drawXMark(float cx, float cy) {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(COL_MARK_FORBID);
        shapes.rect(cx - 1.5f, cy - 1.5f, 1.5f, 1.5f, 16, 3, 1, 1, 45);
        shapes.rect(cx - 1.5f, cy - 1.5f, 1.5f, 1.5f, 16, 3, 1, 1, -45);
        shapes.end();
    }

    private void drawShapePart(Item.ShapePart part, float px, float py,
                                Color color, float inflate, boolean filled) {
        if (filled) {
            shapes.begin(ShapeRenderer.ShapeType.Filled);
        } else {
            shapes.begin(ShapeRenderer.ShapeType.Line);
        }
        shapes.setColor(color);

        float w = part.width + inflate * 2;
        float h = part.height + inflate * 2;

        switch (part.type) {
            case RECT:
                if (part.rotation != 0) {
                    shapes.rect(px - w / 2, py - h / 2, w / 2, h / 2,
                                w, h, 1f, 1f, part.rotation);
                } else {
                    shapes.rect(px - w / 2, py - h / 2, w, h);
                }
                break;

            case CIRCLE:
                shapes.circle(px, py, w / 2, 16);
                break;

            case TRIANGLE:
                shapes.triangle(
                    px, py + h / 2,
                    px - w / 2, py - h / 2,
                    px + w / 2, py - h / 2
                );
                break;
        }

        shapes.end();
    }

    private void drawFlash() {
        if (flashTimer > 0 && flashColor != null) {
            float alpha = flashTimer / 0.3f * 0.25f;
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            shapes.setColor(flashColor.r, flashColor.g, flashColor.b, alpha);
            shapes.rect(0, 0, WORLD_W, WORLD_H);
            shapes.end();
        }
    }

    private void drawHUD() {
        font.setColor(COL_HUD_TEXT);
        float scale = font.getScaleX();
        font.getData().setScale(0.8f);

        // Top bar
        font.draw(batch, "SHIFT " + state.shiftNumber, 8, TOP_BAR_Y + 15);
        font.draw(batch, "SCORE: " + state.score, 100, TOP_BAR_Y + 15);
        font.draw(batch, "TIME: " + state.getTimeRemaining(), 210, TOP_BAR_Y + 15);

        // Strikes
        String strikesText = "X:" + state.strikes + "/" + GameState.MAX_STRIKES;
        if (state.strikes > 0) {
            font.setColor(COL_WRONG);
        }
        font.draw(batch, strikesText, 330, TOP_BAR_Y + 15);
        font.setColor(COL_HUD_TEXT);

        // Queue count
        String queueText = "Q:" + state.inspectionQueue.size();
        if (state.currentInspectionBag != null) {
            queueText += "+1";
        }
        font.draw(batch, queueText, 420, TOP_BAR_Y + 15);

        // Conveyor zone prompt
        font.getData().setScale(0.6f);
        if (currentBeltBag != null && bagInScanZone()) {
            font.setColor(COL_SCAN_BORDER);
            String prompt = "[D] PULL TO INSPECT";
            layout.setText(font, prompt);
            font.draw(batch, prompt, SCAN_ZONE_X + (SCAN_ZONE_W - layout.width) / 2, SCAN_ZONE_TOP + 8);
            font.setColor(COL_HUD_TEXT);
        }

        // Speed indicator
        if (speedBoost) {
            font.setColor(COL_CORRECT);
            font.draw(batch, ">> 2x SPEED >>", 8, CONVEYOR_BOTTOM + 8);
            font.setColor(COL_HUD_TEXT);
        }

        font.getData().setScale(scale);
    }

    /** Draw text in the inspection zone. */
    private void drawInspectionText() {
        float scale = font.getScaleX();

        // Reference card text (right side)
        font.setColor(COL_HUD_TEXT);
        font.getData().setScale(0.6f);
        font.draw(batch, "FORBIDDEN:", REF_CARD_X + 5, INSPECT_TOP - 5);

        font.getData().setScale(0.5f);
        ShiftConfig config = state.shiftConfig;
        float tagY = INSPECT_TOP - 20;
        for (String tag : config.forbiddenTags) {
            font.setColor(COL_WRONG);
            font.draw(batch, "* " + tag.toUpperCase(), REF_CARD_X + 8, tagY);
            tagY -= 12;
        }

        // Controls hint at bottom of inspection zone
        font.setColor(COL_DIM_TEXT);
        font.getData().setScale(0.5f);
        font.draw(batch, "[A] CLEAR  [S] FORBID  [ENTER] SUBMIT", 8, 12);

        if (state.currentInspectionBag != null) {
            // Item name when selected
            if (state.selectedItemIndex >= 0 && state.selectedItemIndex < state.currentInspectionBag.contents.size()) {
                Item sel = state.currentInspectionBag.contents.get(state.selectedItemIndex);
                font.setColor(COL_SELECTED);
                font.getData().setScale(0.6f);
                font.draw(batch, sel.name, 10, INSPECT_ITEMS_Y + INSPECT_ITEMS_H + 18);

                // Show tags
                font.getData().setScale(0.45f);
                font.setColor(COL_DIM_TEXT);
                StringBuilder tagStr = new StringBuilder("Tags: ");
                for (int t = 0; t < sel.tags.length; t++) {
                    if (t > 0) tagStr.append(", ");
                    tagStr.append(sel.tags[t]);
                }
                font.draw(batch, tagStr.toString(), 10, INSPECT_ITEMS_Y + INSPECT_ITEMS_H + 8);
            }

            // Bag items count
            font.setColor(COL_HUD_TEXT);
            font.getData().setScale(0.5f);
            int marked = 0;
            for (Item it : state.currentInspectionBag.contents) {
                if (it.mark != Item.InspectionMark.UNMARKED) marked++;
            }
            String bagInfo = "Items: " + marked + "/" + state.currentInspectionBag.contents.size() + " marked";
            font.draw(batch, bagInfo, 10, 25);
        } else {
            // No bag being inspected
            font.setColor(COL_DIM_TEXT);
            font.getData().setScale(0.6f);
            String noBag = "NO BAG - Pull suspicious bags with [D]";
            layout.setText(font, noBag);
            font.draw(batch, noBag, (REF_CARD_X - layout.width) / 2, INSPECT_ITEMS_Y + INSPECT_ITEMS_H / 2 + 5);
        }

        font.getData().setScale(scale);
        font.setColor(COL_HUD_TEXT);
    }

    private void drawFeedback() {
        if (state.feedbackTimer > 0 && state.feedbackText != null) {
            float alpha = Math.min(1f, state.feedbackTimer / 0.5f);
            Color col = state.feedbackCorrect ? COL_CORRECT : COL_WRONG;
            font.setColor(col.r, col.g, col.b, alpha);

            font.getData().setScale(1.0f);
            layout.setText(font, state.feedbackText);
            font.draw(batch, state.feedbackText, (WORLD_W - layout.width) / 2,
                      CONVEYOR_BOTTOM + 45);

            font.getData().setScale(0.8f);
            font.setColor(COL_HUD_TEXT);
        }
    }

    private void drawBriefing() {
        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(COL_OVERLAY);
        shapes.rect(0, 0, WORLD_W, WORLD_H);
        shapes.end();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        font.setColor(COL_HUD_TEXT);

        ShiftConfig config = state.shiftConfig;

        // Title
        font.getData().setScale(1.0f);
        layout.setText(font, config.briefingTitle);
        font.draw(batch, config.briefingTitle, (WORLD_W - layout.width) / 2, 220);

        // Briefing lines
        font.getData().setScale(0.7f);
        float lineY = 185;
        for (String line : config.briefingLines) {
            if (line.isEmpty()) {
                lineY -= 10;
                continue;
            }
            layout.setText(font, line);
            font.draw(batch, line, (WORLD_W - layout.width) / 2, lineY);
            lineY -= 18;
        }

        // Prompt
        font.getData().setScale(0.6f);
        font.setColor(COL_SCAN_BORDER);
        String prompt = "Press ENTER to begin";
        layout.setText(font, prompt);
        font.draw(batch, prompt, (WORLD_W - layout.width) / 2, 60);

        font.setColor(COL_HUD_TEXT);
        font.getData().setScale(0.8f);
        batch.end();
    }

    private void drawResults() {
        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(COL_OVERLAY);
        shapes.rect(0, 0, WORLD_W, WORLD_H);
        shapes.end();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        font.setColor(COL_HUD_TEXT);

        String rating = state.getRating();

        font.getData().setScale(1.0f);
        String title = "SHIFT " + state.shiftNumber + " COMPLETE";
        if (rating.equals("FIRED")) title = "YOU'RE FIRED!";
        layout.setText(font, title);
        font.draw(batch, title, (WORLD_W - layout.width) / 2, 220);

        font.getData().setScale(0.7f);

        int totalBags = state.bagsPassed + state.bagsInspected;
        String[] lines = {
            "Bags Processed: " + totalBags + " (passed: " + state.bagsPassed + ", inspected: " + state.bagsInspected + ")",
            "Items Classified: " + state.totalClassifications,
            "Correct: " + state.itemsClassifiedCorrectly + "  Wrong: " + state.itemsClassifiedIncorrectly,
            "Accuracy: " + String.format("%.0f%%", state.getAccuracy()),
            "Score: " + state.score,
            "Rating: " + rating
        };

        float lineY = 185;
        for (String line : lines) {
            layout.setText(font, line);
            font.draw(batch, line, (WORLD_W - layout.width) / 2, lineY);
            lineY -= 18;
        }

        font.getData().setScale(0.6f);
        String prompt;
        if (rating.equals("FIRED") || state.shiftNumber >= 5) {
            prompt = "Press ENTER to return to menu";
        } else {
            prompt = "Press ENTER for next shift";
        }
        layout.setText(font, prompt);
        font.draw(batch, prompt, (WORLD_W - layout.width) / 2, 60);

        font.getData().setScale(0.8f);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (shapes != null) shapes.dispose();
    }
}
