package it.unive.dais.legodroid.ourUtil;

import android.util.Log;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import it.unive.dais.legodroid.code.ManualActivity;
import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.plugs.TachoMotor;
import it.unive.dais.legodroid.lib.plugs.UltrasonicSensor;

public class Grabber extends TachoMotor implements Runnable{

    Future<Float> position;
    Future<Float> position1;
    Future<Float> position2;

    boolean isPresent;

    public Grabber(EV3.Api api, EV3.OutputPort outputPort){
        super(api, outputPort);
    }

    public Grabber(EV3.Api api, EV3.OutputPort outputPort, int speed, int power) {
        this(api, outputPort);

        try{
            setPower(power);
            setSpeed(speed);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /*public static void moveGrabber(Grabber grabber, Thread t3, EV3.Api api, ManualActivity.Direction direction, int speed, int power){
        grabber = new Grabber(api, EV3.OutputPort.A, direction, speed, power );
        t3 = new Thread(grabber);
        t3.start();
    }*/


    @Override
    public void run() {
        try {
            super.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public static void moveDownGrabber(EV3.Api api, Grabber grabber){
        grabber = new Grabber(api, EV3.OutputPort.A);
        try {
            grabber.setStepPower(-60, 5, 0, 145, true);
            try {
                Thread.currentThread().sleep(1000);
                grabber.stop();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void moveUpGrabber(EV3.Api api, Grabber grabber){
        grabber = new Grabber(api, EV3.OutputPort.A);
        try {
            grabber.setStepPower(60, 5, 0, 145, true);
            try {
                Thread.currentThread().sleep(1000);
                grabber.stop();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            grabber.stop();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void inizializeGrabber(EV3.Api api, Grabber grabber, Thread thread){
        grabber = new Grabber(api, EV3.OutputPort.A, 30, 30 );
        thread = new Thread(grabber);
        thread.start();
        try {
            Thread.currentThread().sleep(1000);
            grabber.brake();
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
        thread.interrupt();
    }

    public static void stopGrabber(EV3.Api api, Grabber grabber, Thread thread){
        if(grabber != null){
            try {
                grabber.stop();
            } catch (IOException e) {
                e.printStackTrace();
            }
            thread.interrupt();
        }
    }



    public static void moveGrabber(EV3.Api api, Grabber grabber, Thread thread, int speed, int power){
        grabber = new Grabber(api, EV3.OutputPort.A, speed, power );
        thread = new Thread(grabber);
        thread.start();
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
}
