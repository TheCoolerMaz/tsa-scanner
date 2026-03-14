package com.tsascanner.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.tsascanner.TsaGame;
import com.tsascanner.screens.transitions.FadeTransition;
import com.tsascanner.ui.KeyboardNav;
import com.tsascanner.util.Prefs;

/**
 * Settings screen with shader and audio options.
 */
public class SettingsScreen extends GameScreen {

    private Stage stage;
    private Prefs prefs;

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        
        prefs = TsaGame.INSTANCE.prefs;
        Skin skin = TsaGame.INSTANCE.assets.getSkin();
        
        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);
        
        // Title
        Label title = new Label("Settings", skin);
        title.setFontScale(1.5f);
        root.add(title).colspan(2).padBottom(40);
        root.row();
        
        // === Shader Effects ===
        root.add(new Label("-- Shaders --", skin)).colspan(2).padBottom(10);
        root.row();
        
        // CRT
        final CheckBox crtBox = new CheckBox(" CRT", skin);
        crtBox.setChecked(prefs.isCrtEnabled());
        crtBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                prefs.setCrtEnabled(crtBox.isChecked());
                TsaGame.INSTANCE.shaders.rebuildFromPrefs(prefs);
            }
        });
        root.add(crtBox).left().colspan(2).padBottom(5);
        root.row();
        
        // Bloom
        final CheckBox bloomBox = new CheckBox(" Bloom", skin);
        bloomBox.setChecked(prefs.isBloomEnabled());
        bloomBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                prefs.setBloomEnabled(bloomBox.isChecked());
                TsaGame.INSTANCE.shaders.rebuildFromPrefs(prefs);
            }
        });
        root.add(bloomBox).left().colspan(2).padBottom(5);
        root.row();
        
        // Vignette
        final CheckBox vignetteBox = new CheckBox(" Vignette", skin);
        vignetteBox.setChecked(prefs.isVignetteEnabled());
        vignetteBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                prefs.setVignetteEnabled(vignetteBox.isChecked());
                TsaGame.INSTANCE.shaders.rebuildFromPrefs(prefs);
            }
        });
        root.add(vignetteBox).left().colspan(2).padBottom(5);
        root.row();
        
        // Dither
        final CheckBox ditherBox = new CheckBox(" Dither", skin);
        ditherBox.setChecked(prefs.isDitherEnabled());
        ditherBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                prefs.setDitherEnabled(ditherBox.isChecked());
                TsaGame.INSTANCE.shaders.rebuildFromPrefs(prefs);
            }
        });
        root.add(ditherBox).left().colspan(2).padBottom(20);
        root.row();
        
        // === Audio ===
        root.add(new Label("-- Audio --", skin)).colspan(2).padBottom(10);
        root.row();
        
        // Master Volume
        root.add(new Label("Master", skin)).left().padRight(20);
        final Slider masterSlider = new Slider(0f, 1f, 0.1f, false, skin);
        masterSlider.setValue(prefs.getMasterVolume());
        masterSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                prefs.setMasterVolume(masterSlider.getValue());
            }
        });
        root.add(masterSlider).width(200);
        root.row().padTop(10);
        
        // Music Volume
        root.add(new Label("Music", skin)).left().padRight(20);
        final Slider musicSlider = new Slider(0f, 1f, 0.1f, false, skin);
        musicSlider.setValue(prefs.getMusicVolume());
        musicSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                prefs.setMusicVolume(musicSlider.getValue());
            }
        });
        root.add(musicSlider).width(200);
        root.row().padTop(10);
        
        // SFX Volume
        root.add(new Label("SFX", skin)).left().padRight(20);
        final Slider sfxSlider = new Slider(0f, 1f, 0.1f, false, skin);
        sfxSlider.setValue(prefs.getSfxVolume());
        sfxSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                prefs.setSfxVolume(sfxSlider.getValue());
            }
        });
        root.add(sfxSlider).width(200);
        root.row().padTop(30);
        
        // Back button
        TextButton backBtn = new TextButton("Back", skin);
        backBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                manager.pop(new FadeTransition(0.3f));
            }
        });
        root.add(backBtn).colspan(2).width(200).height(50);
        
        // Enable keyboard navigation
        KeyboardNav nav = new KeyboardNav(stage);
        nav.add(crtBox, bloomBox, vignetteBox, ditherBox, masterSlider, musicSlider, sfxSlider, backBtn);
        nav.setFocus(0);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.12f, 0.12f, 0.18f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        // ESC to go back
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            manager.pop(new FadeTransition(0.3f));
        }
        
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
