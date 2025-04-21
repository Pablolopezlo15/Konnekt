package pl.konnekt.ui.components

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavController

val LocalNavController = staticCompositionLocalOf<NavController> { 
    error("No NavController provided") 
}