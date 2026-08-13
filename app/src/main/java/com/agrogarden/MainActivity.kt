package com.agrogarden

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.work.*
import com.agrogarden.data.*
import com.agrogarden.notifications.ReminderWorker
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 33) requestPermissions(arrayOf("android.permission.POST_NOTIFICATIONS"), 10)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getSystemService(NotificationManager::class.java).createNotificationChannel(
                NotificationChannel("agro", "AgroGarden", NotificationManager.IMPORTANCE_DEFAULT)
            )
        }
        setContent { AgroApp(AppDb.get(this)) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgroApp(db: AppDb) {
    val tabs = listOf("🌱 Посевы", "🌾 Семена", "🧪 Удобрения", "💧 Полив", "🔔 Задачи", "💰 Продажи", "🧺 Урожай", "📉 Расходы", "🛡️ Обработки", "📅 Календарь", "📦 Склад", "📊 Прибыль")
    var selected by remember { mutableIntStateOf(0) }
    var exportRequested by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    val exporter = rememberLauncherForExport { uri ->
        if (uri != null) scope.launch { exportJson(context, db, uri) }
    }

    Scaffold(topBar = { TopAppBar(title = { Text("AgroGarden") }) }) { padding ->
        Column(Modifier.padding(padding)) {
            ScrollableTabRow(selectedTabIndex = selected, edgePadding = 8.dp) {
                tabs.forEachIndexed { i, title ->
                    Tab(selected = selected == i, onClick = { selected = i }, text = { Text(title) })
                }
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp), horizontalArrangement = Arrangement.End) {
                OutlinedButton(onClick = { exportRequested = true }) { Text("💾 Экспорт / резервная копия") }
            }
            when (selected) {
                0 -> CropScreen(db)
                1 -> SeedScreen(db)
                2 -> FertilizerScreen(db)
                3 -> IrrigationScreen(db, context)
                4 -> TaskScreen(db, context)
                5 -> SalesScreen(db)
                6 -> HarvestScreen(db)
                7 -> ExpenseScreen(db)
                8 -> TreatmentScreen(db)
                9 -> CalendarScreen(db)
                10 -> WarehouseScreen(db)
                11 -> ProfitScreen(db)
            }
        }
    }
    if (exportRequested) { exportRequested = false; exporter.launch("agrogarden-backup.json") }
}

@Composable
fun rememberLauncherForExport(onResult: (android.net.Uri?) -> Unit) = androidx.activity.compose.rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json"), onResult)

