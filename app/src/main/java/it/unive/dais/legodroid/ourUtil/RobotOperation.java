package it.unive.dais.legodroid.ourUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import it.unive.dais.legodroid.code.ManualActivity;
import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.plugs.LightSensor;

import static java.lang.Math.abs;

public final class RobotOperation {

    public static LightSensor.Color followLine (EV3.Api api, LightSensorMonitor lightSensorMonitor,
                                                ManualActivity.Direction direction,
                                                LightSensor.Color lineColor, short lineReflectedColor,
                                                short backgroundReflectedColor, ArrayList<LightSensor.Color> colorsList)
    throws RobotException{



        LightSensorColor lightSensorColor;

        LightSensor.Color colorRes;

        try {

            Thread right, left;

            Motor rightMotor = new Motor(api, EV3.OutputPort.C,0,0);
            Motor leftMotor = new Motor(api, EV3.OutputPort.B,0,0);

            right = new Thread(rightMotor);
            left = new Thread(leftMotor);

            right.start();
            left.start();

            Short reflectedIntensity;

            double P = 0.5;
            double I = 0.0005;
            double D = 0.01;
            int OFFSET = 50;

            int maxSpeed;
            int integral = 0;
            int derivative;
            int lastError = 0;

            boolean exception = false;

            maxSpeed = 3;

            LightSensorIntensity lightSensorIntensity = new LightSensorIntensity(api, EV3.InputPort._1, lightSensorMonitor);
            Thread lightIntensity = new Thread(lightSensorIntensity);
            lightIntensity.start();

            lightSensorColor = new LightSensorColor(api, EV3.InputPort._1, colorsList, lightSensorMonitor);
            Thread lightColor = new Thread(lightSensorColor);
            lightColor.start();

            /*GyroSensorAngle gyroSensorAngle = new GyroSensorAngle(api);
            float angleStart = gyroSensorAngle.getAngleNow();*/

            int turningValue;
            int leftPower, rightPower, difference;

            Boolean flag = false;

            while (!flag) {

                reflectedIntensity = null;
                while(reflectedIntensity ==  null) {
                    try {
                        reflectedIntensity = lightSensorIntensity.getReflectedNow();
                    } catch (ExecutionException e) {
                        try {
                            rightMotor.brake();
                            leftMotor.brake();
                            rightMotor.setPower(0);
                            leftMotor.setPower(0);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                            throw new RobotException("Something went wrong. Please try this operation again.");
                        }
                        reflectedIntensity = null;
                        lightSensorMonitor.release();
                        e.printStackTrace();

                    }
                }

                int error = (normalizeOnPercent(reflectedIntensity, lineReflectedColor,
                        backgroundReflectedColor) - OFFSET) /10;
                if (error * lastError > 0)
                    integral = integral + error;
                else
                    integral = 0;
                derivative = error - lastError;
                lastError = error;

                if(direction == ManualActivity.Direction.FORWARD) {
                    turningValue = (int) (P*error + I*integral + D*derivative);

                    leftPower = maxSpeed - turningValue;
                    rightPower = maxSpeed + turningValue;

                    difference = 10 - abs(leftPower - rightPower);
                    if(difference < 3)
                        difference = 3;

                    rightMotor.setPower(rightPower * difference);
                    leftMotor.setPower(leftPower * difference);

                } /*else {

                    P = 0.1; //TODO: to be tested
                    D = 0.3;
                    maxSpeed = 3;

                    Future<Float> futureAngleNow = gyroSensorAngle.getAngle();

                    turningValue = (int) (P*error + I*integral + D*derivative);

                    leftPower = maxSpeed - turningValue;
                    rightPower = maxSpeed + turningValue;

                    difference = 10 - abs(leftPower - rightPower);
                    if(difference < 3)
                        difference = 3;
                    difference = 3;

                    if(leftPower > 10)
                        leftPower = 10;
                    if(leftPower < 0)
                        leftPower = -leftPower;
                    if(leftPower < 3)
                        leftPower = 3;
                    if(rightPower > 10)
                        rightPower = 10;
                    if(rightPower < 0)
                        rightPower = -rightPower;
                    if(rightPower < 3)
                        rightPower = 3;

                    if(rightPower == 0 && leftPower == 0){
                        leftPower = 10;
                        rightPower = 10;
                    }

                    Float angleNow = null;
                    while(angleNow == null){
                        try {
                            angleNow = futureAngleNow.get();
                        } catch (ExecutionException e){
                            angleNow = null;
                        }
                    }*/

                    /*if((angleNow - angleStart) < 0) {
                        rightMotor.setPower(rightPower * difference * (-1) * (int)(1.2 * abs(angleNow - angleStart)));
                        leftMotor.setPower(leftPower * difference * (-1));
                    } else {
                        if((angleNow - angleStart) > 0) {
                            rightMotor.setPower(rightPower * difference * (-1));
                            leftMotor.setPower(leftPower * difference * (-1) * (int) (1.2 * abs(angleNow - angleStart)));
                        } else {
                            rightMotor.setPower(rightPower * difference * (-1));
                            leftMotor.setPower(leftPower * difference * (-1));
                        }
                    }

                    //maxSpeed = 1;
                }*/

                Thread.sleep(25);

                flag = null;
                while(flag == null) {
                    try {
                        flag = lightSensorColor.getIsObstacleFound();
                    } catch (ExecutionException e) {
                        leftMotor.setPower(0);
                        rightMotor.setPower(0);
                        rightMotor.brake();
                        leftMotor.brake();
                        exception = true;
                        lightSensorMonitor.release();
                        e.printStackTrace();
                        flag = null;
                    }
                }

                if(!exception) {
                    rightMotor.setPower(0);
                    leftMotor.setPower(0);
                } else{
                    exception = false;
                }
            }

            rightMotor.brake();
            leftMotor.brake();

            //colorRes = lightSensorColor.getColorNow();

            right.interrupt();
            left.interrupt();

            lightColor.interrupt();
            lightIntensity.interrupt();

            if (lightSensorColor.isHasExceptionOccurred()) {
                throw new RobotException("Something went wrong. Please try this operation again.");
            }

        }
        catch (IOException | InterruptedException e) {
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
            try {
                Motor rightMotor = new Motor(api, EV3.OutputPort.C);
                Motor leftMotor = new Motor(api, EV3.OutputPort.B);
                rightMotor.setPower(0);
                leftMotor.setPower(0);
                rightMotor.start();
                leftMotor.start();
                rightMotor.brake();
                leftMotor.brake();
            } catch (IOException ex) {
                ex.printStackTrace();
                throw new RobotException("Something went wrong. Please try this operation again.");
            }
            e.printStackTrace();
            throw new RobotException("Something went wrong. Please try this operation again.");
        }
        t.interrupt();
    }

