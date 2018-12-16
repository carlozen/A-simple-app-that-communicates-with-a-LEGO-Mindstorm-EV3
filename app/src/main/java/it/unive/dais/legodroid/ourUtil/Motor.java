package it.unive.dais.legodroid.ourUtil;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import it.unive.dais.legodroid.code.ManualActivity;
import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.plugs.TachoMotor;

public class Motor extends TachoMotor implements Runnable{

    final private float wheelRadius = 2.25f; //TODO: misura in centimetri -> cambiare?

    public Motor(EV3.Api api, EV3.OutputPort outputPort){
        super(api, outputPort);
    }

    public Motor(EV3.Api api, EV3.OutputPort outputPort, int speed, int power){
        this(api, outputPort);
        try {
            setSpeed(speed);
            setPower(power);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Motor(EV3.Api api, EV3.OutputPort outputPort, ManualActivity.Direction direction) {
        this(api, outputPort);

        try{
            setPower(100);
            if(direction == ManualActivity.Direction.BACKWARD){
                setSpeed(-100);
            }else{
                setSpeed(100);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }




    @Override
    public void run(){
        try {
            super.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void move(float a) throws IOException, ExecutionException, InterruptedException {

        float alpha = (180 * a) / ((float)Math.PI * wheelRadius); //TODO: le misure sono in centimetri, se sono in altra unità di misura modificare a e wheelRadius

        float position = getPosition().get();

        setPower(20);

        while(getPosition().get() < position + alpha){ //TODO: forse  getPosition().get() < position + alpha --> dipende da come gira
        }

        setPower(0);

    }
}
