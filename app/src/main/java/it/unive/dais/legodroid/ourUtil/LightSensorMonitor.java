package it.unive.dais.legodroid.ourUtil;

public class LightSensorMonitor {

    boolean mutex = true;

    public synchronized void give() throws InterruptedException {
        while(mutex == false)
            wait();
        mutex = false;
    }

    public synchronized void release() {
        mutex = true;
        notifyAll();
    }
}
