package ru.infoenergo.mis.helpers

import android.app.Application
import android.content.res.Resources


class App : Application() {

    override fun onCreate() {
        super.onCreate()
        appResources = resources
    }

    companion object {
        lateinit var appResources: Resources

        @JvmStatic
        fun appResources(): Resources {
            return appResources
        }
    }
}