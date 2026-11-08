package com.hackernewspulse.ui

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.hackernewspulse.data.remote.responses.StoryResponse
import com.hackernewspulse.ui.theme.secondaryAccent
import java.util.concurrent.TimeUnit

@Composable
fun StoryCard(
    story: StoryResponse,
    index: Int,
    onItemClick: (String) -> Unit,
    onFetchDetails: suspend (Long) -> StoryResponse?
) {
    var storyState by remember(story.id) {
        mutableStateOf(if (!story.title.isNullOrBlank() && !story.url.isNullOrBlank()) story else null)
    }

    LaunchedEffect(story.id) {
        if (storyState == null) {
            try {
                val detail = onFetchDetails(story.id)
                if (detail != null) {
                    storyState = detail
                }
            } catch (e: Exception) {
                // Keep skeleton if transient error occurs
            }
        }
    }

    val currentStory = storyState
    if (currentStory == null) {
        StoryCardSkeleton()
    } else {
        RealStoryCard(story = currentStory, index = index, onItemClick = onItemClick)
    }
}

@Composable
private fun RealStoryCard(story: StoryResponse, index: Int, onItemClick: (String) -> Unit) {
    val isPrimary = index % 2 == 0
    val accentColor = if (isPrimary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryAccent

    val domain = remember(story.url) {
        story.url?.let { Uri.parse(it).host?.removePrefix("www.") }
    }
    val timeAgo = remember(story.time) {
        story.time?.let { getRelativeTime(it) } ?: ""
    }
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { story.url?.let { onItemClick(it) } },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawRect(
                        color = accentColor,
                        size = Size(width = 4.dp.toPx(), height = size.height)
                    )
                }
                .padding(start = 20.dp, top = 16.dp, end = 16.dp, bottom = 16.dp)
        ) {
            Text(
                text = story.title ?: "No title",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 22.sp
                ),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "by ${story.by ?: "unknown"}",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = accentColor,
                        fontWeight = FontWeight.Medium
                    )
                )
                if (domain != null) {
                    Text(
                        text = " • ",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    val faviconRequest = remember(domain) {
                        ImageRequest.Builder(context)
                            .data("https://www.google.com/s2/favicons?sz=64&domain=$domain")
                            .crossfade(true)
                            .build()
                    }
                    AsyncImage(
                        model = faviconRequest,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = domain,
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🕒 $timeAgo",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                val score = story.score ?: 0
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = accentColor.copy(alpha = 0.2f),
                    contentColor = accentColor
                ) {
                    Text(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        text = "$score ${if (score == 1) "pt" else "pts"}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun StoryCardSkeleton() {
    val barColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawRect(
                        color = barColor,
                        size = Size(width = 4.dp.toPx(), height = size.height)
                    )
                }
                .padding(start = 20.dp, top = 16.dp, end = 16.dp, bottom = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(20.dp)
                    .background(barColor, RoundedCornerShape(4.dp))
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .height(14.dp)
                    .background(barColor, RoundedCornerShape(4.dp))
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(12.dp)
                        .background(barColor, RoundedCornerShape(4.dp))
                )
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(20.dp)
                        .background(barColor, RoundedCornerShape(4.dp))
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
