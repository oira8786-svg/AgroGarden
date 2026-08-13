package com.agrogarden

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.agrogarden.data.AppDb
import com.agrogarden.data.Crop
import com.agrogarden.data.Expense
import com.agrogarden.data.Fertilizer
import com.agrogarden.data.Harvest
import com.agrogarden.data.Irrigation
import com.agrogarden.data.Note
import com.agrogarden.data.Sale
import com.agrogarden.data.Seed
import com.agrogarden.data.Task
import com.agrogarden.data.Treatment
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 33) requestPermissions(arrayOf("android.permission.POST_NOTIFICATIONS"), 10)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getSystemService(NotificationManager::class.java).createNotificationChannel(
                NotificationChannel("agro", "FAYOZAGRO", NotificationManager.IMPORTANCE_DEFAULT)
            )
        }
        setContent { FAYOZAGRO(AppDb.get(this)) }
    }
}

private data class Tab(val title: String, val icon: String)

@Composable
fun FAYOZAGRO(db: AppDb) {
    val tabs = remember {
        listOf(
            Tab("Главная", "⌂"), Tab("Посевы", "🌾"), Tab("Семена", "🌱"), Tab("Удобрения", "🧪"),
            Tab("Полив", "💧"), Tab("Напоминания", "🔔"), Tab("Продажи", "💰"), Tab("Урожай", "🧺"),
            Tab("Расходы", "📉"), Tab("Обработки", "🛡️"), Tab("Календарь", "📅"), Tab("Склад", "📦"),
            Tab("Прибыль", "📊"), Tab("Блокнот", "📝")
        )
    }
    var selected by remember { mutableIntStateOf(0) }
    Scaffold(
        topBar = { TopAppBar(title = { Text("FAYOZAGRO • ${tabs[selected].title}") }) },
        bottomBar = {
            NavigationBar {
                val start = (selected / 4) * 4
                tabs.drop(start).take(4).forEachIndexed { local, tab ->
                    val index = start + local
                    NavigationBarItem(selected == index, { selected = index }, { Text(tab.icon) }, label = { Text(tab.title.take(9)) })
                }
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat((tabs.size + 3) / 4) { page -> OutlinedButton({ selected = page * 4 }) { Text("${page + 1}") } }
            }
            when (selected) {
                0 -> HomeScreen(db) { selected = it }
                1 -> CropsScreen(db); 2 -> SeedsScreen(db); 3 -> FertilizersScreen(db); 4 -> IrrigationScreen(db)
                5 -> TasksScreen(db); 6 -> SalesScreen(db); 7 -> HarvestScreen(db); 8 -> ExpensesScreen(db)
                9 -> TreatmentsScreen(db); 10 -> CalendarScreen(db); 11 -> WarehouseScreen(db); 12 -> ProfitScreen(db); 13 -> NotesScreen(db)
            }
        }
    }
}

@Composable private fun HomeScreen(db: AppDb, open: (Int) -> Unit) {
    val fertilizers by db.fertilizers().all().collectAsState(emptyList()); val tasks by db.tasks().all().collectAsState(emptyList()); val sales by db.sales().all().collectAsState(emptyList()); val expenses by db.expenses().all().collectAsState(emptyList())
    val revenue = sales.sumOf { it.quantity * it.price }; val costs = expenses.sumOf { it.amount }
    LazyColumn(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Text("Панель хозяйства", style = MaterialTheme.typography.headlineSmall) }
        item { InfoCard("💰 Финансы", "Выручка: ${money(revenue)} • Расходы: ${money(costs)} • Прибыль: ${money(revenue - costs)}") }
        item { InfoCard("⚠️ Минимальные остатки", if (fertilizers.any { it.quantity <= it.minStock }) "Есть удобрения ниже минимума" else "Все остатки в норме") }
        item { InfoCard("🔔 Напоминания", "Незавершённых задач: ${tasks.count { !it.done }}") }
        item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { Button({ open(2) }, Modifier.weight(1f)) { Text("🌱 Семена") }; Button({ open(3) }, Modifier.weight(1f)) { Text("🧪 Запасы") } } }
        item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { Button({ open(4) }, Modifier.weight(1f)) { Text("💧 Полив") }; Button({ open(5) }, Modifier.weight(1f)) { Text("🔔 Задачи") } } }
        item { Text("Во всех редактируемых разделах доступны Добавить, Изменить и Удалить.") }
    }
}

