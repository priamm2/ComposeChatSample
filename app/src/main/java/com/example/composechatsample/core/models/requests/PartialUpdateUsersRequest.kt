package com.example.composechatsample.core.models.requests

import com.example.composechatsample.core.models.dto.PartialUpdateUserDto
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class PartialUpdateUsersRequest(val users: List<PartialUpdateUserDto>)