package it.unive.dais.legodroid.ourUtil;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Icon;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.Locale;

import it.unive.dais.legodroid.R;
import it.unive.dais.legodroid.code.VirtualMapActivity;
import it.unive.dais.legodroid.lib.plugs.LightSensor;

public class DrawView extends View {

    private Paint mainLinePaint;
    private Paint tracksPaint;
    private VirtualMap map;
    private Context context;
    private RelativeLayout layoutDestination;
    private final ArrayList<PositionButton> positionButtonList = new ArrayList<>();

    int left = getLeft();
    int right = getRight();
    int top = getTop();
    int bottom = getBottom();


    public DrawView(Context context, VirtualMap map, RelativeLayout layoutDestination) {
        super(context);
        this.map = map;
        this.mainLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.tracksPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.context = context;
        this.layoutDestination = layoutDestination;

        this.mainLinePaint.setStrokeWidth(20);
        this.mainLinePaint.setColor(Color.BLACK);

        this.tracksPaint.setStrokeWidth(20);

        this.mainLinePaint.setStrokeWidth(10);
    }

     @Override
     protected void onDraw (Canvas canvas) {
        left = getLeft();
        right = getRight();
        top = getTop();
        bottom = getBottom();
        canvas.drawLine(left, top, left, bottom, mainLinePaint);
        drawTracks(canvas);
     }

     private void drawTracks (Canvas canvas) {
         final ArrayList<VirtualMap.MapTrack> trackList = map.getMapTrackList();
         final int NUMBER_OF_TRACKS = trackList.size();
         final int TRACK_OFFSET = (top - bottom)/(NUMBER_OF_TRACKS+1);
         int linePosition = bottom + TRACK_OFFSET;

         for (int i = 0; i < NUMBER_OF_TRACKS; i++) {
             this.tracksPaint.setColor(getTrackColor(trackList.get(i).getTrackColor()));
             canvas.drawLine(left, linePosition, right, linePosition, tracksPaint);
             drawTrackObjects (canvas, trackList.get(i), linePosition, i);
             linePosition += TRACK_OFFSET;
         }
     }

    private void drawTrackObjects(Canvas canvas, VirtualMap.MapTrack mapTrack, int lineVerticalPosition, int lineNumber) {
        final ArrayList<Boolean> positionList = mapTrack.getObjectList();
        final int NUMBER_OF_OBJECTS = positionList.size();
        final int POSITION_OFFSET = (right - left)/(NUMBER_OF_OBJECTS+1);
        int lineHorizontalPosition = left + POSITION_OFFSET;
        for (int i = 0; i < NUMBER_OF_OBJECTS; i++) {
            PositionButton positionButton = new PositionButton (context,lineHorizontalPosition, lineVerticalPosition,
                    lineNumber, i, positionList.get(i));
            layoutDestination.addView(positionButton);
            lineHorizontalPosition += POSITION_OFFSET;
        }
    }

    public ArrayList<PositionButton> getPositionButtonList() {
        return this.positionButtonList;
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
            default:
                throw new IllegalArgumentException();
        }
    }

    public static class PositionButton extends android.support.v7.widget.AppCompatButton {

        private boolean isOccupied;
        private int trackNumber;
        private int positionNumber;

        public PositionButton(Context context, int lineHorizontalPosition, int lineVerticalPosition, int trackNumber, int positionNumber, boolean value) {
            super(context);

            this.trackNumber = trackNumber;
            this.positionNumber = positionNumber;
            this.isOccupied = value;

            this.setY(lineVerticalPosition - 20);
            this.setX(lineHorizontalPosition);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.width = 40;
            params.height = 40;
            params.alignWithParent= false;
            this.setLayoutParams(params);
            if (value) {
                this.setBackgroundColor(Color.BLACK);
            }
            else {
                this.setBackgroundColor(Color.WHITE);
            }

            this.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    Snackbar snackbar = Snackbar.make(view,
                            String.format(Locale.ENGLISH,
                                    "Linea numero: %d\nPosizione numero: %d\nOccupata: %b", trackNumber, positionNumber, isOccupied),
                            Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
            });

        }
    }

}
