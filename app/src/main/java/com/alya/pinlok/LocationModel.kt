package com.alya.pinlok

import android.provider.ContactsContract

data class LocationModel(
    val id: Int,
    val name: String,
    val note: String,
    val lat: Double,
    val lng: Double,
    val photo: String // path foto
)
