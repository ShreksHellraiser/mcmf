package com.github.shrekshellraiser.core.uxn;

import java.util.ArrayList;

public class Stack {
    public static final int STACK_SIZE = 256;
    private final byte[] data;
    private int top = STACK_SIZE - 1;
    private String label;

    public Stack(String label) {
        data = new byte[STACK_SIZE];
        this.label = label;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(label);
        sb.append(" ");
        for (int i = wrap(top - 7); i != wrap(top + 1); i = wrap(i + 1)) {
            int x = wrap(i);
            sb.append(String.format("%02x", data[wrap(x)]));
            sb.append(x == 255 ? "|" : " "); // show the bottom of the stack
        }
        sb.append('<');
        return sb.toString();
    }

    public ArrayList<Integer> getTopValues() {
        ArrayList<Integer> topValues = new ArrayList<>();
        for (int i = wrap(top - 7); i != wrap(top + 1); i = wrap(i + 1)) {
            int x = wrap(i);
            topValues.add((int) data[x]);
        }
        return topValues;
    }

    public void pushByte(byte value) {
        data[top] = value;
        top++;
        wrap();
    }
    public void pushShort(short value) {
        pushByte((byte) ((value >>> 8) & 0xFF)); // High byte
        pushByte((byte) (value & 0xFF)); // Low byte
    }

    public byte popByte() {
        top--;
        wrap();
        return data[top];
    }
    public short popShort() {
        // HB LB -
        byte lower = popByte(); // pop LB ( HB LB - HB )
        byte upper = popByte(); // pop HB ( HB - )
        return (short) (upper << 8 | lower);
    }

    public void setPtr(int top) {
        this.top = wrap(top);
    }
    public int getPtr() {
        return top;
    }

    // "Registers"
    // From uxn.c
    // [ Z ][ Y ][ X ][ L ][ N ][ T ] <
    // [ . ][ . ][ . ][   H2   ][ . ] <
    // [   L2   ][   N2   ][   T2   ] <
    private int getShort(int offset) {
        int high = (data[wrap(top - offset - 1)] & 0xFF) << 8;
        int low = data[wrap(top - offset)] & 0xFF;
        return (high | low) & 0xFFFF;
    }
    private void setShort(int offset, short v) {
        int high = (v >>> 8) & 0xFF;
        int low = v & 0xFF;
        data[wrap(top - offset - 1)] = (byte)high;
        data[wrap(top - offset)] = (byte)low;
    }

    public int getT(boolean isShort) {
        if (isShort) {
            return getShort(0);
        }
        return data[top] & 0xFF;
    }
    public void setT(boolean isShort, int t) {
        if (isShort) {
            setShort(0, (short) t);
            return;
        }
        data[top] = (byte) t;
    }

    public int getN(boolean isShort) {
        if (isShort) {
            return getShort(2);
        }
        return data[wrap(top - 1)] & 0xFF;
    }
    public void setN(boolean isShort, int n) {
        if (isShort) {
            setShort(2, (short) n);
            return;
        }
        data[wrap(top - 1)] = (byte) n;
    }

    public int getL(boolean isShort) {
        if (isShort) {
            return getShort(4);
        }
        return data[wrap(top - 2)] & 0xFF;
    }
    public void setL(boolean isShort, int l) {
        if (isShort) {
            setShort(4, (short) l);
            return;
        }
        data[wrap(top - 2)] = (byte) l;
    }

    public byte getX() {
        return data[wrap(top - 3)];
    }
    public void setX(byte l) {
        data[wrap(top - 3)] = l;
    }

    public byte getY() {
        return data[wrap(top - 4)];
    }
    public void setY(byte l) {
        data[wrap(top - 4)] = l;
    }

    public byte getZ() {
        return data[wrap(top - 5)];
    }
    public void setZ(byte l) {
        data[wrap(top - 5)] = l;
    }

    // [ . ][ . ][ . ][   H2   ][ . ] <
    // offset is right aligned
    public int getH2() {
        return getShort(1);
    }
    public void setH2(short h) {
        setShort(1, h);
    }
    // [   L2   ][   N2   ][   T2   ] <

    public void shift(int y) {
        top += y;
        wrap();
    }

    private int wrap(int i) {
        i = i % STACK_SIZE;
        if (i < 0) {
            i += STACK_SIZE;
        }
        return i;
    }
    private void wrap() {
        top = wrap(top);
    }
}
