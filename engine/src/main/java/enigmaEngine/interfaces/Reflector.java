package enigmaEngine.interfaces;

import java.io.Serializable;

public interface Reflector extends Serializable {

    enum ReflectorID {
        I,
        II,
        III,
        IV,
        V;
    }
    int findPairByIndex(int idx); // returns ReflectorDictionary[index], returns pairIndex
    ReflectorID getReflectorID();
}