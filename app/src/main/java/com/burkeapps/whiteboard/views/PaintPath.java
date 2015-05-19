package com.burkeapps.whiteboard.views;

import android.graphics.Paint;
import android.graphics.Path;

/**
 * This class composes a paint with an associated path object.
 *
 * Created by Mike on 5/19/2015.
 */
public class PaintPath {

    Path path;
    Paint paint;

    PaintPath(Paint paint, Path path){
        this.paint = paint;
        this.path = path;
    }

    public Paint getPaint() {
        return paint;
    }

    public Path getPath() {
        return path;
    }
}
