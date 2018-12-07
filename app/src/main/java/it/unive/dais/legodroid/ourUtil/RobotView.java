package it.unive.dais.legodroid.ourUtil;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.widget.RelativeLayout;

import java.util.ArrayList;

public class RobotView extends android.support.v7.widget.AppCompatImageView {

    private int startingPositionX;
    private int startingPositionY;
    private int trackOffset;
    private final ArrayList<Integer> positionOffsetList = new ArrayList<>();

    public RobotView(Context context) {
        super(context);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.width = 45;
        params.height = 45;
        params.alignWithParent= false;
        this.setBackgroundColor(Color.RED);
        this.setLayoutParams(params);
        this.setVisibility(VISIBLE);

    }

    public void setStartingPosition (int x, int y) {
        this.startingPositionX = x;
        this.startingPositionY = y;
        this.setX(startingPositionX);
        this.setY(startingPositionY);
    }

    public int getStartingPositionX() {
        return startingPositionX;
    }

    public int getStartingPositionY() {
        return startingPositionY;
    }

    public void setTrackOffset(int trackOffset) {
        this.trackOffset = trackOffset;
    }

    public void setPositionOffsetOfTrack (int positionOffset) {
        this.positionOffsetList.add(positionOffset);
    }

    public int getPositionOffsetOfTrack (int trackNumber) {
        return positionOffsetList.get(trackNumber);
    }

    public void setPositionByTrack(Integer trackNumber, Integer positionNumber) {
        if (trackNumber != null) {
            this.setY(this.getStartingPositionY() + trackOffset * (trackNumber + 1));
            if (positionNumber != null)
                this.setX(this.getStartingPositionX() - getPositionOffsetOfTrack(trackNumber)*(positionNumber+1));
            else
                this.setX (startingPositionX);
        }

        else {
            this.setY(startingPositionY);
            this.setX(startingPositionX);
        }
    }
}
