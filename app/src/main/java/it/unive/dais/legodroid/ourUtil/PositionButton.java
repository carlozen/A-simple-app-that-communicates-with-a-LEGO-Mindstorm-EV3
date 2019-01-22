package it.unive.dais.legodroid.ourUtil;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.widget.RelativeLayout;

import it.unive.dais.legodroid.lib.plugs.LightSensor;

public class PositionButton extends android.support.v7.widget.AppCompatButton {

    private VirtualMapUI UIManager;
    private VirtualMap map;
    private int trackNumber;
    private int positionNumber;

    public PositionButton(Context context, VirtualMapUI UIManager,
                          int trackNumber, int positionNumber) {
        super(context);

        this.UIManager = UIManager;
        this.map = UIManager.getVirtualMap();

        this.trackNumber = trackNumber;
        this.positionNumber = positionNumber;
        this.setPadding(30,30,30,30);

        if (isOccupied())
            this.setBackgroundColor(Color.BLACK);
        else
            this.setBackgroundColor(Color.WHITE);



        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.width = 45;
        params.height = 45;
        params.alignWithParent= false;
        this.setLayoutParams(params);

        setSingleButtonListener(VirtualMapUI.ActivityState.NOTHING_DONE, null);
    }

    public boolean isOccupied(){
        return map.getMapTrackList().get(this.trackNumber).getObjectList().get(this.positionNumber);
    }

    public void setPosition (int x, int y) {
        this.setY(y);
        this.setX(x);
    }

    public void changeOccupiedState () {
        map.getMapTrackList().get(trackNumber).getObjectList().set(positionNumber,
                !map.getMapTrackList().get(trackNumber).getObjectList().get(positionNumber));
        if (isOccupied()) {
            this.setBackgroundColor(Color.BLACK);
        }
        else {
            this.setBackgroundColor(Color.WHITE);
        }
    }

    public int getBackgroundColor () {
        return ((ColorDrawable)getBackground()).getColor();
    }

    public void setSingleButtonListener (VirtualMapUI.ActivityState activityState, PositionButton referencedButton) {
        switch (activityState) {
            case NOTHING_DONE: {
                setListenerOnNothingDoneState();
                break;
            }

            case ADD_OBJECT: {
                setListenerOnAddObjectState();
                break;
            }
            case MOVE_OBJECT: {
                setListenerOnMoveState (referencedButton);
                break;
            }
            default: {
                setListenerOnRobotOperationState ();
                break;
            }
        }
    }

    private void setListenerOnRobotOperationState() {
        this.setClickable(false);
    }

    private void setListenerOnNothingDoneState () {
        if (this.isOccupied()) {
            this.setOnClickListener(new ButtonsGeneralListener(this, UIManager));
        }

        else {
            this.setClickable(false);
        }
    }

    private void setListenerOnAddObjectState() {
        if (this.isOccupied()) {
            this.setClickable(false);
        }

        else {
            AnimationGenerator.setFadingAnimation(this);
            this.setOnClickListener(new ButtonsGeneralListener(this, UIManager));
        }
    }

    private void setListenerOnMoveState(@NonNull PositionButton referencedButton) {
        if (this.isOccupied()) {
            this.setClickable(false);
            if (this.trackNumber == referencedButton.trackNumber &&
                    this.positionNumber == referencedButton.positionNumber) {
                AnimationGenerator.setGlowingAnimation(this);
            }
        }

        else {
            AnimationGenerator.setFadingAnimation(this);
            this.setOnClickListener(new ButtonsGeneralListener(this, UIManager));
        }
    }

    public int getTrackNumber() {
        return trackNumber;
    }

    public int getPositionNumber() {
        return positionNumber;
    }


    public VirtualMapUI getUIManager() {
        return UIManager;
    }
}
