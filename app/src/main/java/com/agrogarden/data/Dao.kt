package com.agrogarden.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao interface CropDao {
    @Query("SELECT * FROM crops ORDER BY id DESC") fun all(): Flow<List<Crop>>
    @Insert suspend fun add(x: Crop): Long
    @Update suspend fun update(x: Crop)
    @Delete suspend fun delete(x: Crop)
}

@Dao interface SeedDao {
    @Query("SELECT * FROM seeds ORDER BY id DESC") fun all(): Flow<List<Seed>>
    @Insert suspend fun add(x: Seed): Long
    @Update suspend fun update(x: Seed)
    @Delete suspend fun delete(x: Seed)
}

@Dao interface FertilizerDao {
    @Query("SELECT * FROM fertilizers ORDER BY id DESC") fun all(): Flow<List<Fertilizer>>
    @Insert suspend fun add(x: Fertilizer): Long
    @Update suspend fun update(x: Fertilizer)
    @Delete suspend fun delete(x: Fertilizer)
}

@Dao interface IrrigationDao {
    @Query("SELECT * FROM irrigations ORDER BY date, time") fun all(): Flow<List<Irrigation>>
    @Insert suspend fun add(x: Irrigation): Long
    @Update suspend fun update(x: Irrigation)
    @Delete suspend fun delete(x: Irrigation)
}

@Dao interface SaleDao {
    @Query("SELECT * FROM sales ORDER BY id DESC") fun all(): Flow<List<Sale>>
    @Insert suspend fun add(x: Sale): Long
    @Update suspend fun update(x: Sale)
    @Delete suspend fun delete(x: Sale)
}

@Dao interface HarvestDao {
    @Query("SELECT * FROM harvests ORDER BY id DESC") fun all(): Flow<List<Harvest>>
    @Insert suspend fun add(x: Harvest): Long
    @Update suspend fun update(x: Harvest)
    @Delete suspend fun delete(x: Harvest)
}

@Dao interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY id DESC") fun all(): Flow<List<Expense>>
    @Insert suspend fun add(x: Expense): Long
    @Update suspend fun update(x: Expense)
    @Delete suspend fun delete(x: Expense)
}

@Dao interface TreatmentDao {
    @Query("SELECT * FROM treatments ORDER BY id DESC") fun all(): Flow<List<Treatment>>
    @Insert suspend fun add(x: Treatment): Long
    @Update suspend fun update(x: Treatment)
    @Delete suspend fun delete(x: Treatment)
}

@Dao interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY date, time") fun all(): Flow<List<Task>>
    @Insert suspend fun add(x: Task): Long
    @Update suspend fun update(x: Task)
    @Delete suspend fun delete(x: Task)
}

@Dao interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY createdAt DESC") fun all(): Flow<List<Note>>
    @Insert suspend fun add(x: Note): Long
    @Update suspend fun update(x: Note)
    @Delete suspend fun delete(x: Note)
}

@Dao interface FavoritePlantDao {
    @Query("SELECT * FROM favorite_plants ORDER BY addedAt DESC") fun all(): Flow<List<FavoritePlant>>
    @Query("SELECT COUNT(*) FROM favorite_plants WHERE plantId = :plantId") suspend fun count(plantId: String): Int
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun add(x: FavoritePlant)
    @Delete suspend fun delete(x: FavoritePlant)
}
