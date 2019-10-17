package com.befovy.flutter_volume

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar
import java.lang.ref.WeakReference
import kotlin.math.max
import kotlin.math.min

class FlutterVolumePlugin(registrar: Registrar) : MethodCallHandler {

    companion object {
        @JvmStatic
        fun registerWith(registrar: Registrar) {
            val channel = MethodChannel(registrar.messenger(), "com.befovy.flutter_volume")

            plugin = FlutterVolumePlugin(registrar)
            channel.setMethodCallHandler(plugin)
        }
        private const val VOLUME_CHANGED_ACTION = "android.media.VOLUME_CHANGED_ACTION"

        private lateinit var plugin: FlutterVolumePlugin
    }

    private val mRegistrar: Registrar = registrar
    private val mEventSink = QueuingEventSink()
    private var mEventChannel: EventChannel? = null
    private var mWatching: Boolean = false
    private var mVolumeReceiver: VolumeReceiver? = null

    private var volStep = 1.0f / 16.0f

    private var minStep: Float = 0.0f

    init {
        val max = audioManager().getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        minStep = 1.0f / max.toFloat()
        volStep = max(minStep, volStep)
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        when (call.method) {
            "up" -> {
                var stepUp = volStep
                if (call.hasArgument("step")) {
                    val step = call.argument<Double>("step")
                    stepUp = step?.toFloat() ?: stepUp
                }
                stepUp = max(minStep, stepUp)
                result.success(volumeUp(stepUp))
            }
            "down" -> {
                var stepDown = volStep
                if (call.hasArgument("step")) {
                    val step = call.argument<Double>("step")
                    stepDown = step?.toFloat() ?: stepDown
                }
                stepDown = max(minStep, stepDown)
                result.success(volumeUp(-stepDown))
            }
            "mute" -> {
                result.success(setVolume(0.0f))
            }
            "get" -> {
                result.success(getVolume())
            }
            "set" -> {
                var vol = getVolume()
                if (call.hasArgument("vol")) {
                    val v = call.argument<Double>("vol")
                    vol = setVolume(v!!.toFloat())
                }
                result.success(vol)
            }
            "enable_watch" -> {
                enableWatch()
                result.success(null)
            }
            "disable_watch" -> {
                disableWatch()
                result.success(null)
            }
            else -> result.notImplemented()
        }
    }

    private fun audioManager(): AudioManager {
        val activity = mRegistrar.activity()
        return activity.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    private val flag: Int
        get() {
            return AudioManager.FLAG_SHOW_UI
        }

    fun getVolume(type: Int = AudioManager.STREAM_MUSIC): Float {
        val audioManager = audioManager()
        val max = audioManager.getStreamMaxVolume(type).toFloat()
        val vol = audioManager.getStreamVolume(type).toFloat()
        return vol / max
    }

    private fun setVolume(vol: Float, type: Int = AudioManager.STREAM_MUSIC): Float {
        val audioManager = audioManager()
        val max = audioManager.getStreamMaxVolume(type)
        var volIndex = (vol * max).toInt()
        volIndex = min(volIndex, max)
        volIndex = max(volIndex, 0)
        audioManager.setStreamVolume(type, volIndex, flag)
        return volIndex.toFloat() / max.toFloat()
    }

    private fun volumeUp(step: Float, type: Int = AudioManager.STREAM_MUSIC): Float {
        var vol = getVolume(type) + step
        vol = setVolume(vol, type)
        return vol
    }

    private fun enableWatch() {

        if (!mWatching) {
            mWatching = true
            mEventChannel = EventChannel(mRegistrar.messenger(), "com.befovy.flutter_volume/event")
            mEventChannel!!.setStreamHandler(object : EventChannel.StreamHandler {
                override fun onListen(o: Any?, eventSink: EventChannel.EventSink) {
                    mEventSink.setDelegate(eventSink)
                }

                override fun onCancel(o: Any?) {
                    mEventSink.setDelegate(null)
                }
            })

            mVolumeReceiver = VolumeReceiver(this)
            val filter = IntentFilter()
            filter.addAction(VOLUME_CHANGED_ACTION)
            mRegistrar.activeContext().registerReceiver(mVolumeReceiver, filter)
        }

    }

    private fun disableWatch() {
        if (mWatching) {
            mWatching = false
            mEventChannel!!.setStreamHandler(null)
            mEventChannel = null

            mRegistrar.activeContext().unregisterReceiver(mVolumeReceiver)
            mVolumeReceiver = null
        }
    }

    fun sink(event: Any) {
        mEventSink.success(event)
    }

}


private class VolumeReceiver(plugin: FlutterVolumePlugin) : BroadcastReceiver() {
    private var mPlugin: WeakReference<FlutterVolumePlugin> = WeakReference(plugin)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.media.VOLUME_CHANGED_ACTION") {
            val plugin = mPlugin.get()
            if (plugin != null) {
                val volume = plugin.getVolume()
                val event: MutableMap<String, Any> = mutableMapOf()
                event["event"] = "vol"
                event["v"] = volume
                event["t"] = AudioManager.STREAM_MUSIC
                plugin.sink(event)
            }
        }
    }
}
