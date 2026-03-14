package com.tsascanner.screens.transitions;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Interpolation;
import com.tsascanner.screens.GameScreen;

/**
 * Slide transition between screens.
 * 
 * Old screen slides out while new screen slides in from the specified direction.
 */
public class SlideTransition implements Transition {

    public enum Direction { LEFT, RIGHT, UP, DOWN }

    private final float duration;
    private final Direction direction;
    private final Interpolation interpolation;
    
    private float elapsed;
    private SpriteBatch batch;
    private FrameBuffer fromBuffer;
    private FrameBuffer toBuffer;
    private boolean buffersCreated;

    /** Create slide with default duration (0.4s) and direction. */
    public SlideTransition(Direction direction) {
        this(direction, 0.4f);
    }

    /** Create slide with custom duration. */
    public SlideTransition(Direction direction, float duration) {
        this(direction, duration, Interpolation.smoother);
    }

    /** Create slide with custom interpolation. */
    public SlideTransition(Direction direction, float duration, Interpolation interpolation) {
        this.direction = direction;
        this.duration = duration;
        this.interpolation = interpolation;
    }

    @Override
    public void start() {
        elapsed = 0;
        batch = new SpriteBatch();
        buffersCreated = false;
    }

    private void createBuffers() {
        int w = Gdx.graphics.getWidth();
        int h = Gdx.graphics.getHeight();
        fromBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, w, h, false);
        toBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, w, h, false);
        buffersCreated = true;
    }

    @Override
    public void update(float delta) {
        elapsed += delta;
    }

    @Override
    public void render(GameScreen from, GameScreen to) {
        if (!buffersCreated) {
            createBuffers();
        }
        
        float progress = interpolation.apply(Math.min(elapsed / duration, 1f));
        int w = Gdx.graphics.getWidth();
        int h = Gdx.graphics.getHeight();
        
        // Render "from" screen to buffer
        fromBuffer.begin();
        from.render(Gdx.graphics.getDeltaTime());
        fromBuffer.end();
        
        // Render "to" screen to buffer
        toBuffer.begin();
        to.render(Gdx.graphics.getDeltaTime());
        toBuffer.end();
        
        // Calculate positions based on direction
        float fromX = 0, fromY = 0, toX = 0, toY = 0;
        
        switch (direction) {
            case LEFT:
                fromX = -w * progress;
                toX = w * (1 - progress);
                break;
            case RIGHT:
                fromX = w * progress;
                toX = -w * (1 - progress);
                break;
            case UP:
                fromY = h * progress;
                toY = -h * (1 - progress);
                break;
            case DOWN:
                fromY = -h * progress;
                toY = h * (1 - progress);
                break;
        }
        
        // Draw both screens at their positions
        Texture fromTex = fromBuffer.getColorBufferTexture();
        Texture toTex = toBuffer.getColorBufferTexture();
        
        batch.begin();
        // Note: FBO textures are upside-down, so we flip Y
        batch.draw(fromTex, fromX, fromY + h, w, -h);
        batch.draw(toTex, toX, toY + h, w, -h);
        batch.end();
    }

    @Override
    public boolean isComplete() {
        if (elapsed >= duration) {
            dispose();
            return true;
        }
        return false;
    }
    
    private void dispose() {
        if (fromBuffer != null) {
            fromBuffer.dispose();
            fromBuffer = null;
        }
        if (toBuffer != null) {
            toBuffer.dispose();
            toBuffer = null;
        }
        if (batch != null) {
            batch.dispose();
            batch = null;
        }
    }
}
