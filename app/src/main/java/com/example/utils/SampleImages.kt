package com.example.utils

import android.graphics.*
import android.util.Base64
import java.io.ByteArrayOutputStream

data class SampleImageItem(
    val id: String,
    val name: String,
    val description: String,
    val category: String,
    val beforeLabel: String = "SD 360p (Noisy)",
    val afterLabel: String = "HDR 8K (Enhanced)"
)

object SampleImages {
    val list = listOf(
        SampleImageItem("cyber_face", "Cyberpunk Face", "Blurry, noisy neon portrait restoration", "Face Restoration"),
        SampleImageItem("neon_city", "Cosmic Cityscape", "Low-light grainy skyline to sharp HDR view", "Noise Reduction"),
        SampleImageItem("macro_core", "Super-Res Lens", "Blurry macro license plate to clean details", "HD Upscale"),
        SampleImageItem("retro_classic", "Retro Sepia", "Scratched historic photo scratch clean-up", "Color Correction")
    )

    fun toBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    fun fromBase64(base64: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(base64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            null
        }
    }

    fun generate(id: String, isEnhanced: Boolean): Bitmap {
        val width = 600
        val height = 600
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        when (id) {
            "cyber_face" -> drawCyberFace(canvas, paint, width, height, isEnhanced)
            "neon_city" -> drawNeonCity(canvas, paint, width, height, isEnhanced)
            "macro_core" -> drawMacroCore(canvas, paint, width, height, isEnhanced)
            "retro_classic" -> drawRetroClassic(canvas, paint, width, height, isEnhanced)
            else -> drawDefault(canvas, paint, width, height, isEnhanced)
        }
        return bitmap
    }

    private fun drawCyberFace(canvas: Canvas, paint: Paint, w: Int, h: Int, isEnhanced: Boolean) {
        // Deep space dark background
        canvas.drawColor(Color.parseColor("#080512"))

        // Grid overlay
        paint.color = Color.parseColor("#1B1437")
        paint.strokeWidth = 2f
        for (i in 0..w step 40) {
            canvas.drawLine(i.toFloat(), 0f, i.toFloat(), h.toFloat(), paint)
            canvas.drawLine(0f, i.toFloat(), w.toFloat(), i.toFloat(), paint)
        }

        // Draw portrait background glow
        val radial = RadialGradient(
            w / 2f, h / 2f, h / 2f,
            Color.parseColor("#6400FF"), Color.TRANSPARENT,
            Shader.TileMode.CLAMP
        )
        paint.shader = radial
        paint.alpha = 150
        canvas.drawCircle(w / 2f, h / 2f, w / 2f, paint)
        paint.shader = null
        paint.alpha = 255

        // Draw character visor (sleek glassmorphic mask in center)
        val visorPath = Path().apply {
            moveTo(w * 0.3f, h * 0.35f)
            lineTo(w * 0.7f, h * 0.35f)
            lineTo(w * 0.75f, h * 0.5f)
            lineTo(w * 0.5f, h * 0.65f)
            lineTo(w * 0.25f, h * 0.5f)
            close()
        }

        // Helmet body
        val helmetGradient = LinearGradient(
            w * 0.3f, h * 0.25f, w * 0.7f, h * 0.75f,
            Color.parseColor("#1E1B4B"), Color.parseColor("#31104D"),
            Shader.TileMode.CLAMP
        )
        paint.shader = helmetGradient
        canvas.drawPath(Path().apply {
            moveTo(w * 0.25f, h * 0.25f)
            lineTo(w * 0.75f, h * 0.25f)
            lineTo(w * 0.82f, h * 0.55f)
            lineTo(w * 0.65f, h * 0.78f)
            lineTo(w * 0.35f, h * 0.78f)
            lineTo(w * 0.18f, h * 0.55f)
            close()
        }, paint)

        // Draw neon glowing patterns
        paint.shader = null
        if (isEnhanced) {
            // SHARP ENHANCED LOOK - Beautiful vivid colors & glowing highlights
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 6f
            paint.color = Color.parseColor("#00E5FF") // Neon Cyan
            canvas.drawPath(visorPath, paint)

            paint.color = Color.parseColor("#D500F9") // Neon Pink/Purple
            canvas.drawCircle(w * 0.5f, h * 0.44f, 40f, paint)

            paint.style = Paint.Style.FILL
            paint.color = Color.parseColor("#FFFFFF")
            canvas.drawCircle(w * 0.5f, h * 0.44f, 15f, paint)

            // Sharp flares
            paint.strokeWidth = 2f
            paint.color = Color.WHITE
            canvas.drawLine(w * 0.5f, h * 0.44f - 50f, w * 0.5f, h * 0.44f + 50f, paint)
            canvas.drawLine(w * 0.5f - 50f, h * 0.44f, w * 0.5f + 50f, h * 0.44f, paint)

            // Extra details
            paint.color = Color.parseColor("#00FF66")
            canvas.drawRoundRect(RectF(w * 0.35f, h * 0.72f, w * 0.65f, h * 0.75f), 10f, 10f, paint)

            // Labels
            paint.textSize = 28f
            paint.color = Color.parseColor("#00E5FF")
            paint.textAlign = Paint.Align.CENTER
            paint.isFakeBoldText = true
            canvas.drawText("⚡ FACE RESTORE: ACTIVED (8K UHD)", w / 2f, h * 0.9f, paint)
        } else {
            // BLURRY LOOK WITH HEAVY GRAIN AND ARTIFACTS
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 24f // fat blurred lines
            paint.color = Color.argb(80, 0, 229, 255)
            canvas.drawPath(visorPath, paint)

            paint.style = Paint.Style.FILL
            paint.color = Color.argb(100, 213, 0, 249)
            canvas.drawCircle(w * 0.5f, h * 0.44f, 40f, paint)

            // Blur layer: Draw lots of random green/purple dots for noise
            paint.style = Paint.Style.FILL
            val rand = java.util.Random(42)
            for (i in 0..1500) {
                paint.color = if (rand.nextBoolean()) Color.argb(80, 255, 0, 0) else Color.argb(80, 0, 255, 255)
                val rx = rand.nextFloat() * w
                val ry = rand.nextFloat() * h
                canvas.drawCircle(rx, ry, 5f + rand.nextFloat() * 10f, paint)
            }

            paint.style = Paint.Style.STROKE
            paint.color = Color.argb(160, 255, 255, 255)
            paint.strokeWidth = 8f
            canvas.drawRect(RectF(w * 0.2f, h * 0.2f, w * 0.8f, h * 0.8f), paint)

            // Scan lines
            paint.style = Paint.Style.FILL
            paint.color = Color.argb(40, 255, 255, 255)
            for (y in 0..h step 15) {
                canvas.drawRect(0f, y.toFloat(), w.toFloat(), y + 4f, paint)
            }

            paint.textSize = 24f
            paint.color = Color.RED
            paint.textAlign = Paint.Align.CENTER
            paint.isFakeBoldText = true
            canvas.drawText("⚠️ LOW-RES / COMPRESSED SOURCE [480p]", w / 2f, h * 0.9f, paint)
        }
    }

