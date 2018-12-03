package it.unive.dais.legodroid.ourUtil;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Locale;

import it.unive.dais.legodroid.code.ManualActivity;
import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.plugs.LightSensor;

//TODO
public class VirtualMap implements Parcelable {

    private ArrayList<MapTrack> trackList;
    private Short blackColorIntensity;
    private Short backgroundColorIntensity;

    public VirtualMap (ArrayList<MapTrack> trackList, short blackColorIntensity, short backgroundColorIntensity) {
        this.trackList = trackList;
        this.blackColorIntensity = blackColorIntensity;
        this.backgroundColorIntensity = backgroundColorIntensity;
    }

    //When you open a saved map and it needs to recatch the blackColorIntensity and the backgroundColorIntensity
    public VirtualMap (ArrayList<MapTrack> trackList) {
        this.trackList = trackList;
        this.blackColorIntensity = null;
        this.backgroundColorIntensity = null;
    }

    public ArrayList<MapTrack> getMapTrackList () {
        return  this.trackList;
    }

    public static VirtualMap scan (EV3.Api api, LightSensor.Color colorStop, ArrayList<LightSensor.Color> colorsToCheck) throws RobotException {

        LightSensorMonitor lightSensorMonitor = new LightSensorMonitor();

        LightSensor.Color colorFound = null;

        ArrayList<MapTrack> trackList = new ArrayList<>();

        //Checks if the Robot is starting from a black line
        RobotOperation.checkColor(api, lightSensorMonitor, LightSensor.Color.BLACK, true);

        short blackLineIntensity = RobotOperation.getReflectedIntensity(api, lightSensorMonitor);

        //Tries to get the background color intensity
        short backgroundColorIntensity = RobotOperation.getBackgroundColorIntensity (api, lightSensorMonitor);

        while (colorFound != colorStop) {
            //Follows the black line

            colorFound = RobotOperation.followLine(api,
                    lightSensorMonitor,
                    ManualActivity.Direction.FORWARD,
                    LightSensor.Color.BLACK,
                    blackLineIntensity,
                    backgroundColorIntensity,
                    colorsToCheck
            );

            //Se non è alla fine del percorso scansiona la linea
            if (colorFound != colorStop) {

                //RobotOperation.moveToTrack(api);

                scanTrack(api, lightSensorMonitor, backgroundColorIntensity, trackList);

                backTrack(api, lightSensorMonitor, backgroundColorIntensity, trackList.get(trackList.size() - 1));
            }
        }

        if (trackList.size() == 0)
            throw new RobotException("It seems your map has no tracks. \n" +
                    "Every map needs at least one track with at least one object position.");

        RobotOperation.robotRotation(api, -145);
        RobotOperation.smallMovement(api, ManualActivity.Direction.FORWARD, 30);


        while (RobotOperation.getReflectedColor(api, lightSensorMonitor) != colorStop) {
            //Follows the black line
            RobotOperation.followLine(api,
                    lightSensorMonitor,
                    ManualActivity.Direction.FORWARD,
                    LightSensor.Color.BLACK,
                    blackLineIntensity,
                    backgroundColorIntensity,
                    colorsToCheck
            );

            //Se non è alla fine del percorso scansiona la linea
            if (RobotOperation.getReflectedColor(api, lightSensorMonitor) != colorStop) {
                RobotOperation.smallMovement(api,ManualActivity.Direction.FORWARD,30);
            }
        }

        //The robot sets to his starting position.
        RobotOperation.robotRotation(api, 145);
        RobotOperation.smallMovement(api, ManualActivity.Direction.FORWARD, 10);
        RobotOperation.robotRotation(api, 45);

        return new VirtualMap(trackList, blackLineIntensity, backgroundColorIntensity);
    }

    private static void backTrack(EV3.Api api, LightSensorMonitor lightSensorMonitor, short backgroundColorIntensity, MapTrack mapTrack) throws RobotException {

        for (int i = 0; i < mapTrack.objectList.size() + 1; i++) {

            //Moves a little bit back to position to the line
            RobotOperation.smallMovement (api, ManualActivity.Direction.BACKWARD, 30);

            //Follow the line backwards until the next position.
            ArrayList<LightSensor.Color> colorsToCheck = new ArrayList<>();
            colorsToCheck.add(LightSensor.Color.BLACK);

            RobotOperation.followLine(api,
                    lightSensorMonitor,
                    ManualActivity.Direction.BACKWARD,
                    mapTrack.trackColor,
                    mapTrack.trackColorIntensity,
                    backgroundColorIntensity,
                    colorsToCheck
            );

            //Check it is a black spot.
            RobotOperation.checkColor(api, lightSensorMonitor, LightSensor.Color.BLACK, true);
        }

        RobotOperation.robotRotation(api, -90); //TODO: to be tested
        RobotOperation.smallMovement(api, ManualActivity.Direction.FORWARD, 30); //TODO: to be tested
    }

