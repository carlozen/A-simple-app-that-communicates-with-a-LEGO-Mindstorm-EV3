package it.unive.dais.legodroid.code;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import java.io.IOException;

import it.unive.dais.legodroid.R;
import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.comm.BluetoothConnection;

public class ManualActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual);

        Button up = findViewById(R.id.up);
        Button down = findViewById(R.id.down);
        Button left = findViewById(R.id.left);
        Button right = findViewById(R.id.right);

        try {

            EV3 ev3 = new EV3(new BluetoothConnection("EV3").connect());

        } catch (IOException e) {

            e.printStackTrace();
            startActivity(new Intent(ManualActivity.this, PopupConnectionActivity.class));

        }

        up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });


    }

}
