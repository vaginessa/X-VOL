package github.tornaco.x.keys.util;

import android.os.Build;

/**
 * Created by guohao4 on 2017/10/24.
 * Email: Tornaco@163.com
 */

public abstract class OSUtil {

    public static boolean isMOrAbove() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    public static boolean isNOrAbove() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;
    }

    public static boolean isOOrAbove() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }

    public static boolean isLOrBelow() {
        return Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1;
    }

    public static boolean isLenovoDevice() {
        return Build.MANUFACTURER.contains("ZUK") || Build.MANUFACTURER.contains("Lenovo");
    }

    public static boolean isNTDDevice() {
        return Build.MANUFACTURER.contains("NTD");
    }

    public static boolean isHuaWeiDevice() {
        return Build.MANUFACTURER.contains("HUAWEI");
    }

    public static boolean isNubiaDevice() {
        return Build.FINGERPRINT.contains("nubia");
    }
}
