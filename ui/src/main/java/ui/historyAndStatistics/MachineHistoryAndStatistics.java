package ui.historyAndStatistics;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

public class MachineHistoryAndStatistics implements Iterable<MachineCodeData>, Serializable {
    private final List<MachineCodeData> machineCodeData;

    public MachineHistoryAndStatistics() {
        this.machineCodeData = new ArrayList<>();
    }

    public void add(MachineCodeData machineCodeData) {
        this.machineCodeData.add(machineCodeData);
    }

    public void addActivateDataToCurrentMachineCode(String rawData, String processedData, int timeElapsed) {
       machineCodeData.get(machineCodeData.size() - 1).addActivateData(rawData, processedData, timeElapsed);
    }

    public String getCurrentMachineCode() {
        return machineCodeData.get(this.machineCodeData.size() - 1).getMachineCode();
    }

    public boolean isEmpty() {
        return this.machineCodeData.isEmpty();
    }

    @Override
    public Iterator<MachineCodeData> iterator() {
        return this.machineCodeData.iterator();
    }

    @Override
    public void forEach(Consumer<? super MachineCodeData> action) {
        Iterable.super.forEach(action);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (MachineCodeData currCodeData : this.machineCodeData) {
            sb.append(currCodeData.getMachineCode()).append("\n");
            int i = 1;
            for (MachineActivateData machineActivateData : currCodeData.getMachineActivateData()) {
                sb.append("\t").append(i).append(". ").append(machineActivateData.getRawData())
                        .append(" -> ").append(machineActivateData.getProcessedData())
                        .append(" : ").append(machineActivateData.getTimeElapsed()).append(" nano-seconds")
                        .append("\n");
                i++;
            }
        }
        return sb.toString();
    }
}