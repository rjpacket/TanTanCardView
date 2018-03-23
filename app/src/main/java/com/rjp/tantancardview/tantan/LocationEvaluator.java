package com.rjp.tantancardview.tantan;

import android.animation.TypeEvaluator;
import android.graphics.Point;

/**
 * author : Gimpo create on 2018/3/22 15:16
 * email  : jimbo922@163.com
 */

public class LocationEvaluator implements TypeEvaluator<Point> {
    @Override
    public Point evaluate(float fraction, Point sp, Point ep) {
        Point p = new Point();
        p.x = (int) (sp.x + (ep.x - sp.x) * fraction);
        p.y = (int) (sp.y + (ep.y - sp.y) * fraction);
        return p;
    }
}