    public static LightSensor.Color getReflectedColor(EV3.Api api, LightSensorMonitor lightSensorMonitor) throws RobotException {
        try {
            LightSensor.Color color = null;

            LightSensorColor lightSensorColor = new LightSensorColor(api,EV3.InputPort._1, lightSensorMonitor);
            Thread t = new Thread(lightSensorColor);
            t.start();

            while(color == null) {
                try {
                    color = lightSensorColor.getColorNow();
                } catch (ExecutionException e) {
                    color = null;
                    lightSensorMonitor.release();
                    try {
                        Motor rightMotor = new Motor(api, EV3.OutputPort.C);
                        Motor leftMotor = new Motor(api, EV3.OutputPort.B);
                        rightMotor.setPower(0);
                        leftMotor.setPower(0);
                        leftMotor.start();
                        rightMotor.start();
                        rightMotor.brake();
                        leftMotor.brake();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        throw new RobotException("Something went wrong. Please try this operation again.");
                    }
                }
            }



            lightSensorColor.killThread(t);

            return color;
        } catch (IOException | InterruptedException e) {
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
            //t.interrupt();

            return res;
        } catch (IOException | InterruptedException | ExecutionException e) {
            try {
                Motor rightMotor = new Motor(api, EV3.OutputPort.C);
                Motor leftMotor = new Motor(api, EV3.OutputPort.B);
                rightMotor.setPower(0);
                leftMotor.setPower(0);
                leftMotor.start();
                rightMotor.start();
                rightMotor.brake();
                leftMotor.brake();
            } catch (IOException ex) {
                ex.printStackTrace();
                throw new RobotException("Something went wrong. Please try this operation again.");
            }
            e.printStackTrace();
            throw new RobotException("Something went wrong. Please try this operation again.");
        }
    }

    //TODO Far muovere il robot a destra per prendere il colore prima dell'operazione di scelta del colore.
    public static short getBackgroundColorIntensity(EV3.Api api, LightSensorMonitor lightSensorMonitor) throws RobotException {

        robotRotation (api, 30, VirtualMap.Wheel.LEFT);
        //turnUntilColor(api, lightSensorMonitor, LightSensor.Color.WHITE, VirtualMap.Wheel.LEFT, ManualActivity.Direction.FORWARD);

        //smallMovement(api, ManualActivity.Direction.FORWARD, 100);

        checkColor(api, lightSensorMonitor, LightSensor.Color.WHITE, true);
        short backgroundColor = getReflectedIntensity(api, lightSensorMonitor);

        //smallMovement(api, ManualActivity.Direction.BACKWARD, -100);

        //robotRotation (api, -30, VirtualMap.Wheel.LEFT);
        turnUntilColor(api, lightSensorMonitor, LightSensor.Color.BLACK, VirtualMap.Wheel.LEFT, ManualActivity.Direction.BACKWARD);


        return backgroundColor;
    }

