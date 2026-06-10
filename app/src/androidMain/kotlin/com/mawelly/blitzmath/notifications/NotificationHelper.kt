package com.mawelly.blitzmath.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.mawelly.blitzmath.MainActivity
import com.mawelly.blitzmath.R
import com.mawelly.blitzmath.localization.AppLanguage
import kotlin.random.Random

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "engagement_notifications"
        const val NOTIFICATION_ID = 1001
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "BlitzMath Hatırlatıcı"
            val descriptionText = "Geri dön ve zihnini tazele!"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showRetentionNotification(language: AppLanguage) {
        val (title, body) = getRandomQuote(language)
        showNotification(title, body, NOTIFICATION_ID)
    }

    fun showRechargeNotification(cardName: String, language: AppLanguage) {
        val title = when (language) {
            AppLanguage.TURKISH -> "Enerji Doldu! ⚡"
            AppLanguage.ENGLISH -> "Energy Charged! ⚡"
            AppLanguage.SPANISH -> "¡Energía Cargada! ⚡"
            AppLanguage.GERMAN -> "Energie Geladen! ⚡"
            AppLanguage.FRENCH -> "Énergie Chargée ! ⚡"
            AppLanguage.ITALIAN -> "Energia Caricata! ⚡"
            AppLanguage.PORTUGUESE -> "Energia Carregada! ⚡"
            AppLanguage.HINDI -> "ऊर्जा चार्ज हो गई! ⚡"
            AppLanguage.CHINESE -> "能量已满！ ⚡"
            AppLanguage.RUSSIAN -> "Энергия Заряжена! ⚡"
        }
        val body = when (language) {
            AppLanguage.TURKISH -> "$cardName artık hazır. Gel ve zihnini tazele! 🧠"
            AppLanguage.ENGLISH -> "$cardName is now ready. Come and refresh your mind! 🧠"
            AppLanguage.SPANISH -> "$cardName ya está listo. ¡Ven y refresca tu mente! 🧠"
            AppLanguage.GERMAN -> "$cardName ist jetzt bereit. Komm und erfrische deinen Geist! 🧠"
            AppLanguage.FRENCH -> "$cardName est maintenant prêt. Venez vous rafraîchir l'esprit ! 🧠"
            AppLanguage.ITALIAN -> "$cardName è ora pronto. Vieni a rinfrescare la tua mente! 🧠"
            AppLanguage.PORTUGUESE -> "$cardName já está pronto. Venha e refresque sua mente! 🧠"
            AppLanguage.HINDI -> "$cardName अब तैयार है। आओ और अपने मन को तरोताजा करो! 🧠"
            AppLanguage.CHINESE -> "$cardName 已准备就绪。快来挑战你的大脑吧！ 🧠"
            AppLanguage.RUSSIAN -> "$cardName теперь готов. Приходи и освежи свой разум! 🧠"
        }
        showNotification(title, body, cardName.hashCode())
    }

    private fun showNotification(title: String, body: String, id: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, id, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(com.mawelly.blitzmath.R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(id, builder.build())
    }

    private fun getRandomQuote(language: AppLanguage): Pair<String, String> {
        val quotes = when (language) {
            AppLanguage.TURKISH -> listOf(
                "Zihnini Tazelemek İster Misin?" to "Matematik evrenin dilidir, gel biraz pratik yapalım! ✨",
                "BlitzMath Seni Bekliyor!" to "Birkaç hızlı soruyla beynini canlandırmaya ne dersin? 🧠",
                "Yeni Skorlara Hazır Mısın?" to "Rekorun seni bekliyor, gel ve onu tazele! 🏆",
                "Günün Matematik Pratiği!" to "Sadece 2 dakika ayırarak zihnini zinde tutabilirsin. 💪",
                "Matematiğin Büyüsüne Dön!" to "Sayılar seni özledi, gel ve çözmeye başla! ⚡"
            )
            AppLanguage.SPANISH -> listOf(
                "¿Quieres refrescar tu mente?" to "Las matemáticas son el lenguaje del universo, ¡practiquemos! ✨",
                "¡BlitzMath te espera!" to "¿Qué tal si estimulas tu cerebro con unas preguntas? 🧠",
                "¿Listo para nuevas puntuaciones?" to "Tu récord te espera, ¡ven a superarlo! 🏆"
            )
            AppLanguage.GERMAN -> listOf(
                "Möchten Sie Ihren Geist erfrischen?" to "Mathe ist die Sprache des Universums, lass uns üben! ✨",
                "BlitzMath wartet!" to "Wie wäre es mit ein paar Fragen zur Stimulation? 🧠",
                "Bereit für neue Bestleistungen?" to "Dein Rekord wartet, komm ve knacke ihn! 🏆"
            )
            AppLanguage.FRENCH -> listOf(
                "Envie de rafraîchir votre esprit ?" to "Les maths sont le langage de l'univers, pratiquons ! ✨",
                "BlitzMath vous attend !" to "Et si vous stimuliez votre cerveau avec quelques questions ? 🧠",
                "Prêt pour de nouveaux scores ?" to "Votre record vous attend, venez le battre ! 🏆"
            )
            AppLanguage.ITALIAN -> listOf(
                "Vuoi rinfrescare la tua mente?" to "La matematica è il linguaggio dell'universo, pratichiamo! ✨",
                "BlitzMath ti aspetta!" to "Che ne dici di stimolare il tuo cervello con qualche domanda? 🧠",
                "Pronto per nuovi punteggi?" to "Il tuo record ti aspetta, vieni a superarlo! 🏆"
            )
            AppLanguage.PORTUGUESE -> listOf(
                "Quer refrescar sua mente?" to "A matemática é a linguagem do universo, vamos praticar! ✨",
                "BlitzMath espera por você!" to "Que tal estimular seu cérebro com algumas perguntas? 🧠",
                "Pronto para novos recordes?" to "Seu recorde espera por você, venha superá-lo! 🏆"
            )
            AppLanguage.HINDI -> listOf(
                "अपने मन को तरोताजा करना चाहते हैं?" to "गणित ब्रह्मांड की भाषा है, आइए अभ्यास करें! ✨",
                "ब्लिट्जमैथ आपका इंतजार कर रहा है!" to "कुछ त्वरित प्रश्नों के साथ अपने मस्तिष्क को उत्तेजित करें? 🧠",
                "नए स्कोर के लिए तैयार हैं?" to "आपका रिकॉर्ड इंतजार कर रहा है, आएं और इसे तोड़ें! 🏆"
            )
            AppLanguage.CHINESE -> listOf(
                "想让你的大脑保持清晰吗？" to "数学是宇宙的语言，让我们一起练习吧！ ✨",
                "BlitzMath 在等着你！" to "用几个快速的问题来刺激你的大脑如何？ 🧠",
                "准备好刷新高分了吗？" to "你的纪录正在等你来刷新！ 🏆"
            )
            else -> listOf(
                "Want to Refresh Your Mind?" to "Math is the language of the universe, let's practice! ✨",
                "BlitzMath is Waiting!" to "How about stimulating your brain with a few quick questions? 🧠",
                "Ready for New Scores?" to "Your record is waiting, come and beat it! 🏆",
                "Daily Math Practice!" to "Keep your mind sharp by spending just 2 minutes. 💪",
                "Back to the Magic of Math!" to "The numbers missed you, come and start solving! ⚡"
            )
        }
        return quotes.random()
    }
    fun showScientistEngagementNotification(language: AppLanguage, cardName: String, imageId: String?, isUnlocked: Boolean) {
        val messages = if (isUnlocked) {
            when (language) {
                AppLanguage.TURKISH -> listOf(
                    "Enerjim tam dolu! Yeni zaferler için seni bekliyorum ⚡",
                    "Takım hazır! Hadi oyuna girip beyin fırtınası yapalım 🌪️",
                    "Laboratuvar hazır, dehamızı konuşturma vakti geldi 👨‍🔬"
                )
                AppLanguage.ENGLISH -> listOf(
                    "My energy is full! I'm waiting for you for new victories ⚡",
                    "The team is ready! Let's get into the game and brainstorm 🌪️",
                    "The lab is ready, it's time to show our genius 👨‍🔬"
                )
                AppLanguage.SPANISH -> listOf(
                    "¡Mi energía está a tope! Te espero para nuevas victorias ⚡",
                    "¡El equipo está listo! Entremos al juego a pensar 🌪️",
                    "¡El laboratorio está listo, es hora de mostrar nuestro genio! 👨‍🔬"
                )
                AppLanguage.GERMAN -> listOf(
                    "Meine Energie ist voll! Ich warte auf dich für neue Siege ⚡",
                    "Das Team ist bereit! Lass uns ins Spiel einsteigen und nachdenken 🌪️",
                    "Das Labor ist bereit, es ist Zeit, unser Genie zu zeigen 👨‍🔬"
                )
                AppLanguage.FRENCH -> listOf(
                    "Mon énergie est à fond ! Je t'attends pour de nouvelles victoires ⚡",
                    "L'Equipe est prête ! Entrons dans le jeu et réfléchissons 🌪️",
                    "Le laboratoire est prêt, il est temps de montrer notre génie 👨‍🔬"
                )
                AppLanguage.ITALIAN -> listOf(
                    "La mia energia è al massimo! Ti aspetto per nuove vittorie ⚡",
                    "Il team è pronto! Entriamo in gioco e pensiamo 🌪️",
                    "Il laboratorio è pronto, è ora di mostrare il nostro genio 👨‍🔬"
                )
                AppLanguage.PORTUGUESE -> listOf(
                    "Minha energia está no máximo! Estou te esperando para novas vitórias ⚡",
                    "A equipe está pronta! Vamos entrar no jogo e pensar 🌪️",
                    "O laboratório está pronto, é hora de mostrar nosso gênio 👨‍🔬"
                )
                AppLanguage.HINDI -> listOf(
                    "मेरी ऊर्जा पूरी है! मैं नई जीत के लिए आपका इंतजार कर रहा हूँ ⚡",
                    "टीम तैयार है! खेल में आएं और सोचें 🌪️",
                    "प्रयोगशाला तैयार है, यह हमारी प्रतिभा दिखाने का समय है 👨‍🔬"
                )
                AppLanguage.CHINESE -> listOf(
                    "我的能量满满！我在等你迎接新的胜利 ⚡",
                    "团队准备好了！让我们进入游戏并进行头脑风暴 🌪️",
                    "实验室准备好了！是时候展示我们的天才了 👨‍🔬"
                )
                AppLanguage.RUSSIAN -> listOf(
                    "Моя энергия на пределе! Я жду тебя для новых побед ⚡",
                    "Команда готова! Давайте войдем в игру и подумаем 🌪️",
                    "Лаборатория готова! Пришло время показать наш гений 👨‍🔬"
                )
            }
        } else {
            when (language) {
                AppLanguage.TURKISH -> listOf(
                    "Hadi gel kilidimi aç, beraber zihnini geliştirelim! 🧠",
                    "Laboratuvarımda seni bekliyorum, dehamı keşfet! 🔬",
                    "Beni takımına kat ve rekorları kıralım! 🚀"
                )
                AppLanguage.ENGLISH -> listOf(
                    "Come unlock me, let's develop your mind together! 🧠",
                    "I'm waiting in my lab, discover my genius! 🔬",
                    "Add me to your team and let's break records! 🚀"
                )
                AppLanguage.SPANISH -> listOf(
                    "¡Ven a desbloquearme, desarrollemos tu mente juntos! 🧠",
                    "¡Te espero en mi laboratorio, descubre mi genio! 🔬",
                    "¡Súmame a tu equipo y rompamos récords! 🚀"
                )
                AppLanguage.GERMAN -> listOf(
                    "Komm und schalte mich frei, lass uns gemeinsam deinen Geist entwickeln! 🧠",
                    "Ich warte in meinem Labor, entdecke mein Genie! 🔬",
                    "Füge mich deinem Team hinzu und lass uns Rekorde brechen! 🚀"
                )
                AppLanguage.FRENCH -> listOf(
                    "Viens me débloquer, développons ton esprit ensemble ! 🧠",
                    "Je t'attends dans mon labo, découvre mon génie ! 🔬",
                    "Ajoute-moi à ton équipe et battons des records ! 🚀"
                )
                AppLanguage.ITALIAN -> listOf(
                    "Vieni a sbloccarmi, sviluppiamo insieme la tua mente! 🧠",
                    "Ti aspetto nel mio laboratorio, scopri il mio genio! 🔬",
                    "Aggiungimi al tuo team e battiamo i record! 🚀"
                )
                AppLanguage.PORTUGUESE -> listOf(
                    "Venha me desbloquear, vamos desenvolver sua mente juntos! 🧠",
                    "Estou esperando no meu laboratório, descubra minha genialidade! 🔬",
                    "Adicione-me ao seu time e vamos quebrar recordes! 🚀"
                )
                AppLanguage.HINDI -> listOf(
                    "आओ मुझे अनलॉक करें, साथ मिलकर अपना दिमाग विकसित करें! 🧠",
                    "मैं अपनी प्रयोगशाला में इंतजार कर रहा हूँ, मेरी प्रतिभा को खोजें! 🔬",
                    "मुझे अपनी टीम में शामिल करें और रिकॉर्ड तोड़ें! 🚀"
                )
                AppLanguage.CHINESE -> listOf(
                    "快来解锁我，一起开发你的大脑吧！ 🧠",
                    "我在实验室等你，发现我的天才吧！ 🔬",
                    "把我加入你的团队，一起打破纪录！ 🚀"
                )
                AppLanguage.RUSSIAN -> listOf(
                    "Приходи и разблокируй меня, давай развивать твой ум вместе! 🧠",
                    "Я жду в своей лаборатории, открой мой гений! 🔬",
                    "Добавь меня в свою команду и давай бить рекорды! 🚀"
                )
            }
        }
        val body = messages.random()
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, cardName.hashCode(), intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(cardName)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        
        if (imageId != null) {
            val resId = context.resources.getIdentifier(imageId, "drawable", context.packageName)
            if (resId != 0) {
                val largeIcon = android.graphics.BitmapFactory.decodeResource(context.resources, resId)
                builder.setLargeIcon(largeIcon)
                builder.setStyle(NotificationCompat.BigPictureStyle().bigPicture(largeIcon).bigLargeIcon(null as android.graphics.Bitmap?))
            }
        }
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(cardName.hashCode(), builder.build())
    }

}
