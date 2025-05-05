package com.github.shrekshellraiser.gui;

public enum ToggleSwitchType {
    TOGGLE(0,21,0,17, 21),
    ROCKER(17, 21, 0, 17, 21),
    PIANO(0, 46, 68, 21, 22, -1, -2, 90);
    public final int tx;
    public final int h;
    public final int w;
    public final int hh;
    public final int hw;
    public final int offTy;
    public final int onTy;
    public final int padX;
    public final int padY;
    public final int disabledTY;

    ToggleSwitchType(int tx, int switchOffTY, int switchOnTY, int w, int h, int padX, int padY, int switchDisabledTY) {
        this.tx = tx;
        this.h = h;
        this.w = w;
        this.hh = h / 2;
        this.hw = w / 2;
        this.offTy = switchOffTY;
        this.onTy = switchOnTY;
        this.padX = padX;
        this.padY = padY;
        this.disabledTY = switchDisabledTY;
    }
    ToggleSwitchType(int tx, int switchOffTY, int switchOnTY, int w, int h, int padX, int padY) {
        this(tx, switchOffTY, switchOnTY, w, h, padX, padY, switchOffTY);
    }

    ToggleSwitchType(int tx, int switchOffTY, int switchOnTY, int w, int h) {
        this(tx, switchOffTY, switchOnTY, w, h, 0, 0);
    }
}