@Composable private fun InfoCard(title: String, text: String) { Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { Text(title, style = MaterialTheme.typography.titleMedium); Spacer(Modifier.height(4.dp)); Text(text) } } }
@Composable private fun SearchField(value: String, onChange: (String) -> Unit) { OutlinedTextField(value, onChange, Modifier.fillMaxWidth(), label = { Text("Поиск") }, singleLine = true) }
@Composable private fun Field(value: String, onChange: (String) -> Unit, label: String, modifier: Modifier = Modifier) { OutlinedTextField(value, onChange, modifier.fillMaxWidth(), label = { Text(label) }) }
@Composable private fun Actions(onEdit: () -> Unit, onDelete: () -> Unit) { Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) { TextButton(onEdit) { Text("✏️ Изменить") }; TextButton(onDelete) { Text("🗑️") } } }

@Composable private fun CropsScreen(db: AppDb) {
    val list by db.crops().all().collectAsState(emptyList()); val scope = rememberCoroutineScope(); var query by remember { mutableStateOf("") }; var editing by remember { mutableStateOf<Crop?>(null) }; var adding by remember { mutableStateOf(false) }
    CrudHeader("Посевы", query, { query = it }, { adding = true })
    LazyColumn(Modifier.padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) { items(list.filter { it.name.contains(query, true) || it.status.contains(query, true) }) { x -> Card(Modifier.fillMaxWidth()) { ListRow("🌾 ${x.name}", "${x.area} га • посев ${x.sowDate} • уборка ${x.harvestDate}\nСтатус: ${x.status}\n${x.notes}") { Actions({ editing = x }, { scope.launch { db.crops().delete(x) } }) } } } }
    if (adding) CropEditor(null, { x -> scope.launch { db.crops().add(x) }; adding = false }, { adding = false }); editing?.let { old -> CropEditor(old, { x -> scope.launch { db.crops().update(x) }; editing = null }, { editing = null }) }
}
@Composable private fun CropEditor(item: Crop?, save: (Crop) -> Unit, cancel: () -> Unit) {
    var name by remember(item) { mutableStateOf(item?.name ?: "") }; var area by remember(item) { mutableStateOf(item?.area?.toString() ?: "0") }; var sow by remember(item) { mutableStateOf(item?.sowDate ?: "") }; var harvest by remember(item) { mutableStateOf(item?.harvestDate ?: "") }; var status by remember(item) { mutableStateOf(item?.status ?: "Запланировано") }; var notes by remember(item) { mutableStateOf(item?.notes ?: "") }
    EditorDialog(if (item == null) "Новый посев" else "Изменить посев", cancel, { if (name.isNotBlank()) save(Crop(item?.id ?: 0, name, area.toDoubleOrNull() ?: 0.0, sow, harvest, status, notes)) }) { Field(name, { name = it }, "Культура"); Field(area, { area = it }, "Площадь, га"); Field(sow, { sow = it }, "Дата посева"); Field(harvest, { harvest = it }, "Дата уборки"); Field(status, { status = it }, "Статус"); Field(notes, { notes = it }, "Примечания") }
}

