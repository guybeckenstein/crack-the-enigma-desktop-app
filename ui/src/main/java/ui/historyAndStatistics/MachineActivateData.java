package ui.historyAndStatistics;

import java.io.Serializable;

public class MachineActivateData implements Serializable {
    private final String rawData;
    private final String processedData;
    private final int timeElapsed;

    public MachineActivateData(String rawData, String processedData, int timeElapsed) {
        this.rawData = rawData;
        this.processedData = processedData;
        this.timeElapsed = timeElapsed;
    }

    public String getRawData() {
        return rawData;
    }

    public String getProcessedData() {
        return processedData;
    }

    public int getTimeElapsed() {
        return timeElapsed;
    }
}