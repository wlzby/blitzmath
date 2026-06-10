$ErrorActionPreference = 'Stop'

[System.Console]::OutputEncoding = [System.Text.Encoding]::UTF8

$filePath = "c:\Users\ozbayv\AndroidStudioProjects\blitzmath\app\src\main\java\com\mawelly\blitzmath\localization\Strings.kt"
$tempPath = "$filePath.tmp"

$dict = @{
    "无效的用户名！（2-15个字符）" = "Недопустимое имя! (2-15 символов)"
    "用户名包含不当词汇！" = "Имя содержит недопустимые слова!"
    "经典" = "КЛАССИКА"
    "混合模式" = "СМЕШАННЫЙ РЕЖИМ"
    "设置" = "НАСТРОЙКИ"
    "全球排行榜" = "ГЛОБАЛЬНАЯ ТАБЛИЦА"
    "退出" = "ВЫХОД"
    "其他游戏" = "ДРУГИЕ ИГРЫ"
    "Blitz Math Challenge" = "Blitz Math Challenge"
    "▶  经典模式" = "▶  КЛАССИКА"
    "🎰  混合模式" = "🎰  СМЕШАННЫЙ РЕЖИМ"
    "🔧  设置" = "🔧  НАСТРОЙКИ"
    "⭐  高分" = "⭐  РЕКОРДЫ"
    "🚪  退出" = "🚪  ВЫХОД"
    "选择语言" = "ВЫБЕРИТЕ ЯЗЫК"
    "❌ 后退 1 题！" = "❌ 1 ВОПРОС НАЗАД!"
    "⏰ 时间到！" = "⏰ ВРЕМЯ ВЫШЛО!"
    "输入你的名字" = "Введите ваше имя"
    "请输入你的名字！" = "Пожалуйста, введите ваше имя!"
    "简体中文" = "Русский"
    "📖 经典" = "📖 КЛАССИКА"
    "顺序进度" = "Последовательный прогресс"
    "加法 → 减法 → 乘法 → 除法 → 混合\n200 个关卡 • 每关 10 题" = "Сложение → Вычитание → Умножение → Деление → Смешанный\n200 Уровней • по 10 вопросов"
    "随机运算" = "Случайные операции"
    "每关不同的运算\n200 个关卡 • 不断变化的难度" = "Разные операции\n200 Уровней • Меняющаяся сложность"
    "等级" = "УРОВЕНЬ"
    "分数" = "СЧЕТ"
    "连击" = "СЕРИЯ"
    "时间" = "ВРЕМЯ"
    "问题" = "Вопрос"
    "/" = "/"
    "加！" = "Сложи!"
    "减！" = "Вычти!"
    "乘！" = "Умножь!"
    "除！" = "Раздели!"
    "等级完成！" = "УРОВЕНЬ ПРОЙДЕН!"
    "恭喜！" = "Поздравляем!"
    "完美！" = "ИДЕАЛЬНО!"
    "做得好！" = "Отличная работа!"
    "继续努力！" = "Продолжай!"
    "下一关" = "СЛЕДУЮЩИЙ УРОВЕНЬ"
    "重试" = "ПОВТОРИТЬ"
    "主菜单" = "ГЛАВНОЕ МЕНЮ"
    "正确答案" = "Правильные ответы"
    "错误答案" = "Неправильные ответы"
    "准确率" = "Точность"
    "奖励分" = "Бонусные очки"
    "游戏结束" = "ИГРА ОКОНЧЕНА"
    "答错了！" = "НЕПРАВИЛЬНО!"
    "最终分数" = "Итоговый счет"
    "最高连击" = "Лучшая серия"
    "新纪录！" = "НОВЫЙ РЕКОРД!"
    "音效" = "Звук"
    "音乐" = "Музыка"
    "自动主题" = "Авто тема"
    "根据一天中的时间变化" = "Меняется в зависимости от времени суток"
    "主题" = "Тема"
    "滑动 →" = "Смахни →"
    "语言" = "Язык"
    "震动" = "Вибрация"
    "难度" = "Сложность"
    "简单" = "Легко"
    "中等" = "Средне"
    "困难" = "Сложно"
    "时间限制" = "Лимит времени"
    "秒" = "секунд"
    "暂无分数" = "Пока нет рекордов"
    "排名" = "Место"
    "日期" = "Дата"
    "全部清除" = "Очистить все"
    "你确定要清除所有分数吗？" = "Вы уверены, что хотите удалить все рекорды?"
    "是" = "Да"
    "否" = "Нет"
    "确定" = "ОК"
    "取消" = "Отмена"
    "继续" = "Продолжить"
    "暂停" = "ПАУЗА"
    "恢复" = "ПРОДОЛЖИТЬ"
    "加法" = "Сложение"
    "减法" = "Вычитание"
    "乘法" = "Умножение"
    "除法" = "Деление"
    "混合" = "Смешанный"
    "如何玩？" = "Как играть?"
    "快速求解！" = "Решай вопросы быстро!"
    "3 秒内回答" = "Отвечай за 3 секунды"
    "正确答案 = +10 分" = "Правильный ответ = +10 очков"
    "连击获得奖励！" = "Делай серии для бонуса!"
    "明白了！" = "Понятно!"
    "成就" = "Достижения"
    "首场胜利" = "Первая победа"
    "速度狂人" = "Демон скорости"
    "数学大师" = "Мастер математики"
    "连击之王" = "Король серий"
    "完美" = "Идеально"
    "错误" = "Ошибка"
    "请输入答案" = "Пожалуйста, введите ответ"
    "输入无效" = "Неверный ввод"
    "%" = "%"
    "分" = "очков"
    "组合" = "КОМБО"
    "达到" = "Достигнуто"
    "x" = "x"
    "🏆 全球排行榜" = "🏆 ГЛОБАЛЬНАЯ ТАБЛИЦА"
    "你的排名" = "Ваше место"
    "加载中..." = "Загрузка..."
    "连接错误" = "Ошибка подключения"
    "使用 Google 登录" = "Войти через Google"
    "使用 Facebook 登录" = "Войти через Facebook"
    "以游客身份游戏" = "Играть как гость"
    "或" = "ИЛИ"
}

