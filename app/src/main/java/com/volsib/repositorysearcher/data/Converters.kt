package com.volsib.repositorysearcher.data

import androidx.room.TypeConverter
import com.volsib.repositorysearcher.models.Owner

class Converters {
    @TypeConverter
    fun fromOwner(owner: Owner): String? {
        return owner.login
    }

    @TypeConverter
    fun toOwner(login: String): Owner {
        return Owner(login, null, null)
    }
}
