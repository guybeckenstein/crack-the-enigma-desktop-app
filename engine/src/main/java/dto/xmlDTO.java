package dto;

import java.util.List;
public class xmlDTO {
    private final List<Integer> rotorsFromXML;
    private final List<String> reflectorsFromXML;
    private final List<Character> ABCFromXML;

    private final List<String> dictionaryWordsFromXML;
    private final int totalAgents;

    public xmlDTO(List<Integer> rotorsFromXML, List<String> reflectorsFromXML, List<Character> ABCFromXML,
                  List<String> dictionaryWordsFromXML, int totalAgents) {
        this.rotorsFromXML = rotorsFromXML;
        this.reflectorsFromXML = reflectorsFromXML;
        this.ABCFromXML = ABCFromXML;
        this.dictionaryWordsFromXML = dictionaryWordsFromXML;
        this.totalAgents = totalAgents;
    }

    public List<Integer> getRotorsFromXML() { // All rotors IDs in XML
        return this.rotorsFromXML;
    }
    public List<String> getReflectorsFromXML() { // All reflectors IDs in XML
        return this.reflectorsFromXML;
    }
    public List<Character> getABCFromXML() { // All ABC characters in XML
        return this.ABCFromXML;
    }
    public List<String> getDictionaryWordsFromXML() {
        return this.dictionaryWordsFromXML;
    }
    public int getTotalAgents() {
        return this.totalAgents;
    }
}
