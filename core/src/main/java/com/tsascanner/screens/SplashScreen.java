package com.tsascanner.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.tsascanner.screens.transitions.FadeTransition;

/**
 * Simple splash/loading screen.
 * 
 * Shows while assets load, then transitions to your main menu.
 * Replace this with your actual splash screen.
 */
public class SplashScreen extends GameScreen {

    private SpriteBatch batch;
    private ShapeRenderer shapes;
    private BitmapFont font;
    private float elapsed;

    @Override
    public void show() {
        batch = new SpriteBatch();
        shapes = new ShapeRenderer();
        font = new BitmapFont(); // Default font for now
        elapsed = 0;
    }

    @Override
    public void render(float delta) {
        elapsed += delta;
        
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        float centerX = Gdx.graphics.getWidth() / 2f;
        float centerY = Gdx.graphics.getHeight() / 2f;
        
        // Draw pulsing circle
        float pulse = (float) Math.sin(elapsed * 3) * 0.2f + 0.8f;
        
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.3f * pulse, 0.5f * pulse, 0.8f * pulse, 1f);
        shapes.circle(centerX, centerY, 50 * pulse);
        shapes.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
        
        // Draw text
        batch.begin();
        font.setColor(Color.WHITE);
        String text = "TSA SCANNER";
        float textWidth = font.getRegion().getRegionWidth(); // Rough estimate
        font.draw(batch, text, centerX - 50, centerY - 80);
        font.draw(batch, "Press any key to continue", centerX - 80, centerY - 110);
        batch.end();
        
        // Transition on key press or click (after brief delay)
        if (elapsed > 0.5f && (Gdx.input.justTouched() || Gdx.input.isKeyJustPressed(Input.Keys.ANY_KEY))) {
            manager.swap(new MenuScreen(), new FadeTransition());
        }
    }

    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (shapes != null) shapes.dispose();
        if (font != null) font.dispose();
    }
}
