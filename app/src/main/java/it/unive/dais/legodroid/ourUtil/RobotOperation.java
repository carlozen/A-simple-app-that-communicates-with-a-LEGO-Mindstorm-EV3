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
    public static void followLine (EV3.Api api, ManualActivity.Direction direction,
                                   LightSensor.Color lineColor, short lineReflectedColor,
                                   short backgroundReflectedColor, ArrayList<LightSensor.Color> colorsList)
    throws RobotException{
        try {

             class SynchronizedBoolean {
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
            final SynchronizedBoolean hasExceptionOccurred = new SynchronizedBoolean(false);

            final LightSensor lightSensor = new LightSensor(api, EV3.InputPort._1);
            final TachoMotor leftTachoMotor = api.getTachoMotor(EV3.OutputPort.B);
            final TachoMotor rightTachoMotor = api.getTachoMotor(EV3.OutputPort.C);
            Motor rightMotor;
            Motor leftMotor;

            final int P = 1;
            final int I = 0;
            final int D = 0;
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

            new Thread (() -> {
                try {
                    Future<LightSensor.Color> reflectedColor = lightSensor.getColor();
                    while (!colorsList.contains(reflectedColor.get())) {
                        Thread.sleep(200);
                        reflectedColor = lightSensor.getColor();

                    }
                    isObstacleFound.setBoolean(true);
                } catch (IOException | ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                    hasExceptionOccurred.setBoolean(true);
                }
            }).start();

            Future<Short> reflectedIntensity = lightSensor.getReflected();

            while (!isObstacleFound.getBoolean() || !hasExceptionOccurred.getBoolean()) {
                int error = (normalizeOnPercent(reflectedIntensity.get(), lineReflectedColor,
                        backgroundReflectedColor) - OFFSET) /10;
                integral = integral + error;
                derivative = error - lastError;
                int turningValue = P*error + I*integral + D*derivative;

                rightMotor = new Motor(rightTachoMotor, (maxSpeed + turningValue), 50);

                leftMotor = new Motor(leftTachoMotor, (maxSpeed - turningValue), 50);

                rightMotor.start();
                leftMotor.start();

                reflectedIntensity = lightSensor.getReflected();
            }

            leftTachoMotor.stop();
            rightTachoMotor.stop();

            if (hasExceptionOccurred.getBoolean()) {
                throw new RobotException("Something went wrong. Please try this operation again.");
            }

        }
        catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new RobotException("Something went wrong. Please try this operation again.");
        }
    }


    private static int normalizeOnPercent (short value, short min, short max) {
        return (100 * (value - min))/(max - min);
    }


    public static void checkColor(EV3.Api api, LightSensor.Color color, boolean isColor) throws RobotException {

        final LightSensor lightSensor = api.getLightSensor(EV3.InputPort._1);
        try {
            Future<LightSensor.Color> reflectedColor = lightSensor.getColor();

            if (isColor) {
                if (reflectedColor.get() != color)
                    throw new RobotException("It seems like the Robot has failed this operation. \n" +
                            "Please, make sure you are correctly following the rules of this specific operation or that the " +
                            "map format is correct and then try to start this operation again.");
            }

            else {
                if (reflectedColor.get() == color)
                    throw new RobotException("It seems like the Robot has failed this operation. \n" +
                            "Please, make sure you are correctly following the rules of this specific operation or that the " +
                            "map format is correct and then try to start this operation again.");
            }

        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new RobotException("Something went wrong. Please try this operation again.");
        }
    }

    public static LightSensor.Color getReflectedColor(EV3.Api api) throws RobotException {
        try {
            final LightSensor lightSensor = api.getLightSensor(EV3.InputPort._1);
            Future<LightSensor.Color> reflectedColor = lightSensor.getColor();
            return reflectedColor.get();
        } catch (IOException | ExecutionException | InterruptedException e) {
            e.printStackTrace();
            throw new RobotException("Something went wrong. Please try this operation again.");
        }
    }

    public static short getReflectedIntensity(EV3.Api api) throws RobotException {
        try {
            final LightSensor lightSensor = api.getLightSensor(EV3.InputPort._1);
            Future<Short> reflectedIntensity = lightSensor.getReflected();
            return reflectedIntensity.get();
        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new RobotException("Something went wrong. Please try this operation again.");
        }
    }

    //TODO Far muovere il robot a destra per prendere il colore prima dell'operazione di scelta del colore.
    public static short getBackgroundColorIntensity(EV3.Api api) throws RobotException {

        robotRotation (api, 90);

        smallMovement(api, ManualActivity.Direction.FORWARD, 100);

        checkColor(api, LightSensor.Color.WHITE, true);
        short backgroundColor = getReflectedIntensity(api);

        smallMovement(api, ManualActivity.Direction.BACKWARD, -100);

        robotRotation (api, -90);

        return backgroundColor;
    }

    public static void robotRotation(EV3.Api api, float angle) throws RobotException {
        try {
            TachoMotor leftTachoMotor = api.getTachoMotor(EV3.OutputPort.B);
            TachoMotor rightTachoMotor = api.getTachoMotor(EV3.OutputPort.C);
            Motor rightMotor = null;
            Motor leftMotor = null;

            GyroSensor gyroSensor = api.getGyroSensor(EV3.InputPort._3);
            Future<Float> futureAngle = gyroSensor.getAngle();

            if (angle < 0) {
                float currentAngle = futureAngle.get();
                while (futureAngle.get() > currentAngle + angle) {
                    rightMotor = new Motor(rightTachoMotor, 5, 50);
                    leftMotor = new Motor(leftTachoMotor, -5, 50);
                    rightMotor.start();
                    leftMotor.start();
                    futureAngle = gyroSensor.getAngle();
                }
            }

            else {
                float currentAngle = futureAngle.get();
                while (futureAngle.get() < currentAngle + angle) {
                    rightMotor = new Motor(rightTachoMotor, -5, 50);
                    leftMotor = new Motor(leftTachoMotor, 5, 50);
                    rightMotor.start();
                    leftMotor.start();
                    futureAngle = gyroSensor.getAngle();
                }
            }

            rightTachoMotor.stop();
            leftTachoMotor.stop();

        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new RobotException("Something went wrong. Please try this operation again.");
        }
    }

    public static void moveToTrack(EV3.Api api) throws RobotException {

        robotRotation (api, 90);

        smallMovement(api, ManualActivity.Direction.FORWARD, 50);

        checkColor(api, LightSensor.Color.WHITE, true);
        short backgroundColor = getReflectedIntensity(api);

        smallMovement(api, ManualActivity.Direction.BACKWARD, -50);

        robotRotation (api, -90);
        checkColor(api, LightSensor.Color.WHITE, false);
    }

    public static void smallMovement(EV3.Api api, ManualActivity.Direction direction, int newPositionOffset) throws RobotException {
        try {
            TachoMotor leftTachoMotor = api.getTachoMotor(EV3.OutputPort.B);
            TachoMotor rightTachoMotor = api.getTachoMotor(EV3.OutputPort.C);

            Motor rightMotor = null;
            Motor leftMotor = null;

            Future<Float> leftMotorPosition = leftTachoMotor.getPosition();
            Future<Float> rightMotorPosition = rightTachoMotor.getPosition();

            switch (direction) {
                case FORWARD: {
                    float leftStartingPosition = leftMotorPosition.get();
                    float rightStartingPosition = rightMotorPosition.get();
                    while (leftMotorPosition.get() < leftStartingPosition + newPositionOffset ||
                            rightMotorPosition.get() < rightStartingPosition + newPositionOffset) {
                        rightMotor = new Motor(rightTachoMotor, 5, 50);
                        leftMotor = new Motor(leftTachoMotor, 5, 50);
                        rightMotor.start();
                        leftMotor.start();
                        leftMotorPosition = leftTachoMotor.getPosition();
                        rightMotorPosition = rightTachoMotor.getPosition();
                    }
                    leftTachoMotor.stop();
                    rightTachoMotor.stop();
                    break;
                }
                case BACKWARD: {
                    float leftStartingPosition = leftMotorPosition.get();
                    float rightStartingPosition = rightMotorPosition.get();
                    while (leftMotorPosition.get() > leftStartingPosition + newPositionOffset ||
                            rightMotorPosition.get() > rightStartingPosition + newPositionOffset) {
                        rightMotor = new Motor(rightTachoMotor, -5, 50);
                        leftMotor = new Motor(leftTachoMotor, -5, 50);
                        rightMotor.start();
                        leftMotor.start();
                        leftMotorPosition = leftTachoMotor.getPosition();
                        rightMotorPosition = rightTachoMotor.getPosition();
                    }
                    leftTachoMotor.stop();
                    rightTachoMotor.stop();
                    break;
                }
                default:
                    throw new RobotException("Something went wrong. Please try this operation again.");
            }
        } catch (InterruptedException | ExecutionException | IOException e) {
            e.printStackTrace();
        }
    }
}
