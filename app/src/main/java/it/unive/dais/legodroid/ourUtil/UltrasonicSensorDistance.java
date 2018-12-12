package it.unive.dais.legodroid.ourUtil;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.plugs.UltrasonicSensor;

class UltrasonicSensorDistance extends UltrasonicSensor implements Runnable{

    public UltrasonicSensorDistance(EV3.Api api){
        super(api, EV3.InputPort._4);
    }

    @Override
    public void run() {
    }

    public boolean isDetected(float distance) throws IOException, ExecutionException, InterruptedException {
        Float obstacle = super.getDistance().get();
        if(obstacle <= distance)
            return true;
        else
            return false;
    }


}
