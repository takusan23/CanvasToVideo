package io.github.takusan23.canvastovideo

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

enum class MainScreenPath(val path: String) {
    HomeScreen("home_screen"),
    BasicScreen("basic_screen"),
    SlideShowScreen("slideshow_screen"),
    EndRollScreen("end_roll_screen")
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = MainScreenPath.HomeScreen.path) {
        composable(MainScreenPath.HomeScreen.path) { HomeScreen { navController.navigate(it.path) } }
        composable(MainScreenPath.BasicScreen.path) { BasicScreen() }
        composable(MainScreenPath.SlideShowScreen.path) { SlideShowScreen() }
        composable(MainScreenPath.EndRollScreen.path) { EndRollScreen() }
    }
}