    private static void scanTrack(EV3.Api api, LightSensorMonitor lightSensorMonitor, short backgroundColorIntensity, ArrayList<MapTrack> trackList) throws RobotException {

        LightSensor.Color trackColor = RobotOperation.getReflectedColor(api, lightSensorMonitor);
        short trackColorIntensity = RobotOperation.getReflectedIntensity(api, lightSensorMonitor);
        int positionsNumber = 0;

        ArrayList<LightSensor.Color> colorsToCheck = new ArrayList<>();
        colorsToCheck.add(LightSensor.Color.RED);
        colorsToCheck.add(LightSensor.Color.BLACK);

        while (RobotOperation.getReflectedColor(api, lightSensorMonitor) != LightSensor.Color.RED) {
            RobotOperation.followLine(api,
                    lightSensorMonitor,
                    ManualActivity.Direction.FORWARD,
                    trackColor,
                    trackColorIntensity,
                    backgroundColorIntensity,
                    colorsToCheck
            );

            if (RobotOperation.getReflectedColor(api, lightSensorMonitor) == LightSensor.Color.BLACK) {

                positionsNumber++;
                ArrayList<LightSensor.Color> listTrackColor = new ArrayList<>();
                listTrackColor.add(trackColor);

                RobotOperation.followLine(api,
                        lightSensorMonitor,
                        ManualActivity.Direction.FORWARD,
                        LightSensor.Color.BLACK,
                        RobotOperation.getReflectedIntensity(api, lightSensorMonitor),
                        RobotOperation.getBackgroundColorIntensity(api, lightSensorMonitor),
                        listTrackColor
                );

            } else
                RobotOperation.checkColor(api, lightSensorMonitor, LightSensor.Color.RED, true);
        }

        if (positionsNumber < 0)
            throw new RobotException("It seems this track is empty. \n" +
                    "Every track needs at least a black object position.");
        else
            trackList.add(new MapTrack(trackColor, trackColorIntensity, positionsNumber));
    }

    public static final Parcelable.Creator <VirtualMap> CREATOR = new Parcelable.Creator<VirtualMap>() {

        @Override
        public VirtualMap createFromParcel(Parcel in) {
            return new VirtualMap(in);
        }

        @Override
        public VirtualMap[] newArray(int size) {
            return new VirtualMap[size];
        }
    };

    //Constructor

    public VirtualMap (Parcel in) {
        this.trackList = in.readArrayList(MapTrack.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeArray(this.trackList.toArray());
    }

    @Override
    public String toString () {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format(Locale.ENGLISH,"Virtual Map of %d tracks \n", this.trackList.size()));
        for (int i = 0; i< this.trackList.size(); i++)
            builder.append(String.format(Locale.ENGLISH, "TRACK %d: Positions %d; Color %s\n", i+1, this.trackList.get(i).objectList.size(),
                    this.trackList.get(i).trackColor.toString()));
        return builder.toString();
    }

    public static class MapTrack implements Parcelable{

        private LightSensor.Color trackColor;
        private int trackColorEnum;
        private short trackColorIntensity;
        private ArrayList<Boolean> objectList;

        public MapTrack(LightSensor.Color trackColor, short trackColorIntensity, int objectPositionNumber) {
            this.trackColor = trackColor;
            this.trackColorEnum = trackColor.ordinal();
            this.trackColorIntensity = trackColorIntensity;
            this.objectList = new ArrayList<>(objectPositionNumber);
            for (int i = 0; i< objectPositionNumber; i++) {
                objectList.add(false);
            }
        }

        public MapTrack (LightSensor.Color trackColor, int objectPositionNumber) {
            this.trackColor = trackColor;
            this.trackColorEnum = trackColor.ordinal();
            this.trackColorIntensity = 0;
            this.objectList = new ArrayList<>(objectPositionNumber);
            for (int i = 0; i< objectPositionNumber; i++) {
                objectList.add(false);
            }
        }

        public LightSensor.Color getTrackColor() {
            return trackColor;
        }

        public ArrayList<Boolean> getObjectList() {
            return this.objectList;
        }

        public void addEmptyObjectPosition () {
            objectList.add(false);
        }

        public void addObject (int position) {
            objectList.set(position, true);
        }

        public static final Parcelable.Creator <MapTrack> CREATOR = new Parcelable.Creator<MapTrack>() {

            @Override
            public MapTrack createFromParcel(Parcel in) {
                return new MapTrack(in);
            }

            @Override
            public MapTrack[] newArray(int size) {
                return new MapTrack[size];
            }
        };

        //Constructor

        public MapTrack (Parcel in) {
            this.objectList = in.readArrayList(Boolean.class.getClassLoader());
            this.trackColor = LightSensor.Color.values()[in.readInt()];
        }


        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeArray(this.objectList.toArray());
            parcel.writeInt(this.trackColor.ordinal());
        }
    }
}

