package it.unive.dais.legodroid.code;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import java.io.IOException;

import it.unive.dais.legodroid.R;
import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.plugs.TachoMotor;
import it.unive.dais.legodroid.lib.util.Prelude;
import it.unive.dais.legodroid.ourUtil.Grabber;
import it.unive.dais.legodroid.ourUtil.Motor;

public class ManualActivity extends AppCompatActivity{

    public enum Direction {
        FORWARD,
        BACKWARD,
        LEFT,
        RIGHT
    }

    protected Motor tachoGrabber = null;

    protected Motor rightMotor = null;
    protected Motor leftMotor = null;
    protected Grabber grabber = null;

    private Thread t1,t2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual);

        Button up = findViewById(R.id.up);
        Button down = findViewById(R.id.down);
        Button left = findViewById(R.id.left);
        Button right = findViewById(R.id.right);

        Button raiseGrabber = findViewById(R.id.raiseGrabber);
        Button lowerGrabber = findViewById(R.id.lowerGrabber);


        ManualActivity thisActivity = this;

        up.setOnTouchListener(startAndStop(thisActivity, 2, Direction.FORWARD));
        down.setOnTouchListener(startAndStop(thisActivity, 2, Direction.BACKWARD));
        right.setOnTouchListener(startAndStop(thisActivity, 1, Direction.RIGHT));
        left.setOnTouchListener(startAndStop(thisActivity, 1, Direction.LEFT));

        raiseGrabber.setOnClickListener(v -> Prelude.trap(() -> MainActivity.ev3.run(this::raiseGrabber)));

    }



    private View.OnTouchListener startAndStop(ManualActivity manualActivity, int numberOfMotors, Direction direction) {
        return new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    Prelude.trap(() -> MainActivity.ev3.run(api -> manualActivity.startMotors(api, direction, numberOfMotors)));
                } else {
                    if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        Prelude.trap(() -> MainActivity.ev3.run(manualActivity::stopMotors));
                    }
                }
                return true;
            }
        };
    }

    private void raiseGrabber(EV3.Api api){
        //tachoGrabber = api.getTachoMotor(EV3.OutputPort.A);

        grabber = new Grabber(tachoGrabber, 0);
        grabber.start();
    }

    private void stopMotors(EV3.Api api) {

        try {

            if(leftMotor != null)
                leftMotor.stop();
            if(rightMotor != null)
                rightMotor.stop();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void startMotors(EV3.Api api, Direction direction, int numberOfMotors){

        if(numberOfMotors == 2) {

            leftMotor = new Motor(api, EV3.OutputPort.B, direction);
            t1 = new Thread(leftMotor);

            rightMotor = new Motor(api, EV3.OutputPort.C, direction);
            t2 = new Thread(rightMotor);

            t1.start();
            t2.start();

        } else {
            if(direction == Direction.LEFT) {

                rightMotor = new Motor(api, EV3.OutputPort.C,100,100);
                t2 = new Thread(rightMotor);
                t2.start();

            } else {

                leftMotor = new Motor(api, EV3.OutputPort.B, 100, 100);
                t1 = new Thread(leftMotor);
                t1.start();

            }
        }



    }

























}
