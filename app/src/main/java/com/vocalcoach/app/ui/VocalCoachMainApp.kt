package com.vocalcoach.app.ui

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.vocalcoach.app.ui.navigation.Screen
import com.vocalcoach.app.ui.screens.*
import com.vocalcoach.app.ui.viewmodel.MainViewModel

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VocalCoachMainApp(
    viewModel: MainViewModel = viewModel()
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val bottomNavItems = listOf(
        BottomNavItem(Screen.Home, "首页", Icons.Filled.Home, Icons.Outlined.Home),
        BottomNavItem(Screen.Tasks, "任务", Icons.Filled.Assignment, Icons.Outlined.Assignment),
        BottomNavItem(Screen.Lessons, "课程", Icons.Filled.LibraryMusic, Icons.Outlined.LibraryMusic),
        BottomNavItem(Screen.Profile, "我的", Icons.Filled.Person, Icons.Outlined.Person)
    )

    val showBottomBar = currentDestination?.route in bottomNavItems.map { it.screen.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = NavigationBarDefaults.Elevation
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any {
                            it.route == item.screen.route
                        } == true

                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.label
                                )
                            },
                            label = { Text(item.label) },
                            selected = selected,
                            onClick = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToTasks = {
                        navController.navigate(Screen.Tasks.route)
                    },
                    onNavigateToProfile = {
                        navController.navigate(Screen.Profile.route)
                    },
                    onNavigateToAchievements = {
                        navController.navigate(Screen.Achievements.route)
                    }
                )
            }

            composable(Screen.Tasks.route) {
                TasksScreen(
                    viewModel = viewModel,
                    onTaskClick = { task ->
                        viewModel.selectTask(task)
                        navController.navigate(Screen.Practice.createRoute(task.id))
                    }
                )
            }

            composable(Screen.Practice.route) {
                PracticeScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onComplete = {
                        navController.navigate(Screen.Score.route) {
                            popUpTo(Screen.Tasks.route)
                        }
                    }
                )
            }

            composable(Screen.Score.route) {
                ScoreScreen(
                    viewModel = viewModel,
                    onBack = {
                        viewModel.clearScoreResult()
                        navController.navigate(Screen.Tasks.route) {
                            popUpTo(Screen.Home.route)
                        }
                    },
                    onBackToHome = {
                        viewModel.clearScoreResult()
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Lessons.route) {
                LessonsScreen(viewModel = viewModel)
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    viewModel = viewModel,
                    onNavigateToAchievements = {
                        navController.navigate(Screen.Achievements.route)
                    }
                )
            }

            composable(Screen.Achievements.route) {
                AchievementsScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
