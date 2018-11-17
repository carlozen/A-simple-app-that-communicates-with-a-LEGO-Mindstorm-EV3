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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
                try {

                    EV3 ev3 = new EV3(new BluetoothConnection("EV3").connect());
                    Intent intent = new Intent(MainActivity.this, ChoiceModeActivity.class);

                    startActivity(intent);

                } catch (IOException e) {

                    e.printStackTrace();
                    startActivity(new Intent(MainActivity.this, PopupConnectionActivity.class));

                }
            }
        });
    }

}
