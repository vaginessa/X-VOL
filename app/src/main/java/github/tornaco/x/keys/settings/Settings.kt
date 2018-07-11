package github.tornaco.x.keys.settings

import android.content.Context
import android.provider.Settings

/**
 * Created by Tornaco on 2018/7/11 15:46.
 * This file is writen for project X-Keys at host guohao4.
 */
public class Settings {

    private val KEY_ENABLED = "X-KEYS-ENABLED"
    private val KEY_READY = "X-KEYS-READY"

    public fun isEnabled(context: Context): Boolean {
        return Settings.System.getInt(context.contentResolver, KEY_ENABLED, 0) == 1
    }

    public fun setEnabled(context: Context, enable: Boolean) {
        Settings.System.putInt(context.contentResolver, KEY_ENABLED, if (enable) 1 else 0)
    }

    public fun isReady(context: Context): Boolean {
        return Settings.System.getInt(context.contentResolver, KEY_READY, 0) == 1
    }

    public fun setReady(context: Context, enable: Boolean) {
        Settings.System.putInt(context.contentResolver, KEY_READY, if (enable) 1 else 0)
    }
}