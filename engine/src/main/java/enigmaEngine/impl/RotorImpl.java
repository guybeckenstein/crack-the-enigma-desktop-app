package enigmaEngine.impl;

import enigmaEngine.interfaces.Rotatable;
import enigmaEngine.interfaces.Rotor;

import java.io.*;
import java.util.Collections;
import java.util.List;

public class RotorImpl implements Rotor, Rotatable, Serializable {
    private final int id;
    private final int notch;
    private final char notchCharacter;
    private int countRotations;
    private int startIndex;
    private final List<Character> rightSide;
    private final List<Character> leftSide;
    private final int rotorSize;
    private final char firstCharacter;
    private Rotatable rotateNextRotor;

    public RotorImpl(int id, int notch, List<Character> rightSide, List<Character> leftSide) {
        this.id = id;
        this.notch = notch;
        this.notchCharacter = rightSide.get(notch);
        this.rightSide = rightSide;
        this.leftSide = leftSide;
        this.countRotations = 0;
        this.rotorSize = rightSide.size();
        this.rotateNextRotor = null;
        this.startIndex = 0;
        this.firstCharacter = rightSide.get(0);
    }

    @Override
    public int getNumberOfRotations() {
        return this.countRotations;
    }

    @Override
    public Character peekWindow() {
        return this.rightSide.get(0);
    }

    @Override
    public int getNotchIndex() {
        return this.rightSide.indexOf(this.notchCharacter);
    }

    @Override
    public int getOutputIndex(int inputIndex, Direction dir) {
        if (dir == Direction.RIGHT) {
            return rightSide.indexOf(leftSide.get(inputIndex));
        } else {
            return leftSide.indexOf(rightSide.get(inputIndex));
        }
    }

    @Override
    public void setStartIndex(char startCharacter) {
        this.startIndex = this.rightSide.indexOf(startCharacter);

        for (int i = 0; i < this.startIndex; i++) {
            rotate();
        }
    }

    @Override
    public void rotate() {
        Collections.rotate(rightSide, -1);
        Collections.rotate(leftSide, -1);
        this.countRotations = Math.floorMod((countRotations + 1), rotorSize);

        if (isNotchOnTop() && rotateNextRotor != null) { // rotates the next rotor when needed
            rotateNextRotor.rotate();
        }
    }

    private boolean isNotchOnTop() {
        return countRotations == notch;
    }

    @Override
    public void setRotateNextRotor(Rotatable rotateNextRotor) {
        this.rotateNextRotor = rotateNextRotor;
    }

    @Override
    public void resetRotor() {
        rotateNextRotor = null;
        while (rightSide.indexOf(firstCharacter) > 0) {
            rotate();
        }

        startIndex = 0;
        countRotations = 0;
    }
}