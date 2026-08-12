package com.agrogarden.notifications
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.agrogarden.R

class ReminderWorker(ctx:Context, params:WorkerParameters):CoroutineWorker(ctx,params){
 override suspend fun doWork():Result{
   val n=NotificationCompat.Builder(applicationContext,"agro")
     .setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle("AgroGarden")
     .setContentText(inputData.getString("title")?:"Аграрная задача")
     .setPriority(NotificationCompat.PRIORITY_DEFAULT).build()
   NotificationManagerCompat.from(applicationContext).notify(System.currentTimeMillis().toInt(),n)
   return Result.success()
 }
}
