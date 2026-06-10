package com.mawelly.blitzmath.game

// ESKİ: LevelConfig (KALSIN - geriye uyumluluk için)
data class LevelConfig(
    val level: Int,
    val name: String,
    val description: String,
    val timeLimit: Float,
    val numberRange: IntRange,
    val operationTypes: List<OperationType>,
    val questionsToPass: Int = 10
)

// YENİ: Checkpoint sistemi için config
data class CheckpointConfig(
    val checkpointNumber: Int,
    val questionInCheckpoint: Int,
    val operationType: OperationType,
    val difficulty: DifficultyLevel,
    val timeLimit: Float,
    val numberRange: IntRange
)

enum class OperationType {
    ADDITION, SUBTRACTION, MULTIPLICATION, DIVISION, MIXED
}

enum class DifficultyLevel {
    EASY, NORMAL, HARD, EXPERT, MASTER
}

// YENİ: CheckpointManager - TAMAMEN YENİ SİSTEM
object CheckpointManager {

    // Her checkpoint'te farklı işlem (1-4 arası döngü)
    fun getOperationForCheckpoint(checkpoint: Int): OperationType {
        return when ((checkpoint - 1) % 4) {
            0 -> OperationType.ADDITION      // 1, 5, 9...
            1 -> OperationType.SUBTRACTION   // 2, 6, 10...
            2 -> OperationType.MULTIPLICATION// 3, 7, 11...
            3 -> OperationType.DIVISION      // 4, 8, 12...
            else -> OperationType.ADDITION
        }
    }

    fun getDifficulty(checkpoint: Int): DifficultyLevel {
        return when (checkpoint) {
            in 1..20 -> DifficultyLevel.EASY
            in 21..40 -> DifficultyLevel.NORMAL
            in 41..60 -> DifficultyLevel.HARD
            in 61..80 -> DifficultyLevel.EXPERT
            else -> DifficultyLevel.MASTER
        }
    }

    fun getNumberRange(checkpoint: Int): IntRange {
        val op = getOperationForCheckpoint(checkpoint)
        val base = when (op) {
            OperationType.ADDITION -> 10..20
            OperationType.SUBTRACTION -> 5..20
            OperationType.MULTIPLICATION -> 2..10
            OperationType.DIVISION -> 2..6  // Bölme için taban aralığı küçültüldü
            OperationType.MIXED -> 2..20
        }

        // Zorluk arttıkça aralığı genişlet (100 bölüme göre yavaşlatıldı)
        val multiplier = if (op == OperationType.DIVISION) {
            // Bölme için çarpanı çok daha yavaş artır
            (checkpoint / 15) + 1
        } else {
            // Diğer işlemler için 10 bölümde bir artır
            (checkpoint / 10) + 1
        }
        
        // maxLimit 100 bölüme yayıldı
        val dynamicLimit = if (op == OperationType.DIVISION) {
            40 + checkpoint // Bölme limiti level başı +1 artar
        } else {
            100 + (checkpoint * 3) // Diğer limitler level başı +3 artar
        }

        val start = (base.first * multiplier)
        val end = (base.last * multiplier).coerceAtMost(dynamicLimit)
        
        // Çakışma ve dar aralık kontrolü: En az 10-20 fark olmalı
        val minWidth = if (op == OperationType.DIVISION) 15 else 40
        val finalStart = if (end - start < minWidth) (end - minWidth).coerceAtLeast(base.first) else start
        
        val safeEnd = end.coerceAtLeast(finalStart + 5)
        return finalStart..safeEnd
    }

    // Süre de 100 bölüme göre kademeli artıyor (sayılar büyüdükçe süre tanınmalı)
    fun getTimeLimit(checkpoint: Int): Float {
        return when (checkpoint) {
            in 1..20 -> 5f
            in 21..40 -> 6f
            in 41..60 -> 7f
            in 61..80 -> 8f
            else -> 10f
        }
    }

    fun getConfig(checkpoint: Int, questionInCheckpoint: Int): CheckpointConfig {
        return CheckpointConfig(
            checkpointNumber = checkpoint,
            questionInCheckpoint = questionInCheckpoint,
            operationType = getOperationForCheckpoint(checkpoint),
            difficulty = getDifficulty(checkpoint),
            timeLimit = getTimeLimit(checkpoint),
            numberRange = getNumberRange(checkpoint)
        )
    }

    // İşlem adını döndür (UI için)
    fun getOperationName(operation: OperationType, isTurkish: Boolean = true): String {
        return if (isTurkish) {
            when (operation) {
                OperationType.ADDITION -> "TOPLAMA"
                OperationType.SUBTRACTION -> "ÇIKARMA"
                OperationType.MULTIPLICATION -> "ÇARPMA"
                OperationType.DIVISION -> "BÖLME"
                OperationType.MIXED -> "KARIŞIK"
            }
        } else {
            when (operation) {
                OperationType.ADDITION -> "ADDITION"
                OperationType.SUBTRACTION -> "SUBTRACTION"
                OperationType.MULTIPLICATION -> "MULTIPLICATION"
                OperationType.DIVISION -> "DIVISION"
                OperationType.MIXED -> "MIXED"
            }
        }
    }
}