package com.ideastormsoftware.presmedia.interaction;

import java.awt.event.KeyEvent;

/**
 * @author Phillip
 */
public class KeyActionDescriptor implements ActionDescriptor {

    private final Action action;
    private final int keyCode;
    private final int modifiers;

    public enum Action {
        pressed, released
    }

    public KeyActionDescriptor(Action action, int keyCode, int modifiers) {
        this.action = action;
        this.keyCode = keyCode;
        this.modifiers = modifiers;
    }
    
    public boolean matchesEvent (Action action, KeyEvent event) {
        return this.action.equals(action) && this.keyCode == event.getKeyCode() && this.modifiers == event.getModifiers();
    }
}