@Composable private fun SeedsScreen(db: AppDb) {
    val list by db.seeds().all().collectAsState(emptyList()); val scope = rememberCoroutineScope(); var query by remember { mutableStateOf("") }; var adding by remember { mutableStateOf(false) }; var editing by remember { mutableStateOf<Seed?>(null) }
    CrudHeader("Семена", query, { query = it }, { adding = true }); LazyColumn(Modifier.padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) { items(list.filter { it.name.contains(query, true) || it.batch.contains(query, true) }) { x -> Card(Modifier.fillMaxWidth()) { ListRow("🌱 ${x.name}", "${x.quantity} ${x.unit} • партия: ${x.batch} • срок: ${x.expiry}") { Actions({ editing = x }, { scope.launch { db.seeds().delete(x) } }) } } } }
    if (adding) SeedEditor(null, { x -> scope.launch { db.seeds().add(x) }; adding = false }, { adding = false }); editing?.let { old -> SeedEditor(old, { x -> scope.launch { db.seeds().update(x) }; editing = null }, { editing = null }) }
}
@Composable private fun SeedEditor(item: Seed?, save: (Seed) -> Unit, cancel: () -> Unit) {
    var name by remember(item) { mutableStateOf(item?.name ?: "") }; var quantity by remember(item) { mutableStateOf(item?.quantity?.toString() ?: "0") }; var unit by remember(item) { mutableStateOf(item?.unit ?: "шт") }; var batch by remember(item) { mutableStateOf(item?.batch ?: "") }; var expiry by remember(item) { mutableStateOf(item?.expiry ?: "") }
    EditorDialog(if (item == null) "Новые семена" else "Изменить семена", cancel, { if (name.isNotBlank()) save(Seed(item?.id ?: 0, name, quantity.toDoubleOrNull() ?: 0.0, unit, batch, expiry)) }) { Field(name, { name = it }, "Название"); Field(quantity, { quantity = it }, "Количество"); Field(unit, { unit = it }, "Единица измерения"); Field(batch, { batch = it }, "Партия"); Field(expiry, { expiry = it }, "Срок годности") }
}

@Composable private fun FertilizersScreen(db: AppDb) {
    val list by db.fertilizers().all().collectAsState(emptyList()); val scope = rememberCoroutineScope(); var query by remember { mutableStateOf("") }; var adding by remember { mutableStateOf(false) }; var editing by remember { mutableStateOf<Fertilizer?>(null) }
    CrudHeader("Удобрения", query, { query = it }, { adding = true }); LazyColumn(Modifier.padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) { items(list.filter { it.name.contains(query, true) }) { x -> Card(Modifier.fillMaxWidth()) { ListRow(if (x.quantity <= x.minStock) "⚠️ ${x.name}" else "🧪 ${x.name}", "Остаток: ${x.quantity} ${x.unit} • минимум: ${x.minStock}") { Actions({ editing = x }, { scope.launch { db.fertilizers().delete(x) } }) } } } }
    if (adding) FertilizerEditor(null, { x -> scope.launch { db.fertilizers().add(x) }; adding = false }, { adding = false }); editing?.let { old -> FertilizerEditor(old, { x -> scope.launch { db.fertilizers().update(x) }; editing = null }, { editing = null }) }
}
@Composable private fun FertilizerEditor(item: Fertilizer?, save: (Fertilizer) -> Unit, cancel: () -> Unit) {
    var name by remember(item) { mutableStateOf(item?.name ?: "") }; var quantity by remember(item) { mutableStateOf(item?.quantity?.toString() ?: "0") }; var unit by remember(item) { mutableStateOf(item?.unit ?: "кг") }; var min by remember(item) { mutableStateOf(item?.minStock?.toString() ?: "1") }
    EditorDialog(if (item == null) "Новое удобрение" else "Изменить удобрение", cancel, { if (name.isNotBlank()) save(Fertilizer(item?.id ?: 0, name, quantity.toDoubleOrNull() ?: 0.0, unit, min.toDoubleOrNull() ?: 0.0)) }) { Field(name, { name = it }, "Название"); Field(quantity, { quantity = it }, "Остаток"); Field(unit, { unit = it }, "Единица"); Field(min, { min = it }, "Минимальный запас") }
}

