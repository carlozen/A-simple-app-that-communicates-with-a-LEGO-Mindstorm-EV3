package it.unive.dais.legodroid.ourUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import it.unive.dais.legodroid.code.ManualActivity;
import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.plugs.LightSensor;
import it.unive.dais.legodroid.lib.plugs.UltrasonicSensor;

import static java.lang.Math.abs;

public final class RobotOperation {

    public static LightSensor.Color followLine (EV3.Api api, LightSensorMonitor lightSensorMonitor,
                                                ManualActivity.Direction direction,
                                                LightSensor.Color lineColor, short lineReflectedColor,
                                                short backgroundReflectedColor, ArrayList<LightSensor.Color> colorsList)
    throws RobotException{



        LightSensorColor lightSensorColor;

        try {

            Thread right, left;

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
            int derivative;
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

            int turningValue;
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
                        Thread.sleep(15);
                    }
                } else {
                    rightMotor.setPower(-power);
                    while (gyroSensorAngle.getAngleNow() < currentAngle + angle) {
                        Thread.sleep(15);
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
    }

   /* public static void smallMovement(EV3.Api api, ManualActivity.Direction direction, int newPositionOffset) throws RobotException {
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
    }*/

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

            leftMotor.brake();
            rightMotor.brake();

            right.interrupt();
            left.interrupt();

        } catch (IOException | RobotException e) {
            e.printStackTrace();
        }

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
            Thread.sleep(180);
            leftMotor.setPower(0);
            rightMotor.setPower(0);

        }

        leftMotor.brake();
        rightMotor.brake();

        right.interrupt();
        left.interrupt();
    }

    public static void reachOriginFromPos(EV3.Api api, LightSensorMonitor lightSensorMonitor, int numberPosStart, ArrayList<LightSensor.Color> colorsToCheck, LightSensor.Color colorStop, short blackLineIntensity, short backgroundColorIntensity) throws InterruptedException, IOException, RobotException {
        returnToBeginOfTrack(api, lightSensorMonitor, numberPosStart, colorsToCheck, blackLineIntensity, backgroundColorIntensity);

        LightSensor.Color colorFound = null;

        while(colorFound != colorStop){
            colorFound = RobotOperation.followLine(api,
                    lightSensorMonitor,
                    ManualActivity.Direction.BACKWARD,
                    LightSensor.Color.BLACK,
                    blackLineIntensity,
                    backgroundColorIntensity,
                    colorsToCheck
            );

            if(colorFound != LightSensor.Color.RED){
                smallMovementUntilColor(api, lightSensorMonitor, LightSensor.Color.BLACK, ManualActivity.Direction.BACKWARD, ManualActivity.Direction.RIGHT);
            }
        }
        smallMovementUntilColor(api, lightSensorMonitor, LightSensor.Color.BLACK, ManualActivity.Direction.FORWARD, ManualActivity.Direction.LEFT);
    }

    public static void reachPosFromOrigin(EV3.Api api, LightSensorMonitor lightSensorMonitor, int trackNumber, int numberOfPos, ArrayList<LightSensor.Color> colorsToCheck, short blackLineIntensity, short backgroundColorIntensity) throws RobotException, IOException, InterruptedException {

        RobotOperation.checkColor(api, lightSensorMonitor, LightSensor.Color.BLACK, true);

        reachBeginOfTrack(api, lightSensorMonitor, trackNumber, colorsToCheck, blackLineIntensity, backgroundColorIntensity);

        reachPosFromBeginOfTrack(api, lightSensorMonitor, numberOfPos, colorsToCheck, blackLineIntensity, backgroundColorIntensity);

    }

    public static void reachPosFromPos(EV3.Api api, LightSensorMonitor lightSensorMonitor, VirtualMap virtualMap, int trackNumberStart, int numberStartPos, int trackNumberEnd, int numberEndPos, ArrayList<LightSensor.Color> colorsToCheck, short blackLineIntensity, short backgroundColorIntensity) throws InterruptedException, IOException, RobotException {
        ArrayList<VirtualMap.MapTrack> mapTrackList = virtualMap.getMapTrackList();

        int deltaPos = 0;

        if(trackNumberEnd > mapTrackList.size() || trackNumberStart > mapTrackList.size()){
            throw new RobotException("Track number do not exist.");
        }

        if(trackNumberEnd == trackNumberStart){
            reachAnotherPosOfTrack(api, lightSensorMonitor, numberStartPos, numberEndPos, colorsToCheck, blackLineIntensity, backgroundColorIntensity);
        } else {
            returnToBeginOfTrack(api, lightSensorMonitor, trackNumberStart, colorsToCheck, blackLineIntensity, backgroundColorIntensity);
            if (trackNumberEnd > trackNumberStart) {
                reachPosFromOrigin(api, lightSensorMonitor, trackNumberEnd, numberEndPos, colorsToCheck, blackLineIntensity, backgroundColorIntensity);
            } else {

                while(deltaPos != trackNumberStart - trackNumberEnd){
                    RobotOperation.followLine(api,
                            lightSensorMonitor,
                            ManualActivity.Direction.BACKWARD,
                            LightSensor.Color.BLACK,
                            blackLineIntensity,
                            backgroundColorIntensity,
                            colorsToCheck
                    );

                    deltaPos++;

                    if(deltaPos != trackNumberStart - trackNumberEnd){
                        smallMovementUntilColor(api, lightSensorMonitor, LightSensor.Color.BLACK, ManualActivity.Direction.BACKWARD, ManualActivity.Direction.RIGHT);
                    }
                }
                reachPosFromBeginOfTrack(api, lightSensorMonitor, numberEndPos, colorsToCheck, blackLineIntensity, backgroundColorIntensity);

            }

        }

    }

    private static void reachAnotherPosOfTrack(EV3.Api api, LightSensorMonitor lightSensorMonitor, int numberStartPos, int numberEndPos, ArrayList<LightSensor.Color> colorsToCheck, short blackLineIntensity, short backgroundColorIntensity) throws RobotException, IOException, InterruptedException {
        if(numberEndPos > numberStartPos){
            int positions = numberEndPos - numberStartPos;

            while(positions > 0){
                RobotOperation.followLine(api,
                        lightSensorMonitor,
                        ManualActivity.Direction.FORWARD,
                        LightSensor.Color.BLACK,
                        blackLineIntensity,
                        backgroundColorIntensity,
                        colorsToCheck
                );


                positions --;
                if(positions > 0) {
                    smallMovementUntilColor(api, lightSensorMonitor, LightSensor.Color.BLACK, ManualActivity.Direction.FORWARD, ManualActivity.Direction.LEFT);
                }
            }
        } else {
            if(numberEndPos < numberStartPos){
                int position = numberStartPos - numberEndPos;

                while(position > 0){
                    LightSensor.Color colorFound = RobotOperation.followLine(api,
                            lightSensorMonitor,
                            ManualActivity.Direction.BACKWARD,
                            LightSensor.Color.BLACK,
                            blackLineIntensity,
                            backgroundColorIntensity,
                            colorsToCheck
                    );


                    position--;
                    if(position > 0){
                        smallMovementUntilColor(api, lightSensorMonitor, LightSensor.Color.BLACK, ManualActivity.Direction.BACKWARD, ManualActivity.Direction.RIGHT);
                    }

                }
            }
        }
    }

    public static void reachEndFromPos(EV3.Api api, LightSensorMonitor lightSensorMonitor, int numberOfPos, ArrayList<LightSensor.Color> colorsToCheck, LightSensor.Color colorStop, short blackLineIntensity, short backgroundColorIntensity) throws RobotException, IOException, InterruptedException {

        RobotOperation.checkColor(api, lightSensorMonitor, LightSensor.Color.BLACK, true);

        returnToBeginOfTrack(api, lightSensorMonitor, numberOfPos, colorsToCheck, blackLineIntensity, backgroundColorIntensity);

        reachEndFromBeginOfTrack(api, lightSensorMonitor, colorsToCheck, colorStop, blackLineIntensity, backgroundColorIntensity);
    }

    private static void reachEndFromBeginOfTrack(EV3.Api api, LightSensorMonitor lightSensorMonitor, ArrayList<LightSensor.Color> colorsToCheck, LightSensor.Color colorStop, short blackLineIntensity, short backgroundColorIntensity) throws InterruptedException, IOException, RobotException {

        LightSensor.Color colorFound = null;

        while(colorFound != colorStop){
            colorFound = RobotOperation.followLine(api,
                    lightSensorMonitor,
                    ManualActivity.Direction.FORWARD,
                    LightSensor.Color.BLACK,
                    blackLineIntensity,
                    backgroundColorIntensity,
                    colorsToCheck
            );

            if(colorFound != colorStop){
                smallMovementUntilColor(api, lightSensorMonitor, LightSensor.Color.BLACK, ManualActivity.Direction.FORWARD, ManualActivity.Direction.LEFT);
            }
        }
    }

    private static void returnToBeginOfTrack(EV3.Api api, LightSensorMonitor lightSensorMonitor, int numberOfPos, ArrayList<LightSensor.Color> colorsToCheck, short blackLineIntensity, short backgroundColorIntensity) throws RobotException, IOException, InterruptedException {

        int position = numberOfPos;

        while(position >= 0){

            RobotOperation.followLine(api,
                    lightSensorMonitor,
                    ManualActivity.Direction.BACKWARD,
                    LightSensor.Color.BLACK,
                    blackLineIntensity,
                    backgroundColorIntensity,
                    colorsToCheck
            );

            if (position >= 1) {
                RobotOperation.smallMovementUntilColor(api, lightSensorMonitor, LightSensor.Color.BLACK, ManualActivity.Direction.BACKWARD, ManualActivity.Direction.RIGHT);
            } else {
                RobotOperation.turnUntilColor(api, lightSensorMonitor, LightSensor.Color.BLACK, VirtualMap.Wheel.RIGHT, ManualActivity.Direction.BACKWARD);
            }
            position--;


        }
    }

    private static void reachPosFromBeginOfTrack(EV3.Api api, LightSensorMonitor lightSensorMonitor, int numberOfPos, ArrayList<LightSensor.Color> colorsToCheck, short blackLineIntensity, short backgroundColorIntensity) throws RobotException, IOException, InterruptedException {

        RobotOperation.robotRotation(api, -70, VirtualMap.Wheel.RIGHT);
        RobotOperation.turnUntilColor(api, lightSensorMonitor, LightSensor.Color.BLACK, VirtualMap.Wheel.LEFT, ManualActivity.Direction.FORWARD);

        int position = 0;

        while(position != numberOfPos) {

            RobotOperation.followLine(api,
                    lightSensorMonitor,
                    ManualActivity.Direction.FORWARD,
                    LightSensor.Color.BLACK,
                    blackLineIntensity,
                    backgroundColorIntensity,
                    colorsToCheck
            );


            position ++;
            if(position != numberOfPos) {
                smallMovementUntilColor(api, lightSensorMonitor, LightSensor.Color.BLACK, ManualActivity.Direction.FORWARD, ManualActivity.Direction.LEFT);
            }

        }
    }

    private static void reachBeginOfTrack(EV3.Api api, LightSensorMonitor lightSensorMonitor, int trackNumber, ArrayList<LightSensor.Color> colorsToCheck, short blackLineIntensity, short backgroundColorIntensity) throws RobotException, IOException, InterruptedException {

        int i =-1;

        while (i != trackNumber){
            RobotOperation.followLine(api,
                    lightSensorMonitor,
                    ManualActivity.Direction.FORWARD,
                    LightSensor.Color.BLACK,
                    blackLineIntensity,
                    backgroundColorIntensity,
                    colorsToCheck
            );

            i++;

            if(i != trackNumber){
                smallMovementUntilColor(api, lightSensorMonitor, LightSensor.Color.BLACK, ManualActivity.Direction.FORWARD, ManualActivity.Direction.LEFT);
            }
        }
    }


    //TODO MAKE PRIVATE IN FUTURE
    public static void pickUpObject (EV3.Api api, AsyncRobotTask robotTask) throws  RobotException{

        try {
            final float MAX_OBJ_DISTANCE = 15;
            final int POWER = 10;

            UltrasonicSensor ultrasonicSensor = api.getUltrasonicSensor(EV3.InputPort._4);
            Future<Float> futureDistance;
            float currentDistance = 0;


            GyroSensorAngle gyroSensorAngle = new GyroSensorAngle(api, EV3.InputPort._3);
            Thread gyroSensor = new Thread(gyroSensorAngle);
            final float STARTING_ANGLE = gyroSensorAngle.getAngleNow();

            Motor leftMotor = new Motor(api, EV3.OutputPort.B);
            Motor rightMotor = new Motor(api, EV3.OutputPort.C);
            Thread left = new Thread(leftMotor);
            Thread right = new Thread(rightMotor);


            boolean isObjectFound = false;
            float objectDistance = 0;

            left.start();
            right.start();
            gyroSensor.start();
            while (!isObjectFound && gyroSensorAngle.getAngleNow() > STARTING_ANGLE - 180) {
                futureDistance = ultrasonicSensor.getDistance();
                currentDistance = futureDistance.get();
                if (currentDistance <= MAX_OBJ_DISTANCE) {
                    isObjectFound = true;
                    objectDistance = currentDistance;
                    leftMotor.brake();
                    rightMotor.brake();
                    leftMotor.setPower(0);
                    rightMotor.setPower(0);
                }
                leftMotor.setPower(-POWER);
                rightMotor.setPower(POWER);
            }

            leftMotor.brake();
            rightMotor.brake();

            leftMotor.setPower(0);
            rightMotor.setPower(0);

            leftMotor.start();
            rightMotor.start();

            if (!isObjectFound) {
                left.interrupt();
                right.interrupt();
                gyroSensor.interrupt();
                throw new RobotException("Object not found.");
            }

            final float ANGLE_MIN = gyroSensorAngle.getAngleNow();

            while (currentDistance <= objectDistance) {
                futureDistance = ultrasonicSensor.getDistance();
                currentDistance = futureDistance.get();
                leftMotor.setPower(-POWER);
                rightMotor.setPower(POWER);
            }
            leftMotor.brake();
            rightMotor.brake();

            leftMotor.setPower(0);
            rightMotor.setPower(0);

            robotTask.moveToTrack(2);

            leftMotor.start();
            rightMotor.start();

            final float ANGLE_MAX = gyroSensorAngle.getAngleNow();
            float ANGLE_MED;
            if (currentDistance >= 10)
                ANGLE_MED = (ANGLE_MAX + ANGLE_MIN)/2 + (currentDistance/5);
            else
                ANGLE_MED = (ANGLE_MAX + ANGLE_MIN)/2;

            while (gyroSensorAngle.getAngleNow() < ANGLE_MED) {
                leftMotor.setPower(POWER);
                rightMotor.setPower(-POWER);
            }

            leftMotor.brake();
            rightMotor.brake();

            leftMotor.setPower(0);
            rightMotor.setPower(0);

            leftMotor.start();
            rightMotor.start();

            Future<Float> futureMotorPositionLeft = leftMotor.getPosition();
            final float START_POS_LEFT = futureMotorPositionLeft.get();

            Future<Float> futureMotorPositionRight = leftMotor.getPosition();
            final float START_POS_RIGHT = futureMotorPositionRight.get();

            robotTask.moveToPositionOnTrack(1,3);

            while (currentDistance > 5) {
                futureDistance = ultrasonicSensor.getDistance();
                currentDistance = futureDistance.get();

                leftMotor.setPower(POWER);
                rightMotor.setPower(POWER);
            }

            leftMotor.setPower(0);
            rightMotor.setPower(0);

            Grabber grabber = new Grabber(api, EV3.OutputPort.A);
            Grabber.moveDownGrabber(api, grabber);

            futureMotorPositionLeft = leftMotor.getPosition();
            futureMotorPositionRight = rightMotor.getPosition();
            float currentPositionLeft = futureMotorPositionLeft.get();
            float currentPositionRight = futureMotorPositionRight.get();

            while (currentPositionLeft > START_POS_LEFT && currentPositionRight > START_POS_RIGHT) {
                futureMotorPositionLeft = leftMotor.getPosition();
                futureMotorPositionRight = rightMotor.getPosition();
                currentPositionLeft = futureMotorPositionLeft.get();
                currentPositionRight = futureMotorPositionRight.get();
                leftMotor.setPower(-POWER);
                rightMotor.setPower(-POWER);
            }

            leftMotor.setPower(0);
            rightMotor.setPower(0);

            Grabber.moveUpGrabber(api, grabber);

            left.interrupt();
            right.interrupt();
            gyroSensor.interrupt();



        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new RobotException("Something went wrong. Please try this operation again.");
        }
    }


    public static void addObject (EV3.Api api, AsyncRobotTask asyncRobotTask,
                                  PositionButton referencedButton) throws RobotException {
        try {
            ArrayList<LightSensor.Color> colorsToCheck = new ArrayList<>();
            colorsToCheck.add(LightSensor.Color.RED);
            colorsToCheck.add(LightSensor.Color.BROWN);
            colorsToCheck.add(LightSensor.Color.YELLOW);
            colorsToCheck.add(LightSensor.Color.GREEN);

            short blackLineIntensity = getReflectedIntensity(api, new LightSensorMonitor());
            short backgroundIntensity = getBackgroundColorIntensity(api, new LightSensorMonitor());

            reachPosFromOrigin(api, new LightSensorMonitor(), referencedButton.getTrackNumber(),
                    referencedButton.getPositionNumber(),
                    colorsToCheck, blackLineIntensity, backgroundIntensity);

            asyncRobotTask.moveToPositionOnTrack(referencedButton.getTrackNumber(), referencedButton.getPositionNumber());

            referencedButton.changeOccupiedState();

            asyncRobotTask.moveToBeginning();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            throw new RobotException("Something went wrong, please try again");
        }
    }

    //PositionButton hanno un metodo changeOccupiedState per cambiare lo stato occupato/libero.
    //AsyncTask chiama i metodi per muovere il robot sulla mappa virtuale,
    // destinationButton è il bottone dove l'oggetto deve essere spostato e ha metodi per prendere numero del track e numero della posizione;
    //buttonToMoveObjFrom è il bottone da cui muovere l'oggetto.

    public static void moveObject(EV3.Api api, AsyncRobotTask asyncRobotTask,
                                  PositionButton destinationButton, PositionButton buttonToMoveObjFrom) throws RobotException {

    }

    public static void removeObject(EV3.Api api, AsyncRobotTask asyncRobotTask,
                                    PositionButton referencedButton) throws RobotException {

    }
}
