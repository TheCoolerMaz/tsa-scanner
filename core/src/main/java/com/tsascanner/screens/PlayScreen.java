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
 * All rendering with ShapeRenderer (geometry) and SpriteBatch (text).
 * 480x270 virtual resolution with FitViewport.
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
    private static final Color COL_SCAN_ZONE     = new Color(0.2f, 0.5f, 0.2f, 0.15f);
    private static final Color COL_SCAN_BORDER   = new Color(0.3f, 0.7f, 0.3f, 0.5f);
    private static final Color COL_CORRECT       = new Color(0.2f, 0.9f, 0.3f, 1f);
    private static final Color COL_WRONG         = new Color(0.9f, 0.2f, 0.2f, 1f);
    private static final Color COL_HUD_TEXT      = new Color(0.85f, 0.85f, 0.90f, 1f);
    private static final Color COL_OVERLAY       = new Color(0f, 0f, 0f, 0.75f);

    // ===== Layout =====
    private static final float TOP_BAR_Y = 250;
    private static final float TOP_BAR_H = 20;
    private static final float BOTTOM_BAR_Y = 0;
    private static final float BOTTOM_BAR_H = 55;
    private static final float BELT_Y = 65;
    private static final float BELT_H = 35;
    private static final float SCAN_ZONE_X = 160;
    private static final float SCAN_ZONE_W = 160;

    // ===== Timing =====
    private static final float BAG_INSPECT_TIMEOUT = 8f;

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
    private boolean xrayOn;
    private float inspectTimer;
    private float beltAnimOffset;
    private boolean waitingForNextBag;
    private float nextBagDelay;
    private boolean showResults;

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
        if (assetFont != null) {
            font = assetFont;
        } else {
            font = new BitmapFont();
        }

        // Init game state
        state = new GameState();
        state.reset(1);

        // Spawn first bag
        spawnNextBag();

        xrayOn = false;
        showResults = false;
        flashTimer = 0;
        beltAnimOffset = 0;
        waitingForNextBag = false;
    }

    @Override
    public void render(float delta) {
        // Clamp delta to avoid spiral of death
        delta = Math.min(delta, 0.05f);

        // Handle input
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

        // === Shape rendering ===
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

        // === Text rendering ===
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        drawHUD();
        drawFeedback();
        batch.end();

        // === Results overlay ===
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

        // Move current bag
        if (currentBag != null) {
            switch (currentBag.state) {
                case ON_BELT:
                    currentBag.x += state.getBeltSpeed() * delta;
                    // Check if reached scan zone
                    if (currentBag.x >= SCAN_ZONE_X + SCAN_ZONE_W / 2 - currentBag.width / 2) {
                        currentBag.x = SCAN_ZONE_X + SCAN_ZONE_W / 2 - currentBag.width / 2;
                        currentBag.state = Bag.BagState.INSPECTING;
                        inspectTimer = 0;
                    }
                    break;

                case INSPECTING:
                    inspectTimer += delta;
                    if (inspectTimer >= BAG_INSPECT_TIMEOUT) {
                        // Auto-pass on timeout
                        state.autoPass(currentBag);
                        xrayOn = false;
                        triggerFlash(!currentBag.containsWeapon());
                        startBagExit();
                    }
                    break;

                case PASSED:
                case FLAGGED:
                    // Slide off to the right
                    currentBag.x += state.getBeltSpeed() * 2f * delta;
                    if (currentBag.x > WORLD_W + 20) {
                        currentBag = null;
                        waitingForNextBag = true;
                        nextBagDelay = 0.5f;
                    }
                    break;
            }
        }
    }

    // ==================== INPUT ====================

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            manager.pop(new FadeTransition(0.3f));
            return;
        }

        if (currentBag != null && currentBag.state == Bag.BagState.INSPECTING) {
            // X-ray toggle
            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                xrayOn = !xrayOn;
            }

            // Pass
            if (Gdx.input.isKeyJustPressed(Input.Keys.A)) {
                boolean wasWeapon = currentBag.containsWeapon();
                state.scorePass(currentBag);
                xrayOn = false;
                triggerFlash(!wasWeapon);
                startBagExit();
            }

            // Flag
            if (Gdx.input.isKeyJustPressed(Input.Keys.D)) {
                boolean wasWeapon = currentBag.containsWeapon();
                state.scoreFlag(currentBag);
                xrayOn = false;
                triggerFlash(wasWeapon);
                startBagExit();
            }
        }
    }

    private void handleResultsInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) ||
            Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {

            String rating = state.getRating();
            if (rating.equals("FIRED") || state.shiftNumber >= 5) {
                // Game over — back to menu
                manager.pop(new FadeTransition(0.3f));
            } else {
                // Next shift
                state.nextShift();
                showResults = false;
                xrayOn = false;
                spawnNextBag();
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            manager.pop(new FadeTransition(0.3f));
        }
    }

    // ==================== HELPERS ====================

    private void spawnNextBag() {
        // Ensure roughly 30-40% of bags have weapons for good gameplay
        float weaponChance = 0.35f;
        if (MathUtils.random() < weaponChance) {
            currentBag = BagGenerator.generateWeaponBag(state.shiftNumber);
        } else {
            currentBag = BagGenerator.generate(state.shiftNumber);
            // If it accidentally has a weapon, that's fine
        }
        currentBag.x = -currentBag.width;
        currentBag.y = BELT_Y + BELT_H;
        xrayOn = false;
        inspectTimer = 0;
    }

    private void startBagExit() {
        // Bag state already set by scorePass/scoreFlag
        // It will slide off in update()
    }

    private void triggerFlash(boolean correct) {
        flashTimer = 0.3f;
        flashColor = correct ? COL_CORRECT : COL_WRONG;
    }

    // ==================== DRAWING ====================

    private void drawBackground() {
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        // Top bar background
        shapes.setColor(COL_TOP_BAR);
        shapes.rect(0, TOP_BAR_Y, WORLD_W, TOP_BAR_H);

        // Bottom bar background
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

        // Belt edges
        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(COL_BELT_MARKS);
        shapes.line(0, BELT_Y, WORLD_W, BELT_Y);
        shapes.line(0, BELT_Y + BELT_H, WORLD_W, BELT_Y + BELT_H);
        shapes.end();
    }

    private void drawScanZone() {
        // Semi-transparent scan zone
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(COL_SCAN_ZONE);
        shapes.rect(SCAN_ZONE_X, BELT_Y, SCAN_ZONE_W, 150);
        shapes.end();

        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(COL_SCAN_BORDER);
        shapes.rect(SCAN_ZONE_X, BELT_Y, SCAN_ZONE_W, 150);
        shapes.end();
    }

    private void drawBag() {
        float bx = currentBag.x;
        float by = currentBag.y;
        float bw = currentBag.width;
        float bh = currentBag.height;

        if (xrayOn && currentBag.state == Bag.BagState.INSPECTING) {
            // X-ray view
            // Bag fill — dark translucent blue
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            shapes.setColor(COL_XRAY_BG);
            shapes.rect(bx, by, bw, bh);
            shapes.end();

            // Draw items inside
            drawItems(bx + bw / 2, by + bh / 2);

            // Bag outline
            shapes.begin(ShapeRenderer.ShapeType.Line);
            shapes.setColor(COL_XRAY_OUTLINE);
            shapes.rect(bx, by, bw, bh);
            shapes.end();
        } else {
            // Normal view — solid bag
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

                // Glow effect (slightly larger, translucent)
                drawShapePart(part, px, py, COL_XRAY_GLOW, 2f, true);

                // Solid shape
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
                    // Rotated rectangle: use ShapeRenderer's rect with rotation
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
        String topLeft = "SHIFT " + state.shiftNumber;
        String topScore = "SCORE: " + state.score;
        String topTime = "TIME: " + state.getTimeRemaining();
        String topBags = "BAGS: " + state.bagsProcessed + "/" + state.bagsRequired;

        font.draw(batch, topLeft, 8, TOP_BAR_Y + 15);
        font.draw(batch, topScore, 120, TOP_BAR_Y + 15);
        font.draw(batch, topTime, 260, TOP_BAR_Y + 15);
        font.draw(batch, topBags, 380, TOP_BAR_Y + 15);

        // Strikes display
        if (state.strikes > 0) {
            font.setColor(COL_WRONG);
            String strikesText = "STRIKES: " + state.strikes + "/" + GameState.MAX_STRIKES;
            layout.setText(font, strikesText);
            font.draw(batch, strikesText, WORLD_W - layout.width - 8, TOP_BAR_Y + 15);
            font.setColor(COL_HUD_TEXT);
        }

        // Bottom bar
        font.getData().setScale(0.7f);

        String controls = "[SPACE] X-RAY    [A] PASS    [D] FLAG";
        font.draw(batch, controls, 8, BOTTOM_BAR_H - 8);

        // Streak indicator
        String streakText = "STREAK: x" + (state.streak > 0 ? String.format("%.1f", state.multiplier) : "1");
        layout.setText(font, streakText);
        font.draw(batch, streakText, WORLD_W - layout.width - 8, BOTTOM_BAR_H - 8);

        // X-ray indicator
        if (xrayOn) {
            font.setColor(COL_XRAY_ITEM);
            font.draw(batch, "X-RAY ACTIVE", 8, BOTTOM_BAR_H - 22);
            font.setColor(COL_HUD_TEXT);
        }

        // Inspect timer bar (bottom of scan area)
        if (currentBag != null && currentBag.state == Bag.BagState.INSPECTING) {
            float timerPct = 1f - (inspectTimer / BAG_INSPECT_TIMEOUT);
            // Draw timer bar using shapes — need to break batch
            batch.end();
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            Color timerColor = timerPct > 0.3f ? COL_CORRECT : COL_WRONG;
            shapes.setColor(timerColor.r, timerColor.g, timerColor.b, 0.6f);
            shapes.rect(SCAN_ZONE_X, BELT_Y - 5, SCAN_ZONE_W * timerPct, 4);
            shapes.end();
            batch.begin();
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
            float fx = (WORLD_W - layout.width) / 2;
            float fy = 230;
            font.draw(batch, state.feedbackText, fx, fy);

            font.getData().setScale(0.8f);
            font.setColor(COL_HUD_TEXT);
        }
    }

    private void drawResults() {
        // Dim overlay
        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(COL_OVERLAY);
        shapes.rect(0, 0, WORLD_W, WORLD_H);
        shapes.end();

        // Results text
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
        // Don't dispose the asset font — only dispose if we created our own
        // We can't easily track this, so just null it
    }
}
