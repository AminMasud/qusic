package com.qusic.noteguesstrainer

import android.app.Application

class NoteGuessTrainerApplication : Application() {
    val appContainer: AppContainer by lazy {
        AppContainer(this)
    }
}
