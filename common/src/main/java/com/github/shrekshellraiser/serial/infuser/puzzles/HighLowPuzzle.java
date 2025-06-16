package com.github.shrekshellraiser.serial.infuser.puzzles;

public class HighLowPuzzle implements InfuserPuzzle {
    private static final String ABOUT = "Guess the number between 0 and 100.\n?";
    private int chosenNumber;
    private boolean solved = false;
    private void chooseNumber() {
        chosenNumber = (int) (Math.random() * 100);
    }
    public HighLowPuzzle() {
        chooseNumber();
    }
    @Override
    public String tick(String input) {
        if (input.isEmpty()) {
            return ABOUT;
        }
        try {
            int guess = Integer.parseInt(input);
            if (guess == chosenNumber) {
                solved = true;
                return "CORRECT!\n";
            } else if (guess < chosenNumber) {
                return "LOW!\n?";
            }
            return "HIGH!\n?";
        } catch (NumberFormatException e) {
            return ABOUT;
        }
    }
    @Override
    public boolean isSolved() {
        return solved;
    }
    @Override
    public void reset() {
        chooseNumber();
        solved = false;
    }
}