    public static void robotRotation(EV3.Api api, float angle, VirtualMap.Wheel wheel) throws RobotException {
        try {
            int power = 20;

            GyroSensorAngle gyroSensorAngle = new GyroSensorAngle(api);
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
                        Thread.sleep(50);
                    }
                } else {
                    leftMotor.setPower(power);
                    while (gyroSensorAngle.getAngleNow() < currentAngle + angle) {
                        Thread.sleep(50);
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

            leftMotor.brake();
            rightMotor.brake();

            right.interrupt();
            left.interrupt();

        } catch (IOException | RobotException e) {
            e.printStackTrace();
        }

    }

    public static void smallMovementUntilBlackOrWhite(EV3.Api api, LightSensorMonitor lightSensorMonitor, ManualActivity.Direction direction, ManualActivity.Direction turn, int difference) throws RobotException, IOException, InterruptedException {
        int power = 20;
        int sign;

        if(direction == ManualActivity.Direction.FORWARD){
            sign = 1;
        } else {
            sign = -1;
        }

        ArrayList<LightSensor.Color> colorArray = new ArrayList<>();
        colorArray.add(LightSensor.Color.BLACK);
        colorArray.add(LightSensor.Color.WHITE);
        colorArray.add(LightSensor.Color.BLUE);

        Motor rightMotor = new Motor(api, EV3.OutputPort.C);
        Motor leftMotor = new Motor(api, EV3.OutputPort.B);

        Thread right = new Thread(rightMotor);
        Thread left = new Thread(leftMotor);

        right.start();
        left.start();

        while(!colorArray.contains(RobotOperation.getReflectedColor(api, lightSensorMonitor))) {

            if (turn == ManualActivity.Direction.LEFT) {
                leftMotor.setPower((power - difference) * sign);
                rightMotor.setPower(power * sign);
            } else {
                rightMotor.setPower((power - difference) * sign);
                leftMotor.setPower(power * sign);
            }

            Thread.sleep(60);
            leftMotor.setPower(0);
            rightMotor.setPower(0);

        }

        leftMotor.brake();
        rightMotor.brake();

        right.interrupt();
        left.interrupt();
    }

    public static void reachOriginFromPos(EV3.Api api, LightSensorMonitor lightSensorMonitor, ArrayList<LightSensor.Color> colorsToCheck, LightSensor.Color colorStop, short blackLineIntensity, short backgroundColorIntensity, AsyncRobotTask asyncRobotTask, PositionButton referencedButton) throws InterruptedException, IOException, RobotException {
        //returnToBeginOfTrack(api, lightSensorMonitor, colorsToCheck, blackLineIntensity, backgroundColorIntensity, asyncRobotTask, referencedButton);

        VirtualMap.backTrack(api, lightSensorMonitor, blackLineIntensity, backgroundColorIntensity, colorsToCheck, referencedButton.getPositionNumber());

        LightSensor.Color colorFound = null;

        int i = referencedButton.getTrackNumber();

        asyncRobotTask.moveToTrack(i);

        RobotOperation.robotRotation(api, 55, VirtualMap.Wheel.LEFT);
        RobotOperation.turnUntilColor(api, lightSensorMonitor, LightSensor.Color.BLACK, VirtualMap.Wheel.RIGHT, ManualActivity.Direction.FORWARD);

        while(colorFound != colorStop){
            colorFound = RobotOperation.followLine(api,
                    lightSensorMonitor,
                    ManualActivity.Direction.FORWARD,
                    LightSensor.Color.BLACK,
                    blackLineIntensity,
                    backgroundColorIntensity,
                    colorsToCheck
            );

            i--;

            asyncRobotTask.moveToTrack(i);

            if(colorFound != LightSensor.Color.RED){
                smallMovementUntilBlackOrWhite(api, lightSensorMonitor, ManualActivity.Direction.FORWARD, ManualActivity.Direction.RIGHT, 2);
            }
        }

        asyncRobotTask.moveToBeginning();

        //TODO GIRARE IL ROBOT OPPURE LASCIARE COSI'?
        //smallMovementUntilBlackOrWhite(api, lightSensorMonitor, ManualActivity.Direction.FORWARD, ManualActivity.Direction.RIGHT, 1);

    }

    public static void reachPosFromOrigin(EV3.Api api, LightSensorMonitor lightSensorMonitor, ArrayList<LightSensor.Color> colorsToCheck,
                                          short blackLineIntensity, short backgroundColorIntensity,
                                          AsyncRobotTask asyncRobotTask, PositionButton referencedButton) throws RobotException, IOException, InterruptedException {

        RobotOperation.checkColor(api, lightSensorMonitor, LightSensor.Color.BLACK, true);

        reachBeginOfTrack(api, lightSensorMonitor, referencedButton.getTrackNumber(), colorsToCheck, blackLineIntensity,
                backgroundColorIntensity, asyncRobotTask);

        RobotOperation.robotRotation(api, -90, VirtualMap.Wheel.RIGHT);
        RobotOperation.turnUntilColor(api, lightSensorMonitor, LightSensor.Color.BLACK, VirtualMap.Wheel.LEFT, ManualActivity.Direction.FORWARD);


        reachPosFromBeginOfTrack(api, lightSensorMonitor, colorsToCheck, blackLineIntensity,
                backgroundColorIntensity, asyncRobotTask, referencedButton);

    }

