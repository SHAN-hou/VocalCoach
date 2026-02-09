package com.vocalcoach.app.ui.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Tasks : Screen("tasks")
    data object Practice : Screen("practice/{taskId}") {
        fun createRoute(taskId: Long) = "practice/$taskId"
    }
    data object Score : Screen("score")
    data object Lessons : Screen("lessons")
    data object LessonDetail : Screen("lesson_detail/{lessonId}") {
        fun createRoute(lessonId: Long) = "lesson_detail/$lessonId"
    }
    data object Profile : Screen("profile")
    data object Achievements : Screen("achievements")
}
