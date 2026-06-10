$translationsJson = Get-Content -Path "c:\Users\ozbayv\AndroidStudioProjects\blitzmath\translations_all.json" -Raw | ConvertFrom-Json

$properties = @(
    @{ name="getBannedWordError"; english="Username contains inappropriate words!" },
    @{ name="music"; english="Music" },
    @{ name="sound"; english="Sound Effects" },
    @{ name="voiceFeedback"; english="Voice Feedback" },
    @{ name="voiceFeedbackDesc"; english="Voices questions and results" },
    @{ name="vibration"; english="Vibration" },
    @{ name="vibrationDesc"; english="Get tactile feedback on mistakes." },
    @{ name="vibrationStrength"; english="Vibration Intensity" },
    @{ name="vibrationTest"; english="TEST VIBRATION" },
    @{ name="autoTheme"; english="Auto Theme" },
    @{ name="autoThemeDesc"; english="Changes automatically based on time" },
    @{ name="theme"; english="Theme" },
    @{ name="swipeHint"; english="Swipe to select" },
    @{ name="languageLabel"; english="Language" },
    @{ name="supportTitle"; english="Contact us, we would be happy to help you." },
    @{ name="whatsappMessage"; english="Chat on WhatsApp" },
    @{ name="emailMessage"; english="Send Email" },
    @{ name="challenge"; english="CHALLENGE" },
    @{ name="otherGames"; english="Our Other Games" },
    @{ name="checkpointLabel"; english="Checkpoint" },
    @{ name="statCheck"; english="CHECK" },
    @{ name="statScore"; english="SCORE" },
    @{ name="statStreak"; english="STREAK" },
    @{ name="cardUnlocked"; english="NEW CARD UNLOCKED!" },
    @{ name="bonus"; english="Bonus" },
    @{ name="checkpointComplete"; english="CHECKPOINT COMPLETE!" },
    @{ name="collection"; english="COLLECTION" },
    @{ name="question"; english="Question" },
    @{ name="level"; english="Level" },
    @{ name="score"; english="NORMAL_SCORE" }, # Special case in JSON
    @{ name="globalLeaderboard"; english="GLOBAL LEADERBOARD" },
    @{ name="yourRank"; english="Your Rank" },
    @{ name="congratulations"; english="Congratulations!" },
    @{ name="nextLevel"; english="NEXT LEVEL" },
    @{ name="retry"; english="RETRY" },
    @{ name="saveMeTitle"; english="SECOND CHANCE" },
    @{ name="watchAdToSave"; english="Watch Ad to Refill Lives" },
    @{ name="noThanks"; english="No, Thanks" },
    @{ name="getNameRequired"; english="You must enter a name to continue!" },
    @{ name="time"; english="TIME" },
    @{ name="gameOver"; english="GAME OVER" },
    @{ name="finalScore"; english="FINAL SCORE" },
    @{ name="newRecord"; english="NEW RECORD!" },
    @{ name="backToMenu"; english="MENU" },
    @{ name="watchAdContinue"; english="Watch Ad & Continue" },
    @{ name="shareScore"; english="Share Score" },
    @{ name="menuLeaderboard"; english="LEADERBOARD" },
    @{ name="dailyReward"; english="DAILY REWARD" },
    @{ name="dailyBonusDesc"; english="Come back every day to collect more stars!" },
    @{ name="stars"; english="STARS" },
    @{ name="outOfLivesTitle"; english="OUT OF LIVES!" },
    @{ name="outOfLivesMessage"; english="Wait to refill or watch an ad to refill all lives instantly!" },
    @{ name="outOfLivesRefillAd"; english="Watch Ad & Get 5 Lives" },
    @{ name="claim"; english="CLAIM" },
    @{ name="streak"; english="STREAK" },
    @{ name="playAsGuest"; english="PLAY AS GUEST" },
    @{ name="or"; english="OR" },
    @{ name="enterName"; english="Enter Name" },
    @{ name="highScores"; english="HIGH SCORES" },
    @{ name="noScoresYet"; english="No scores found yet!" },
    @{ name="date"; english="Date" },
    @{ name="clearAll"; english="CLEAR ALL" },
    @{ name="hintAdd"; english="Hint: Split numbers into tens in your mind." },
    @{ name="hintSubtract"; english="Hint: Subtract tens first, then ones." },
    @{ name="hintMultiply"; english="Hint: Remember the multiplication table!" },
    @{ name="hintDivide"; english="Hint: Think of it as the reverse of multiplication." },
    @{ name="addition"; english="Addition" },
    @{ name="subtraction"; english="Subtraction" },
    @{ name="multiplication"; english="Multiplication" },
    @{ name="division"; english="Division" },
    @{ name="mixed"; english="Mixed" },
    @{ name="menuClassic"; english="CLASSIC MODE" },
    @{ name="menuMixed"; english="MIXED MODE" },
    @{ name="menuChallenge"; english="CHALLENGE MODE" },
    @{ name="ok"; english="OK" },
    @{ name="menuSettings"; english="SETTINGS" },
    @{ name="usageRights"; english="Usage Rights" },
    @{ name="updateTitle"; english="UPDATE REQUIRED" },
    @{ name="updateMessage"; english="Math Lab Updated! ⚡ Scientists are now more powerful and our energy system is fully revamped. Download the latest version to keep competing at the highest level!" },
    @{ name="updateButton"; english="UPDATE NOW" },
    @{ name="exitDialogTitle"; english="Exit" },
    @{ name="exitDialogMessage"; english="Would you like to try our other games before leaving?" },
    @{ name="exitConfirm"; english="Yes, Check" },
    @{ name="exitDismiss"; english="No, Exit" },
    @{ name="storeTitle"; english="STORE" },
    @{ name="equip"; english="EQUIP" },
    @{ name="remove"; english="REMOVE" },
    @{ name="points"; english="Points" },
    @{ name="equippedAbilities"; english="Equipped Abilities" },
    @{ name="noAbilitiesEquipped"; english="No abilities equipped yet." },
    @{ name="reviewInvitationTitle"; english="Enjoying Blitz Math?" },
    @{ name="reviewInvitationMessage"; english="Would you like to leave a quick review to help us grow? 1000 Stars will be our gift to you!" },
    @{ name="welcomeGiftTitle"; english="Welcome Gift!" },
    @{ name="welcomeGiftMessage"; english="Welcome to the BlitzMath family! In honor of your first game, we are gifting you the Pythagoras card. You can equip it from the Collection screen!" },
    @{ name="rateNow"; english="Rate Now" },
    @{ name="privacyPolicy"; english="Privacy Policy" },
    @{ name="totalTimeBonus"; english="Extra Time Earned" },
    @{ name="totalPlayTime"; english="Total Play Time" },
    @{ name="watchAdToPlayAgain"; english="Watch Ad to Play Again" },
    @{ name="challengeAlreadyPlayed"; english="You've used all challenge attempts for today!" }
)

