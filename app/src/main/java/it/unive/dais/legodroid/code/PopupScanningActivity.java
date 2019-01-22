package it.unive.dais.legodroid.code;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import it.unive.dais.legodroid.R;
import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.plugs.LightSensor;
import it.unive.dais.legodroid.lib.util.Prelude;
import it.unive.dais.legodroid.ourUtil.AnimationGenerator;
import it.unive.dais.legodroid.ourUtil.Motor;
import it.unive.dais.legodroid.ourUtil.RobotException;
import it.unive.dais.legodroid.ourUtil.RobotOperation;
import it.unive.dais.legodroid.ourUtil.VirtualMap;

public class PopupScanningActivity extends AppCompatActivity {

    private EV3.Api ev3api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popup_scanning);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;

        getWindow().setLayout((int)(width * 0.8),(int)(height * 0.7));

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.gravity = Gravity.CENTER;
        params.x = 0;
        params.y = -20;

        getWindow().setAttributes(params);

        TextView scanningText = findViewById(R.id.scanning_text);
        AnimationGenerator.setFadingAnimation(scanningText);

        ArrayList<LightSensor.Color> colorsToCheck = new ArrayList<>();
        colorsToCheck.add(LightSensor.Color.GREEN);
        colorsToCheck.add(LightSensor.Color.YELLOW);
        colorsToCheck.add(LightSensor.Color.RED);
        Prelude.trap(() -> MainActivity.ev3.run(api -> scanMap(api, PopupScanningActivity.this, LightSensor.Color.RED, colorsToCheck)));



    }


    private void scanMap(EV3.Api api, PopupScanningActivity scanningActivity, LightSensor.Color colorStop, ArrayList<LightSensor.Color> colorsToCheck) {
        try {
            ev3api = api;
            VirtualMap virtualMap = VirtualMap.scan(api, colorStop, colorsToCheck);
            virtualMap.save();
            Intent intent = new Intent(scanningActivity, VirtualMapActivity.class);
            intent.putExtra("map", virtualMap);
            scanningActivity.startActivity(intent);
            scanningActivity.finish();
            //VirtualMap.backTrack(api, new LightSensorMonitor(), (short)3, (short)42, LightSensor.Color.YELLOW, 2);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            try {
                throw new RobotException("Qualcosa è andato storto, ritentare l'operazione");
            } catch (RobotException e1) {
                e1.printStackTrace();
                Intent intent = new Intent(scanningActivity, PopupErrorActivity.class);
                intent.putExtra("error", e1.getMessage());
                scanningActivity.startActivity(intent);
                scanningActivity.finish();
            }
        } catch (RobotException  e) {
            e.printStackTrace();
            Intent intent = new Intent(scanningActivity, PopupErrorActivity.class);
            intent.putExtra("error", e.getMessage());
            scanningActivity.startActivity(intent);
            scanningActivity.finish();
        }
    }

}
