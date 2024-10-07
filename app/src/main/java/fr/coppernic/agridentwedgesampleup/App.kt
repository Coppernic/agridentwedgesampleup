package fr.coppernic.agridentwedgesampleup

import android.app.Application
import fr.coppernic.agridentwedgesampleup.di.AppModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.ksp.generated.module

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(AppModule().module)
        }
    }
}
