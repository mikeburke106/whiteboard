package com.burkeapps.whiteboard;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;


public class ColorSelectActivity extends Activity {

    private static final String COLOR_INTENT_STR = "com.burkeapps.whiteboard.ColorSelectActivity.color";

    private enum DrawColor{
        BLACK("Black", Color.BLACK),
        BLUE("Blue", Color.BLUE),
        DARK_GRAY("Dark Gray", Color.DKGRAY),
        GREEN("Green", Color.GREEN),
        LIGHT_GRAY("Light Gray", Color.LTGRAY),
        RED("Red", Color.RED),
        ORANGE("Orange", Color.rgb(255, 165, 0)),
        YELLOW("Yellow", Color.YELLOW),

        ;

        final String name;
        final int color;
        DrawColor(String name, int color){
            this.name = name;
            this.color = color;
        }

        @Override
        public String toString(){
            return this.name;
        }
    }

    ListView listview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_select);

        listview = (ListView) findViewById(R.id.listview);
        ArrayAdapter<DrawColor> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, DrawColor.values());
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DrawColor drawColor = ((ArrayAdapter<DrawColor>) parent.getAdapter()).getItem(position);
                log("onItemClick, draw color=" + drawColor);
                int clickedColor = drawColor.color;
                returnActivityResult(clickedColor);
            }
        });
    }

    private void returnActivityResult(int color) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(COLOR_INTENT_STR, color);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    public static int getColor(Intent data){
        return data.getIntExtra(COLOR_INTENT_STR, Color.BLACK);
    }

    private static void log(String msg){
        Log.d("MainActivity", msg);
    }
}
