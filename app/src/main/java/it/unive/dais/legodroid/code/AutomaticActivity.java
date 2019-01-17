package it.unive.dais.legodroid.code;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import java.io.IOException;
import java.util.ArrayList;

import it.unive.dais.legodroid.R;
import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.plugs.LightSensor;
import it.unive.dais.legodroid.lib.util.Prelude;
import it.unive.dais.legodroid.ourUtil.RobotException;
import it.unive.dais.legodroid.ourUtil.VirtualMap;

public class AutomaticActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_automatic);

        //TODO TEST BUTTONS START

        Button addMapTestButton = findViewById(R.id.add_map_test);
        addMapTestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<VirtualMap.MapTrack> trackList = new ArrayList<>();
                trackList.add(new VirtualMap.MapTrack(LightSensor.Color.GREEN, 2));
                trackList.add(new VirtualMap.MapTrack(LightSensor.Color.GREEN, 1));
                VirtualMap map = new VirtualMap(trackList, (short)3, (short)49); //TODO: change?
                map.save();
            }
        });

        //TODO TEST BUTTONS END

        Button selectMapButton = findViewById(R.id.select_map);
        selectMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AutomaticActivity.this, PopupSavedMapsActivity.class));
            }
        });

        Button followLineButton = findViewById(R.id.followLine);
        followLineButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(AutomaticActivity.this, PopupScanningActivity.class));

                    /*
                    ArrayList<LightSensor.Color> colorsToCheck = new ArrayList<>();
                    colorsToCheck.add(LightSensor.Color.GREEN);
                    colorsToCheck.add(LightSensor.Color.YELLOW);
                    colorsToCheck.add(LightSensor.Color.RED);
                    Prelude.trap(() -> MainActivity.ev3.run(api -> AutomaticActivity.scanMap(api, AutomaticActivity.this, LightSensor.Color.RED, colorsToCheck)));
                    */

                }
            }
        );
    }

    private static void scanMap(EV3.Api api, AutomaticActivity automaticActivity, LightSensor.Color colorStop, ArrayList<LightSensor.Color> colorsToCheck) {
        try {
            VirtualMap virtualMap = VirtualMap.scan(api, colorStop, colorsToCheck);
            virtualMap.save();
            Intent intent = new Intent(automaticActivity, VirtualMapActivity.class);
            intent.putExtra("map", virtualMap);
            automaticActivity.startActivity(intent);
            //VirtualMap.backTrack(api, new LightSensorMonitor(), (short)3, (short)42, LightSensor.Color.YELLOW, 2);
        } catch (RobotException | InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

}

