package it.unive.dais.legodroid.ourUtil;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.plugs.GyroSensor;

class GyroSensorAngle extends GyroSensor implements Runnable{

    public GyroSensorAngle(EV3.Api api, EV3.InputPort inputPort) {
        super(api, inputPort);
    }

    @Override
    public void run() {

    }

    public Float getAngleNow() throws IOException, ExecutionException, InterruptedException {
        return getAngle().get();
    }


}
