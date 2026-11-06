package vn.savify.karaoke

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.ConnectivityManager
import android.net.Network
import android.net.wifi.WifiManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import java.net.Inet4Address
import java.net.NetworkInterface

class Core(
    val controller: Controller,
    val server: KaraokeServer,
    val settings: Settings,
    val scope: CoroutineScope,
    ctx: Context
) {
    val ip = MutableStateFlow<String?>(Lan.find(ctx))
    val port: Int get() = server.listeningPort
}

object AppRuntime {
    val core = MutableStateFlow<Core?>(null)
}

object Lan {
    fun find(ctx: Context): String? {
        // 1. the active default network
        try {
            val cm = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val lp = cm.getLinkProperties(cm.activeNetwork)
            if (lp != null) {
                for (la in lp.linkAddresses) {
                    val a = la.address
                    if (a is Inet4Address && a.isSiteLocalAddress) return a.hostAddress
                }
            }
        } catch (e: Exception) {
        }
        // 2. enumerate interfaces: up, non-loopback, site-local IPv4; prefer wlan/eth
        val cands = ArrayList<Pair<String, String>>()
        try {
            val nis = NetworkInterface.getNetworkInterfaces()?.toList() ?: emptyList()
            for (ni in nis) {
                if (!ni.isUp || ni.isLoopback) continue
                for (a in ni.inetAddresses.toList()) {
                    if (a is Inet4Address && a.isSiteLocalAddress) {
                        cands.add(Pair(ni.name, a.hostAddress ?: continue))
                    }
                }
            }
        } catch (e: Exception) {
        }
        val pref = cands.firstOrNull { it.first.startsWith("wlan") || it.first.startsWith("eth") }
        return (pref ?: cands.firstOrNull())?.second
    }
}

class KaraokeService : Service() {
    private var wake: PowerManager.WakeLock? = null
    private var wifi: WifiManager.WifiLock? = null
    private var cm: ConnectivityManager? = null
    private var cb: ConnectivityManager.NetworkCallback? = null
    private var core: Core? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    @Suppress("DEPRECATION")
    override fun onCreate() {
        super.onCreate()
        try {
            startFg()
            if (AppRuntime.core.value != null) return

            val ctx = applicationContext
            val settings = Settings(ctx)
            Np.init()
            val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
            val ctl = Controller(ctx, settings, scope)
            val srv = KaraokeServer.startOnFreePort(ctx, ctl, settings)
            val c = Core(ctl, srv, settings, scope, ctx)
            core = c
            AppRuntime.core.value = c
            ctl.start()

            try {
                val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
                wake = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "karaoke:wake").also { it.acquire() }
            } catch (e: Throwable) {
                CrashLog.write(ctx, "WAKELOCK", e)
            }
            try {
                // Ethernet-only boxes may have no WifiManager at all
                val wm = ctx.getSystemService(Context.WIFI_SERVICE) as? WifiManager
                wifi = wm?.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "karaoke:wifi")?.also { it.acquire() }
            } catch (e: Throwable) {
                CrashLog.write(ctx, "WIFILOCK", e)
            }
            try {
                val mgr = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                cm = mgr
                val callback = object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: Network) { c.ip.value = Lan.find(ctx) }
                    override fun onLost(network: Network) { c.ip.value = Lan.find(ctx) }
                    override fun onLinkPropertiesChanged(network: Network, lp: android.net.LinkProperties) {
                        c.ip.value = Lan.find(ctx)
                    }
                }
                cb = callback
                mgr.registerDefaultNetworkCallback(callback)
            } catch (e: Throwable) {
                CrashLog.write(ctx, "NETCALLBACK", e)
            }
        } catch (e: Throwable) {
            CrashLog.write(applicationContext, "SERVICE", e)
            stopSelf()
        }
    }

    @Suppress("DEPRECATION")
    private fun startFg() {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val b: Notification.Builder = if (Build.VERSION.SDK_INT >= 26) {
            nm.createNotificationChannel(
                NotificationChannel("karaoke", "Savify Karaoke", NotificationManager.IMPORTANCE_LOW)
            )
            Notification.Builder(this, "karaoke")
        } else {
            Notification.Builder(this)
        }
        val n = b.setContentTitle("Savify Karaoke")
            .setContentText("Đang chạy / Running")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .build()
        if (Build.VERSION.SDK_INT >= 29) {
            startForeground(1, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        } else {
            startForeground(1, n)
        }
    }

    override fun onDestroy() {
        AppRuntime.core.value = null
        try {
            cb?.let { cm?.unregisterNetworkCallback(it) }
        } catch (e: Exception) {
        }
        core?.let {
            try {
                it.server.stop()
            } catch (e: Exception) {
            }
            it.controller.release()
            it.scope.cancel()
        }
        try {
            wake?.release()
            wifi?.release()
        } catch (e: Exception) {
        }
        super.onDestroy()
    }
}
