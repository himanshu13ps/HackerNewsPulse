package com.hackernewspulse.ui

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.hackernewspulse.data.paging.StoryType
import com.hackernewspulse.ui.theme.DarkBlue
import com.hackernewspulse.ui.theme.LightBlue
import com.hackernewspulse.viewmodel.MainViewModel

@Composable
fun MainScreen(viewModel: MainViewModel = hiltViewModel()) {
    val stories = viewModel.stories.collectAsLazyPagingItems()
    val storyType by viewModel.storyType.collectAsState()
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(LightBlue, DarkBlue)
                )
            )
    ) {
        Column {
            TopBar(storyType = storyType, onTabSelected = { viewModel.setStoryType(it) })

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(stories.itemCount) {
                    index ->
                    stories[index]?.let { story ->
                        StoryCard(story = story, onItemClick = { url ->
                            openCustomTab(context, url)
                        })
                    }
                }

                stories.apply {
                    when {
                        loadState.refresh is LoadState.Loading -> {
                            item {
                                Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        CircularProgressIndicator()
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("Loading stories...", color = Color.White)
                                    }
                                }
                            }
                        }
                        loadState.append is LoadState.Loading -> {
                            item {
                                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.Center) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Loading more...", color = Color.White)
                                }
                            }
                        }
                        loadState.refresh is LoadState.Error -> {
                            item { Text("Error loading stories", color = Color.Red) }
                        }
                        loadState.append is LoadState.Error -> {
                            item { Text("Error loading more stories", color = Color.Red) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TopBar(storyType: StoryType, onTabSelected: (StoryType) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Hacker News",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 28.sp
            )
        )
        StoryTypeSelector(selectedType = storyType, onTabSelected = onTabSelected)
    }
}

@Composable
fun StoryTypeSelector(selectedType: StoryType, onTabSelected: (StoryType) -> Unit) {
    val types = StoryType.values()
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Color.Black.copy(alpha = 0.2f))
            .padding(4.dp)
    ) {
        types.forEach { type ->
            val isSelected = type == selectedType
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (isSelected) LightBlue.copy(alpha = 0.5f) else Color.Transparent)
                    .clickable { onTabSelected(type) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = type.name.toLowerCase().capitalize(),
                    color = Color.White,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

fun openCustomTab(context: Context, url: String) {
    val builder = CustomTabsIntent.Builder()
    val customTabsIntent = builder.build()
    customTabsIntent.launchUrl(context, Uri.parse(url))
}
