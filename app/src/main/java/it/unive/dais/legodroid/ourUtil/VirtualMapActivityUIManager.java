package it.unive.dais.legodroid.ourUtil;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import it.unive.dais.legodroid.R;

//TODO those fields shouldn't be static.
public class VirtualMapActivityUIManager {

    private Context context;

    private ActivityState activityState;

    private ParcelablePositionButton lastClickedButton;

    private boolean hasRobotOperationStarted;

    private ArrayList<ArrayList<PositionButton>> positionButtonList;

    private VirtualMap virtualMap;

    private RelativeLayout relativeLayout;

    private AsyncRobotTask asyncRobotTask = null;

    private View robotView;


    private VirtualMapActivityUIManager (Context context, ActivityState activityState,
                                         ParcelablePositionButton lastClickedButton,
                                         boolean hasRobotOperationStarted,
                                         VirtualMap virtualMap, RelativeLayout relativeLayout,
                                         View robot_view) {
        this.context = context;
        this.activityState = activityState;
        this.lastClickedButton = lastClickedButton;
        this.hasRobotOperationStarted = hasRobotOperationStarted;
        this.virtualMap = virtualMap;
        this.relativeLayout = relativeLayout;
        this.positionButtonList= new ArrayList<>();
        this.robotView = robot_view;

        for (int i = 0; i< virtualMap.getMapTrackList().size(); i++) {
            positionButtonList.add(new ArrayList<>());
            for (int j = 0; j<virtualMap.getMapTrackList().get(i).getObjectList().size(); j++) {
                positionButtonList.get(i).add(new PositionButton(context, this, i, j));
            }
        }
    }

    public static VirtualMapActivityUIManager generate (Context context, ActivityState activityState,
                                                        ParcelablePositionButton lastClickedButton,
                                                        boolean hasRobotOperationStarted,
                                                        VirtualMap virtualMap, RelativeLayout relativeLayout,
                                                        View robotView) {
        return new VirtualMapActivityUIManager(context, activityState,lastClickedButton, hasRobotOperationStarted,
                virtualMap, relativeLayout, robotView);
    }

    public void setUIState (ActivityState activityState,
                            PositionButton lastClickedButton) {
        this.setAllButtonsListeners(activityState, lastClickedButton);
        this.setDialogueLayout();
    }

    public void setRobotOperation (ActivityState activityState,
                                   PositionButton lastClickedButton,
                                   ParcelablePositionButton destinationButton) {
        setUIState(activityState, lastClickedButton);

        if (activityState == ActivityState.ROBOT_REMOVING_OBJECT ||
                activityState == ActivityState.ROBOT_ADDING_OBJECT ||
                activityState == ActivityState.ROBOT_MOVING_OBJECT) {
            if (!this.hasRobotOperationStarted()) {
                asyncRobotTask = new AsyncRobotTask(this, destinationButton, robotView);
                asyncRobotTask.execute();
            }
        }
    }

    private void setActivityState(ActivityState activityState) {
        this.activityState = activityState;
    }

    private void setLastClickedButton(PositionButton lastClickedButton) {
        try {
            this.lastClickedButton = new ParcelablePositionButton(lastClickedButton.getTrackNumber(),
                    lastClickedButton.getPositionNumber());
        }
        catch (NullPointerException e) {
            this.lastClickedButton = null;
        }
    }

    public void resetAllButtonsListeners() {
        setAllButtonsListeners(activityState, getLastClickedButton());
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

    public boolean hasRobotOperationStarted() {
        return hasRobotOperationStarted;
    }

    public ParcelablePositionButton getLastClickedButtonParcelable() {
        try {
            return new ParcelablePositionButton(lastClickedButton.getTrackNumber(),
                    lastClickedButton.getPositionNumber());
        } catch (NullPointerException e) {
            return null;
        }
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

    public void setHasRobotOperationStarted(boolean hasRobotOperationStarted) {
        this.hasRobotOperationStarted = hasRobotOperationStarted;
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

        private VirtualMapActivityUIManager UIManager;
        private ParcelablePositionButton buttonDestination = null;

        public AsyncRobotTask (VirtualMapActivityUIManager UIManager,
                               ParcelablePositionButton positionButton,
                               View robotView) {
            this.UIManager = UIManager;
            this.buttonDestination = positionButton;
        }

        @Override
        protected void onPreExecute() {
            UIManager.setHasRobotOperationStarted(true);
            robotView.setVisibility(View.VISIBLE);
        }

        @Override
        protected Object doInBackground(Object[] object) {
            Log.d("OP", "INIZIO OPERAZIONE 1");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            publishProgress(90,10);

            Log.d("OP", "INIZIO OPERAZIONE 2");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            publishProgress(90,90);

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }


        @Override
        protected void onPostExecute(Object object) {
            switch (UIManager.getActivityState()) {

                case ROBOT_REMOVING_OBJECT: {
                    UIManager.setHasRobotOperationStarted(false);
                    UIManager.getLastClickedButton().changeOccupiedState();
                    UIManager.setUIState(ActivityState.NOTHING_DONE, null);
                    break;
                }
                case ROBOT_ADDING_OBJECT: {
                    UIManager.setHasRobotOperationStarted(false);
                    UIManager.getLastClickedButton().changeOccupiedState();
                    UIManager.setUIState(ActivityState.NOTHING_DONE, null);
                    break;
                }
                case ROBOT_MOVING_OBJECT: {
                    UIManager.setHasRobotOperationStarted(false);
                    UIManager.getLastClickedButton().changeOccupiedState();
                    UIManager.getPositionButtonList().get(this.buttonDestination.getTrackNumber())
                            .get(this.buttonDestination.getPositionNumber()).changeOccupiedState();

                    UIManager.setUIState(ActivityState.NOTHING_DONE, null);
                    break;
                }
            }
            robotView.setVisibility(View.GONE);
        }

        @Override
        protected void onProgressUpdate(Integer[] values) {
            int x = values[0];
            int y = values[1];
            super.onProgressUpdate(values);
            AnimationGenerator.translationAnimation(robotView, x,y);
        }
    }

}
