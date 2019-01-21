package it.unive.dais.legodroid.ourUtil;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.plugs.TachoMotor;
import it.unive.dais.legodroid.lib.plugs.UltrasonicSensor;

public class Grabber extends TachoMotor implements Runnable{

    private final float angle = 90;
    private static boolean up = false;

    boolean isPresent;

    public static void setDown () {
        up = false;
    }

    public static void setUp () {
        up = true;
    }

    public Grabber(EV3.Api api){
        super(api, EV3.OutputPort.A);
    }

    @Override
    public void run() {
        try {
            super.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean getIsPresent(EV3.Api api){
        Future<Float> futureDistance;
        float distance;
        UltrasonicSensor ultrasonicSensor = new UltrasonicSensor(api, EV3.InputPort._4);
        try {
            futureDistance = ultrasonicSensor.getDistance();
            distance = futureDistance.get();
            if (distance < 6.0)
                return true;
        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return false;
    }


    public void killThread(Thread t3) {
        try {
            stop();
        } catch (IOException e) {
            e.printStackTrace();
        }
        t3.interrupt();
    }

    public void down() throws IOException, ExecutionException, InterruptedException {
        if(isUp()){
            float position = getPosition().get();

            setPower(-50);

            while(getPosition().get() > position - angle){
            }

            setPower(0);

            up = false;
        }

    }

    public boolean isUp() {
        return up;
    }

    public void up() throws IOException, ExecutionException, InterruptedException {
        if(!isUp()){
            float position = getPosition().get();

            setPower(80);

            while(getPosition().get() < position + angle){
            }

            setPower(0);

            up = true;
        }
    }
}
