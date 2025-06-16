package com.ud.taller
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ud.taller.screens.HomeScreen
import com.ud.taller.screens.OnlineGameScreen

@Composable
fun NavigationHost() {
    val navController = rememberNavController()

    NavHost(navController, startDestination = "home") {
        composable("home") { HomeScreen(navController) }
        composable("game/{gameId}", arguments = listOf(navArgument("gameId") { type = NavType.StringType })) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId")!!
            OnlineGameScreen(gameId) {
                navController.popBackStack()
            }
        }
    }
}