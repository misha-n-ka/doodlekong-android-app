package ru.mkirilkin.doodlekong.data.remote.responses

import com.google.gson.annotations.SerializedName

data class BasicApiResponse(
    @SerializedName("successful") val successful: Boolean,
    @SerializedName("message") val message: String? = null
)
