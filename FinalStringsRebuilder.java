import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.*;

public class FinalStringsRebuilder {
    public static void main(String[] args) throws Exception {
        Map<String, Map<String, String>> translations = new HashMap<>();

        // 1. Hardcoded Turkish Fallbacks & UI Map
        Map<String, String> tr = new HashMap<>();
        tr.put("Settings", "Ayarlar");
        tr.put("Music", "Müzik");
        tr.put("Sound", "Ses");
        tr.put("Sound Effects", "Ses Efektleri");
        tr.put("Vibration Strength", "Titreşim Şiddeti");
        tr.put("Auto Theme", "Otomatik Tema");
        tr.put("Auto Theme Description", "Zamana göre otomatik değişir");
        tr.put("Voice Feedback", "Sesli Geri Bildirim");
        tr.put("Voice Feedback Description", "Soruları ve sonuçları seslendirir");
        tr.put("Theme", "Tema");
        tr.put("Swipe Hint", "Seçmek için kaydır");
        tr.put("Language Label", "Dil");
        tr.put("Store Title", "MAĞAZA");
        tr.put("Equipped Abilities", "Kuşanılmış Yetenekler");
        tr.put("No Abilities Equipped", "Henüz yetenek kuşanılmadı.");
        tr.put("Usage Rights", "Kullanım Hakları");
        tr.put("Paused", "DURAKLATILDI");
        tr.put("Review Invitation Title", "Oyunumuzu Beğendiniz mi?");
        tr.put("Review Invitation Message", "Gelişmemize yardımcı olmak için yorum bırakmak ister misiniz? 1000 Yıldız hediyemiz olacak!");
        tr.put("Rate Now", "Puanla");
        tr.put("No Thanks", "Hayır, Teşekkürler");
        tr.put("Welcome Gift Title", "Hoş Geldin Hediyesi!");
        tr.put("Welcome Gift Message", "BlitzMath ailesine hoş geldin! İlk oyunun şerefine sana Pisagor kartını hediye ediyoruz.");
        tr.put("OK", "Tamam");
        tr.put("Stat Score", "PUAN");
        tr.put("Stat Check", "KONTROL");
        tr.put("Stat Streak", "SERİ");
        tr.put("Question", "SORU");
        tr.put("Time", "SÜRE");
        tr.put("Addition", "Toplama");
        tr.put("Subtraction", "Çıkarma");
        tr.put("Multiplication", "Çarpma");
        tr.put("Division", "Bölme");
        tr.put("Mixed", "Karışık");
        tr.put("Checkpoint Label", "Seviye");
        tr.put("Congratulations", "Tebrikler!");
        tr.put("Next Level", "Sonraki Seviye");
        tr.put("Watch Ad To Save", "Canlanmak için İzle");
        tr.put("Total Time Bonus", "Toplam Süre Bonusu");
        tr.put("Watch Ad Continue", "Reklamla Devam Et");
        tr.put("Slogan", "Matematiksel Zekanı Hızlandır!");
        tr.put("Other Games", "Diğer Oyunlarımız");
        tr.put("Exit Dialog Title", "Çıkış");
        tr.put("Exit Dialog Message", "Ayrılmadan önce diğer oyunlarımızı denemek ister misiniz?");
        tr.put("Exit Confirm", "Evet, Bak");
        tr.put("Exit Dismiss", "Hayır, Çık");
        tr.put("Daily Reward", "GÜNLÜK ÖDÜL");
        tr.put("Streak", "Seri");
        tr.put("Daily Bonus Desc", "Her gün gelerek daha fazla yıldız kazan!");
        tr.put("Stars", "Yıldız");
        tr.put("Claim", "AL");
        tr.put("Out Of Lives Refill Ad", "Reklamla Can Yenile");
        tr.put("Usage Rights", "Kullanım Hakları");
        tr.put("Points", "Puan");
        tr.put("Equip", "KUŞAN");
        tr.put("Remove", "ÇIKAR");
        tr.put("Privacy Policy", "Gizlilik Politikası");
        tr.put("Total Play Time", "Toplam Oyun Süresi");
        tr.put("High Scores", "Yüksek Skorlar");
        tr.put("No Scores Yet", "Henüz skor yok");
        tr.put("Score", "Puan");
        tr.put("Level", "Seviye");
        tr.put("Date", "Tarih");
        tr.put("Clear All", "Hepsini Temizle");
        tr.put("Global Leaderboard", "Dünya Sıralaması");
        tr.put("Your Rank", "Sıralamanız");
        tr.put("Top Score", "En Yüksek Puan");
        tr.put("Update Title", "GÜNCELLEME GEREKİYOR");
        tr.put("Update Message", "Matematik Laboratuvarı Güncellendi! ⚡ Bilim insanları artık daha güçlü. Devam etmek için son sürümü indirin!");
        tr.put("Update Button", "ŞİMDİ GÜNCELLE");
        tr.put("Support Title", "Destek ve Yardım");
        tr.put("Support Desc", "Herhangi bir sorun veya öneriniz için bize ulaşın.");
        tr.put("Whatsapp Message", "WhatsApp ile Bize Yazın");
        tr.put("Email Message", "E-posta ile Bize Ulaşın");
        tr.put("Close", "Kapat");
        tr.put("Card Unlocked", "YENİ KART AÇILDI!");
        tr.put("Stat Charges", "Kullanım Hakkı");
        tr.put("Hint Add", "Topla");
        tr.put("Hint Subtract", "Çıkar");
        tr.put("Hint Multiply", "Çarp");
        tr.put("Hint Divide", "Böl");

        // Load JSONs
        parseAllJson(readFile("translations_all.json"), translations);
        parsePropJson(readFile("translations.json"), translations);

        // Inject TR
        for (String k : tr.keySet()) {
            Map<String, String> m = translations.getOrDefault(k, new HashMap<>());
            m.put("TURKISH", tr.get(k));
            translations.put(k, m);
        }

        // Generate Strings.kt
        StringBuilder sb = new StringBuilder();
        sb.append("package com.mawelly.blitzmath.localization\n\n");
        sb.append("import androidx.compose.runtime.getValue\nimport androidx.compose.runtime.mutableStateOf\nimport androidx.compose.runtime.setValue\n");
        sb.append("import kotlinx.coroutines.flow.MutableStateFlow\nimport kotlinx.coroutines.flow.StateFlow\nimport kotlinx.coroutines.flow.asStateFlow\n\n");
        sb.append("object Strings {\n\n");
        sb.append("    private val _currentLanguage = MutableStateFlow(AppLanguage.TURKISH)\n    val currentLanguageFlow: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()\n\n");
        sb.append("    var currentLanguage: AppLanguage by mutableStateOf(AppLanguage.TURKISH)\n        private set\n\n");
        sb.append("    fun setLanguage(lang: AppLanguage) { currentLanguage = lang; _currentLanguage.value = lang }\n\n");
        sb.append("    val bannedWords = listOf(\"piç\", \"pic\", \"amk\", \"ananı\", \"göt\", \"oç\", \"orospu\", \"yarrak\", \"sik\", \"amcık\", \"meme\", \"mal\", \"salak\")\n\n");

        // Map English keys to property names
        String[][] ui = {
            {"menuClassic", "Classic Mode"}, {"menuMixed", "Mixed Mode"}, {"menuChallenge", "Challenge Mode"},
            {"collection", "Collection"}, {"menuLeaderboard", "Leaderboard"}, {"menuSettings", "Settings"},
            {"settings", "Settings"}, {"music", "Music"}, {"sound", "Sound"}, {"vibrationStrength", "Vibration Strength"},
            {"autoTheme", "Auto Theme"}, {"autoThemeDesc", "Auto Theme Description"},
            {"voiceFeedback", "Voice Feedback"}, {"voiceFeedbackDesc", "Voice Feedback Description"},
            {"theme", "Theme"}, {"swipeHint", "Swipe Hint"}, {"languageLabel", "Language Label"},
            {"storeTitle", "Store Title"}, {"equippedAbilities", "Equipped Abilities"}, {"noAbilitiesEquipped", "No Abilities Equipped"},
            {"usageRights", "Usage Rights"}, {"paused", "Paused"}, {"reviewInvitationTitle", "Review Invitation Title"},
            {"reviewInvitationMessage", "Review Invitation Message"}, {"rateNow", "Rate Now"}, {"noThanks", "No Thanks"},
            {"welcomeGiftTitle", "Welcome Gift Title"}, {"welcomeGiftMessage", "Welcome Gift Message"}, {"ok", "OK"},
            {"statScore", "Stat Score"}, {"statCheck", "Stat Check"}, {"statStreak", "Stat Streak"},
            {"question", "Question"}, {"time", "Time"}, {"addition", "Addition"}, {"subtraction", "Subtraction"},
            {"multiplication", "Multiplication"}, {"division", "Division"}, {"mixed", "Mixed"},
            {"checkpointLabel", "Checkpoint Label"}, {"congratulations", "Congratulations"}, {"nextLevel", "Next Level"},
            {"watchAdToSave", "Watch Ad To Save"}, {"totalTimeBonus", "Total Time Bonus"}, {"watchAdContinue", "Watch Ad Continue"},
            {"slogan", "Slogan"}, {"otherGames", "Other Games"}, {"exitDialogTitle", "Exit Dialog Title"},
            {"exitDialogMessage", "Exit Dialog Message"}, {"exitConfirm", "Exit Confirm"}, {"exitDismiss", "Exit Dismiss"},
            {"dailyReward", "Daily Reward"}, {"streak", "Streak"}, {"dailyBonusDesc", "Daily Bonus Desc"}, {"stars", "Stars"}, {"claim", "Claim"},
            {"outOfLivesRefillAd", "Out Of Lives Refill Ad"}, {"points", "Points"}, {"equip", "Equip"}, {"remove", "Remove"},
            {"privacyPolicy", "Privacy Policy"}, {"totalPlayTime", "Total Play Time"},
            {"highScores", "High Scores"}, {"noScoresYet", "No Scores Yet"}, {"score", "Score"},
            {"level", "Level"}, {"date", "Date"}, {"clearAll", "Clear All"},
            {"globalLeaderboard", "Global Leaderboard"}, {"yourRank", "Your Rank"}, {"topScore", "Top Score"},
            {"updateTitle", "Update Title"}, {"updateMessage", "Update Message"}, {"updateButton", "Update Button"},
            {"supportTitle", "Support Title"}, {"supportDesc", "Support Desc"}, {"whatsappMessage", "Whatsapp Message"},
            {"emailMessage", "Email Message"}, {"close", "Close"}, {"cardUnlocked", "Card Unlocked"}, {"statCharges", "Stat Charges"},
            {"hintAdd", "Hint Add"}, {"hintSubtract", "Hint Subtract"}, {"hintMultiply", "Hint Multiply"}, {"hintDivide", "Hint Divide"},
            {"finalScore", "FINAL SCORE"}, {"newRecord", "NEW RECORD!"}, {"saveMeTitle", "SECOND CHANCE"},
            {"enterName", "Enter Name"},
            {"outOfLivesTitle", "OUT OF LIVES!"}, {"outOfLivesMessage", "Wait or watch ad!"}, {"continue_", "CONTINUE"},
            {"backToMenu", "BACK TO MENU"}, {"retry", "RETRY"}, {"challengeAlreadyPlayed", "You've used all challenge attempts for today!"}
        };

        for (String[] p : ui) appendProperty(sb, p[0], p[1], translations);

        // Language Names
        String[][] ln = {{"turkish", "Türkçe"}, {"english", "English"}, {"spanish", "Español"}, {"german", "Deutsch"}, {"french", "Français"}, {"italian", "Italiano"}, {"portuguese", "Português"}, {"hindi", "हिन्दी"}, {"chinese", "简体中文"}, {"russian", "Русский"}};
        for (String[] l : ln) sb.append("    val ").append(l[0]).append(": String get() = \"").append(escape(l[1])).append("\"\n\n");

        sb.append(getFooter());
        sb.append("}\n");

        writeFile("app/src/main/java/com/mawelly/blitzmath/localization/Strings.kt", sb.toString());
        System.out.println("Strings.kt rebuilt successfully with full mapping.");
    }

