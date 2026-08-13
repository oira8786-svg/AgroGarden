package com.agrogarden.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Crop::class, Seed::class, Fertilizer::class, Irrigation::class, Sale::class, Harvest::class, Expense::class, Treatment::class, Task::class, Note::class, FavoritePlant::class],
    version = 2,
    exportSchema = true
)
abstract class AppDb : RoomDatabase() {
    abstract fun crops(): CropDao
    abstract fun seeds(): SeedDao
    abstract fun fertilizers(): FertilizerDao
    abstract fun irrigations(): IrrigationDao
    abstract fun sales(): SaleDao
    abstract fun harvests(): HarvestDao
    abstract fun expenses(): ExpenseDao
    abstract fun treatments(): TreatmentDao
    abstract fun tasks(): TaskDao
    abstract fun notes(): NoteDao
    abstract fun favorites(): FavoritePlantDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS notes (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, title TEXT NOT NULL, text TEXT NOT NULL, createdAt INTEGER NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS favorite_plants (plantId TEXT NOT NULL, addedAt INTEGER NOT NULL, PRIMARY KEY(plantId))")
            }
        }

        @Volatile private var INSTANCE: AppDb? = null
        fun get(ctx: Context): AppDb = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(ctx.applicationContext, AppDb::class.java, "agrogarden.db")
                .addMigrations(MIGRATION_1_2)
                .build().also { INSTANCE = it }
        }
    }
}
