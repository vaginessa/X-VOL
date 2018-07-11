package github.tornaco.x.keys

import android.app.Activity
import android.os.Bundle
import android.widget.Switch
import android.widget.TextView
import github.tornaco.x.keys.settings.Settings

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.main)

        val pwmReady: Boolean = Settings().isReady(this)
        findViewById<TextView>(R.id.textView).text = if (pwmReady) "已激活" else "未激活或出错"

        if (pwmReady) {
            findViewById<Switch>(R.id.enabler).isChecked = Settings().isEnabled(this)
            findViewById<Switch>(R.id.enabler).setOnClickListener {
                Settings().setEnabled(applicationContext, findViewById<Switch>(R.id.enabler).isChecked)
            }
        }
        findViewById<Switch>(R.id.enabler).isEnabled = pwmReady
    }
}
