package com.github.shrekshellraiser.core.uxn;

public interface UXNEvent {
    /**
     * A UXN event. when this function is called implementations should set the PC of the uxn and relevant device bytes
     */
    void handle(UXNBus bus);

    /**
     * A callback function to be called after the vector finishes execution.
     */
    void post(UXNBus bus);
}
