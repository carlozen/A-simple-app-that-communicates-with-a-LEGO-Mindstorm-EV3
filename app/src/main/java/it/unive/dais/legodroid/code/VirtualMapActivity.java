package it.unive.dais.legodroid.code;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import it.unive.dais.legodroid.R;
import it.unive.dais.legodroid.ourUtil.MapView;
import it.unive.dais.legodroid.ourUtil.ParcelableButton;
import it.unive.dais.legodroid.ourUtil.PositionButton;
import it.unive.dais.legodroid.ourUtil.VirtualMap;

public class VirtualMapActivity extends AppCompatActivity {

    VirtualMap map;
    MapView.ActivityState currentState;
    static ArrayList<ArrayList<PositionButton>> positionButtonList;
    ParcelableButton lastClickedButton;
    MapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_virtual_map);

        Bundle data = getIntent().getExtras();

        if (savedInstanceState==null) {
            map = (VirtualMap) (data != null ? data.getParcelable("map") : null);
            currentState = MapView.ActivityState.NOTHING_DONE;
            lastClickedButton = null;
        }
        else {
            map = savedInstanceState.getParcelable("saved_map");
            int currentStateId = savedInstanceState.getInt("activity_state");
            currentState = MapView.ActivityState.values()[currentStateId];
            try {
                lastClickedButton = savedInstanceState.getParcelable("last_button");
            } catch (NullPointerException e) {
                lastClickedButton = null;
            }
        }
    }

    @Override
    protected void onResume() {
        initiateAll();
        super.onResume();
    }

    @Override
    protected void onPause() {
        currentState = MapView.getActivityState();
        lastClickedButton = MapView.getLastClickedButtonParcelable();
        ((RelativeLayout)findViewById(R.id.map_layout)).removeAllViews();
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("saved_map", map);
        outState.putInt("activity_state", MapView.getActivityState().ordinal());
        outState.putParcelable("last_button", MapView.getLastClickedButtonParcelable());
        super.onSaveInstanceState(outState);
    }

    public void initiateAll () {
        positionButtonList= new ArrayList<>();

        for (int i = 0; i< map.getMapTrackList().size(); i++) {
            positionButtonList.add(new ArrayList<>());
            for (int j = 0; j<map.getMapTrackList().get(i).getObjectList().size(); j++) {
                positionButtonList.get(i).add(new PositionButton(map, this,
                        findViewById(R.id.dialogue_layout), i, j));
            }
        }

        RelativeLayout mapLayout = findViewById(R.id.map_layout);

        mapView = new MapView(this, map, mapLayout, positionButtonList, this.currentState, this.lastClickedButton);
        mapLayout.addView(mapView);

        setDialogueLayout(this, findViewById(R.id.dialogue_layout));
    }

    public static void setDialogueLayout(Context context, RelativeLayout dialogueLayout) {
        Button addObjectButton = dialogueLayout.findViewById(R.id.addObject);
        TextView operationDescriptionView = dialogueLayout.findViewById(R.id.operation_description);

        switch (MapView.getActivityState()) {
            case ADD_OBJECT: {
                operationDescriptionView.setText("Select one of the blank spaces to add an object.");
                break;
            }
            case NOTHING_DONE: {
                operationDescriptionView.setText("Add an object or press on an existing one to move it");
                addObjectButton.setVisibility(View.VISIBLE);
                addObjectButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        MapView.setButtonListOnClickListeners(positionButtonList, MapView.ActivityState.ADD_OBJECT, null);
                        addObjectButton.setVisibility(View.GONE);
                        operationDescriptionView.setText("Select one of the blank spaces to add an object.");
                    }
                });
                break;
            }
            case MOVE_OBJECT: {
                addObjectButton.setVisibility(View.GONE);
                operationDescriptionView.setText("Select one of the blank spaces to move the selected object.");
                break;
            }
        }
    }
}
