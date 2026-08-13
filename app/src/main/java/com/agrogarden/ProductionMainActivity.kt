package com.agrogarden

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.agrogarden.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class ProductionMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 33) requestPermissions(arrayOf("android.permission.POST_NOTIFICATIONS"), 10)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) getSystemService(NotificationManager::class.java).createNotificationChannel(NotificationChannel("agro", "AgroGarden", NotificationManager.IMPORTANCE_DEFAULT))
        setContent { ProductionShell(AppDb.get(this)) }
    }
}

@Composable
private fun ProductionShell(db: AppDb) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    val restore = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) scope.launch { restoreProductionBackup(context, db, uri) }
    }
    Box(Modifier.fillMaxSize()) {
        AgroApp(db)
        FloatingActionButton(
            onClick = { restore.launch(arrayOf("application/json", "text/*")) },
            modifier = Modifier.align(Alignment.BottomEnd)
        ) { Text("♻️") }
    }
}

private suspend fun restoreProductionBackup(context: Context, db: AppDb, uri: Uri) = withContext(Dispatchers.IO) {
    val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() } ?: return@withContext
    val root = JSONObject(text)
    db.clearAllTables()
    fun arr(name: String) = root.optJSONArray(name) ?: JSONArray()
    var a = arr("crops"); for (i in 0 until a.length()) { val x=a.getJSONObject(i); db.crops().add(Crop(x.optLong("id"),x.optString("name"),x.optDouble("area"),x.optString("sowDate"),x.optString("harvestDate"),x.optString("status"),x.optString("notes"))) }
    a = arr("seeds"); for (i in 0 until a.length()) { val x=a.getJSONObject(i); db.seeds().add(Seed(x.optLong("id"),x.optString("name"),x.optDouble("quantity"),x.optString("unit"),x.optString("batch"),x.optString("expiry"))) }
    a = arr("fertilizers"); for (i in 0 until a.length()) { val x=a.getJSONObject(i); db.fertilizers().add(Fertilizer(x.optLong("id"),x.optString("name"),x.optDouble("quantity"),x.optString("unit"),x.optDouble("minStock"))) }
    a = arr("irrigations"); for (i in 0 until a.length()) { val x=a.getJSONObject(i); db.irrigations().add(Irrigation(x.optLong("id"),x.optString("crop"),x.optString("date"),x.optString("time"),x.optDouble("volume"),x.optInt("repeatDays",1))) }
    a = arr("sales"); for (i in 0 until a.length()) { val x=a.getJSONObject(i); db.sales().add(Sale(x.optLong("id"),x.optString("product"),x.optDouble("quantity"),x.optDouble("price"),x.optString("buyer"),x.optString("date"))) }
    a = arr("harvests"); for (i in 0 until a.length()) { val x=a.getJSONObject(i); db.harvests().add(Harvest(x.optLong("id"),x.optString("crop"),x.optDouble("quantity"),x.optString("date"),x.optString("quality"))) }
    a = arr("expenses"); for (i in 0 until a.length()) { val x=a.getJSONObject(i); db.expenses().add(Expense(x.optLong("id"),x.optString("category"),x.optDouble("amount"),x.optString("date"),x.optString("note"))) }
    a = arr("treatments"); for (i in 0 until a.length()) { val x=a.getJSONObject(i); db.treatments().add(Treatment(x.optLong("id"),x.optString("crop"),x.optString("issue"),x.optString("product"),x.optString("date"),x.optString("note"))) }
    a = arr("tasks"); for (i in 0 until a.length()) { val x=a.getJSONObject(i); db.tasks().add(Task(x.optLong("id"),x.optString("title"),x.optString("date"),x.optString("time"),x.optInt("repeatDays",1),x.optBoolean("done"))) }
    a = arr("notes"); for (i in 0 until a.length()) { val x=a.getJSONObject(i); db.notes().add(Note(x.optLong("id"),x.optString("title"),x.optString("text"),x.optLong("createdAt"))) }
    a = arr("favorites"); for (i in 0 until a.length()) { val x=a.getJSONObject(i); db.favorites().add(FavoritePlant(x.optString("plantId"),x.optLong("addedAt"))) }
}
