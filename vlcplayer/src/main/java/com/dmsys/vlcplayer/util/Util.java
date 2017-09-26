/*****************************************************************************
 * UiTools.java
 * ****************************************************************************
 * Copyright © 2011-2014 VLC authors and VideoLAN
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston MA 02110-1301, USA.
 *****************************************************************************/

package com.dmsys.vlcplayer.util;

import android.content.res.Resources;
import android.support.annotation.Nullable;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;

public class Util {
    public final static String TAG = "VLC/Util";


    public static boolean close(Closeable closeable) {
        if (closeable != null)
            try {
                closeable.close();
                return true;
            } catch (IOException e) {
            }
        return false;
    }

    public static boolean isListEmpty(@Nullable Collection collection) {
        return collection == null || collection.isEmpty();
    }


    public static int getScreentW() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public static int getScreentH() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }




}
