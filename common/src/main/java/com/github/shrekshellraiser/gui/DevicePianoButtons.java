package com.github.shrekshellraiser.gui;

import net.minecraft.client.gui.components.Button;

import java.util.ArrayList;

public class DevicePianoButtons extends PianoButtonGroup<Integer> {
    public DevicePianoButtons(int x, int y, int k, int l, int slots) {
        super(x,y,k,l);
        ArrayList<Integer> values = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        for (int i = 0; i < 16; i++) {
            values.add(i);
            if (i == 0) {
                labels.add(""); // don't label device 00
                continue;
            }
            labels.add(String.format("%X0", i));
        }
        setValues(values);
        setLabels(labels);
        setSelectionWidth(slots);
        ArrayList<Integer> disabled = new ArrayList<>();
        disabled.add(0);
        setDisabled(disabled);
    }

    @Override
    public void onPress(Button button) {
        PianoButtonEntry<Integer> entry = buttonMap.get(button);
        int i = buttons.indexOf(entry);
        i = Math.max(Math.min(i, 16 - selectionWidth), 1);
        Button realButton = buttons.get(i).button;
        super.onPress(realButton);
    }
}
