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
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.tsascanner.TsaGame;
import com.tsascanner.game.Bag;
import com.tsascanner.game.BagGenerator;
import com.tsascanner.game.GameState;
import com.tsascanner.game.Item;
import com.tsascanner.screens.transitions.FadeTransition;

/**
 * Main gameplay screen — TSA checkpoint scanner.
 *
 * Bags move continuously on the conveyor. X-ray is always active within
 * the scan zone — player must flag/pass while the bag is inside the box.
 */
public class PlayScreen extends GameScreen {

    // ===== Virtual resolution =====
    private static final float WORLD_W = 480;
    private static final float WORLD_H = 270;

    // ===== Colors =====
    private static final Color COL_BG            = new Color(0.08f, 0.08f, 0.14f, 1f);
    private static final Color COL_TOP_BAR       = new Color(0.12f, 0.12f, 0.20f, 1f);
    private static final Color COL_BOTTOM_BAR    = new Color(0.10f, 0.10f, 0.18f, 1f);
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

    // ===== Layout =====
    private static final float TOP_BAR_Y = 250;
    private static final float TOP_BAR_H = 20;
    private static final float BOTTOM_BAR_Y = 0;
    private static final float BOTTOM_BAR_H = 45;
    private static final float BELT_Y = 65;
    private static final float BELT_H = 35;
    private static final float SCAN_ZONE_X = 140;
    private static final float SCAN_ZONE_W = 200;
    private static final float SCAN_ZONE_TOP = 215;

    // ===== Rendering =====
    private OrthographicCamera camera;
    private FitViewport viewport;
    private SpriteBatch batch;
    private ShapeRenderer shapes;
    private BitmapFont font;
    private GlyphLayout layout;

    // ===== Game State =====
    private GameState state;
    private Bag currentBag;
    private float beltAnimOffset;
    private boolean waitingForNextBag;
    private float nextBagDelay;
    private boolean showResults;
    private boolean decided; // has the player made a decision on the current bag?

    // ===== Feedback flash =====
    private float flashTimer;
    private Color flashColor;

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

        // Spawn first bag
        spawnNextBag();

