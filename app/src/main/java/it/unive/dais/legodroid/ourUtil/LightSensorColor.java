package it.unive.dais.legodroid.ourUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.plugs.LightSensor;

class LightSensorColor extends LightSensor implements Runnable{

    private ArrayList<Color> colorsList;
    private LightSensorMonitor lightSensorMonitor;
    private boolean hasExceptionOccurred = false;

    public LightSensorColor(EV3.Api api, EV3.InputPort inputPort, ArrayList<Color> colorsList, LightSensorMonitor lightSensorMonitor) {
        super(api, inputPort);
        this.colorsList = colorsList;
        this.lightSensorMonitor = lightSensorMonitor;
    }

    @Override
    public void run() {

    }

    public boolean getIsObstacleFound() throws InterruptedException {
        boolean res = false;
        try{

            lightSensorMonitor.give();
            res = colorsList.contains(getColor().get());
            lightSensorMonitor.release();

        } catch (IOException | ExecutionException | InterruptedException e) {
            e.printStackTrace();
            hasExceptionOccurred = true;
        }
        return res;
    }

    public boolean isHasExceptionOccurred() {
        return hasExceptionOccurred;
    }
}
