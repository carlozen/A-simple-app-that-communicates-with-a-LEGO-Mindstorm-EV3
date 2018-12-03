package it.unive.dais.legodroid.ourUtil;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.RelativeLayout;

public class PositionButton extends android.support.v7.widget.AppCompatButton {

    private VirtualMap map;
    private int trackNumber;
    private int positionNumber;
    private RelativeLayout dialogueLayout;

    public PositionButton(VirtualMap map, Context context, RelativeLayout dialogueLayout,
                          int trackNumber, int positionNumber) {
        super(context);

        this.map = map;
        this.dialogueLayout = dialogueLayout;

        this.trackNumber = trackNumber;
        this.positionNumber = positionNumber;

        if (isOccupied())
            this.setBackgroundColor(Color.BLACK);
        else
            this.setBackgroundColor(Color.WHITE);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.width = 40;
        params.height = 40;
        params.alignWithParent= false;
        this.setLayoutParams(params);

        setSingleButtonListener(MapView.ActivityState.NOTHING_DONE, null);
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
        if (isOccupied())
            this.setBackgroundColor(Color.BLACK);
        else
            this.setBackgroundColor(Color.WHITE);

    }

    protected void setSingleButtonListener (MapView.ActivityState activityState, PositionButton referencedButton) {
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
        }
    }

    private void setListenerOnNothingDoneState () {
        if (this.isOccupied()) {
            this.setOnClickListener(new ButtonsGeneralListener(this, dialogueLayout));
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
            this.setFadingAnimation();
            this.setOnClickListener(new ButtonsGeneralListener(this, dialogueLayout));
        }
    }

    private void setListenerOnMoveState(@NonNull PositionButton referencedButton) {
        if (this.isOccupied()) {
            this.setClickable(false);
            if (this.trackNumber == referencedButton.trackNumber &&
                    this.positionNumber == referencedButton.positionNumber) {
                this.setGlowingAnimation();
            }
        }

        else {
            this.setFadingAnimation();
            this.setOnClickListener(new ButtonsGeneralListener(this, dialogueLayout));
        }
    }

    private void setFadingAnimation() {

        Animation fadeIn =  new AlphaAnimation(0.0f,1.0f);
        Animation fadeOut =  new AlphaAnimation(1.0f,0.0f);
        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                animation.setStartOffset(0);
                animation.setDuration(1200);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                startAnimation(fadeOut);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                animation.setStartOffset(1000);
                animation.setDuration(1200);

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                startAnimation(fadeIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        startAnimation(fadeOut);

    }

    private void setGlowingAnimation() {

        setBackgroundColor(Color.RED);
        Animation redIn =  new AlphaAnimation(0.7f,1.0f);
        Animation redOut =  new AlphaAnimation(1.0f,0.7f);
        redIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                animation.setStartOffset(0);
                animation.setDuration(1200);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                startAnimation(redOut);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        redOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                animation.setStartOffset(1000);
                animation.setDuration(1200);

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                startAnimation(redIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        startAnimation(redOut);

    }

    public int getTrackNumber() {
        return trackNumber;
    }

    public int getPositionNumber() {
        return positionNumber;
    }
}
