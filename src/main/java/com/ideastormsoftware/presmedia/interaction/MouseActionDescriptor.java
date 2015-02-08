package com.ideastormsoftware.presmedia.interaction;

import java.awt.event.MouseEvent;

/**
 * @author Phillip
 */
public class MouseActionDescriptor implements ActionDescriptor{

    private final Action action;
    private final int buttonMask;
    private final int modifiers;

    public enum Action {

        enter, exit, move, drag, click
    }

    public MouseActionDescriptor(Action action, int buttonMask, int modifiers) {
        this.action = action;
        this.buttonMask = buttonMask;
        this.modifiers = modifiers;
    }

    public boolean matchesEvent(Action action, MouseEvent event) {
        return (this.action.equals(action) && 
                (this.buttonMask | this.modifiers) == event.getModifiersEx());
    }
}
