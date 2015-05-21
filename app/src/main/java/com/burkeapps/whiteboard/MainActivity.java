package com.burkeapps.whiteboard;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.burkeapps.whiteboard.views.WhiteboardView;


public class MainActivity extends ActionBarActivity {

    private static final int REQ_COLOR = 0;
    private static final int REQ_THICKNESS = 1;

    WhiteboardView whiteboard;
    MenuItem colorsItem, eraserItem, markerItem, undoItem, redoItem;
    private WhiteboardView.PathListener pathListener = new WhiteboardView.PathListener() {
        @Override
        public void onPathCompleted() {
            redrawMenuItems();
        }

        @Override
        public void onPathUndone() {
            redrawMenuItems();
        }

        @Override
        public void onPathRedone() {
            redrawMenuItems();
        }

        @Override
        public void onPathsCleared() {
            redrawMenuItems();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        whiteboard = (WhiteboardView) findViewById(R.id.whiteboard);
        whiteboard.setPathListener(pathListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        eraserItem = menu.findItem(R.id.action_eraser);
        markerItem = menu.findItem(R.id.action_marker);
        colorsItem = menu.findItem(R.id.action_colors);
        undoItem = menu.findItem(R.id.action_undo);
        redoItem = menu.findItem(R.id.action_redo);
        redrawMenuItems();
        return true;
    }

    private void redrawMenuItems(){
        boolean eraseMode = (whiteboard.getTouchMode() == WhiteboardView.MODE_ERASER);

        // if whiteboard is in erase mode, hide eraser and colors menu items and
        // show marker menu item.  Otherwise, show the opposite.
        eraserItem.setVisible(!eraseMode);
        colorsItem.setVisible(!eraseMode);
        markerItem.setVisible(eraseMode);

        undoItem.setVisible(whiteboard.canUndo());
        redoItem.setVisible(whiteboard.canRedo());
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
                changeMarkerColor();
                return true;
            case R.id.action_thickness:
                changeMarkerThickness();
                return true;
            case R.id.action_undo:
                undoLastPath();
                return true;
            case R.id.action_redo:
                redoLastPath();
                return true;
            case R.id.action_share:
                shareWhiteboardImage();
                return true;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        //redraw whiteboard after views have stabilized
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                whiteboard.redraw();
            }
        }, 200);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode != Activity.RESULT_OK) return;
        switch(requestCode){
            case REQ_COLOR:
                int color = ColorSelectActivity.getColor(data);
                whiteboard.setMarkerColor(color);
                break;
            case REQ_THICKNESS:
                float density = getResources().getDisplayMetrics().density;
                int thickness = ThicknessSelectActivity.getThickness(data, density);
                if(thickness > 0) {
                    whiteboard.setMarkerThickness(thickness);
                }
                break;
            default:
                break;
        }
    }

    private void eraseWhiteboard(){
        whiteboard.clear();
    }

    private void activateEraser(){
        whiteboard.activateEraser();
        redrawMenuItems();
    }

    private void activateMarker(){
        whiteboard.activateMarker();
        redrawMenuItems();
    }

    private void changeMarkerColor() {
        startActivityForResult(new Intent(MainActivity.this, ColorSelectActivity.class), REQ_COLOR);
    }

    private void changeMarkerThickness() {
        startActivityForResult(new Intent(MainActivity.this, ThicknessSelectActivity.class), REQ_THICKNESS);
    }

    private void undoLastPath() {
        whiteboard.undo();
    }

    private void redoLastPath() {
        whiteboard.redo();
    }

    private void shareWhiteboardImage() {
        Toast.makeText(this, "Saving image...", Toast.LENGTH_LONG).show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                showShareIntent();
            }
        }).start();
    }

    private void showShareIntent() {
        final Bitmap image = whiteboard.screenshot();
        final String filename = "whiteboard-image" + String.valueOf(System.currentTimeMillis());
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), image, filename, null);

        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType("application/image");
        sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(path));
        startActivity(Intent.createChooser(sendIntent, "Share Image"));
    }
}
