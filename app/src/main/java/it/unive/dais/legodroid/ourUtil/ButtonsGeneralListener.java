package it.unive.dais.legodroid.ourUtil;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.RelativeLayout;

import it.unive.dais.legodroid.R;
import it.unive.dais.legodroid.code.VirtualMapActivity;

public class ButtonsGeneralListener implements View.OnClickListener {

    private MapView.ActivityState activityState;
    private PositionButton referencedButton;
    private PositionButton button;
    private RelativeLayout dialogueLayout;

    public ButtonsGeneralListener(PositionButton button, RelativeLayout dialogueLayout) {
        this.dialogueLayout = dialogueLayout;
        this.activityState = MapView.getActivityState();
        this.referencedButton = MapView.getLastClickedButton();
        this.button=button;
        button.setClickable(true);
    }

    @Override
    public void onClick(View view) {
        switch (activityState) {
            case MOVE_OBJECT: {
                button.changeOccupiedState();
                referencedButton.changeOccupiedState();
                MapView.setButtonListOnClickListeners(MapView.getPositionButtonList(),
                        MapView.ActivityState.NOTHING_DONE, null);
                VirtualMapActivity.setDialogueLayout(button.getContext(), dialogueLayout);
                break;
            }
            case ADD_OBJECT: {
                button.changeOccupiedState();
                MapView.setButtonListOnClickListeners(MapView.getPositionButtonList(),
                        MapView.ActivityState.NOTHING_DONE, null);
                VirtualMapActivity.setDialogueLayout(button.getContext(), dialogueLayout);
                break;
            }
            case NOTHING_DONE: {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(button.getContext());
                dialogBuilder.setMessage("SELECT WHAT YOU WANT TO DO WITH THIS OBJECT");
                dialogBuilder.setPositiveButton("DELETE", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        button.changeOccupiedState();
                        MapView.setButtonListOnClickListeners(MapView.getPositionButtonList(),
                                MapView.ActivityState.NOTHING_DONE, null);
                        VirtualMapActivity.setDialogueLayout(button.getContext(), dialogueLayout);
                    }
                });
                dialogBuilder.setNegativeButton("MOVE", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        MapView.setButtonListOnClickListeners(MapView.getPositionButtonList(),
                                MapView.ActivityState.MOVE_OBJECT, button);
                        VirtualMapActivity.setDialogueLayout(button.getContext(), dialogueLayout);
                    }
                });
                AlertDialog dialog = dialogBuilder.create();
                dialog.show();
                break;
            }
        }
    }
}
