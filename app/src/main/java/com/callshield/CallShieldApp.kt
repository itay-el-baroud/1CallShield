package com.callshield

import android.app.Application
import com.callshield.data.AppDatabase

class CallShieldApp : Application() {
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
}
