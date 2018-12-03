package it.unive.dais.legodroid.ourUtil;

import android.os.Parcel;
import android.os.Parcelable;

public class ParcelablePositionButton implements Parcelable {
    private int trackNumber;
    private int positionNumber;

    public ParcelablePositionButton(int trackNumber, int positionNumber) {
        this.trackNumber = trackNumber;
        this.positionNumber = positionNumber;
    }

    protected ParcelablePositionButton(Parcel in) {
        trackNumber = in.readInt();
        positionNumber = in.readInt();
    }

    public static final Creator<ParcelablePositionButton> CREATOR = new Creator<ParcelablePositionButton>() {
        @Override
        public ParcelablePositionButton createFromParcel(Parcel in) {
            return new ParcelablePositionButton(in);
        }

        @Override
        public ParcelablePositionButton[] newArray(int size) {
            return new ParcelablePositionButton[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(trackNumber);
        parcel.writeInt(positionNumber);
    }

    public int getTrackNumber() {
        return trackNumber;
    }

    public int getPositionNumber() {
        return positionNumber;
    }
}
