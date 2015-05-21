package com.burkeapps.whiteboard.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.burkeapps.whiteboard.R;

import java.util.LinkedList;

/**
 * A WhiteboardView provides a blank whiteboard which is available for drawing.  Different colors
 * of markers can be used on the whiteboard and previous drawings can be erased using an eraser
 * (marker with a white color) or using the clear() method.
 */
public class WhiteboardView extends View {

    /**
     * Constant indicating current whiteboard is in erase mode.
     */
    public static final int MODE_ERASER = 0;
    /**
     * Constant indicating current whiteboard is in marker mode.
     */
    public static final int MODE_MARKER = 1;

    Path touchPath;
    Paint touchPaint, canvasPaint, backgroundPaint;
    Bitmap canvasBitmap;
    Canvas touchCanvas;
    LinkedList<PaintPath> pathHistory, undoHistory;
    PathListener l;
    int canvasHeight, canvasWidth;
    int markerColor, eraserColor;
    int markerThickness;
    int touchMode = MODE_MARKER;

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

        eraserColor = getDefaultEraserColor();
        markerColor = a.getColor(R.styleable.WhiteboardView_markerColor, getDefaultMarkerColor());
        markerThickness = a.getDimensionPixelSize(R.styleable.WhiteboardView_markerThickness,
                getDefaultMarkerThickness());
        touchMode = a.getInt(R.styleable.WhiteboardView_touchMode, touchMode);
        a.recycle();

        // lazily instantiate our objects
        initTouchPaint();
        initBackgroundPaint();
        initCanvasPaint();
        initTouchPath();
        initHistory();
    }

    private int getDefaultEraserColor() {
        return getContext().getResources().getColor(R.color.whiteboard_default_eraser_color);
    }

    private int getDefaultMarkerThickness() {
        return (int) getContext().getResources().getDimension(R.dimen.whiteboard_marker_thickness);
    }

    private int getDefaultMarkerColor() {
        return getContext().getResources().getColor(R.color.whiteboard_default_marker_color);
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

    private void initBackgroundPaint(){
        if(backgroundPaint == null){
            backgroundPaint = new Paint();
            backgroundPaint.setAntiAlias(true);
            backgroundPaint.setColor(Color.WHITE);
            backgroundPaint.setStyle(Paint.Style.FILL);
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

    private void initHistory() {
        pathHistory = new LinkedList<>();
        undoHistory = new LinkedList<>();
    }

    private void initCanvas(){
        // free up any existing bitmap from memory
        if(canvasBitmap != null){
            canvasBitmap.recycle();
        }

        // and create a new bitmap based on current size
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
    public boolean onTouchEvent (@NonNull MotionEvent event){
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
                recordPath();

                // notify listener that a path was drawn
                if(l != null){
                    l.onPathCompleted();
                }
                break;
            default:
                // don't care about this touch event - tell system we didn't process it
                return false;
        }

        // re-draw and tell system we processed this event by returning true
        invalidate();
        return true;
    }

    private void recordPath() {
        // save the current path to history and reset the path
        // TODO: store paint items in cache to avoid creating duplicate paint objects
        PaintPath paintPath = new PaintPath(new Paint(touchPaint), new Path(touchPath));
        pathHistory.push(paintPath);
        touchPath.reset();

        // as soon as another path has been entered, user can no longer re-do
        undoHistory.clear();
    }

    private void redrawCanvasBitmap() {
        // create a new canvas
        initCanvas();

        // and draw the path history over it
        for(int i=pathHistory.size()-1; i>=0; i--){
            PaintPath paintPath = pathHistory.get(i);
            touchCanvas.drawPath(paintPath.getPath(), paintPath.getPaint());
        }

        invalidate();
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
        initHistory();

        if(l != null){
            l.onPathsCleared();
        }

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
    public void setMarkerColor(int color){
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

    /**
     * Sets the current thickness of the marker/eraser.  The input value should take
     * screen density into account.
     *
     * @param thickness The thickness to set
     */
    public void setMarkerThickness(int thickness){
        markerThickness = thickness;
        touchPaint.setStrokeWidth(thickness);
    }

    /**
     * Undoes the previously drawn path.  If no path has been drawn, does nothing.
     */
    public void undo(){
        if(pathHistory.size() > 0){
            PaintPath undoPath = pathHistory.pop();
            undoHistory.push(undoPath);
            redrawCanvasBitmap();

            // notify listener
            if(l != null){
                l.onPathUndone();
            }
        }
    }

    /**
     * Redoes the previously undone path.  If no path has been undone, does nothing.
     */
    public void redo(){
        if(undoHistory.size() > 0){
            PaintPath lastUndone = undoHistory.pop();
            pathHistory.push(lastUndone);
            redrawCanvasBitmap();

            // notify listener
            if(l != null){
                l.onPathRedone();
            }
        }
    }

    /**
     * Indicates whether the whiteboard can undo a path.
     *
     * @return True if a path can be undone, false otherwise.
     */
    public boolean canUndo(){
        return (pathHistory.size() > 0);
    }

    /**
     * Indicates whether the whiteboard can redo a path.
     *
     * @return True if a path can be redone, false otherwise.
     */
    public boolean canRedo(){
        return (undoHistory.size() > 0);
    }

    /**
     * Sets a path listener for this whiteboard.
     *
     * @param l The listener to set
     */
    public void setPathListener(PathListener l){
        this.l = l;
    }

    /**
     * Redraws the entire whiteboard from scratch
     */
    public void redraw(){
        redrawCanvasBitmap();
    }

    /**
     * Creates a screenshot bitmap of the current status of the whiteboard.
     *
     * @return A bitmap representation of the whiteboard
     */
    public Bitmap screenshot(){
        Bitmap result = Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888);
        Canvas resultCanvas = new Canvas(result);
        resultCanvas.drawPaint(backgroundPaint);
        resultCanvas.drawBitmap(canvasBitmap, 0, 0, null);

        return result;
    }

    /**
     * Interface to listen for path-based events occurring on this whiteboard.
     */
    public interface PathListener{
        void onPathCompleted();
        void onPathUndone();
        void onPathRedone();
        void onPathsCleared();
    }
}