    private fun drawNeonCity(canvas: Canvas, paint: Paint, w: Int, h: Int, isEnhanced: Boolean) {
        // Starry violet background
        canvas.drawColor(Color.parseColor("#04040A"))

        // Distant stars / sky glow
        val radial = RadialGradient(
            w * 0.8f, h * 0.2f, h * 0.6f,
            Color.parseColor("#220A45"), Color.TRANSPARENT,
            Shader.TileMode.CLAMP
        )
        paint.shader = radial
        paint.alpha = 180
        canvas.drawCircle(w * 0.8f, h * 0.2f, w * 0.6f, paint)
        paint.shader = null
        paint.alpha = 255

        // Draw moon
        if (isEnhanced) {
            paint.color = Color.parseColor("#FFF3E0")
            canvas.drawCircle(w * 0.75f, h * 0.2f, 35f, paint)
            // Moon aura
            paint.color = Color.argb(40, 255, 243, 224)
            canvas.drawCircle(w * 0.75f, h * 0.2f, 60f, paint)
        } else {
            // Blurry pixelated moon
            paint.color = Color.argb(120, 255, 243, 224)
            canvas.drawCircle(w * 0.75f, h * 0.2f, 40f, paint)
        }

        // Draw neon buildings (silhouettes)
        val buildings = listOf(
            RectF(w * 0.1f, h * 0.4f, w * 0.28f, h.toFloat()),
            RectF(w * 0.3f, h * 0.3f, w * 0.52f, h.toFloat()),
            RectF(w * 0.55f, h * 0.45f, w * 0.75f, h.toFloat()),
            RectF(w * 0.78f, h * 0.35f, w * 0.94f, h.toFloat())
        )

        val bColors = listOf("#0D0B2E", "#140D2F", "#08061C", "#100924")
        val neonLines = listOf("#FF007F", "#39FF14", "#00FFFF", "#FFFF00")

        buildings.forEachIndexed { idx, rect ->
            paint.color = Color.parseColor(bColors[idx])
            paint.style = Paint.Style.FILL
            canvas.drawRect(rect, paint)

            if (isEnhanced) {
                // Clear neon lining
                paint.color = Color.parseColor(neonLines[idx])
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 4f
                canvas.drawRect(rect, paint)

                // Precise windows
                paint.color = Color.parseColor("#FFFFD0")
                paint.style = Paint.Style.FILL
                for (wy in (rect.top + 20).toInt()..h - 20 step 45) {
                    for (wx in (rect.left + 15).toInt()..rect.right.toInt() - 20 step 30) {
                        canvas.drawRect(wx.toFloat(), wy.toFloat(), wx + 10f, wy + 15f, paint)
                    }
                }
            } else {
                // Fuzzy/Noisy building edge
                paint.color = Color.argb(100, 255, 0, 128)
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 15f
                canvas.drawRect(rect, paint)

                // Grainy noise representation
                paint.style = Paint.Style.FILL
                val rand = java.util.Random((100 + idx).toLong())
                paint.color = Color.argb(180, 200, 180, 100)
                for (i in 0..50) {
                    val wx = rect.left + rand.nextFloat() * (rect.width() - 10f)
                    val wy = rect.top + rand.nextFloat() * (rect.height() - 20f)
                    canvas.drawCircle(wx, wy, 4f, paint)
                }
            }
        }

        if (!isEnhanced) {
            // Apply severe camera sensor noise (color noise)
            val rand = java.util.Random(999)
            paint.style = Paint.Style.FILL
            for (i in 0..3000) {
                val value = rand.nextInt(3)
                paint.color = when(value) {
                    0 -> Color.argb(70, 255, 0, 0)
                    1 -> Color.argb(70, 0, 0, 255)
                    else -> Color.argb(70, 0, 255, 0)
                }
                canvas.drawCircle(rand.nextFloat() * w, rand.nextFloat() * h, 3f + rand.nextFloat() * 5f, paint)
            }
            paint.textSize = 24f
            paint.color = Color.parseColor("#FF5252")
            paint.textAlign = Paint.Align.CENTER
            paint.isFakeBoldText = true
            canvas.drawText("⚠️ NOISY HIGH-ISO CAMERA SHOT [50% NOISE]", w / 2f, h * 0.92f, paint)
        } else {
            // Sharp crystal labels
            paint.textSize = 28f
            paint.color = Color.parseColor("#39FF14")
            paint.textAlign = Paint.Align.CENTER
            paint.isFakeBoldText = true
            canvas.drawText("✨ ISO NOISE REMOVED (SUPER HDR 8K)", w / 2f, h * 0.92f, paint)
        }
    }

