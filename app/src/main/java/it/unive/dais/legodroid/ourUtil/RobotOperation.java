package it.unive.dais.legodroid.ourUtil;

import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import it.unive.dais.legodroid.code.ManualActivity;
import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.plugs.GyroSensor;
import it.unive.dais.legodroid.lib.plugs.LightSensor;
import it.unive.dais.legodroid.lib.plugs.TachoMotor;

import static java.lang.Math.abs;

//TODO
public final class RobotOperation {

    private static final String genericErrorMessage = "Something went wrong.\n" +
            "Please try this operation again.";

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

    public static VirtualMap mapScan(EV3.Api api) throws RobotException {

        ArrayList<VirtualMap.MapTrack> trackList = new ArrayList<>();

        //Checks if the Robot is starting from a black line
        if (!RobotOperation.checkColor(api, LightSensor.Color.BLACK, true))
            throw new RobotException("The Robot Light Sensor must be positioned on the part of the black line " +
                    "pointed by the guide.");

        short blackLineIntensity = RobotOperation.getReflectedIntensity(api);

        //Tries to get the background color intensity
        short backgroundColorIntensity = RobotOperation.getBackgroundColorIntensity (api);

        while (RobotOperation.getReflectedColor(api) != LightSensor.Color.RED) {
            //Follows the black line
            ArrayList<LightSensor.Color> colorsToCheck = new ArrayList<>();
            colorsToCheck.add(LightSensor.Color.RED);
            colorsToCheck.add(LightSensor.Color.YELLOW);

            RobotOperation.followLine(api,
                    ManualActivity.Direction.FORWARD,
                    blackLineIntensity,
                    backgroundColorIntensity,
                    colorsToCheck
            );

            //Se non è alla fine del percorso scansiona la linea
            if (RobotOperation.getReflectedColor(api) != LightSensor.Color.RED) {

                RobotOperation.moveToTrack(api);

                scanTrack(api, backgroundColorIntensity, trackList);

                backTrack(api, backgroundColorIntensity, trackList.get(trackList.size() - 1));
            }
        }

        if (trackList.size() == 0)
            throw new RobotException("It seems your map has no tracks. \n" +
                    "Every map needs at least one track with at least one object position.\n" +
                    "If this isn't the case, please, make sure you positioned the robot facing the " +
                    "direction pointed by the guide.");

        RobotOperation.robotRotation(api, -145);
        RobotOperation.smallMovement(api, ManualActivity.Direction.FORWARD, 30);

        ArrayList<LightSensor.Color> colorsToCheck = new ArrayList<>();
        colorsToCheck.add(LightSensor.Color.RED);
        colorsToCheck.add(LightSensor.Color.YELLOW);


        while (RobotOperation.getReflectedColor(api) != LightSensor.Color.RED) {
            //Follows the black line
            RobotOperation.followLine(api,
                    ManualActivity.Direction.FORWARD,
                    blackLineIntensity,
                    backgroundColorIntensity,
                    colorsToCheck
            );

            //Se non è alla fine del percorso scansiona la linea
            if (RobotOperation.getReflectedColor(api) != LightSensor.Color.RED) {
                RobotOperation.smallMovement(api,ManualActivity.Direction.FORWARD,30);
            }
        }

        //The robot sets to his starting position.
        RobotOperation.robotRotation(api, 145);
        RobotOperation.smallMovement(api, ManualActivity.Direction.FORWARD, 10);
        RobotOperation.robotRotation(api, 45);

        return new VirtualMap(trackList, blackLineIntensity, backgroundColorIntensity);
    }


    //TODO CHECK
    public static void followLine (EV3.Api api, ManualActivity.Direction direction, short lineReflectedColor,
                                   short backgroundReflectedColor, ArrayList<LightSensor.Color> colorsList)
    throws RobotException{
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

            LightSensorMonitor lightSensorMonitor = new LightSensorMonitor();

            final double P = 0.5;
            final double I = 0.05;
            final double D = 0.5;
            final int OFFSET = 50;

            int maxSpeed;
            int integral = 0;
            int derivative = 0;
            int lastError = 0;

            if (direction == ManualActivity.Direction.FORWARD)
                maxSpeed = 3;
            else if (direction == ManualActivity.Direction.BACKWARD)
                maxSpeed = -3;
                //TODO To be changed to not throw exceptions
            else throw new IllegalArgumentException();

            LightSensorIntensity lightSensorIntensity = new LightSensorIntensity(api, EV3.InputPort._1, lightSensorMonitor);
            Thread lightIntensity = new Thread(lightSensorIntensity);
            lightIntensity.start();

            LightSensorColor lightSensorColor = new LightSensorColor(api, EV3.InputPort._1, colorsList, lightSensorMonitor);
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

                //if the difference between two values is big then is more probably make mistakes -> go slowly
                //if the difference is small you go on -> go quickly
                difference = 10 - abs(leftPower - rightPower);
                if(difference < 3)
                    difference = 3;

                rightMotor.setPower(rightPower * difference);
                leftMotor.setPower(leftPower * difference);

                Thread.sleep(25);

            }

            rightMotor.brake();
            leftMotor.brake();

