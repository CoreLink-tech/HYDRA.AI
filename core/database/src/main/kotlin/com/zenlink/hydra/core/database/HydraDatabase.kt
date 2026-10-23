package com.zenlink.hydra.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.zenlink.hydra.core.database.dao.ChatDao
import com.zenlink.hydra.core.database.dao.MessageDao
import com.zenlink.hydra.core.database.dao.ModelDao
import com.zenlink.hydra.core.database.dao.SettingDao
import com.zenlink.hydra.core.database.dao.ShareDao
import com.zenlink.hydra.core.database.entities.ChatEntity
import com.zenlink.hydra.core.database.entities.MessageEntity
import com.zenlink.hydra.core.database.entities.ModelEntity
import com.zenlink.hydra.core.database.entities.SettingEntity
import com.zenlink.hydra.core.database.entities.ShareEntity
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory

@Database(
    entities = [
        ChatEntity::class,
        MessageEntity::class,
        ModelEntity::class,
        SettingEntity::class,
        ShareEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class HydraDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun messageDao(): MessageDao
    abstract fun modelDao(): ModelDao
    abstract fun settingDao(): SettingDao
    abstract fun shareDao(): ShareDao

    companion object {
        private const val DB_NAME = "hydra.db"

        /**
         * Opens the SQLCipher-encrypted database. Every page on disk is encrypted with a key
         * derived from the Keystore-wrapped passphrase in [KeyProvider].
         */
        fun open(context: Context): HydraDatabase {
            val passphrase = KeyProvider(context).passphrase()
            return Room.databaseBuilder(context.applicationContext, HydraDatabase::class.java, DB_NAME)
                .openHelperFactory(SupportOpenHelperFactory(passphrase))
                .addMigrations(*migrations)
                .build()
        }

        /** Add a Migration here for every schema bump. Never use fallbackToDestructiveMigration. */
        private val migrations: Array<Migration> = emptyArray()
    }
}
