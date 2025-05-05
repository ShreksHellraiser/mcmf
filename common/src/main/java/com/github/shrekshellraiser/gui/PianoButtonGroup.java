package com.github.shrekshellraiser.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;

import java.util.*;

import static com.github.shrekshellraiser.gui.ToggleSwitchType.PIANO;

public class PianoButtonGroup<T> implements Button.OnPress {
    private final int x;
    private final int y;
    private final int k;
    private final int l;
    private int selected = 0;
    private PianoButtonCallback<T> callback;
    protected ArrayList<T> values;
    protected ArrayList<String> labels;
    protected ArrayList<Integer> disabledButtons;
    private int bWide = 4;
    private int bTall = 4;
    private int bWidth = PIANO.w + PIANO.padX;
    private int bHeight = PIANO.h + PIANO.padY;
    private boolean built = false;
    protected int selectionWidth = 1;
    private int maxSelection = 0;

    protected final ArrayList<PianoButtonEntry<T>> buttons = new ArrayList<>();
    protected final Map<Button,PianoButtonEntry<T>> buttonMap = new HashMap<>();

    public PianoButtonGroup(int x, int y, int k, int l) {
        this.x = x;
        this.y = y;
        this.k = k;
        this.l = l;
    }

    private void checkBuilt() {
        if (built) {
            throw new RuntimeException("Attempt to modify finalized PianoButtonGroup!");
        }
    }

    public PianoButtonGroup<T> setValues(ArrayList<T> values) {
        checkBuilt();
        this.values = values;
        return this;
    }

    public PianoButtonGroup<T> setLabels(ArrayList<String> labels) {
        checkBuilt();
        this.labels = labels;
        return this;
    }

    public PianoButtonGroup<T> setSize(int buttonWidth, int buttonHeight) {
        checkBuilt();
        this.bWidth = buttonWidth;
        this.bHeight = buttonHeight;
        return this;
    }

    public PianoButtonGroup<T> setSelectionWidth(int width) {
        this.selectionWidth = width;
        maxSelection = bWide * bTall - (selectionWidth + 1);
        return this;
    }

    public PianoButtonGroup<T> setDims(int buttonsWide, int buttonsTall) {
        checkBuilt();
        this.bWide = buttonsWide;
        this.bTall = buttonsTall;
        maxSelection = bWide * bTall - (selectionWidth + 1);
        return this;
    }

    public PianoButtonGroup<T> setDisabled(ArrayList<Integer> disabled) {
        checkBuilt();
        disabledButtons = disabled;
        return this;
    }

    public PianoButtonGroup<T> setCallback(PianoButtonCallback<T> callback) {
        this.callback = callback;
        return this;
    }

    public PianoButtonGroup<T> setSelected(int i) {
        selected = i;
        return this;
    }

    private void generateLabels() {
        checkBuilt();
        if (this.labels == null) {
            labels = new ArrayList<>();
            for (int i = 0; i < bWide * bTall; i++) {
                labels.add(String.format("%s", values.get(i)));
            }
        }
    }

    public PianoButtonGroup<T> build(ModifiableScreen s, Minecraft minecraft) {
        generateLabels();
        built = true;
        for (int i = 0; i < bWide * bTall; i++ ) {
            int ix = i % bWide;
            int iy = i / bWide;
            int ax = k + x + bWidth * ix;
            int ay = l + y + bHeight * iy;
            ToggleSwitchButton b = new ToggleSwitchButton(ax, ay, labels.get(i), this,
                    minecraft.font, PIANO, ToggleSwitchButton.LabelPosition.ON);
            if (disabledButtons.contains(i)) {
                b.setEnabled(false);
            }
            PianoButtonEntry<T> entry = new PianoButtonEntry<T>(b, values.get(i));
            buttons.add(entry);
            buttonMap.put(b, entry);
            s.addWidget(b);
        }
        return this;
    }

    @Override
    public void onPress(Button button) {
        if (callback != null) {
            PianoButtonEntry<T> b = buttonMap.get(button);
            callback.callback(b.value);
        }
    }

    public void renderBg(PoseStack poseStack, int i, int j) {
        for (int d = 0; d < bWide * bTall; d++) {
            boolean active = d >= selected && d < selected + selectionWidth;
            buttons.get(d).button.renderBg(poseStack, i, j, active);
        }
    }

    public void render(PoseStack poseStack, int i, int j, float f) {
        for (int d = 0; d < bWide * bTall; d++) {
            boolean active = d >= selected && d < selected + selectionWidth;
            buttons.get(d).button.render(poseStack, i, j, f, active);
        }
    }

    protected static class PianoButtonEntry<T> {
        ToggleSwitchButton button;
        T value;
        public PianoButtonEntry(ToggleSwitchButton button, T v) {
            this.value = v;
            this.button = button;
        }
    }

    public interface PianoButtonCallback<T> {
        void callback(T value);
    }

    public interface ModifiableScreen {
        <T extends GuiEventListener & NarratableEntry> T addWidget(T guiEventListener);
    }

}
