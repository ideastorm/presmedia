package com.ideastormsoftware.presmedia.sources;

import com.ideastormsoftware.presmedia.interaction.ActionDescriptor;
import java.util.Set;

/**
 * @author Phillip
 */
public interface InteractiveSource {

    public Set<ActionDescriptor> getSupportedActions();

    public void fireAction(ActionDescriptor action);
}
