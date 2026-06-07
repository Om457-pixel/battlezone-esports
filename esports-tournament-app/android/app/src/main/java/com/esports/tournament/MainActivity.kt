package com.esports.tournament

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.esports.tournament.ui.navigation.AppNavigation
import com.esports.tournament.ui.theme.DarkBackground
import com.esports.tournament.ui.theme.EsportsTournamentTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EsportsTournamentTheme {
                AppNavigation()
            }
        }
    }
}
