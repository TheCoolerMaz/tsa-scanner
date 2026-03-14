package com.tsascanner.screens;

import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.tsascanner.TsaGame;
import com.tsascanner.ecs.systems.MovementSystem;
import com.tsascanner.screens.transitions.FadeTransition;

/**
 * Main gameplay screen — TSA checkpoint scanner.
 *
 * TODO: Implement conveyor belt, x-ray view, item inspection, and scoring.
 */
public class PlayScreen extends GameScreen {

    private Engine engine;
    private SpriteBatch batch;
    private ShapeRenderer shapes;

    @Override
    public void show() {
        batch = new SpriteBatch();
        shapes = new ShapeRenderer();

        // Set up ECS
        engine = new Engine();
        engine.addSystem(new MovementSystem());
    }

    @Override
    public void render(float delta) {
        // Handle input
        handleInput();

        // Update ECS
        engine.update(delta);

        // Clear screen
        Gdx.gl.glClearColor(0.05f, 0.05f, 0.1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Placeholder — draw scanner outline
        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(Color.GREEN);
        float cx = Gdx.graphics.getWidth() / 2f;
        float cy = Gdx.graphics.getHeight() / 2f;
        shapes.rect(cx - 200, cy - 120, 400, 240);
        shapes.end();

        // HUD text
        batch.begin();
        if (TsaGame.INSTANCE.assets.getFont() != null) {
            TsaGame.INSTANCE.assets.getFont().draw(batch,
                "SCANNER READY — ESC to menu", 10, 30);
        }
        batch.end();
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            manager.pop(new FadeTransition(0.3f));
        }
    }

    @Override
    public void resize(int width, int height) {
        // Update camera/viewport here
    }

    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (shapes != null) shapes.dispose();
    }
}
