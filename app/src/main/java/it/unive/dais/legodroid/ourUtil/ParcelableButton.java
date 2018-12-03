package it.unive.dais.legodroid.ourUtil;

import android.os.Parcel;
import android.os.Parcelable;

public class ParcelableButton implements Parcelable {
    private int trackNumber;
    private int positionNumber;

    public ParcelableButton (int trackNumber, int positionNumber) {
        this.trackNumber = trackNumber;
        this.positionNumber = positionNumber;
    }

    protected ParcelableButton(Parcel in) {
        trackNumber = in.readInt();
        positionNumber = in.readInt();
    }

    public static final Creator<ParcelableButton> CREATOR = new Creator<ParcelableButton>() {
        @Override
        public ParcelableButton createFromParcel(Parcel in) {
            return new ParcelableButton(in);
        }

        @Override
        public ParcelableButton[] newArray(int size) {
            return new ParcelableButton[size];
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
