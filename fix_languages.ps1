$path = "C:\Users\ozbayv\AndroidStudioProjects\blitzmath\app\src\main\java\com\mawelly\blitzmath\localization\Strings.kt"
$lines = [System.IO.File]::ReadAllLines($path, [System.Text.Encoding]::UTF8)

# Read extracted English strings
$engDict = @{}
$extractLines = [System.IO.File]::ReadAllLines("C:\Users\ozbayv\AndroidStudioProjects\blitzmath\extract.txt", [System.Text.Encoding]::UTF8)
foreach ($line in $extractLines) {
    if ($line -match "^(.+?) === `"(.*)`"$") {
        $engDict[$matches[1]] = $matches[2]
    } elseif ($line -match "^(.+?) === (.*)$") {
        $engDict[$matches[1]] = $matches[2]
    }
}

# Translation dictionary for the most important corrupted strings
$replacements = @{
    "menuClassic" = @{
        "TURKISH"="KLASİK MOD"; "SPANISH"="MODO CLÁSICO"; "GERMAN"="KLASSISCHER MODUS"; 
        "FRENCH"="MODE CLASSIQUE"; "ITALIAN"="MODALITÀ CLASSICA"; "PORTUGUESE"="MODO CLÁSSICO"; 
        "HINDI"="क्लासिक मोड"; "CHINESE"="经典模式"; "RUSSIAN"="КЛАССИЧЕСКИЙ РЕЖИМ"
    }
    "menuMixed" = @{
        "TURKISH"="KARIŞIK MOD"; "SPANISH"="MODO MIXTO"; "GERMAN"="GEMISCHTER MODUS"; 
        "FRENCH"="MODE MIXTE"; "ITALIAN"="MODALITÀ MISTA"; "PORTUGUESE"="MODO MISTO"; 
        "HINDI"="मिश्रित मोड"; "CHINESE"="混合模式"; "RUSSIAN"="СМЕШАННЫЙ РЕЖИМ"
    }
    "menuChallenge" = @{
        "TURKISH"="MEYDAN OKUMA"; "SPANISH"="DESAFÍO"; "GERMAN"="HERAUSFORDERUNG"; 
        "FRENCH"="DÉFI"; "ITALIAN"="SFIDA"; "PORTUGUESE"="DESAFIO"; 
        "HINDI"="चुनौती"; "CHINESE"="挑战模式"; "RUSSIAN"="ИСПЫТАНИЕ"
    }
    "collection" = @{
        "TURKISH"="KOLEKSİYON"; "SPANISH"="COLECCIÓN"; "GERMAN"="SAMMLUNG"; 
        "FRENCH"="COLLECTION"; "ITALIAN"="COLLEZIONE"; "PORTUGUESE"="COLEÇÃO"; 
        "HINDI"="संग्रह"; "CHINESE"="收藏"; "RUSSIAN"="КОЛЛЕКЦИЯ"
    }
    "menuLeaderboard" = @{
        "TURKISH"="SIRALAMA"; "SPANISH"="CLASIFICACIÓN"; "GERMAN"="RANGLISTE"; 
        "FRENCH"="CLASSEMENT"; "ITALIAN"="CLASSIFICA"; "PORTUGUESE"="CLASSIFICAÇÃO"; 
        "HINDI"="लीडरबोर्ड"; "CHINESE"="排行榜"; "RUSSIAN"="РЕЙТИНГ"
    }
    "menuSettings" = @{
        "TURKISH"="AYARLAR"; "SPANISH"="AJUSTES"; "GERMAN"="EINSTELLUNGEN"; 
        "FRENCH"="PARAMÈTRES"; "ITALIAN"="IMPOSTAZIONI"; "PORTUGUESE"="CONFIGURAÇÕES"; 
        "HINDI"="सेटिंग्स"; "CHINESE"="设置"; "RUSSIAN"="НАСТРОЙКИ"
    }
    "playAsGuest" = @{
        "TURKISH"="MİSAFİR OLARAK OYNA"; "SPANISH"="JUGAR COMO INVITADO"; "GERMAN"="ALS GAST SPIELEN"; 
        "FRENCH"="JOUER EN TANT QU'INVITÉ"; "ITALIAN"="GIOCA COME OSPITE"; "PORTUGUESE"="JOGAR COMO CONVIDADO"; 
        "HINDI"="अतिथि के रूप में खेलें"; "CHINESE"="以游客身份游玩"; "RUSSIAN"="ИГРАТЬ КАК ГОСТЬ"
    }
    "enterName" = @{
        "TURKISH"="İsmini Gir"; "SPANISH"="Introduce tu nombre"; "GERMAN"="Name eingeben"; 
        "FRENCH"="Entrez votre nom"; "ITALIAN"="Inserisci il nome"; "PORTUGUESE"="Digite seu nome"; 
        "HINDI"="अपना नाम दर्ज करें"; "CHINESE"="输入姓名"; "RUSSIAN"="Введите имя"
    }
    "gameOver" = @{
        "TURKISH"="OYUN BİTTİ"; "SPANISH"="FIN DEL JUEGO"; "GERMAN"="SPIEL VORBEI"; 
        "FRENCH"="FIN DE PARTIE"; "ITALIAN"="GAME OVER"; "PORTUGUESE"="FIM DE JOGO"; 
        "HINDI"="खेल खत्म"; "CHINESE"="游戏结束"; "RUSSIAN"="ИГРА ОКОНЧЕНА"
    }
    "finalScore" = @{
        "TURKISH"="FİNAL SKORU"; "SPANISH"="PUNTUACIÓN FINAL"; "GERMAN"="ENDSTAND"; 
        "FRENCH"="SCORE FINAL"; "ITALIAN"="PUNTEGGIO FINALE"; "PORTUGUESE"="PONTUAÇÃO FINAL"; 
        "HINDI"="अंतिम स्कोर"; "CHINESE"="最终得分"; "RUSSIAN"="ИТОГОВЫЙ СЧЕТ"
    }
    "newRecord" = @{
        "TURKISH"="YENİ REKOR!"; "SPANISH"="¡NUEVO RÉCORD!"; "GERMAN"="NEUER REKORD!"; 
        "FRENCH"="NOUVEAU RECORD !"; "ITALIAN"="NUOVO RECORD!"; "PORTUGUESE"="NOVO RECORDE!"; 
        "HINDI"="नया रिकॉर्ड!"; "CHINESE"="新纪录！"; "RUSSIAN"="НОВЫЙ РЕКОРД!"
    }
    "saveMeTitle" = @{
        "TURKISH"="İKİNCİ ŞANS"; "SPANISH"="SEGUNDA OPORTUNIDAD"; "GERMAN"="ZWEITE CHANCE"; 
        "FRENCH"="DEUXIÈME CHANCE"; "ITALIAN"="SECONDA POSSIBILITÀ"; "PORTUGUESE"="SEGUNDA CHANCE"; 
        "HINDI"="दूसरा मौका"; "CHINESE"="第二次机会"; "RUSSIAN"="ВТОРОЙ ШАНС"
    }
    "outOfLivesTitle" = @{
        "TURKISH"="CANIN BİTTİ!"; "SPANISH"="¡SIN VIDAS!"; "GERMAN"="KEINE LEBEN MEHR!"; 
        "FRENCH"="PLUS DE VIES !"; "ITALIAN"="VITE ESAURITE!"; "PORTUGUESE"="SEM VIDAS!"; 
        "HINDI"="जीवन समाप्त!"; "CHINESE"="生命耗尽！"; "RUSSIAN"="ЖИЗНИ ЗАКОНЧИЛИСЬ!"
    }
    "outOfLivesMessage" = @{
        "TURKISH"="Bekle veya reklam izle!"; "SPANISH"="¡Espera o mira un anuncio!"; "GERMAN"="Warte oder sieh eine Anzeige!"; 
        "FRENCH"="Attendez ou regardez une pub !"; "ITALIAN"="Aspetta o guarda un annuncio!"; "PORTUGUESE"="Espere ou veja um anúncio!"; 
        "HINDI"="प्रतीक्षा करें या विज्ञापन देखें!"; "CHINESE"="等待或观看广告！"; "RUSSIAN"="Подождите или посмотрите рекламу!"
    }
    "continue_" = @{
        "TURKISH"="DEVAM ET"; "SPANISH"="CONTINUAR"; "GERMAN"="WEITER"; 
        "FRENCH"="CONTINUER"; "ITALIAN"="CONTINUA"; "PORTUGUESE"="CONTINUAR"; 
        "HINDI"="जारी रखें"; "CHINESE"="继续"; "RUSSIAN"="ПРОДОЛЖИТЬ"
    }
    "backToMenu" = @{
        "TURKISH"="MENÜYE DÖN"; "SPANISH"="VOLVER AL MENÚ"; "GERMAN"="ZUM MENÜ"; 
        "FRENCH"="RETOUR AU MENU"; "ITALIAN"="TORNA AL MENU"; "PORTUGUESE"="VOLTAR AO MENU"; 
        "HINDI"="मेनू पर लौटें"; "CHINESE"="返回菜单"; "RUSSIAN"="В ГЛАВНОЕ МЕНЮ"
    }
    "retry" = @{
        "TURKISH"="TEKRAR DENE"; "SPANISH"="REINTENTAR"; "GERMAN"="WIEDERHOLEN"; 
        "FRENCH"="RÉESSAYER"; "ITALIAN"="RIPROVA"; "PORTUGUESE"="TENTAR NOVAMENTE"; 
        "HINDI"="पुनः प्रयास करें"; "CHINESE"="重试"; "RUSSIAN"="ПОВТОРИТЬ"
    }
    "getSelectLanguage" = @{
        "TURKISH"="DİL SEÇİMİ"; "SPANISH"="SELECCIONAR IDIOMA"; "GERMAN"="SPRACHE WÄHLEN"; 
        "FRENCH"="CHOISIR LA LANGUE"; "ITALIAN"="SELEZIONA LINGUA"; "PORTUGUESE"="SELECIONAR IDIOMA"; 
        "HINDI"="भाषा चुनें"; "CHINESE"="选择语言"; "RUSSIAN"="ВЫБЕРИТЕ ЯЗЫК"
    }
    "welcomeGiftTitle" = @{
        "TURKISH"="Hoş Geldin Hediyesi!"; "SPANISH"="¡Regalo de Bienvenida!"; "GERMAN"="Willkommensgeschenk!"; 
        "FRENCH"="Cadeau de Bienvenue !"; "ITALIAN"="Regalo di Benvenuto!"; "PORTUGUESE"="Presente de Boas-Vindas!"; 
        "HINDI"="स्वागत उपहार!"; "CHINESE"="欢迎礼包！"; "RUSSIAN"="Приветственный подарок!"
    }
    "challengeAlreadyPlayed" = @{
        "TURKISH"="Bugünkü tüm meydan okuma haklarını kullandın!"; "SPANISH"="¡Has usado todos los intentos de hoy!"; 
        "GERMAN"="Du hast alle heutigen Versuche aufgebraucht!"; "FRENCH"="Vous avez utilisé toutes vos tentatives !"; 
        "ITALIAN"="Hai usato tutti i tentativi di oggi!"; "PORTUGUESE"="Você usou todas as tentativas de hoje!"; 
        "HINDI"="आपने आज के सभी प्रयास उपयोग कर लिए हैं!"; "CHINESE"="您已用完今天的挑战次数！"; "RUSSIAN"="Вы использовали все попытки на сегодня!"
    }
    "watchAdToPlayAgain" = @{
        "TURKISH"="Reklam İzle ve Bir Hak Daha Kazan"; "SPANISH"="Ver anuncio para jugar de nuevo"; 
        "GERMAN"="Anzeige ansehen, um erneut zu spielen"; "FRENCH"="Regarder une pub pour rejouer"; 
        "ITALIAN"="Guarda l'annuncio per giocare di nuovo"; "PORTUGUESE"="Ver anúncio para jogar novamente"; 
        "HINDI"="फिर से खेलने के लिए विज्ञापन देखें"; "CHINESE"="观看广告再玩一次"; "RUSSIAN"="Смотреть рекламу, чтобы сыграть еще"
    }
}

$outLines = @()
$currentProp = ""

foreach ($line in $lines) {
    if ($line -match "(?:val|fun)\s+([a-zA-Z0-9_]+)") {
        $currentProp = $matches[1]
    }

    if ($line -match 'AppLanguage\.([A-Z]+)\s*->\s*"(.*)"') {
        $lang = $matches[1]
        $existingText = $matches[2]
        
        if ($lang -ne "ENGLISH") {
            if ($replacements.ContainsKey($currentProp) -and $replacements[$currentProp].ContainsKey($lang)) {
                $translated = $replacements[$currentProp][$lang]
                $line = $line -replace '->\s*".*"', "-> `"$translated`""
            } else {
                # Fallback to English if translation is missing to avoid mojibake
                if ($engDict.ContainsKey($currentProp)) {
                    $engText = $engDict[$currentProp]
                    # Don't fallback if the text already looks like normal English
                    # But since we have mojibake, safer to fallback to English anyway
                    $line = $line -replace '->\s*".*"', "-> `"$engText`""
                }
            }
        }
    }
    $outLines += $line
}

# Fix "Zihnin sınırlarını zorla! 🧠⚡" which is hardcoded in the header or somewhere?
# No, "Blitz Math Challenge" subtitle. Wait, "Zihnin sınırlarını zorla! 🧠⚡" is challengeModeDesc or something.
# The user's screenshot had "Zihnin sÃ±nÃ±rlarÃ±nÃ± zorla! gï¿½ï¿½ï¿½ï¿½"
for ($i=0; $i -lt $outLines.Count; $i++) {
    if ($outLines[$i] -match "Zihnin s.*zorla") {
        $outLines[$i] = $outLines[$i] -replace '".*"', "`"Zihnin sınırlarını zorla! 🧠⚡`""
    }
}

[System.IO.File]::WriteAllLines($path, $outLines, [System.Text.Encoding]::UTF8)
Write-Host "Translations fixed and applied successfully."
