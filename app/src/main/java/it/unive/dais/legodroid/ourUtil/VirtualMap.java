package it.unive.dais.legodroid.ourUtil;

import android.os.Parcel;
import android.os.Parcelable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import it.unive.dais.legodroid.code.MainActivity;
import it.unive.dais.legodroid.code.ManualActivity;
import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.plugs.LightSensor;

//TODO
public class VirtualMap implements Parcelable {

    public enum Wheel {
        LEFT,
        RIGHT
    }

    private ArrayList<MapTrack> trackList;
    private Short blackColorIntensity;
    private Short backgroundColorIntensity;


    public VirtualMap (ArrayList<MapTrack> trackList, short blackColorIntensity, short backgroundColorIntensity) {
        this.trackList = trackList;
        this.blackColorIntensity = blackColorIntensity;
        this.backgroundColorIntensity = backgroundColorIntensity;

    }

    //Constructor used when you open a saved map and it needs to find the blackColorIntensity and the backgroundColorIntensity from scratch
    public VirtualMap (ArrayList<MapTrack> trackList) {
        this.trackList = trackList;
        this.blackColorIntensity = null;
        this.backgroundColorIntensity = null;
    }

    public boolean isFull () {
        for (MapTrack track : trackList) {
            for (boolean bool : track.getObjectList()) {
                if (!bool) {
                    return false;
                }
            }
        }
        return true;
    }

    public ArrayList<MapTrack> getMapTrackList () {
        return  this.trackList;
    }

    public static VirtualMap scan (EV3.Api api, LightSensor.Color colorStop, ArrayList<LightSensor.Color> colorsToCheck) throws RobotException, IOException, InterruptedException {

        LightSensorMonitor lightSensorMonitor = new LightSensorMonitor();

        LightSensor.Color colorFound = null;

        ArrayList<MapTrack> trackList = new ArrayList<>();

        //Checks if the Robot is starting from a black line
        RobotOperation.checkColor(api, lightSensorMonitor, LightSensor.Color.BLACK, true);

        short blackLineIntensity = RobotOperation.getReflectedIntensity(api, lightSensorMonitor);

        short backgroundColorIntensity = RobotOperation.getBackgroundColorIntensity (api, lightSensorMonitor);

        while (colorFound != colorStop) {

                colorFound = RobotOperation.followLine(api,
                        lightSensorMonitor,
                        ManualActivity.Direction.FORWARD,
                        LightSensor.Color.BLACK,
                        blackLineIntensity,
                        backgroundColorIntensity,
                        colorsToCheck
                );

            if (colorFound != colorStop) {

                int position = scanTrack(api, lightSensorMonitor, blackLineIntensity, colorStop, backgroundColorIntensity, trackList);
                backTrack(api, lightSensorMonitor, backgroundColorIntensity, blackLineIntensity, colorFound, position);

            }
        }

        if (trackList.size() == 0)
            throw new RobotException("It seems your map has no tracks. \n" +
                    "Every map needs at least one track with at least one object position.");

        return new VirtualMap(trackList, blackLineIntensity, backgroundColorIntensity);
    }

    private static void backTrack(EV3.Api api, LightSensorMonitor lightSensorMonitor, short blackLineIntensity, short backgroundColorIntensity, LightSensor.Color trackColor, int positionNumber) throws RobotException, IOException, InterruptedException {

        ArrayList<LightSensor.Color> colorsToCheck = new ArrayList<>();
        colorsToCheck.add(trackColor);

        LightSensor.Color colorFound;

        Integer positions = positionNumber;

        RobotOperation.smallMovementUntilColor(api, lightSensorMonitor, LightSensor.Color.BLACK, ManualActivity.Direction.BACKWARD, ManualActivity.Direction.RIGHT);

        while(positions >= 0) {
            colorFound = RobotOperation.followLine(api,
                    lightSensorMonitor,
                    ManualActivity.Direction.BACKWARD,
                    LightSensor.Color.BLACK,
                    blackLineIntensity,
                    backgroundColorIntensity,
                    colorsToCheck
            );

            if(colorFound == trackColor) {
                if (positions >= 1) {
                    RobotOperation.smallMovementUntilColor(api, lightSensorMonitor, LightSensor.Color.BLACK, ManualActivity.Direction.BACKWARD, ManualActivity.Direction.RIGHT);
                } else {
                    RobotOperation.turnUntilColor(api, lightSensorMonitor, LightSensor.Color.BLACK, Wheel.RIGHT, ManualActivity.Direction.BACKWARD);
                }
                positions--;
            }
        }


    }

