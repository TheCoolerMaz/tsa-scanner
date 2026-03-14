package com.tsascanner.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Disposable;

/**
 * Central asset loader and accessor.
 * 
 * Queue assets with loadAll(), then call finishLoading() before use.
 * Access loaded assets through typed getter methods.
 * 
 * For jam convenience, assets can also be loaded directly if needed.
 */
public class Assets implements Disposable {

    private final AssetManager manager;
    
    // Cached references for fast access
    private Skin skin;
    private BitmapFont font;

    public Assets() {
        manager = new AssetManager();
    }

    /** Queue all game assets for loading. */
    public void loadAll() {
        // UI Skin
        if (Gdx.files.internal("ui/skin.json").exists()) {
            manager.load("ui/skin.json", Skin.class);
        }
        
        // Font (standalone, skin also includes one)
        if (Gdx.files.internal("fonts/pixel.fnt").exists()) {
            manager.load("fonts/pixel.fnt", BitmapFont.class);
        }
        
        // Sprites
        if (Gdx.files.internal("sprites/sprites.atlas").exists()) {
            manager.load("sprites/sprites.atlas", TextureAtlas.class);
        }
        
        // Add more asset loading here as needed
    }

    /** Block until all queued assets are loaded. */
    public void finishLoading() {
        manager.finishLoading();
        cacheReferences();
    }

    /** Update async loading. Returns true when complete. */
    public boolean update() {
        boolean done = manager.update();
        if (done) {
            cacheReferences();
        }
        return done;
    }

    /** Get loading progress (0.0 to 1.0). */
    public float getProgress() {
        return manager.getProgress();
    }

    private void cacheReferences() {
        if (manager.isLoaded("ui/skin.json")) {
            skin = manager.get("ui/skin.json", Skin.class);
        }
        if (manager.isLoaded("fonts/pixel.fnt")) {
            font = manager.get("fonts/pixel.fnt", BitmapFont.class);
        }
    }

    // ===== Typed Accessors =====

    public Skin getSkin() {
        return skin;
    }

    public BitmapFont getFont() {
        return font;
    }

    public TextureAtlas getSprites() {
        return manager.get("sprites/sprites.atlas", TextureAtlas.class);
    }

    // ===== Generic Accessors =====

    public <T> T get(String path, Class<T> type) {
        return manager.get(path, type);
    }

    public <T> void load(String path, Class<T> type) {
        manager.load(path, type);
    }

    public boolean isLoaded(String path) {
        return manager.isLoaded(path);
    }

    // ===== Direct Loading (for jams when you just need it now) =====

    public Texture loadTexture(String path) {
        if (!manager.isLoaded(path)) {
            manager.load(path, Texture.class);
            manager.finishLoadingAsset(path);
        }
        return manager.get(path, Texture.class);
    }

    public Sound loadSound(String path) {
        if (!manager.isLoaded(path)) {
            manager.load(path, Sound.class);
            manager.finishLoadingAsset(path);
        }
        return manager.get(path, Sound.class);
    }

    public Music loadMusic(String path) {
        if (!manager.isLoaded(path)) {
            manager.load(path, Music.class);
            manager.finishLoadingAsset(path);
        }
        return manager.get(path, Music.class);
    }

    @Override
    public void dispose() {
        manager.dispose();
    }
}
