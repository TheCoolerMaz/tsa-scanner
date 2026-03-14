package com.tsascanner.screens.transitions;

import com.tsascanner.screens.GameScreen;

/**
 * Interface for screen transition effects.
 * 
 * Transitions handle the visual effect when switching between screens.
 * The ScreenManager calls these methods during a transition.
 */
public interface Transition {

    /** Called when the transition begins. */
    void start();
    
    /** Called each frame during the transition. */
    void update(float delta);
    
    /** Render the transition effect. May render from/to screens as needed. */
    void render(GameScreen from, GameScreen to);
    
    /** Returns true when the transition is complete. */
    boolean isComplete();
}
