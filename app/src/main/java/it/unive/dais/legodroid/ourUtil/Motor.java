package it.unive.dais.legodroid.ourUtil;

import java.io.IOException;

import it.unive.dais.legodroid.code.ManualActivity;
import it.unive.dais.legodroid.lib.plugs.TachoMotor;

public class Motor extends Thread{

    private int speed;
    private int power;

    TachoMotor tachoMotor;

    public Motor(TachoMotor tachoMotor) {
        this.tachoMotor = tachoMotor;

        speed = 70;
        power = 100;

    }

    public Motor(TachoMotor tachoMotor, ManualActivity.Direction direction){

        this(tachoMotor);

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
