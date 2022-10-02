package enigmaEngine.impl;

import enigmaEngine.interfaces.Reflector;

import java.io.*;
import java.util.HashMap;
import java.util.List;

public class ReflectorImpl implements Reflector, Serializable {
    private final HashMap<Integer, Integer> indexPairs;
    private final ReflectorID id;

    public ReflectorImpl(List<Integer> input, List<Integer> output, ReflectorID id) {
        this.id = id;
        this.indexPairs = new HashMap<>();

        for (int i = 0; i < input.size(); i++) {
            indexPairs.put(input.get(i), output.get(i));
            indexPairs.put(output.get(i), input.get(i));
        }
    }

    @Override
    public int findPairByIndex(int idx) { // returns ReflectorDictionary[index]
        return this.getIndexPairs().get(idx);
    }

    @Override
    public ReflectorID getReflectorID() {
        return this.id;
    }

    private HashMap<Integer, Integer> getIndexPairs() {
        return indexPairs;
    }

    @Override
    public Reflector clone() {
        try {
            super.clone();
            Reflector clone;
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(this);
            ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
            clone = (Reflector) objectInputStream.readObject();
            return clone;
        } catch (CloneNotSupportedException | IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}