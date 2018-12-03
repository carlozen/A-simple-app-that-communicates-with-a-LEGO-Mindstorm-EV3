package it.unive.dais.legodroid.ourUtil;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.RelativeLayout;

import java.util.ArrayList;

import it.unive.dais.legodroid.code.VirtualMapActivity;
import it.unive.dais.legodroid.lib.plugs.LightSensor;

public class VirtualMapView extends View {

    private Paint mainLinePaint;
    private Paint tracksPaint;
    private Paint redLinePaint;
    private VirtualMap map;
    private RelativeLayout layoutDestination;
    private VirtualMapActivityUIManager UIManager;
    private /*static*/ ArrayList<ArrayList<PositionButton>> positionButtonList;
    private final int LINE_STROKE = 20;

    int left;
    int right;
    int top;
    int bottom;

    public VirtualMapView(RelativeLayout layoutDestination, VirtualMapActivityUIManager UIManager) {
        super(UIManager.getContext());

        this.map = UIManager.getVirtualMap();

        this.positionButtonList = UIManager.getPositionButtonList();

        this.mainLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.tracksPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.redLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.layoutDestination = layoutDestination;

        this.mainLinePaint.setStrokeWidth(LINE_STROKE);
        this.mainLinePaint.setColor(Color.BLACK);

        this.tracksPaint.setStrokeWidth(LINE_STROKE);

        this.redLinePaint.setStrokeWidth(LINE_STROKE);
        this.redLinePaint.setColor(Color.RED);
    }

    /*
    public VirtualMapView(Context context, VirtualMap map, RelativeLayout layoutDestination,
                          ArrayList<ArrayList<PositionButton>> positionButtonList, VirtualMapActivity.ActivityState activityState,
                          @Nullable ParcelablePositionButton lastClickedButton) {
        super(context);

        this.map = map;

        VirtualMapView.positionButtonList = positionButtonList;

        this.mainLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.tracksPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.redLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.layoutDestination = layoutDestination;

        this.mainLinePaint.setStrokeWidth(LINE_STROKE);
        this.mainLinePaint.setColor(Color.BLACK);

        this.tracksPaint.setStrokeWidth(LINE_STROKE);

        this.redLinePaint.setStrokeWidth(LINE_STROKE);
        this.redLinePaint.setColor(Color.RED);
    }

    */

     @Override
     protected void onDraw (Canvas canvas) {
        left = getLeft();
        right = getRight() - layoutDestination.getPaddingStart();
        top = getTop();
        bottom = getBottom() - layoutDestination.getPaddingStart();
        canvas.drawLine(left, top, left, bottom, mainLinePaint);
        canvas.drawLine(left - (LINE_STROKE*3)/2, top, left + (LINE_STROKE*3)/2, top, redLinePaint);
        drawTracks(canvas);
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
            PositionButton currentButton = positionButtonList.get(lineNumber).get(i);
            currentButton.setPosition(lineHorizontalPosition + layoutDestination.getPaddingStart(), lineVerticalPosition - LINE_STROKE + layoutDestination.getPaddingStart()-1);
            lineHorizontalPosition += POSITION_OFFSET;
        }
    }

    public RelativeLayout getLayoutDestination () {
        return this.layoutDestination;
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
}