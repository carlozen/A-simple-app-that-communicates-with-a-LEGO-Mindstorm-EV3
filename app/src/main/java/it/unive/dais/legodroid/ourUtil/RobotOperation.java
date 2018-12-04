package it.unive.dais.legodroid.ourUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import it.unive.dais.legodroid.code.ManualActivity;
import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.plugs.GyroSensor;
import it.unive.dais.legodroid.lib.plugs.LightSensor;

import static java.lang.Math.abs;

//TODO
public final class RobotOperation {

    /*
    //TODO ENUM Specifico per impedire direzioni diverse da FORWARD e BACKWARD
    public static void followLine (EV3.Api api, ManualActivity.Direction direction,
                                   LightSensor.Color lineColor, short lineReflectedColor,
                                   short backgroundReflectedColor, ArrayList<LightSensor.Color> colorsList) {
        try {
            TachoMotor leftTachoMotor = api.getTachoMotor(EV3.OutputPort.B);
            TachoMotor rightTachoMotor = api.getTachoMotor(EV3.OutputPort.C);
            Motor rightMotor = null;
            Motor leftMotor = null;

            final LightSensor lightSensor = api.getLightSensor(EV3.InputPort._1);

            final int I = 1;
            final int OFFSET = 50;
            int maxSpeed;
            double integral = 0;
            double lastError = 0;
            if (direction == ManualActivity.Direction.FORWARD)
                maxSpeed = 5;
            else if (direction == ManualActivity.Direction.BACKWARD)
                maxSpeed = -9;
            //TODO To be changed to not throw exceptions
            else throw new IllegalArgumentException();

            //Future<LightSensor.Color> reflectedColor = lightSensor.getColor();


            Future<Short> reflectedIntensity = lightSensor.getReflected();

            short actualIntensity = reflectedIntensity.get();


            while (actualIntensity < 100) {

                int error = (actualIntensity);

                reflectedIntensity = lightSensor.getReflected();

                if (error <= 3) {
                    error = -1;
                }
                else {
                    error = 1;
                }

                if (error<0) {
                    rightMotor = new Motor(rightTachoMotor, 2, 100);

                    leftMotor = new Motor(leftTachoMotor, 3, 100);

                    rightMotor.start();
                    leftMotor.start();
                }

                else {
                    rightMotor = new Motor(rightTachoMotor, 3, 100);

                    leftMotor = new Motor(leftTachoMotor, 2, 100);

                    rightMotor.start();
                    leftMotor.start();
                }

                //reflectedColor = lightSensor.getColor();
                actualIntensity = reflectedIntensity.get();
            }

            leftTachoMotor.stop();
            rightTachoMotor.stop();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

*/

