package com.volsib.repositorysearcher.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.volsib.repositorysearcher.models.Repo
import com.volsib.repositorysearcher.models.SearchHistory
import com.volsib.repositorysearcher.models.User

@Database(
    entities = [Repo::class, User::class, SearchHistory::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class RepoDatabase : RoomDatabase() {
    abstract fun repoDao(): RepoDao
    abstract fun userDao(): UserDao
    abstract fun searchHistoryDao(): SearchHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: RepoDatabase? = null

        fun getDatabase(context: Context): RepoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RepoDatabase::class.java,
                    "repo_database"
                )
                .fallbackToDestructiveMigration() // Для простоты, в продакшене нужна миграция
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}