    private static int scanTrack(EV3.Api api, LightSensorMonitor lightSensorMonitor, short blackLineIntensity, LightSensor.Color colorStop, short backgroundColorIntensity, ArrayList<MapTrack> trackList) throws RobotException, IOException, InterruptedException {

        LightSensor.Color trackColor = RobotOperation.getReflectedColor(api, lightSensorMonitor);

        Integer positionsNumber = 0;

        ArrayList<LightSensor.Color> colorsToCheck = new ArrayList<>();
        colorsToCheck.add(colorStop);
        colorsToCheck.add(trackColor);

        LightSensor.Color colorFound = null;

        RobotOperation.robotRotation(api, -70, Wheel.RIGHT);

        RobotOperation.turnUntilColor(api, lightSensorMonitor, LightSensor.Color.BLACK, Wheel.LEFT, ManualActivity.Direction.FORWARD);



        while (colorFound != colorStop) {
            colorFound = RobotOperation.followLine(api,
                    lightSensorMonitor,
                    ManualActivity.Direction.FORWARD,
                    LightSensor.Color.BLACK,
                    blackLineIntensity,
                    backgroundColorIntensity,
                    colorsToCheck
            );

            if (colorFound == trackColor) {

                positionsNumber++;

                RobotOperation.smallMovementUntilColor(api, lightSensorMonitor, LightSensor.Color.BLACK, ManualActivity.Direction.FORWARD, ManualActivity.Direction.LEFT);

            } else {
                RobotOperation.checkColor(api, lightSensorMonitor, colorStop, true);
            }
        }

        if (positionsNumber < 0)
            throw new RobotException("It seems this track is empty. \n" +
                    "Every track needs at least a black object position.");
        else
            trackList.add(trackList.size(), new MapTrack(trackColor, positionsNumber));

        return positionsNumber;
    }

    //Parcelable method
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

    //Parcelable Constructor
    private VirtualMap (Parcel in) {
        this.trackList = in.readArrayList(MapTrack.class.getClassLoader());
    }

    //Useless method for our needs, but needs implementation.
    @Override
    public int describeContents() {
        return 0;
    }

    //Parcelable method
    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeArray(this.trackList.toArray());
    }

    @Override
    public String toString () {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format(Locale.ENGLISH,"Virtual Map of %d tracks \n", this.trackList.size()));
        for (int i = 0; i< this.trackList.size(); i++) {
            builder.append(String.format(Locale.ENGLISH, "TRACK %d: Positions %d; Color %s\n", i, this.trackList.get(i).objectList.size(),
                    this.trackList.get(i).trackColor.toString()));
            for (int j = 0; j<this.trackList.get(i).getObjectList().size();j++) {
                builder.append(String.format(Locale.ENGLISH,"\t Position %d: %b\n", j, this.trackList.get(i).getObjectList().get(j)));
            }
        }
        return builder.toString();
    }

    public boolean save(String mapName) {
        try {
       //     String writeValue = MainActivity.mGson.toJson(this);
        //    MainActivity.mEditor.putString(mapName, writeValue);
            MainActivity.mEditor.commit();
            return true;
        }
        catch(Exception e)
        {
            return false;
        }
    }

    public static ArrayList<VirtualMap> getSavedMaps() {
        ArrayList<VirtualMap> virtualMaps = new ArrayList<>();

        Map loadValues = MainActivity.mSettings.getAll();

        Iterator<String> it = loadValues.values().iterator();
        while(it.hasNext()){
            String value = it.next();
          //  virtualMaps.add(MainActivity.mGson.fromJson(value, VirtualMap.class));
        }

        return virtualMaps;
    }

    public static class MapTrack implements Parcelable{

        private LightSensor.Color trackColor;
        private short trackColorIntensity;
        private ArrayList<Boolean> objectList;

        public MapTrack(LightSensor.Color trackColor, int objectPositionNumber) {
            this.trackColor = trackColor;
            this.objectList = new ArrayList<>(objectPositionNumber);
            for (int i = 0; i< objectPositionNumber; i++) {
                objectList.add(false);
            }
        }

        public MapTrack(LightSensor.Color trackColor, short trackColorIntensity, int objectPositionNumber) {
            this.trackColor = trackColor;
            this.trackColorIntensity = trackColorIntensity;
            this.objectList = new ArrayList<>(objectPositionNumber);
            for (int i = 0; i< objectPositionNumber; i++) {
                objectList.add(false);
            }
        }

        //Parcelable method
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

        //Parcelable Constructor
        public MapTrack (Parcel in) {
            this.objectList = in.readArrayList(Boolean.class.getClassLoader());
            this.trackColor = LightSensor.Color.values()[in.readInt()];
        }

        //Useless method for our needs, but needs implementation.
        @Override
        public int describeContents() {
            return 0;
        }

        //Parcelable method
        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeArray(this.objectList.toArray());
            parcel.writeInt(this.trackColor.ordinal());
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


    }
}

