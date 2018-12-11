package it.unive.dais.legodroid.code;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import java.io.IOException;
import java.util.concurrent.Future;

import it.unive.dais.legodroid.R;
import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.util.Consumer;
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


    protected Motor rightMotor = null;
    protected Motor leftMotor = null;
    protected Grabber grabber = null;

    private Thread t1,t2;
    private Thread t3;

    private boolean isGrabberUp = true;
    private boolean load;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual);

        Button up = findViewById(R.id.up);
        Button down = findViewById(R.id.down);
        Button left = findViewById(R.id.left);
        Button right = findViewById(R.id.right);

        Button takeGrabber = findViewById(R.id.take);
        Button releaseGrabber = findViewById(R.id.release);


        /*ManualActivity thisActivity = this;
        try {
            isGrabberUp = true;
            MainActivity.ev3.run(api -> Grabber.inizializeGrabber(api, grabber, t3));
        } catch (EV3.AlreadyRunningException e) {
            e.printStackTrace();
        }

        up.setOnTouchListener(startAndStop(thisActivity, 2, Direction.FORWARD));
        down.setOnTouchListener(startAndStop(thisActivity, 2, Direction.BACKWARD));
        right.setOnTouchListener(startAndStop(thisActivity, 1, Direction.RIGHT));
        left.setOnTouchListener(startAndStop(thisActivity, 1, Direction.LEFT));


        //releaseGrabber.setOnTouchListener(moveGrabber(thisActivity, 10, 10));
        //takeGrabber.setOnTouchListener(moveGrabber(thisActivity, -10, -10));

        releaseGrabber.setOnTouchListener(raiseUp(thisActivity));
        takeGrabber.setOnTouchListener(moveDown(thisActivity));*/
    }



    private View.OnTouchListener raiseUp(ManualActivity manualActivity){
        return new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    if (isGrabberUp == false) {
                        isGrabberUp = true;
                        //Prelude.trap(() -> MainActivity.ev3.run(api -> Grabber.moveUpGrabber(api, grabber)));
                        load = false;
                    }
                }
                return true;
            }
        };
    }

    private View.OnTouchListener moveDown(ManualActivity manualActivity){
        return new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    if(isGrabberUp == true){
                        isGrabberUp = false;
                        //Prelude.trap(() -> MainActivity.ev3.run(api -> Grabber.moveDownGrabber(api, grabber)));

                        try {
                            MainActivity.ev3.run(new Consumer<EV3.Api>() {
                                @Override
                                public void call(EV3.Api data) {
                                    load = Grabber.getIsPresent(data);
                                }
                            });
                        } catch (EV3.AlreadyRunningException e) {
                            e.printStackTrace();
                        }
                    }
                }
                return true;
            }
        };
    }


    private View.OnTouchListener startAndStop(ManualActivity manualActivity, int numberOfMotors, Direction direction) {
        return new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    Prelude.trap(() -> MainActivity.ev3.run(api -> manualActivity.startMotors(api, direction, numberOfMotors)));
                } else {
                    if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        Prelude.trap(() -> MainActivity.ev3.run(manualActivity::stopMotors));
                    }
                }
                return true;
            }
        };
    }

    private View.OnTouchListener moveGrabber(ManualActivity manualActivity, int speed, int power) {
        return new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                        //Prelude.trap(() -> MainActivity.ev3.run(api -> Grabber.moveGrabber(api, grabber, t3, speed, power)));
                } else {
                    if(motionEvent.getAction()== MotionEvent.ACTION_UP){
                        //Prelude.trap(() -> MainActivity.ev3.run(api -> Grabber.stopGrabber(api, grabber, t3)));
                    }
                }
                return true;
            }
        };
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

    private void stopMotors(EV3.Api api) {

        try{

            if(leftMotor != null) {
                leftMotor.stop();
                t1.interrupt();
            }

            if(rightMotor != null) {
                rightMotor.stop();
                t2.interrupt();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

























}
