package com.tsascanner.screens;

import com.badlogic.gdx.Screen;

/**
 * Base class for all game screens.
 * 
 * Provides lifecycle methods and access to the screen manager.
 * Extend this for each screen in your game (menu, gameplay, etc.)
 */
public abstract class GameScreen implements Screen {

    protected ScreenManager manager;
    
    /** Called when this screen becomes active. */
    @Override
    public void show() {}
    
    /** Called when this screen is no longer active. */
    @Override
    public void hide() {}
    
    /** Called each frame. Delta is time since last frame in seconds. */
    @Override
    public abstract void render(float delta);
    
    /** Called when the window is resized. */
    @Override
    public void resize(int width, int height) {}
    
    /** Called when the application is paused (mobile). */
    @Override
    public void pause() {}
    
    /** Called when the application resumes from pause. */
    @Override
    public void resume() {}
    
    /** Called when this screen is removed from the manager. Clean up resources here. */
    @Override
    public void dispose() {}
    
    /** Set by ScreenManager when this screen is pushed. */
    void setManager(ScreenManager manager) {
        this.manager = manager;
    }
    
    /** Access the screen manager to push/pop screens. */
    protected ScreenManager getManager() {
        return manager;
    }
}
