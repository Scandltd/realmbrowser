package com.scand.realmbrowser;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.Nullable;

/**
 * Created by Slabodeniuk on 6/16/15.
 */
public class Utils {

    @Nullable
    public static Drawable getDrawable(Resources res, int id, @Nullable Resources.Theme theme) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return res.getDrawable(id, theme);
        } else {
            return res.getDrawable(id);
        }
    }

}
