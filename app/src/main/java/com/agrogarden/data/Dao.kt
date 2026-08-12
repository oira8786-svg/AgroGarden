package com.agrogarden.data
import androidx.room.*
@Dao interface CropDao { @Query("SELECT * FROM crops ORDER BY id DESC") fun all(): kotlinx.coroutines.flow.Flow<List<Crop>>; @Insert suspend fun add(x:Crop); @Delete suspend fun delete(x:Crop) }
@Dao interface SeedDao { @Query("SELECT * FROM seeds ORDER BY id DESC") fun all(): kotlinx.coroutines.flow.Flow<List<Seed>>; @Insert suspend fun add(x:Seed); @Delete suspend fun delete(x:Seed) }
@Dao interface FertilizerDao { @Query("SELECT * FROM fertilizers ORDER BY id DESC") fun all(): kotlinx.coroutines.flow.Flow<List<Fertilizer>>; @Insert suspend fun add(x:Fertilizer); @Delete suspend fun delete(x:Fertilizer) }
@Dao interface IrrigationDao { @Query("SELECT * FROM irrigations ORDER BY date,time") fun all(): kotlinx.coroutines.flow.Flow<List<Irrigation>>; @Insert suspend fun add(x:Irrigation); @Delete suspend fun delete(x:Irrigation) }
@Dao interface SaleDao { @Query("SELECT * FROM sales ORDER BY id DESC") fun all(): kotlinx.coroutines.flow.Flow<List<Sale>>; @Insert suspend fun add(x:Sale); @Delete suspend fun delete(x:Sale) }
@Dao interface HarvestDao { @Query("SELECT * FROM harvests ORDER BY id DESC") fun all(): kotlinx.coroutines.flow.Flow<List<Harvest>>; @Insert suspend fun add(x:Harvest); @Delete suspend fun delete(x:Harvest) }
@Dao interface ExpenseDao { @Query("SELECT * FROM expenses ORDER BY id DESC") fun all(): kotlinx.coroutines.flow.Flow<List<Expense>>; @Insert suspend fun add(x:Expense); @Delete suspend fun delete(x:Expense) }
@Dao interface TreatmentDao { @Query("SELECT * FROM treatments ORDER BY id DESC") fun all(): kotlinx.coroutines.flow.Flow<List<Treatment>>; @Insert suspend fun add(x:Treatment); @Delete suspend fun delete(x:Treatment) }
@Dao interface TaskDao { @Query("SELECT * FROM tasks ORDER BY date,time") fun all(): kotlinx.coroutines.flow.Flow<List<Task>>; @Insert suspend fun add(x:Task); @Delete suspend fun delete(x:Task) }
