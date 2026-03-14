package com.tsascanner;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.tsascanner.assets.Assets;
import com.tsascanner.graphics.ShaderPipeline;
import com.tsascanner.screens.PlayScreen;
import com.tsascanner.screens.ScreenManager;
import com.tsascanner.util.Prefs;

/**
 * Main game entry point.
 * 
 * Initializes core systems and manages the game lifecycle.
 */
public class TsaGame extends Game {

    public static TsaGame INSTANCE;
    
    public Assets assets;
    public ScreenManager screens;
    public ShaderPipeline shaders;
    public Prefs prefs;

    @Override
    public void create() {
        INSTANCE = this;
        
        Gdx.app.setLogLevel(Application.LOG_DEBUG);
        
        // Initialize core systems
        prefs = new Prefs();
        assets = new Assets();
        assets.loadAll();
        assets.finishLoading();
        
        shaders = new ShaderPipeline();
        shaders.rebuildFromPrefs(prefs);
        
        screens = new ScreenManager(this);
        screens.push(new PlayScreen());
    }

    @Override
    public void render() {
        // Guard against early render calls
        if (screens == null || shaders == null) return;
        
        // Clear screen
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        // Render through shader pipeline
        if (Gdx.app.getType() != Application.ApplicationType.HeadlessDesktop) {
            shaders.begin();
        }
        
        // Update and render screens
        screens.update(Gdx.graphics.getDeltaTime());
        super.render();
        
        if (Gdx.app.getType() != Application.ApplicationType.HeadlessDesktop) {
            shaders.end();
            shaders.render();
        }
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        if (shaders != null) shaders.resize(width, height);
        if (screens != null) screens.resize(width, height);
    }

    @Override
    public void dispose() {
        if (screens != null) screens.dispose();
        if (shaders != null) shaders.dispose();
        if (assets != null) assets.dispose();
    }
}