$lines = Get-Content $filePath -Encoding UTF8
$outLines = @()

$russianSlogans = @"
    private val russianSlogans = listOf(
        "Думай быстро, бей точно! 🧠⚡",
        "10 минут в день укрепляют твой ум! 💪",
        "Первый шаг к математическому гению! 🎯",
        "Подними свой мозг на вершину! 🚀",
        "Каждый вопрос — это победа! 🏆",
        "Пусть начнется тренировка мозга! 🧘",
        "Стань мастером вычислений! 🎓",
        "Танцуй с цифрами! 💃",
        "Испытай границы своего разума! 🔥",
        "Скорость и точность в одном флаконе! ⚡"
    )
"@

foreach ($line in $lines) {
    if ($line -match "private val chineseSlogans") {
        # Insert russian slogans before chinese or after
        $outLines += $russianSlogans
        $outLines += $line
        continue
    }

    if ($line -match "AppLanguage.CHINESE -> chineseSlogans.random\(\)") {
        $outLines += $line
        $outLines += '            AppLanguage.RUSSIAN -> russianSlogans.random()'
        continue
    }

    if ($line -match 'AppLanguage\.CHINESE\s*->\s*"(.*?)"') {
        $outLines += $line
        $chineseText = $matches[1]

        # Handle unescaped \n
        $chineseText = $chineseText -replace '\\n', "`n"
        
        # Exact match or some fallback
        $russianText = $dict[$chineseText]
        if (-not $russianText) {
            # fallback: just duplicate Chinese if missing
            $russianText = $chineseText
            Write-Host "Missing translation for: $chineseText"
        }

        $russianText = $russianText -replace "`n", '\n'

        $newLine = $line -replace 'AppLanguage\.CHINESE', 'AppLanguage.RUSSIAN'
        $newLine = $newLine -replace '"(.*?)"', "`"$russianText`""
        
        $outLines += $newLine
        continue
    }

    if ($line -match 'else -> ".*"') {
        # Might need to add AppLanguage.RUSSIAN here if there's no CHINESE block?
        # Let's hope all strings have CHINESE blocks. If they rely on else, we'll see.
        $outLines += $line
        continue
    }

    $outLines += $line
}

[IO.File]::WriteAllLines($tempPath, $outLines, [System.Text.Encoding]::UTF8)
Move-Item -Path $tempPath -Destination $filePath -Force

Write-Host "Replaced Strings.kt!"