@Composable
fun ScreenColumn(content: @Composable ColumnScope.() -> Unit) = LazyColumn(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { item { Column(content = content) } }

@Composable
fun CropScreen(db: AppDb) {
    val list by db.crops().all().collectAsState(emptyList()); val scope = rememberCoroutineScope(); var name by remember { mutableStateOf("") }
    ScreenColumn {
        Row(Modifier.fillMaxWidth()) { OutlinedTextField(name, { name = it }, Modifier.weight(1f), label = { Text("Культура") }); Button({ if (name.isNotBlank()) { scope.launch { db.crops().add(Crop(name=name, area=0.0, sowDate="", harvestDate="", status="Запланировано", notes="")); name="" } } }, Modifier.padding(start=8.dp)) { Text("➕") } }
        items(list) { crop -> ListItem(headlineContent={Text(crop.name)}, supportingContent={Text("Площадь: ${crop.area} | ${crop.status}")}, trailingContent={TextButton({scope.launch{db.crops().delete(crop)}}){Text("🗑️")}}) }
    }
}

@Composable
fun SeedScreen(db: AppDb) {
    val list by db.seeds().all().collectAsState(emptyList()); val scope=rememberCoroutineScope(); var name by remember{mutableStateOf("")}; var qty by remember{mutableStateOf("0")}; var unit by remember{mutableStateOf("шт")}; var batch by remember{mutableStateOf("")}; var expiry by remember{mutableStateOf("")}
    ScreenColumn {
        OutlinedTextField(name,{name=it},Modifier.fillMaxWidth(),label={Text("Семена")}); Row(Modifier.fillMaxWidth()){OutlinedTextField(qty,{qty=it},Modifier.weight(1f),label={Text("Количество")}); OutlinedTextField(unit,{unit=it},Modifier.weight(1f).padding(start=6.dp),label={Text("Единица")})}
        Row(Modifier.fillMaxWidth()){OutlinedTextField(batch,{batch=it},Modifier.weight(1f),label={Text("Партия")}); OutlinedTextField(expiry,{expiry=it},Modifier.weight(1f).padding(start=6.dp),label={Text("Срок годности")})}
        Button({if(name.isNotBlank()){scope.launch{db.seeds().add(Seed(name=name,quantity=qty.toDoubleOrNull()?:0.0,unit=unit,batch=batch,expiry=expiry));name=""}}},Modifier.fillMaxWidth()){Text("Добавить семена")}
        items(list){seed->ListItem(headlineContent={Text(seed.name)},supportingContent={Text("${seed.quantity} ${seed.unit} | партия ${seed.batch} | годность ${seed.expiry}")},trailingContent={TextButton({scope.launch{db.seeds().delete(seed)}}){Text("🗑️")}})}
    }
}

@Composable
fun FertilizerScreen(db: AppDb) {
    val list by db.fertilizers().all().collectAsState(emptyList()); val scope=rememberCoroutineScope(); var name by remember{mutableStateOf("")}; var qty by remember{mutableStateOf("0")}; var unit by remember{mutableStateOf("кг")}; var min by remember{mutableStateOf("1")}
    ScreenColumn {
        OutlinedTextField(name,{name=it},Modifier.fillMaxWidth(),label={Text("Удобрение")}); Row(Modifier.fillMaxWidth()){OutlinedTextField(qty,{qty=it},Modifier.weight(1f),label={Text("Остаток")}); OutlinedTextField(min,{min=it},Modifier.weight(1f).padding(start=6.dp),label={Text("Минимум")}); OutlinedTextField(unit,{unit=it},Modifier.weight(1f).padding(start=6.dp),label={Text("Единица")})}
        Button({if(name.isNotBlank()){scope.launch{db.fertilizers().add(Fertilizer(name=name,quantity=qty.toDoubleOrNull()?:0.0,unit=unit,minStock=min.toDoubleOrNull()?:0.0));name=""}}},Modifier.fillMaxWidth()){Text("Добавить удобрение")}
        items(list){f->ListItem(headlineContent={Text(f.name)},supportingContent={Text("Остаток: ${f.quantity} ${f.unit} | минимум: ${f.minStock}")},trailingContent={Row{if(f.quantity<=f.minStock)Text("⚠️");TextButton({scope.launch{db.fertilizers().delete(f)}}){Text("🗑️")}}})}
    }
}

@Composable
fun IrrigationScreen(db: AppDb, context: Context) {
    val list by db.irrigations().all().collectAsState(emptyList()); val scope=rememberCoroutineScope(); var crop by remember{mutableStateOf("")}; var date by remember{mutableStateOf("")}; var time by remember{mutableStateOf("")}; var volume by remember{mutableStateOf("0")}; var repeat by remember{mutableStateOf("7")}
    ScreenColumn {
        OutlinedTextField(crop,{crop=it},Modifier.fillMaxWidth(),label={Text("Культура")}); Row(Modifier.fillMaxWidth()){OutlinedTextField(date,{date=it},Modifier.weight(1f),label={Text("Дата: yyyy-MM-dd")}); OutlinedTextField(time,{time=it},Modifier.weight(1f).padding(start=6.dp),label={Text("Время: HH:mm")})}; Row(Modifier.fillMaxWidth()){OutlinedTextField(volume,{volume=it},Modifier.weight(1f),label={Text("Объём")}); OutlinedTextField(repeat,{repeat=it},Modifier.weight(1f).padding(start=6.dp),label={Text("Каждые N дней")})}
        Button({if(crop.isNotBlank()){scope.launch{val x=Irrigation(crop=crop,date=date,time=time,volume=volume.toDoubleOrNull()?:0.0,repeatDays=repeat.toIntOrNull()?.coerceAtLeast(1)?:7);db.irrigations().add(x);scheduleReminder(context,"Полив: ${x.crop}",x.date,x.time,x.repeatDays);crop=""}}},Modifier.fillMaxWidth()){Text("Добавить полив + напоминание")}
        items(list){x->ListItem(headlineContent={Text("💧 ${x.crop}")},supportingContent={Text("${x.date} ${x.time} | ${x.volume} | каждые ${x.repeatDays} дн.")},trailingContent={TextButton({scope.launch{db.irrigations().delete(x)}}){Text("🗑️")}})}
    }
}

@Composable
fun TaskScreen(db: AppDb, context: Context) {
    val list by db.tasks().all().collectAsState(emptyList()); val scope=rememberCoroutineScope(); var title by remember{mutableStateOf("")}; var date by remember{mutableStateOf("")}; var time by remember{mutableStateOf("")}; var repeat by remember{mutableStateOf("1")}
    ScreenColumn {
        OutlinedTextField(title,{title=it},Modifier.fillMaxWidth(),label={Text("Задача")}); Row(Modifier.fillMaxWidth()){OutlinedTextField(date,{date=it},Modifier.weight(1f),label={Text("Дата: yyyy-MM-dd")}); OutlinedTextField(time,{time=it},Modifier.weight(1f).padding(start=6.dp),label={Text("Время: HH:mm")}); OutlinedTextField(repeat,{repeat=it},Modifier.weight(1f).padding(start=6.dp),label={Text("Дни")})}
        Button({if(title.isNotBlank()){scope.launch{val x=Task(title=title,date=date,time=time,repeatDays=repeat.toIntOrNull()?.coerceAtLeast(1)?:1);db.tasks().add(x);scheduleReminder(context,x.title,x.date,x.time,x.repeatDays);title=""}}},Modifier.fillMaxWidth()){Text("Добавить задачу + уведомление")}
        items(list){x->ListItem(headlineContent={Text(if(x.done)"✅ ${x.title}" else "🔔 ${x.title}")},supportingContent={Text("${x.date} ${x.time} | каждые ${x.repeatDays} дн.")},trailingContent={Row{TextButton({scope.launch{db.tasks().update(x.copy(done=!x.done))}}){Text(if(x.done)"↩" else "✓")};TextButton({scope.launch{db.tasks().delete(x)}}){Text("🗑️")}}})}
    }
}

@Composable
fun SalesScreen(db: AppDb) {
    val list by db.sales().all().collectAsState(emptyList()); val scope=rememberCoroutineScope(); var product by remember{mutableStateOf("")};var qty by remember{mutableStateOf("0")};var price by remember{mutableStateOf("0")};var buyer by remember{mutableStateOf("")};var date by remember{mutableStateOf("")}
    ScreenColumn{OutlinedTextField(product,{product=it},Modifier.fillMaxWidth(),label={Text("Товар / урожай")});Row(Modifier.fillMaxWidth()){OutlinedTextField(qty,{qty=it},Modifier.weight(1f),label={Text("Количество")});OutlinedTextField(price,{price=it},Modifier.weight(1f).padding(start=6.dp),label={Text("Цена")})};Row(Modifier.fillMaxWidth()){OutlinedTextField(buyer,{buyer=it},Modifier.weight(1f),label={Text("Покупатель")});OutlinedTextField(date,{date=it},Modifier.weight(1f).padding(start=6.dp),label={Text("Дата")})};Button({if(product.isNotBlank())scope.launch{db.sales().add(Sale(product=product,quantity=qty.toDoubleOrNull()?:0.0,price=price.toDoubleOrNull()?:0.0,buyer=buyer,date=date));product=""}},Modifier.fillMaxWidth()){Text("Добавить продажу")};items(list){x->ListItem(headlineContent={Text(x.product)},supportingContent={Text("${x.quantity} × ${x.price} = ${x.quantity*x.price} | ${x.buyer} | ${x.date}")},trailingContent={TextButton({scope.launch{db.sales().delete(x)}}){Text("🗑️")}})}}
}

@Composable
fun HarvestScreen(db: AppDb) { val list by db.harvests().all().collectAsState(emptyList());val scope=rememberCoroutineScope();var crop by remember{mutableStateOf("")};var qty by remember{mutableStateOf("0")};var date by remember{mutableStateOf("")};var quality by remember{mutableStateOf("")};ScreenColumn{OutlinedTextField(crop,{crop=it},Modifier.fillMaxWidth(),label={Text("Культура")});Row(Modifier.fillMaxWidth()){OutlinedTextField(qty,{qty=it},Modifier.weight(1f),label={Text("Количество")});OutlinedTextField(date,{date=it},Modifier.weight(1f).padding(start=6.dp),label={Text("Дата")});OutlinedTextField(quality,{quality=it},Modifier.weight(1f).padding(start=6.dp),label={Text("Качество")})};Button({if(crop.isNotBlank())scope.launch{db.harvests().add(Harvest(crop=crop,quantity=qty.toDoubleOrNull()?:0.0,date=date,quality=quality));crop=""}},Modifier.fillMaxWidth()){Text("Добавить урожай")};items(list){x->ListItem(headlineContent={Text("🧺 ${x.crop}")},supportingContent={Text("${x.quantity} | ${x.date} | ${x.quality}")},trailingContent={TextButton({scope.launch{db.harvests().delete(x)}}){Text("🗑️")}})}}

@Composable
fun ExpenseScreen(db: AppDb) { val list by db.expenses().all().collectAsState(emptyList());val scope=rememberCoroutineScope();var category by remember{mutableStateOf("")};var amount by remember{mutableStateOf("0")};var date by remember{mutableStateOf("")};var note by remember{mutableStateOf("")};ScreenColumn{OutlinedTextField(category,{category=it},Modifier.fillMaxWidth(),label={Text("Материал / категория")});Row(Modifier.fillMaxWidth()){OutlinedTextField(amount,{amount=it},Modifier.weight(1f),label={Text("Сумма")});OutlinedTextField(date,{date=it},Modifier.weight(1f).padding(start=6.dp),label={Text("Дата")})};OutlinedTextField(note,{note=it},Modifier.fillMaxWidth(),label={Text("Комментарий")});Button({if(category.isNotBlank())scope.launch{db.expenses().add(Expense(category=category,amount=amount.toDoubleOrNull()?:0.0,date=date,note=note));category=""}},Modifier.fillMaxWidth()){Text("Добавить расход")};items(list){x->ListItem(headlineContent={Text("📉 ${x.category}")},supportingContent={Text("${x.amount} | ${x.date} | ${x.note}")},trailingContent={TextButton({scope.launch{db.expenses().delete(x)}}){Text("🗑️")}})}}

@Composable
fun TreatmentScreen(db: AppDb) { val list by db.treatments().all().collectAsState(emptyList());val scope=rememberCoroutineScope();var crop by remember{mutableStateOf("")};var issue by remember{mutableStateOf("")};var product by remember{mutableStateOf("")};var date by remember{mutableStateOf("")};var note by remember{mutableStateOf("")};ScreenColumn{OutlinedTextField(crop,{crop=it},Modifier.fillMaxWidth(),label={Text("Культура")});Row(Modifier.fillMaxWidth()){OutlinedTextField(issue,{issue=it},Modifier.weight(1f),label={Text("Болезнь / вредитель / профилактика")});OutlinedTextField(product,{product=it},Modifier.weight(1f).padding(start=6.dp),label={Text("Препарат")})};Row(Modifier.fillMaxWidth()){OutlinedTextField(date,{date=it},Modifier.weight(1f),label={Text("Дата")});OutlinedTextField(note,{note=it},Modifier.weight(1f).padding(start=6.dp),label={Text("Примечание")})};Button({if(crop.isNotBlank())scope.launch{db.treatments().add(Treatment(crop=crop,issue=issue,product=product,date=date,note=note));crop=""}},Modifier.fillMaxWidth()){Text("Добавить обработку")};items(list){x->ListItem(headlineContent={Text("🛡️ ${x.crop}")},supportingContent={Text("${x.issue} | ${x.product} | ${x.date} | ${x.note}")},trailingContent={TextButton({scope.launch{db.treatments().delete(x)}}){Text("🗑️")}})}}

@Composable
fun CalendarScreen(db: AppDb) { val irrigations by db.irrigations().all().collectAsState(emptyList());val tasks by db.tasks().all().collectAsState(emptyList());ScreenColumn{Text("📅 Календарь",style=MaterialTheme.typography.titleLarge);items(irrigations){x->ListItem(headlineContent={Text("💧 ${x.date} ${x.time} — ${x.crop}")},supportingContent={Text("Полив ${x.volume}, каждые ${x.repeatDays} дн.")})};items(tasks){x->ListItem(headlineContent={Text("🔔 ${x.date} ${x.time} — ${x.title}")},supportingContent={Text(if(x.done)"Выполнено" else "Запланировано")})}}}

@Composable
fun WarehouseScreen(db: AppDb) { val seeds by db.seeds().all().collectAsState(emptyList());val fertilizers by db.fertilizers().all().collectAsState(emptyList());ScreenColumn{Text("📦 Семена",style=MaterialTheme.typography.titleLarge);items(seeds){x->ListItem(headlineContent={Text(x.name)},supportingContent={Text("${x.quantity} ${x.unit} | ${x.batch} | ${x.expiry}")})};Text("🧪 Удобрения",style=MaterialTheme.typography.titleLarge);items(fertilizers){x->ListItem(headlineContent={Text(x.name)},supportingContent={Text("${x.quantity} ${x.unit} | минимум ${x.minStock}")},trailingContent={if(x.quantity<=x.minStock)Text("⚠️")})}}}

@Composable
fun ProfitScreen(db: AppDb) { val sales by db.sales().all().collectAsState(emptyList());val expenses by db.expenses().all().collectAsState(emptyList());val revenue=sales.sumOf{it.quantity*it.price};val costs=expenses.sumOf{it.amount};val profit=revenue-costs;ScreenColumn{Text("📊 Финансы",style=MaterialTheme.typography.headlineSmall);Card(Modifier.fillMaxWidth()){Column(Modifier.padding(16.dp)){Text("Выручка: %.2f".format(revenue));Text("Расходы: %.2f".format(costs));Text("Прибыль: %.2f".format(profit),style=MaterialTheme.typography.titleLarge)}}}}

fun scheduleReminder(context: Context, title: String, date: String, time: String, repeatDays: Int) {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    val delay = runCatching { Duration.between(LocalDateTime.now(), LocalDateTime.parse("$date $time", formatter)).toMillis().coerceAtLeast(0) }.getOrDefault(0L)
    val request = PeriodicWorkRequestBuilder<ReminderWorker>(repeatDays.toLong().coerceAtLeast(1), TimeUnit.DAYS).setInitialDelay(delay, TimeUnit.MILLISECONDS).setInputData(workDataOf("title" to title)).build()
    WorkManager.getInstance(context).enqueueUniquePeriodicWork("agro-$title-$date-$time", ExistingPeriodicWorkPolicy.UPDATE, request)
}

suspend fun exportJson(context: Context, db: AppDb, uri: android.net.Uri) {
    val crops=db.crops().all().first();val seeds=db.seeds().all().first();val fertilizers=db.fertilizers().all().first();val irrigations=db.irrigations().all().first();val tasks=db.tasks().all().first();val sales=db.sales().all().first();val harvests=db.harvests().all().first();val expenses=db.expenses().all().first();val treatments=db.treatments().all().first()
    fun q(s:String)=s.replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n")
    fun obj(fields: List<Pair<String,String>>)=fields.joinToString(",","{","}"){"\"${q(it.first)}\":\"${q(it.second)}\""}
    fun arr(items:List<List<Pair<String,String>>>)=items.joinToString(",","[","]"){obj(it)}
    val json="""{"crops":${arr(crops.map{listOf("id" to it.id.toString(),"name" to it.name,"area" to it.area.toString(),"sowDate" to it.sowDate,"harvestDate" to it.harvestDate,"status" to it.status,"notes" to it.notes)})},"seeds":${arr(seeds.map{listOf("id" to it.id.toString(),"name" to it.name,"quantity" to it.quantity.toString(),"unit" to it.unit,"batch" to it.batch,"expiry" to it.expiry)})},"fertilizers":${arr(fertilizers.map{listOf("id" to it.id.toString(),"name" to it.name,"quantity" to it.quantity.toString(),"unit" to it.unit,"minStock" to it.minStock.toString())})},"irrigations":${arr(irrigations.map{listOf("crop" to it.crop,"date" to it.date,"time" to it.time,"volume" to it.volume.toString(),"repeatDays" to it.repeatDays.toString())})},"tasks":${arr(tasks.map{listOf("title" to it.title,"date" to it.date,"time" to it.time,"repeatDays" to it.repeatDays.toString(),"done" to it.done.toString())})},"sales":${arr(sales.map{listOf("product" to it.product,"quantity" to it.quantity.toString(),"price" to it.price.toString(),"buyer" to it.buyer,"date" to it.date)})},"harvests":${arr(harvests.map{listOf("crop" to it.crop,"quantity" to it.quantity.toString(),"date" to it.date,"quality" to it.quality)})},"expenses":${arr(expenses.map{listOf("category" to it.category,"amount" to it.amount.toString(),"date" to it.date,"note" to it.note)})},"treatments":${arr(treatments.map{listOf("crop" to it.crop,"issue" to it.issue,"product" to it.product,"date" to it.date,"note" to it.note)})}}"""
    context.contentResolver.openOutputStream(uri)?.use{it.write(json.toByteArray())}
}
