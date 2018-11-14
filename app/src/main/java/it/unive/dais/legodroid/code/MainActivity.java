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


public class MainActivity extends AppCompatActivity {

    protected Boolean connection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EV3 ev3;

        try {
            ev3 = new EV3(new BluetoothConnection("EV3").connect());
            connection = true;
        } catch (IOException e) {
            e.printStackTrace();
            connection = false;
        }

        Button rules = findViewById(R.id.rules);
        Button play = findViewById(R.id.play);

        rules.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, RulesActivity.class));
            }
        });

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(connection) {
                    startActivity(new Intent(MainActivity.this, ChoiceModeActivity.class));
                } else {
                    startActivity(new Intent(MainActivity.this, PopupConnectionActivity.class));
                }
            }
        });
    }

}