        showResults = false;
        flashTimer = 0;
        beltAnimOffset = 0;
        waitingForNextBag = false;
    }

    @Override
    public void render(float delta) {
        delta = Math.min(delta, 0.05f);

        if (showResults) {
            handleResultsInput();
        } else {
            handleInput();
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
        if (currentBag != null) {
            drawBag();
        }
        drawFlash();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        drawHUD();
        drawFeedback();
        batch.end();

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
        if (state.shiftTimer >= state.shiftDuration) {
            state.shiftOver = true;
            showResults = true;
            return;
        }

        // Animate belt
        beltAnimOffset += state.getBeltSpeed() * delta;
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
                spawnNextBag();
            }
            return;
        }

        // Move current bag — bags always move
        if (currentBag != null) {
            currentBag.x += state.getBeltSpeed() * delta;

            // Check if bag has left the scan zone without a decision
            if (!decided && bagPastScanZone()) {
                // Auto-pass: player didn't act in time
                state.autoPass(currentBag);
                triggerFlash(!currentBag.containsWeapon());
                decided = true;
            }

            // Check if bag has exited the screen
            if (currentBag.x > WORLD_W + 20) {
                currentBag = null;
                waitingForNextBag = true;
                nextBagDelay = 0.3f;
            }
        }
    }

    /** Is the bag currently overlapping the scan zone? */
    private boolean bagInScanZone() {
        if (currentBag == null) return false;
        float bagRight = currentBag.x + currentBag.width;
        return bagRight > SCAN_ZONE_X && currentBag.x < SCAN_ZONE_X + SCAN_ZONE_W;
    }

    /** Has the bag moved fully past the scan zone? */
    private boolean bagPastScanZone() {
        if (currentBag == null) return false;
        return currentBag.x > SCAN_ZONE_X + SCAN_ZONE_W;
    }

    // ==================== INPUT ====================

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            manager.pop(new FadeTransition(0.3f));
            return;
        }

        // Can only act on a bag while it's in the scan zone and undecided
        if (currentBag != null && !decided && bagInScanZone()) {
            // Pass
            if (Gdx.input.isKeyJustPressed(Input.Keys.A)) {
                boolean wasWeapon = currentBag.containsWeapon();
                state.scorePass(currentBag);
                triggerFlash(!wasWeapon);
                decided = true;
            }

            // Flag
            if (Gdx.input.isKeyJustPressed(Input.Keys.D)) {
                boolean wasWeapon = currentBag.containsWeapon();
                state.scoreFlag(currentBag);
                triggerFlash(wasWeapon);
                decided = true;
            }
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
                spawnNextBag();
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            manager.pop(new FadeTransition(0.3f));
        }
    }

    // ==================== HELPERS ====================

    private void spawnNextBag() {
        float weaponChance = 0.35f;
        if (MathUtils.random() < weaponChance) {
            currentBag = BagGenerator.generateWeaponBag(state.shiftNumber);
        } else {
            currentBag = BagGenerator.generate(state.shiftNumber);
        }
        currentBag.x = -currentBag.width - MathUtils.random(10f, 40f);
        currentBag.y = BELT_Y + BELT_H;
        decided = false;
    }

    private void triggerFlash(boolean correct) {
        flashTimer = 0.3f;
        flashColor = correct ? COL_CORRECT : COL_WRONG;
    }

    // ==================== DRAWING ====================

    private void drawBackground() {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(COL_TOP_BAR);
        shapes.rect(0, TOP_BAR_Y, WORLD_W, TOP_BAR_H);
        shapes.setColor(COL_BOTTOM_BAR);
        shapes.rect(0, BOTTOM_BAR_Y, WORLD_W, BOTTOM_BAR_H);
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

        // Scan line accents at top and bottom
        shapes.setColor(COL_SCAN_BORDER.r, COL_SCAN_BORDER.g, COL_SCAN_BORDER.b, 0.3f);
        shapes.line(SCAN_ZONE_X, BELT_Y + zoneH / 3, SCAN_ZONE_X + SCAN_ZONE_W, BELT_Y + zoneH / 3);
        shapes.line(SCAN_ZONE_X, BELT_Y + zoneH * 2 / 3, SCAN_ZONE_X + SCAN_ZONE_W, BELT_Y + zoneH * 2 / 3);
        shapes.end();
    }

    private void drawBag() {
        float bx = currentBag.x;
        float by = currentBag.y;
        float bw = currentBag.width;
        float bh = currentBag.height;

        boolean inZone = bagInScanZone();

        if (inZone && !decided) {
            // X-ray view — automatic when inside scan zone
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            shapes.setColor(COL_XRAY_BG);
            shapes.rect(bx, by, bw, bh);
            shapes.end();

            // Draw items
            drawItems(bx + bw / 2, by + bh / 2);

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

    private void drawItems(float centerX, float centerY) {
        for (Item item : currentBag.contents) {
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
        font.draw(batch, "SCORE: " + state.score, 120, TOP_BAR_Y + 15);
        font.draw(batch, "TIME: " + state.getTimeRemaining(), 260, TOP_BAR_Y + 15);
        font.draw(batch, "BAGS: " + state.bagsProcessed + "/" + state.bagsRequired, 380, TOP_BAR_Y + 15);

        // Strikes
        if (state.strikes > 0) {
            font.setColor(COL_WRONG);
            String strikesText = "STRIKES: " + state.strikes + "/" + GameState.MAX_STRIKES;
            layout.setText(font, strikesText);
            font.draw(batch, strikesText, WORLD_W - layout.width - 8, TOP_BAR_Y + 15);
            font.setColor(COL_HUD_TEXT);
        }

        // Bottom bar
        font.getData().setScale(0.7f);
        font.draw(batch, "[A] PASS    [D] FLAG", 8, BOTTOM_BAR_H - 10);

        // Streak
        String streakText = "STREAK: x" + (state.streak > 0 ? String.format("%.1f", state.multiplier) : "1");
        layout.setText(font, streakText);
        font.draw(batch, streakText, WORLD_W - layout.width - 8, BOTTOM_BAR_H - 10);

        // Scan zone prompt when bag is inside
        if (currentBag != null && bagInScanZone() && !decided) {
            font.setColor(COL_SCAN_BORDER);
            font.getData().setScale(0.6f);
            String prompt = "SCANNING...";
            layout.setText(font, prompt);
            font.draw(batch, prompt, SCAN_ZONE_X + (SCAN_ZONE_W - layout.width) / 2, SCAN_ZONE_TOP + 12);
            font.setColor(COL_HUD_TEXT);
        }

        font.getData().setScale(scale);
    }

    private void drawFeedback() {
        if (state.feedbackTimer > 0 && state.feedbackText != null) {
            float alpha = Math.min(1f, state.feedbackTimer / 0.5f);
            Color col = state.feedbackCorrect ? COL_CORRECT : COL_WRONG;
            font.setColor(col.r, col.g, col.b, alpha);

            font.getData().setScale(1.2f);
            layout.setText(font, state.feedbackText);
            font.draw(batch, state.feedbackText, (WORLD_W - layout.width) / 2, 240);

            font.getData().setScale(0.8f);
            font.setColor(COL_HUD_TEXT);
        }
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
        font.draw(batch, title, (WORLD_W - layout.width) / 2, 210);

        font.getData().setScale(0.7f);

        String[] lines = {
            "Bags Processed: " + state.bagsProcessed,
            "Accuracy: " + String.format("%.0f%%", state.getAccuracy()),
            "Score: " + state.score,
            "Rating: " + rating
        };

        float lineY = 180;
        for (String line : lines) {
            layout.setText(font, line);
            font.draw(batch, line, (WORLD_W - layout.width) / 2, lineY);
            lineY -= 20;
        }

        font.getData().setScale(0.6f);
        String prompt;
        if (rating.equals("FIRED") || state.shiftNumber >= 5) {
            prompt = "Press ENTER to return to menu";
        } else {
            prompt = "Press ENTER for next shift";
        }
        layout.setText(font, prompt);
        font.draw(batch, prompt, (WORLD_W - layout.width) / 2, 80);

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
