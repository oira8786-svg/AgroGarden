package com.agrogarden.data
import androidx.room.*
@Database(entities=[Crop::class,Seed::class,Fertilizer::class,Irrigation::class,Sale::class,Harvest::class,Expense::class,Treatment::class,Task::class],version=1)
abstract class AppDb:RoomDatabase(){
 abstract fun crops():CropDao; abstract fun seeds():SeedDao; abstract fun fertilizers():FertilizerDao
 abstract fun irrigations():IrrigationDao; abstract fun sales():SaleDao; abstract fun harvests():HarvestDao
 abstract fun expenses():ExpenseDao; abstract fun treatments():TreatmentDao; abstract fun tasks():TaskDao
 companion object { @Volatile private var INSTANCE:AppDb?=null
 fun get(ctx:android.content.Context)=INSTANCE?:synchronized(this){ INSTANCE?:Room.databaseBuilder(ctx,AppDb::class.java,"agrogarden.db").build().also{INSTANCE=it}}
 }}
