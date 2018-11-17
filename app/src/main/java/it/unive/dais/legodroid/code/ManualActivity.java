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
import it.unive.dais.legodroid.ourUtil.Motor;

public class ManualActivity extends AppCompatActivity{

    public enum Direction {
        FORWARD,
        BACKWARD,
        LEFT,
        RIGHT
    }

    protected TachoMotor leftMotor = null;
    protected TachoMotor rightMotor = null;

    protected Motor right = null;
    protected Motor left = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual);

        Button up = findViewById(R.id.up);
        Button down = findViewById(R.id.down);
        Button left = findViewById(R.id.left);
        Button right = findViewById(R.id.right);


        /*up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*try {
                    MainActivity.ev3.run(api -> {
                        leftMotor = api.getTachoMotor(EV3.OutputPort.B);
                        Log.i("porta","porta settata --> ");
                        try {
                            leftMotor.setSpeed(30);
                            leftMotor.start();
                            Log.i("acceso","motore acceso ");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                } catch (EV3.AlreadyRunningException e) {
                    e.printStackTrace();
                }

            }
        });*/

        ManualActivity thisActivity = this;

        //up.setOnClickListener(v -> Prelude.trap(() -> MainActivity.ev3.run(this::useTwoMotors)));
        up.setOnTouchListener(startAndStop(thisActivity, 2, Direction.FORWARD));
        down.setOnTouchListener(startAndStop(thisActivity, 2, Direction.BACKWARD));
        right.setOnTouchListener(startAndStop(thisActivity, 1, Direction.RIGHT));
        left.setOnTouchListener(startAndStop(thisActivity, 1, Direction.LEFT));

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

            leftMotor = api.getTachoMotor(EV3.OutputPort.B);
            rightMotor = api.getTachoMotor(EV3.OutputPort.C);

            left = new Motor(leftMotor, direction);
            right = new Motor(rightMotor, direction);

            left.start();
            right.start();

        } else {
            if(direction == Direction.LEFT) {

                rightMotor = api.getTachoMotor(EV3.OutputPort.C);
                right = new Motor(rightMotor);
                right.start();

            } else {

                leftMotor = api.getTachoMotor(EV3.OutputPort.B);
                left = new Motor(leftMotor);
                left.start();

            }
        }



    }

























}
