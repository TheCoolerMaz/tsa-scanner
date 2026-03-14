package com.tsascanner.ui;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;

/**
 * Keyboard navigation helper for Scene2d UI.
 * 
 * Enables Tab/Shift+Tab to cycle focus, Enter/Space to activate.
 * 
 * Usage:
 *   KeyboardNav nav = new KeyboardNav(stage);
 *   nav.add(playBtn, settingsBtn, quitBtn);
 *   nav.setFocus(0); // Focus first button
 */
public class KeyboardNav {
    
    private final Stage stage;
    private final Array<Actor> focusables = new Array<>();
    private int focusIndex = -1;
    
    public KeyboardNav(Stage stage) {
        this.stage = stage;
        
        stage.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                return handleKey(keycode);
            }
        });
    }
    
    /** Add focusable actors in tab order. */
    public KeyboardNav add(Actor... actors) {
        for (Actor actor : actors) {
            focusables.add(actor);
        }
        return this;
    }
    
    /** Set focus to actor at index. */
    public void setFocus(int index) {
        if (focusables.size == 0) return;
        
        // Clamp to valid range
        focusIndex = ((index % focusables.size) + focusables.size) % focusables.size;
        stage.setKeyboardFocus(focusables.get(focusIndex));
    }
    
    /** Move focus forward or backward. */
    public void moveFocus(int delta) {
        setFocus(focusIndex + delta);
    }
    
    /** Get currently focused actor. */
    public Actor getFocused() {
        if (focusIndex >= 0 && focusIndex < focusables.size) {
            return focusables.get(focusIndex);
        }
        return null;
    }
    
    private boolean handleKey(int keycode) {
        switch (keycode) {
            case Input.Keys.TAB:
                if (com.badlogic.gdx.Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) 
                    || com.badlogic.gdx.Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)) {
                    moveFocus(-1);
                } else {
                    moveFocus(1);
                }
                return true;
                
            case Input.Keys.UP:
                moveFocus(-1);
                return true;
                
            case Input.Keys.DOWN:
                moveFocus(1);
                return true;
                
            case Input.Keys.ENTER:
            case Input.Keys.SPACE:
                activateFocused();
                return true;
        }
        return false;
    }
    
    private void activateFocused() {
        Actor focused = getFocused();
        if (focused instanceof Button) {
            Button btn = (Button) focused;
            // Fire a change event as if clicked
            ChangeListener.ChangeEvent event = new ChangeListener.ChangeEvent();
            btn.fire(event);
        }
    }
}
