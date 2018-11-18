package it.unive.dais.legodroid.ourUtil;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import it.unive.dais.legodroid.lib.plugs.TachoMotor;

public class Grabber extends Thread{

    Future<Float> position;
    Future<Float> position1;
    Future<Float> position2;

    TachoMotor tachoMotor;

    public Grabber(TachoMotor tachoMotor, int stato){
        this.tachoMotor = tachoMotor;
    }

    public void run(){
        super.run();
        try {
            position = tachoMotor.getPosition();
            float uno = position.get();
            tachoMotor.resetPosition();
            position2 = tachoMotor.getPosition();
            float due = position2.get();
            tachoMotor.goToPositionAbs(100);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}
