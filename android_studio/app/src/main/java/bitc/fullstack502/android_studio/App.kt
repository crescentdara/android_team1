package bitc.fullstack502.android_studio

import android.app.Application
import bitc.fullstack502.android_studio.util.AuthManager

class App : Application() {
    companion object { lateinit var instance: App; private set }
    override fun onCreate() {
        super.onCreate()
        instance = this
        AuthManager.init(this)
    }
}