$sb = New-Object System.Text.StringBuilder
$sb.AppendLine("package com.mawelly.blitzmath.localization")
$sb.AppendLine()
$sb.AppendLine("import androidx.compose.runtime.getValue")
$sb.AppendLine("import androidx.compose.runtime.mutableStateOf")
$sb.AppendLine("import androidx.compose.runtime.setValue")
$sb.AppendLine("import com.mawelly.blitzmath.localization.AppLanguage")
$sb.AppendLine("import kotlinx.coroutines.flow.MutableStateFlow")
$sb.AppendLine("import kotlinx.coroutines.flow.StateFlow")
$sb.AppendLine("import kotlinx.coroutines.flow.asStateFlow")
$sb.AppendLine("import kotlin.random.Random")
$sb.AppendLine()
$sb.AppendLine("object Strings {")
$sb.AppendLine()
$sb.AppendLine("    // StateFlow ile dil değişimini dinle")
$sb.AppendLine("    private val _currentLanguage = MutableStateFlow(AppLanguage.TURKISH)")
$sb.AppendLine("    val currentLanguageFlow: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()")
$sb.AppendLine()
$sb.AppendLine("    // Compose için observable state")
$sb.AppendLine("    var currentLanguage: AppLanguage by mutableStateOf(AppLanguage.TURKISH)")
$sb.AppendLine("        private set")
$sb.AppendLine()
$sb.AppendLine("    fun setLanguage(lang: AppLanguage) {")
$sb.AppendLine("        currentLanguage = lang")
$sb.AppendLine("        _currentLanguage.value = lang")
$sb.AppendLine("    }")
$sb.AppendLine()
$sb.AppendLine("    fun getLanguage(): AppLanguage = currentLanguage")
$sb.AppendLine()
$sb.AppendLine("    val bannedWords = setOf(")
$sb.AppendLine("        \"amk\", \"aq\", \"amq\", \"mk\", \"mq\", \"sik\", \"siktir\", \"sikerim\", \"sikik\", \"siktim\", \"sikem\",")
$sb.AppendLine("        \"sikti\", \"sikiyor\", \"sikmiş\", \"siksin\", \"siktigim\", \"siktimin\", \"sikikler\", \"sikemiyor\",")
$sb.AppendLine("        \"orospu\", \"orospunun\", \"orospuçu\", \"orospular\", \"orospuluk\", \"orospudan\",")
$sb.AppendLine("        \"piç\", \"pic\", \"piçler\", \"picler\", \"piçlik\", \"piclik\",")
$sb.AppendLine("        \"yarrak\", \"yarak\", \"yrrk\", \"yrk\", \"yarragim\", \"yaraklar\",")
$sb.AppendLine("        \"ananı\", \"anani\", \"anan\", \"avradını\", \"avradini\", \"avrat\", \"karı\", \"kari\",")
$sb.AppendLine("        \"göt\", \"götün\", \"götünü\", \"götler\", \"götveren\",")
$sb.AppendLine("        \"am\", \"ami\", \"amini\", \"amcik\", \"amcık\", \"amciklar\",")
$sb.AppendLine("        \"pezevenk\", \"pezevengin\", \"pezevenkler\",")
$sb.AppendLine("        \"kahpe\", \"kahpeşi\", \"kahpelik\",")
$sb.AppendLine("        \"dalyarak\", \"dalyarrak\", \"dal\", \"yarrami\",")
$sb.AppendLine("        \"sulaleni\", \"sulalenin\", \"sülaleni\", \"sülalenin\",")
$sb.AppendLine("        \"orospuçoc\", \"orospuçoçu\", \"orospuçocu\", \"orospuçocuğu\",")
$sb.AppendLine("        \"gavat\", \"gavatlar\", \"gavatlik\",")
$sb.AppendLine("        \"kevaşe\", \"kevaşe\", \"kevaşeler\",")
$sb.AppendLine("        \"fahişe\", \"fahişe\", \"fahişeler\",")
$sb.AppendLine("        \"gerzek\", \"gerzekler\", \"mal\", \"mallar\", \"salak\", \"salaklar\",")
$sb.AppendLine("        \"aptal\", \"aptallar\", \"gerizekalı\", \"gerizekali\", \"gerzek\"")
$sb.AppendLine("    )")
$sb.AppendLine()

