package com.mawelly.blitzmath.game

import com.mawelly.blitzmath.data.IGameDataStore
import kotlinx.coroutines.flow.first

object DailyTasksManager {
    
    private const val DAY_MS = 86400000L

    data class Task(
        val id: String,
        val titleTr: String,
        val titleEn: String,
        val descTr: String,
        val descEn: String,
        val target: Int,
        val reward: Int
    )

    val tasks = listOf(
        Task("task_classic", "Klasik Meydan Okuyucu", "Classic Challenger", "1 Klasik oyun oyna", "Play 1 Classic game", 1, 10),
        Task("task_mixed", "Karışık Zihin", "Mixed Mind", "1 Karışık oyun oyna", "Play 1 Mixed game", 1, 15),
        Task("task_score", "Puan Patlaması", "Score Blast", "Bir oyunda 20 veya üzeri puan yap", "Score 20 or more in a game", 20, 20)
    )

    data class TaskState(
        val day: Long,
        val classicPlayed: Int,
        val mixedPlayed: Int,
        val highestScore: Int,
        val claimedTasks: List<String>
    )

    fun parseState(stateStr: String, currentTime: Long): TaskState {
        val today = currentTime / DAY_MS
        if (stateStr.isEmpty()) {
            return TaskState(today, 0, 0, 0, emptyList())
        }
        
        try {
            val parts = stateStr.split("|")
            if (parts.size >= 2) {
                val day = parts[0].toLongOrNull() ?: today
                if (day != today) {
                    // Reset for a new day
                    return TaskState(today, 0, 0, 0, emptyList())
                }
                
                var classic = 0
                var mixed = 0
                var high = 0
                
                val stats = parts[1].split(";")
                stats.forEach { stat ->
                    val kv = stat.split(":")
                    if (kv.size == 2) {
                        when (kv[0]) {
                            "classic" -> classic = kv[1].toIntOrNull() ?: 0
                            "mixed" -> mixed = kv[1].toIntOrNull() ?: 0
                            "high" -> high = kv[1].toIntOrNull() ?: 0
                        }
                    }
                }
                
                val claimed = if (parts.size >= 3 && parts[2].startsWith("claimed:")) {
                    parts[2].removePrefix("claimed:").split(",").filter { it.isNotEmpty() }
                } else {
                    emptyList()
                }
                
                return TaskState(day, classic, mixed, high, claimed)
            }
        } catch (e: Exception) {
            // Ignore parse errors, return empty today state
        }
        return TaskState(today, 0, 0, 0, emptyList())
    }

    fun serializeState(state: TaskState): String {
        val claimedStr = state.claimedTasks.joinToString(",")
        return "${state.day}|classic:${state.classicPlayed};mixed:${state.mixedPlayed};high:${state.highestScore}|claimed:$claimedStr"
    }

    suspend fun updateProgress(dataStore: IGameDataStore, mode: String, score: Int, currentTime: Long) {
        val stateStr = dataStore.dailyTasksClaimed.first()
        val state = parseState(stateStr, currentTime)
        
        val newClassic = if (mode == "classic") state.classicPlayed + 1 else state.classicPlayed
        val newMixed = if (mode == "mixed") state.mixedPlayed + 1 else state.mixedPlayed
        val newHigh = if (score > state.highestScore) score else state.highestScore
        
        val newState = state.copy(
            classicPlayed = newClassic,
            mixedPlayed = newMixed,
            highestScore = newHigh
        )
        
        dataStore.saveDailyTasksClaimed(serializeState(newState))
    }

    fun isTaskCompleted(task: Task, state: TaskState): Boolean {
        return when (task.id) {
            "task_classic" -> state.classicPlayed >= task.target
            "task_mixed" -> state.mixedPlayed >= task.target
            "task_score" -> state.highestScore >= task.target
            else -> false
        }
    }

    fun getTaskProgress(task: Task, state: TaskState): Int {
        return when (task.id) {
            "task_classic" -> state.classicPlayed.coerceAtMost(task.target)
            "task_mixed" -> state.mixedPlayed.coerceAtMost(task.target)
            "task_score" -> state.highestScore.coerceAtMost(task.target)
            else -> 0
        }
    }
}
