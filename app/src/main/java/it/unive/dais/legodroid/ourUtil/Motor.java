package it.unive.dais.legodroid.ourUtil;

import java.io.IOException;

import it.unive.dais.legodroid.code.ManualActivity;
import it.unive.dais.legodroid.lib.plugs.TachoMotor;

public class Motor extends Thread{

    private int speed;
    private int power;

    TachoMotor tachoMotor;

    public Motor(TachoMotor tachoMotor, int speed, int power){

        this.tachoMotor = tachoMotor;
        this.power = power;
        this.speed = speed;

    }

    public Motor(TachoMotor tachoMotor) {

        this(tachoMotor, 70, 100);

    }

    public Motor(TachoMotor tachoMotor, ManualActivity.Direction direction){

        this(tachoMotor, 70, 100);

        if(direction == ManualActivity.Direction.BACKWARD){
            speed = 40;
            power = 100;
        }

    }

    @Override
    public void run(){
        super.run();
        try {
            tachoMotor.setPower(power);
            tachoMotor.setSpeed(speed);
            tachoMotor.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