@Composable private fun IrrigationScreen(db: AppDb) {
    val list by db.irrigations().all().collectAsState(emptyList()); val scope = rememberCoroutineScope(); var query by remember { mutableStateOf("") }; var adding by remember { mutableStateOf(false) }; var editing by remember { mutableStateOf<Irrigation?>(null) }
    CrudHeader("Полив", query, { query = it }, { adding = true }); LazyColumn(Modifier.padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) { items(list.filter { it.crop.contains(query, true) }) { x -> Card(Modifier.fillMaxWidth()) { ListRow("💧 ${x.crop}", "${x.date} ${x.time} • ${x.volume} л • каждые ${x.repeatDays} дн.") { Actions({ editing = x }, { scope.launch { db.irrigations().delete(x) } }) } } } }
    if (adding) IrrigationEditor(null, { x -> scope.launch { db.irrigations().add(x) }; adding = false }, { adding = false }); editing?.let { old -> IrrigationEditor(old, { x -> scope.launch { db.irrigations().update(x) }; editing = null }, { editing = null }) }
}
@Composable private fun IrrigationEditor(item: Irrigation?, save: (Irrigation) -> Unit, cancel: () -> Unit) {
    var crop by remember(item) { mutableStateOf(item?.crop ?: "") }; var date by remember(item) { mutableStateOf(item?.date ?: "") }; var time by remember(item) { mutableStateOf(item?.time ?: "") }; var volume by remember(item) { mutableStateOf(item?.volume?.toString() ?: "0") }; var repeat by remember(item) { mutableStateOf(item?.repeatDays?.toString() ?: "7") }
    EditorDialog(if (item == null) "Новый полив" else "Изменить полив", cancel, { if (crop.isNotBlank()) save(Irrigation(item?.id ?: 0, crop, date, time, volume.toDoubleOrNull() ?: 0.0, repeat.toIntOrNull() ?: 0)) }) { Field(crop, { crop = it }, "Культура"); Field(date, { date = it }, "Дата (ГГГГ-ММ-ДД)"); Field(time, { time = it }, "Время"); Field(volume, { volume = it }, "Объём, л"); Field(repeat, { repeat = it }, "Периодичность, дней") }
}

@Composable private fun TasksScreen(db: AppDb) {
    val list by db.tasks().all().collectAsState(emptyList()); val scope = rememberCoroutineScope(); var query by remember { mutableStateOf("") }; var adding by remember { mutableStateOf(false) }; var editing by remember { mutableStateOf<Task?>(null) }
    CrudHeader("Напоминания", query, { query = it }, { adding = true }); LazyColumn(Modifier.padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) { items(list.filter { it.title.contains(query, true) }) { x -> Card(Modifier.fillMaxWidth()) { ListRow(if (x.done) "✅ ${x.title}" else "🔔 ${x.title}", "${x.date} ${x.time} • каждые ${x.repeatDays} дн.") { Row { TextButton({ scope.launch { db.tasks().update(x.copy(done = !x.done)) } }) { Text(if (x.done) "Открыть" else "Готово") }; Actions({ editing = x }, { scope.launch { db.tasks().delete(x) } }) } } } } }
    if (adding) TaskEditor(null, { x -> scope.launch { db.tasks().add(x) }; adding = false }, { adding = false }); editing?.let { old -> TaskEditor(old, { x -> scope.launch { db.tasks().update(x) }; editing = null }, { editing = null }) }
}
@Composable private fun TaskEditor(item: Task?, save: (Task) -> Unit, cancel: () -> Unit) {
    var title by remember(item) { mutableStateOf(item?.title ?: "") }; var date by remember(item) { mutableStateOf(item?.date ?: "") }; var time by remember(item) { mutableStateOf(item?.time ?: "") }; var repeat by remember(item) { mutableStateOf(item?.repeatDays?.toString() ?: "0") }; var done by remember(item) { mutableStateOf(item?.done ?: false) }
    EditorDialog(if (item == null) "Новое напоминание" else "Изменить напоминание", cancel, { if (title.isNotBlank()) save(Task(item?.id ?: 0, title, date, time, repeat.toIntOrNull() ?: 0, done)) }) { Field(title, { title = it }, "Задача"); Field(date, { date = it }, "Дата"); Field(time, { time = it }, "Время"); Field(repeat, { repeat = it }, "Повторять каждые, дней"); TextButton({ done = !done }) { Text(if (done) "Статус: выполнено" else "Статус: не выполнено") } }
}

