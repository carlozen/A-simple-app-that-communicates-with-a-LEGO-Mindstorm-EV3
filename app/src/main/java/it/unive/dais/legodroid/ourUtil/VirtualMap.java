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

    //Constructor used when you open a saved map and it needs to find the blackColorIntensity and the backgroundColorIntensity from scratch
    public VirtualMap (ArrayList<MapTrack> trackList) {
        this.trackList = trackList;
        this.blackColorIntensity = null;
        this.backgroundColorIntensity = null;
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
    public VirtualMap (Parcel in) {
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

    public ArrayList<MapTrack> getMapTrackList () {
        return  this.trackList;
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

        public short getTrackColorIntensity() {
            return this.trackColorIntensity;
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

