package com.github.shrekshellraiser.serial.terminal;

import java.util.ArrayList;
import java.util.Arrays;

public class TerminalBuffer {
    private final char[] data = new char[SerialTerminalContainerData.MAX_SIZE];
    private int cursor = 0;
    private int maxSize;
    private int width;
    private int height;
    private final ArrayList<Integer> arguments = new ArrayList<>();
    private String parsedArgument;
    private boolean isParsingEscapeSequence = false;
    private ParseState parseState = ParseState.ESCAPE;

    private enum ParseState {
        ESCAPE,
        ARGUMENTS
    }

    public void setResolution(int width, int height) {
        this.width = width;
        this.height = height;
        maxSize = width * height;
    }

    public int getY() {
        return cursor / width;
    }
    public int getX() {
        return cursor % width;
    }

    public char getChar(int i) {
        return data[i];
    }
    public char getChar(int x, int y) {
        int i = y * width + x;
        return getChar(i);
    }

    private void clearLine(int y) {
        Arrays.fill(data, y * width, (y + 1) * width, ' ');
    }

    private void nextLine() {
        int y = getY() + 1;
        cursor = y * width;
        if (getY() >= height) {
            System.arraycopy(data, width, data, 0, maxSize - width);
            clearLine(height - 1);
            cursor = maxSize - width;
        }
    }
    private void putChar(char ch) {
        data[cursor] = ch;
        cursor++;
        if (cursor >= maxSize) {
            nextLine();
        }
    }

    private void setCursor(int i) {
        cursor = i;
    }
    private void setCursor(int x, int y) {
        setCursor(y * width + x);
    }

    private void clear() {
        Arrays.fill(data, ' ');
    }

    public void reset() {
        clear();
        cursor = 0;
    }

    private void handleEscapeCode(char command) {
        switch (command) {
            case 'H', 'f' -> {
                if (arguments.size() != 2) {
                    cursor = 0; // move to home position (0,0)
                } else {
                    int x = arguments.get(0);
                    int y = arguments.get(1);
                    setCursor(x, y);
                }
            }
            case 'A' -> {
                if (arguments.size() == 1) {
                    // move up N lines
                    int y = getY();
                    setCursor(getX(), y - arguments.get(0));
                }
            }
            case 'B' -> {
                if (arguments.size() == 1) {
                    // move down N lines
                    int y = getY();
                    setCursor(getX(), y + arguments.get(0));
                }
            }
            case 'J' -> {
                // Erase
                if (arguments.isEmpty()) {
                    // TODO erase after cursor
                    return;
                }
                int arg = arguments.get(0);
                if (arg == 2) {
                    clear();
                }
            }
        }
    }

    private boolean parseCharacter(char ch) {
        switch (parseState) {
            case ESCAPE -> {
                if (ch == '[') {
                    parseState = ParseState.ARGUMENTS;
                    arguments.clear();
                    parsedArgument = "";
                } else {
                    // not an escape code
                    isParsingEscapeSequence = false;
                    return true;
                }
            }
            case ARGUMENTS -> {
                if (Character.isAlphabetic(ch)) {
                    if (!parsedArgument.isEmpty()) {
                        arguments.add(Integer.valueOf(parsedArgument));
                    }
                    handleEscapeCode(ch);
                    isParsingEscapeSequence = false;
                } else if (Character.isDigit(ch)) {
                    parsedArgument += ch;
                } else if (ch == ';') {
                    // separator
                    arguments.add(Integer.valueOf(parsedArgument));
                    parsedArgument = "";
                } else {
                    // ???
                    isParsingEscapeSequence = false;
                    return true;
                }
            }
        }
        return false;
    }

    public void write(char ch) {
        if (ch == '\n') {
            nextLine();
            return;
        } else if (ch == 27) { // ESC
            isParsingEscapeSequence = true;
            parseState = ParseState.ESCAPE;
            return;
        } else if (ch == 127) { // delete
            data[cursor] = ' ';
            return;
        } else if (ch == '\b') { // backspace
            setCursor(cursor - 1);
            return;
        }
        if (isParsingEscapeSequence) {
            if (!parseCharacter(ch)) return;
        }
        putChar(ch);
    }

}
