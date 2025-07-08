// app/src/main/java/com/example/eventsityapp/NavRoutes.kt
package com.example.eventsityapp

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.eventsityapp.ui.*
import com.example.eventsityapp.viewmodel.AuthViewModel
import com.example.eventsityapp.viewmodel.EventViewModel

object NavRoutes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val EVENT_LIST = "event_list"
    const val EVENT_CREATE = "event_create"
    const val EVENT_EDIT = "event_edit/{eventId}"
    const val EVENT_SEARCH = "event_search/{query}"
    const val PROFILE = "profile"
}

@Composable
fun AppNavigation(eventViewModel: EventViewModel, authViewModel: AuthViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = NavRoutes.LOGIN) {
        composable(NavRoutes.LOGIN) {
            LoginScreen(navController, authViewModel)
        }
        composable(NavRoutes.REGISTER) {
            RegistrationScreen(navController, authViewModel)
        }
        composable(NavRoutes.EVENT_LIST) {
            EventListScreen(navController, eventViewModel)
        }
        composable(NavRoutes.EVENT_CREATE) {
            EventCreateScreen(navController, eventViewModel)
        }
        composable(
            route = NavRoutes.EVENT_EDIT,
            arguments = listOf(navArgument("eventId") { type = NavType.IntType })
        ) { backStackEntry ->
            EventCreateScreen(
                navController,
                eventViewModel,
                eventId = backStackEntry.arguments?.getInt("eventId")
            )
        }
        composable(
            route = NavRoutes.EVENT_SEARCH,
            arguments = listOf(navArgument("query") { type = NavType.StringType })
        ) { backStackEntry ->
            EventSearchScreen(
                navController,
                eventViewModel,
                initialQuery = backStackEntry.arguments?.getString("query") ?: ""
            )
        }
        composable(NavRoutes.PROFILE) {
            ProfileScreen(navController, eventViewModel, authViewModel)
        }
    }
}