@Composable private fun SalesScreen(db: AppDb) {
    val list by db.sales().all().collectAsState(emptyList()); val scope = rememberCoroutineScope(); var query by remember { mutableStateOf("") }; var adding by remember { mutableStateOf(false) }; var editing by remember { mutableStateOf<Sale?>(null) }; val revenue = list.sumOf { it.quantity * it.price }
    CrudHeader("Продажи • выручка ${money(revenue)}", query, { query = it }, { adding = true }); LazyColumn(Modifier.padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) { items(list.filter { it.product.contains(query, true) || it.buyer.contains(query, true) }) { x -> Card(Modifier.fillMaxWidth()) { ListRow("💰 ${x.product}", "${x.quantity} × ${money(x.price)} = ${money(x.quantity * x.price)}\nПокупатель: ${x.buyer} • ${x.date}") { Actions({ editing = x }, { scope.launch { db.sales().delete(x) } }) } } } }
    if (adding) SaleEditor(null, { x -> scope.launch { db.sales().add(x) }; adding = false }, { adding = false }); editing?.let { old -> SaleEditor(old, { x -> scope.launch { db.sales().update(x) }; editing = null }, { editing = null }) }
}
@Composable private fun SaleEditor(item: Sale?, save: (Sale) -> Unit, cancel: () -> Unit) {
    var product by remember(item) { mutableStateOf(item?.product ?: "") }; var quantity by remember(item) { mutableStateOf(item?.quantity?.toString() ?: "1") }; var price by remember(item) { mutableStateOf(item?.price?.toString() ?: "0") }; var buyer by remember(item) { mutableStateOf(item?.buyer ?: "") }; var date by remember(item) { mutableStateOf(item?.date ?: "") }
    EditorDialog(if (item == null) "Новая продажа" else "Изменить продажу", cancel, { if (product.isNotBlank()) save(Sale(item?.id ?: 0, product, quantity.toDoubleOrNull() ?: 0.0, price.toDoubleOrNull() ?: 0.0, buyer, date)) }) { Field(product, { product = it }, "Товар / урожай"); Field(quantity, { quantity = it }, "Количество"); Field(price, { price = it }, "Цена за единицу"); Text("Выручка: ${money((quantity.toDoubleOrNull() ?: 0.0) * (price.toDoubleOrNull() ?: 0.0))}"); Field(buyer, { buyer = it }, "Покупатель"); Field(date, { date = it }, "Дата") }
}

@Composable private fun HarvestScreen(db: AppDb) {
    val list by db.harvests().all().collectAsState(emptyList()); val scope = rememberCoroutineScope(); var query by remember { mutableStateOf("") }; var adding by remember { mutableStateOf(false) }; var editing by remember { mutableStateOf<Harvest?>(null) }
    CrudHeader("Урожай", query, { query = it }, { adding = true }); LazyColumn(Modifier.padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) { items(list.filter { it.crop.contains(query, true) || it.quality.contains(query, true) }) { x -> Card(Modifier.fillMaxWidth()) { ListRow("🧺 ${x.crop}", "${x.quantity} • ${x.date} • качество: ${x.quality}") { Actions({ editing = x }, { scope.launch { db.harvests().delete(x) } }) } } } }
    if (adding) HarvestEditor(null, { x -> scope.launch { db.harvests().add(x) }; adding = false }, { adding = false }); editing?.let { old -> HarvestEditor(old, { x -> scope.launch { db.harvests().update(x) }; editing = null }, { editing = null }) }
}
@Composable private fun HarvestEditor(item: Harvest?, save: (Harvest) -> Unit, cancel: () -> Unit) {
    var crop by remember(item) { mutableStateOf(item?.crop ?: "") }; var quantity by remember(item) { mutableStateOf(item?.quantity?.toString() ?: "0") }; var date by remember(item) { mutableStateOf(item?.date ?: "") }; var quality by remember(item) { mutableStateOf(item?.quality ?: "") }
    EditorDialog(if (item == null) "Новый урожай" else "Изменить урожай", cancel, { if (crop.isNotBlank()) save(Harvest(item?.id ?: 0, crop, quantity.toDoubleOrNull() ?: 0.0, date, quality)) }) { Field(crop, { crop = it }, "Культура"); Field(quantity, { quantity = it }, "Количество"); Field(date, { date = it }, "Дата"); Field(quality, { quality = it }, "Качество") }
}