    private fun drawMacroCore(canvas: Canvas, paint: Paint, w: Int, h: Int, isEnhanced: Boolean) {
        // Deep emerald background
        canvas.drawColor(Color.parseColor("#040C0B"))

        // Draw radial zoom lines
        paint.color = Color.parseColor("#1B2925")
        paint.strokeWidth = 2f
        for (i in 0..360 step 15) {
            val angle = i * Math.PI / 180.0
            val x2 = w / 2 + Math.cos(angle) * w
            val y2 = h / 2 + Math.sin(angle) * h
            canvas.drawLine(w/2f, h/2f, x2.toFloat(), y2.toFloat(), paint)
        }

        // Draw License plate / text frame in center
        val rect = RectF(w * 0.15f, h * 0.35f, w * 0.85f, h * 0.65f)
        paint.color = Color.parseColor("#F5F5F5")
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(rect, 15f, 15f, paint)

        // Draw inner border
        paint.color = Color.parseColor("#1A237E")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 10f
        canvas.drawRoundRect(rect, 15f, 15f, paint)

        // Draw country/state banner
        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#1A237E")
        canvas.drawRect(RectF(w * 0.15f, h * 0.35f, w * 0.85f, h * 0.43f), paint)

        paint.color = Color.WHITE
        paint.textSize = 22f
        paint.textAlign = Paint.Align.CENTER
        paint.isFakeBoldText = true
        canvas.drawText("SUPER RESOLUTION 8K CAPABLE", w / 2f, h * 0.41f, paint)

        // Draw license numbers: "GEMINI 3"
        paint.color = Color.BLACK
        paint.textAlign = Paint.Align.CENTER

        if (isEnhanced) {
            paint.textSize = 80f
            paint.style = Paint.Style.FILL
            canvas.drawText("GEM-981", w / 2f, h * 0.58f, paint)

            paint.textSize = 24f
            paint.color = Color.parseColor("#00E5FF")
            paint.isFakeBoldText = true
            canvas.drawText("✦ PIXEL STITCHED SUPER-RESOLUTION (16x UPSCALED)", w / 2f, h * 0.88f, paint)
        } else {
            // Blurry license values
            paint.textSize = 80f
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 18f // blurry thick lines
            paint.color = Color.argb(120, 100, 100, 100)
            canvas.drawText("G E M", w / 2f, h * 0.58f, paint)

            // Blur noise block overlay
            paint.style = Paint.Style.FILL
            paint.color = Color.argb(150, 245, 245, 245)
            canvas.drawCircle(w * 0.5f, h * 0.53f, 75f, paint)

            paint.textSize = 24f
            paint.color = Color.RED
            paint.isFakeBoldText = true
            canvas.drawText("⚠️ LOW DENSITY MULTI-OCTET ARTIFACTS", w / 2f, h * 0.88f, paint)
        }
    }