    private static void appendProperty(StringBuilder sb, String name, String eng, Map<String, Map<String, String>> translations) {
        sb.append("    val ").append(name).append(": String get() = when (currentLanguage) {\n");
        Map<String, String> t = translations.get(eng);
        if (t != null) {
            String[] langs = {"TURKISH", "ENGLISH", "SPANISH", "GERMAN", "FRENCH", "ITALIAN", "PORTUGUESE", "HINDI", "CHINESE", "RUSSIAN"};
            for (String l : langs) {
                String v = t.get(l);
                if (v == null && l.equals("ENGLISH")) v = eng;
                if (v != null) sb.append("        AppLanguage.").append(l).append(" -> \"").append(escape(v)).append("\"\n");
            }
        }
        sb.append("        else -> \"").append(escape(eng)).append("\"\n    }\n\n");
    }

    private static String escape(String s) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '"') out.append("\\\"");
            else if (c == '\n') out.append("\\n");
            else if (c == '\\') out.append("\\\\");
            else if (c == '$') out.append("\\$");
            else if (c > 127) out.append(String.format("\\u%04x", (int) c));
            else out.append(c);
        }
        return out.toString();
    }

    private static String readFile(String path) throws IOException {
        File f = new File(path); if (!f.exists()) return "{}";
        byte[] b = new byte[(int) f.length()]; try (FileInputStream fis = new FileInputStream(f)) { fis.read(b); }
        return new String(b, StandardCharsets.UTF_8);
    }

    private static void writeFile(String path, String content) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(path)) { fos.write(content.getBytes(StandardCharsets.UTF_8)); }
    }

    private static void parseAllJson(String json, Map<String, Map<String, String>> result) {
        Matcher m = Pattern.compile("\"([^\"]+)\"\\s*:\\s*\\{([^\\}]+)\\}", Pattern.DOTALL).matcher(json);
        while (m.find()) {
            String k = m.group(1).replace("g????", "⚡"); Map<String, String> sub = new HashMap<>();
            Matcher sm = Pattern.compile("\"([A-Z]+)\"\\s*:\\s*\"([^\"]+)\"").matcher(m.group(2));
            while (sm.find()) sub.put(sm.group(1), sm.group(2));
            result.put(k, sub);
        }
    }

    private static void parsePropJson(String json, Map<String, Map<String, String>> result) {
        Matcher m = Pattern.compile("\"([^\"]+)\"\\s*:\\s*\\{([^\\}]+)\\}", Pattern.DOTALL).matcher(json);
        while (m.find()) {
            String k = m.group(1); Map<String, String> sub = new HashMap<>();
            Matcher sm = Pattern.compile("\"([A-Z]+)\"\\s*:\\s*\"([^\"]+)\"").matcher(m.group(2));
            while (sm.find()) sub.put(sm.group(1), sm.group(2));
            result.put(k, sub);
        }
    }

    private static String getFooter() {
        return "\n    fun getSelectLanguage(lang: AppLanguage? = null): String = when (lang ?: currentLanguage) {\n" +
               "        AppLanguage.TURKISH -> \"Dil Seçimi\"\n        AppLanguage.ENGLISH -> \"Select Language\"\n        AppLanguage.HINDI -> \"भाषा चुनें\"\n        AppLanguage.CHINESE -> \"选择语言\"\n        AppLanguage.RUSSIAN -> \"Выберите язык\"\n        else -> \"Select Language\"\n    }\n\n" +
               "    fun getBannedWordError(lang: AppLanguage? = null): String = when (lang ?: currentLanguage) {\n        AppLanguage.TURKISH -> \"Kullanıcı adı uygunsuz kelimeler içeriyor!\"\n        else -> \"Username contains inappropriate words!\"\n    }\n\n" +
               "    fun getInvalidNameError(lang: AppLanguage? = null): String = when (lang ?: currentLanguage) {\n        AppLanguage.TURKISH -> \"Geçersiz kullanıcı adı!\"\n        else -> \"Invalid username!\"\n    }\n\n" +
               "    fun getNameRequired(lang: AppLanguage? = null): String = when (lang ?: currentLanguage) {\n        AppLanguage.TURKISH -> \"İsim gereklidir!\"\n        else -> \"Name is required!\"\n    }\n\n" +
               "    val challenge: String get() = when(currentLanguage) { AppLanguage.TURKISH -> \"MEYDAN OKUMA\"; else -> \"CHALLENGE\" }\n" +
               "    val checkpointComplete: String get() = when(currentLanguage) { AppLanguage.TURKISH -> \"Seviye Tamamlandı!\"; else -> \"Checkpoint Complete!\" }\n" +
               "    val gameOver: String get() = when(currentLanguage) { AppLanguage.TURKISH -> \"Oyun Bitti!\"; else -> \"Game Over!\" }\n" +
               "    val watchAdToPlayAgain: String get() = when(currentLanguage) { AppLanguage.TURKISH -> \"Tekrar oynamak için reklam izle\"; else -> \"Watch ad to play again\" }\n" +
               "    val shareScore: String get() = when(currentLanguage) { AppLanguage.TURKISH -> \"Puanı Paylaş\"; else -> \"Share Score\" }\n\n" +
               "    val randomExitVoicePrompt: String get() = \"exit_prompt\"\n\n" +
               "    fun getShareMessage(score: Int, checkpoint: Int): String = when(currentLanguage) {\n        AppLanguage.TURKISH -> \"BlitzMath'te $score puan yaptım ve $checkpoint. seviyeyi geçtim! 🧠⚡ Haydi sen de dene!\"\n        else -> \"I scored $score points and reached level $checkpoint in BlitzMath! 🧠⚡ Come and try to beat me!\"\n    }\n\n" +
               "    fun getRechargeAdsInfo(count: Int): String = when(currentLanguage) {\n        AppLanguage.TURKISH -> \"Reklamla Yenile ($count)\"\n        else -> \"Recharge with Ads ($count)\"\n    }\n\n" +
               "    fun getAdsCountMessage(count: Int): String = when(currentLanguage) {\n        AppLanguage.TURKISH -> \"$count Reklam Kaldı\"\n        else -> \"$count Ads Remaining\"\n    }\n\n" +
               "    fun getTimeOutMessage(): String = when(currentLanguage) { AppLanguage.TURKISH -> \"SÜRE BİTTİ!\"; else -> \"TIME OUT!\" }\n" +
               "    fun getWrongAnswerMessage(): String = when(currentLanguage) { AppLanguage.TURKISH -> \"YANLIŞ CEVAP!\"; else -> \"WRONG ANSWER!\" }\n\n" +
               "    fun getScientistDescription(id: String): String = when(id) {\n" +
               "        \"einstein\" -> when(currentLanguage) { AppLanguage.TURKISH -> \"Zamanın akışını yavaşlatır.\"; else -> \"Slows down the flow of time.\" }\n" +
               "        \"tesla\" -> when(currentLanguage) { AppLanguage.TURKISH -> \"1 kesin yanlış cevabı yok eder.\"; else -> \"Zaps away 1 definitely wrong answer.\" }\n" +
               "        \"newton\" -> when(currentLanguage) { AppLanguage.TURKISH -> \"Bir defalık can kalkanı verir.\"; else -> \"Provides a one-time life shield.\" }\n" +
               "        \"curie\" -> when(currentLanguage) { AppLanguage.TURKISH -> \"Mevcut puanınıza anında %25 ekler.\"; else -> \"Adds 25% extra points instantly.\" }\n" +
               "        \"pythagoras\" -> when(currentLanguage) { AppLanguage.TURKISH -> \"Süreye +5 saniye ekler.\"; else -> \"Adds +5 seconds to the timer.\" }\n" +
               "        \"turing\" -> when(currentLanguage) { AppLanguage.TURKISH -> \"İpucu olarak bir yanlış cevabı eler.\"; else -> \"Eliminates one wrong answer as a hint.\" }\n" +
               "        \"cahit_arf\" -> when(currentLanguage) { AppLanguage.TURKISH -> \"Süreyi tamamen dondurur.\"; else -> \"Freezes time completely.\" }\n" +
               "        \"gauss\" -> when(currentLanguage) { AppLanguage.TURKISH -> \"Soruyu atlar ve doğru cevaplanmış sayar.\"; else -> \"Skips the question and counts it as correct.\" }\n" +
               "        else -> when(currentLanguage) { AppLanguage.TURKISH -> \"Özel yetenek.\"; else -> \"Special ability.\" }\n" +
               "    }\n\n" +
               "    fun isValidUsername(name: String): Boolean = name.length in 3..15 && name.all { it.isLetterOrDigit() }\n" +
               "    fun isUsernameBanned(name: String): Boolean = bannedWords.any { name.lowercase().contains(it) }\n";
    }
}
