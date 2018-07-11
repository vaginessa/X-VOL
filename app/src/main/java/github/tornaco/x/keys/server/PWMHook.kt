package github.tornaco.x.keys.server

import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.KeyEvent
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import github.tornaco.x.keys.BuildConfig
import github.tornaco.x.keys.settings.Settings
import github.tornaco.x.keys.util.OSUtil


/**
 * Created by Tornaco on 2018/7/11 13:00.
 * This file is writen for project X-Keys at host guohao4.
 */
class PWMHook : IXposedHookLoadPackage {

    private val LOG_TAG = "X-Keys-PWM-"

    private val PWM_CLASS_NAME: String =
            if (OSUtil.isMOrAbove()) "com.android.server.policy.PhoneWindowManager"
            else "com.android.internal.policy.impl.PhoneWindowManager"

    private val ANDROID_PACKAGE_NAME: String = "android"
    private val INTERCEPT_METHOD_NAME: String = "interceptKeyBeforeDispatching"
    private val INIT_METHOD_NAME: String = "init"

    private val interceptor: KeyEventInterceptor = KeyEventInterceptor()

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam?) {
        val loadingPackage: String = lpparam?.packageName ?: "noop"
        if (ANDROID_PACKAGE_NAME == loadingPackage) {
            handleLoadAndroid(lpparam)
        }
    }

    private fun handleLoadAndroid(lpparam: XC_LoadPackage.LoadPackageParam?) {
        if (lpparam != null) {
            XposedBridge.log(LOG_TAG + "Android starting...")

            val clz: Class<*>? = XposedHelpers.findClass(PWM_CLASS_NAME, lpparam.classLoader)
            XposedBridge.log(LOG_TAG + "Hooking pwm class " + clz)

            if (clz != null) {
                // Hook init.
                val constructorHooks: Set<*> = XposedBridge.hookAllMethods(clz,
                        INIT_METHOD_NAME,
                        object : XC_MethodHook() {
                            override fun beforeHookedMethod(param: MethodHookParam?) {
                                super.beforeHookedMethod(param)
                                // Publish KeyEventInterceptor.
                                if (param != null) {
                                    val context: Context = param.args[0] as Context
                                    interceptor.pwmReady(context)
                                }
                            }
                        })


                // Hook methods.
                val methodHooks: Set<*> = XposedBridge.hookAllMethods(clz, INTERCEPT_METHOD_NAME,
                        object : XC_MethodHook() {
                            override fun beforeHookedMethod(param: MethodHookParam?) {
                                super.beforeHookedMethod(param)
                                interceptor.interceptKeyBeforeDispatching(param)
                            }
                        })

                XposedBridge.log(LOG_TAG + "Hooked pwm class " + constructorHooks)
                XposedBridge.log(LOG_TAG + "Hooked pwm class " + methodHooks)

                interceptor.methodHooked = methodHooks.isNotEmpty()
            }
        }
    }


    class KeyEventInterceptor : Handler.Callback {

        private val LOG_TAG = "X-Keys-KeyEventInterceptor-"
        private val DEBUG_KEY_CODE: Boolean = BuildConfig.DEBUG

        private val RETURN_CODE_CONSUMED: Int = -1

        private val MSG_VOL_ADJUST: Int = 1

        private val LONG_PRESS_TIME_MILLS: Long = 1500

        private var mHandler: Handler? = null
        private var mContext: Context? = null

        var methodHooked: Boolean = false

        override fun handleMessage(msg: Message?): Boolean {
            try {
                if (msg?.what ?: Int.MAX_VALUE == MSG_VOL_ADJUST) {
                    val code = msg?.obj
                    KeyEventSender.injectKey(
                            if (code == KeyEvent.KEYCODE_VOLUME_UP) KeyEvent.KEYCODE_MEDIA_PREVIOUS
                            else KeyEvent.KEYCODE_MEDIA_NEXT,
                            true, true)
                    XposedBridge.log(LOG_TAG + "send MSG_VOL_ADJUST")
                }
            } catch (e: Throwable) {
                XposedBridge.log(LOG_TAG + "FATAL: " + Log.getStackTraceString(e))
            }
            return true
        }

        fun pwmReady(context: Context) {
            mHandler = Handler(this)
            mContext = context
            XposedBridge.log(LOG_TAG + "pwmReady, handler: " + mHandler + " context: " + context)

            val ready = mHandler != null && methodHooked
            Settings().setReady(context, ready)

            // Grant permission.
            val pm: PackageManager = context.packageManager
            // pm.grantRuntimePermission(BuildConfig.APPLICATION_ID, android.Manifest.permission.WRITE_SETTINGS, UserHandle.CURRENT)
        }

        fun interceptKeyBeforeDispatching(param: XC_MethodHook.MethodHookParam?) {
            if (param != null && mHandler != null && isMusicActive()) {
                XposedBridge.log(LOG_TAG + "interceptKeyBeforeDispatching")

                val event: KeyEvent = param.args[1] as KeyEvent
                val flags: Int = event.flags
                val longPressOrig: Boolean = flags and KeyEvent.FLAG_LONG_PRESS != 0
                val down: Boolean = event.action == KeyEvent.ACTION_DOWN
                val canceled: Boolean = event.isCanceled
                val keyCode: Int = event.keyCode
                val source: Int = event.source
                val system: Boolean = flags and KeyEvent.FLAG_FROM_SYSTEM != 0

                if (DEBUG_KEY_CODE) {
                    XposedBridge.log(LOG_TAG + "interceptKeyTi keyCode=" + keyCode
                            + " down=" + down
                            + " system=" + system
                            + " source=" + source
                            + " longPressOrig=" + longPressOrig
                            + " canceled=" + canceled)
                }

                if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                    if (down) {
                        if (source != KeyEventSender.SOURCE_CLASS_X) {
                            mHandler!!.removeMessages(MSG_VOL_ADJUST)
                            mHandler!!.sendMessageDelayed(mHandler!!.obtainMessage(MSG_VOL_ADJUST, keyCode), LONG_PRESS_TIME_MILLS)
                            KeyEventSender.injectKey(keyCode, true, false)
                            param.result = RETURN_CODE_CONSUMED
                        }
                    } else {
                        // Key released.
                        mHandler!!.removeMessages(MSG_VOL_ADJUST)
                    }
                }
            }
        }

        /**
         * @return Whether music is being played right now "locally" (e.g. on the device's speakers
         * or wired headphones) or "remotely" (e.g. on a device using the Cast protocol and
         * controlled by this device, or through remote submix).
         */
        private fun isMusicActive(): Boolean {
            val am = mContext?.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            return am.isMusicActive
        }
    }
}