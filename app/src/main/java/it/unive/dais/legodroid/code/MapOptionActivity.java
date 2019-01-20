package it.unive.dais.legodroid.code;

import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import it.unive.dais.legodroid.R;

public class MapOptionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_option);

        ViewPager viewPager = findViewById(R.id.viewPagerMap);
        ImageAdapterMap adapterMap = new ImageAdapterMap(this);
        viewPager.setAdapter(adapterMap);
    }
}