    //TODO CHECK
    public static LightSensor.Color followLine (EV3.Api api, LightSensorMonitor lightSensorMonitor,
                                                ManualActivity.Direction direction,
                                                LightSensor.Color lineColor, short lineReflectedColor,
                                                short backgroundReflectedColor, ArrayList<LightSensor.Color> colorsList)
    throws RobotException{



        LightSensorColor lightSensorColor = null;

        try {

             /*class SynchronizedBoolean {
                private boolean bool;

                private SynchronizedBoolean (boolean bool) {
                    this.setBoolean(bool);
                }

                public synchronized boolean getBoolean () {
                    return bool;
                }

                public synchronized void setBoolean (boolean bool) {
                    this.bool = bool;
                }

            }


            final SynchronizedBoolean isObstacleFound = new SynchronizedBoolean(false);
            final SynchronizedBoolean hasExceptionOccurred = new SynchronizedBoolean(false);*/

            /* boolean isObstacleFound = false;
             boolean hasExceptionOccurred = false;*/

            //final LightSensor lightSensor = new LightSensor(api, EV3.InputPort._1);
            Thread right = null, left = null;

            Motor rightMotor = new Motor(api, EV3.OutputPort.C,0,0);
            Motor leftMotor = new Motor(api, EV3.OutputPort.B,0,0);

            right = new Thread(rightMotor);
            left = new Thread(leftMotor);

            right.start();
            left.start();

            Short reflectedIntensity;




            final double P = 0.5;
            final double I = 0.05;
            final double D = 0.5;
            final int OFFSET = 50;

            int maxSpeed;
            int integral = 0;
            int derivative = 0;
            int lastError = 0;

            //if (direction == ManualActivity.Direction.FORWARD)
                maxSpeed = 2;
            /*else if (direction == ManualActivity.Direction.BACKWARD)
                maxSpeed = -3;*/
                //TODO To be changed to not throw exceptions
            //else throw new IllegalArgumentException();

            LightSensorIntensity lightSensorIntensity = new LightSensorIntensity(api, EV3.InputPort._1, lightSensorMonitor);
            Thread lightIntensity = new Thread(lightSensorIntensity);
            lightIntensity.start();

            lightSensorColor = new LightSensorColor(api, EV3.InputPort._1, colorsList, lightSensorMonitor);
            Thread lightColor = new Thread(lightSensorColor);
            lightColor.start();

            int turningValue = 0;
            int leftPower, rightPower, difference;

            while (!lightSensorColor.getIsObstacleFound()) {

                rightMotor.setPower(0);
                leftMotor.setPower(0);

                reflectedIntensity = lightSensorIntensity.getReflectedNow();

                int error = (normalizeOnPercent(reflectedIntensity, lineReflectedColor,
                        backgroundReflectedColor) - OFFSET) /10;
                if (error * lastError > 0)
                    integral = integral + error;
                else
                    integral = 0;
                derivative = error - lastError;
                lastError = error;
                turningValue = (int) (P*error + I*integral + D*derivative);

                leftPower = maxSpeed - turningValue;
                rightPower = maxSpeed + turningValue;

                if(leftPower == 0 && rightPower == 0){
                    leftPower = 20;
                    rightPower = 20;
                }

                //if the difference between two values is big then is more probably make mistakes -> go slowly
                //if the difference is small you go on -> go quickly

                difference = 10 - abs(leftPower - rightPower);
                if(difference < 3)
                    difference = 3;

                if(direction == ManualActivity.Direction.FORWARD) {
                    rightMotor.setPower(rightPower * difference);
                    leftMotor.setPower(leftPower * difference);
                } else {
                    rightMotor.setPower(rightPower * difference * (-1));
                    leftMotor.setPower(leftPower * difference * (-1));
                }

                Thread.sleep(25);

            }

            rightMotor.setPower(0);
            leftMotor.setPower(0);

            rightMotor.brake();
            leftMotor.brake();

            right.interrupt();
            left.interrupt();

            lightColor.interrupt();
            lightIntensity.interrupt();

            if (lightSensorColor.isHasExceptionOccurred()) {
                throw new RobotException("Something went wrong. Please try this operation again.");
            }

        }
        catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new RobotException("Something went wrong. Please try this operation again.");
        }
        return lightSensorColor.getColorObstacleFound();
    }


    private static int normalizeOnPercent (short value, short min, short max) {
        return (100 * (value - min))/(max - min);
    }


    public static void checkColor(EV3.Api api, LightSensorMonitor lightSensorMonitor, LightSensor.Color color, boolean isColor) throws RobotException {

        ArrayList<LightSensor.Color> colorArrayList = new ArrayList<>();
        colorArrayList.add(color);

        LightSensorColor lightSensorColor = new LightSensorColor(api, EV3.InputPort._1, colorArrayList, lightSensorMonitor);
        Thread t = new Thread(lightSensorColor);
        t.start();
        try {
            LightSensor.Color reflectedColor = lightSensorColor.getColorNow();

            if (isColor) {
                if (reflectedColor != color)
                    throw new RobotException("It seems like the Robot has failed this operation. \n" +
                            "Please, make sure you are correctly following the rules of this specific operation or that the " +
                            "map format is correct and then try to start this operation again.");
            }

            else {
                if (reflectedColor == color)
                    throw new RobotException("It seems like the Robot has failed this operation. \n" +
                            "Please, make sure you are correctly following the rules of this specific operation or that the " +
                            "map format is correct and then try to start this operation again.");
            }

        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new RobotException("Something went wrong. Please try this operation again.");
        }
        t.interrupt();
        return;
    }

    public static LightSensor.Color getReflectedColor(EV3.Api api, LightSensorMonitor lightSensorMonitor) throws RobotException {
        try {
            LightSensor.Color color;

            LightSensorColor lightSensorColor = new LightSensorColor(api,EV3.InputPort._1, lightSensorMonitor);
            Thread t = new Thread(lightSensorColor);
            t.start();

            color = lightSensorColor.getColorNow();
            t.interrupt();

            return color;
        } catch (IOException | ExecutionException | InterruptedException e) {
            e.printStackTrace();
            throw new RobotException("Something went wrong. Please try this operation again.");
        }
    }

    public static short getReflectedIntensity(EV3.Api api, LightSensorMonitor lightSensorMonitor) throws RobotException {
        try {
            short res;

            LightSensorIntensity lightSensorIntensity = new LightSensorIntensity(api, EV3.InputPort._1, lightSensorMonitor);
            Thread t = new Thread(lightSensorIntensity);
            t.start();

            res = lightSensorIntensity.getReflectedNow();
            t.interrupt();

            return res;
        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new RobotException("Something went wrong. Please try this operation again.");
        }
    }

    //TODO Far muovere il robot a destra per prendere il colore prima dell'operazione di scelta del colore.
    public static short getBackgroundColorIntensity(EV3.Api api, LightSensorMonitor lightSensorMonitor) throws RobotException {

        checkColor(api, lightSensorMonitor, LightSensor.Color.BLACK, true);
        robotRotation (api, 30, VirtualMap.Wheel.LEFT);
        //turnUntilColor(api, lightSensorMonitor, LightSensor.Color.WHITE, VirtualMap.Wheel.LEFT, ManualActivity.Direction.FORWARD);

        //smallMovement(api, ManualActivity.Direction.FORWARD, 100);

        checkColor(api, lightSensorMonitor, LightSensor.Color.WHITE, true);
        short backgroundColor = getReflectedIntensity(api, lightSensorMonitor);

        //smallMovement(api, ManualActivity.Direction.BACKWARD, -100);

        robotRotation (api, -30, VirtualMap.Wheel.LEFT);
        //turnUntilColor(api, lightSensorMonitor, LightSensor.Color.BLACK, VirtualMap.Wheel.LEFT, ManualActivity.Direction.BACKWARD);


        return backgroundColor;
    }

    public static void robotRotation(EV3.Api api, float angle, VirtualMap.Wheel wheel) throws RobotException {
        try {
            int power = 20;

            GyroSensorAngle gyroSensorAngle = new GyroSensorAngle(api, EV3.InputPort._3);
            Thread gyroSensor = new Thread(gyroSensorAngle);
            gyroSensor.start();

            float currentAngle = gyroSensorAngle.getAngleNow();
            if(wheel == VirtualMap.Wheel.LEFT) {
                Motor leftMotor = new Motor(api, EV3.OutputPort.B);
                Thread left = new Thread(leftMotor);
                left.start();
                if (angle < 0) {
                    leftMotor.setPower(-power);
                    while (gyroSensorAngle.getAngleNow() > currentAngle + angle) {
                        Thread.sleep(15);
                    }
                } else {
                    leftMotor.setPower(power);
                    while (gyroSensorAngle.getAngleNow() < currentAngle + angle) {
                        Thread.sleep(15);
                    }
                }
                leftMotor.setPower(0);
                leftMotor.brake();
                left.interrupt();
            } else {
                Motor rightMotor = new Motor(api, EV3.OutputPort.C);
                Thread right = new Thread(rightMotor);
                right.start();
                if (angle < 0) {
                    rightMotor.setPower(power);
                    while (gyroSensorAngle.getAngleNow() > currentAngle + angle) {
                    }
                } else {
                    rightMotor.setPower(-power);
                    while (gyroSensorAngle.getAngleNow() < currentAngle + angle) {
                    }
                }
                rightMotor.setPower(0);
                rightMotor.brake();
                right.interrupt();
            }

            gyroSensor.interrupt();

        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new RobotException("Something went wrong. Please try this operation again.");
        }
        return;
    }

    /*public static void moveToTrack(EV3.Api api) throws RobotException {

        robotRotation (api, 10);

        smallMovement(api, ManualActivity.Direction.FORWARD, 50);

        checkColor(api, LightSensor.Color.WHITE, true);
        short backgroundColor = getReflectedIntensity(api);

        smallMovement(api, ManualActivity.Direction.BACKWARD, -50);

        robotRotation (api, -90);
        checkColor(api, LightSensor.Color.WHITE, false);
    }*/

    public static void smallMovement(EV3.Api api, ManualActivity.Direction direction, int newPositionOffset) throws RobotException {
        try {
            Thread right = null, left = null;

            Motor rightMotor = new Motor(api, EV3.OutputPort.C);
            Motor leftMotor = new Motor(api, EV3.OutputPort.B);

            rightMotor.setPower(50);
            leftMotor.setPower(50);

            Future<Float> leftMotorPosition = leftMotor.getPosition();
            Future<Float> rightMotorPosition = rightMotor.getPosition();

            switch (direction) {
                case FORWARD: {
                    float leftStartingPosition = leftMotorPosition.get();
                    float rightStartingPosition = rightMotorPosition.get();
                    while (leftMotorPosition.get() < leftStartingPosition + newPositionOffset ||
                            rightMotorPosition.get() < rightStartingPosition + newPositionOffset) {

                        rightMotor.setSpeed(5);
                        leftMotor.setSpeed(5);

                        right = new Thread(rightMotor);
                        left = new Thread(leftMotor);

                        right.start();
                        left.start();

                        leftMotorPosition = leftMotor.getPosition();
                        rightMotorPosition = rightMotor.getPosition();
                    }
                    right.interrupt();
                    left.interrupt();
                    break;
                }
                case BACKWARD: {
                    float leftStartingPosition = leftMotorPosition.get();
                    float rightStartingPosition = rightMotorPosition.get();
                    while (leftMotorPosition.get() > leftStartingPosition + newPositionOffset ||
                            rightMotorPosition.get() > rightStartingPosition + newPositionOffset) {

                        rightMotor.setSpeed(-5);
                        leftMotor.setSpeed(-5);

                        right = new Thread(rightMotor);
                        left = new Thread(leftMotor);

                        right.start();
                        left.start();

                        leftMotorPosition = leftMotor.getPosition();
                        rightMotorPosition = rightMotor.getPosition();
                    }
                    left.interrupt();
                    right.interrupt();
                    break;
                }
                default:
                    throw new RobotException("Something went wrong. Please try this operation again.");
            }
        } catch (InterruptedException | ExecutionException | IOException e) {
            e.printStackTrace();
        }
    }

    public static void turnUntilColor(EV3.Api api, LightSensorMonitor lightSensorMonitor, LightSensor.Color color, VirtualMap.Wheel wheel, ManualActivity.Direction direction) {
        try {
            int power;
            if (direction == ManualActivity.Direction.FORWARD){
                power = 20;
            } else {
                power = -20;
            }
            Motor rightMotor = new Motor(api, EV3.OutputPort.C);
            Motor leftMotor = new Motor(api, EV3.OutputPort.B);

            Thread right = new Thread(rightMotor);
            Thread left = new Thread(leftMotor);

            right.start();
            left.start();

            if(wheel == VirtualMap.Wheel.LEFT) {
                leftMotor.setPower(power);
                while (RobotOperation.getReflectedColor(api, lightSensorMonitor) != color) {
                    try {
                        Thread.sleep(25);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                rightMotor.setPower(power);
                while (RobotOperation.getReflectedColor(api, lightSensorMonitor) != color) {
                    try {
                        Thread.sleep(25);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            rightMotor.setPower(0);
            leftMotor.setPower(0);

            right.interrupt();
            left.interrupt();

        } catch (IOException | RobotException e) {
            e.printStackTrace();
        }
        return;
    }

    public static void smallMovementUntilColor(EV3.Api api, LightSensorMonitor lightSensorMonitor, LightSensor.Color trackColor, ManualActivity.Direction direction, ManualActivity.Direction turn) throws RobotException, IOException, InterruptedException {
        int power = 20;
        int sign;

        if(direction == ManualActivity.Direction.FORWARD){
            sign = 1;
        } else {
            sign = -1;
        }

        Motor rightMotor = new Motor(api, EV3.OutputPort.C);
        Motor leftMotor = new Motor(api, EV3.OutputPort.B);

        Thread right = new Thread(rightMotor);
        Thread left = new Thread(leftMotor);

        right.start();
        left.start();

        while(RobotOperation.getReflectedColor(api, lightSensorMonitor) != trackColor){
            if(turn == ManualActivity.Direction.LEFT){
                leftMotor.setPower((power - 10) * sign);
                rightMotor.setPower(power * sign);
            } else {
                rightMotor.setPower((power - 10) * sign);
                leftMotor.setPower(power * sign);
            }
            Thread.sleep(150);
            leftMotor.setPower(0);
            rightMotor.setPower(0);

            leftMotor.brake();
            rightMotor.brake();
        }

        right.interrupt();
        left.interrupt();
    }
}
