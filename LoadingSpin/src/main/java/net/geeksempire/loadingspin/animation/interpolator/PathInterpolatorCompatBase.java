/*
 * Copyright © 2020 By Geeks Empire.
 *
 * Created by Elias Fazel on 8/29/20 8:13 AM
 * Last modified 6/26/20 3:50 PM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */

package net.geeksempire.loadingspin.animation.interpolator;

import android.graphics.Path;
import android.view.animation.Interpolator;

/**
 * Base implementation for path interpolator compatibility.
 */
class PathInterpolatorCompatBase {

    private PathInterpolatorCompatBase() {
        // prevent instantiation
    }

    public static Interpolator create(Path path) {
        return new PathInterpolatorDonut(path);
    }

    public static Interpolator create(float controlX, float controlY) {
        return new PathInterpolatorDonut(controlX, controlY);
    }

    public static Interpolator create(float controlX1, float controlY1,
                                      float controlX2, float controlY2) {
        return new PathInterpolatorDonut(controlX1, controlY1, controlX2, controlY2);
    }
}
