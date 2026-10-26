package com.example.budgetboss.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.budgetboss.core.util.formatAmount
import com.example.budgetboss.data.BudgetGoalEntity
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.max

data class ChartSegment(
    val amount: Double,
    val color: Color,
    val label: String,
)

data class ChartDay(
    val date: LocalDate,
    val segments: List<ChartSegment>,
) {
    val total: Double = segments.sumOf { it.amount }
}

@Composable
fun DailySpendingChart(
    chartDays: List<ChartDay>,
    budgetGoal: BudgetGoalEntity,
    modifier: Modifier = Modifier,
    onDaySelected: ((ChartDay) -> Unit)? = null,
) {
    val daysInMonth = YearMonth.now().lengthOfMonth()
    val dailyMin = budgetGoal.minMonthlyGoal / daysInMonth
    val dailyMax = budgetGoal.maxMonthlyGoal / daysInMonth
    val maxValue = max(
        chartDays.maxOfOrNull { it.total.toFloat() } ?: 1f,
        (dailyMax * 1.2).toFloat().coerceAtLeast(1f),
    )

    val minLineColor = Color(0xFF15803D)
    val maxLineColor = Color(0xFFDC2626)

    Column(modifier = modifier) {
        Card(
            shape = FeatureCardShape,
            border = SubtleBorder(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Spacer(modifier = Modifier.size(width = 12.dp, height = 3.dp).background(minLineColor, CircleShape))
                        Text("Min R${formatAmount(dailyMin)}/day", style = MaterialTheme.typography.labelSmall, color = minLineColor)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Spacer(modifier = Modifier.size(width = 12.dp, height = 3.dp).background(maxLineColor, CircleShape))
                        Text("Max R${formatAmount(dailyMax)}/day", style = MaterialTheme.typography.labelSmall, color = maxLineColor)
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                    ) {
                        if (chartDays.isEmpty()) return@Canvas

                        val topPadding = 16.dp.toPx()
                        val bottomPadding = 20.dp.toPx()
                        val chartHeight = (size.height - topPadding - bottomPadding).coerceAtLeast(1f)
                        val baselineY = topPadding + chartHeight
                        val barAreaWidth = size.width
                        val barWidth = (barAreaWidth / (chartDays.size * 1.6f)).coerceAtLeast(10.dp.toPx())
                        val spacing = (barAreaWidth - (barWidth * chartDays.size)) / (chartDays.size + 1)

                        val minY = baselineY - ((dailyMin.toFloat() / maxValue) * chartHeight)
                        drawLine(
                            color = minLineColor.copy(alpha = 0.7f),
                            start = androidx.compose.ui.geometry.Offset(0f, minY),
                            end = androidx.compose.ui.geometry.Offset(barAreaWidth, minY),
                            strokeWidth = 2.dp.toPx(),
                            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                                floatArrayOf(10.dp.toPx(), 6.dp.toPx()),
                            ),
                        )

                        val maxY = baselineY - ((dailyMax.toFloat() / maxValue) * chartHeight)
                        drawLine(
                            color = maxLineColor.copy(alpha = 0.7f),
                            start = androidx.compose.ui.geometry.Offset(0f, maxY),
                            end = androidx.compose.ui.geometry.Offset(barAreaWidth, maxY),
                            strokeWidth = 2.dp.toPx(),
                            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                                floatArrayOf(10.dp.toPx(), 6.dp.toPx()),
                            ),
                        )

                        chartDays.forEachIndexed { index, day ->
                            var top = baselineY
                            val x = spacing + index * (barWidth + spacing)
                            day.segments.forEach { segment ->
                                val height = ((segment.amount.toFloat() / maxValue) * chartHeight).coerceAtLeast(0f)
                                drawRect(
                                    color = segment.color,
                                    topLeft = androidx.compose.ui.geometry.Offset(x, top - height),
                                    size = androidx.compose.ui.geometry.Size(barWidth, height),
                                )
                                top -= height
                            }
                        }
                    }
                }
                if (chartDays.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                    ) {
                        chartDays.forEach { day ->
                            Text(
                                text = day.date.dayOfMonth.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                modifier = if (onDaySelected != null) {
                                    Modifier.clickable { onDaySelected(day) }
                                } else {
                                    Modifier
                                },
                            )
                        }
                    }
                }
            }
        }
        if (onDaySelected != null && chartDays.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tap a day number to view the exact spend and category breakdown.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(chartDays.flatMap { it.segments.map { segment -> segment.label to segment.color } }.distinctBy { it.first }) { legend ->
                AssistChip(
                    onClick = {},
                    label = { Text(legend.first) },
                    leadingIcon = {
                        Spacer(
                            modifier = Modifier
                                .size(12.dp)
                                .background(legend.second, CircleShape),
                        )
                    },
                )
            }
        }
    }
}

