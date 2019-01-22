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

                }
            }
        );
    }

}

