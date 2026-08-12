package com.agrogarden.data

import androidx.room.*
@Entity(tableName="crops") data class Crop(@PrimaryKey(autoGenerate=true) val id:Long=0,val name:String,val area:Double,val sowDate:String,val harvestDate:String,val status:String,val notes:String)
@Entity(tableName="seeds") data class Seed(@PrimaryKey(autoGenerate=true) val id:Long=0,val name:String,val quantity:Double,val unit:String,val batch:String,val expiry:String)
@Entity(tableName="fertilizers") data class Fertilizer(@PrimaryKey(autoGenerate=true) val id:Long=0,val name:String,val quantity:Double,val unit:String,val minStock:Double)
@Entity(tableName="irrigations") data class Irrigation(@PrimaryKey(autoGenerate=true) val id:Long=0,val crop:String,val date:String,val time:String,val volume:Double,val repeatDays:Int)
@Entity(tableName="sales") data class Sale(@PrimaryKey(autoGenerate=true) val id:Long=0,val product:String,val quantity:Double,val price:Double,val buyer:String,val date:String)
@Entity(tableName="harvests") data class Harvest(@PrimaryKey(autoGenerate=true) val id:Long=0,val crop:String,val quantity:Double,val date:String,val quality:String)
@Entity(tableName="expenses") data class Expense(@PrimaryKey(autoGenerate=true) val id:Long=0,val category:String,val amount:Double,val date:String,val note:String)
@Entity(tableName="treatments") data class Treatment(@PrimaryKey(autoGenerate=true) val id:Long=0,val crop:String,val issue:String,val product:String,val date:String,val note:String)
@Entity(tableName="tasks") data class Task(@PrimaryKey(autoGenerate=true) val id:Long=0,val title:String,val date:String,val time:String,val repeatDays:Int,val done:Boolean=false)
