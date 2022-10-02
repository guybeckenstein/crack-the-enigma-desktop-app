package enigmaEngine.interfaces;

import java.io.Serializable;

public interface Rotor extends Rotatable, Serializable {

    Character peekWindow();

    enum Direction {
        LEFT, RIGHT
    }

    int getNotchIndex();

    int getOutputIndex(int inputIndex, Direction dir);

    void setStartIndex(char startCharacter);

    void setRotateNextRotor(Rotatable rotateNextRotor);

    void resetRotor();
}