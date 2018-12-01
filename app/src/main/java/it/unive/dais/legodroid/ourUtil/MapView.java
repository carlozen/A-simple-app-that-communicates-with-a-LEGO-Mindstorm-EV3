package it.unive.dais.legodroid.ourUtil;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.RelativeLayout;

import java.util.ArrayList;

import it.unive.dais.legodroid.lib.plugs.LightSensor;

public class MapView extends View {

    private Paint mainLinePaint;
    private Paint tracksPaint;
    private Paint redLinePaint;
    private VirtualMap map;
    private RelativeLayout layoutDestination;
    private static ArrayList<ArrayList<PositionButton>> positionButtonList;
    private static ActivityState activityState;
    @Nullable private static PositionButton lastClickedButton;
    private final int LINE_STROKE = 20;

    int left;
    int right;
    int top;
    int bottom;

    public MapView(Context context, VirtualMap map, RelativeLayout layoutDestination,
                   ArrayList<ArrayList<PositionButton>> positionButtonList, ActivityState activityState,
                   @Nullable ParcelableButton lastClickedButton) {
        super(context);

        this.map = map;

        MapView.positionButtonList = positionButtonList;

        this.mainLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.tracksPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.redLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.layoutDestination = layoutDestination;

        MapView.activityState = activityState;

        if (lastClickedButton == null) {
            MapView.lastClickedButton = null;
        }
        else {
            MapView.lastClickedButton = positionButtonList.get(lastClickedButton.getTrackNumber()).
                    get(lastClickedButton.getPositionNumber());
        }

        this.mainLinePaint.setStrokeWidth(LINE_STROKE);
        this.mainLinePaint.setColor(Color.BLACK);

        this.tracksPaint.setStrokeWidth(LINE_STROKE);

        this.redLinePaint.setStrokeWidth(LINE_STROKE);
        this.redLinePaint.setColor(Color.RED);
    }

    public MapView (Context context, VirtualMap map, RelativeLayout layoutDestination, ArrayList<ArrayList<PositionButton>> positionButtonList, ActivityState activityState) {
        this (context, map, layoutDestination, positionButtonList, activityState, null);
    }

     @Override
     protected void onDraw (Canvas canvas) {
        left = getLeft();
        right = getRight() - layoutDestination.getPaddingStart();
        top = getTop();
        bottom = getBottom() - layoutDestination.getPaddingStart();
        canvas.drawLine(left, top, left, bottom, mainLinePaint);
        canvas.drawLine(left - (LINE_STROKE*3)/2, top, left + (LINE_STROKE*3)/2, top, redLinePaint);
        drawTracks(canvas);
        MapView.setButtonListOnClickListeners(positionButtonList, MapView.activityState, MapView.lastClickedButton);
     }

     private void drawTracks (Canvas canvas) {
         final int NUMBER_OF_TRACKS = map.getMapTrackList().size();
         final int TRACK_OFFSET = (top - bottom)/(NUMBER_OF_TRACKS+1);
         int linePosition = bottom + TRACK_OFFSET;

         for (int i = 0; i < NUMBER_OF_TRACKS; i++) {
             this.tracksPaint.setColor(getTrackColor(map.getMapTrackList().get(i).getTrackColor()));
             canvas.drawLine(left - LINE_STROKE/2, linePosition, right, linePosition, tracksPaint);
             canvas.drawLine(right - LINE_STROKE/2, linePosition + (LINE_STROKE*3)/2, right - LINE_STROKE/2, linePosition - (LINE_STROKE*3)/2, redLinePaint);
             initiateTrackButtons(linePosition, i);
             linePosition += TRACK_OFFSET;
         }
     }

    private void initiateTrackButtons(int lineVerticalPosition, int lineNumber) {
        final int NUMBER_OF_OBJECTS = map.getMapTrackList().get(lineNumber).getObjectList().size();
        final int POSITION_OFFSET = (right - left)/(NUMBER_OF_OBJECTS+1);
        int lineHorizontalPosition = left - LINE_STROKE + POSITION_OFFSET;
        for (int i = 0; i < NUMBER_OF_OBJECTS; i++) {
            PositionButton currentButton = MapView.positionButtonList.get(lineNumber).get(i);
            currentButton.setPosition(lineHorizontalPosition, lineVerticalPosition-LINE_STROKE);
            layoutDestination.removeView(currentButton);
            layoutDestination.addView(currentButton);
            lineHorizontalPosition += POSITION_OFFSET;
        }
    }

    private int getTrackColor(LightSensor.Color trackColor) {
        switch (trackColor) {
            case YELLOW: {
                return Color.YELLOW;
            }
            case BLUE: {
                return Color.BLUE;
            }
            case GREEN: {
                return Color.GREEN;
            }

            case BROWN: {
                return Color.rgb(139,69,19);
            }

            default:
                throw new IllegalArgumentException();
        }
    }

    public static void setButtonListOnClickListeners(ArrayList<ArrayList<PositionButton>> positionButtonList, ActivityState activityState, PositionButton positionButton) {
        MapView.activityState = activityState;
        MapView.lastClickedButton = positionButton;
        for (ArrayList<PositionButton> array : positionButtonList) {
            for (PositionButton button : array) {
                button.setAnimation(null);
                button.clearAnimation();
                button.setSingleButtonListener(activityState, positionButton);
            }
        }
    }

    public static PositionButton getLastClickedButton() {
        return MapView.lastClickedButton;
    }

    public static ParcelableButton getLastClickedButtonParcelable() {
        try {
            return new ParcelableButton(MapView.lastClickedButton.getTrackNumber(),
                    MapView.lastClickedButton.getPositionNumber());
        } catch (NullPointerException e) {
            return null;
        }
    }

    public static ActivityState getActivityState() {
        return MapView.activityState;
    }

    public static ArrayList<ArrayList<PositionButton>> getPositionButtonList() {
        return MapView.positionButtonList;
    }

    public enum ActivityState {
        NOTHING_DONE,
        ADD_OBJECT,
        MOVE_OBJECT,
    }
}
