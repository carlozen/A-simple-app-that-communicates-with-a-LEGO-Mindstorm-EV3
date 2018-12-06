package it.unive.dais.legodroid.code;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.RelativeLayout;

import java.util.ArrayList;

import it.unive.dais.legodroid.R;
import it.unive.dais.legodroid.ourUtil.VirtualMapActivityUIManager;
import it.unive.dais.legodroid.ourUtil.VirtualMapView;
import it.unive.dais.legodroid.ourUtil.ParcelablePositionButton;
import it.unive.dais.legodroid.ourUtil.PositionButton;
import it.unive.dais.legodroid.ourUtil.VirtualMap;

public class VirtualMapActivity extends AppCompatActivity {

    private VirtualMapActivityUIManager UIManager;

    VirtualMapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_virtual_map);

        VirtualMap map;
        VirtualMapActivityUIManager.ActivityState activityState;
        ParcelablePositionButton lastClickedButton;
        boolean hasRobotOperationStarted;

        Bundle data = getIntent().getExtras();

        if (savedInstanceState==null) {
            map = (VirtualMap) (data != null ? data.getParcelable("map") : null);
            activityState = VirtualMapActivityUIManager.ActivityState.NOTHING_DONE;
            lastClickedButton = null;
            hasRobotOperationStarted = false;
        }
        else {
            map = savedInstanceState.getParcelable("saved_map");
            int currentStateId = savedInstanceState.getInt("activity_state");
            activityState = VirtualMapActivityUIManager.ActivityState.values()[currentStateId];
            try {
                lastClickedButton = savedInstanceState.getParcelable("last_button");
            } catch (NullPointerException e) {
                lastClickedButton = null;
            }
            hasRobotOperationStarted = true;
        }

        UIManager = VirtualMapActivityUIManager.generate(this, activityState ,
                lastClickedButton, hasRobotOperationStarted, map, findViewById(R.id.dialogue_layout),
                findViewById(R.id.robot_view));

        RelativeLayout mapLayout = findViewById(R.id.map_layout);

        mapView = new VirtualMapView(mapLayout, UIManager);
        mapLayout.addView(mapView);

        UIManager.setDialogueLayout();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        VirtualMapActivityUIManager.AsyncRobotTask asyncRobotTask = UIManager.getAsyncRobotTask();
        if (asyncRobotTask != null) {
            asyncRobotTask.cancel(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        RelativeLayout relativeLayout = mapView.getLayoutDestination();
        for (ArrayList<PositionButton> arrayList : UIManager.getPositionButtonList()) {
            for (PositionButton button : arrayList) {
                relativeLayout.removeView(button);
                relativeLayout.addView(button);
            }
        }
        UIManager.resetAllButtonsListeners();
    }

    @Override
    protected void onPause() {
        UIManager.unsetAllButtons();
        super.onPause();
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("saved_map", UIManager.getVirtualMap());
        outState.putInt("activity_state", UIManager.getActivityState().ordinal());
        outState.putParcelable("last_button", UIManager.getLastClickedButtonParcelable());
        super.onSaveInstanceState(outState);
    }

}
