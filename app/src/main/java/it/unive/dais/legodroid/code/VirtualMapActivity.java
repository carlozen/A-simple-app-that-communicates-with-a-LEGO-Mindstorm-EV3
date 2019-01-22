package it.unive.dais.legodroid.code;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.RelativeLayout;

import java.io.IOException;
import java.util.ArrayList;

import it.unive.dais.legodroid.R;
import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.util.Prelude;
import it.unive.dais.legodroid.ourUtil.AsyncRobotTask;
import it.unive.dais.legodroid.ourUtil.Motor;
import it.unive.dais.legodroid.ourUtil.RobotException;
import it.unive.dais.legodroid.ourUtil.RobotOperation;
import it.unive.dais.legodroid.ourUtil.RobotView;
import it.unive.dais.legodroid.ourUtil.VirtualMapUI;
import it.unive.dais.legodroid.ourUtil.VirtualMapView;
import it.unive.dais.legodroid.ourUtil.PositionButton;
import it.unive.dais.legodroid.ourUtil.VirtualMap;

public class VirtualMapActivity extends AppCompatActivity {

    private VirtualMapUI UIManager;
    private float virtualRobotXPos = -1;
    private float virtualRobotYPos = -1;

    VirtualMapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_virtual_map);

        VirtualMap map;

        Bundle data = getIntent().getExtras();

        if (savedInstanceState==null) {
            map = (VirtualMap) (data != null ? data.getParcelable("map") : null);
        }
        else {
            map = savedInstanceState.getParcelable("saved_map");
        }

        RobotView robotView = new RobotView(this);

        UIManager = VirtualMapUI.generate(this, VirtualMapUI.ActivityState.NOTHING_DONE,
                null,  map, findViewById(R.id.dialogue_layout), robotView);

        RelativeLayout mapLayout = findViewById(R.id.map_layout);

        mapView = new VirtualMapView(mapLayout, UIManager);
        mapLayout.addView(mapView);
        mapLayout.addView(robotView);

        UIManager.setDialogueLayout();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AsyncRobotTask asyncRobotTask = UIManager.getAsyncRobotTask();
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
    public void onBackPressed() {
        AsyncRobotTask asyncRobotTask = UIManager.getAsyncRobotTask();
        if (asyncRobotTask == null || asyncRobotTask.getStatus() != AsyncTask.Status.RUNNING) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onPause() {
        UIManager.unsetAllButtons();
        virtualRobotXPos = UIManager.getRobotView().getX();
        virtualRobotYPos = UIManager.getRobotView().getY();
        super.onPause();
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("saved_map", UIManager.getVirtualMap());
        super.onSaveInstanceState(outState);
    }

}
