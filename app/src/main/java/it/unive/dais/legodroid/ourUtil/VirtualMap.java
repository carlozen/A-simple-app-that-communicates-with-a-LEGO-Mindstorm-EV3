package it.unive.dais.legodroid.ourUtil;

import android.os.Parcel;
import android.os.Parcelable;
import java.io.IOException;
import java.util.ArrayList;
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

    public Short getBlackColorIntensity() {
        return blackColorIntensity;
    }

    public Short getBackgroundColorIntensity() {
        return backgroundColorIntensity;
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

                ArrayList<LightSensor.Color> colorArrayList = new ArrayList<>();
                colorArrayList.add(colorFound);

                int position = scanTrack(api, lightSensorMonitor, blackLineIntensity, colorStop, colorFound, backgroundColorIntensity, trackList);

                RobotOperation.robotRotation(api, -90, Wheel.RIGHT);
                RobotOperation.turnUntilColor(api, lightSensorMonitor, LightSensor.Color.BLACK, Wheel.LEFT, ManualActivity.Direction.BACKWARD);

                backTrack(api, lightSensorMonitor, backgroundColorIntensity, blackLineIntensity, colorArrayList, position);

                //RobotOperation.robotRotation(api, -55, Wheel.RIGHT);
                //RobotOperation.turnUntilColor(api, lightSensorMonitor, LightSensor.Color.BLACK, Wheel.LEFT, ManualActivity.Direction.FORWARD);
                RobotOperation.moveForward (api, 300);
                RobotOperation.turnUntilColor(api, lightSensorMonitor, LightSensor.Color.BLACK, Wheel.LEFT, ManualActivity.Direction.BACKWARD);
            }
        }

        if (trackList.size() == 0)
            throw new RobotException("Sembra che il Robot non abbia corsie per posizionare oggetti. " +
                    "Ogni mappa necessita di almeno una corsia con almeno una posizione.");

        return new VirtualMap(trackList, blackLineIntensity, backgroundColorIntensity);
    }

    public static void backTrack(EV3.Api api, LightSensorMonitor lightSensorMonitor, short blackLineIntensity, short backgroundColorIntensity, ArrayList<LightSensor.Color> colorsToCheck, int positionNumber) throws RobotException, IOException, InterruptedException {

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

            positions--;
            if (positions >= 0) {
                RobotOperation.smallMovementUntilBlackOrWhite(api, lightSensorMonitor, ManualActivity.Direction.FORWARD, ManualActivity.Direction.LEFT, 1);
            }
        }

    }

    private static int scanTrack(EV3.Api api, LightSensorMonitor lightSensorMonitor, short blackLineIntensity, LightSensor.Color colorStop, LightSensor.Color trackColor, short backgroundColorIntensity, ArrayList<MapTrack> trackList) throws RobotException, IOException, InterruptedException {

        Integer positionsNumber = 0;

        ArrayList<LightSensor.Color> colorsToCheck = new ArrayList<>();
        colorsToCheck.add(colorStop);
        colorsToCheck.add(trackColor);

        LightSensor.Color colorFound = null;

        RobotOperation.robotRotation(api, -90, Wheel.RIGHT);

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

                RobotOperation.smallMovementUntilBlackOrWhite(api, lightSensorMonitor, ManualActivity.Direction.FORWARD, ManualActivity.Direction.LEFT, 1);

            } else {
                RobotOperation.checkColor(api, lightSensorMonitor, colorStop, true);
            }
        }

        if (positionsNumber <= 0)
            throw new RobotException("Sembra che questa corsia sia vuota. " +
                    "Ogni corsia necessita di almeno una posizione del suo colore.");
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
        this.backgroundColorIntensity = (short)in.readInt();
        this.blackColorIntensity = (short)in.readInt();
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
        parcel.writeInt(this.blackColorIntensity);
        parcel.writeInt(this.backgroundColorIntensity);
    }

    @Override
    public String toString () {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format(Locale.ENGLISH,"Mappa Virtuale con %d corsie \n", this.trackList.size()));
        for (int i = 0; i< this.trackList.size(); i++) {
            builder.append(String.format(Locale.ENGLISH, "CORSIA %d: Posizioni %d; Colore %s\n", i, this.trackList.get(i).objectList.size(),
                    this.trackList.get(i).trackColor.toString()));
        }
        return builder.toString();
    }

    public boolean save() {
        try {
            String writeValue = MainActivity.mGson.toJson(this);

            Map loadValues = MainActivity.mSettings.getAll();

            MainActivity.mEditor.putString(Integer.valueOf(loadValues.size()).toString(), writeValue);
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

        for (String value : (Iterable<String>) loadValues.values()) {
              virtualMaps.add(MainActivity.mGson.fromJson(value, VirtualMap.class));
        }

        return virtualMaps;
    }

    public static boolean removeSavedMap (int position) {
        try {
            MainActivity.mEditor.remove(Integer.valueOf(position).toString());

            Map loadValues = MainActivity.mSettings.getAll();

            for (int i = position + 1; i< loadValues.size(); i++) {
                MainActivity.mEditor.putString(Integer.valueOf(i-1).toString(),
                        MainActivity.mSettings.getString(Integer.valueOf(i).toString(), ""));
                MainActivity.mEditor.remove(Integer.valueOf(i).toString());
            }

            MainActivity.mEditor.commit();

            return true;
        }
        catch(Exception e)
        {
            return false;
        }
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

