package com.tsascanner.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

/**
 * Game preferences wrapper.
 * 
 * Handles saving/loading user settings.
 * All shader toggles and intensities are stored here.
 */
public class Prefs {

    private static final String PREFS_NAME = "tsa-scanner-prefs";

    // Shader toggles
    private static final String KEY_BLOOM_ENABLED = "bloom.enabled";
    private static final String KEY_VIGNETTE_ENABLED = "vignette.enabled";
    private static final String KEY_CRT_ENABLED = "crt.enabled";
    private static final String KEY_DITHER_ENABLED = "dither.enabled";

    // Shader intensities
    private static final String KEY_BLOOM_INTENSITY = "bloom.intensity";
    private static final String KEY_VIGNETTE_INTENSITY = "vignette.intensity";
    private static final String KEY_CRT_INTENSITY = "crt.intensity";
    private static final String KEY_DITHER_STRENGTH = "dither.strength";

    // Audio
    private static final String KEY_MASTER_VOLUME = "audio.master";
    private static final String KEY_MUSIC_VOLUME = "audio.music";
    private static final String KEY_SFX_VOLUME = "audio.sfx";
    private static final String KEY_MUTED = "audio.muted";

    private Preferences prefs;

    public Prefs() {
        prefs = Gdx.app.getPreferences(PREFS_NAME);
    }

    private void flush() {
        prefs.flush();
    }

    // ===== Shader Toggles =====

    public boolean isBloomEnabled() {
        return prefs.getBoolean(KEY_BLOOM_ENABLED, false);
    }

    public void setBloomEnabled(boolean enabled) {
        prefs.putBoolean(KEY_BLOOM_ENABLED, enabled);
        flush();
    }

    public boolean isVignetteEnabled() {
        return prefs.getBoolean(KEY_VIGNETTE_ENABLED, false);
    }

    public void setVignetteEnabled(boolean enabled) {
        prefs.putBoolean(KEY_VIGNETTE_ENABLED, enabled);
        flush();
    }

    public boolean isCrtEnabled() {
        return prefs.getBoolean(KEY_CRT_ENABLED, false);
    }

    public void setCrtEnabled(boolean enabled) {
        prefs.putBoolean(KEY_CRT_ENABLED, enabled);
        flush();
    }

    public boolean isDitherEnabled() {
        return prefs.getBoolean(KEY_DITHER_ENABLED, false);
    }

    public void setDitherEnabled(boolean enabled) {
        prefs.putBoolean(KEY_DITHER_ENABLED, enabled);
        flush();
    }

    // ===== Shader Intensities =====

    public float getBloomIntensity() {
        return prefs.getFloat(KEY_BLOOM_INTENSITY, 0.5f);
    }

    public void setBloomIntensity(float intensity) {
        prefs.putFloat(KEY_BLOOM_INTENSITY, intensity);
        flush();
    }

    public float getVignetteIntensity() {
        return prefs.getFloat(KEY_VIGNETTE_INTENSITY, 0.3f);
    }

    public void setVignetteIntensity(float intensity) {
        prefs.putFloat(KEY_VIGNETTE_INTENSITY, intensity);
        flush();
    }

    public float getCrtIntensity() {
        return prefs.getFloat(KEY_CRT_INTENSITY, 1.0f);
    }

    public void setCrtIntensity(float intensity) {
        prefs.putFloat(KEY_CRT_INTENSITY, intensity);
        flush();
    }

    public float getDitherStrength() {
        return prefs.getFloat(KEY_DITHER_STRENGTH, 0.1f);
    }

    public void setDitherStrength(float strength) {
        prefs.putFloat(KEY_DITHER_STRENGTH, strength);
        flush();
    }

    // ===== Audio =====

    public float getMasterVolume() {
        return prefs.getFloat(KEY_MASTER_VOLUME, 1.0f);
    }

    public void setMasterVolume(float volume) {
        prefs.putFloat(KEY_MASTER_VOLUME, volume);
        flush();
    }

    public float getMusicVolume() {
        return prefs.getFloat(KEY_MUSIC_VOLUME, 0.7f);
    }

    public void setMusicVolume(float volume) {
        prefs.putFloat(KEY_MUSIC_VOLUME, volume);
        flush();
    }

    public float getSfxVolume() {
        return prefs.getFloat(KEY_SFX_VOLUME, 1.0f);
    }

    public void setSfxVolume(float volume) {
        prefs.putFloat(KEY_SFX_VOLUME, volume);
        flush();
    }

    public boolean isMuted() {
        return prefs.getBoolean(KEY_MUTED, false);
    }

    public void setMuted(boolean muted) {
        prefs.putBoolean(KEY_MUTED, muted);
        flush();
    }
}
