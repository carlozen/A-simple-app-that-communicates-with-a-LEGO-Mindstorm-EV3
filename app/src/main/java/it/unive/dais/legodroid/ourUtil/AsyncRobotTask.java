package it.unive.dais.legodroid.ourUtil;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.view.View;

import it.unive.dais.legodroid.code.PopupErrorActivity;
import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.plugs.LightSensor;

@SuppressLint("StaticFieldLeak")
public final class AsyncRobotTask extends AsyncTask<Void, Integer, Void> {

    private EV3.Api api;
    private VirtualMapUI UIManager;
    private PositionButton buttonToMoveObjFrom = null;
    private RobotView robotView;

    AsyncRobotTask(EV3.Api api, VirtualMapUI UIManager,
                   PositionButton positionButton) {
        this.api = api;
        this.UIManager = UIManager;
        this.buttonToMoveObjFrom = positionButton;
        this.robotView = UIManager.getRobotView();
    }

    @Override
    protected void onPreExecute() {
        if (robotView.getVisibility()==View.GONE) {
            robotView.setVisibility(View.VISIBLE);
        }
        robotView.setPositionByTrack(null,null);
    }

    public void moveToTrack(int track) {
        publishProgress(track, null);
    }

    public void moveToBeginning () {
        publishProgress(null, null);
    }

    public void moveToPositionOnTrack (int track, int position) {
        publishProgress(track, position);
    }

    @Override
    protected Void doInBackground(Void[] voids) {
        try {
            switch (UIManager.getActivityState()) {
                case ROBOT_ADDING_OBJECT: {
                    RobotOperation.addObject(api, this, UIManager.getVirtualMap().getBackgroundColorIntensity(),
                            UIManager.getVirtualMap().getBlackColorIntensity(), UIManager.getLastClickedButton());
                    break;
                }

                //TODO: cambia all'interno della mappa
                case ROBOT_MOVING_OBJECT: {
                    RobotOperation.moveObject(api, this, UIManager.getVirtualMap().getBackgroundColorIntensity(),
                            UIManager.getVirtualMap().getBlackColorIntensity(), UIManager.getLastClickedButton(), buttonToMoveObjFrom);
                    break;
                }

                case ROBOT_REMOVING_OBJECT: {
                    RobotOperation.removeObject (api, this, UIManager.getVirtualMap().getBackgroundColorIntensity(),
                            UIManager.getVirtualMap().getBlackColorIntensity(), UIManager.getLastClickedButton());
                    break;
                }
                default: {
                    throw new RobotException("Something went wrong. Please try this operation again.");
                }
            }
        } catch (RobotException e) {
            if (buttonToMoveObjFrom != null && buttonToMoveObjFrom.getBackgroundColor() == Color.RED)
                buttonToMoveObjFrom.setBackgroundColor(Color.BLACK);
            if (UIManager.getLastClickedButton() != null && UIManager.getLastClickedButton().getBackgroundColor() == Color.RED)
                UIManager.getLastClickedButton().setBackgroundColor(Color.BLACK);
            e.printStackTrace();
            Intent intent = new Intent(UIManager.getContext(), PopupErrorActivity.class);
            intent.putExtra("error", e.getMessage());
            UIManager.getContext().startActivity(intent);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        robotView.setPositionByTrack(null, null);
        UIManager.setUIState(VirtualMapUI.ActivityState.NOTHING_DONE, null);
    }

    @Override
    protected void onProgressUpdate(Integer[] values) {
        Integer x = values[0];
        Integer y = values[1];
        super.onProgressUpdate(values);
        robotView.setPositionByTrack(x, y);
    }
}