@Composable private fun ExpensesScreen(db: AppDb) {
    val list by db.expenses().all().collectAsState(emptyList()); val scope = rememberCoroutineScope(); var query by remember { mutableStateOf("") }; var adding by remember { mutableStateOf(false) }; var editing by remember { mutableStateOf<Expense?>(null) }; val total = list.sumOf { it.amount }
    CrudHeader("Расходы • ${money(total)}", query, { query = it }, { adding = true }); LazyColumn(Modifier.padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) { items(list.filter { it.category.contains(query, true) || it.note.contains(query, true) }) { x -> Card(Modifier.fillMaxWidth()) { ListRow("📉 ${x.category}", "${money(x.amount)} • ${x.date}\n${x.note}") { Actions({ editing = x }, { scope.launch { db.expenses().delete(x) } }) } } } }
    if (adding) ExpenseEditor(null, { x -> scope.launch { db.expenses().add(x) }; adding = false }, { adding = false }); editing?.let { old -> ExpenseEditor(old, { x -> scope.launch { db.expenses().update(x) }; editing = null }, { editing = null }) }
}
@Composable private fun ExpenseEditor(item: Expense?, save: (Expense) -> Unit, cancel: () -> Unit) {
    var category by remember(item) { mutableStateOf(item?.category ?: "") }; var amount by remember(item) { mutableStateOf(item?.amount?.toString() ?: "0") }; var date by remember(item) { mutableStateOf(item?.date ?: "") }; var note by remember(item) { mutableStateOf(item?.note ?: "") }
    EditorDialog(if (item == null) "Новый расход" else "Изменить расход", cancel, { if (category.isNotBlank()) save(Expense(item?.id ?: 0, category, amount.toDoubleOrNull() ?: 0.0, date, note)) }) { Field(category, { category = it }, "Категория / материал"); Field(amount, { amount = it }, "Сумма"); Field(date, { date = it }, "Дата"); Field(note, { note = it }, "Комментарий") }
}

@Composable private fun TreatmentsScreen(db: AppDb) {
    val list by db.treatments().all().collectAsState(emptyList()); val scope = rememberCoroutineScope(); var query by remember { mutableStateOf("") }; var adding by remember { mutableStateOf(false) }; var editing by remember { mutableStateOf<Treatment?>(null) }
    CrudHeader("Обработки", query, { query = it }, { adding = true }); LazyColumn(Modifier.padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) { items(list.filter { it.crop.contains(query, true) || it.issue.contains(query, true) || it.product.contains(query, true) }) { x -> Card(Modifier.fillMaxWidth()) { ListRow("🛡️ ${x.crop}", "Проблема: ${x.issue}\nПрепарат: ${x.product} • ${x.date}\n${x.note}") { Actions({ editing = x }, { scope.launch { db.treatments().delete(x) } }) } } } }
    if (adding) TreatmentEditor(null, { x -> scope.launch { db.treatments().add(x) }; adding = false }, { adding = false }); editing?.let { old -> TreatmentEditor(old, { x -> scope.launch { db.treatments().update(x) }; editing = null }, { editing = null }) }
}
@Composable private fun TreatmentEditor(item: Treatment?, save: (Treatment) -> Unit, cancel: () -> Unit) {
    var crop by remember(item) { mutableStateOf(item?.crop ?: "") }; var issue by remember(item) { mutableStateOf(item?.issue ?: "") }; var product by remember(item) { mutableStateOf(item?.product ?: "") }; var date by remember(item) { mutableStateOf(item?.date ?: "") }; var note by remember(item) { mutableStateOf(item?.note ?: "") }
    EditorDialog(if (item == null) "Новая обработка" else "Изменить обработку", cancel, { if (crop.isNotBlank()) save(Treatment(item?.id ?: 0, crop, issue, product, date, note)) }) { Field(crop, { crop = it }, "Культура"); Field(issue, { issue = it }, "Болезнь / вредитель / профилактика"); Field(product, { product = it }, "Препарат"); Field(date, { date = it }, "Дата"); Field(note, { note = it }, "Примечание") }
}

@Composable private fun CalendarScreen(db: AppDb) {
    val irrigations by db.irrigations().all().collectAsState(emptyList()); val tasks by db.tasks().all().collectAsState(emptyList())
    LazyColumn(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) { item { Text("📅 Календарь", style = MaterialTheme.typography.headlineSmall) }; item { Text("Поливы и аграрные задачи. Редактирование выполняется в разделах Полив и Напоминания.") }; items(irrigations) { Text("💧 ${it.date} ${it.time} • ${it.crop} • ${it.volume} л") }; items(tasks) { Text("🔔 ${it.date} ${it.time} • ${it.title} ${if (it.done) "✅" else ""}") } }
}

