package com.tsascanner.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.utils.Array;
import com.tsascanner.screens.transitions.Transition;

/**
 * Stack-based screen manager with transition support.
 * 
 * Manages a stack of screens and handles transitions between them.
 * 
 * Usage:
 *   screens.push(new MenuScreen());                    // Push without transition
 *   screens.push(new GameScreen(), new FadeTransition());  // Push with fade
 *   screens.pop();                                     // Return to previous
 *   screens.swap(new GameOverScreen());                // Replace current
 */
public class ScreenManager {

    private final Game game;
    private final Array<GameScreen> stack = new Array<>();
    
    private Transition activeTransition;
    private GameScreen transitionFrom;
    private GameScreen transitionTo;
    private boolean isPopping;

    public ScreenManager(Game game) {
        this.game = game;
    }

    /** Push a new screen onto the stack (no transition). */
    public void push(GameScreen screen) {
        push(screen, null);
    }

    /** Push a new screen with a transition effect. */
    public void push(GameScreen screen, Transition transition) {
        screen.setManager(this);
        
        if (transition != null && stack.size > 0) {
            startTransition(stack.peek(), screen, transition, false);
        } else {
            if (stack.size > 0) {
                stack.peek().hide();
            }
            stack.add(screen);
            game.setScreen(screen);
            screen.show();
        }
    }

    /** Pop the current screen and return to the previous one. */
    public void pop() {
        pop(null);
    }

    /** Pop with a transition effect. */
    public void pop(Transition transition) {
        if (stack.size <= 1) return;
        
        GameScreen current = stack.peek();
        GameScreen previous = stack.get(stack.size - 2);
        
        if (transition != null) {
            startTransition(current, previous, transition, true);
        } else {
            current.hide();
            current.dispose();
            stack.pop();
            game.setScreen(previous);
            previous.show();
        }
    }

    /** Replace current screen (no stack change). */
    public void swap(GameScreen screen) {
        swap(screen, null);
    }

    /** Replace current screen with transition. */
    public void swap(GameScreen screen, Transition transition) {
        if (stack.size == 0) {
            push(screen, transition);
            return;
        }
        
        screen.setManager(this);
        GameScreen current = stack.pop();
        
        if (transition != null) {
            stack.add(screen);
            startTransition(current, screen, transition, false);
        } else {
            current.hide();
            current.dispose();
            stack.add(screen);
            game.setScreen(screen);
            screen.show();
        }
    }

    /** Clear all screens and push a new one. */
    public void reset(GameScreen screen) {
        while (stack.size > 0) {
            GameScreen s = stack.pop();
            s.hide();
            s.dispose();
        }
        push(screen);
    }

    private void startTransition(GameScreen from, GameScreen to, Transition transition, boolean popping) {
        this.transitionFrom = from;
        this.transitionTo = to;
        this.activeTransition = transition;
        this.isPopping = popping;
        transition.start();
        
        if (!popping) {
            stack.add(to);
        }
    }

    /** Called each frame by TsaGame. */
    public void update(float delta) {
        if (activeTransition != null) {
            activeTransition.update(delta);
            
            if (activeTransition.isComplete()) {
                completeTransition();
            }
        }
    }

    private void completeTransition() {
        transitionFrom.hide();
        if (isPopping) {
            transitionFrom.dispose();
            stack.pop();
        }
        
        game.setScreen(transitionTo);
        transitionTo.show();
        
        activeTransition = null;
        transitionFrom = null;
        transitionTo = null;
    }

    /** Render transition overlay (called during render). */
    public void renderTransition() {
        if (activeTransition != null) {
            activeTransition.render(transitionFrom, transitionTo);
        }
    }

    /** Returns true if a transition is in progress. */
    public boolean isTransitioning() {
        return activeTransition != null;
    }

    public void resize(int width, int height) {
        for (GameScreen screen : stack) {
            screen.resize(width, height);
        }
    }

    public void dispose() {
        for (GameScreen screen : stack) {
            screen.dispose();
        }
        stack.clear();
    }
    
    /** Get the current screen, or null if stack is empty. */
    public GameScreen current() {
        return stack.size > 0 ? stack.peek() : null;
    }
}
