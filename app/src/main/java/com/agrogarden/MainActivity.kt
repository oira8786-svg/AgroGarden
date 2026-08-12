package com.agrogarden
import android.app.*
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.*
import com.agrogarden.data.*
import kotlinx.coroutines.launch

class MainActivity:ComponentActivity(){
 override fun onCreate(b:Bundle?){super.onCreate(b)
   if(android.os.Build.VERSION.SDK_INT>=33) requestPermissions(arrayOf("android.permission.POST_NOTIFICATIONS"),10)
   val ch=NotificationChannel("agro","AgroGarden",NotificationManager.IMPORTANCE_DEFAULT)
   getSystemService(NotificationManager::class.java).createNotificationChannel(ch)
   setContent{AgroApp(AppDb.get(this))}
 }
}
@Composable fun AgroApp(db:AppDb){
 var tab by remember{mutableIntStateOf(0)}
 val tabs=listOf("🌱 Посевы","🌾 Семена","🧪 Удобрения","💧 Полив","🔔 Задачи","💰 Продажи","🧺 Урожай","📉 Расходы","🛡️ Обработки","📅 Календарь","📦 Склад","📊 Прибыль")
 Scaffold(topBar={TopAppBar(title={Text("AgroGarden")})},bottomBar={NavigationBar{tabs.take(4).forEachIndexed{i,t->NavigationBarItem(selected=tab==i,onClick={tab=i},icon={Text(t.take(2))},label={Text(t.drop(2).take(8))})}}}){p->
   Column(Modifier.padding(p).padding(12.dp)){Text(tabs[tab],style=MaterialTheme.typography.headlineSmall); Spacer(Modifier.height(12.dp))
    when(tab){0->CropScreen(db);1->SeedScreen(db);2->FertilizerScreen(db);3->IrrigationScreen(db);else->Dashboard(tab)}
   }}
}
@Composable fun Dashboard(tab:Int){Card(Modifier.fillMaxWidth()){Column(Modifier.padding(16.dp)){Text("Раздел готов к работе");Text("Данные сохраняются локально в Room.")}}}

@Composable fun CropScreen(db:AppDb){val list by db.crops().all().collectAsState(emptyList()); val scope=rememberCoroutineScope(); var name by remember{mutableStateOf("")}
 Column{Row{OutlinedTextField(name,{name=it},label={Text("Культура")},Modifier.weight(1f));Button({if(name.isNotBlank())scope.launch{db.crops().add(Crop(name=name,area=0.0,sowDate="",harvestDate="",status="Запланировано",notes=""))};name=""},Modifier.padding(start=8.dp)){Text("➕")}}
 LazyColumn{items(list){x->ListItem(headlineContent={Text(x.name)},supportingContent={Text("Площадь: ${x.area} | ${x.status}")},trailingContent={TextButton({scope.launch{db.crops().delete(x)}}){Text("🗑️")}})}}}}
@Composable fun SeedScreen(db:AppDb){val list by db.seeds().all().collectAsState(emptyList()); val scope=rememberCoroutineScope(); var name by remember{mutableStateOf("")}
 Column{Row{OutlinedTextField(name,{name=it},label={Text("Семена")},Modifier.weight(1f));Button({if(name.isNotBlank())scope.launch{db.seeds().add(Seed(name=name,quantity=0.0,unit="шт",batch="",expiry=""))};name=""},Modifier.padding(start=8.dp)){Text("➕")}};LazyColumn{items(list){x->ListItem(headlineContent={Text(x.name)},supportingContent={Text("${x.quantity} ${x.unit} | партия ${x.batch} | годность ${x.expiry}")},trailingContent={TextButton({scope.launch{db.seeds().delete(x)}}){Text("🗑️")}})}}}}
@Composable fun FertilizerScreen(db:AppDb){val list by db.fertilizers().all().collectAsState(emptyList());val scope=rememberCoroutineScope();var name by remember{mutableStateOf("")}
 Column{Row{OutlinedTextField(name,{name=it},label={Text("Удобрение")},Modifier.weight(1f));Button({if(name.isNotBlank())scope.launch{db.fertilizers().add(Fertilizer(name,0.0,"кг",1.0))};name=""},Modifier.padding(start=8.dp)){Text("➕")}};LazyColumn{items(list){x->ListItem(headlineContent={Text(x.name)},supportingContent={Text("Остаток ${x.quantity} ${x.unit}; минимум ${x.minStock}")},trailingContent={if(x.quantity<=x.minStock)Text("⚠️") else Text("OK")})}}}}
@Composable fun IrrigationScreen(db:AppDb){val list by db.irrigations().all().collectAsState(emptyList());val scope=rememberCoroutineScope();var crop by remember{mutableStateOf("")}
 Column{Row{OutlinedTextField(crop,{crop=it},label={Text("Культура")},Modifier.weight(1f));Button({if(crop.isNotBlank())scope.launch{db.irrigations().add(Irrigation(crop,"","",0.0,7))};crop=""},Modifier.padding(start=8.dp)){Text("➕")}};LazyColumn{items(list){x->ListItem(headlineContent={Text(x.crop)},supportingContent={Text("${x.date} ${x.time} | ${x.volume} л | каждые ${x.repeatDays} дн.")},trailingContent={TextButton({scope.launch{db.irrigations().delete(x)}}){Text("🗑️")}})}}}}