@Composable private fun WarehouseScreen(db: AppDb) {
    val seeds by db.seeds().all().collectAsState(emptyList()); val fertilizers by db.fertilizers().all().collectAsState(emptyList())
    LazyColumn(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { item { Text("📦 Склад", style = MaterialTheme.typography.headlineSmall) }; item { Text("Семена") }; items(seeds) { InfoCard("🌱 ${it.name}", "${it.quantity} ${it.unit} • партия ${it.batch} • срок ${it.expiry}") }; item { Text("Удобрения") }; items(fertilizers) { InfoCard(if (it.quantity <= it.minStock) "⚠️ ${it.name}" else "🧪 ${it.name}", "${it.quantity} ${it.unit} • минимум ${it.minStock}") } }
}

@Composable private fun ProfitScreen(db: AppDb) {
    val sales by db.sales().all().collectAsState(emptyList()); val expenses by db.expenses().all().collectAsState(emptyList()); val revenue = sales.sumOf { it.quantity * it.price }; val costs = expenses.sumOf { it.amount }
    LazyColumn(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { item { Text("📊 Прибыль", style = MaterialTheme.typography.headlineSmall) }; item { InfoCard("Выручка", money(revenue)) }; item { InfoCard("Расходы", money(costs)) }; item { InfoCard("Прибыль", money(revenue - costs)) }; item { Text("Показатель рассчитывается автоматически из Продаж и Расходов.") } }
}

@Composable private fun NotesScreen(db: AppDb) {
    val list by db.notes().all().collectAsState(emptyList()); val scope = rememberCoroutineScope(); var query by remember { mutableStateOf("") }; var adding by remember { mutableStateOf(false) }; var editing by remember { mutableStateOf<Note?>(null) }
    CrudHeader("Блокнот", query, { query = it }, { adding = true }); LazyColumn(Modifier.padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) { items(list.filter { it.title.contains(query, true) || it.text.contains(query, true) }) { x -> Card(Modifier.fillMaxWidth()) { ListRow("📝 ${x.title}", x.text) { Actions({ editing = x }, { scope.launch { db.notes().delete(x) } }) } } } }
    if (adding) NoteEditor(null, { x -> scope.launch { db.notes().add(x) }; adding = false }, { adding = false }); editing?.let { old -> NoteEditor(old, { x -> scope.launch { db.notes().update(x) }; editing = null }, { editing = null }) }
}
@Composable private fun NoteEditor(item: Note?, save: (Note) -> Unit, cancel: () -> Unit) {
    var title by remember(item) { mutableStateOf(item?.title ?: "") }; var text by remember(item) { mutableStateOf(item?.text ?: "") }
    EditorDialog(if (item == null) "Новая запись" else "Изменить запись", cancel, { if (title.isNotBlank()) save(Note(item?.id ?: 0, title, text, item?.createdAt ?: System.currentTimeMillis())) }) { Field(title, { title = it }, "Заголовок"); Field(text, { text = it }, "Текст") }
}

@Composable private fun CrudHeader(title: String, query: String, setQuery: (String) -> Unit, add: () -> Unit) { Column(Modifier.padding(12.dp)) { Text(title, style = MaterialTheme.typography.headlineSmall); Spacer(Modifier.height(6.dp)); SearchField(query, setQuery); Spacer(Modifier.height(6.dp)); Button(add, Modifier.fillMaxWidth()) { Text("➕ Добавить") } } }
@Composable private fun ListRow(title: String, details: String, actions: @Composable () -> Unit) { Column(Modifier.padding(12.dp)) { Text(title, style = MaterialTheme.typography.titleMedium); Spacer(Modifier.height(4.dp)); Text(details); Spacer(Modifier.height(4.dp)); Divider(); actions() } }
@Composable private fun EditorDialog(title: String, cancel: () -> Unit, save: () -> Unit, content: @Composable ColumnScope.() -> Unit) { AlertDialog(onDismissRequest = cancel, title = { Text(title) }, text = { Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp), content = content) }, confirmButton = { Button(save) { Text("Сохранить") } }, dismissButton = { TextButton(cancel) { Text("Отмена") } }) }
private fun money(value: Double): String = String.format("%.2f", value)
