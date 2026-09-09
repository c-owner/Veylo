package app.veylo.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [WallpaperProjectEntity::class], version = 1, exportSchema = true)
abstract class VeyloDatabase : RoomDatabase() {
    abstract fun wallpaperProjectDao(): WallpaperProjectDao

    companion object {
        fun create(context: Context): VeyloDatabase = Room.databaseBuilder(
            context.applicationContext,
            VeyloDatabase::class.java,
            "veylo.db",
        ).build()
    }
}
