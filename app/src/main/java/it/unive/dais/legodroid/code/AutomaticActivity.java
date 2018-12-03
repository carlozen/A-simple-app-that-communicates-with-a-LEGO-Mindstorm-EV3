package it.unive.dais.legodroid.code;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

import it.unive.dais.legodroid.R;
import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.plugs.LightSensor;
import it.unive.dais.legodroid.lib.util.Prelude;
import it.unive.dais.legodroid.ourUtil.RobotException;
import it.unive.dais.legodroid.ourUtil.RobotOperation;
import it.unive.dais.legodroid.ourUtil.VirtualMap;

public class AutomaticActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_automatic);

        Button selectMapButton = findViewById(R.id.select_map);
        selectMapButton.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    Intent virtualMapIntent = new Intent (AutomaticActivity.this, VirtualMapActivity.class);

                                                    ArrayList<VirtualMap.MapTrack> trackList = new ArrayList<>();
                                                    trackList.add(new VirtualMap.MapTrack(LightSensor.Color.YELLOW, 2));
                                                    trackList.add(new VirtualMap.MapTrack(LightSensor.Color.GREEN, 3));
                                                    trackList.add(new VirtualMap.MapTrack(LightSensor.Color.BLUE, 2));

                                                    VirtualMap map = new VirtualMap(trackList);

                                                    virtualMapIntent.putExtra("map", map);
                                                    startActivity(virtualMapIntent);
                                                }
                                            }
        );

        Button followLineButton = findViewById(R.id.followLine);
        followLineButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ArrayList<LightSensor.Color> colorsToCheck = new ArrayList<>();
                    colorsToCheck.add(LightSensor.Color.GREEN);
                    colorsToCheck.add(LightSensor.Color.YELLOW);
                    Prelude.trap(() -> MainActivity.ev3.run(api -> AutomaticActivity.scanMap(api, LightSensor.Color.RED, colorsToCheck)));
                }
            }
        );
    }

    private static void scanMap(EV3.Api api, LightSensor.Color colorStop, ArrayList<LightSensor.Color> colorsToCheck) {
        try {
            VirtualMap virtualMap = VirtualMap.scan(api, colorStop, colorsToCheck);
        } catch (RobotException e) {
            e.printStackTrace();
        }
    }

}

