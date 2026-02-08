package com.vocalcoach.app

import android.app.Application
import com.vocalcoach.app.data.local.AppDatabase

class VocalCoachApp : Application() {
    val database: AppDatabase by lazy {
        AppDatabase.getDatabase(this)
    }
}
