package automateDecryption;

import java.util.concurrent.ThreadFactory;

public class AgentFactory implements ThreadFactory {

    private static AgentFactory instance;
    private int agentNumber = 0;

    private AgentFactory() {
    }

    public static synchronized AgentFactory getInstance() {
        if (instance == null) {
            instance = new AgentFactory();
        }

        return instance;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(r, agentNumber + "");
        agentNumber++;
        return t;
    }
}
