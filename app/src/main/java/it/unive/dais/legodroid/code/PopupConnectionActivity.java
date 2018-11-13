package it.unive.dais.legodroid.code;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;

import it.unive.dais.legodroid.R;

class PopupConnectionActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popup_connection);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);



    }

}
