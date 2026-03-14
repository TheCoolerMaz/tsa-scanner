package com.tsascanner.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.tsascanner.TsaGame;
import com.tsascanner.screens.transitions.FadeTransition;
import com.tsascanner.ui.KeyboardNav;

/**
 * Main menu screen.
 */
public class MenuScreen extends GameScreen {

    private Stage stage;

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        
        Skin skin = TsaGame.INSTANCE.assets.getSkin();
        
        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);
        
        // Title
        Label title = new Label("TSA SCANNER", skin);
        title.setFontScale(2f);
        root.add(title).padBottom(60);
        root.row();
        
        // Play button
        TextButton playBtn = new TextButton("Play", skin);
        playBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                manager.push(new PlayScreen(), new FadeTransition(0.3f));
            }
        });
        root.add(playBtn).width(200).height(50).padBottom(15);
        root.row();
        
        // Settings button
        TextButton settingsBtn = new TextButton("Settings", skin);
        settingsBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                manager.push(new SettingsScreen(), new FadeTransition(0.3f));
            }
        });
        root.add(settingsBtn).width(200).height(50).padBottom(15);
        root.row();
        
        // Quit button
        TextButton quitBtn = new TextButton("Quit", skin);
        quitBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.exit();
            }
        });
        root.add(quitBtn).width(200).height(50);
        
        // Enable keyboard navigation
        KeyboardNav nav = new KeyboardNav(stage);
        nav.add(playBtn, settingsBtn, quitBtn);
        nav.setFocus(0);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.12f, 0.12f, 0.18f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        if (stage != null) stage.dispose();
    }
}
