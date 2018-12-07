package it.unive.dais.legodroid.ourUtil;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import it.unive.dais.legodroid.R;
import it.unive.dais.legodroid.code.MainActivity;
import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.util.Prelude;

//TODO those fields shouldn't be static.
public class VirtualMapUI {

    private Context context;

    private ActivityState activityState;

    private PositionButton lastClickedButton;

    private ArrayList<ArrayList<PositionButton>> positionButtonList;

    private VirtualMap virtualMap;

    private RelativeLayout relativeLayout;

    private AsyncRobotTask asyncRobotTask = null;

    private RobotView robotView;


    private VirtualMapUI(Context context, ActivityState activityState,
                         PositionButton lastClickedButton,
                         VirtualMap virtualMap, RelativeLayout relativeLayout,
                         RobotView robotView) {
        this.context = context;
        this.activityState = activityState;
        this.lastClickedButton = lastClickedButton;
        this.virtualMap = virtualMap;
        this.relativeLayout = relativeLayout;
        this.positionButtonList= new ArrayList<>();
        this.robotView = robotView;

        for (int i = 0; i< virtualMap.getMapTrackList().size(); i++) {
            positionButtonList.add(new ArrayList<>());
            for (int j = 0; j<virtualMap.getMapTrackList().get(i).getObjectList().size(); j++) {
                positionButtonList.get(i).add(new PositionButton(context, this, i, j));
            }
        }
    }

    public static VirtualMapUI generate (Context context, ActivityState activityState,
                                         PositionButton lastClickedButton,
                                         VirtualMap virtualMap, RelativeLayout relativeLayout,
                                         RobotView robotView) {
        return new VirtualMapUI(context, activityState,lastClickedButton,
                virtualMap, relativeLayout, robotView);
    }

    public void setUIState (ActivityState activityState,
                            PositionButton lastClickedButton) {
        this.setAllButtonsListeners(activityState, lastClickedButton);
        this.setDialogueLayout();
    }

    public void setRobotOperation (ActivityState activityState,
                                   PositionButton lastClickedButton,
                                   PositionButton destinationButton) {
        setUIState(activityState, lastClickedButton);

        if (activityState == ActivityState.ROBOT_REMOVING_OBJECT ||
                activityState == ActivityState.ROBOT_ADDING_OBJECT ||
                activityState == ActivityState.ROBOT_MOVING_OBJECT) {
            asyncRobotTask = new AsyncRobotTask(this, destinationButton);
            asyncRobotTask.execute();
        }
    }

    private void setActivityState(ActivityState activityState) {
        this.activityState = activityState;
    }


    public void resetAllButtonsListeners() {
        setAllButtonsListeners(activityState, getLastClickedButton());
    }

    public void setLastClickedButton(PositionButton lastClickedButton) {
        this.lastClickedButton = lastClickedButton;
    }

    private void setAllButtonsListeners(ActivityState activityState, PositionButton lastClickedButton) {
        setActivityState(activityState);
        setLastClickedButton(lastClickedButton);
        for (ArrayList<PositionButton> array : positionButtonList) {
            for (PositionButton button : array) {
                button.setAnimation(null);
                button.clearAnimation();
                button.setSingleButtonListener(getActivityState(), getLastClickedButton());
            }
        }
    }

    public void unsetAllButtons () {
        for (ArrayList<PositionButton> arrayList : positionButtonList) {
            for (PositionButton button : arrayList) {
                button.setOnClickListener(null);
                button.setAnimation(null);
            }
        }
    }

    public void setDialogueLayout() {
        Button addObjectButton = relativeLayout.findViewById(R.id.addObject);
        TextView operationDescriptionView = relativeLayout.findViewById(R.id.operation_description);
        operationDescriptionView.setAnimation(null);

        switch (getActivityState()) {
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
                        setAllButtonsListeners(ActivityState.ADD_OBJECT, null);
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
            default: {
                addObjectButton.setVisibility(View.GONE);
                AnimationGenerator.setFadingAnimation(operationDescriptionView);
                operationDescriptionView.setText("The Robot is Operating...");
                break;
            }
        }
    }

    public ActivityState getActivityState() {
        return activityState;
    }

    public ArrayList<ArrayList<PositionButton>> getPositionButtonList() {
        return positionButtonList;
    }

    public VirtualMap getVirtualMap() {
        return virtualMap;
    }

    public Context getContext() {
        return this.context;
    }

    public PositionButton getLastClickedButton() {
        try {
            return positionButtonList.get(lastClickedButton.getTrackNumber()).
                    get(lastClickedButton.getPositionNumber());
        } catch (Exception e) {
            return null;
        }
    }

    public RobotView getRobotView() {
        return this.robotView;
    }


    public enum ActivityState {
        NOTHING_DONE,
        ADD_OBJECT,
        MOVE_OBJECT,
        ROBOT_MOVING_OBJECT,
        ROBOT_REMOVING_OBJECT,
        ROBOT_ADDING_OBJECT,
    }

    public AsyncRobotTask getAsyncRobotTask() {
        return this.asyncRobotTask;
    }


    //TODO check all this, that doesn't work correctly.
    public class AsyncRobotTask extends AsyncTask <Object, Integer, Object>{

        private VirtualMapUI UIManager;
        private PositionButton buttonDestination = null;
        private RobotView robotView;

        public AsyncRobotTask (VirtualMapUI UIManager,
                               PositionButton positionButton) {
            this.UIManager = UIManager;
            this.buttonDestination = positionButton;
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
        protected Object doInBackground(Object[] object) {
            Log.d("OP", "INIZIO OPERAZIONE 1");

            Prelude.trap(() -> MainActivity.ev3.run(api -> VirtualMapUI.tempOperation(api, this)));

            /*
            publishProgress(1,null);

            Log.d("OP", "INIZIO OPERAZIONE 2");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            publishProgress(2,1);

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            */
            return null;
        }


        @Override
        protected void onPostExecute(Object object) {
            switch (UIManager.getActivityState()) {

                case ROBOT_REMOVING_OBJECT: {
                    UIManager.getLastClickedButton().changeOccupiedState();
                    UIManager.setUIState(ActivityState.NOTHING_DONE, null);
                    break;
                }
                case ROBOT_ADDING_OBJECT: {
                    UIManager.getLastClickedButton().changeOccupiedState();
                    UIManager.setUIState(ActivityState.NOTHING_DONE, null);
                    break;
                }
                case ROBOT_MOVING_OBJECT: {
                    UIManager.getLastClickedButton().changeOccupiedState();
                    UIManager.getPositionButtonList().get(this.buttonDestination.getTrackNumber())
                            .get(this.buttonDestination.getPositionNumber()).changeOccupiedState();

                    UIManager.setUIState(ActivityState.NOTHING_DONE, null);
                    break;
                }
            }
            robotView.setPositionByTrack(null,null);
        }

        @Override
        protected void onProgressUpdate(Integer[] values) {
            Integer x = values[0];
            Integer y = values[1];
            super.onProgressUpdate(values);
            robotView.setPositionByTrack(x, y);
        }
    }


    private static void tempOperation (EV3.Api api, AsyncRobotTask asyncRobotTask) {
        try {
            RobotOperation.pickUpObject(api, asyncRobotTask);
        } catch (RobotException e) {
            e.printStackTrace();
        }
    }

}
