package it.unive.dais.legodroid.ourUtil;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.View;

public class ButtonsGeneralListener implements View.OnClickListener {

    private VirtualMapUI.ActivityState activityState;
    private PositionButton referencedButton;
    private PositionButton button;
    private VirtualMapUI UIManager;

    public ButtonsGeneralListener(PositionButton button, VirtualMapUI UIManager) {
        this.UIManager = UIManager;
        this.activityState = this.UIManager.getActivityState();
        this.referencedButton = this.UIManager.getLastClickedButton();
        this.button=button;
        button.setClickable(true);
    }

    @Override
    public void onClick(View view) {
        switch (activityState) {
            case MOVE_OBJECT: {
                UIManager.setRobotOperation(VirtualMapUI.ActivityState.ROBOT_MOVING_OBJECT, button,
                        referencedButton);
                break;
            }
            case ADD_OBJECT: {
                UIManager.setRobotOperation(VirtualMapUI.ActivityState.ROBOT_ADDING_OBJECT, button, null);
                break;
            }
            case NOTHING_DONE: {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(button.getContext());
                dialogBuilder.setMessage("SELECT WHAT YOU WANT TO DO WITH THIS OBJECT");
                dialogBuilder.setPositiveButton("DELETE", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        button.getShape().setColor(Color.RED);
                        UIManager.setRobotOperation(VirtualMapUI.ActivityState.ROBOT_REMOVING_OBJECT, button, null);
                    }
                });
                if (!UIManager.getVirtualMap().isFull()) {
                    dialogBuilder.setNegativeButton("MOVE", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            UIManager.setUIState(VirtualMapUI.ActivityState.MOVE_OBJECT, button);
                        }
                    });
                }
                AlertDialog dialog = dialogBuilder.create();
                dialog.show();
                break;
            }
        }
    }
}
