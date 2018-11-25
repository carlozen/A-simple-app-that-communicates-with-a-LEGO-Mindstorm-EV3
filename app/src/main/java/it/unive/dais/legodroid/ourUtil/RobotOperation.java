package it.unive.dais.legodroid.ourUtil;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import it.unive.dais.legodroid.code.ManualActivity;
import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.plugs.GyroSensor;
import it.unive.dais.legodroid.lib.plugs.LightSensor;
import it.unive.dais.legodroid.lib.plugs.TachoMotor;

//TODO
public final class RobotOperation {

    //TODO ENUM Specifico per impedire direzioni diverse da FORWARD e BACKWARD
    public static void followLine (EV3.Api api, ManualActivity.Direction direction, LightSensor.Color lineColor, short lineReflectedColor, short backgroundReflectedColor) {
        try {
            TachoMotor leftTachoMotor = api.getTachoMotor(EV3.OutputPort.B);
            TachoMotor rightTachoMotor = api.getTachoMotor(EV3.OutputPort.C);
            Motor rightMotor = null;
            Motor leftMotor = null;

            //TODO CHECK CORRECT INPUTPORT

            final LightSensor lightSensor = api.getLightSensor(EV3.InputPort._1);

            int constantP = 1000;
            int constantI = 0;
            int constantD = 0;
            int offset = (lineReflectedColor + backgroundReflectedColor) / 2;
            int maxSpeed;
            int integral = 0;
            int derivative = 0;
            int lastError = 0;
            Future<LightSensor.Color> reflectedColor = lightSensor.getColor();

            if (direction == ManualActivity.Direction.FORWARD)
                maxSpeed = 30;
            else if (direction == ManualActivity.Direction.BACKWARD)
                maxSpeed = -30;
            //TODO To be changed to not throw exceptions
            else throw new IllegalArgumentException();

            while (reflectedColor.get() == LightSensor.Color.WHITE || reflectedColor.get() == lineColor) {
                Future<Short> reflectedIntensity = lightSensor.getReflected();
                int error = reflectedIntensity.get() - offset;
                integral = integral + error;
                derivative = error - lastError;
                int turningValue = (constantP * error + constantI * integral + constantD * derivative)/100;

                rightMotor = new Motor(rightTachoMotor, maxSpeed + turningValue, 50);

                leftMotor = new Motor(leftTachoMotor, maxSpeed - turningValue, 50);

                rightMotor.start();
                leftMotor.start();
                reflectedColor = lightSensor.getColor();
            }
            leftTachoMotor.stop();
            rightTachoMotor.stop();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void checkColor(EV3.Api api, LightSensor.Color color, boolean isColor) throws RobotException {

        //TODO CHECK CORRECT INPUTPORT

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

            //TODO CHECK CORRECT INPUTPORT

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

            //TODO CHECK CORRECT INPUTPORT

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

        smallMovement(api, ManualActivity.Direction.FORWARD, 20);

        checkColor(api, LightSensor.Color.WHITE, true);
        short backgroundColor = getReflectedIntensity(api);

        smallMovement(api, ManualActivity.Direction.BACKWARD, -20);

        robotRotation (api, -90);

        return backgroundColor;
    }

    public static void robotRotation(EV3.Api api, float angle) throws RobotException {
        try {
            TachoMotor leftTachoMotor = api.getTachoMotor(EV3.OutputPort.B);
            TachoMotor rightTachoMotor = api.getTachoMotor(EV3.OutputPort.C);
            Motor rightMotor = null;
            Motor leftMotor = null;

            //TODO CHECK INPUT PORT
            GyroSensor gyroSensor = api.getGyroSensor(EV3.InputPort._2);
            Future<Float> startingAngle = gyroSensor.getAngle();

            if (angle < 0) {
                while (startingAngle.get() > angle) {
                    rightMotor = new Motor(rightTachoMotor, 30, 50);
                    leftMotor = new Motor(leftTachoMotor, -30, 50);
                    rightMotor.start();
                    leftMotor.start();
                    startingAngle = gyroSensor.getAngle();
                }
            }

            else {
                while (startingAngle.get() < angle) {
                    rightMotor = new Motor(rightTachoMotor, -30, 50);
                    leftMotor = new Motor(leftTachoMotor, 30, 50);
                    rightMotor.start();
                    leftMotor.start();
                    startingAngle = gyroSensor.getAngle();
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
        try {
            TachoMotor leftTachoMotor = api.getTachoMotor(EV3.OutputPort.B);
            TachoMotor rightTachoMotor = api.getTachoMotor(EV3.OutputPort.C);

            Motor rightMotor = null;
            Motor leftMotor = null;

            GyroSensor gyroSensor = api.getGyroSensor(EV3.InputPort._2);
            Future<Float> angle = gyroSensor.getAngle();


            while (angle.get() < 90) {
                rightMotor = new Motor(rightTachoMotor, 10, 50);
                leftMotor = new Motor(leftTachoMotor, 40, 50);
                rightMotor.start();
                leftMotor.start();
                angle = gyroSensor.getAngle();
            }
            rightTachoMotor.stop();
            leftTachoMotor.stop();
            checkColor(api, LightSensor.Color.WHITE, false);

        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new RobotException("Something went wrong. Please try this operation again.");
        }

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
                    while (leftMotorPosition.get() < newPositionOffset || rightMotorPosition.get() < newPositionOffset) {
                        rightMotor = new Motor(rightTachoMotor, 30, 50);
                        leftMotor = new Motor(leftTachoMotor, 30, 50);
                        rightMotor.start();
                        leftMotor.start();
                        leftMotorPosition = leftTachoMotor.getPosition();
                        rightMotorPosition = rightTachoMotor.getPosition();
                    }
                    leftTachoMotor.stop();
                    rightTachoMotor.stop();
                }
                case BACKWARD: {
                    while (leftMotorPosition.get() > - newPositionOffset || rightMotorPosition.get() > - newPositionOffset) {
                        rightMotor = new Motor(rightTachoMotor, -30, 50);
                        leftMotor = new Motor(leftTachoMotor, -30, 50);
                        rightMotor.start();
                        leftMotor.start();
                        leftMotorPosition = leftTachoMotor.getPosition();
                        rightMotorPosition = rightTachoMotor.getPosition();
                    }
                    leftTachoMotor.stop();
                    rightTachoMotor.stop();
                }
                default:
                    throw new RobotException("Something went wrong. Please try this operation again.");
            }
        } catch (InterruptedException | ExecutionException | IOException e) {
            e.printStackTrace();
        }
    }
}
