package com.hackernewspulse.data.remote.responses

import kotlinx.serialization.Serializable

@Serializable
data class StoryResponse(
    val id: Long,
    val by: String? = null,
    val descendants: Int? = null,
    val kids: List<Long>? = null,
    val score: Int? = null,
    val time: Long? = null,
    val title: String? = null,
    val type: String? = null,
    val url: String? = null
)