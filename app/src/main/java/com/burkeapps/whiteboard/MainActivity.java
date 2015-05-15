package com.burkeapps.whiteboard;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.burkeapps.whiteboard.views.WhiteboardView;


public class MainActivity extends ActionBarActivity {

    WhiteboardView whiteboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        whiteboard = (WhiteboardView) findViewById(R.id.whiteboard);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch(id){
            case R.id.action_erase:
                whiteboard.erase();
                return true;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