    public static ManualActivity.Direction reachPosFromPos(EV3.Api api, LightSensorMonitor lightSensorMonitor, ArrayList<LightSensor.Color> colorsToCheck, short blackLineIntensity, short backgroundColorIntensity, AsyncRobotTask asyncRobotTask, PositionButton buttonToMoveObjFrom, PositionButton destinationButton) throws InterruptedException, IOException, RobotException {
        int deltaPos = 0;

        ManualActivity.Direction sameTrackMovement = null;

        if(buttonToMoveObjFrom.getTrackNumber() == destinationButton.getTrackNumber()){
            sameTrackMovement = reachAnotherPosOfTrack(api, lightSensorMonitor, destinationButton.getPositionNumber(), colorsToCheck, blackLineIntensity, backgroundColorIntensity, asyncRobotTask, buttonToMoveObjFrom);
        } else {
/*
            RobotOperation.robotRotation(api, 55, VirtualMap.Wheel.LEFT);
            RobotOperation.turnUntilColor(api, lightSensorMonitor, LightSensor.Color.BLACK, VirtualMap.Wheel.RIGHT, ManualActivity.Direction.FORWARD);
*/
            VirtualMap.backTrack(api, lightSensorMonitor, blackLineIntensity, backgroundColorIntensity, colorsToCheck, buttonToMoveObjFrom.getPositionNumber());

            if (destinationButton.getTrackNumber() > buttonToMoveObjFrom.getTrackNumber()) {

                RobotOperation.robotRotation(api, -55, VirtualMap.Wheel.RIGHT);
                RobotOperation.turnUntilColor(api, lightSensorMonitor, LightSensor.Color.BLACK, VirtualMap.Wheel.LEFT, ManualActivity.Direction.FORWARD);

                PositionButton positionButton = new PositionButton(destinationButton.getContext(), destinationButton.getUIManager(), destinationButton.getTrackNumber() - buttonToMoveObjFrom.getTrackNumber(), destinationButton.getPositionNumber());

                reachPosFromOrigin(api, lightSensorMonitor, colorsToCheck, blackLineIntensity, backgroundColorIntensity, asyncRobotTask, positionButton);
            } else {

                RobotOperation.robotRotation(api, 55, VirtualMap.Wheel.LEFT);
                RobotOperation.turnUntilColor(api, lightSensorMonitor, LightSensor.Color.BLACK, VirtualMap.Wheel.RIGHT, ManualActivity.Direction.FORWARD);

                while(deltaPos != buttonToMoveObjFrom.getTrackNumber() - destinationButton.getTrackNumber()){
                    RobotOperation.followLine(api,
                            lightSensorMonitor,
                            ManualActivity.Direction.FORWARD,
                            LightSensor.Color.BLACK,
                            blackLineIntensity,
                            backgroundColorIntensity,
                            colorsToCheck
                    );

                    deltaPos++;

                    asyncRobotTask.moveToTrack(buttonToMoveObjFrom.getTrackNumber() - deltaPos);

                    if(deltaPos != buttonToMoveObjFrom.getTrackNumber() - destinationButton.getTrackNumber()){
                        smallMovementUntilBlackOrWhite(api, lightSensorMonitor, ManualActivity.Direction.BACKWARD, ManualActivity.Direction.RIGHT, 1);
                    }
                }

                RobotOperation.robotRotation(api, 90, VirtualMap.Wheel.LEFT);
                RobotOperation.turnUntilColor(api, lightSensorMonitor, LightSensor.Color.BLACK, VirtualMap.Wheel.RIGHT, ManualActivity.Direction.FORWARD);


                reachPosFromBeginOfTrack(api, lightSensorMonitor, colorsToCheck, blackLineIntensity, backgroundColorIntensity, asyncRobotTask, destinationButton);

            }

        }
        if(sameTrackMovement == null){
            return ManualActivity.Direction.FORWARD;
        } else {
            return sameTrackMovement;
        }

    }

