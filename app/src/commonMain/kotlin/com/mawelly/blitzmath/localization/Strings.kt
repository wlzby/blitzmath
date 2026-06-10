package com.mawelly.blitzmath.localization

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object Strings {

    private val _currentLanguage = MutableStateFlow(AppLanguage.TURKISH)
    val currentLanguageFlow: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    var currentLanguage: AppLanguage by mutableStateOf(AppLanguage.TURKISH)
        private set

    fun setLanguage(lang: AppLanguage) { currentLanguage = lang; _currentLanguage.value = lang }

    val bannedWords = listOf("piç", "pic", "amk", "ananı", "göt", "oç", "orospu", "yarrak", "sik", "amcık", "meme", "mal", "salak")

    val menuClassic: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "KLASİK MOD"
        AppLanguage.ENGLISH -> "CLASSIC MODE"
        AppLanguage.SPANISH -> "MODO CLÁSICO"
        AppLanguage.GERMAN -> "KLASSISCHER MODUS"
        AppLanguage.FRENCH -> "MODE CLASSIQUE"
        AppLanguage.ITALIAN -> "MODALITÀ CLASSICA"
        AppLanguage.PORTUGUESE -> "MODO CLÁSSICO"
        AppLanguage.HINDI -> "क्लासिक मोड"
        AppLanguage.CHINESE -> "经典模式"
        AppLanguage.RUSSIAN -> "КЛАССИЧЕСКИЙ РЕЖИМ"
        else -> "CLASSIC MODE"
    }

    val menuMixed: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "KARIŞIK MOD"
        AppLanguage.ENGLISH -> "MIXED MODE"
        AppLanguage.SPANISH -> "MODO MIXTO"
        AppLanguage.GERMAN -> "GEMISCHTER MODUS"
        AppLanguage.FRENCH -> "MODE MIXTE"
        AppLanguage.ITALIAN -> "MODALITÀ MISTA"
        AppLanguage.PORTUGUESE -> "MODO MISTO"
        AppLanguage.HINDI -> "मिश्रित मोड"
        AppLanguage.CHINESE -> "混合模式"
        AppLanguage.RUSSIAN -> "СМЕШАННЫЙ РЕЖИМ"
        else -> "MIXED MODE"
    }

    val menuChallenge: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "MEYDAN OKUMA"
        AppLanguage.ENGLISH -> "CHALLENGE"
        AppLanguage.SPANISH -> "DESAFÍO"
        AppLanguage.GERMAN -> "HERAUSFORDERUNG"
        AppLanguage.FRENCH -> "DÉFI"
        AppLanguage.ITALIAN -> "SFIDA"
        AppLanguage.PORTUGUESE -> "DESAFIO"
        AppLanguage.HINDI -> "चुनौती"
        AppLanguage.CHINESE -> "挑战模式"
        AppLanguage.RUSSIAN -> "ИСПЫТАНИЕ"
        else -> "CHALLENGE"
    }

    val menuChallengeSubtitle: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "Zamana karşı yarış ve rekabet et"
        AppLanguage.ENGLISH -> "Beat the clock and compete"
        AppLanguage.SPANISH -> "Vence al reloj y compite"
        AppLanguage.GERMAN -> "Gegen die Zeit antreten"
        AppLanguage.FRENCH -> "Battez le record et participez"
        AppLanguage.ITALIAN -> "Sfida il tempo e gareggia"
        AppLanguage.PORTUGUESE -> "Vença o tempo e compita"
        AppLanguage.HINDI -> "समय को मात दें और प्रतिस्पर्धा करें"
        AppLanguage.CHINESE -> "挑战极限，参与竞争"
        AppLanguage.RUSSIAN -> "Победи время и соревнуйся"
        else -> "Beat the clock and compete"
    }

    val collection: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "KOLEKSİYON"
        AppLanguage.ENGLISH -> "COLLECTION"
        AppLanguage.SPANISH -> "COLECCIÓN"
        AppLanguage.GERMAN -> "SAMMLUNG"
        AppLanguage.FRENCH -> "COLLECTION"
        AppLanguage.ITALIAN -> "COLLEZIONE"
        AppLanguage.PORTUGUESE -> "COLEÇÃO"
        AppLanguage.HINDI -> "संग्रह"
        AppLanguage.CHINESE -> "收藏"
        AppLanguage.RUSSIAN -> "КОЛЛЕКЦИЯ"
        else -> "COLLECTION"
    }

    val menuLeaderboard: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "SIRALAMA"
        AppLanguage.ENGLISH -> "LEADERBOARD"
        AppLanguage.SPANISH -> "CLASIFICACIÓN"
        AppLanguage.GERMAN -> "RANGLISTE"
        AppLanguage.FRENCH -> "CLASSEMENT"
        AppLanguage.ITALIAN -> "CLASSIFICA"
        AppLanguage.PORTUGUESE -> "CLASSIFICAÇÃO"
        AppLanguage.HINDI -> "लीडरबोर्ड"
        AppLanguage.CHINESE -> "排行榜"
        AppLanguage.RUSSIAN -> "РЕЙТИНГ"
        else -> "LEADERBOARD"
    }

    val menuSettings: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "AYARLAR"
        AppLanguage.ENGLISH -> "SETTINGS"
        AppLanguage.SPANISH -> "AJUSTES"
        AppLanguage.GERMAN -> "EINSTELLUNGEN"
        AppLanguage.FRENCH -> "PARAMÈTRES"
        AppLanguage.ITALIAN -> "IMPOSTAZIONI"
        AppLanguage.PORTUGUESE -> "CONFIGURAÇÕES"
        AppLanguage.HINDI -> "सेटिंग्स"
        AppLanguage.CHINESE -> "设置"
        AppLanguage.RUSSIAN -> "НАСТРОЙКИ"
        else -> "SETTINGS"
    }

    val settings: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "Ayarlar"
        AppLanguage.ENGLISH -> "Settings"
        AppLanguage.SPANISH -> "Ajustes"
        AppLanguage.GERMAN -> "Einstellungen"
        AppLanguage.FRENCH -> "Paramètres"
        AppLanguage.ITALIAN -> "Impostazioni"
        AppLanguage.PORTUGUESE -> "Configurações"
        AppLanguage.HINDI -> "सेटिंग्स"
        AppLanguage.CHINESE -> "设置"
        AppLanguage.RUSSIAN -> "Настройки"
        else -> "Settings"
    }

    val music: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "Müzik"
        AppLanguage.ENGLISH -> "Music"
        AppLanguage.SPANISH -> "Música"
        AppLanguage.GERMAN -> "Musik"
        AppLanguage.FRENCH -> "Musique"
        AppLanguage.ITALIAN -> "Musica"
        AppLanguage.PORTUGUESE -> "Música"
        AppLanguage.HINDI -> "संगीत"
        AppLanguage.CHINESE -> "音乐"
        AppLanguage.RUSSIAN -> "Музыка"
        else -> "Music"
    }

    val sound: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "Ses"
        AppLanguage.ENGLISH -> "Sound"
        AppLanguage.SPANISH -> "Sonido"
        AppLanguage.GERMAN -> "Ton"
        AppLanguage.FRENCH -> "Son"
        AppLanguage.ITALIAN -> "Suono"
        AppLanguage.PORTUGUESE -> "Som"
        AppLanguage.HINDI -> "ध्वनि"
        AppLanguage.CHINESE -> "音效"
        AppLanguage.RUSSIAN -> "Звук"
        else -> "Sound"
    }

    val vibrationStrength: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "Titreşim Şiddeti"
        AppLanguage.ENGLISH -> "Vibration Strength"
        AppLanguage.SPANISH -> "Intensidad de vibración"
        AppLanguage.GERMAN -> "Vibrationsstärke"
        AppLanguage.FRENCH -> "Intensité de vibration"
        AppLanguage.ITALIAN -> "Intensità vibrazione"
        AppLanguage.PORTUGUESE -> "Intensidade da vibração"
        AppLanguage.HINDI -> "कंपन की तीव्रता"
        AppLanguage.CHINESE -> "振动强度"
        AppLanguage.RUSSIAN -> "Интенсивность вибрации"
        else -> "Vibration Strength"
    }

    val autoTheme: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "Otomatik Tema"
        AppLanguage.ENGLISH -> "Auto Theme"
        AppLanguage.SPANISH -> "Tema automático"
        AppLanguage.GERMAN -> "Automatisches Design"
        AppLanguage.FRENCH -> "Thème automatique"
        AppLanguage.ITALIAN -> "Tema automatico"
        AppLanguage.PORTUGUESE -> "Tema automático"
        AppLanguage.HINDI -> "ऑटो थीम"
        AppLanguage.CHINESE -> "自动主题"
        AppLanguage.RUSSIAN -> "Авто-тема"
        else -> "Auto Theme"
    }

    val autoThemeDesc: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "Zamana göre otomatik değişir"
        AppLanguage.ENGLISH -> "Changes automatically based on time"
        AppLanguage.SPANISH -> "Cambia automáticamente según la hora"
        AppLanguage.GERMAN -> "Ändert sich automatisch nach Zeit"
        AppLanguage.FRENCH -> "Change automatiquement selon l'heure"
        AppLanguage.ITALIAN -> "Cambia automaticamente in base all'ora"
        AppLanguage.PORTUGUESE -> "Muda automaticamente com base no tempo"
        AppLanguage.HINDI -> "समय के आधार पर स्वचालित रूप से बदलता है"
        AppLanguage.CHINESE -> "根据时间自动更改"
        AppLanguage.RUSSIAN -> "Изменяется автоматически в зависимости от времени"
        else -> "Changes automatically based on time"
    }

    val voiceFeedback: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "Sesli Geri Bildirim"
        AppLanguage.ENGLISH -> "Voice Feedback"
        AppLanguage.SPANISH -> "Comentarios de voz"
        AppLanguage.GERMAN -> "Sprachfeedback"
        AppLanguage.FRENCH -> "Retour vocal"
        AppLanguage.ITALIAN -> "Feedback vocale"
        AppLanguage.PORTUGUESE -> "Feedback de voz"
        AppLanguage.HINDI -> "वॉयस फीडबैक"
        AppLanguage.CHINESE -> "语音反馈"
        AppLanguage.RUSSIAN -> "Голосовая обратная связь"
        else -> "Voice Feedback"
    }

    val voiceFeedbackDesc: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "Soruları ve sonuçları seslendirir"
        AppLanguage.ENGLISH -> "Voices questions and results"
        AppLanguage.SPANISH -> "Vocaliza preguntas y resultados"
        AppLanguage.GERMAN -> "Spricht Fragen und Ergebnisse"
        AppLanguage.FRENCH -> "Annonce les questions et les résultats"
        AppLanguage.ITALIAN -> "Vocalizza domande e risultati"
        AppLanguage.PORTUGUESE -> "Vocaliza perguntas e resultados"
        AppLanguage.HINDI -> "प्रश्नों और परिणामों को आवाज देता है"
        AppLanguage.CHINESE -> "朗读问题和结果"
        AppLanguage.RUSSIAN -> "Озвучивает вопросы и результаты"
        else -> "Voices questions and results"
    }

    val theme: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "Tema"
        AppLanguage.ENGLISH -> "Theme"
        AppLanguage.SPANISH -> "Tema"
        AppLanguage.GERMAN -> "Thema"
        AppLanguage.FRENCH -> "Thème"
        AppLanguage.ITALIAN -> "Tema"
        AppLanguage.PORTUGUESE -> "Tema"
        AppLanguage.HINDI -> "थीम"
        AppLanguage.CHINESE -> "主题"
        AppLanguage.RUSSIAN -> "Тема"
        else -> "Theme"
    }

    val swipeHint: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "Seçmek için kaydır"
        AppLanguage.ENGLISH -> "Swipe to select"
        AppLanguage.SPANISH -> "Desliza para seleccionar"
        AppLanguage.GERMAN -> "Zum Auswählen wischen"
        AppLanguage.FRENCH -> "Balayer pour sélectionner"
        AppLanguage.ITALIAN -> "Scorri per selezionare"
        AppLanguage.PORTUGUESE -> "Deslize para selecionar"
        AppLanguage.HINDI -> "चुनने के लिए स्वाइप करें"
        AppLanguage.CHINESE -> "滑动选择"
        AppLanguage.RUSSIAN -> "Смахните для выбора"
        else -> "Swipe to select"
    }

    val languageLabel: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "Dil"
        AppLanguage.ENGLISH -> "Language"
        AppLanguage.SPANISH -> "Idioma"
        AppLanguage.GERMAN -> "Sprache"
        AppLanguage.FRENCH -> "Langue"
        AppLanguage.ITALIAN -> "Lingua"
        AppLanguage.PORTUGUESE -> "Idioma"
        AppLanguage.HINDI -> "भाषा"
        AppLanguage.CHINESE -> "语言"
        AppLanguage.RUSSIAN -> "Язык"
        else -> "Language"
    }

    val storeTitle: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "MAĞAZA"
        AppLanguage.ENGLISH -> "STORE"
        AppLanguage.SPANISH -> "TIENDA"
        AppLanguage.GERMAN -> "STORE"
        AppLanguage.FRENCH -> "BOUTIQUE"
        AppLanguage.ITALIAN -> "NEGOZIO"
        AppLanguage.PORTUGUESE -> "LOJA"
        AppLanguage.HINDI -> "स्टोर"
        AppLanguage.CHINESE -> "商店"
        AppLanguage.RUSSIAN -> "МАГАЗИН"
        else -> "STORE"
    }

    val equippedAbilities: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "Ku\u015fan\u0131lm\u0131\u015f Yetenekler"
        AppLanguage.ENGLISH -> "Equipped Abilities"
        AppLanguage.HINDI -> "\u0938\u0941\u0938\u091c\u094d\u091c\u093f\u0924 \u0915\u094d\u0937\u092e\u0924\u093e\u090f\u0902"
        AppLanguage.CHINESE -> "\u5df2\u88c5\u5907\u6280\u80fd"
        AppLanguage.RUSSIAN -> "\u042d\u043a\u0438\u043f\u0438\u0440\u043e\u0432\u0430\u043d\u043d\u044b\u0435 \u0441\u043f\u043e\u0441\u043e\u0431\u043d\u043e\u0441\u0442\u0438"
        else -> "Equipped Abilities"
    }

    val noAbilitiesEquipped: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "Henüz yetenek kuşanılmadı."
        AppLanguage.ENGLISH -> "No abilities equipped yet."
        AppLanguage.SPANISH -> "Aún no hay habilidades equipadas."
        AppLanguage.GERMAN -> "Noch keine Fähigkeiten ausgerüstet."
        AppLanguage.FRENCH -> "Aucune capacité équipée pour le moment."
        AppLanguage.ITALIAN -> "Ancora nessuna abilità equipaggiata."
        AppLanguage.PORTUGUESE -> "Nenhuma habilidade equipada ainda."
        AppLanguage.HINDI -> "अभी तक कोई क्षमताएं सुसज्जित नहीं हैं।"
        AppLanguage.CHINESE -> "尚未装备任何技能。"
        AppLanguage.RUSSIAN -> "Способности еще не экипированы."
        else -> "No abilities equipped yet."
    }

    val usageRights: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "Kullanım Hakları"
        AppLanguage.ENGLISH -> "Usage Rights"
        AppLanguage.SPANISH -> "Derechos de uso"
        AppLanguage.GERMAN -> "Nutzungsrechte"
        AppLanguage.FRENCH -> "Droits d'utilisation"
        AppLanguage.ITALIAN -> "Diritti d'uso"
        AppLanguage.PORTUGUESE -> "Direitos de uso"
        AppLanguage.HINDI -> "उपयोग अधिकार"
        AppLanguage.CHINESE -> "使用权"
        AppLanguage.RUSSIAN -> "Права использования"
        else -> "Usage Rights"
    }

    val paused: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "DURAKLATILDI"
        AppLanguage.ENGLISH -> "PAUSED"
        AppLanguage.SPANISH -> "PAUSADO"
        AppLanguage.GERMAN -> "PAUSE"
        AppLanguage.FRENCH -> "PAUSE"
        AppLanguage.ITALIAN -> "IN PAUSA"
        AppLanguage.PORTUGUESE -> "PAUSADO"
        AppLanguage.HINDI -> "ठहरा हुआ"
        AppLanguage.CHINESE -> "暂停"
        AppLanguage.RUSSIAN -> "ПАУЗА"
        else -> "PAUSED"
    }

    val reviewInvitationTitle: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "Oyunumuzu Beğendiniz mi?"
        AppLanguage.ENGLISH -> "Enjoying Blitz Math?"
        AppLanguage.SPANISH -> "¿Te gusta Blitz Math?"
        AppLanguage.GERMAN -> "Gefällt dir Blitz Math?"
        AppLanguage.FRENCH -> "Vous aimez Blitz Math ?"
        AppLanguage.ITALIAN -> "Ti piace Blitz Math?"
        AppLanguage.PORTUGUESE -> "Gostando do Blitz Math?"
        AppLanguage.HINDI -> "ब्लिट्ज मैथ का आनंद ले रहे हैं?"
        AppLanguage.CHINESE -> "喜欢 Blitz Math 吗？"
        AppLanguage.RUSSIAN -> "Нравится Blitz Math?"
        else -> "Enjoying Blitz Math?"
    }

    val reviewInvitationMessage: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "Gelişmemize yardımcı olmak için yorum bırakmak ister misiniz? 1000 Yıldız hediyemiz olacak!"
        AppLanguage.ENGLISH -> "Would you like to leave a quick review to help us grow? 1000 Stars will be our gift to you!"
        AppLanguage.SPANISH -> "¿Te gustaría dejar una reseña para ayudarnos a crecer? ¡Te regalaremos 1000 estrellas!"
        AppLanguage.GERMAN -> "Möchtest du eine Bewertung abgeben? Wir schenken dir 1000 Sterne!"
        AppLanguage.FRENCH -> "Voulez-vous laisser un avis pour nous aider ? 1000 étoiles vous seront offertes !"
        AppLanguage.ITALIAN -> "Ti piacerebbe lasciare una recensione? Ti regaleremo 1000 stelle!"
        AppLanguage.PORTUGUESE -> "Gostaria de deixar uma avaliação? 1000 estrelas serão nosso presente!"
        AppLanguage.HINDI -> "क्या आप हमारी मदद करने के लिए एक त्वरित समीक्षा छोड़ना चाहेंगे? 1000 सितारे आपको हमारा उपहार होगा!"
        AppLanguage.CHINESE -> "您想留下一条简短的评论帮助我们成长吗？我们将会送您1000颗星星作为礼物！"
        AppLanguage.RUSSIAN -> "Не хотели бы вы оставить краткий отзыв, чтобы помочь нам расти? 1000 Звезд станут нашим подарком вам!"
        else -> "Would you like to leave a quick review to help us grow? 1000 Stars will be our gift to you!"
    }

    val rateNow: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "Puanla"
        AppLanguage.ENGLISH -> "Rate Now"
        AppLanguage.SPANISH -> "Calificar ahora"
        AppLanguage.GERMAN -> "Jetzt bewerten"
        AppLanguage.FRENCH -> "Noter maintenant"
        AppLanguage.ITALIAN -> "Valuta ora"
        AppLanguage.PORTUGUESE -> "Avaliar agora"
        AppLanguage.HINDI -> "अभी रेट करें"
        AppLanguage.CHINESE -> "立即评价"
        AppLanguage.RUSSIAN -> "Оценить сейчас"
        else -> "Rate Now"
    }

    val noThanks: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "Hayır, Teşekkürler"
        AppLanguage.ENGLISH -> "No Thanks"
        AppLanguage.SPANISH -> "No, gracias"
        AppLanguage.GERMAN -> "Nein, danke"
        AppLanguage.FRENCH -> "Non, merci"
        AppLanguage.ITALIAN -> "No, grazie"
        AppLanguage.PORTUGUESE -> "Não, obrigado"
        AppLanguage.HINDI -> "नहीं धन्यवाद"
        AppLanguage.CHINESE -> "不，谢谢"
        AppLanguage.RUSSIAN -> "Нет, спасибо"
        else -> "No Thanks"
    }

    val welcomeGiftTitle: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "Hoş Geldin Hediyesi!"
        AppLanguage.ENGLISH -> "Welcome Gift!"
        AppLanguage.SPANISH -> "¡Regalo de bienvenida!"
        AppLanguage.GERMAN -> "Willkommensgeschenk!"
        AppLanguage.FRENCH -> "Cadeau de bienvenue !"
        AppLanguage.ITALIAN -> "Regalo di benvenuto!"
        AppLanguage.PORTUGUESE -> "Presente de boas-vindas!"
        AppLanguage.HINDI -> "स्वागत उपहार!"
        AppLanguage.CHINESE -> "欢迎礼物！"
        AppLanguage.RUSSIAN -> "Приветственный подарок!"
        else -> "Welcome Gift!"
    }

    val welcomeGiftMessage: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "BlitzMath ailesine hoş geldin! İlk oyunun şerefine sana Pisagor kartını hediye ediyoruz."
        AppLanguage.ENGLISH -> "Welcome to the BlitzMath family! In honor of your first game, we are gifting you the Pythagoras card."
        AppLanguage.SPANISH -> "¡Bienvenido a la familia BlitzMath! En honor a tu primer juego, te regalamos la carta de Pitágoras."
        AppLanguage.GERMAN -> "Willkommen in der BlitzMath-Familie! Zu Ehren deines ersten Spiels schenken wir dir die Pythagoras-Karte."
        AppLanguage.FRENCH -> "Bienvenue dans la famille BlitzMath ! En l'honneur de votre premier jeu, nous vous offrons la carte Pythagore."
        AppLanguage.ITALIAN -> "Benvenuto nella famiglia BlitzMath! In onore della tua prima partita, ti regaliamo la carta di Pitagora."
        AppLanguage.PORTUGUESE -> "Bem-vindo à família BlitzMath! Em honra ao seu primeiro jogo, estamos lhe presenteando com a carta de Pitágoras."
        AppLanguage.HINDI -> "BlitzMath परिवार में आपका स्वागत है! आपके पहले खेल के सम्मान में, हम आपको पाइथागोरस कार्ड उपहार में दे रहे हैं।"
        AppLanguage.CHINESE -> "欢迎来到 BlitzMath 家族！为了庆祝您的第一次游戏，我们赠送您毕达哥拉斯卡片。"
        AppLanguage.RUSSIAN -> "Добро пожаловать в семью BlitzMath! В честь вашей первой игры мы дарим вам карту Пифагора."
        else -> "Welcome to the BlitzMath family! In honor of your first game, we are gifting you the Pythagoras card."
    }

    val ok: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "Tamam"
        AppLanguage.ENGLISH -> "OK"
        AppLanguage.SPANISH -> "Aceptar"
        AppLanguage.GERMAN -> "OK"
        AppLanguage.FRENCH -> "OK"
        AppLanguage.ITALIAN -> "OK"
        AppLanguage.PORTUGUESE -> "OK"
        AppLanguage.HINDI -> "ठीक है"
        AppLanguage.CHINESE -> "确定"
        AppLanguage.RUSSIAN -> "ОК"
        else -> "OK"
    }

    val statScore: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "PUAN"
        AppLanguage.ENGLISH -> "SCORE"
        AppLanguage.SPANISH -> "PUNTOS"
        AppLanguage.GERMAN -> "PUNKTE"
        AppLanguage.FRENCH -> "SCORE"
        AppLanguage.ITALIAN -> "PUNTEGGIO"
        AppLanguage.PORTUGUESE -> "PONTOS"
        AppLanguage.HINDI -> "स्कोर"
        AppLanguage.CHINESE -> "分数"
        AppLanguage.RUSSIAN -> "СЧЕТ"
        else -> "SCORE"
    }

    val statSpeed: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "HIZ"
        AppLanguage.ENGLISH -> "SPEED"
        AppLanguage.SPANISH -> "VELOCIDAD"
        AppLanguage.GERMAN -> "TEMPO"
        AppLanguage.FRENCH -> "VITESSE"
        AppLanguage.ITALIAN -> "VELOCITÀ"
        AppLanguage.PORTUGUESE -> "VELOCIDADE"
        AppLanguage.HINDI -> "गति"
        AppLanguage.CHINESE -> "速度"
        AppLanguage.RUSSIAN -> "СКОРОСТЬ"
        else -> "SPEED"
    }

    val statCheck: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "KONTROL"
        AppLanguage.ENGLISH -> "CHECK"
        AppLanguage.SPANISH -> "COMPROBAR"
        AppLanguage.GERMAN -> "PRÜFEN"
        AppLanguage.FRENCH -> "VÉRIFIER"
        AppLanguage.ITALIAN -> "VERIFICA"
        AppLanguage.PORTUGUESE -> "VERIFICAR"
        AppLanguage.HINDI -> "चेक"
        AppLanguage.CHINESE -> "检查"
        AppLanguage.RUSSIAN -> "ПРОВЕРКА"
        else -> "CHECK"
    }

    val statStreak: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "SERİ"
        AppLanguage.ENGLISH -> "STREAK"
        AppLanguage.SPANISH -> "RACHA"
        AppLanguage.GERMAN -> "SERIE"
        AppLanguage.FRENCH -> "SÉRIE"
        AppLanguage.ITALIAN -> "SERIE"
        AppLanguage.PORTUGUESE -> "SEQUÊNCIA"
        AppLanguage.HINDI -> "स्ट्रीक"
        AppLanguage.CHINESE -> "连胜"
        AppLanguage.RUSSIAN -> "СЕРИЯ"
        else -> "STREAK"
    }

    val question: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "SORU"
        AppLanguage.ENGLISH -> "Question"
        AppLanguage.HINDI -> "\u092a\u094d\u0930\u0936\u094d\u0928"
        AppLanguage.CHINESE -> "\u95ee\u9898"
        AppLanguage.RUSSIAN -> "\u0412\u043e\u043f\u0440\u043e\u0441"
        else -> "Question"
    }

    val time: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "SÜRE"
        AppLanguage.ENGLISH -> "TIME"
        AppLanguage.SPANISH -> "TIEMPO"
        AppLanguage.GERMAN -> "ZEIT"
        AppLanguage.FRENCH -> "TEMPS"
        AppLanguage.ITALIAN -> "TEMPO"
        AppLanguage.PORTUGUESE -> "TEMPO"
        AppLanguage.HINDI -> "समय"
        AppLanguage.CHINESE -> "时间"
        AppLanguage.RUSSIAN -> "ВРЕМЯ"
        else -> "TIME"
    }

    val addition: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "Toplama"
        AppLanguage.ENGLISH -> "Addition"
        AppLanguage.HINDI -> "\u091c\u094b\u0921\u093c"
        AppLanguage.CHINESE -> "\u52a0\u6cd5"
        AppLanguage.RUSSIAN -> "\u0421\u043b\u043e\u0436\u0435\u043d\u0438\u0435"
        else -> "Addition"
    }

    val subtraction: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "\u00c7\u0131karma"
        AppLanguage.ENGLISH -> "Subtraction"
        AppLanguage.HINDI -> "\u0918\u091f\u093e\u0935"
        AppLanguage.CHINESE -> "\u51cf\u6cd5"
        AppLanguage.RUSSIAN -> "\u0412\u044b\u0447\u0438\u0442\u0430\u043d\u0438\u0435"
        else -> "Subtraction"
    }

    val multiplication: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "\u00c7arpma"
        AppLanguage.ENGLISH -> "Multiplication"
        AppLanguage.HINDI -> "\u0917\u0941\u0923\u093e"
        AppLanguage.CHINESE -> "\u4e58\u6cd5"
        AppLanguage.RUSSIAN -> "\u0423\u043c\u043d\u043e\u0436\u0435\u043d\u0438\u0435"
        else -> "Multiplication"
    }

    val division: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "B\u00f6lme"
        AppLanguage.ENGLISH -> "Division"
        AppLanguage.HINDI -> "\u092d\u093e\u0917"
        AppLanguage.CHINESE -> "\u9664\u6cd5"
        AppLanguage.RUSSIAN -> "\u0414\u0435\u043b\u0435\u043d\u0438\u0435"
        else -> "Division"
    }

    val mixed: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "Kar\u0131\u015f\u0131k"
        AppLanguage.ENGLISH -> "Mixed"
        AppLanguage.HINDI -> "\u092e\u093f\u0936\u094d\u0930\u093f\u0924"
        AppLanguage.CHINESE -> "\u6df7\u5408"
        AppLanguage.RUSSIAN -> "\u0421\u043c\u0435\u0448\u0430\u043d\u043d\u044b\u0439"
        else -> "Mixed"
    }

    val checkpointLabel: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "Seviye"
        AppLanguage.ENGLISH -> "Checkpoint"
        AppLanguage.SPANISH -> "Punto de control"
        AppLanguage.GERMAN -> "Kontrollpunkt"
        AppLanguage.FRENCH -> "Point de passage"
        AppLanguage.ITALIAN -> "Punto di controllo"
        AppLanguage.PORTUGUESE -> "Ponto de controle"
        AppLanguage.HINDI -> "चेकपॉइंट"
        AppLanguage.CHINESE -> "关卡"
        AppLanguage.RUSSIAN -> "Контрольная точка"
        else -> "Checkpoint"
    }

    val congratulations: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "Tebrikler!"
        AppLanguage.ENGLISH -> "Congratulations!"
        AppLanguage.SPANISH -> "¡Felicidades!"
        AppLanguage.GERMAN -> "Glückwunsch!"
        AppLanguage.FRENCH -> "Félicitations !"
        AppLanguage.ITALIAN -> "Congratulazioni!"
        AppLanguage.PORTUGUESE -> "Parabéns!"
        AppLanguage.HINDI -> "बधाई हो!"
        AppLanguage.CHINESE -> "恭喜！"
        AppLanguage.RUSSIAN -> "Поздравляем!"
        else -> "Congratulations!"
    }

    val nextLevel: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "Sonraki Seviye"
        AppLanguage.ENGLISH -> "Next Level"
        AppLanguage.SPANISH -> "Siguiente nivel"
        AppLanguage.GERMAN -> "Nächstes Level"
        AppLanguage.FRENCH -> "Niveau suivant"
        AppLanguage.ITALIAN -> "Prossimo livello"
        AppLanguage.PORTUGUESE -> "Próximo nível"
        AppLanguage.HINDI -> "अगला स्तर"
        AppLanguage.CHINESE -> "下一关"
        AppLanguage.RUSSIAN -> "Следующий уровень"
        else -> "Next Level"
    }

    val watchAdToSave: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "Canlanmak için İzle"
        AppLanguage.ENGLISH -> "Watch Ad to Resurrect"
        AppLanguage.SPANISH -> "Ver anuncio para resucitar"
        AppLanguage.GERMAN -> "Anzeige ansehen zum Wiederbeleben"
        AppLanguage.FRENCH -> "Regarder une pub pour ressusciter"
        AppLanguage.ITALIAN -> "Guarda l'annuncio per risorgere"
        AppLanguage.PORTUGUESE -> "Ver anúncio para ressuscitar"
        AppLanguage.HINDI -> "पुनर्जीवित करने के लिए विज्ञापन देखें"
        AppLanguage.CHINESE -> "观看广告以复活"
        AppLanguage.RUSSIAN -> "Посмотреть рекламу для воскрешения"
        else -> "Watch Ad to Resurrect"
    }

    val totalTimeBonus: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "Toplam Süre Bonusu"
        AppLanguage.ENGLISH -> "Total Time Bonus"
        AppLanguage.SPANISH -> "Bono de tiempo total"
        AppLanguage.GERMAN -> "Gesamtzeitbonus"
        AppLanguage.FRENCH -> "Bonus de temps total"
        AppLanguage.ITALIAN -> "Bonus tempo totale"
        AppLanguage.PORTUGUESE -> "Bônus de tempo total"
        AppLanguage.HINDI -> "कुल समय बोनस"
        AppLanguage.CHINESE -> "总时间奖励"
        AppLanguage.RUSSIAN -> "Общий бонус времени"
        else -> "Total Time Bonus"
    }

    val watchAdContinue: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "Reklamla Devam Et"
        AppLanguage.ENGLISH -> "Continue with Ad"
        AppLanguage.SPANISH -> "Continuar con anuncio"
        AppLanguage.GERMAN -> "Mit Anzeige fortfahren"
        AppLanguage.FRENCH -> "Continuer avec une pub"
        AppLanguage.ITALIAN -> "Continua con l'annuncio"
        AppLanguage.PORTUGUESE -> "Continuar com anúncio"
        AppLanguage.HINDI -> "विज्ञापन के साथ जारी रखें"
        AppLanguage.CHINESE -> "继续观看广告"
        AppLanguage.RUSSIAN -> "Продолжить с рекламой"
        else -> "Continue with Ad"
    }

    private val turkishSlogans = listOf(
        "Hızlı düşün, doğru vur! 🧠⚡",
        "Günde 10 dakika zihnini güçlendirir! 💪",
        "Matematik dehası olmaya ilk adım! 🎯",
        "Beynini zirveye taşı! 🚀",
        "Her soru bir zaferdir! 🏆",
        "Zihin antrenmanı başlasın! 🧘",
        "Hesaplama ustası ol! 🎓",
        "Rakamlarla dans et! 💃",
        "Zihninin sınırlarını zorla! 🔥",
        "Hız ve doğruluk bir arada! ⚡"
    )

    private val englishSlogans = listOf(
        "Think fast, strike true! 🧠⚡",
        "10 minutes a day strengthens your mind! 💪",
        "First step to becoming a math genius! 🎯",
        "Take your brain to the top! 🚀",
        "Every question is a victory! 🏆",
        "Let the mind training begin! 🧘",
        "Become a master of calculation! 🎓",
        "Dance with numbers! 💃",
        "Push the limits of your mind! 🔥",
        "Speed and accuracy in one! ⚡"
    )

    private val spanishSlogans = listOf(
        "¡Piensa rápido, acierta seguro! 🧠⚡",
        "¡10 minutos al día fortalecen tu mente! 💪",
        "¡Primer paso para ser un genio! 🎯",
        "¡Lleva tu cerebro a la cima! 🚀",
        "¡Cada pregunta es una victoria! 🏆",
        "¡Que empiece el entrenamiento! 🧘",
        "¡Sé un maestro del cálculo! 🎓",
        "¡Baila con los números! 💃",
        "¡Desafía tus límites! 🔥",
        "¡Velocidad y precisión! ⚡"
    )

    private val germanSlogans = listOf(
        "Schnell denken, richtig treffen! 🧠⚡",
        "10 Minuten am Tag stärken den Geist! 💪",
        "Der erste Schritt zum Mathe-Genie! 🎯",
        "Bring dein Gehirn an die Spitze! 🚀",
        "Jede Frage ist ein Sieg! 🏆",
        "Das Gehirntraining beginnt! 🧘",
        "Werde zum Rechenmeister! 🎓",
        "Tanze mit den Zahlen! 💃",
        "Teste deine Grenzen! 🔥",
        "Tempo und Präzision! ⚡"
    )

    private val frenchSlogans = listOf(
        "Pensez vite, visez juste ! 🧠⚡",
        "10 minutes par jour musclent l'esprit ! 💪",
        "Premier pas vers le génie des maths ! 🎯",
        "Propulsez votre cerveau au sommet ! 🚀",
        "Chaque question est une victoire ! 🏆",
        "Que l'entraînement commence ! 🧘",
        "Devenez un maître du calcul ! 🎓",
        "Dansez avec les chiffres ! 💃",
        "Repoussez vos limites ! 🔥",
        "Vitesse et précision ! ⚡"
    )

    private val italianSlogans = listOf(
        "Pensa in fretta, colpisci giusto! 🧠⚡",
        "10 minuti al giorno rafforzano la mente! 💪",
        "Primo passo per diventare un genio! 🎯",
        "Porta il tuo cervello in cima! 🚀",
        "Ogni domanda è una vittoria! 🏆",
        "Inizia l'allenamento mentale! 🧘",
        "Diventa un maestro del calcolo! 🎓",
        "Danza con i numeri! 💃",
        "Sfida i tuoi limiti! 🔥",
        "Velocità e precisione! ⚡"
    )

    private val portugueseSlogans = listOf(
        "Pense rápido, acerte em cheio! 🧠⚡",
        "10 minutos por dia fortalecem a mente! 💪",
        "Primeiro passo para ser um gênio! 🎯",
        "Leve seu cérebro ao topo! 🚀",
        "Cada pergunta é uma vitória! 🏆",
        "Que comece o treino mental! 🧘",
        "Seja um mestre do cálculo! 🎓",
        "Dance com os números! 💃",
        "Desafie seus limites! 🔥",
        "Velocidade e precisão! ⚡"
    )

    private val hindiSlogans = listOf(
        "तेजी से सोचें, सही निशाना लगाएं! 🧠⚡",
        "दिन में 10 मिनट दिमाग को मजबूत करते हैं! 💪",
        "गणित का जीनियस बनने का पहला कदम! 🎯",
        "अपने दिमाग को शिखर पर ले जाएं! 🚀",
        "हर प्रश्न एक जीत है! 🏆",
        "दिमागी कसरत शुरू करें! 🧘",
        "गणना के मास्टर बनें! 🎓",
        "संख्याओं के साथ खेलें! 💃",
        "अपनी सीमाओं को चुनौती दें! 🔥",
        "गति और सटीकता एक साथ! ⚡"
    )

    private val chineseSlogans = listOf(
        "快速思考，精准出击！🧠⚡",
        "每天10分钟，锻炼你的大脑！💪",
        "迈向数学天才的第一步！🎯",
        "让你的大脑达到巅峰！🚀",
        "每一个问题都是一次胜利！🏆",
        "开始你的脑力训练吧！🧘",
        "成为计算大师！🎓",
        "与数字共舞！💃",
        "挑战你的极限！ 🔥",
        "速度与精准的完美结合！⚡"
    )

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
        "Скорость и точность в одном флакоne! ⚡"
    )

    val slogan: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> turkishSlogans.random()
        AppLanguage.ENGLISH -> englishSlogans.random()
        AppLanguage.SPANISH -> spanishSlogans.random()
        AppLanguage.GERMAN -> germanSlogans.random()
        AppLanguage.FRENCH -> frenchSlogans.random()
        AppLanguage.ITALIAN -> italianSlogans.random()
        AppLanguage.PORTUGUESE -> portugueseSlogans.random()
        AppLanguage.HINDI -> hindiSlogans.random()
        AppLanguage.CHINESE -> chineseSlogans.random()
        AppLanguage.RUSSIAN -> russianSlogans.random()
        else -> englishSlogans.random()
    }

    val otherGames: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "Diğer Oyunlarımız"
        AppLanguage.ENGLISH -> "Our Other Games"
        AppLanguage.SPANISH -> "Nuestros otros juegos"
        AppLanguage.GERMAN -> "Unsere anderen Spiele"
        AppLanguage.FRENCH -> "Nos autres jeux"
        AppLanguage.ITALIAN -> "Altri nostri giochi"
        AppLanguage.PORTUGUESE -> "Nossos outros jogos"
        AppLanguage.HINDI -> "हमारे अन्य खेल"
        AppLanguage.CHINESE -> "我们的其他游戏"
        AppLanguage.RUSSIAN -> "Наши другие игры"
        else -> "Our Other Games"
    }

    val exitDialogTitle: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "Çıkış"
        AppLanguage.ENGLISH -> "Exit"
        AppLanguage.SPANISH -> "Salir"
        AppLanguage.GERMAN -> "Beenden"
        AppLanguage.FRENCH -> "Quitter"
        AppLanguage.ITALIAN -> "Esci"
        AppLanguage.PORTUGUESE -> "Sair"
        AppLanguage.HINDI -> "बाहर निकलें"
        AppLanguage.CHINESE -> "退出"
        AppLanguage.RUSSIAN -> "Выход"
        else -> "Exit"
    }

    val exitDialogMessage: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "Ayrılmadan önce diğer oyunlarımızı denemek ister misiniz?"
        AppLanguage.ENGLISH -> "Would you like to try our other games before leaving?"
        AppLanguage.SPANISH -> "¿Te gustaría probar nuestros otros juegos antes de salir?"
        AppLanguage.GERMAN -> "Möchten Sie unsere anderen Spiele ausprobieren, bevor Sie gehen?"
        AppLanguage.FRENCH -> "Souhaitez-vous essayer nos autres jeux avant de partir ?"
        AppLanguage.ITALIAN -> "Ti piacerebbe provare i nostri altri giochi prima di uscire?"
        AppLanguage.PORTUGUESE -> "Gostaria de experimentar nossos outros jogos antes de sair?"
        AppLanguage.HINDI -> "क्या आप जाने से पहले हमारे अन्य खेलों को आजमाना चाहेंगे?"
        AppLanguage.CHINESE -> "您想在离开前尝试一下我们的其他游戏吗？"
        AppLanguage.RUSSIAN -> "Не хотели бы вы попробовать наши другие игры перед уходом?"
        else -> "Would you like to try our other games before leaving?"
    }

    val exitConfirm: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "Evet, Bak"
        AppLanguage.ENGLISH -> "Yes, Check"
        AppLanguage.SPANISH -> "Sí, ver"
        AppLanguage.GERMAN -> "Ja, prüfen"
        AppLanguage.FRENCH -> "Oui, voir"
        AppLanguage.ITALIAN -> "Sì, vedi"
        AppLanguage.PORTUGUESE -> "Sim, ver"
        AppLanguage.HINDI -> "हाँ, चेक करें"
        AppLanguage.CHINESE -> "是的，查看"
        AppLanguage.RUSSIAN -> "Да, проверить"
        else -> "Yes, Check"
    }

    val exitDismiss: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "Hayır, Çık"
        AppLanguage.ENGLISH -> "No, Exit"
        AppLanguage.SPANISH -> "No, salir"
        AppLanguage.GERMAN -> "Nein, beenden"
        AppLanguage.FRENCH -> "Non, quitter"
        AppLanguage.ITALIAN -> "No, esci"
        AppLanguage.PORTUGUESE -> "Não, sair"
        AppLanguage.HINDI -> "नहीं, बाहर निकलें"
        AppLanguage.CHINESE -> "否，退出"
        AppLanguage.RUSSIAN -> "Нет, выйти"
        else -> "No, Exit"
    }

    val dailyReward: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "GÜNLÜK ÖDÜL"
        AppLanguage.ENGLISH -> "DAILY REWARD"
        AppLanguage.SPANISH -> "RECOMPENSA DIARIA"
        AppLanguage.GERMAN -> "TÄGLICHE BELOHNUNG"
        AppLanguage.FRENCH -> "RÉCOMPENSE QUOTIDIENNE"
        AppLanguage.ITALIAN -> "RICOMPENSA GIORNALIERA"
        AppLanguage.PORTUGUESE -> "RECOMPENSA DIÁRIA"
        AppLanguage.HINDI -> "दैनिक इनाम"
        AppLanguage.CHINESE -> "每日奖励"
        AppLanguage.RUSSIAN -> "ЕЖЕДНЕВНАЯ НАГРАДА"
        else -> "DAILY REWARD"
    }

    val streak: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "Seri"
        AppLanguage.ENGLISH -> "STREAK"
        AppLanguage.SPANISH -> "RACHA"
        AppLanguage.GERMAN -> "STREAK"
        AppLanguage.FRENCH -> "SÉRIE"
        AppLanguage.ITALIAN -> "SERIE"
        AppLanguage.PORTUGUESE -> "SEQUÊNCIA"
        AppLanguage.HINDI -> "स्ट्रीक"
        AppLanguage.CHINESE -> "连胜"
        AppLanguage.RUSSIAN -> "СЕРИЯ"
        else -> "STREAK"
    }

    val dailyBonusDesc: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "Her gün gelerek daha fazla yıldız kazan!"
        AppLanguage.ENGLISH -> "Come back every day to collect more stars!"
        AppLanguage.SPANISH -> "¡Vuelve cada día para conseguir más estrellas!"
        AppLanguage.GERMAN -> "Komm jeden Tag wieder, um mehr Sterne zu sammeln!"
        AppLanguage.FRENCH -> "Revenez chaque jour pour collecter plus d'étoiles !"
        AppLanguage.ITALIAN -> "Torna ogni giorno per raccogliere più stelle!"
        AppLanguage.PORTUGUESE -> "Volte todos os dias para coletar mais estrelas!"
        AppLanguage.HINDI -> "और सितारे इकट्ठा करने के लिए हर दिन वापस आएं!"
        AppLanguage.CHINESE -> "每天回来收集更多星星！"
        AppLanguage.RUSSIAN -> "Возвращайтесь каждый день, чтобы собрать больше звезд!"
        else -> "Come back every day to collect more stars!"
    }

    val stars: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "Yıldız"
        AppLanguage.ENGLISH -> "STARS"
        AppLanguage.SPANISH -> "ESTRELLAS"
        AppLanguage.GERMAN -> "STERNE"
        AppLanguage.FRENCH -> "ÉTOILES"
        AppLanguage.ITALIAN -> "STELLE"
        AppLanguage.PORTUGUESE -> "ESTRELAS"
        AppLanguage.HINDI -> "सितारे"
        AppLanguage.CHINESE -> "星星"
        AppLanguage.RUSSIAN -> "ЗВЕЗДЫ"
        else -> "STARS"
    }

    val claim: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "AL"
        AppLanguage.ENGLISH -> "CLAIM"
        AppLanguage.SPANISH -> "RECLAMAR"
        AppLanguage.GERMAN -> "ABHOLEN"
        AppLanguage.FRENCH -> "RÉCUPÉRER"
        AppLanguage.ITALIAN -> "RICEVI"
        AppLanguage.PORTUGUESE -> "REIVINDICAR"
        AppLanguage.HINDI -> "दावा करें"
        AppLanguage.CHINESE -> "领取"
        AppLanguage.RUSSIAN -> "ПОЛУЧИТЬ"
        else -> "CLAIM"
    }

    val outOfLivesRefillAd: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "Reklamla Can Yenile"
        AppLanguage.ENGLISH -> "Watch Ad & Get 5 Lives"
        AppLanguage.SPANISH -> "Ver anuncio y obtener 5 vidas"
        AppLanguage.GERMAN -> "Anzeige ansehen & 5 Leben erhalten"
        AppLanguage.FRENCH -> "Regarder une pub et gagner 5 vies"
        AppLanguage.ITALIAN -> "Guarda l'annuncio e ottieni 5 vite"
        AppLanguage.PORTUGUESE -> "Ver anúncio e ganhar 5 vidas"
        AppLanguage.HINDI -> "विज्ञापन देखें और 5 लाइव्स प्राप्त करें"
        AppLanguage.CHINESE -> "观看广告并获得5条生命"
        AppLanguage.RUSSIAN -> "Посмотреть рекламу и получить 5 жизней"
        else -> "Watch Ad & Get 5 Lives"
    }

    val points: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "Puan"
        AppLanguage.ENGLISH -> "Points"
        AppLanguage.HINDI -> "\u0905\u0902\u0915"
        AppLanguage.CHINESE -> "\u70b9\u6570"
        AppLanguage.RUSSIAN -> "\u041e\u0447\u043a\u0438"
        else -> "Points"
    }

    val equip: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "KUŞAN"
        AppLanguage.ENGLISH -> "EQUIP"
        AppLanguage.SPANISH -> "EQUIPAR"
        AppLanguage.GERMAN -> "AUSRÜSTEN"
        AppLanguage.FRENCH -> "ÉQUIPER"
        AppLanguage.ITALIAN -> "EQUIPAGGIA"
        AppLanguage.PORTUGUESE -> "EQUIPAR"
        AppLanguage.HINDI -> "लैस करें"
        AppLanguage.CHINESE -> "装备"
        AppLanguage.RUSSIAN -> "СНАРЯДИТЬ"
        else -> "EQUIP"
    }

    val remove: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "ÇIKAR"
        AppLanguage.ENGLISH -> "REMOVE"
        AppLanguage.SPANISH -> "QUITAR"
        AppLanguage.GERMAN -> "ENTFERNEN"
        AppLanguage.FRENCH -> "RETIRER"
        AppLanguage.ITALIAN -> "RIMUOVI"
        AppLanguage.PORTUGUESE -> "REMOVER"
        AppLanguage.HINDI -> "हटाएं"
        AppLanguage.CHINESE -> "移除"
        AppLanguage.RUSSIAN -> "УДАЛИТЬ"
        else -> "REMOVE"
    }

    val privacyPolicy: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "Gizlilik Politikas\u0131"
        AppLanguage.ENGLISH -> "Privacy Policy"
        AppLanguage.HINDI -> "\u0917\u094b\u092a\u0928\u0940\u092f\u0924\u093e \u0928\u0940\u0924\u093f"
        AppLanguage.CHINESE -> "\u9690\u79c1\u653f\u7b56"
        AppLanguage.RUSSIAN -> "\u041f\u043e\u043b\u0438\u0442\u0438\u043a\u0430 \u043a\u043e\u043d\u0444\u0438\u0434\u0435\u043d\u0446\u0438\u0430\u043b\u044c\u043d\u043e\u0441\u0442\u0438"
        else -> "Privacy Policy"
    }

    val totalPlayTime: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "Toplam Oyun Süresi"
        AppLanguage.ENGLISH -> "Total Play Time"
        AppLanguage.SPANISH -> "Tiempo total de juego"
        AppLanguage.GERMAN -> "Gesamtspielzeit"
        AppLanguage.FRENCH -> "Temps de jeu total"
        AppLanguage.ITALIAN -> "Tempo di gioco totale"
        AppLanguage.PORTUGUESE -> "Tempo total de jogo"
        AppLanguage.HINDI -> "कुल खेलने का समय"
        AppLanguage.CHINESE -> "总游戏时间"
        AppLanguage.RUSSIAN -> "Общее время игры"
        else -> "Total Play Time"
    }

    val highScores: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "Yüksek Skorlar"
        AppLanguage.ENGLISH -> "HIGH SCORES"
        AppLanguage.SPANISH -> "PUNTUACIONES ALTAS"
        AppLanguage.GERMAN -> "BESTENLISTE"
        AppLanguage.FRENCH -> "MEILLEURS SCORES"
        AppLanguage.ITALIAN -> "MIGLIORI PUNTEGGI"
        AppLanguage.PORTUGUESE -> "RECORDES"
        AppLanguage.HINDI -> "उच्च स्कोर"
        AppLanguage.CHINESE -> "最高分"
        AppLanguage.RUSSIAN -> "РЕКОРДЫ"
        else -> "HIGH SCORES"
    }

    val noScoresYet: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "Henüz skor yok"
        AppLanguage.ENGLISH -> "No scores found yet!"
        AppLanguage.SPANISH -> "¡No se han encontrado puntuaciones!"
        AppLanguage.GERMAN -> "Noch keine Ergebnisse!"
        AppLanguage.FRENCH -> "Aucun score trouvé !"
        AppLanguage.ITALIAN -> "Nessun punteggio trovato!"
        AppLanguage.PORTUGUESE -> "Nenhum recorde encontrado!"
        AppLanguage.HINDI -> "अभी तक कोई स्कोर नहीं मिला!"
        AppLanguage.CHINESE -> "暂无得分记录！"
        AppLanguage.RUSSIAN -> "Счета еще не найдены!"
        else -> "No scores found yet!"
    }

    val score: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "Puan"
        AppLanguage.ENGLISH -> "SCORE"
        AppLanguage.SPANISH -> "PUNTOS"
        AppLanguage.GERMAN -> "PUNKTE"
        AppLanguage.FRENCH -> "SCORE"
        AppLanguage.ITALIAN -> "PUNTEGGIO"
        AppLanguage.PORTUGUESE -> "PONTOS"
        AppLanguage.HINDI -> "स्कोर"
        AppLanguage.CHINESE -> "分数"
        AppLanguage.RUSSIAN -> "СЧЕТ"
        else -> "SCORE"
    }

    val level: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "Seviye"
        AppLanguage.ENGLISH -> "Level"
        AppLanguage.HINDI -> "\u0938\u094d\u0924\u0930"
        AppLanguage.CHINESE -> "\u5173\u5361"
        AppLanguage.RUSSIAN -> "\u0423\u0440\u043e\u0432\u0435\u043d\u044c"
        else -> "Level"
    }

    val date: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "Tarih"
        AppLanguage.ENGLISH -> "Date"
        AppLanguage.HINDI -> "\u0924\u093f\u0925\u093f"
        AppLanguage.CHINESE -> "\u65e5\u671f"
        AppLanguage.RUSSIAN -> "\u0414\u0430\u0442\u0430"
        else -> "Date"
    }

    val clearAll: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "Hepsini Temizle"
        AppLanguage.ENGLISH -> "Clear All"
        AppLanguage.SPANISH -> "Borrar todo"
        AppLanguage.GERMAN -> "Alles löschen"
        AppLanguage.FRENCH -> "Tout effacer"
        AppLanguage.ITALIAN -> "Cancella tutto"
        AppLanguage.PORTUGUESE -> "Limpar tudo"
        AppLanguage.HINDI -> "सभी साफ करें"
        AppLanguage.CHINESE -> "清除全部"
        AppLanguage.RUSSIAN -> "Очистить все"
        else -> "Clear All"
    }

    val globalLeaderboard: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "Dünya Sıralaması"
        AppLanguage.ENGLISH -> "GLOBAL LEADERBOARD"
        AppLanguage.SPANISH -> "CLASIFICACIÓN GLOBAL"
        AppLanguage.GERMAN -> "GLOBALE RANGLISTE"
        AppLanguage.FRENCH -> "CLASSEMENT MONDIAL"
        AppLanguage.ITALIAN -> "CLASSIFICA GLOBALE"
        AppLanguage.PORTUGUESE -> "CLASSIFICAÇÃO GLOBAL"
        AppLanguage.HINDI -> "ग्लोबल लीडरबोर्ड"
        AppLanguage.CHINESE -> "全球排行榜"
        AppLanguage.RUSSIAN -> "ГЛОБАЛЬНАЯ ТАБЛИЦА ЛИДЕРОВ"
        else -> "GLOBAL LEADERBOARD"
    }

    val yourRank: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "S\u0131ralaman\u0131z"
        AppLanguage.ENGLISH -> "Your Rank"
        AppLanguage.HINDI -> "\u0906\u092a\u0915\u0940 \u0930\u0948\u0902\u0915"
        AppLanguage.CHINESE -> "\u4f60\u7684\u6392\u540d"
        AppLanguage.RUSSIAN -> "\u0412\u0430\u0448 \u0440\u0435\u0439\u0442\u0438\u043d\u0433"
        else -> "Your Rank"
    }

    val topScore: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "En Yüksek Puan"
        AppLanguage.ENGLISH -> "TOP SCORE"
        AppLanguage.SPANISH -> "MEJOR PUNTUACIÓN"
        AppLanguage.GERMAN -> "BESTERGEBNIS"
        AppLanguage.FRENCH -> "MEILLEUR SCORE"
        AppLanguage.ITALIAN -> "MIGLIOR PUNTEGGIO"
        AppLanguage.PORTUGUESE -> "MELHOR PONTUAÇÃO"
        AppLanguage.HINDI -> "शीर्ष स्कोर"
        AppLanguage.CHINESE -> "最高分"
        AppLanguage.RUSSIAN -> "ЛУЧШИЙ СЧЕТ"
        else -> "TOP SCORE"
    }

    val updateTitle: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "GÜNCELLEME GEREKİYOR"
        AppLanguage.ENGLISH -> "UPDATE REQUIRED"
        AppLanguage.SPANISH -> "ACTUALIZACIÓN REQUERIDA"
        AppLanguage.GERMAN -> "UPDATE ERFORDERLICH"
        AppLanguage.FRENCH -> "MISE À JOUR REQUISE"
        AppLanguage.ITALIAN -> "AGGIORNAMENTO RICHIESTO"
        AppLanguage.PORTUGUESE -> "ATUALIZAÇÃO NECESSÁRIA"
        AppLanguage.HINDI -> "अद्यतन आवश्यक"
        AppLanguage.CHINESE -> "需要更新"
        AppLanguage.RUSSIAN -> "ТРЕБУЕТСЯ ОБНОВЛЕНИЕ"
        else -> "UPDATE REQUIRED"
    }

    val updateMessage: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "Matematik Laboratuvarı Güncellendi! ⚡ Bilim insanları artık daha güçlü. Devam etmek için son sürümü indirin!"
        AppLanguage.ENGLISH -> "Math Lab Updated! ⚡ Scientists are now more powerful. Download the latest version to keep competing!"
        AppLanguage.SPANISH -> "¡Laboratorio de matemáticas actualizado! ⚡ Los científicos son ahora más poderosos. ¡Descarga la última versión!"
        AppLanguage.GERMAN -> "Mathe-Labor aktualisiert! ⚡ Wissenschaftler sind jetzt mächtiger. Lade die neueste Version herunter!"
        AppLanguage.FRENCH -> "Laboratoire de maths mis à jour ! ⚡ Les scientifiques sont plus puissants. Téléchargez la dernière version !"
        AppLanguage.ITALIAN -> "Laboratorio di matematica aggiornato! ⚡ Gli scienziati sono più potenti. Scarica l'ultima versione!"
        AppLanguage.PORTUGUESE -> "Laboratório de Matemática Atualizado! ⚡ Os cientistas estão mais poderosos. Baixe a versão mais recente!"
        AppLanguage.HINDI -> "मैथ लैब अपडेट हो गया! ⚡ वैज्ञानिक अब अधिक शक्तिशाली हैं। प्रतिस्पर्धा जारी रखने के लिए नवीनतम संस्करण डाउनलोड करें!"
        AppLanguage.CHINESE -> "数学实验室已更新！⚡ 科学家们现在更加强大。下载最新版本，继续竞争！"
        AppLanguage.RUSSIAN -> "Math Lab обновлен! ⚡ Ученые теперь еще сильнее. Скачайте последнюю версию, чтобы продолжать соревноваться!"
        else -> "Math Lab Updated! ⚡ Scientists are now more powerful. Download the latest version to keep competing!"
    }

    val updateButton: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "ŞİMDİ GÜNCELLE"
        AppLanguage.ENGLISH -> "UPDATE NOW"
        AppLanguage.SPANISH -> "ACTUALIZAR AHORA"
        AppLanguage.GERMAN -> "JETZT AKTUALISIEREN"
        AppLanguage.FRENCH -> "METTRE À JOUR"
        AppLanguage.ITALIAN -> "AGGIORNA ORA"
        AppLanguage.PORTUGUESE -> "ATUALIZAR AGORA"
        AppLanguage.HINDI -> "अभी अपडेट करें"
        AppLanguage.CHINESE -> "立即更新"
        AppLanguage.RUSSIAN -> "ОБНОВИТЬ СЕЙЧАС"
        else -> "UPDATE NOW"
    }

    val supportTitle: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "Destek ve Yardım"
        AppLanguage.ENGLISH -> "SUPPORT & HELP"
        AppLanguage.SPANISH -> "SOPORTE Y AYUDA"
        AppLanguage.GERMAN -> "SUPPORT & HILFE"
        AppLanguage.FRENCH -> "SUPPORT ET AIDE"
        AppLanguage.ITALIAN -> "SUPPORTO E AIUTO"
        AppLanguage.PORTUGUESE -> "SUPORTE E AJUDA"
        AppLanguage.HINDI -> "सहायता और मदद"
        AppLanguage.CHINESE -> "支持与帮助"
        AppLanguage.RUSSIAN -> "ПОДДЕРЖКА И ПОМОЩЬ"
        else -> "SUPPORT & HELP"
    }

    val supportDesc: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "Herhangi bir sorun veya öneriniz için bize ulaşın."
        AppLanguage.ENGLISH -> "Contact us, we would be happy to help you."
        AppLanguage.SPANISH -> "Contáctanos, estaremos encantados de ayudarte."
        AppLanguage.GERMAN -> "Kontaktieren Sie uns, wir helfen Ihnen gerne weiter."
        AppLanguage.FRENCH -> "Contactez-nous, nous serions ravis de vous aider."
        AppLanguage.ITALIAN -> "Contattaci, saremo felici di aiutarti."
        AppLanguage.PORTUGUESE -> "Contate-nos, teremos prazer em ajudá-lo."
        AppLanguage.HINDI -> "हमसे संपर्क करें, हमें आपकी मदद करने में खुशी होगी।"
        AppLanguage.CHINESE -> "联系我们，我们将很乐意为您提供帮助。"
        AppLanguage.RUSSIAN -> "Свяжитесь с нами, мы будем рады вам помочь."
        else -> "Contact us, we would be happy to help you."
    }

    val whatsappMessage: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "WhatsApp ile Bize Yazın"
        AppLanguage.ENGLISH -> "Chat on WhatsApp"
        AppLanguage.SPANISH -> "Chat en WhatsApp"
        AppLanguage.GERMAN -> "Auf WhatsApp chatten"
        AppLanguage.FRENCH -> "Discuter sur WhatsApp"
        AppLanguage.ITALIAN -> "Chatta su WhatsApp"
        AppLanguage.PORTUGUESE -> "Conversar no WhatsApp"
        AppLanguage.HINDI -> "WhatsApp पर चैट करें"
        AppLanguage.CHINESE -> "在 WhatsApp 上聊天"
        AppLanguage.RUSSIAN -> "Чат в WhatsApp"
        else -> "Chat on WhatsApp"
    }

    val emailMessage: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "E-posta ile Bize Ulaşın"
        AppLanguage.ENGLISH -> "Send Email"
        AppLanguage.SPANISH -> "Enviar correo electrónico"
        AppLanguage.GERMAN -> "E-Mail senden"
        AppLanguage.FRENCH -> "Envoyer un e-mail"
        AppLanguage.ITALIAN -> "Invia e-mail"
        AppLanguage.PORTUGUESE -> "Enviar e-mail"
        AppLanguage.HINDI -> "ईमेल भेजें"
        AppLanguage.CHINESE -> "发送电子邮件"
        AppLanguage.RUSSIAN -> "Отправить email"
        else -> "Send Email"
    }

    val close: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "Kapat"
        AppLanguage.ENGLISH -> "CLOSE"
        AppLanguage.SPANISH -> "CERRAR"
        AppLanguage.GERMAN -> "SCHLIESSEN"
        AppLanguage.FRENCH -> "FERMER"
        AppLanguage.ITALIAN -> "CHIUDI"
        AppLanguage.PORTUGUESE -> "FECHAR"
        AppLanguage.HINDI -> "बंद करें"
        AppLanguage.CHINESE -> "关闭"
        AppLanguage.RUSSIAN -> "ЗАКРЫТЬ"
        else -> "CLOSE"
    }

    val cardUnlocked: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "YENİ KART AÇILDI!"
        AppLanguage.ENGLISH -> "NEW CARD UNLOCKED!"
        AppLanguage.SPANISH -> "¡NUEVA CARTA DESBLOQUEADA!"
        AppLanguage.GERMAN -> "NEUE KARTE FREIGESCHALTET!"
        AppLanguage.FRENCH -> "NOUVELLE CARTE DÉBLOQUÉE !"
        AppLanguage.ITALIAN -> "NUOVA CARTA SBLOCCATA!"
        AppLanguage.PORTUGUESE -> "NOVA CARTA DESBLOQUEADA!"
        AppLanguage.HINDI -> "नया कार्ड अनलॉक किया गया!"
        AppLanguage.CHINESE -> "解锁新卡片！"
        AppLanguage.RUSSIAN -> "ОТКРЫТА НОВАЯ КАРТА!"
        else -> "NEW CARD UNLOCKED!"
    }

    val statCharges: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "Kullanım Hakkı"
        AppLanguage.ENGLISH -> "USAGE RIGHTS"
        AppLanguage.SPANISH -> "DERECHOS DE USO"
        AppLanguage.GERMAN -> "NUTZUNGSRECHTE"
        AppLanguage.FRENCH -> "DROITS D'UTILISATION"
        AppLanguage.ITALIAN -> "DIRITTI D'USO"
        AppLanguage.PORTUGUESE -> "DIREITOS DE USO"
        AppLanguage.HINDI -> "उपयोग अधिकार"
        AppLanguage.CHINESE -> "使用权"
        AppLanguage.RUSSIAN -> "ПРАВА ИСПОЛЬЗОВАНИЯ"
        else -> "USAGE RIGHTS"
    }

    val hintAdd: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "Topla"
        AppLanguage.ENGLISH -> "Addition"
        AppLanguage.SPANISH -> "Suma"
        AppLanguage.GERMAN -> "Addition"
        AppLanguage.FRENCH -> "Addition"
        AppLanguage.ITALIAN -> "Addizione"
        AppLanguage.PORTUGUESE -> "Adição"
        AppLanguage.HINDI -> "जोड़"
        AppLanguage.CHINESE -> "加法"
        AppLanguage.RUSSIAN -> "Сложение"
        else -> "Addition"
    }

    val hintSubtract: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "Çıkar"
        AppLanguage.ENGLISH -> "Subtraction"
        AppLanguage.SPANISH -> "Resta"
        AppLanguage.GERMAN -> "Subtraktion"
        AppLanguage.FRENCH -> "Soustraction"
        AppLanguage.ITALIAN -> "Sottrazione"
        AppLanguage.PORTUGUESE -> "Subtração"
        AppLanguage.HINDI -> "घटाव"
        AppLanguage.CHINESE -> "减法"
        AppLanguage.RUSSIAN -> "Вычитание"
        else -> "Subtraction"
    }

    val hintMultiply: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "Çarp"
        AppLanguage.ENGLISH -> "Multiplication"
        AppLanguage.SPANISH -> "Multiplicación"
        AppLanguage.GERMAN -> "Multiplikation"
        AppLanguage.FRENCH -> "Multiplication"
        AppLanguage.ITALIAN -> "Moltiplicazione"
        AppLanguage.PORTUGUESE -> "Multiplicação"
        AppLanguage.HINDI -> "गुणा"
        AppLanguage.CHINESE -> "乘法"
        AppLanguage.RUSSIAN -> "Умножение"
        else -> "Multiplication"
    }

    val hintDivide: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "Böl"
        AppLanguage.ENGLISH -> "Division"
        AppLanguage.SPANISH -> "División"
        AppLanguage.GERMAN -> "Division"
        AppLanguage.FRENCH -> "Division"
        AppLanguage.ITALIAN -> "Divisione"
        AppLanguage.PORTUGUESE -> "Divisão"
        AppLanguage.HINDI -> "भाग"
        AppLanguage.CHINESE -> "除法"
        AppLanguage.RUSSIAN -> "Деление"
        else -> "Division"
    }

    val finalScore: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "TOPLAM PUAN"
        AppLanguage.ENGLISH -> "FINAL SCORE"
        AppLanguage.SPANISH -> "PUNTUACIÓN FINAL"
        AppLanguage.GERMAN -> "ENDERGEBNIS"
        AppLanguage.FRENCH -> "SCORE FINAL"
        AppLanguage.ITALIAN -> "PUNTEGGIO FINALE"
        AppLanguage.PORTUGUESE -> "PONTUAÇÃO FINAL"
        AppLanguage.HINDI -> "अंतिम स्कोर"
        AppLanguage.CHINESE -> "最终得分"
        AppLanguage.RUSSIAN -> "ИТОГОВЫЙ СЧЕТ"
        else -> "FINAL SCORE"
    }

    val newRecord: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "YENİ REKOR!"
        AppLanguage.ENGLISH -> "NEW RECORD!"
        AppLanguage.SPANISH -> "¡NUEVO RÉCORD!"
        AppLanguage.GERMAN -> "NEUER REKORD!"
        AppLanguage.FRENCH -> "NOUVEAU RECORD !"
        AppLanguage.ITALIAN -> "NUOVO RECORD!"
        AppLanguage.PORTUGUESE -> "NOVO RECORDE!"
        AppLanguage.HINDI -> "नया रिकॉर्ड!"
        AppLanguage.CHINESE -> "新记录！"
        AppLanguage.RUSSIAN -> "НОВЫЙ РЕКОРД!"
        else -> "NEW RECORD!"
    }

    val saveMeTitle: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "İKİNCİ ŞANS"
        AppLanguage.ENGLISH -> "SECOND CHANCE"
        AppLanguage.SPANISH -> "SEGUNDA OPORTUNIDAD"
        AppLanguage.GERMAN -> "ZWEITE CHANCE"
        AppLanguage.FRENCH -> "DEUXIÈME CHANCE"
        AppLanguage.ITALIAN -> "SECONDA POSSIBILITÀ"
        AppLanguage.PORTUGUESE -> "SEGUNDA CHANCE"
        AppLanguage.HINDI -> "दूसरा मौका"
        AppLanguage.CHINESE -> "第二次机会"
        AppLanguage.RUSSIAN -> "ВТОРОЙ ШАНС"
        else -> "SECOND CHANCE"
    }

    val enterName: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "İsim Girin"
        AppLanguage.ENGLISH -> "Enter Name"
        AppLanguage.SPANISH -> "Introducir nombre"
        AppLanguage.GERMAN -> "Name eingeben"
        AppLanguage.FRENCH -> "Entrer le nom"
        AppLanguage.ITALIAN -> "Inserisci nome"
        AppLanguage.PORTUGUESE -> "Inserir nome"
        AppLanguage.HINDI -> "नाम दर्ज करें"
        AppLanguage.CHINESE -> "输入姓名"
        AppLanguage.RUSSIAN -> "Введите имя"
        else -> "Enter Name"
    }

    val outOfLivesTitle: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "CANLAR BİTTİ!"
        AppLanguage.ENGLISH -> "OUT OF LIVES!"
        AppLanguage.SPANISH -> "¡SIN VIDAS!"
        AppLanguage.GERMAN -> "KEINE LEBEN MEHR!"
        AppLanguage.FRENCH -> "PLUS DE VIES !"
        AppLanguage.ITALIAN -> "VITE ESAURITE!"
        AppLanguage.PORTUGUESE -> "SEM VIDAS!"
        AppLanguage.HINDI -> "लाइव्स खत्म!"
        AppLanguage.CHINESE -> "生命耗尽！"
        AppLanguage.RUSSIAN -> "ЖИЗНИ ЗАКОНЧИЛИСЬ!"
        else -> "OUT OF LIVES!"
    }

    val outOfLivesMessage: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "Dolmasını bekle veya reklam izleyerek tüm canlarını hemen doldur!"
        AppLanguage.ENGLISH -> "Wait to refill or watch an ad to refill all lives instantly!"
        AppLanguage.SPANISH -> "¡Espera o mira un anuncio para rellenar todas las vidas al instante!"
        AppLanguage.GERMAN -> "Warten Sie oder sehen Sie sich eine Anzeige an, um alle Leben sofort aufzufüllen!"
        AppLanguage.FRENCH -> "Attendez ou regardez une pub pour recharger toutes vos vies instantanément !"
        AppLanguage.ITALIAN -> "Aspetta o guarda un annuncio per ricaricare istantaneamente tutte le vite!"
        AppLanguage.PORTUGUESE -> "Espere ou veja um anúncio para recarregar todas as vidas instantaneamente!"
        AppLanguage.HINDI -> "भरने की प्रतीक्षा करें या तुरंत सभी लाइव्स भरने के लिए विज्ञापन देखें!"
        AppLanguage.CHINESE -> "等待恢复或观看广告立即恢复所有生命！"
        AppLanguage.RUSSIAN -> "Подождите восстановления или посмотрите рекламу, чтобы восполнить все жизни мгновенно!"
        else -> "Wait to refill or watch an ad to refill all lives instantly!"
    }

    val continue_: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "DEVAM ET"
        AppLanguage.ENGLISH -> "CONTINUE"
        AppLanguage.SPANISH -> "CONTINUAR"
        AppLanguage.GERMAN -> "WEITER"
        AppLanguage.FRENCH -> "CONTINUER"
        AppLanguage.ITALIAN -> "CONTINUA"
        AppLanguage.PORTUGUESE -> "CONTINUAR"
        AppLanguage.HINDI -> "जारी रखें"
        AppLanguage.CHINESE -> "继续"
        AppLanguage.RUSSIAN -> "ПРОДОЛЖИТЬ"
        else -> "CONTINUE"
    }

    val backToMenu: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "MENÜYE DÖN"
        AppLanguage.ENGLISH -> "BACK TO MENU"
        AppLanguage.SPANISH -> "VOLVER AL MENÚ"
        AppLanguage.GERMAN -> "ZURÜCK ZUM MENÜ"
        AppLanguage.FRENCH -> "RETOUR AU MENU"
        AppLanguage.ITALIAN -> "TORNA AL MENU"
        AppLanguage.PORTUGUESE -> "VOLTAR AO MENU"
        AppLanguage.HINDI -> "मेनू पर वापस जाएं"
        AppLanguage.CHINESE -> "返回主菜单"
        AppLanguage.RUSSIAN -> "В МЕНЮ"
        else -> "BACK TO MENU"
    }

    val retry: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "TEKRAR DENE"
        AppLanguage.ENGLISH -> "RETRY"
        AppLanguage.SPANISH -> "REINTENTAR"
        AppLanguage.GERMAN -> "WIEDERHOLEN"
        AppLanguage.FRENCH -> "RÉESSAYER"
        AppLanguage.ITALIAN -> "RIPROVA"
        AppLanguage.PORTUGUESE -> "TENTAR NOVAMENTE"
        AppLanguage.HINDI -> "पुनः प्रयास करें"
        AppLanguage.CHINESE -> "重试"
        AppLanguage.RUSSIAN -> "ПОВТОРИТЬ"
        else -> "RETRY"
    }

    val challengeAlreadyPlayed: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "Bugünkü tüm meydan okuma haklarını kullandın!"
        AppLanguage.ENGLISH -> "You've used all challenge attempts for today!"
        AppLanguage.SPANISH -> "¡Has agotado todos los intentos de desafío de hoy!"
        AppLanguage.GERMAN -> "Du hast alle heutigen Herausforderungsversuche aufgebraucht!"
        AppLanguage.FRENCH -> "Vous avez épuisé toutes vos tentatives de défi pour aujourd'hui !"
        AppLanguage.ITALIAN -> "Hai esaurito tutti i tentativi di sfida per oggi!"
        AppLanguage.PORTUGUESE -> "Você usó todas as tentativas de desafio para hoje!"
        AppLanguage.HINDI -> "आपने आज के लिए सभी चुनौती प्रयास उपयोग कर लिए हैं!"
        AppLanguage.CHINESE -> "您已用完今天的全部挑战尝试次数！"
        AppLanguage.RUSSIAN -> "Вы использовали все попытки испытаний на сегодня!"
        else -> "You've used all challenge attempts for today!"
    }

    val turkish: String get() = "T\u00fcrk\u00e7e"

    val english: String get() = "English"

    val spanish: String get() = "Espa\u00f1ol"

    val german: String get() = "Deutsch"

    val french: String get() = "Fran\u00e7ais"

    val italian: String get() = "Italiano"

    val portuguese: String get() = "Portugu\u00eas"

    val hindi: String get() = "\u0939\u093f\u0928\u094d\u0926\u0940"

    val chinese: String get() = "\u7b80\u4f53\u4e2d\u6587"

    val russian: String get() = "\u0420\u0443\u0441\u0441\u043a\u0438\u0439"


    fun getSelectLanguage(lang: AppLanguage? = null): String = when (lang ?: currentLanguage) {
        AppLanguage.TURKISH -> "Dil Seçimi"
        AppLanguage.ENGLISH -> "Select Language"
        AppLanguage.HINDI -> "भाषा चुनें"
        AppLanguage.CHINESE -> "选择语言"
        AppLanguage.RUSSIAN -> "Выберите язык"
        else -> "Select Language"
    }

    fun getBannedWordError(lang: AppLanguage? = null): String = when (lang ?: currentLanguage) {
        AppLanguage.TURKISH -> "Kullanıcı adı uygunsuz kelimeler içeriyor!"
        AppLanguage.ENGLISH -> "Username contains inappropriate words!"
        AppLanguage.SPANISH -> "¡El nombre de usuario contiene palabras inapropiadas!"
        AppLanguage.GERMAN -> "Benutzername enthält unangemessene Wörter!"
        AppLanguage.FRENCH -> "Le nom d'utilisateur contient des mots inappropriés !"
        AppLanguage.ITALIAN -> "Il nome utente contiene parole inappropriate!"
        AppLanguage.PORTUGUESE -> "O nome de usuário contém palavras inadequadas!"
        AppLanguage.HINDI -> "उपयोगकर्ता नाम में अनुचित शब्द हैं!"
        AppLanguage.CHINESE -> "用户名包含不当词汇！"
        AppLanguage.RUSSIAN -> "Имя пользователя содержит недопустимые слова!"
        else -> "Username contains inappropriate words!"
    }

    fun getInvalidNameError(lang: AppLanguage? = null): String = when (lang ?: currentLanguage) {
        AppLanguage.TURKISH -> "Geçersiz kullanıcı adı!"
        AppLanguage.ENGLISH -> "Invalid username! (3-15 chars, special chars not allowed)"
        AppLanguage.SPANISH -> "¡Nombre de usuario no válido! (3-15 caracteres, no se permiten caracteres especiales)"
        AppLanguage.GERMAN -> "Ungültiger Benutzername! (3-15 Zeichen, Sonderzeichen nicht erlaubt)"
        AppLanguage.FRENCH -> "Nom d'utilisateur invalide ! (3-15 caractères, caractères spéciaux non autorisés)"
        AppLanguage.ITALIAN -> "Nome utente non valido! (3-15 caratteri, caratteri speciali non consentiti)"
        AppLanguage.PORTUGUESE -> "Nome de usuário inválido! (3-15 caracteres, caracteres especiais não permitidos)"
        AppLanguage.HINDI -> "अमान्य उपयोगकर्ता नाम! (3-15 वर्ण, विशेष वर्णों की अनुमति नहीं है)"
        AppLanguage.CHINESE -> "无效的用户名！（3-15个字符，不允许使用特殊字符）"
        AppLanguage.RUSSIAN -> "Недействительное имя пользователя! (3-15 символов, специальные символы не допускаются)"
        else -> "Invalid username!"
    }

    fun getNameRequired(lang: AppLanguage? = null): String = when (lang ?: currentLanguage) {
        AppLanguage.TURKISH -> "İsim gereklidir!"
        AppLanguage.ENGLISH -> "You must enter a name to continue!"
        AppLanguage.SPANISH -> "¡Debes introducir un nombre para continuar!"
        AppLanguage.GERMAN -> "Sie müssen einen Namen eingeben, um fortzufahren!"
        AppLanguage.FRENCH -> "Vous devez entrer un nom pour continuer !"
        AppLanguage.ITALIAN -> "Devi inserire un nome per continuare!"
        AppLanguage.PORTUGUESE -> "Você deve inserir um nome para continuar!"
        AppLanguage.HINDI -> "जारी रखने के लिए आपको एक नाम दर्ज करना होगा!"
        AppLanguage.CHINESE -> "您必须输入姓名才能继续！"
        AppLanguage.RUSSIAN -> "Вы должны ввести имя, чтобы продолжить!"
        else -> "Name is required!"
    }

    val challenge: String get() = when(currentLanguage) {
        AppLanguage.TURKISH -> "MEYDAN OKUMA"
        AppLanguage.ENGLISH -> "CHALLENGE"
        AppLanguage.SPANISH -> "DESAFÍO"
        AppLanguage.GERMAN -> "HERAUSFORDERUNG"
        AppLanguage.FRENCH -> "DÉFI"
        AppLanguage.ITALIAN -> "SFIDA"
        AppLanguage.PORTUGUESE -> "DESAFIO"
        AppLanguage.HINDI -> "चुनौती"
        AppLanguage.CHINESE -> "挑战"
        AppLanguage.RUSSIAN -> "ИСПЫТАНИЕ"
        else -> "CHALLENGE"
    }
    val checkpointComplete: String get() = when(currentLanguage) {
        AppLanguage.TURKISH -> "Seviye Tamamlandı!"
        AppLanguage.ENGLISH -> "Level Complete!"
        AppLanguage.SPANISH -> "¡Nivel completado!"
        AppLanguage.GERMAN -> "Level abgeschlossen!"
        AppLanguage.FRENCH -> "Niveau terminé !"
        AppLanguage.ITALIAN -> "Livello completato!"
        AppLanguage.PORTUGUESE -> "Nível concluído!"
        AppLanguage.HINDI -> "स्तर पूरा हुआ!"
        AppLanguage.CHINESE -> "关卡完成！"
        AppLanguage.RUSSIAN -> "Уровень пройден!"
        else -> "Level Complete!"
    }
    val gameOver: String get() = when(currentLanguage) {
        AppLanguage.TURKISH -> "Oyun Bitti!"
        AppLanguage.ENGLISH -> "Game Over!"
        AppLanguage.SPANISH -> "¡Fin del juego!"
        AppLanguage.GERMAN -> "Spiel vorbei!"
        AppLanguage.FRENCH -> "Fin de partie !"
        AppLanguage.ITALIAN -> "Game Over!"
        AppLanguage.PORTUGUESE -> "Fim de jogo!"
        AppLanguage.HINDI -> "खेल खत्म!"
        AppLanguage.CHINESE -> "游戏结束！"
        AppLanguage.RUSSIAN -> "Игра окончена!"
        else -> "Game Over!"
    }
    val watchAdToPlayAgain: String get() = when(currentLanguage) {
        AppLanguage.TURKISH -> "Tekrar oynamak için reklam izle"
        AppLanguage.ENGLISH -> "Watch ad to play again"
        AppLanguage.SPANISH -> "Ver anuncio para jugar de nuevo"
        AppLanguage.GERMAN -> "Anzeige ansehen, um erneut zu spielen"
        AppLanguage.FRENCH -> "Regarder une pub pour rejouer"
        AppLanguage.ITALIAN -> "Guarda l'annuncio per giocare di nuovo"
        AppLanguage.PORTUGUESE -> "Ver anúncio para jogar novamente"
        AppLanguage.HINDI -> "फिर से खेलने के लिए विज्ञापन देखें"
        AppLanguage.CHINESE -> "观看广告再玩一次"
        AppLanguage.RUSSIAN -> "Смотреть рекламу, чтобы сыграть еще"
        else -> "Watch ad to play again"
    }
    val shareScore: String get() = when(currentLanguage) {
        AppLanguage.TURKISH -> "Puanı Paylaş"
        AppLanguage.ENGLISH -> "Share Score"
        AppLanguage.SPANISH -> "Compartir puntuación"
        AppLanguage.GERMAN -> "Punktestand teilen"
        AppLanguage.FRENCH -> "Partager le score"
        AppLanguage.ITALIAN -> "Condividi il punteggio"
        AppLanguage.PORTUGUESE -> "Compartilhar pontuação"
        AppLanguage.HINDI -> "स्कोर साझा करें"
        AppLanguage.CHINESE -> "分享得分"
        AppLanguage.RUSSIAN -> "Поделиться счетом"
        else -> "Share Score"
    }

    val randomExitVoicePrompt: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "Ayrılmadan önce diğer oyunlarımızı da denemek ister misiniz?"
        AppLanguage.ENGLISH -> "Would you like to try our other games before you go?"
        AppLanguage.SPANISH -> "¿Te gustaría probar nuestros otros juegos antes de irte?"
        AppLanguage.GERMAN -> "Möchten Sie unsere anderen Spiele ausprobieren, bevor Sie gehen?"
        AppLanguage.FRENCH -> "Souhaitez-vous essayer nos autres jeux avant de partir ?"
        AppLanguage.ITALIAN -> "Ti piacerebbe provare i nostri altri giochi prima di uscire?"
        AppLanguage.PORTUGUESE -> "Gostaria de experimentar nossos outros jogos antes de sair?"
        AppLanguage.HINDI -> "जाने से पहले क्या आप हमारे अन्य खेलों को आजमाना चाहेंगे?"
        AppLanguage.CHINESE -> "您想在离开前尝试一下我们的其他游戏吗？"
        AppLanguage.RUSSIAN -> "Не хотели бы вы попробовать наши другие игры перед уходом?"
        else -> "Would you like to try our other games before you go?"
    }

    fun getShareMessage(score: Int, checkpoint: Int): String = when(currentLanguage) {
        AppLanguage.TURKISH -> "BlitzMath'te $score puan yaptım ve $checkpoint. seviyeyi geçtim! 🧠⚡ Haydi sen de dene!"
        AppLanguage.ENGLISH -> "I scored $score points and reached level $checkpoint in BlitzMath! 🧠⚡ Come and try to beat me!"
        AppLanguage.SPANISH -> "¡Logré $score puntos y llegué al nivel $checkpoint en BlitzMath! 🧠⚡ ¡Ven e intenta superarme!"
        AppLanguage.GERMAN -> "Ich habe $score Punkte erreicht und Level $checkpoint in BlitzMath geschafft! 🧠⚡ Versuch, mich zu schlagen!"
        AppLanguage.FRENCH -> "J'ai marqué $score points et atteint le niveau $checkpoint sur BlitzMath ! 🧠⚡ Venez essayer de me battre !"
        AppLanguage.ITALIAN -> "Ho totalizzato $score punti e raggiunto il livello $checkpoint su BlitzMath! 🧠⚡ Vieni a provare a battermi!"
        AppLanguage.PORTUGUESE -> "Marquei $score pontos e cheguei ao nível $checkpoint no BlitzMath! 🧠⚡ Venha tentar me superar!"
        AppLanguage.HINDI -> "मैंने BlitzMath पर $score अंक बनाए और $checkpoint स्तर तक पहुँचा! 🧠⚡ आओ और मुझे हराने की कोशिश करो!"
        AppLanguage.CHINESE -> "我在 BlitzMath 中获得了 $score 分并达到了第 $checkpoint 关！🧠⚡ 快来挑战我吧！"
        AppLanguage.RUSSIAN -> "Я набрал $score очков и достиг уровня $checkpoint в BlitzMath! 🧠⚡ Попробуй побить мой рекорд!"
        else -> "I scored $score points and reached level $checkpoint in BlitzMath! 🧠⚡ Come and try to beat me!"
    }

    fun getRechargeAdsInfo(count: Int): String = when(currentLanguage) {
        AppLanguage.TURKISH -> "Reklamla Yenile ($count)"
        AppLanguage.ENGLISH -> "Recharge with Ads ($count)"
        AppLanguage.SPANISH -> "Recargar con anuncios ($count)"
        AppLanguage.GERMAN -> "Mit Anzeigen aufladen ($count)"
        AppLanguage.FRENCH -> "Recharger avec des pubs ($count)"
        AppLanguage.ITALIAN -> "Ricarica con annunci ($count)"
        AppLanguage.PORTUGUESE -> "Recarregar com anúncios ($count)"
        AppLanguage.HINDI -> "विज्ञापनों के साथ रिचार्ज करें ($count)"
        AppLanguage.CHINESE -> "通过广告补充 ($count)"
        AppLanguage.RUSSIAN -> "Восполнить через рекламу ($count)"
        else -> "Recharge with Ads ($count)"
    }

    fun getAdsCountMessage(count: Int): String = when(currentLanguage) {
        AppLanguage.TURKISH -> "$count Reklam Kaldı"
        AppLanguage.ENGLISH -> "$count Ads Remaining"
        AppLanguage.SPANISH -> "$count anuncios restantes"
        AppLanguage.GERMAN -> "$count Anzeigen verbleibend"
        AppLanguage.FRENCH -> "$count pubs restantes"
        AppLanguage.ITALIAN -> "$count annunci rimanenti"
        AppLanguage.PORTUGUESE -> "$count anúncios restantes"
        AppLanguage.HINDI -> "$count विज्ञापन शेष"
        AppLanguage.CHINESE -> "剩余 $count 个广告"
        AppLanguage.RUSSIAN -> "Осталось $count рекламы"
        else -> "$count Ads Remaining"
    }

    fun getTimeOutMessage(): String = when(currentLanguage) { 
        AppLanguage.TURKISH -> "SÜRE BİTTİ!"
        AppLanguage.ENGLISH -> "TIME OUT!"
        AppLanguage.SPANISH -> "¡TIEMPO AGOTADO!"
        AppLanguage.GERMAN -> "ZEIT ABGELAUFEN!"
        AppLanguage.FRENCH -> "TEMPS ÉCOULÉ !"
        AppLanguage.ITALIAN -> "TEMPO SCADUTO!"
        AppLanguage.PORTUGUESE -> "TEMPO ESGOTADO!"
        AppLanguage.HINDI -> "समय समाप्त!"
        AppLanguage.CHINESE -> "时间到！"
        AppLanguage.RUSSIAN -> "ВРЕМЯ ВЫШЛО!"
        else -> "TIME OUT!" 
    }
    
    fun getWrongAnswerMessage(): String = when(currentLanguage) { 
        AppLanguage.TURKISH -> "YANLIŞ CEVAP!"
        AppLanguage.ENGLISH -> "WRONG ANSWER!"
        AppLanguage.SPANISH -> "¡RESPUESTA INCORRECTA!"
        AppLanguage.GERMAN -> "FALSCHE ANTWORT!"
        AppLanguage.FRENCH -> "MAUVAISE RÉPONSE !"
        AppLanguage.ITALIAN -> "RISPOSTA ERRATA!"
        AppLanguage.PORTUGUESE -> "RESPOSTA ERRADA!"
        AppLanguage.HINDI -> "गलत उत्तर!"
        AppLanguage.CHINESE -> "回答错误！"
        AppLanguage.RUSSIAN -> "НЕВЕРНЫЙ ОТВЕТ!"
        else -> "WRONG ANSWER!" 
    }

    fun getScientistDescription(id: String): String = when(id) {
        "einstein" -> when(currentLanguage) { 
            AppLanguage.TURKISH -> "Zamanın akışını yavaşlatır."
            AppLanguage.ENGLISH -> "Slows down the flow of time to help you break speed records."
            AppLanguage.SPANISH -> "Ralentiza el paso del tiempo para ayudarte a batir récords."
            AppLanguage.GERMAN -> "Verlangsamt den Zeitfluss, um Geschwindigkeitsrekorde zu brechen."
            AppLanguage.FRENCH -> "Ralentit le flux du temps pour vous aider à battre des records."
            AppLanguage.ITALIAN -> "Rallenta lo scorrere del tempo per aiutarti a battere i record."
            AppLanguage.PORTUGUESE -> "Retarda o fluxo do tempo para ajudá-lo a bater recordes de velocidade."
            AppLanguage.HINDI -> "गति रिकॉर्ड तोड़ने में आपकी मदद करने के लिए समय के प्रवाह को धीма करता है।"
            AppLanguage.CHINESE -> "减缓时间流逝，帮助您打破速度记录。"
            AppLanguage.RUSSIAN -> "Замедляет течение времени, чтобы помочь вам побить рекорды скорости."
            else -> "Slows down the flow of time."
        }
        "tesla" -> when(currentLanguage) { 
            AppLanguage.TURKISH -> "1 kesin yanlış cevabı yok eder."
            AppLanguage.ENGLISH -> "Zaps away 1 definitely wrong answer (50/50 chance)."
            AppLanguage.SPANISH -> "Elimina 1 respuesta definitivamente incorrecta (50/50)."
            AppLanguage.GERMAN -> "Entfernt 1 definitiv falsche Antwort (50/50-Chance)."
            AppLanguage.FRENCH -> "Élimine 1 réponse définitivement fausse (50/50)."
            AppLanguage.ITALIAN -> "Elimina 1 risposta sicuramente sbagliata (50/50)."
            AppLanguage.PORTUGUESE -> "Elimina 1 resposta definitivamente errada (chance 50/50)."
            AppLanguage.HINDI -> "1 निश्चित रूप से गलत उत्तर (50/50 मौका) को हटा देता है।"
            AppLanguage.CHINESE -> "消灭1个绝对错误的答案（50/50几率）。"
            AppLanguage.RUSSIAN -> "Уничтожает 1 заведомо неверный ответ (шанс 50/50)."
            else -> "Zaps away 1 definitely wrong answer." 
        }
        "newton" -> when(currentLanguage) { 
            AppLanguage.TURKISH -> "Bir defalık can kalkanı verir."
            AppLanguage.ENGLISH -> "Provides a one-time life shield (no life lost on mistake)."
            AppLanguage.SPANISH -> "Proporciona un escudo de vida único (sin pérdida de vida en caso de error)."
            AppLanguage.GERMAN -> "Bietet einen einmaligen Lebensschild (kein Lebensverlust bei Fehlern)."
            AppLanguage.FRENCH -> "Fournit un bouclier de vie unique (pas de vie perdue en cas d'erreur)."
            AppLanguage.ITALIAN -> "Fornisce uno scudo vitale unico (nessuna vita persa in caso di errore)."
            AppLanguage.PORTUGUESE -> "Fornece um escudo de vida único (nenhuma vida perdida em caso de erro)."
            AppLanguage.HINDI -> "एक बार का जीवन ढाल प्रदान करता है (गलती पर कोई जीवन नहीं खोता)।"
            AppLanguage.CHINESE -> "提供一次性生命护盾（失误时不扣除生命）。"
            AppLanguage.RUSSIAN -> "Дает одноразовый щит жизни (жизнь не теряется при ошибке)."
            else -> "Provides a one-time life shield." 
        }
        "curie" -> when(currentLanguage) { 
            AppLanguage.TURKISH -> "Mevcut puanınıza anında %25 ekler."
            AppLanguage.ENGLISH -> "Adds 25% extra points to your current score instantly."
            AppLanguage.SPANISH -> "Añade instantáneamente un 25% de puntos extra a tu puntuación actual."
            AppLanguage.GERMAN -> "Fügt Ihrem aktuellen Punktestand sofort 25 % Extrapunkte hinzu."
            AppLanguage.FRENCH -> "Ajoute instantanément 25 % de points supplémentaires à votre score actuel."
            AppLanguage.ITALIAN -> "Aggiunge istantaneamente il 25% di punti extra al tuo punteggio attuale."
            AppLanguage.PORTUGUESE -> "Adiciona 25% de pontos extras à sua pontuação atual instantaneamente."
            AppLanguage.HINDI -> "आपके वर्तमान स्कोर में तुरंत 25% अतिरिक्त अंक जोड़ता है।"
            AppLanguage.CHINESE -> "立刻为您的当前分数增加25%的额外积分。"
            AppLanguage.RUSSIAN -> "Мгновенно добавляет 25% дополнительных очков к вашему текущему счету."
            else -> "Adds 25% extra points instantly." 
        }
        "pythagoras" -> when(currentLanguage) { 
            AppLanguage.TURKISH -> "Süreye +5 saniye ekler."
            AppLanguage.ENGLISH -> "Adds +5 seconds to the timer when used."
            AppLanguage.SPANISH -> "Añade +5 segundos al temporizador cuando se usa."
            AppLanguage.GERMAN -> "Fügt dem Timer bei Verwendung +5 Sekunden hinzu."
            AppLanguage.FRENCH -> "Ajoute +5 secondes au minuteur lors de son utilisation."
            AppLanguage.ITALIAN -> "Aggiunge +5 secondi al timer quando usato."
            AppLanguage.PORTUGUESE -> "Adiciona +5 segundos ao cronômetro quando usado."
            AppLanguage.HINDI -> "उपयोग करने पर टाइमر में +5 सेकंड जोड़ता है।"
            AppLanguage.CHINESE -> "使用时计时器增加+5秒。"
            AppLanguage.RUSSIAN -> "Добавляет +5 секунд к таймеру при использовании."
            else -> "Adds +5 seconds to the timer." 
        }
        "turing" -> when(currentLanguage) { 
            AppLanguage.TURKISH -> "İpucu olarak bir yanlış cevabı eler."
            AppLanguage.ENGLISH -> "Eliminates one wrong answer as a hint."
            AppLanguage.SPANISH -> "Elimina una respuesta incorrecta como pista."
            AppLanguage.GERMAN -> "Eliminiert eine falsche Antwort als Hinweis."
            AppLanguage.FRENCH -> "Élimine une mauvaise réponse en guise d'indice."
            AppLanguage.ITALIAN -> "Elimina una risposta sbagliata come suggerimento."
            AppLanguage.PORTUGUESE -> "Elimina uma resposta errada como dica."
            AppLanguage.HINDI -> "एक संकेत के रूप में एक गलत उत्तर हटा देता है।"
            AppLanguage.CHINESE -> "消除一个错误答案作为提示。"
            AppLanguage.RUSSIAN -> "Исключает один неправильный ответ в качестве подсказки."
            else -> "Eliminates one wrong answer as a hint." 
        }
        "cahit_arf" -> when(currentLanguage) { 
            AppLanguage.TURKISH -> "Süreyi tamamen dondurur."
            AppLanguage.ENGLISH -> "Freezes time completely for that question."
            AppLanguage.SPANISH -> "Congela el tiempo por completo para esa pregunta."
            AppLanguage.GERMAN -> "Friert die Zeit für diese Frage komplett ein."
            AppLanguage.FRENCH -> "Gèle complètement le temps pour cette question."
            AppLanguage.ITALIAN -> "Congela completamente il tempo per quella domanda."
            AppLanguage.PORTUGUESE -> "Congela o tempo completamente para essa pergunta."
            AppLanguage.HINDI -> "उस प्रश्न के लिए समय पूरी तरह से रोक देता है।"
            AppLanguage.CHINESE -> "该问题的时间完全冻结。"
            AppLanguage.RUSSIAN -> "Полностью замораживает время для этого вопроса."
            else -> "Freezes time completely." 
        }
        "gauss" -> when(currentLanguage) { 
            AppLanguage.TURKISH -> "Soruyu atlar ve doğru cevaplanmış sayar."
            AppLanguage.ENGLISH -> "Skips the question and counts it as correctly answered."
            AppLanguage.SPANISH -> "Salta la pregunta y la cuenta como respondida correctamente."
            AppLanguage.GERMAN -> "Überspringt die Frage und zählt sie als richtig beantwortet."
            AppLanguage.FRENCH -> "Passe la question et la compte comme répondue correctement."
            AppLanguage.ITALIAN -> "Salta la domanda e la conta come risposta corretta."
            AppLanguage.PORTUGUESE -> "Pula a pergunta e a conta como respondida corretamente."
            AppLanguage.HINDI -> "प्रश्न छोड़ देता है और इसे सही उत्तर के रूप में गिनता है।"
            AppLanguage.CHINESE -> "跳过问题并计为回答正确。"
            AppLanguage.RUSSIAN -> "Пропускает вопрос и засчитывает его как правильный ответ."
            else -> "Skips the question and counts it as correct." 
        }
        else -> when(currentLanguage) { 
            AppLanguage.TURKISH -> "Özel yetenek."
            AppLanguage.ENGLISH -> "Special ability."
            AppLanguage.SPANISH -> "Habilidad especial."
            AppLanguage.GERMAN -> "Spezialfähigkeit."
            AppLanguage.FRENCH -> "Capacité spéciale."
            AppLanguage.ITALIAN -> "Abilità speciale."
            AppLanguage.PORTUGUESE -> "Habilidade especial."
            AppLanguage.HINDI -> "विशेष क्षमता।"
            AppLanguage.CHINESE -> "特殊能力。"
            AppLanguage.RUSSIAN -> "Особая способность."
            else -> "Special ability." 
        }
    }

    val vsMatchedTitle: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "RAKİP BULUNDU!"
        AppLanguage.ENGLISH -> "OPPONENT FOUND!"
        AppLanguage.SPANISH -> "¡OPONENTE ENCONTRADO!"
        AppLanguage.GERMAN -> "GEGNER GEFUNDEN!"
        AppLanguage.FRENCH -> "ADVERSAIRE TROUVÉ!"
        AppLanguage.ITALIAN -> "AVVERSARIO TROVATO!"
        AppLanguage.PORTUGUESE -> "OPONENTE ENCONTRADO!"
        AppLanguage.HINDI -> "विरोधी मिल गया!"
        AppLanguage.CHINESE -> "找到对手！"
        AppLanguage.RUSSIAN -> "ПРОТИВНИК НАЙДЕН!"
        else -> "OPPONENT FOUND!"
    }

    val vsPreparingText: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "Düello Başlıyor..."
        AppLanguage.ENGLISH -> "Duel starting..."
        AppLanguage.SPANISH -> "Duelo comenzando..."
        AppLanguage.GERMAN -> "Duell beginnt..."
        AppLanguage.FRENCH -> "Le duel commence..."
        AppLanguage.ITALIAN -> "Duello in arrivo..."
        AppLanguage.PORTUGUESE -> "Duelo começando..."
        AppLanguage.HINDI -> "द्वंद्वयुद्ध शुरू हो रहा है..."
        AppLanguage.CHINESE -> "决斗即将开始..."
        AppLanguage.RUSSIAN -> "Дуэль начинается..."
        else -> "Duel starting..."
    }

    val vsYou: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "SEN"
        AppLanguage.ENGLISH -> "YOU"
        AppLanguage.SPANISH -> "TÚ"
        AppLanguage.GERMAN -> "DU"
        AppLanguage.FRENCH -> "TOI"
        AppLanguage.ITALIAN -> "TU"
        AppLanguage.PORTUGUESE -> "VOCÊ"
        AppLanguage.HINDI -> "आप"
        AppLanguage.CHINESE -> "你"
        AppLanguage.RUSSIAN -> "ТЫ"
        else -> "YOU"
    }

    val vsGameOverVictory: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "DÜELLO ZAFERİ! 🏆"
        AppLanguage.ENGLISH -> "DUEL VICTORY! 🏆"
        AppLanguage.SPANISH -> "¡VICTORIA DE DUELO! 🏆"
        AppLanguage.GERMAN -> "DUELL-SIEG! 🏆"
        AppLanguage.FRENCH -> "VICTOIRE DE DUEL! 🏆"
        AppLanguage.ITALIAN -> "VITTORIA DUELLO! 🏆"
        AppLanguage.PORTUGUESE -> "VITÓRIA DE DUELO! 🏆"
        AppLanguage.HINDI -> "द्वंद्वयुद्ध विजय! 🏆"
        AppLanguage.CHINESE -> "决斗胜利！🏆"
        AppLanguage.RUSSIAN -> "ПОБЕДА В ДУЭЛИ! 🏆"
        else -> "DUEL VICTORY! 🏆"
    }

    val vsGameOverDefeat: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "KAYBETTİN 💔"
        AppLanguage.ENGLISH -> "DEFEATED 💔"
        AppLanguage.SPANISH -> "DERROTADO 💔"
        AppLanguage.GERMAN -> "BESIEGT 💔"
        AppLanguage.FRENCH -> "DÉFAITE 💔"
        AppLanguage.ITALIAN -> "SCONFITTO 💔"
        AppLanguage.PORTUGUESE -> "DERROTADO 💔"
        AppLanguage.HINDI -> "पराजित 💔"
        AppLanguage.CHINESE -> "失败 💔"
        AppLanguage.RUSSIAN -> "ПОРАЖЕНИЕ 💔"
        else -> "DEFEATED 💔"
    }

    val vsGameOverDraw: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "BERABERE! 🤝"
        AppLanguage.ENGLISH -> "DRAW MATCH! 🤝"
        AppLanguage.SPANISH -> "¡EMPATE! 🤝"
        AppLanguage.GERMAN -> "UNENTSCHIEDEN! 🤝"
        AppLanguage.FRENCH -> "MATCH NUL! 🤝"
        AppLanguage.ITALIAN -> "PAREGGIO! 🤝"
        AppLanguage.PORTUGUESE -> "EMPATE! 🤝"
        AppLanguage.HINDI -> "ड्रा मैच! 🤝"
        AppLanguage.CHINESE -> "平局！🤝"
        AppLanguage.RUSSIAN -> "НИЧЬЯ! 🤝"
        else -> "DRAW MATCH! 🤝"
    }

    val vsGameOverKnockoutWin: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "NAKAVT! 🥊"
        AppLanguage.ENGLISH -> "KNOCKOUT! 🥊"
        AppLanguage.SPANISH -> "¡NOCAUT! 🥊"
        AppLanguage.GERMAN -> "K.O.! 🥊"
        AppLanguage.FRENCH -> "K.O.! 🥊"
        AppLanguage.ITALIAN -> "KO! 🥊"
        AppLanguage.PORTUGUESE -> "NOCAUTE! 🥊"
        AppLanguage.HINDI -> "नॉकआउट! 🥊"
        AppLanguage.CHINESE -> "击倒！🥊"
        AppLanguage.RUSSIAN -> "НОКАУТ! 🥊"
        else -> "KNOCKOUT! 🥊"
    }

    val vsGameOverKnockoutLoss: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "NAKAVT OLDUN! 🥊"
        AppLanguage.ENGLISH -> "KNOCKED OUT! 🥊"
        AppLanguage.SPANISH -> "¡FUESTE NOQUEADO! 🥊"
        AppLanguage.GERMAN -> "AUSGEKNOCKT! 🥊"
        AppLanguage.FRENCH -> "VOUS ÊTES K.O.! 🥊"
        AppLanguage.ITALIAN -> "SEI STATO MESSO KO! 🥊"
        AppLanguage.PORTUGUESE -> "FOI NOCAUTEADO! 🥊"
        AppLanguage.HINDI -> "नॉक आउट! 🥊"
        AppLanguage.CHINESE -> "被击倒！🥊"
        AppLanguage.RUSSIAN -> "ТЫ В НОКАУТЕ! 🥊"
        else -> "KNOCKED OUT! 🥊"
    }

    val vsRematchRequest: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "Rövanş İste"
        AppLanguage.ENGLISH -> "Request Rematch"
        AppLanguage.SPANISH -> "Pedir revancha"
        AppLanguage.GERMAN -> "Revanche fordern"
        AppLanguage.FRENCH -> "Demander une revanche"
        AppLanguage.ITALIAN -> "Richiedi rivincita"
        AppLanguage.PORTUGUESE -> "Pedir revanche"
        AppLanguage.HINDI -> "रीमैच का अनुरोध करें"
        AppLanguage.CHINESE -> "请求重赛"
        AppLanguage.RUSSIAN -> "Запросить реванш"
        else -> "Request Rematch"
    }

    val vsRematchWaiting: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "Rakip bekleniyor..."
        AppLanguage.ENGLISH -> "Waiting for opponent..."
        AppLanguage.SPANISH -> "Esperando al oponente..."
        AppLanguage.GERMAN -> "Warten auf Gegner..."
        AppLanguage.FRENCH -> "En attente de l'adversaire..."
        AppLanguage.ITALIAN -> "In attesa dell'avversario..."
        AppLanguage.PORTUGUESE -> "Aguardando oponente..."
        AppLanguage.HINDI -> "प्रतिद्वंद्वी की प्रतीक्षा..."
        AppLanguage.CHINESE -> "等待对手..."
        AppLanguage.RUSSIAN -> "Ожидание противника..."
        else -> "Waiting for opponent..."
    }

    val vsRematchAccept: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "Rövanşı Kabul Et"
        AppLanguage.ENGLISH -> "Accept Rematch"
        AppLanguage.SPANISH -> "Aceptar revancha"
        AppLanguage.GERMAN -> "Revanche annehmen"
        AppLanguage.FRENCH -> "Accepter la revanche"
        AppLanguage.ITALIAN -> "Accetta rivincita"
        AppLanguage.PORTUGUESE -> "Aceitar revanche"
        AppLanguage.HINDI -> "रीमैच स्वीकार करें"
        AppLanguage.CHINESE -> "接受重赛"
        AppLanguage.RUSSIAN -> "Принять реванш"
        else -> "Accept Rematch"
    }

    val vsPlayAgain: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "Yeni Rakip Bul"
        AppLanguage.ENGLISH -> "Find New Opponent"
        AppLanguage.SPANISH -> "Buscar nuevo oponente"
        AppLanguage.GERMAN -> "Neuen Gegner finden"
        AppLanguage.FRENCH -> "Trouver un nouvel adversaire"
        AppLanguage.ITALIAN -> "Trova nuovo avversario"
        AppLanguage.PORTUGUESE -> "Encontrar novo oponente"
        AppLanguage.HINDI -> "नया प्रतिद्वंद्वी खोजें"
        AppLanguage.CHINESE -> "寻找新对手"
        AppLanguage.RUSSIAN -> "Найти нового противника"
        else -> "Find New Opponent"
    }

    val vsMenu: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "Menüye Dön"
        AppLanguage.ENGLISH -> "Back to Menu"
        AppLanguage.SPANISH -> "Volver al menú"
        AppLanguage.GERMAN -> "Zurück zum Menü"
        AppLanguage.FRENCH -> "Retour au menu"
        AppLanguage.ITALIAN -> "Torna al menu"
        AppLanguage.PORTUGUESE -> "Voltar ao menu"
        AppLanguage.HINDI -> "मेनू पर वापस जाएं"
        AppLanguage.CHINESE -> "返回菜单"
        AppLanguage.RUSSIAN -> "В меню"
        else -> "Back to Menu"
    }

    val vsEmoteGoodLuck: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "Bol Şanslar!"
        AppLanguage.SPANISH -> "¡Buena Suerte!"
        AppLanguage.GERMAN -> "Viel Glück!"
        AppLanguage.FRENCH -> "Bonne Chance!"
        AppLanguage.ITALIAN -> "Buona Fortuna!"
        AppLanguage.PORTUGUESE -> "Boa Sorte!"
        AppLanguage.HINDI -> "शुभकामनाएँ!"
        AppLanguage.CHINESE -> "祝你好运！"
        AppLanguage.RUSSIAN -> "Удачи!"
        else -> "Good Luck!"
    }

    val vsEmoteTooFast: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "Çok Hızlı!"
        AppLanguage.SPANISH -> "¡Demasiado Rápido!"
        AppLanguage.GERMAN -> "Zu Schnell!"
        AppLanguage.FRENCH -> "Trop Rapide!"
        AppLanguage.ITALIAN -> "Troppo Veloce!"
        AppLanguage.PORTUGUESE -> "Muito Rápido!"
        AppLanguage.HINDI -> "बहुत तेज़!"
        AppLanguage.CHINESE -> "太快了！"
        AppLanguage.RUSSIAN -> "Слишком быстро!"
        else -> "Too Fast!"
    }

    val vsEmoteOops: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "Tüh!"
        AppLanguage.SPANISH -> "¡Uy!"
        AppLanguage.GERMAN -> "Hoppla!"
        AppLanguage.FRENCH -> "Oups!"
        AppLanguage.ITALIAN -> "Ops!"
        AppLanguage.PORTUGUESE -> "Ops!"
        AppLanguage.HINDI -> "ऊप्स!"
        AppLanguage.CHINESE -> "哎呀！"
        AppLanguage.RUSSIAN -> "Ой!"
        else -> "Oops!"
    }

    val vsEmoteHurryUp: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "Acele Et!"
        AppLanguage.SPANISH -> "¡Date Prisa!"
        AppLanguage.GERMAN -> "Beeil Dich!"
        AppLanguage.FRENCH -> "Dépêche-toi!"
        AppLanguage.ITALIAN -> "Sbrigati!"
        AppLanguage.PORTUGUESE -> "Depressa!"
        AppLanguage.HINDI -> "जल्दी करो!"
        AppLanguage.CHINESE -> "快点！"
        AppLanguage.RUSSIAN -> "Поторапливайся!"
        else -> "Hurry Up!"
    }

    val vsEmoteThanks: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "Teşekkürler!"
        AppLanguage.SPANISH -> "¡Gracias!"
        AppLanguage.GERMAN -> "Danke!"
        AppLanguage.FRENCH -> "Merci!"
        AppLanguage.ITALIAN -> "Grazie!"
        AppLanguage.PORTUGUESE -> "Obrigado!"
        AppLanguage.HINDI -> "धन्यवाद!"
        AppLanguage.CHINESE -> "谢谢！"
        AppLanguage.RUSSIAN -> "Спасибо!"
        else -> "Thanks!"
    }

    val vsEmoteGoodGame: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "İyi Oyundu"
        AppLanguage.SPANISH -> "Buen Juego"
        AppLanguage.GERMAN -> "Gutes Spiel"
        AppLanguage.FRENCH -> "Bien Joué"
        AppLanguage.ITALIAN -> "Bella Partita"
        AppLanguage.PORTUGUESE -> "Bom Jogo"
        AppLanguage.HINDI -> "अच्छा खेल"
        AppLanguage.CHINESE -> "好游戏"
        AppLanguage.RUSSIAN -> "Хорошая игра"
        else -> "Good Game"
    }

    val vsTitleStartDuel: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "DÜELLOYA BAŞLA"
        AppLanguage.SPANISH -> "EMPEZAR DUELO"
        AppLanguage.GERMAN -> "DUELL STARTEN"
        AppLanguage.FRENCH -> "COMMENCER LE DUEL"
        AppLanguage.ITALIAN -> "INIZIA DUELLO"
        AppLanguage.PORTUGUESE -> "COMEÇAR DUELO"
        AppLanguage.HINDI -> "द्वंद्व शुरू करें"
        AppLanguage.CHINESE -> "开始决斗"
        AppLanguage.RUSSIAN -> "НАЧАТЬ ДУЭЛЬ"
        else -> "START DUEL"
    }

    val vsLabelPlayerName: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "Oyuncu Adın:"
        AppLanguage.SPANISH -> "Tu Nombre:"
        AppLanguage.GERMAN -> "Dein Name:"
        AppLanguage.FRENCH -> "Ton Nom:"
        AppLanguage.ITALIAN -> "Il Tuo Nome:"
        AppLanguage.PORTUGUESE -> "Seu Nome:"
        AppLanguage.HINDI -> "आपका नाम:"
        AppLanguage.CHINESE -> "你的名字："
        AppLanguage.RUSSIAN -> "Ваше имя:"
        else -> "Your Player Name:"
    }

    val vsPlaceholderUsername: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "Kullanıcı Adı"
        AppLanguage.SPANISH -> "Nombre de usuario"
        AppLanguage.GERMAN -> "Benutzername"
        AppLanguage.FRENCH -> "Nom d'utilisateur"
        AppLanguage.ITALIAN -> "Nome utente"
        AppLanguage.PORTUGUESE -> "Nome de usuário"
        AppLanguage.HINDI -> "उपयोगकर्ता नाम"
        AppLanguage.CHINESE -> "用户名"
        AppLanguage.RUSSIAN -> "Имя пользователя"
        else -> "Username"
    }

    val vsBtnConnect: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "Bağlan"
        AppLanguage.SPANISH -> "Conectar"
        AppLanguage.GERMAN -> "Verbinden"
        AppLanguage.FRENCH -> "Connecter"
        AppLanguage.ITALIAN -> "Connetti"
        AppLanguage.PORTUGUESE -> "Conectar"
        AppLanguage.HINDI -> "जुड़ें"
        AppLanguage.CHINESE -> "连接"
        AppLanguage.RUSSIAN -> "Подключиться"
        else -> "Connect"
    }

    val vsMatchmakingTitle: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "EŞLEŞME ARANIYOR"
        AppLanguage.SPANISH -> "BUSCANDO PARTIDA"
        AppLanguage.GERMAN -> "SPIELERSUCHE"
        AppLanguage.FRENCH -> "RECHERCHE"
        AppLanguage.ITALIAN -> "RICERCA AVVERSARIO"
        AppLanguage.PORTUGUESE -> "BUSCANDO PARTIDA"
        AppLanguage.HINDI -> "मैचमेकिंग"
        AppLanguage.CHINESE -> "匹配中"
        AppLanguage.RUSSIAN -> "ПОИСК ИГРЫ"
        else -> "MATCHMAKING"
    }

    val vsMatchmakingDesc: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "Kendinize uygun bir rakip aranıyor..."
        AppLanguage.SPANISH -> "Buscando un oponente adecuado..."
        AppLanguage.GERMAN -> "Suche nach einem passenden Gegner..."
        AppLanguage.FRENCH -> "Recherche d'un adversaire..."
        AppLanguage.ITALIAN -> "Cerco un avversario..."
        AppLanguage.PORTUGUESE -> "Buscando um oponente adequado..."
        AppLanguage.HINDI -> "एक उपयुक्त प्रतिद्वंद्वी की तलाश..."
        AppLanguage.CHINESE -> "正在寻找合适的对手..."
        AppLanguage.RUSSIAN -> "Поиск подходящего противника..."
        else -> "Looking for a suitable opponent..."
    }

    val vsBtnForfeit: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "Çekil"
        AppLanguage.SPANISH -> "Rendirse"
        AppLanguage.GERMAN -> "Aufgeben"
        AppLanguage.FRENCH -> "Abandonner"
        AppLanguage.ITALIAN -> "Arrenditi"
        AppLanguage.PORTUGUESE -> "Desistir"
        AppLanguage.HINDI -> "हार मानना"
        AppLanguage.CHINESE -> "认输"
        AppLanguage.RUSSIAN -> "Сдаться"
        else -> "Forfeit"
    }

    val vsYourScore: String get() = when (currentLanguage) {
        AppLanguage.TURKISH -> "Senin Skorun"
        AppLanguage.SPANISH -> "Tu Puntuación"
        AppLanguage.GERMAN -> "Deine Punktzahl"
        AppLanguage.FRENCH -> "Ton Score"
        AppLanguage.ITALIAN -> "Il Tuo Punteggio"
        AppLanguage.PORTUGUESE -> "Sua Pontuação"
        AppLanguage.HINDI -> "आपका स्कोर"
        AppLanguage.CHINESE -> "你的分数"
        AppLanguage.RUSSIAN -> "Твой счет"
        else -> "Your Score"
    }

    fun isValidUsername(name: String): Boolean = name.length in 3..15 && name.all { it.isLetterOrDigit() }
    fun isUsernameBanned(name: String): Boolean = bannedWords.any { name.lowercase().contains(it) }
}