foreach ($prop in $properties) {
    $name = $prop.name
    $eng = $prop.english
    
    # Try to find translations in JSON
    $trans = $translationsJson."$eng"
    
    $sb.AppendLine("    val $name: String")
    $sb.AppendLine("        get() = when (currentLanguage) {")
    $sb.AppendLine("            AppLanguage.ENGLISH -> `"$eng`"")
    
    # Special handling for NORMAL_SCORE and STAT_SCORE
    if ($eng -eq "NORMAL_SCORE") { $eng = "Score" }
    if ($eng -eq "STAT_SCORE") { $eng = "SCORE" }

    if ($null -ne $trans) {
        if ($null -ne $trans.TURKISH) { $sb.AppendLine("            AppLanguage.TURKISH -> `"$($trans.TURKISH)`"") }
        if ($null -ne $trans.SPANISH) { $sb.AppendLine("            AppLanguage.SPANISH -> `"$($trans.SPANISH)`"") }
        if ($null -ne $trans.GERMAN) { $sb.AppendLine("            AppLanguage.GERMAN -> `"$($trans.GERMAN)`"") }
        if ($null -ne $trans.FRENCH) { $sb.AppendLine("            AppLanguage.FRENCH -> `"$($trans.FRENCH)`"") }
        if ($null -ne $trans.ITALIAN) { $sb.AppendLine("            AppLanguage.ITALIAN -> `"$($trans.ITALIAN)`"") }
        if ($null -ne $trans.PORTUGUESE) { $sb.AppendLine("            AppLanguage.PORTUGUESE -> `"$($trans.PORTUGUESE)`"") }
        if ($null -ne $trans.HINDI) { $sb.AppendLine("            AppLanguage.HINDI -> `"$($trans.HINDI)`"") }
        if ($null -ne $trans.CHINESE) { $sb.AppendLine("            AppLanguage.CHINESE -> `"$($trans.CHINESE)`"") }
        if ($null -ne $trans.RUSSIAN) { $sb.AppendLine("            AppLanguage.RUSSIAN -> `"$($trans.RUSSIAN)`"") }
    } else {
        # Fallback to English
        $sb.AppendLine("            else -> `"$eng`"")
    }
    
    # Close when
    $sb.AppendLine("        }")
    $sb.AppendLine()
}

# Add special functions
$sb.AppendLine("    fun getSelectLanguage(lang: AppLanguage? = null): String {")
$sb.AppendLine("        return when (lang ?: currentLanguage) {")
$sb.AppendLine("            AppLanguage.TURKISH -> \"Dil Seçimi\"")
$sb.AppendLine("            AppLanguage.ENGLISH -> \"Select Language\"")
$sb.AppendLine("            AppLanguage.SPANISH -> \"Seleccionar Idioma\"")
$sb.AppendLine("            AppLanguage.GERMAN -> \"Sprache wählen\"")
$sb.AppendLine("            AppLanguage.FRENCH -> \"Choisir la langue\"")
$sb.AppendLine("            AppLanguage.ITALIAN -> \"Seleziona lingua\"")
$sb.AppendLine("            AppLanguage.PORTUGUESE -> \"Selecionar Idioma\"")
$sb.AppendLine("            AppLanguage.HINDI -> \"भाषा चुनें\"")
$sb.AppendLine("            AppLanguage.CHINESE -> \"选择语言\"")
$sb.AppendLine("            AppLanguage.RUSSIAN -> \"Выбрать язык\"")
$sb.AppendLine("        }")
$sb.AppendLine("    }")
$sb.AppendLine()
$sb.AppendLine("    fun getShareMessage(score: Int, cp: Int): String {")
$sb.AppendLine("        val template = when (currentLanguage) {")
$sb.AppendLine("            AppLanguage.TURKISH -> \"BlitzMath'te `$score puan topladım! Checkpoint: `$cp. Zekanı göstermeye hazır mısın? Hemen indir ve bana meydan oku! ⚡\n\nPlay Store: `$link\"")
$sb.AppendLine("            else -> \"I scored `$score points on BlitzMath! Checkpoint: `$cp. Are you ready to show your intelligence? Download now and challenge me! ⚡\n\nPlay Store: `$link\"")
$sb.AppendLine("        }")
$sb.AppendLine("        return template.replace(\"`$score\", score.toString()).replace(\"`$cp\", cp.toString()).replace(\"`$link\", \"https://play.google.com/store/apps/details?id=com.mawelly.blitzmath\")")
$sb.AppendLine("    }")
$sb.AppendLine()

# Slogans
$sb.AppendLine("    private val turkishSlogans = listOf(")
$sb.AppendLine("        \"Hızlı düşün, doğru vur! 🚀\", \"Günde 10 dakika, zihninizi güçlendirir! 🧠\", \"Matematik dehası olmak için ilk adım! 🎓\",")
$sb.AppendLine("        \"Zekanı zirveye taşı! 🏔️\", \"Her soru bir zafer! 🏆\", \"Beyin egzersizi başlasın! ⚡\", \"Hesaplamada usta ol! 🔢\",")
$sb.AppendLine("        \"Zihnin sınırlarını zorla! 🔥\", \"Beynin en iyi sporu! 🏅\", \"Rakamların ustası ol! 👑\", \"Düşün, hesapla, zafer kazan! 🎯\",")
$sb.AppendLine("        \"Zeka maratonu başlıyor! 🏁\", \"Sayılarla seni bekliyor! 🌟\", \"Matematikle büyü yap! ✨\", \"Hızlı zihin, keskin sonuç! 🗡️\",")
$sb.AppendLine("        \"Beynin potansiyelini açığa çıkar! 🔓\"")
$sb.AppendLine("    )")
$sb.AppendLine()
$sb.AppendLine("    private val englishSlogans = listOf(")
$sb.AppendLine("        \"Think fast, strike right! 🚀\", \"10 minutes a day strengthens your mind! 🧠\", \"First step to becoming a math genius! 🎓\",")
$sb.AppendLine("        \"Elevate your brain to the top! 🏔️\", \"Every question is a victory! 🏆\", \"Let the brain workout begin! ⚡\",")
$sb.AppendLine("        \"Master the calculation! 🔢\", \"Dance with numbers! 💃\", \"Push your mind's limits! 🔥\", \"Speed and accuracy combined! ⏱️\",")
$sb.AppendLine("        \"Break new ground in math! 🚀\", \"Your brain's best sport! 🏅\", \"Become the master of digits! 👑\", \"Think, calculate, conquer! 🎯\",")
$sb.AppendLine("        \"The brain marathon begins! 🏁\", \"Numbers are waiting for you! 🌟\", \"Work magic with math! ✨\", \"Fast mind, sharp results! 🗡️\"")
$sb.AppendLine("    )")
$sb.AppendLine()
$sb.AppendLine("    val slogan: String")
$sb.AppendLine("        get() = when (currentLanguage) {")
$sb.AppendLine("            AppLanguage.TURKISH -> turkishSlogans.random()")
$sb.AppendLine("            else -> englishSlogans.random()")
$sb.AppendLine("        }")
$sb.AppendLine()
$sb.AppendLine("    fun getAdsCountMessage(count: Int): String {")
$sb.AppendLine("        return when (currentLanguage) {")
$sb.AppendLine("            AppLanguage.TURKISH -> \"$count Reklam İzle\"")
$sb.AppendLine("            else -> \"Watch $count Ads\"")
$sb.AppendLine("        }")
$sb.AppendLine("    }")
$sb.AppendLine()
$sb.AppendLine("}") # Close Strings object

$finalContent = $sb.ToString()
[System.IO.File]::WriteAllText($path, $finalContent, (New-Object System.Text.UTF8Encoding $false))
Write-Output "Strings.kt rebuilt from scratch!"
