package it.unive.dais.legodroid.ourUtil;

import android.content.Intent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import it.unive.dais.legodroid.code.ManualActivity;
import it.unive.dais.legodroid.code.PopupErrorActivity;
import it.unive.dais.legodroid.code.VirtualMapActivity;
import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.plugs.LightSensor;

import static java.lang.Math.abs;

public final class RobotOperation {


    private final static String commonException = "Sembra che qualcosa sia andato storto. Per favore, ritenta l'operazione.";

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
                            rightMotor.setPower(0);
                            leftMotor.setPower(0);
                            rightMotor.setPower(0);
                            leftMotor.setPower(0);
                            leftMotor.brake();
                            rightMotor.brake();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                            throw new RobotException(commonException);
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

                }

                Thread.sleep(25);

                flag = null;
                while(flag == null) {
                    try {
                        flag = lightSensorColor.getIsObstacleFound();
                    } catch (ExecutionException e) {
                        leftMotor.setPower(0);
                        rightMotor.setPower(0);
                        rightMotor.setPower(0);
                        leftMotor.setPower(0);
                        leftMotor.brake();
                        rightMotor.brake();
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

            right.interrupt();
            left.interrupt();

            lightColor.interrupt();
            lightIntensity.interrupt();

            if (lightSensorColor.isHasExceptionOccurred()) {
                throw new RobotException(commonException);
            }

        }
        catch (IOException | InterruptedException e) {
            e.printStackTrace();
            throw new RobotException(commonException);
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
                    throw new RobotException("Sembra che il Robot abbia fallito l'operazione. " +
                            "Per favore, assicurati che il robot sia posizionato nel modo corretto o che " +
                            "il formato della mappa sia corretto e riprova l'operazione.");
            }

            else {
                if (reflectedColor == color)
                    throw new RobotException("Sembra che il Robot abbia fallito l'operazione. " +
                            "Per favore, assicurati che il robot sia posizionato nel modo corretto o che " +
                            "il formato della mappa sia corretto e riprova l'operazione.");
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
                throw new RobotException(commonException);
            }
            e.printStackTrace();
            throw new RobotException(commonException);
        }
        t.interrupt();
    }

    public static LightSensor.Color getReflectedColor(EV3.Api api, LightSensorMonitor lightSensorMonitor, Motor rightMotor, Motor leftMotor) throws RobotException {
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
                        if (rightMotor!= null) {
                            rightMotor.setPower(0);
                        }
                        if (leftMotor != null) {
                            leftMotor.setPower(0);
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        throw new RobotException(commonException);
                    }
                }
            }



            lightSensorColor.killThread(t);

            return color;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            throw new RobotException(commonException);
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
                throw new RobotException(commonException);
            }
            e.printStackTrace();
            throw new RobotException(commonException);
        }
    }

    public static short getBackgroundColorIntensity(EV3.Api api, LightSensorMonitor lightSensorMonitor) throws RobotException {

        robotRotation (api, 30, VirtualMap.Wheel.LEFT);

        checkColor(api, lightSensorMonitor, LightSensor.Color.WHITE, true);
        short backgroundColor = getReflectedIntensity(api, lightSensorMonitor);

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
            throw new RobotException(commonException);
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
                while (RobotOperation.getReflectedColor(api, lightSensorMonitor, null, leftMotor) != color) {
                    leftMotor.setPower(power);
                    try {
                        Thread.sleep(25);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                while (RobotOperation.getReflectedColor(api, lightSensorMonitor, rightMotor, null) != color) {
                    rightMotor.setPower(power);
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

        while(!colorArray.contains(RobotOperation.getReflectedColor(api, lightSensorMonitor, rightMotor, leftMotor))) {

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

    public static void backTrack(EV3.Api api, LightSensorMonitor lightSensorMonitor, short blackLineIntensity,
                                 short backgroundColorIntensity, ArrayList<LightSensor.Color> colorsToCheck,
                                 int trackNumber, int positionNumber, AsyncRobotTask asyncRobotTask) throws RobotException, IOException, InterruptedException {

        Integer positions = positionNumber;

        while(positions >= 0) {
            RobotOperation.followLine(api,
                    lightSensorMonitor,
                    ManualActivity.Direction.FORWARD,
                    LightSensor.Color.BLACK,
                    blackLineIntensity,
                    backgroundColorIntensity,
                    colorsToCheck
            );

            checkColor(api, lightSensorMonitor, LightSensor.Color.RED, false);

            positions--;
            asyncRobotTask.moveToPositionOnTrack(trackNumber, positions);
            if (positions >= 0) {
                RobotOperation.smallMovementUntilBlackOrWhite(api, lightSensorMonitor, ManualActivity.Direction.FORWARD, ManualActivity.Direction.LEFT, 1);
            }
        }

    }

    public static void reachOriginFromPos(EV3.Api api, LightSensorMonitor lightSensorMonitor, ArrayList<LightSensor.Color> colorsToCheck, LightSensor.Color colorStop, short blackLineIntensity, short backgroundColorIntensity, AsyncRobotTask asyncRobotTask, PositionButton referencedButton) throws InterruptedException, IOException, RobotException {
        //returnToBeginOfTrack(api, lightSensorMonitor, colorsToCheck, blackLineIntensity, backgroundColorIntensity, asyncRobotTask, referencedButton);

        backTrack(api, lightSensorMonitor, blackLineIntensity, backgroundColorIntensity, colorsToCheck, referencedButton.getTrackNumber(), referencedButton.getPositionNumber(), asyncRobotTask);

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

    }

    public static void reachPosFromOrigin(EV3.Api api, LightSensorMonitor lightSensorMonitor, ArrayList<LightSensor.Color> colorsToCheck,
                                          short blackLineIntensity, short backgroundColorIntensity,
                                          AsyncRobotTask asyncRobotTask, PositionButton referencedButton) throws RobotException, IOException, InterruptedException {

        try {
            RobotOperation.checkColor(api, lightSensorMonitor, LightSensor.Color.BLACK, true);
        } catch (RobotException e) {
            throw new RobotException("Qualcosa è andato storto. Assicurati che il grabber sia abbassato e che il sensore si trovi sulla linea" +
                    " nera e ad inizio mappa prima di iniziare l'operazione.");
        }

        reachBeginOfTrack(api, lightSensorMonitor, referencedButton.getTrackNumber(), colorsToCheck, blackLineIntensity,
                backgroundColorIntensity, asyncRobotTask);

        RobotOperation.robotRotation(api, -90, VirtualMap.Wheel.RIGHT);
        RobotOperation.turnUntilColor(api, lightSensorMonitor, LightSensor.Color.BLACK, VirtualMap.Wheel.LEFT, ManualActivity.Direction.FORWARD);


        reachPosFromBeginOfTrack(api, lightSensorMonitor, colorsToCheck, blackLineIntensity,
                backgroundColorIntensity, asyncRobotTask, referencedButton);

    }


    public static void reachPosFromTrackOrigin(EV3.Api api, LightSensorMonitor lightSensorMonitor, ArrayList<LightSensor.Color> colorsToCheck,
                                          short blackLineIntensity, short backgroundColorIntensity,
                                          AsyncRobotTask asyncRobotTask, PositionButton referencedButton,
                                               int trackNumber) throws RobotException, IOException, InterruptedException {

        reachBeginOfTrackFromTrack(api, lightSensorMonitor, referencedButton.getTrackNumber(), colorsToCheck, blackLineIntensity,
                backgroundColorIntensity, asyncRobotTask, trackNumber);

        RobotOperation.robotRotation(api, -90, VirtualMap.Wheel.RIGHT);
        RobotOperation.turnUntilColor(api, lightSensorMonitor, LightSensor.Color.BLACK, VirtualMap.Wheel.LEFT, ManualActivity.Direction.FORWARD);


        reachPosFromBeginOfTrack(api, lightSensorMonitor, colorsToCheck, blackLineIntensity,
                backgroundColorIntensity, asyncRobotTask, referencedButton);

    }


    private static void reachBeginOfTrackFromTrack(EV3.Api api, LightSensorMonitor lightSensorMonitor, int trackNumber, ArrayList<LightSensor.Color> colorsToCheck, short blackLineIntensity, short backgroundColorIntensity, AsyncRobotTask asyncRobotTask, int trackOrigin) throws RobotException, IOException, InterruptedException {

        int i =trackOrigin;

        while (i != trackNumber){
            RobotOperation.followLine(api,
                    lightSensorMonitor,
                    ManualActivity.Direction.FORWARD,
                    LightSensor.Color.BLACK,
                    blackLineIntensity,
                    backgroundColorIntensity,
                    colorsToCheck
            );

            checkColor(api, lightSensorMonitor, LightSensor.Color.RED, false);

            i++;

            asyncRobotTask.moveToTrack(i);

            if(i != trackNumber){
                smallMovementUntilBlackOrWhite(api, lightSensorMonitor, ManualActivity.Direction.FORWARD, ManualActivity.Direction.RIGHT, 1);
            }
        }
    }



    public static ManualActivity.Direction reachPosFromPos(EV3.Api api, LightSensorMonitor lightSensorMonitor, ArrayList<LightSensor.Color> colorsToCheck, short blackLineIntensity, short backgroundColorIntensity, AsyncRobotTask asyncRobotTask, PositionButton buttonToMoveObjFrom, PositionButton destinationButton) throws InterruptedException, IOException, RobotException {
        int deltaPos = 0;

        ManualActivity.Direction sameTrackMovement = null;

        if(buttonToMoveObjFrom.getTrackNumber() == destinationButton.getTrackNumber()){
            sameTrackMovement = reachAnotherPosOfTrack(api, lightSensorMonitor, destinationButton.getPositionNumber(), colorsToCheck, blackLineIntensity, backgroundColorIntensity, asyncRobotTask, buttonToMoveObjFrom);
        } else {

            backTrack(api, lightSensorMonitor, blackLineIntensity, backgroundColorIntensity, colorsToCheck, buttonToMoveObjFrom.getTrackNumber(),
                    buttonToMoveObjFrom.getPositionNumber(), asyncRobotTask);

            if (destinationButton.getTrackNumber() > buttonToMoveObjFrom.getTrackNumber()) {

                RobotOperation.robotRotation(api, -55, VirtualMap.Wheel.RIGHT);
                RobotOperation.turnUntilColor(api, lightSensorMonitor, LightSensor.Color.BLACK, VirtualMap.Wheel.LEFT, ManualActivity.Direction.FORWARD);

                PositionButton positionButton = new PositionButton(destinationButton.getContext(), destinationButton.getUIManager(), destinationButton.getTrackNumber() - buttonToMoveObjFrom.getTrackNumber(), destinationButton.getPositionNumber());

                reachPosFromTrackOrigin(api, lightSensorMonitor, colorsToCheck, blackLineIntensity, backgroundColorIntensity, asyncRobotTask, positionButton, buttonToMoveObjFrom.getTrackNumber());
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

            while(positions > 0){
                RobotOperation.followLine(api,
                        lightSensorMonitor,
                        ManualActivity.Direction.FORWARD,
                        LightSensor.Color.BLACK,
                        blackLineIntensity,
                        backgroundColorIntensity,
                        colorsToCheck
                );

                checkColor(api, lightSensorMonitor, LightSensor.Color.RED, false);


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

                    checkColor(api, lightSensorMonitor, LightSensor.Color.RED, false);

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

            checkColor(api, lightSensorMonitor, LightSensor.Color.RED, false);

            position ++;

            asyncRobotTask.moveToPositionOnTrack(referencedButton.getTrackNumber(), position);

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

            checkColor(api, lightSensorMonitor, LightSensor.Color.RED, false);

            i++;

            asyncRobotTask.moveToTrack(i);

            if(i != trackNumber){
                smallMovementUntilBlackOrWhite(api, lightSensorMonitor, ManualActivity.Direction.FORWARD, ManualActivity.Direction.RIGHT, 1);
            }
        }
    }

    public static void pickUpObjectTest(EV3.Api api, PositionButton button, AsyncRobotTask asyncTask) throws RobotException {
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

            grabber.down();

            asyncTask.changeOccupiedState(button);

            grab.interrupt();
            t.interrupt();
            left.interrupt();
            right.interrupt();
        } catch (IOException | ExecutionException | InterruptedException e) {
            e.printStackTrace();
            throw new RobotException(commonException);
        }

    }

    private static void moveUntilObject (EV3.Api api, float distance) throws RobotException{

        try {
            final float wheelRadius = 2.25f;

            Motor rightMotor = new Motor(api, EV3.OutputPort.C);
            Motor leftMotor = new Motor(api, EV3.OutputPort.B);

            Thread right = new Thread(rightMotor);
            Thread left = new Thread(leftMotor);


            float alpha = (180 * distance) / ((float) Math.PI * wheelRadius);

            float leftPosition = leftMotor.getPosition().get();
            float rightPosition = rightMotor.getPosition().get();

            while (leftMotor.getPosition().get() < leftPosition + alpha || rightMotor.getPosition().get() < rightPosition + alpha) {
                rightMotor.setPower(20);
                leftMotor.setPower(20);
            }

            rightMotor.brake();
            leftMotor.brake();
            rightMotor.setPower(0);
            leftMotor.setPower(0);

            right.interrupt();
            left.interrupt();
        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new RobotException(commonException);
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
                throw new RobotException("Qualcosa è andato storto. Il robot non è stato in grado di trovare l'oggetto.");
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
            throw  new RobotException(commonException);
        }
    }


    public static void addObject(EV3.Api api, AsyncRobotTask asyncRobotTask,
                                 Short backgroundColorIntensity, Short blackColorIntensity, PositionButton referencedButton) throws RobotException {
        try {

            Grabber.setDown();

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

            putObject(api, grabber, lightSensorMonitor, ManualActivity.Direction.FORWARD, referencedButton, asyncRobotTask);

            asyncRobotTask.moveToPositionOnTrack(referencedButton.getTrackNumber(), referencedButton.getPositionNumber());

            reachOriginFromPos(api, lightSensorMonitor, colorsToCheck, colorStop, blackColorIntensity, backgroundColorIntensity, asyncRobotTask, referencedButton);


        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            throw new RobotException(commonException);
        }
    }

    private static void putObject(EV3.Api api, Grabber grabber, LightSensorMonitor lightSensorMonitor,
                                  ManualActivity.Direction direction,
                                  PositionButton referencedButton, AsyncRobotTask asyncTask) throws RobotException, IOException, InterruptedException {

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

        asyncTask.changeOccupiedState(referencedButton);

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

    public static void moveObject(EV3.Api api, AsyncRobotTask asyncRobotTask,
                                  Short backgroundColorIntensity, Short blackColorIntensity, PositionButton destinationButton, PositionButton buttonToMoveObjFrom) throws RobotException {
        try {

            Grabber.setDown();
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

            pickUpObjectTest(api, buttonToMoveObjFrom, asyncRobotTask);

            if (destinationButton.getTrackNumber() == buttonToMoveObjFrom.getTrackNumber() &&
                    destinationButton.getPositionNumber() > buttonToMoveObjFrom.getPositionNumber())
                turnUntilColor(api, new LightSensorMonitor(), LightSensor.Color.BLACK, VirtualMap.Wheel.LEFT, ManualActivity.Direction.FORWARD);
            else
                turnUntilColor(api, new LightSensorMonitor(), LightSensor.Color.BLACK, VirtualMap.Wheel.LEFT, ManualActivity.Direction.BACKWARD);


            ManualActivity.Direction direction = reachPosFromPos(api, lightSensorMonitor, colorsToCheck, blackColorIntensity, backgroundColorIntensity,
                    asyncRobotTask, buttonToMoveObjFrom, destinationButton);

            putObject(api, grabber, lightSensorMonitor, direction, destinationButton, asyncRobotTask);


            reachOriginFromPos(api, lightSensorMonitor, colorsToCheck,
                    colorStop, blackColorIntensity, backgroundColorIntensity, asyncRobotTask, destinationButton);

            asyncRobotTask.moveToBeginning();

            t.interrupt();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            throw new RobotException(commonException);
        }

    }

    public static void removeObject(EV3.Api api, AsyncRobotTask asyncRobotTask,
                                    Short backgroundColorIntensity, Short blackColorIntensity, PositionButton referencedButton) throws RobotException {

        try {
            Grabber.setDown();
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

            pickUpObjectTest(api, referencedButton, asyncRobotTask);

            turnUntilColor(api, new LightSensorMonitor(), LightSensor.Color.BLACK, VirtualMap.Wheel.LEFT, ManualActivity.Direction.BACKWARD);

            reachOriginFromPos(api, lightSensorMonitor, colorsToCheck,
                    colorStop, blackColorIntensity, backgroundColorIntensity, asyncRobotTask, referencedButton);

            asyncRobotTask.moveToBeginning();

            try {
                grabber.up();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            throw new RobotException(commonException);
        }

    }

    public static void moveForward(EV3.Api api, int distance) throws RobotException {
        try {
            Motor rightMotor = new Motor(api, EV3.OutputPort.C);
            Motor leftMotor = new Motor(api, EV3.OutputPort.B);

            Thread right = new Thread(rightMotor);
            Thread left = new Thread(leftMotor);

            right.start();
            left.start();

            Future<Float> leftMotorPosition = leftMotor.getPosition();
            Future<Float> rightMotorPosition = rightMotor.getPosition();


            float leftStartingPosition = leftMotorPosition.get();
            float rightStartingPosition = rightMotorPosition.get();
            while (leftMotorPosition.get() < leftStartingPosition + distance ||
                    rightMotorPosition.get() < rightStartingPosition + distance) {
                rightMotor.setPower(20);
                leftMotor.setPower(20);
                leftMotorPosition = leftMotor.getPosition();
                rightMotorPosition = rightMotor.getPosition();
            }
            leftMotor.brake();
            rightMotor.brake();
            leftMotor.setPower(0);
            rightMotor.setPower(0);
            right.interrupt();
            left.interrupt();
        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new RobotException(commonException);
        }
    }

}
