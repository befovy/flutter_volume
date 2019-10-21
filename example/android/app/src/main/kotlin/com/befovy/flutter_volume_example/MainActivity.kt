package com.befovy.flutter_volume_example

import android.os.Bundle
import android.view.KeyEvent
import com.befovy.flutter_volume.CanListenVolumeKey
import com.befovy.flutter_volume.VolumeKeyListener

import io.flutter.app.FlutterActivity
import io.flutter.plugins.GeneratedPluginRegistrant

class MainActivity : FlutterActivity(), CanListenVolumeKey {

    var mlistener: VolumeKeyListener? = null
    override fun setVolumeKeyListener(listener: VolumeKeyListener?) {
        mlistener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GeneratedPluginRegistrant.registerWith(this)
    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_MUTE,
            KeyEvent.KEYCODE_VOLUME_UP,
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                if (mlistener != null) {
                    mlistener!!.onVolumeKeyDown(keyCode, event)
                } else {
                    super.onKeyDown(keyCode, event)
                }
            }
            else -> {
                super.onKeyDown(keyCode, event)

            }
        }
    }

}
