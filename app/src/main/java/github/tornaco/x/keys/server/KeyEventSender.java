package github.tornaco.x.keys.server;

import android.hardware.input.InputManager;
import android.os.SystemClock;
import android.view.InputEvent;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;

import de.robv.android.xposed.XposedHelpers;

/**
 * Created by guohao4 on 2017/11/7.
 * Email: Tornaco@163.com
 */

public class KeyEventSender {

    public static final int SOURCE_CLASS_X = 0x00000080;

    static boolean injectInputEvent(InputEvent event, int mode) {
        InputManager inputManager = InputManager.getInstance();
        try {
            return (boolean) XposedHelpers.callMethod(inputManager,
                    "injectInputEvent", event, mode);
        } catch (Throwable e) {
            return false;
        }
    }

    static void injectKey(int code, boolean withDown, boolean withUp) {
        int flags = KeyEvent.FLAG_FROM_SYSTEM;
        int scancode = 12;
        final long eventTime = SystemClock.uptimeMillis();
        KeyEvent down = new KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN, code, 0,
                0, KeyCharacterMap.VIRTUAL_KEYBOARD, scancode, flags, SOURCE_CLASS_X);
        KeyEvent up = new KeyEvent(eventTime + 10, eventTime + 10, KeyEvent.ACTION_UP, code, 0,
                0, KeyCharacterMap.VIRTUAL_KEYBOARD, scancode, flags, SOURCE_CLASS_X);
        if (withDown) injectInputEvent(down, 0);
        if (withUp) injectInputEvent(up, 0);
    }
}
