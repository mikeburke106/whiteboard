package com.burkeapps.whiteboard.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.burkeapps.whiteboard.R;

/**
 * A WhiteboardView provides a blank whiteboard which is available for drawing.  Different colors
 * of markers can be used on the whiteboard and previous drawings can be erased using an eraser
 * (marker with a white color) or using the clear() method.
 */
public class WhiteboardView extends View {

    // TODO: Read these values from resources
    private static int DEFAULT_MARKER_COLOR = Color.BLACK;
    private static int DEFAULT_MARKER_THICKNESS = 30;
    // TODO: using a white color for eraser won't work with any non-white background
    private static int DEFAULT_ERASER_COLOR = Color.WHITE;

    /**
     * Constant indicating current whiteboard is in erase mode.
     */
    public static int MODE_ERASER = 0;
    /**
     * Constant indicating current whiteboard is in marker mode.
     */
    public static int MODE_MARKER = 1;

    Path touchPath;
    Paint touchPaint, canvasPaint;
    Bitmap canvasBitmap;
    Canvas touchCanvas;
    int canvasHeight, canvasWidth;
    int markerColor = DEFAULT_MARKER_COLOR;
    int eraserColor = DEFAULT_ERASER_COLOR;
    int touchMode = MODE_MARKER;
    int markerThickness = DEFAULT_MARKER_THICKNESS;

    public WhiteboardView(Context context) {
        super(context);
        init(null, 0);
    }

    public WhiteboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public WhiteboardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(attrs,
                R.styleable.WhiteboardView, defStyle, 0);

        markerColor = a.getColor(R.styleable.WhiteboardView_markerColor, markerColor);
        markerThickness = a.getDimensionPixelSize(R.styleable.WhiteboardView_markerThickness,
                markerThickness);
        touchMode = a.getInt(R.styleable.WhiteboardView_touchMode, touchMode);
        Log.d("WhiteboardView", "touchMode: " + touchMode);

        // lazily instantiate our objects
        initTouchPaint();
        initCanvasPaint();
        initTouchPath();
    }

    private void initTouchPaint(){
        if(touchPaint == null) {
            touchPaint = new Paint();
            touchPaint.setAntiAlias(true);
            touchPaint.setColor( (touchMode == MODE_ERASER) ? eraserColor : markerColor);
            touchPaint.setStyle(Paint.Style.STROKE);
            touchPaint.setStrokeWidth(markerThickness);
        }
    }

    private void initCanvasPaint() {
        if(canvasPaint == null) {
            canvasPaint = new Paint();
        }
    }

    private void initTouchPath() {
        if(touchPath == null) {
            touchPath = new Path();
        }

        // ensure path is in an empty state
        touchPath.reset();
    }

    private void initCanvas(){
        canvasBitmap = Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888);
        touchCanvas = new Canvas(canvasBitmap);
    }

    private void initCanvas(int width, int height){
        this.canvasWidth = width;
        this.canvasHeight = height;
        initCanvas();
    }

    @Override
    protected void onSizeChanged (int w, int h, int oldw, int oldh){
        super.onSizeChanged(w,h,oldw,oldh);

        // the size of the whiteboard changed, re-initialize the canvas
        initCanvas(w, h);
    }

    @Override
    public boolean onTouchEvent (MotionEvent event){
        int action = event.getAction();
        switch(action){
            case MotionEvent.ACTION_DOWN:
                // user touched the screen - start path at this point
                touchPath.moveTo(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_MOVE:
                // user moved their touch, connect the two points with a line
                touchPath.lineTo(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_UP:
                // user released their touch, draw it on the canvas and reset the path
                touchCanvas.drawPath(touchPath, touchPaint);
                touchPath.reset();
                break;
            default:
                // don't care about this touch event - tell system we didn't process it
                return false;
        }

        // re-draw and tell system we processed this event by returning true
        invalidate();
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // draw the existing bitmap of our canvas
        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        // draw the touch path over the bitmap
        canvas.drawPath(touchPath, touchPaint);
    }

    /* ********************************************************************************************
    *                                       PUBLIC APIs
    * ********************************************************************************************/

    /**
     * Clears all marks on the whiteboard.
     */
    public void clear(){
        // re-initialize our touch objects
        initTouchPath();
        initCanvas();

        // re-draw the whiteboard
        invalidate();
    }

    /**
     * Sets the color of the current marker.
     *
     * <b>Note: If whiteboard is in MODE_ERASER, this change will not activate
     * until a drawable mode is entered.</b>
     *
     * @param color Color of the marker to set
     */
    public void setDrawColor(int color){
        markerColor = color;
        if(touchMode != MODE_ERASER){
            touchPaint.setColor(markerColor);
        }
    }

    /**
     * Enters whiteboard into MODE_ERASER.
     */
    public void activateEraser(){
        touchMode = MODE_ERASER;
        touchPaint.setColor(eraserColor);
    }

    /**
     * Enters whiteboard into MODE_MARKER.
     */
    public void activateMarker(){
        touchMode = MODE_MARKER;
        touchPaint.setColor(markerColor);
    }

    /**
     * Returns the current mode of this whiteboard.
     *
     * @return The mode of this whiteboard
     */
    public int getTouchMode(){
        return touchMode;
    }
}
