package com.mawelly.blitzmath.ui.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import androidx.core.content.FileProvider
import com.mawelly.blitzmath.localization.Strings
import java.io.File
import java.io.FileOutputStream

object ShareManager {
    /**
     * Captures a screenshot of the current activity and launches the share intent
     * with an image and promotional text.
     */
    fun shareScoreWithScreenshot(activity: Activity, score: Int, checkpoint: Int) {
        val bitmap = captureScreenshot(activity)
        val uri = saveBitmapToCache(activity, bitmap)
        
        if (uri != null) {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, Strings.getShareMessage(score, checkpoint))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            activity.startActivity(Intent.createChooser(shareIntent, Strings.shareScore))
        }
    }

    private fun captureScreenshot(activity: Activity): Bitmap {
        val view = activity.window.decorView
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    private fun saveBitmapToCache(context: Context, bitmap: Bitmap): Uri? {
        return try {
            val cachePath = File(context.cacheDir, "blitzmath_shares")
            cachePath.mkdirs()
            val file = File(cachePath, "blitzmath_score_share.png")
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
