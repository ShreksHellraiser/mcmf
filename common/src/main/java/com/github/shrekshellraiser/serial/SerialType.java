package com.github.shrekshellraiser.serial;

public enum SerialType {
    NO_QUEUE(0),
    STDIN(1),
    ARGUMENT(2),
    ARGUMENT_SPACER(3),
    ARGUMENT_END(4);

    public final int value;
    SerialType(int value) {
        this.value = value;
    }
}
