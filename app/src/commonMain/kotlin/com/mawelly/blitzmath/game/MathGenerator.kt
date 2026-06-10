package com.mawelly.blitzmath.game

import com.mawelly.blitzmath.localization.Strings
import kotlin.random.Random

data class Question(
    val displayText: String,
    val correctAnswer: Int,
    val options: List<Int>,
    val hint: String
)

object MathGenerator {

    fun generateQuestion(config: CheckpointConfig): Question {
        return when (config.operationType) {
            OperationType.ADDITION -> generateAddition(config)
            OperationType.SUBTRACTION -> generateSubtraction(config)
            OperationType.MULTIPLICATION -> generateMultiplication(config)
            OperationType.DIVISION -> generateDivision(config)
            OperationType.MIXED -> generateMixed(config)
        }
    }

    private fun generateAddition(config: CheckpointConfig): Question {
        val range = config.numberRange
        val a = Random.nextInt(range.first, range.last + 1)
        val b = Random.nextInt(range.first, range.last + 1)
        val correct = a + b

        return createQuestion(
            text = "$a + $b",
            correct = correct,
            config = config,
            hint = Strings.hintAdd
        )
    }

    private fun generateSubtraction(config: CheckpointConfig): Question {
        val range = config.numberRange
        val a = Random.nextInt(range.first, range.last + 1)
        val b = Random.nextInt(range.first, a.coerceAtLeast(range.first + 1))
        val correct = a - b

        return createQuestion(
            text = "$a - $b",
            correct = correct,
            config = config,
            hint = Strings.hintSubtract
        )
    }

    private fun generateMultiplication(config: CheckpointConfig): Question {
        val range = config.numberRange
        
        // Çarpma işlemi çok hızlı büyüdüğü için sayı aralığını %66 (3'te 1'e) küçültüyoruz
        val multMax = (range.last / 3.0).toInt().coerceAtLeast(4)
        val multMin = (range.first / 2.0).toInt().coerceAtLeast(1).coerceAtMost(multMax - 1)
        
        val a = Random.nextInt(multMin, multMax + 1)
        val b = Random.nextInt(multMin, multMax + 1)
        val correct = a * b

        return createQuestion(
            text = "$a × $b",
            correct = correct,
            config = config,
            hint = Strings.hintMultiply
        )
    }

    private fun generateDivision(config: CheckpointConfig): Question {
        val range = config.numberRange
        
        // Bölme için de aralığı küçült
        val divMax = (range.last / 3.0).toInt().coerceAtLeast(4)
        val divMin = (range.first / 2.0).toInt().coerceAtLeast(2).coerceAtMost(divMax - 1)

        // DÜZELTİLDİ: Tam bölünecek şekilde üret
        val b = Random.nextInt(divMin, divMax + 1)
        val correct = Random.nextInt(divMin, divMax + 1)
        val a = b * correct

        return createQuestion(
            text = "$a ÷ $b",
            correct = correct,
            config = config,
            hint = Strings.hintDivide
        )
    }

    private fun generateMixed(config: CheckpointConfig): Question {
        val operations = listOf(
            OperationType.ADDITION,
            OperationType.SUBTRACTION,
            OperationType.MULTIPLICATION,
            OperationType.DIVISION
        )
        val randomOp = operations.random()
        val newConfig = config.copy(operationType = randomOp)
        return generateQuestion(newConfig)
    }

    private fun createQuestion(
        text: String,
        correct: Int,
        config: CheckpointConfig,
        hint: String
    ): Question {
        val options = generateSmartOptions(correct, config)

        return Question(
            displayText = text,
            correctAnswer = correct,
            options = options.shuffled(),
            hint = hint
        )
    }

    // DÜZELTİLDİ: Akıllı seçenek üretimi - cevaplara göre değişken zorluk
    private fun generateSmartOptions(correct: Int, config: CheckpointConfig): List<Int> {
        val options = mutableListOf(correct)
        var attempts = 0
        val maxAttempts = 100

        // Zorluk seviyesine göre offset aralığı belirle
        val (minOffset, maxOffset) = when {
            correct <= 20 -> 2 to 8      // Küçük sayılar: 2-8 arası fark
            correct <= 50 -> 5 to 15     // Orta sayılar: 5-15 arası fark
            correct <= 100 -> 10 to 25   // Büyük sayılar: 10-25 arası fark
            else -> 15 to 40             // Çok büyük: 15-40 arası fark
        }

        while (options.size < 3 && attempts < maxAttempts) {
            // Rastgele offset (pozitif veya negatif)
            val offset = if (Random.nextBoolean()) {
                Random.nextInt(minOffset, maxOffset + 1)
            } else {
                -Random.nextInt(minOffset, maxOffset + 1)
            }

            val wrong = correct + offset

            // Kurallar:
            // 1. Doğru cevap olmamalı
            // 2. Pozitif olmalı
            // 3. Daha önce eklenmemiş olmalı
            // 4. Doğru cevaptan en az minOffset uzakta olmalı (çok yakın olmamalı)
            if (wrong != correct &&
                wrong > 0 &&
                wrong !in options &&
                kotlin.math.abs(wrong - correct) >= minOffset
            ) {
                options.add(wrong)
            }
            attempts++
        }

        // Yeterli seçenek üretilemezse basit yedekleme
        while (options.size < 3) {
            val backup = correct + Random.nextInt(minOffset, maxOffset * 2) * if (Random.nextBoolean()) 1 else -1
            if (backup > 0 && backup !in options) {
                options.add(backup)
            }
        }

        return options
    }
}