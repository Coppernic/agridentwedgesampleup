package fr.coppernic.agridentwedgesampleup

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import fr.coppernic.agridentwedgesampleup.ui.navigation.Routes
import fr.coppernic.agridentwedgesampleup.ui.navigation.SetupNavGraph
import fr.coppernic.agridentwedgesampleup.ui.theme.AgridentWedgeSampleUpTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {

            val navController = rememberNavController()

            AgridentWedgeSampleUpTheme {
                SetupNavGraph(
                    startDestination = Routes.MAIN_SCREEN,
                    navController = navController
                )
            }
        }
    }
}