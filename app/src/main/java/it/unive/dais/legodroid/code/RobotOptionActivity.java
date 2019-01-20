package it.unive.dais.legodroid.code;

import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import it.unive.dais.legodroid.R;

public class RobotOptionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_robot_option);

        ViewPager viewPager = findViewById(R.id.viewPagerRobot);
        ImageAdapterRobot adapterRobot = new ImageAdapterRobot(this);
        viewPager.setAdapter(adapterRobot);
    }
}
