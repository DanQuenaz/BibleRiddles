package com.quenazapps.bibleriddles

import android.app.Application
import com.quenazapps.bibleriddles.service.MenuSoundtrack

class BibleRiddlesApplication : Application() {
    internal lateinit var menuSoundtrack: MenuSoundtrack
        private set

    override fun onCreate() {
        super.onCreate()
        menuSoundtrack = MenuSoundtrack(this)
        registerActivityLifecycleCallbacks(menuSoundtrack)
    }
}
