

import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class AddRussian {
    public static void main(String[] args) throws Exception {
        Path path = Paths.get("Strings.kt");
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        List<String> outLines = new ArrayList<>();

        Map<String, String> dict = new HashMap<>();
        dict.put("Geçersiz kullanıcı adı! (2-15 karakter, özel karakterler yasak)", "Недопустимое имя! (2-15 символов)");
        dict.put("Kullanıcı adında uygunsuz kelimeler var!", "Имя содержит недопустимые слова!");
        dict.put("CLASSIC", "КЛАССИКА");
        dict.put("MIXED MODE", "СМЕШАННЫЙ РЕЖИМ");
        dict.put("SETTINGS", "НАСТРОЙКИ");
        dict.put("GLOBAL LEADERBOARD", "ГЛОБАЛЬНАЯ ТАБЛИЦА");
        dict.put("EXIT", "ВЫХОД");
        dict.put("OTHER GAMES", "ДРУГИЕ ИГРЫ");
        dict.put("Blitz Math Challenge", "Blitz Math Challenge");
        dict.put("▶  CLASSIC MODE", "▶  КЛАССИКА");
        dict.put("🎰  MIXED MODE", "🎰  СМЕШАННЫЙ РЕЖИМ");
        dict.put("🔧  SETTINGS", "🔧  НАСТРОЙКИ");
        dict.put("⭐  HIGH SCORES", "⭐  РЕКОРДЫ");
        dict.put("🚪  EXIT", "🚪  ВЫХОД");
        dict.put("SELECT LANGUAGE", "ВЫБЕРИТЕ ЯЗЫК");
        dict.put("❌ 1 QUESTION BACK!", "❌ 1 ВОПРОС НАЗАД!");
        dict.put("⏰ TIME'S UP!", "⏰ ВРЕМЯ ВЫШЛО!");
        dict.put("Enter Your Name", "Введите ваше имя");
        dict.put("Please enter your name!", "Пожалуйста, введите ваше имя!");
        dict.put("Russian", "Русский");
        dict.put("📖 CLASSIC", "📖 КЛАССИКА");
        dict.put("Sequential Progress", "Последовательный прогресс");
        dict.put("Addition → Subtraction → Multiplication → Division → Mixed\\n200 Levels • 10 questions each", "Сложение → Вычитание → Умножение → Деление → Смешанный\\n200 Уровней • по 10 вопросов");
        dict.put("Random Operations", "Случайные операции");
        dict.put("Different operations each level\\n200 Levels • Changing difficulty", "Разные операции\\n200 Уровней • Меняющаяся сложность");
        dict.put("LEVEL", "УРОВЕНЬ");
        dict.put("SCORE", "СЧЕТ");
        dict.put("STREAK", "СЕРИЯ");
        dict.put("TIME", "ВРЕМЯ");
        dict.put("Question", "Вопрос");
        dict.put("/", "/");
        dict.put("Add!", "Сложи!");
        dict.put("Subtract!", "Вычти!");
        dict.put("Multiply!", "Умножь!");
        dict.put("Divide!", "Раздели!");
        dict.put("LEVEL COMPLETE!", "УРОВЕНЬ ПРОЙДЕН!");
        dict.put("Congratulations!", "Поздравляем!");
        dict.put("PERFECT!", "ИДЕАЛЬНО!");
        dict.put("Great Job!", "Отличная работа!");
        dict.put("Good Job!", "Хорошая работа!");
        dict.put("Keep Trying!", "Продолжай!");
        dict.put("NEXT LEVEL", "СЛЕДУЮЩИЙ УРОВЕНЬ");
        dict.put("RETRY", "ПОВТОРИТЬ");
        dict.put("MAIN MENU", "ГЛАВНОЕ МЕНЮ");
        dict.put("Correct Answers", "Правильные ответы");
        dict.put("Wrong Answers", "Неправильные ответы");
        dict.put("Accuracy", "Точность");
        dict.put("Bonus Points", "Бонусные очки");
        dict.put("GAME OVER", "ИГРА ОКОНЧЕНА");
        dict.put("WRONG ANSWER!", "НЕПРАВИЛЬНО!");
        dict.put("Final Score", "Итоговый счет");
        dict.put("Best Streak", "Лучшая серия");
        dict.put("NEW RECORD!", "НОВЫЙ РЕКОРД!");
        dict.put("Sound", "Звук");
        dict.put("Music", "Музыка");
        dict.put("Auto Theme", "Авто тема");
        dict.put("Changes based on time of day", "Меняется в зависимости от времени суток");
        dict.put("Theme", "Тема");
        dict.put("Swipe →", "Смахни →");
        dict.put("Language", "Язык");
        dict.put("Vibration", "Вибрация");
        dict.put("Difficulty", "Сложность");
        dict.put("Easy", "Легко");
        dict.put("Medium", "Средне");
        dict.put("Hard", "Сложно");
        dict.put("Time Limit", "Лимит времени");
        dict.put("seconds", "секунд");
        dict.put("No scores yet", "Пока нет рекордов");
        dict.put("Rank", "Место");
        dict.put("Date", "Дата");
        dict.put("Clear All", "Очистить все");
        dict.put("Are you sure you want to clear all scores?", "Вы уверены, что хотите удалить все рекорды?");
        dict.put("Yes", "Да");
        dict.put("No", "Нет");
        dict.put("OK", "ОК");
        dict.put("Cancel", "Отмена");
        dict.put("Continue", "Продолжить");
        dict.put("PAUSE", "ПАУЗА");
        dict.put("RESUME", "ПРОДОЛЖИТЬ");
        dict.put("QUIT", "ВЫЙТИ");
        dict.put("Addition", "Сложение");
        dict.put("Subtraction", "Вычитание");
        dict.put("Multiplication", "Умножение");
        dict.put("Division", "Деление");
        dict.put("Mixed", "Смешанный");
        dict.put("How to Play?", "Как играть?");
        dict.put("Solve the question quickly!", "Решай вопросы быстро!");
        dict.put("Answer within 3 seconds", "Отвечай за 3 секунды");
        dict.put("Correct answer = +10 points", "Правильный ответ = +10 очков");
        dict.put("Build streaks for bonus!", "Делай серии для бонуса!");
        dict.put("Got it!", "Понятно!");
        dict.put("Achievements", "Достижения");
        dict.put("First Victory", "Первая победа");
        dict.put("Speed Demon", "Демон скорости");
        dict.put("Math Master", "Мастер математики");
        dict.put("Streak King", "Король серий");
        dict.put("Perfect", "Идеально");
        dict.put("Error", "Ошибка");
        dict.put("Please enter an answer", "Пожалуйста, введите ответ");
        dict.put("Invalid input", "Неверный ввод");
        dict.put("%", "%");
        dict.put("points", "очков");
        dict.put("COMBO", "КОМБО");
        dict.put("Reached", "Достигнуто");
        dict.put("x", "x");
        dict.put("🏆 GLOBAL LEADERBOARD", "🏆 ГЛОБАЛЬНАЯ ТАБЛИЦА");
        dict.put("Your Rank", "Ваше место");
        dict.put("Loading...", "Загрузка...");
        dict.put("Connection error", "Ошибка подключения");
        dict.put("Sign in with Google", "Войти через Google");
        dict.put("Sign in with Facebook", "Войти через Facebook");
        dict.put("Play as Guest", "Играть как гость");
        dict.put("OR", "ИЛИ");

        String russianSlogans = "    private val russianSlogans = listOf(\n" +
            "        \"Думай быстро, бей точно! 🧠⚡\",\n" +
            "        \"10 минут в день укрепляют твой ум! 💪\",\n" +
            "        \"Первый шаг к математическому гению! 🎯\",\n" +
            "        \"Подними свой мозг на вершину! 🚀\",\n" +
            "        \"Каждый вопрос — это победа! 🏆\",\n" +
            "        \"Пусть начнется тренировка мозга! 🧘\",\n" +
            "        \"Стань мастером вычислений! \uD83C\uDF93\",\n" +
            "        \"Танцуй с цифрами! 💃\",\n" +
            "        \"Испытай границы своего разума! 🔥\",\n" +
            "        \"Скорость и точность в одном флаконе! ⚡\"\n" +
            "    )";

        String englishVal = null;
        for (String line : lines) {
            if (line.contains("private val chineseSlogans")) {
                outLines.add(russianSlogans);
                outLines.add("");
                outLines.add(line);
                continue;
            }

            if (line.contains("AppLanguage.CHINESE -> chineseSlogans.random()")) {
                outLines.add(line);
                outLines.add("            AppLanguage.RUSSIAN -> russianSlogans.random()");
                continue;
            }

            // Capture English string to search in dict
            if (line.contains("AppLanguage.ENGLISH")) {
                int start = line.indexOf("\"");
                int end = line.lastIndexOf("\"");
                if (start != -1 && end != -1 && start != end) {
                    englishVal = line.substring(start + 1, end).trim();
                } else if (line.contains("AppLanguage.ENGLISH -> englishSlogans.random()")) {
                    englishVal = null;
                }
            }

            // Also check for TURKISH since sometimes they only fallback
            if (line.contains("AppLanguage.TURKISH -> \"Geçersiz kullanıcı adı")) {
               englishVal = "Geçersiz kullanıcı adı! (2-15 karakter, özel karakterler yasak)";
            }
            if (line.contains("AppLanguage.TURKISH -> \"Kullanıcı adında uygunsuz")) {
               englishVal = "Kullanıcı adında uygunsuz kelimeler var!";
            }

            if (line.contains("AppLanguage.CHINESE -> \"")) {
                outLines.add(line);

                String tr = dict.get(englishVal);
                if (tr == null) {
                    System.out.println("MISSING: " + englishVal);
                    // Just duplicate chinese if missing
                    int start = line.indexOf("\"");
                    tr = line.substring(start + 1, line.lastIndexOf("\""));
                }
                
                String newLine = line.replace("AppLanguage.CHINESE", "AppLanguage.RUSSIAN");
                newLine = newLine.replaceAll("\".*\"", "\"" + tr.replace("\\", "\\\\") + "\"");
                // fix escape sequences like \n
                newLine = newLine.replace("\\\\n", "\\n");

                outLines.add(newLine);
                englishVal = null;
                continue;
            }

            // Check if it's the `russian` property which should exist similar to others
            // Let's add `val russian` property! We'll just define it if we see `val chinese`
            if (line.contains("val chinese: String")) {
                outLines.add("    val russian: String\n        get() = when (currentLanguage) {\n            AppLanguage.TURKISH -> \"Rusça\"\n            AppLanguage.ENGLISH -> \"Russian\"\n            else -> \"Русский\"\n        }\n");
                outLines.add(line);
                continue;
            }

            outLines.add(line);
        }

        Files.write(Paths.get("Strings.kt.new"), outLines, StandardCharsets.UTF_8);
        Files.move(Paths.get("Strings.kt.new"), path, StandardCopyOption.REPLACE_EXISTING);
        System.out.println("Done!");
    }
}