            if (lightSensorColor.isHasExceptionOccurred()) {
                throw new RobotException(genericErrorMessage);
            }

        }
        catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new RobotException(genericErrorMessage);
        }
    }


    private static int normalizeOnPercent (short value, short min, short max) {
        return (100 * (value - min))/(max - min);
    }

    private static void backTrack(EV3.Api api, short backgroundColorIntensity, VirtualMap.MapTrack mapTrack) throws RobotException {

        for (int i = 0; i < mapTrack.getObjectList().size() + 1; i++) {



            //Moves a little bit back to position to the line
            RobotOperation.smallMovement (api, ManualActivity.Direction.BACKWARD, 30);


            //Follow the line backwards until the next position.
            ArrayList<LightSensor.Color> colorsToCheck = new ArrayList<>();
            colorsToCheck.add(LightSensor.Color.BLACK);


            RobotOperation.followLine(api,
                    ManualActivity.Direction.BACKWARD,
                    mapTrack.getTrackColorIntensity(),
                    backgroundColorIntensity,
                    colorsToCheck
            );

            //Check it is a black spot.
            if (!RobotOperation.checkColor(api, LightSensor.Color.BLACK, true))
                throw new RobotException(genericErrorMessage);
        }

        RobotOperation.robotRotation(api, -90);
        RobotOperation.smallMovement(api, ManualActivity.Direction.FORWARD, 30);
    }

    private static void scanTrack(EV3.Api api, short backgroundColorIntensity, ArrayList<VirtualMap.MapTrack> trackList) throws RobotException {

        LightSensor.Color trackColor = RobotOperation.getReflectedColor(api);
        short trackColorIntensity = RobotOperation.getReflectedIntensity(api);
        int positionsNumber = 0;

        ArrayList<LightSensor.Color> colorsToCheck = new ArrayList<>();
        colorsToCheck.add(LightSensor.Color.RED);
        colorsToCheck.add(LightSensor.Color.BLACK);

        while (RobotOperation.getReflectedColor(api) != LightSensor.Color.RED) {
            RobotOperation.followLine(api,
                    ManualActivity.Direction.FORWARD,
                    trackColorIntensity,
                    backgroundColorIntensity,
                    colorsToCheck

            );

            if (RobotOperation.getReflectedColor(api) == LightSensor.Color.BLACK) {
                positionsNumber++;
            } else
                if (!RobotOperation.checkColor(api, LightSensor.Color.RED, true))
                    throw new RobotException("It seems like this track contains a wrong color.");
        }

        if (positionsNumber < 0)
            throw new RobotException("It seems this track is empty. \n" +
                    "Every track needs at least a black object position.");
        else
            trackList.add(new VirtualMap.MapTrack(trackColor, trackColorIntensity, positionsNumber));
    }

    public static boolean checkColor(EV3.Api api, LightSensor.Color color, boolean isColor) throws RobotException {

        final LightSensor lightSensor = api.getLightSensor(EV3.InputPort._1);
        try {
            Future<LightSensor.Color> reflectedColor = lightSensor.getColor();

            return isColor == (reflectedColor.get() == color);

        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new RobotException(genericErrorMessage);
        }
    }

    public static LightSensor.Color getReflectedColor(EV3.Api api) throws RobotException {
        try {
            final LightSensor lightSensor = api.getLightSensor(EV3.InputPort._1);
            Future<LightSensor.Color> reflectedColor = lightSensor.getColor();
            return reflectedColor.get();
        } catch (IOException | ExecutionException | InterruptedException e) {
            e.printStackTrace();
            throw new RobotException(genericErrorMessage);
        }
    }

    public static short getReflectedIntensity(EV3.Api api) throws RobotException {
        try {
            final LightSensor lightSensor = api.getLightSensor(EV3.InputPort._1);
            Future<Short> reflectedIntensity = lightSensor.getReflected();
            return reflectedIntensity.get();
        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new RobotException(genericErrorMessage);
        }
    }

    //TODO Far muovere il robot a destra per prendere il colore prima dell'operazione di scelta del colore.
    public static short getBackgroundColorIntensity(EV3.Api api) throws RobotException {

        robotRotation (api, 90);

        smallMovement(api, ManualActivity.Direction.FORWARD, 100);

        if (!checkColor(api, LightSensor.Color.WHITE, true))
            throw new RobotException("The Robot could not detect the correct Map background color.\n" +
                    "Please, make sure the Map is positioned on a white surface and that the Robot is placed correctly.");
        short backgroundColor = getReflectedIntensity(api);

        smallMovement(api, ManualActivity.Direction.BACKWARD, 100);

        robotRotation (api, -90);

        return backgroundColor;
    }

    public static void robotRotation(EV3.Api api, float angle) throws RobotException {
        try {
            Motor rightMotor = new Motor(api, EV3.OutputPort.C, 5, 50);
            Motor leftMotor = new Motor(api, EV3.OutputPort.B, -5, 50);

            Thread right = new Thread(rightMotor);
            Thread left = new Thread(leftMotor);

            GyroSensor gyroSensor = api.getGyroSensor(EV3.InputPort._3);
            Future<Float> futureAngle = gyroSensor.getAngle();

            if (angle < 0) {
                float currentAngle = futureAngle.get();
                while (futureAngle.get() > currentAngle + angle) {

                    right.start();
                    left.start();

                    futureAngle = gyroSensor.getAngle();
                }
            }

            else {
                float currentAngle = futureAngle.get();
                while (futureAngle.get() < currentAngle + angle) {

                    right.start();
                    left.start();

                    futureAngle = gyroSensor.getAngle();
                }
            }

            right.interrupt();
            left.interrupt();

        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new RobotException(genericErrorMessage);
        }
    }

    //TODO
    public static void moveToTrack(EV3.Api api) throws RobotException {

    }


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
                    while (leftMotorPosition.get() > leftStartingPosition - newPositionOffset ||
                            rightMotorPosition.get() > rightStartingPosition - newPositionOffset) {

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
                    throw new RobotException(genericErrorMessage);
            }
        } catch (InterruptedException | ExecutionException | IOException e) {
            e.printStackTrace();
            throw new RobotException(genericErrorMessage);
        }
    }
}
