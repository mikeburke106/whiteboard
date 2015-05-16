package com.burkeapps.whiteboard;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.burkeapps.whiteboard.views.WhiteboardView;


public class MainActivity extends ActionBarActivity {

    WhiteboardView whiteboard;
    MenuItem colorsItem, eraserItem, markerItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        whiteboard = (WhiteboardView) findViewById(R.id.whiteboard);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        eraserItem = menu.findItem(R.id.action_eraser);
        markerItem = menu.findItem(R.id.action_marker);
        colorsItem = menu.findItem(R.id.action_colors);
        activateMarker();
        return true;
    }

    private void redrawMenuItems(int whiteboardMode){
        boolean eraseMode = (whiteboardMode == WhiteboardView.MODE_ERASER);

        // if whiteboard is in erase mode, hide eraser and colors menu items and
        // show marker menu item.  Otherwise, show the opposite.
        eraserItem.setVisible( ! eraseMode );
        colorsItem.setVisible( ! eraseMode );
        markerItem.setVisible(eraseMode);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch(id){
            case R.id.action_eraser:
                activateEraser();
                return true;
            case R.id.action_marker:
                activateMarker();
                return true;
            case R.id.action_clear:
                eraseWhiteboard();
                return true;
            case R.id.action_colors:
                changeWhiteboardColor();
                return true;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode != 0 || resultCode != Activity.RESULT_OK) return;
        int color = ColorSelectActivity.getColor(data);
        whiteboard.setDrawColor(color);
    }

    private void eraseWhiteboard(){
        whiteboard.clear();
    }

    private void activateEraser(){
        whiteboard.activateEraser();
        redrawMenuItems(whiteboard.getTouchMode());
    }

    private void activateMarker(){
        whiteboard.activateMarker();
        redrawMenuItems(whiteboard.getTouchMode());
    }

    private void changeWhiteboardColor() {
        startActivityForResult(new Intent(MainActivity.this, ColorSelectActivity.class), 0);
    }
}
