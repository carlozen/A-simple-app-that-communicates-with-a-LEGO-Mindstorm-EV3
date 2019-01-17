package it.unive.dais.legodroid.code;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Layout;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;

import it.unive.dais.legodroid.R;
import it.unive.dais.legodroid.ourUtil.VirtualMap;

public class PopupSavedMapsActivity extends AppCompatActivity {

    private ArrayList<VirtualMap> virtualMapList = VirtualMap.getSavedMaps();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_saved_maps);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;

        getWindow().setLayout((int)(width * 0.8),(int)(height * 0.7));

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.gravity = Gravity.CENTER;
        params.x = 0;
        params.y = -20;

        getWindow().setAttributes(params);

        Button backButton = findViewById(R.id.saved_maps_back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        ScrollView scrollView = findViewById(R.id.scroll_view);

        if (virtualMapList.size()==0) {
            TextView textView = new TextView(this);
            textView.setText("Nessuna mappa salvata");
            textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            scrollView.addView(textView);
        }

        else {
            TableLayout scrollViewLayout = new TableLayout(this);

            TableLayout.LayoutParams tableLayoutParams = new TableLayout.LayoutParams((int) (width * 0.8), ViewGroup.LayoutParams.WRAP_CONTENT);

            scrollViewLayout.setLayoutParams(tableLayoutParams);

            scrollViewLayout.setStretchAllColumns(false);
            scrollViewLayout.setShrinkAllColumns(true);

            scrollView.addView(scrollViewLayout);
            for (int i = 0 ; i < virtualMapList.size(); i++) {

                TableRow tableRow = new TableRow(this);

                TableRow.LayoutParams rowLayoutParams = new TableRow.LayoutParams();
                rowLayoutParams.gravity = Gravity.CENTER;
                rowLayoutParams.height = TableRow.LayoutParams.WRAP_CONTENT;

                tableRow.setLayoutParams(rowLayoutParams);

                scrollViewLayout.addView(tableRow);

                final VirtualMap map = virtualMapList.get(i);
                Button mapButton = new Button(this);
                mapButton.setTransformationMethod(null);



                mapButton.setGravity(Gravity.CENTER_VERTICAL);
                mapButton.setScaleY(1.2f);

                mapButton.setText(map.toString());



                mapButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(PopupSavedMapsActivity.this, VirtualMapActivity.class);
                        intent.putExtra("map", map);
                        startActivity(intent);
                        finish();
                    }
                });


                ImageButton deleteButton = new ImageButton(this);
                deleteButton.setBackgroundResource(R.drawable.delete_icon);
                //deleteButton.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

                deleteButton.setScaleX(0.4f);
                deleteButton.setScaleY(0.4f);



                int finalI = i;
                deleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        VirtualMap.removeSavedMap(finalI);
                        recreate();
                    }
                });

                //deleteButton.setGravity(Gravity.CENTER_VERTICAL);
                tableRow.addView(mapButton);
                tableRow.addView(deleteButton);
            }
        }


    }

}
