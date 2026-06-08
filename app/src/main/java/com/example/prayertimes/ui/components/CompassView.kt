package com.example.prayertimes.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import com.example.prayertimes.theme.Gold500
import com.example.prayertimes.theme.Teal400
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.abs

@Composable
fun CompassView(azimuth: Float, qiblaBearing: Double, modifier: Modifier = Modifier) {
    val primaryColor    = MaterialTheme.colorScheme.primary
    val surfaceVariant  = MaterialTheme.colorScheme.surfaceVariant
    val onSurface       = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    val animatedAzimuth by animateFloatAsState(
        targetValue = azimuth,
        animationSpec = tween(durationMillis = 500, easing = androidx.compose.animation.core.EaseInOutSine),
        label = "compassAzimuth"
    )

    // Check if device is within ±5° of Qibla direction
    val qiblaRelative = ((qiblaBearing - azimuth + 360) % 360).toFloat()
    val isPointingToQibla = qiblaRelative < 5f || qiblaRelative > 355f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.minDimension / 2 - 24.dp.toPx()

            // Outer ring border
            drawCircle(
                color = if (isPointingToQibla) Teal400.copy(alpha = 0.6f) else surfaceVariant,
                radius = radius + 12.dp.toPx(),
                center = center,
                style = Stroke(width = if (isPointingToQibla) 3.dp.toPx() else 1.5.dp.toPx())
            )
            // Background fill
            drawCircle(
                color = surfaceVariant.copy(alpha = 0.15f),
                radius = radius,
                center = center
            )

            // ROTATING RING: rotates by -animatedAzimuth (so N points to where north is)
            rotate(-animatedAzimuth, pivot = center) {

                // Degree tick marks
                for (i in 0 until 360 step 10) {
                    val angle = Math.toRadians(i.toDouble())
                    val isCardinal = i % 90 == 0
                    val is30 = i % 30 == 0
                    val startR = radius - when {
                        isCardinal -> 18.dp.toPx()
                        is30       -> 12.dp.toPx()
                        else       -> 7.dp.toPx()
                    }
                    val endR = radius - 2.dp.toPx()
                    drawLine(
                        color = when {
                            isCardinal -> onSurface
                            is30       -> onSurfaceVariant.copy(alpha = 0.6f)
                            else       -> onSurfaceVariant.copy(alpha = 0.25f)
                        },
                        start = Offset(
                            center.x + (startR * sin(angle)).toFloat(),
                            center.y - (startR * cos(angle)).toFloat()
                        ),
                        end = Offset(
                            center.x + (endR * sin(angle)).toFloat(),
                            center.y - (endR * cos(angle)).toFloat()
                        ),
                        strokeWidth = if (isCardinal) 3.dp.toPx() else 1.5.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }

                // Cardinal labels painted ON the ring (they rotate with it)
                drawCardinalLabels(center, radius, onSurface, primaryColor)

                // Qibla marker fixed at qiblaBearing angle on the ring
                val qiblaAngleRad = Math.toRadians(qiblaBearing)
                val qiblaColor = if (isPointingToQibla) Teal400 else Gold500
                drawQiblaMarker(
                    center = center,
                    radius = radius - 28.dp.toPx(),
                    angleDeg = qiblaBearing.toFloat(),
                    color = qiblaColor,
                    size = 22.dp.toPx()
                )
            }

            // FIXED red North needle pointing up (does NOT rotate)
            drawNorthNeedle(center, radius + 14.dp.toPx())
            

            // Fixed center dot
            drawCircle(color = primaryColor, radius = 7.dp.toPx(), center = center)
            drawCircle(color = onSurface, radius = 3.dp.toPx(), center = center)
        }
    }
}

private fun DrawScope.drawCardinalLabels(
    center: Offset, radius: Float, textColor: Color, northColor: Color
) {
    val labelRadius = radius - 36.dp.toPx()
    val labels = listOf(
        Triple(0f,   "N", northColor),
        Triple(90f,  "E", textColor),
        Triple(180f, "S", textColor),
        Triple(270f, "W", textColor)
    )
    for ((degrees, label, color) in labels) {
        val angle = Math.toRadians(degrees.toDouble())
        val x = center.x + (labelRadius * sin(angle)).toFloat()
        val y = center.y - (labelRadius * cos(angle)).toFloat()
        drawContext.canvas.nativeCanvas.drawText(
            label, x, y + 7.dp.toPx(),
            android.graphics.Paint().apply {
                this.color = android.graphics.Color.argb(
                    (color.alpha * 255).toInt(),
                    (color.red * 255).toInt(),
                    (color.green * 255).toInt(),
                    (color.blue * 255).toInt()
                )
                textSize = 20.dp.toPx()
                textAlign = android.graphics.Paint.Align.CENTER
                isFakeBoldText = true
                isAntiAlias = true
            }
        )
    }
}

private fun DrawScope.drawQiblaMarker(
    center: Offset, radius: Float, angleDeg: Float, color: Color, size: Float
) {
    val angleRad = Math.toRadians(angleDeg.toDouble())
    val x = center.x + (radius * sin(angleRad)).toFloat()
    val y = center.y - (radius * cos(angleRad)).toFloat()

    val path = Path().apply {
        moveTo(0f, -size)
        lineTo(size * 0.55f, size * 0.6f)
        lineTo(0f, size * 0.25f)
        lineTo(-size * 0.55f, size * 0.6f)
        close()
    }

    drawContext.canvas.save()
    drawContext.canvas.translate(x, y)
    drawContext.canvas.rotate(angleDeg)
    drawPath(path, color)
    drawPath(path, color.copy(alpha = 0.5f), style = Stroke(width = 2.dp.toPx()))
    drawContext.canvas.restore()


}

private fun DrawScope.drawNorthNeedle(center: Offset, outerRadius: Float) {
    val x = center.x
    val y = center.y - outerRadius   // Top of compass ring
    val size = 14.dp.toPx()
    val path = Path().apply {
        moveTo(x, y - size)
        lineTo(x + size * 0.45f, y + size * 0.8f)
        lineTo(x, y + size * 0.3f)
        lineTo(x - size * 0.45f, y + size * 0.8f)
        close()
    }
    drawPath(path, Color.Red)
    drawPath(path, Color.White, style = Stroke(width = 1.dp.toPx()))
}
