package it.unive.dais.legodroid.code;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;

import it.unive.dais.legodroid.R;
import it.unive.dais.legodroid.ourUtil.DrawView;
import it.unive.dais.legodroid.ourUtil.VirtualMap;

public class VirtualMapActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_virtual_map);

        Bundle data = getIntent().getExtras();
        VirtualMap map = (VirtualMap) (data != null ? data.getParcelable("map") : null);

        Button addObjectButton = findViewById(R.id.addObject);

        RelativeLayout mapLayout = findViewById(R.id.mapLayout);

        DrawView mapView = new DrawView(this, map, mapLayout);
        mapLayout.addView(mapView);

    }

}
