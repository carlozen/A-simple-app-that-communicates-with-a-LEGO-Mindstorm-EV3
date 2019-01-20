package it.unive.dais.legodroid.ourUtil;

import android.content.Context;
import android.graphics.Color;
import android.widget.RelativeLayout;

import java.util.ArrayList;

import it.unive.dais.legodroid.R;

public class RobotView extends android.support.v7.widget.AppCompatImageView {

    private int startingPositionX;
    private int startingPositionY;
    private int trackOffset;
    private final ArrayList<Integer> positionOffsetList = new ArrayList<>();
    private boolean positionInitiated = false;

    public RobotView(Context context) {
        super(context);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.width = 80;
        params.height = 80;
        params.alignWithParent= false;
        this.setBackgroundResource(R.drawable.robot_icon);
        //this.setBackgroundColor(Color.RED);
        this.setLayoutParams(params);
        this.setVisibility(VISIBLE);

    }

    public void initiatePosition () {
        this.positionInitiated = true;
    }

    public boolean isInitiated () {
        return positionInitiated;
    }

    public void setStartingPosition (int x, int y) {
        this.startingPositionX = x;
        this.startingPositionY = y;
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
