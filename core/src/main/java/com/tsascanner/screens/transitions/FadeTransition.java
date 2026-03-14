package com.tsascanner.screens.transitions;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.tsascanner.screens.GameScreen;

/**
 * Fade to black (or custom color) transition.
 * 
 * First half: fade out old screen to color.
 * Second half: fade in new screen from color.
 */
public class FadeTransition implements Transition {

    private final float duration;
    private final Color fadeColor;
    private float elapsed;
    private ShapeRenderer shapeRenderer;

    /** Create fade with default duration (0.5s) and black. */
    public FadeTransition() {
        this(0.5f, Color.BLACK);
    }

    /** Create fade with custom duration and black. */
    public FadeTransition(float duration) {
        this(duration, Color.BLACK);
    }

    /** Create fade with custom duration and color. */
    public FadeTransition(float duration, Color fadeColor) {
        this.duration = duration;
        this.fadeColor = new Color(fadeColor);
    }

    @Override
    public void start() {
        elapsed = 0;
        shapeRenderer = new ShapeRenderer();
    }

    @Override
    public void update(float delta) {
        elapsed += delta;
    }

    @Override
    public void render(GameScreen from, GameScreen to) {
        float progress = Math.min(elapsed / duration, 1f);
        
        // Render the appropriate screen
        if (progress < 0.5f) {
            from.render(Gdx.graphics.getDeltaTime());
        } else {
            to.render(Gdx.graphics.getDeltaTime());
        }
        
        // Calculate alpha: 0 -> 1 -> 0
        float alpha;
        if (progress < 0.5f) {
            alpha = progress * 2f; // Fade out: 0 to 1
        } else {
            alpha = (1f - progress) * 2f; // Fade in: 1 to 0
        }
        
        // Draw fade overlay
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(fadeColor.r, fadeColor.g, fadeColor.b, alpha);
        shapeRenderer.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        shapeRenderer.end();
        
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    @Override
    public boolean isComplete() {
        if (elapsed >= duration) {
            if (shapeRenderer != null) {
                shapeRenderer.dispose();
                shapeRenderer = null;
            }
            return true;
        }
        return false;
    }
}
