package net.rpcs3

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import net.rpcs3.ui.navigation.AppNavHost
import kotlin.concurrent.thread

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppNavHost()
        }

        RPCS3.rootDirectory = applicationContext.getExternalFilesDir(null).toString()
        if (!RPCS3.rootDirectory.endsWith("/")) {
            RPCS3.rootDirectory += "/"
        }

        GameRepository.load()
        FirmwareRepository.load()

        Permission.PostNotifications.requestPermission(this)

        with(getSystemService(NOTIFICATION_SERVICE) as NotificationManager) {
            val channel = NotificationChannel(
                "rpcs3-progress",
                "Installation progress",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }

            deleteNotificationChannel("rpcs3-progress")
            createNotificationChannel(channel)
        }

        thread {
            RPCS3.instance.startMainThreadProcessor()
        }

        RPCS3.instance.initialize(RPCS3.rootDirectory)

        thread {
            RPCS3.instance.processCompilationQueue()
        }

        listenUsbEvents(this)
    }
}