    private fun drawRetroClassic(canvas: Canvas, paint: Paint, w: Int, h: Int, isEnhanced: Boolean) {
        // Sepia yellow background
        if (isEnhanced) {
            // Premium rich warm tone
            canvas.drawColor(Color.parseColor("#F5E6CC"))

            val radial = RadialGradient(
                w / 2f, h / 2f, h * 0.7f,
                Color.TRANSPARENT, Color.parseColor("#8C6A3B"),
                Shader.TileMode.CLAMP
            )
            paint.shader = radial
            paint.alpha = 150
            canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
            paint.shader = null
            paint.alpha = 255

            // Draw clean circular portrait illustration
            paint.color = Color.parseColor("#3E2723")
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 5f
            canvas.drawCircle(w / 2f, h / 2f - 40f, 130f, paint)

            // Dynamic classic shape drawing - an elegant antique flower vase
            paint.style = Paint.Style.FILL
            val vasePath = Path().apply {
                moveTo(w * 0.45f, h * 0.35f)
                quadTo(w * 0.35f, h * 0.45f, w * 0.4f, h * 0.55f)
                lineTo(w * 0.6f, h * 0.55f)
                quadTo(w * 0.65f, h * 0.45f, w * 0.55f, h * 0.35f)
                close()
            }
            canvas.drawPath(vasePath, paint)
            canvas.drawRect(RectF(w * 0.38f, h * 0.55f, w * 0.62f, h * 0.58f), paint)

            paint.textSize = 28f
            paint.textAlign = Paint.Align.CENTER
            paint.color = Color.parseColor("#3E2723")
            paint.isFakeBoldText = true
            canvas.drawText("✦ HISTORICAL SCRATCH RESTORATION", w / 2f, h * 0.85f, paint)
        } else {
            // Highly faded, gray/sepia washed out background
            canvas.drawColor(Color.parseColor("#CFC4B2"))

            // Retro scratches
            paint.color = Color.parseColor("#9C8E7E")
            paint.strokeWidth = 3f

            // Draw lots of vertical / slanted scratches
            val rand = java.util.Random(10)
            for (i in 0..15) {
                val sx = rand.nextFloat() * w
                val sy = rand.nextFloat() * h
                canvas.drawLine(sx, sy, sx + (-30 + rand.nextInt(60)), sy + (100 + rand.nextInt(200)), paint)
            }

            // Big white spot artifacts (decay)
            paint.color = Color.argb(120, 255, 255, 255)
            for (i in 0..8) {
                canvas.drawCircle(rand.nextFloat() * w, rand.nextFloat() * h, 15f + rand.nextFloat()*25f, paint)
            }

            // Blur drawing outline
            paint.color = Color.argb(80, 62, 39, 35)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 15f
            canvas.drawCircle(w / 2f, h / 2f - 40f, 130f, paint)

            paint.textSize = 24f
            paint.textAlign = Paint.Align.CENTER
            paint.color = Color.RED
            paint.isFakeBoldText = true
            canvas.drawText("⚠️ SCRATCHED CHIP FILM (1942)", w / 2f, h * 0.85f, paint)
        }
    }

    private fun drawDefault(canvas: Canvas, paint: Paint, w: Int, h: Int, isEnhanced: Boolean) {
        canvas.drawColor(if (isEnhanced) Color.BLUE else Color.DKGRAY)
        paint.color = Color.WHITE
        paint.textSize = 30f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(if (isEnhanced) "Enhanced 8K" else "Original Lofi", w/2f, h/2f, paint)
    }
}
