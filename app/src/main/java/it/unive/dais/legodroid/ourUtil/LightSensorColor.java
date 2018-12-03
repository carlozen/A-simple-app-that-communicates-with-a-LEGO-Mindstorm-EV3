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
    private LightSensor.Color colorObstacleFound = null;

    public LightSensorColor(EV3.Api api, EV3.InputPort inputPort, LightSensorMonitor lightSensorMonitor){
        super(api, inputPort);
        this.lightSensorMonitor = lightSensorMonitor;
    }

    public LightSensorColor(EV3.Api api, EV3.InputPort inputPort, ArrayList<Color> colorsList, LightSensorMonitor lightSensorMonitor) {
        this(api, inputPort, lightSensorMonitor);
        this.colorsList = colorsList;
    }

    @Override
    public void run() {

    }

    public boolean getIsObstacleFound() {
        boolean res = false;
        try{

            lightSensorMonitor.give();
            res = colorsList.contains(getColor().get());

            if(res)
                colorObstacleFound = getColor().get();

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

    public LightSensor.Color getColorObstacleFound() {
        return colorObstacleFound;
    }

    public LightSensor.Color getColorNow() throws IOException, ExecutionException, InterruptedException {
        LightSensor.Color color;

        lightSensorMonitor.give();
        color = super.getColor().get();
        lightSensorMonitor.release();

        return color;
    }
}
