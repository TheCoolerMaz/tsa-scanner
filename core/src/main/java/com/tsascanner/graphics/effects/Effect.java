package com.tsascanner.graphics.effects;

import com.badlogic.gdx.utils.Disposable;
import com.crashinvaders.vfx.effects.ChainVfxEffect;

/**
 * Base interface for custom shader effects.
 * 
 * Effects are post-processing shaders applied to the final render.
 * Implement ChainVfxEffect to integrate with the VFX pipeline.
 */
public interface Effect extends ChainVfxEffect, Disposable {
    
    /** Get the display name for UI. */
    String getName();
    
    /** Set effect intensity (0.0 to 1.0+). */
    void setIntensity(float intensity);
    
    /** Get current intensity. */
    float getIntensity();
}
