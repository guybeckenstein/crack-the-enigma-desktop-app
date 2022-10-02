package ui.historyAndStatistics;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

public class MachineCodeData implements Iterable<MachineActivateData>, Serializable {
    private final String machineCode;   // Example: <2(1),3(2)><FD><II>
    private final List<MachineActivateData> machineActivateData;

    public MachineCodeData(String machineCode) {
        this.machineCode = machineCode;
        this.machineActivateData = new ArrayList<>();
    }

    public void addActivateData(String rawData, String processedData, int timeElapsed) {
        machineActivateData.add(new MachineActivateData(rawData, processedData, timeElapsed));
    }

    public List<MachineActivateData> getMachineActivateData() {
        return machineActivateData;
    }

    public String getMachineCode() {
        return machineCode;
    }

    @Override
    public Iterator<MachineActivateData> iterator() {
        return this.machineActivateData.iterator();
    }

    @Override
    public void forEach(Consumer<? super MachineActivateData> action) {
        Iterable.super.forEach(action);
    }
}