package com.burkeapps.whiteboard;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;


public class ThicknessSelectActivity extends Activity {

    private static final String THICKNESS_INTENT_STR = "com.burkeapps.whiteboard.ThicknessSelectActivity.thickness";
    private static final int NUM_THICKNESSES = 26;
    private static Integer[] thicknesses;

    ListView listview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_select);

        listview = (ListView) findViewById(R.id.listview);
        ArrayAdapter<Integer> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, getThicknesses());
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int thickness = ((ArrayAdapter<Integer>) parent.getAdapter()).getItem(position);
                returnActivityResult(thickness);
            }
        });
    }

    private Integer[] getThicknesses(){
        if(thicknesses == null) {
            thicknesses = new Integer[NUM_THICKNESSES];
            for (int i = 0; i < NUM_THICKNESSES; i++) {
                thicknesses[i] = 10 + (i * 2);
            }
        }
        return thicknesses;
    }

    private void returnActivityResult(int thickness) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(THICKNESS_INTENT_STR, thickness);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    /**
     * Reads the color value from an intent returned as a result.  If the color
     * cannot be found, black will be returned as the default.
     *
     * @param data The intent returned as the result of this activity
     * @return The color read from the input intent
     */
    public static int getThickness(Intent data, float density){
        int result = data.getIntExtra(THICKNESS_INTENT_STR, -1);

        if(result != -1){
            // apply the density
            result = (int) (result * density);
        }

        return result;
    }
}
