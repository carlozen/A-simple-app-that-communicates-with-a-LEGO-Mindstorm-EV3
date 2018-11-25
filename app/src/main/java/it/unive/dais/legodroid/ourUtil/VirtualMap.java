package it.unive.dais.legodroid.ourUtil;

import java.util.ArrayList;

import it.unive.dais.legodroid.code.ManualActivity;
import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.plugs.LightSensor;

//TODO
public class VirtualMap {

    private ArrayList<MapTrack> trackList;
    private Short blackColorIntensity;
    private Short backgroundColorIntensity;

    private VirtualMap (ArrayList<MapTrack> trackList, short blackColorIntensity, short backgroundColorIntensity) {
        this.trackList = trackList;
        this.blackColorIntensity = blackColorIntensity;
        this.backgroundColorIntensity = backgroundColorIntensity;
    }

    //When you open a saved map and it needs to recatch the blackColorIntensity and the backgroundColorIntensity
    private VirtualMap (ArrayList<MapTrack> trackList) {
        this.trackList = trackList;
        this.blackColorIntensity = null;
        this.backgroundColorIntensity = null;
    }

    public static VirtualMap scan (EV3.Api api) throws RobotException {

        ArrayList<MapTrack> trackList = new ArrayList<>();

        //Checks if the Robot is starting from a black line
        RobotOperation.checkColor(api, LightSensor.Color.BLACK, true);

        short blackLineIntensity = RobotOperation.getReflectedIntensity(api);

        //Tries to get the background color intensity
        short backgroundColorIntensity = RobotOperation.getBackgroundColorIntensity (api);

        while (RobotOperation.getReflectedColor(api) != LightSensor.Color.RED) {
            //Follows the black line
            RobotOperation.followLine(api,
                    ManualActivity.Direction.FORWARD,
                    LightSensor.Color.BLACK,
                    blackLineIntensity,
                    backgroundColorIntensity
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
                    "Every map needs at least one track with at least one object position.");

        RobotOperation.robotRotation(api, -145);
        RobotOperation.smallMovement(api, ManualActivity.Direction.FORWARD, 30);

        while (RobotOperation.getReflectedColor(api) != LightSensor.Color.RED) {
            //Follows the black line
            RobotOperation.followLine(api,
                    ManualActivity.Direction.FORWARD,
                    LightSensor.Color.BLACK,
                    blackLineIntensity,
                    backgroundColorIntensity
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

    private static void backTrack(EV3.Api api, short backgroundColorIntensity, MapTrack mapTrack) throws RobotException {

        for (int i = 0; i < mapTrack.objectList.size() + 1; i++) {

            //Moves a little bit back to position to the line
            RobotOperation.smallMovement (api, ManualActivity.Direction.BACKWARD, 30);

            //Follow the line backwards until the next position.
            RobotOperation.followLine(api,
                    ManualActivity.Direction.BACKWARD,
                    mapTrack.trackColor,
                    mapTrack.trackColorIntensity,
                    backgroundColorIntensity
            );

            //Check it is a black spot.
            RobotOperation.checkColor(api, LightSensor.Color.BLACK, true);
        }

        RobotOperation.robotRotation(api, -90);
        RobotOperation.smallMovement(api, ManualActivity.Direction.FORWARD, 30);
    }

    private static void scanTrack(EV3.Api api, short backgroundColorIntensity, ArrayList<MapTrack> trackList) throws RobotException {

        LightSensor.Color trackColor = RobotOperation.getReflectedColor(api);
        short trackColorIntensity = RobotOperation.getReflectedIntensity(api);
        int positionsNumber = 0;

        while (RobotOperation.getReflectedColor(api) != LightSensor.Color.RED) {
            RobotOperation.followLine(api,
                    ManualActivity.Direction.FORWARD,
                    trackColor,
                    trackColorIntensity,
                    backgroundColorIntensity
            );

            if (RobotOperation.getReflectedColor(api) == LightSensor.Color.BLACK) {
                positionsNumber++;
            } else
                RobotOperation.checkColor(api, LightSensor.Color.RED, true);
        }

        if (positionsNumber < 0)
            throw new RobotException("It seems this track is empty. \n" +
                    "Every track needs at least a black object position.");
        else
            trackList.add(new MapTrack(trackColor, trackColorIntensity, positionsNumber));
    }

    public static class MapTrack {

        private LightSensor.Color trackColor;
        private short trackColorIntensity;
        private ArrayList<Boolean> objectList;

        public MapTrack(LightSensor.Color trackColor, short trackColorIntensity, int objectPositionNumber) {
            this.trackColor = trackColor;
            this.trackColorIntensity = trackColorIntensity;
            this.objectList = new ArrayList<>(objectPositionNumber);
            for (int i = 0; i< objectPositionNumber; i++) {
                objectList.add(false);
            }
        }


        public void addEmptyObjectPosition () {
            objectList.add(false);
        }

        public void addObject (int position) {
            objectList.set(position, true);
        }

    }
}

