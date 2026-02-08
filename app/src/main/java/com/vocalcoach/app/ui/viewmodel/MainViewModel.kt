package com.vocalcoach.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vocalcoach.app.VocalCoachApp
import com.vocalcoach.app.data.model.*
import com.vocalcoach.app.data.repository.VocalCoachRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: VocalCoachRepository

    init {
        val database = (application as VocalCoachApp).database
        repository = VocalCoachRepository(database)
    }

    // User Progress
    val userProgress: StateFlow<UserProgress?> = repository.getUserProgress()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // All Lessons
    val allLessons: StateFlow<List<Lesson>> = repository.getAllLessons()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Achievements
    val achievements: StateFlow<List<Achievement>> = repository.getAllAchievements()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Recent Records
    val recentRecords: StateFlow<List<PracticeRecord>> = repository.getRecentRecords(20)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Current day tasks
    private val _currentDayTasks = MutableStateFlow<List<DailyTask>>(emptyList())
    val currentDayTasks: StateFlow<List<DailyTask>> = _currentDayTasks.asStateFlow()

    // Selected task for detail/practice
    private val _selectedTask = MutableStateFlow<DailyTask?>(null)
    val selectedTask: StateFlow<DailyTask?> = _selectedTask.asStateFlow()

    // Selected lesson
    private val _selectedLesson = MutableStateFlow<Lesson?>(null)
    val selectedLesson: StateFlow<Lesson?> = _selectedLesson.asStateFlow()

    // Score result after completing a task
    private val _lastScoreResult = MutableStateFlow<PracticeRecord?>(null)
    val lastScoreResult: StateFlow<PracticeRecord?> = _lastScoreResult.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Show celebration
    private val _showCelebration = MutableStateFlow(false)
    val showCelebration: StateFlow<Boolean> = _showCelebration.asStateFlow()

    init {
        // Load tasks for current day when progress changes
        viewModelScope.launch {
            userProgress.collect { progress ->
                progress?.let {
                    loadTasksForDay(it.currentDay)
                }
            }
        }
    }

    private fun loadTasksForDay(dayNumber: Int) {
        viewModelScope.launch {
            repository.getTasksForDay(dayNumber).collect { tasks ->
                _currentDayTasks.value = tasks
            }
        }
    }

    fun selectTask(task: DailyTask) {
        _selectedTask.value = task
        viewModelScope.launch {
            val lesson = repository.getLessonById(task.lessonId)
            _selectedLesson.value = lesson
        }
    }

    fun completeTask(taskId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val record = repository.completeTask(taskId)
                _lastScoreResult.value = record
                if (record.score >= 80) {
                    _showCelebration.value = true
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun dismissCelebration() {
        _showCelebration.value = false
    }

    fun clearScoreResult() {
        _lastScoreResult.value = null
    }
}