    private static ManualActivity.Direction reachAnotherPosOfTrack(EV3.Api api, LightSensorMonitor lightSensorMonitor, int destinationPos, ArrayList<LightSensor.Color> colorsToCheck, short blackLineIntensity, short backgroundColorIntensity, AsyncRobotTask asyncRobotTask, PositionButton buttonToMoveObjFrom) throws RobotException, IOException, InterruptedException {
        if(destinationPos > buttonToMoveObjFrom.getPositionNumber()){
            int positions = destinationPos - buttonToMoveObjFrom.getPositionNumber();
/*
            robotRotation(api, -60, VirtualMap.Wheel.RIGHT);
            turnUntilColor(api, lightSensorMonitor, LightSensor.Color.BLACK, VirtualMap.Wheel.LEFT, ManualActivity.Direction.FORWARD);
*/
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

                asyncRobotTask.moveToPositionOnTrack(buttonToMoveObjFrom.getTrackNumber(), destinationPos - positions);

                if(positions > 0) {
                    smallMovementUntilBlackOrWhite(api, lightSensorMonitor, ManualActivity.Direction.FORWARD, ManualActivity.Direction.LEFT, 1);
                }
            }
            return ManualActivity.Direction.FORWARD;
        } else {
            if(destinationPos < buttonToMoveObjFrom.getPositionNumber()){
                int position = buttonToMoveObjFrom.getPositionNumber() - destinationPos;

                RobotOperation.turnUntilColor(api, lightSensorMonitor, LightSensor.Color.BLACK, VirtualMap.Wheel.LEFT, ManualActivity.Direction.BACKWARD);

                while(position > 0){
                    RobotOperation.followLine(api,
                            lightSensorMonitor,
                            ManualActivity.Direction.FORWARD,
                            LightSensor.Color.BLACK,
                            blackLineIntensity,
                            backgroundColorIntensity,
                            colorsToCheck
                    );

                    position--;

                    asyncRobotTask.moveToPositionOnTrack(buttonToMoveObjFrom.getTrackNumber(), destinationPos + position);

                    if(position > 0){
                        smallMovementUntilBlackOrWhite(api, lightSensorMonitor, ManualActivity.Direction.BACKWARD, ManualActivity.Direction.RIGHT, 1);
                    }

                }
            }
            return ManualActivity.Direction.BACKWARD;
        }
    }

    public static void reachEndFromPos(EV3.Api api, LightSensorMonitor lightSensorMonitor, ArrayList<LightSensor.Color> colorsToCheck, LightSensor.Color colorStop, short blackLineIntensity, short backgroundColorIntensity, AsyncRobotTask asyncRobotTask, PositionButton referencedButton) throws RobotException, IOException, InterruptedException {

        RobotOperation.checkColor(api, lightSensorMonitor, LightSensor.Color.BLACK, true);

        returnToBeginOfTrack(api, lightSensorMonitor, colorsToCheck, blackLineIntensity, backgroundColorIntensity, asyncRobotTask, referencedButton);

        reachEndFromBeginOfTrack(api, lightSensorMonitor, colorsToCheck, colorStop, blackLineIntensity, backgroundColorIntensity, asyncRobotTask, referencedButton);
    }

    private static void reachEndFromBeginOfTrack(EV3.Api api, LightSensorMonitor lightSensorMonitor, ArrayList<LightSensor.Color> colorsToCheck, LightSensor.Color colorStop, short blackLineIntensity, short backgroundColorIntensity, AsyncRobotTask asyncRobotTask, PositionButton referencedButton) throws InterruptedException, IOException, RobotException {

        LightSensor.Color colorFound = null;

        int i = referencedButton.getTrackNumber();

        asyncRobotTask.moveToTrack(i);

        while(colorFound != colorStop){
            colorFound = RobotOperation.followLine(api,
                    lightSensorMonitor,
                    ManualActivity.Direction.FORWARD,
                    LightSensor.Color.BLACK,
                    blackLineIntensity,
                    backgroundColorIntensity,
                    colorsToCheck
            );

            i--;

            asyncRobotTask.moveToTrack(i);

            if(colorFound != colorStop){
                smallMovementUntilBlackOrWhite(api, lightSensorMonitor, ManualActivity.Direction.FORWARD, ManualActivity.Direction.LEFT, 1);
            }
        }
    }

    private static void returnToBeginOfTrack(EV3.Api api, LightSensorMonitor lightSensorMonitor, ArrayList<LightSensor.Color> colorsToCheck, short blackLineIntensity, short backgroundColorIntensity, AsyncRobotTask asyncRobotTask, PositionButton referencedButton) throws RobotException, IOException, InterruptedException {

        int position = referencedButton.getPositionNumber();

        while(position >= 0){

            RobotOperation.followLine(api,
                    lightSensorMonitor,
                    ManualActivity.Direction.BACKWARD,
                    LightSensor.Color.BLACK,
                    blackLineIntensity,
                    backgroundColorIntensity,
                    colorsToCheck
            );

            asyncRobotTask.moveToPositionOnTrack(referencedButton.getTrackNumber(), position);

            if (position >= 1) {
                RobotOperation.smallMovementUntilBlackOrWhite(api, lightSensorMonitor, ManualActivity.Direction.BACKWARD, ManualActivity.Direction.RIGHT, 1);
            } else {
                RobotOperation.turnUntilColor(api, lightSensorMonitor, LightSensor.Color.BLACK, VirtualMap.Wheel.RIGHT, ManualActivity.Direction.BACKWARD);
            }
            position--;


        }
    }

    private static void reachPosFromBeginOfTrack(EV3.Api api, LightSensorMonitor lightSensorMonitor, ArrayList<LightSensor.Color> colorsToCheck, short blackLineIntensity, short backgroundColorIntensity, AsyncRobotTask asyncRobotTask, PositionButton referencedButton) throws RobotException, IOException, InterruptedException {

        int position = -1;

        while(position != referencedButton.getPositionNumber()) {

            RobotOperation.followLine(api,
                    lightSensorMonitor,
                    ManualActivity.Direction.FORWARD,
                    LightSensor.Color.BLACK,
                    blackLineIntensity,
                    backgroundColorIntensity,
                    colorsToCheck
            );


            position ++;

            asyncRobotTask.moveToPositionOnTrack(referencedButton.getTrackNumber(), referencedButton.getPositionNumber());

            if(position != referencedButton.getPositionNumber()) {
                smallMovementUntilBlackOrWhite(api, lightSensorMonitor, ManualActivity.Direction.FORWARD, ManualActivity.Direction.LEFT, 1);
            }

        }
    }

    private static void reachBeginOfTrack(EV3.Api api, LightSensorMonitor lightSensorMonitor, int trackNumber, ArrayList<LightSensor.Color> colorsToCheck, short blackLineIntensity, short backgroundColorIntensity, AsyncRobotTask asyncRobotTask) throws RobotException, IOException, InterruptedException {

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

            asyncRobotTask.moveToTrack(i);

            if(i != trackNumber){
                smallMovementUntilBlackOrWhite(api, lightSensorMonitor, ManualActivity.Direction.FORWARD, ManualActivity.Direction.RIGHT, 1);
            }
        }
    }

    public static void pickUpObjectTest(EV3.Api api) throws RobotException {
        try {
            final float distance = 25;
            final float distanceToObjectForPick = 5;

            Motor rightMotor = new Motor(api, EV3.OutputPort.C);
            Motor leftMotor = new Motor(api, EV3.OutputPort.B);

            Thread right = new Thread(rightMotor);
            Thread left = new Thread(leftMotor);

            Grabber grabber = new Grabber(api);
            Thread grab = new Thread(grabber);
            grab.start();

            UltrasonicSensorDistance ultrasonicSensorDistance = new UltrasonicSensorDistance(api);
            Thread t = new Thread(ultrasonicSensorDistance);
            t.start();

            grabber.up();

            float distanceToObject = turnUntilObstacle(api, t, ultrasonicSensorDistance, distance, VirtualMap.Wheel.LEFT, ManualActivity.Direction.BACKWARD);
            t.join();

            t = new Thread(ultrasonicSensorDistance);

            right.start();
            left.start();
            t.start();

            moveUntilObject(api, distanceToObject - distanceToObjectForPick);

       //     rightMotor.move(distanceToObject - distanceToObjectForPick);
       //     leftMotor.move(distanceToObject - distanceToObjectForPick);

       //     leftMotor.brake();
       //     rightMotor.brake();

            grabber.down();

            grab.interrupt();
            t.interrupt();
            left.interrupt();
            right.interrupt();
        } catch (IOException | ExecutionException | InterruptedException e) {
            e.printStackTrace();
            throw new RobotException("Qualcosa è andato storto. Riprova l'operazione.");
        }

    }

    private static void moveUntilObject (EV3.Api api, float distance) throws RobotException{

        try {
            final float wheelRadius = 2.25f;

            Motor rightMotor = new Motor(api, EV3.OutputPort.C);
            Motor leftMotor = new Motor(api, EV3.OutputPort.B);

            Thread right = new Thread(rightMotor);
            Thread left = new Thread(leftMotor);


            float alpha = (180 * distance) / ((float) Math.PI * wheelRadius); //TODO: le misure sono in centimetri, se sono in altra unità di misura modificare a e wheelRadius

            float leftPosition = leftMotor.getPosition().get();
            float rightPosition = rightMotor.getPosition().get();

            while (leftMotor.getPosition().get() < leftPosition + alpha || rightMotor.getPosition().get() < rightPosition + alpha) { //TODO: forse  getPosition().get() < position + alpha --> dipende da come gira
                rightMotor.setPower(20);
                leftMotor.setPower(20);
            }

            rightMotor.brake();
            leftMotor.brake();
            rightMotor.setPower(0);
            leftMotor.setPower(0);

            leftMotor.setPower(-20);
            leftMotor.start();

            Thread.sleep(500);
            leftMotor.brake();
            leftMotor.setPower(0);

            right.interrupt();
            left.interrupt();
        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new RobotException("Qualcosa è andato storto, riprova questa operazione.");
        }

    }

    private static float turnUntilObstacle(EV3.Api api, Thread t, UltrasonicSensorDistance ultrasonicSensorDistance, float distance, VirtualMap.Wheel wheel, ManualActivity.Direction direction) throws RobotException{
        try {
            final int power = 20;

            Motor rightMotor = new Motor(api, EV3.OutputPort.C);
            Motor leftMotor = new Motor(api, EV3.OutputPort.B);
            GyroSensorAngle gyroSensorAngle = new GyroSensorAngle(api);

            final float initialAngle = gyroSensorAngle.getAngleNow();

            Thread right = new Thread(rightMotor);
            Thread left = new Thread(leftMotor);

            right.start();
            left.start();

            if (!ultrasonicSensorDistance.isDetected(distance)) {
                if (wheel == VirtualMap.Wheel.LEFT) {
                    if (direction == ManualActivity.Direction.FORWARD) {
                        leftMotor.setPower(power);
                    } else {
                        leftMotor.setPower(-power);
                    }
                } else {
                    if (direction == ManualActivity.Direction.FORWARD) {
                        rightMotor.setPower(power);
                    } else {
                        rightMotor.setPower(-power);
                    }
                }
            }

            boolean isFound = false;

            while (!isFound && gyroSensorAngle.getAngleNow() > initialAngle - 180) {
                isFound = ultrasonicSensorDistance.isDetected(distance);
            }

            if (!isFound) {

                rightMotor.brake();
                leftMotor.brake();
                rightMotor.setPower(0);
                rightMotor.setPower(0);
                left.interrupt();
                right.interrupt();
                throw new RobotException("Il robot non è stato in grado di trovare l'oggetto.");
            }

            rightMotor.brake();
            leftMotor.brake();

            rightMotor.setPower(0);
            rightMotor.setPower(0);

            float distanceToObject = ultrasonicSensorDistance.getDistanceToObject();

            left.interrupt();
            right.interrupt();
            t.interrupt();

            return distanceToObject;
        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw  new RobotException("Qualcosa è andato storto, ripetere l'operazione.");
        }
    }


    /*
    public static void pickUpObject (EV3.Api api) throws  RobotException{
        try {

            Grabber grabber = new Grabber(api);
            Thread grab = new Thread(grabber);
            grab.start();
            grabber.up();

            final float MAX_OBJ_DISTANCE = 25;
            final int POWER = 15;

            UltrasonicSensor ultrasonicSensor = api.getUltrasonicSensor(EV3.InputPort._4);
            Future<Float> futureDistance;
            float currentDistance = 0;


            GyroSensorAngle gyroSensorAngle = new GyroSensorAngle(api);
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
                grab.interrupt();
                throw new RobotException("Object not found.");
            }

            final float ANGLE_MIN = gyroSensorAngle.getAngleNow();

            while (currentDistance <= objectDistance) {
                futureDistance = ultrasonicSensor.getDistance();
                currentDistance = futureDistance.get();
                leftMotor.setPower(-POWER);
            }
            leftMotor.brake();
            rightMotor.brake();

            leftMotor.setPower(0);
            rightMotor.setPower(0);


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

            while (currentDistance > 5) {
                futureDistance = ultrasonicSensor.getDistance();
                currentDistance = futureDistance.get();

                leftMotor.setPower(POWER);
                rightMotor.setPower(POWER);
            }

            leftMotor.setPower(0);
            rightMotor.setPower(0);

            grabber.down();

            /*
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

            left.interrupt();
            right.interrupt();
            gyroSensor.interrupt();
            grab.interrupt();



        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new RobotException("Something went wrong. Please try this operation again.");
        }
    }
*/

    public static void addObject(EV3.Api api, AsyncRobotTask asyncRobotTask,
                                 Short backgroundColorIntensity, Short blackColorIntensity, PositionButton referencedButton) throws RobotException {
        try {
            ArrayList<LightSensor.Color> colorsToCheck = new ArrayList<>();
            colorsToCheck.add(LightSensor.Color.RED);
            //colorsToCheck.add(LightSensor.Color.BROWN);
            colorsToCheck.add(LightSensor.Color.YELLOW);
            colorsToCheck.add(LightSensor.Color.GREEN);

            LightSensor.Color colorStop = LightSensor.Color.RED;

            LightSensorMonitor lightSensorMonitor = new LightSensorMonitor();
            Grabber grabber = new Grabber(api);
            Thread t = new Thread(grabber);
            t.start();
/*
            grabber.up();

            //TODO: user put the object in front

            if(!grabber.isPresent){
                //TODO: warning message
            }

            grabber.down();*/
            grabber.killThread(t);

            t.join();

            reachPosFromOrigin(api, lightSensorMonitor,
                    colorsToCheck, blackColorIntensity, backgroundColorIntensity, asyncRobotTask, referencedButton);

            t = new Thread(grabber);
            t.start();
            putObject(api, grabber, lightSensorMonitor, ManualActivity.Direction.FORWARD);

            asyncRobotTask.moveToPositionOnTrack(referencedButton.getTrackNumber(), referencedButton.getPositionNumber());

            referencedButton.changeOccupiedState();

            reachOriginFromPos(api, lightSensorMonitor, colorsToCheck, colorStop, blackColorIntensity, backgroundColorIntensity, asyncRobotTask, referencedButton);


        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            throw new RobotException("Something went wrong, please try again");
        }
    }

    private static void putObject(EV3.Api api, Grabber grabber, LightSensorMonitor lightSensorMonitor, ManualActivity.Direction direction) throws RobotException, IOException, InterruptedException {

        if(direction == ManualActivity.Direction.FORWARD)
            robotRotation(api, -90, VirtualMap.Wheel.RIGHT);
        else {
            smallMovementUntilBlackOrWhite(api, lightSensorMonitor, ManualActivity.Direction.FORWARD, ManualActivity.Direction.RIGHT, 1);
            robotRotation(api, 90, VirtualMap.Wheel.LEFT);
        }

        try {
            grabber.up();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        RobotOperation.robotRotation(api, -50, VirtualMap.Wheel.LEFT);

        try {
            grabber.down();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        RobotOperation.turnUntilColor(api, lightSensorMonitor, LightSensor.Color.BLACK, VirtualMap.Wheel.LEFT, ManualActivity.Direction.BACKWARD);

    }

    //PositionButton hanno un metodo changeOccupiedState per cambiare lo stato occupato/libero.
    //AsyncTask chiama i metodi per muovere il robot sulla mappa virtuale,
    // destinationButton è il bottone dove l'oggetto deve essere spostato e ha metodi per prendere numero del track e numero della posizione;
    //buttonToMoveObjFrom è il bottone da cui muovere l'oggetto.

    //TODO: complete
    public static void moveObject(EV3.Api api, AsyncRobotTask asyncRobotTask,
                                  Short backgroundColorIntensity, Short blackColorIntensity, PositionButton destinationButton, PositionButton buttonToMoveObjFrom) throws RobotException {
        try {
            ArrayList<LightSensor.Color> colorsToCheck = new ArrayList<>();
            colorsToCheck.add(LightSensor.Color.RED);
            colorsToCheck.add(LightSensor.Color.YELLOW);
            colorsToCheck.add(LightSensor.Color.GREEN);

            LightSensor.Color colorStop = LightSensor.Color.RED;

            LightSensorMonitor lightSensorMonitor = new LightSensorMonitor();
            Grabber grabber = new Grabber(api);
            Thread t = new Thread(grabber);
            t.start();

            reachPosFromOrigin(api, lightSensorMonitor,
                    colorsToCheck, blackColorIntensity, backgroundColorIntensity, asyncRobotTask, buttonToMoveObjFrom);

            pickUpObjectTest(api); //TODO: to be tested

            if (destinationButton.getTrackNumber() == buttonToMoveObjFrom.getTrackNumber() &&
                    destinationButton.getPositionNumber() > buttonToMoveObjFrom.getPositionNumber())
                turnUntilColor(api, new LightSensorMonitor(), LightSensor.Color.BLACK, VirtualMap.Wheel.LEFT, ManualActivity.Direction.FORWARD);
            else
                turnUntilColor(api, new LightSensorMonitor(), LightSensor.Color.BLACK, VirtualMap.Wheel.LEFT, ManualActivity.Direction.BACKWARD);


            buttonToMoveObjFrom.changeOccupiedState();

            ManualActivity.Direction direction = reachPosFromPos(api, lightSensorMonitor, colorsToCheck, blackColorIntensity, backgroundColorIntensity,
                    asyncRobotTask, buttonToMoveObjFrom, destinationButton);

            putObject(api, grabber, lightSensorMonitor, direction);


            destinationButton.changeOccupiedState();

            reachOriginFromPos(api, lightSensorMonitor, colorsToCheck,
                    colorStop, blackColorIntensity, backgroundColorIntensity, asyncRobotTask, destinationButton);

            t.interrupt();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            throw new RobotException("Qualcosa è andato storto, riprova l'operazione.");
        }

    }

    public static void removeObject(EV3.Api api, AsyncRobotTask asyncRobotTask,
                                    Short backgroundColorIntensity, Short blackColorIntensity, PositionButton referencedButton) throws RobotException {

        try {
            ArrayList<LightSensor.Color> colorsToCheck = new ArrayList<>();
            colorsToCheck.add(LightSensor.Color.RED);
            colorsToCheck.add(LightSensor.Color.YELLOW);
            colorsToCheck.add(LightSensor.Color.GREEN);

            LightSensor.Color colorStop = LightSensor.Color.RED;

            LightSensorMonitor lightSensorMonitor = new LightSensorMonitor();
            Grabber grabber = new Grabber(api);
            Thread t = new Thread(grabber);
            t.start();

            reachPosFromOrigin(api, lightSensorMonitor,
                    colorsToCheck, blackColorIntensity, backgroundColorIntensity, asyncRobotTask, referencedButton);

            pickUpObjectTest(api);

            referencedButton.changeOccupiedState();

            turnUntilColor(api, new LightSensorMonitor(), LightSensor.Color.BLACK, VirtualMap.Wheel.LEFT, ManualActivity.Direction.BACKWARD);

            reachOriginFromPos(api, lightSensorMonitor, colorsToCheck,
                    colorStop, blackColorIntensity, backgroundColorIntensity, asyncRobotTask, referencedButton);

            try {
                grabber.up();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            throw new RobotException("Qualcosa è andato storto, riprova questa operazione.");
        }

    }
}
