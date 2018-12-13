package it.unive.dais.legodroid.ourUtil;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.view.View;

import it.unive.dais.legodroid.lib.EV3;

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
        robotView.setVisibility(View.VISIBLE);
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
                    /*RobotOperation.removeObject (api, this, UIManager.getVirtualMap().getBackgroundColorIntensity(),
                            UIManager.getVirtualMap().getBlackColorIntensity(), UIManager.getLastClickedButton());*/
                    break;
                }
                default: {
                    throw new RobotException("Something went wrong. Please try this operation again.");
                }
            }
        } catch (RobotException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
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
