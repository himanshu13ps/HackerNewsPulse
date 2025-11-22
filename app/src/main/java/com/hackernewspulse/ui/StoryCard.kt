package com.hackernewspulse.ui

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.hackernewspulse.data.remote.responses.StoryResponse
import java.util.concurrent.TimeUnit

@Composable
fun StoryCard(story: StoryResponse, onItemClick: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { story.url?.let { onItemClick(it) } },
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = story.title ?: "No title",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "by ${story.by}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                )
                story.url?.let {
                    val domain = Uri.parse(it).host?.removePrefix("www.")
                    if (domain != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = "https://www.google.com/s2/favicons?sz=128&domain=$domain",
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = domain,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val timeAgo = story.time?.let { getRelativeTime(it) } ?: ""
                Text(
                    text = "⏰ $timeAgo",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                )
                val score = story.score ?: 0
                Text(
                    text = "$score ${if (score == 1) "point" else "points"}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                )
            }
        }
    }
}

private fun getRelativeTime(epochSeconds: Long): String {
    val now = System.currentTimeMillis()
    val time = TimeUnit.SECONDS.toMillis(epochSeconds)
    val diff = now - time

    val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
    val hours = TimeUnit.MILLISECONDS.toHours(diff)
    val days = TimeUnit.MILLISECONDS.toDays(diff)

    return when {
        minutes < 1 -> "just now"
        minutes < 60 -> "$minutes ${if (minutes == 1L) "minute" else "minutes"} ago"
        hours < 24 -> "$hours ${if (hours == 1L) "hour" else "hours"} ago"
        else -> "$days ${if (days == 1L) "day" else "days"} ago"
    }
}
