package com.github.shrekshellraiser.serial.infuser.puzzles;

public interface InfuserPuzzle {
    String tick(String input);

    boolean isSolved();

    void reset();
}
