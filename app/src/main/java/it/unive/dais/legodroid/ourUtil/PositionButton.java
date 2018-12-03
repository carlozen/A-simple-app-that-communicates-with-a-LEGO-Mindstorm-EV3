package it.unive.dais.legodroid.ourUtil;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.RelativeLayout;

public class PositionButton extends android.support.v7.widget.AppCompatButton {

    private VirtualMapActivityUIManager UIManager;
    private VirtualMap map;
    private int trackNumber;
    private int positionNumber;
    final private GradientDrawable shape = new GradientDrawable();


    public PositionButton(Context context, VirtualMapActivityUIManager UIManager,
                          int trackNumber, int positionNumber) {
        super(context);

        this.UIManager = UIManager;
        this.map = UIManager.getVirtualMap();

        this.trackNumber = trackNumber;
        this.positionNumber = positionNumber;
        this.setPadding(30,30,30,30);
        shape.setShape(GradientDrawable.OVAL);

        if (isOccupied())
            shape.setColor(Color.BLACK);
        else
            shape.setColor(Color.WHITE);

        shape.setStroke(3, Color.BLACK);

        this.setBackground(shape);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.width = 45;
        params.height = 45;
        params.alignWithParent= false;
        this.setLayoutParams(params);

        setSingleButtonListener(VirtualMapActivityUIManager.ActivityState.NOTHING_DONE, null);
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
            shape.setColor(Color.BLACK);
            this.setBackground(shape);
        }
        else {
            shape.setColor(Color.WHITE);
            this.setBackground(shape);
        }
    }

    public void setSingleButtonListener (VirtualMapActivityUIManager.ActivityState activityState, PositionButton referencedButton) {
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
                AnimationGenerator.setGlowingAnimation(this, shape);
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
}