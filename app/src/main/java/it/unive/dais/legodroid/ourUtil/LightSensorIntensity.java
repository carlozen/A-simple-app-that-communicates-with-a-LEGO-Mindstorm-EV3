package it.unive.dais.legodroid.ourUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.plugs.LightSensor;

class LightSensorIntensity extends LightSensor implements Runnable{

    LightSensorMonitor lightSensorMonitor;

    public LightSensorIntensity(EV3.Api api, EV3.InputPort inputPort, LightSensorMonitor lightSensorMonitor) {
        super(api, inputPort);
        this.lightSensorMonitor = lightSensorMonitor;
    }

    @Override
    public void run() {

    }

    public Short getReflectedNow() throws IOException, ExecutionException, InterruptedException {

        lightSensorMonitor.give();
        Short res = getReflected().get();
        lightSensorMonitor.release();

        return res;
    }
}
