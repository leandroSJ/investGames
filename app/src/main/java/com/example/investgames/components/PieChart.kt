package com.example.investgames.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun PieChart(
    percent: Float,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.LightGray,
    fillColor: Color = Color(0xFF4CAF50)
) {
    val sweep = 360f * percent.coerceIn(0f, 1f)

    Box(
        modifier = modifier.size(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Fundo (círculo completo)
            drawArc(
                color = backgroundColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = true
            )
            // Parte preenchida (percentual)
            drawArc(
                color = fillColor,
                startAngle = -90f,
                sweepAngle = sweep,
                useCenter = true
            )
        }

        // Texto central com o percentual (ex: "0%")
        Text(
            text = "${(percent.coerceIn(0f, 1f) * 100).toInt()}%",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
    }
}
