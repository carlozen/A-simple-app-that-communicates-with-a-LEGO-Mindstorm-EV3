package it.unive.dais.legodroid.code;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

//import com.google.gson.Gson;
//import com.google.gson.GsonBuilder;

import java.io.IOException;

import it.unive.dais.legodroid.R;
import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.comm.BluetoothConnection;


public class MainActivity extends AppCompatActivity {

    public static EV3 ev3;
    public static SharedPreferences mSettings;
    public static SharedPreferences.Editor mEditor;
    //public static Gson mGson;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button rules = findViewById(R.id.rules);
        Button play = findViewById(R.id.play);

        mSettings = getSharedPreferences("MapSaved", Context.MODE_PRIVATE);
        mEditor = mSettings.edit();

      //  GsonBuilder gsonb = new GsonBuilder();
       // mGson = gsonb.create();

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

                    ev3 = new EV3(new BluetoothConnection("EV3").connect());
                    Intent intent = new Intent(MainActivity.this, ChoiceModeActivity.class);
                    //intent.putExtra()


                    startActivity(intent);

                } catch (IOException e) {

                    e.printStackTrace();
                    startActivity(new Intent(MainActivity.this, PopupConnectionActivity.class));

                }
            }
        });
    }

}
