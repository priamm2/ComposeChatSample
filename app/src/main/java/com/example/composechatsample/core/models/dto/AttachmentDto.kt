package com.example.composechatsample.core.models.dto

import com.example.composechatsample.core.StreamHandsOff
import com.squareup.moshi.JsonClass

@StreamHandsOff(
    reason = "Field names can't be changed because [CustomObjectDtoAdapter] class uses reflections to add/remove " +
        "content of [extraData] map",
)
@JsonClass(generateAdapter = true)
data class AttachmentDto(
    val asset_url: String?,
    val author_name: String?,
    val author_link: String?,
    val fallback: String?,
    val file_size: Int = 0,
    val image: String?,
    val image_url: String?,
    val mime_type: String?,
    val name: String?,
    val og_scrape_url: String?,
    val text: String?,
    val thumb_url: String?,
    val title: String?,
    val title_link: String?,
    val type: String?,
    val url: String?,
    val original_height: Int?,
    val original_width: Int?,

    val extraData: Map<String, Any>,
) : ExtraDataDto