package com.agrogarden

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.agrogarden.data.AppDb
import com.agrogarden.data.Crop
import com.agrogarden.data.Fertilizer
import com.agrogarden.data.Irrigation
import com.agrogarden.data.Seed
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= 33) {
            requestPermissions(
                arrayOf("android.permission.POST_NOTIFICATIONS"),
                10
            )
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "agro",
                "AgroGarden",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }

        setContent {
            val db = AppDb.get(this)
            AgroApp(db)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgroApp(db: AppDb) {

    val selectedTabState = remember {
        mutableStateOf(0)
    }

    val selectedTab = selectedTabState.value

    val tabs = listOf(
        "🌱 Посевы",
        "🌾 Семена",
        "🧪 Удобрения",
        "💧 Полив",
        "🔔 Задачи",
        "💰 Продажи",
        "🧺 Урожай",
        "📉 Расходы",
        "🛡️ Обработки",
        "📅 Календарь",
        "📦 Склад",
        "📊 Прибыль"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("AgroGarden")
                }
            )
        },

        bottomBar = {
            NavigationBar {

                val visibleTabs = tabs.take(4)

                visibleTabs.forEachIndexed { index, title ->

                    NavigationBarItem(
                        selected = selectedTab == index,

                        onClick = {
                            selectedTabState.value = index
                        },

                        icon = {
                            Text(title.take(2))
                        },

                        label = {
                            Text(
                                title
                                    .removeRange(0, 2)
                                    .take(8)
                            )
                        }
                    )
                }
            }
        }

    ) { paddingValues ->

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(12.dp)
        ) {

            Text(
                text = tabs[selectedTab],
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            when (selectedTab) {

                0 -> CropScreen(db)

                1 -> SeedScreen(db)

                2 -> FertilizerScreen(db)

                3 -> IrrigationScreen(db)

                else -> DashboardScreen(
                    title = tabs[selectedTab]
                )
            }
        }
    }
}

@Composable
fun DashboardScreen(title: String) {

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = "Раздел AgroGarden готов к подключению."
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = "Данные приложения сохраняются локально в базе Room."
            )
        }
    }
}

@Composable
fun CropScreen(db: AppDb) {

    val list by db.crops()
        .all()
        .collectAsState(emptyList())

    val scope = rememberCoroutineScope()

    var name by remember {
        mutableStateOf("")
    }

    Column {

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {

            OutlinedTextField(
                value = name,

                onValueChange = {
                    name = it
                },

                modifier = Modifier.weight(1f),

                label = {
                    Text("Культура")
                }
            )

            Button(
                onClick = {

                    if (name.isNotBlank()) {

                        scope.launch {

                            db.crops().add(
                                Crop(
                                    name = name,
                                    area = 0.0,
                                    sowDate = "",
                                    harvestDate = "",
                                    status = "Запланировано",
                                    notes = ""
                                )
                            )
                        }

                        name = ""
                    }
                },

                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text("➕")
            }
        }

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        LazyColumn {

            items(list) { crop ->

                ListItem(
                    headlineContent = {
                        Text(crop.name)
                    },

                    supportingContent = {
                        Text(
                            "Площадь: ${crop.area} | ${crop.status}"
                        )
                    },

                    trailingContent = {

                        TextButton(
                            onClick = {

                                scope.launch {
                                    db.crops().delete(crop)
                                }
                            }
                        ) {
                            Text("🗑️")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun SeedScreen(db: AppDb) {

    val list by db.seeds()
        .all()
        .collectAsState(emptyList())

    val scope = rememberCoroutineScope()

    var name by remember {
        mutableStateOf("")
    }

    Column {

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {

            OutlinedTextField(
                value = name,

                onValueChange = {
                    name = it
                },

                modifier = Modifier.weight(1f),

                label = {
                    Text("Семена")
                }
            )

            Button(
                onClick = {

                    if (name.isNotBlank()) {

                        scope.launch {

                            db.seeds().add(
                                Seed(
                                    name = name,
                                    quantity = 0.0,
                                    unit = "шт",
                                    batch = "",
                                    expiry = ""
                                )
                            )
                        }

                        name = ""
                    }
                },

                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text("➕")
            }
        }

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        LazyColumn {

            items(list) { seed ->

                ListItem(
                    headlineContent = {
                        Text(seed.name)
                    },

                    supportingContent = {
                        Text(
                            "${seed.quantity} ${seed.unit} | " +
                                "партия ${seed.batch} | " +
                                "годность ${seed.expiry}"
                        )
                    },

                    trailingContent = {

                        TextButton(
                            onClick = {

                                scope.launch {
                                    db.seeds().delete(seed)
                                }
                            }
                        ) {
                            Text("🗑️")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun FertilizerScreen(db: AppDb) {

    val list by db.fertilizers()
        .all()
        .collectAsState(emptyList())

    val scope = rememberCoroutineScope()

    var name by remember {
        mutableStateOf("")
    }

    Column {

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {

            OutlinedTextField(
                value = name,

                onValueChange = {
                    name = it
                },

                modifier = Modifier.weight(1f),

                label = {
                    Text("Удобрение")
                }
            )

            Button(
                onClick = {

                    if (name.isNotBlank()) {

                        scope.launch {

                            db.fertilizers().add(
                                Fertilizer(
                                    name = name,
                                    quantity = 0.0,
                                    unit = "кг",
                                    minStock = 1.0
                                )
                            )
                        }

                        name = ""
                    }
                },

                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text("➕")
            }
        }

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        LazyColumn {

            items(list) { fertilizer ->

                ListItem(
                    headlineContent = {
                        Text(fertilizer.name)
                    },

                    supportingContent = {
                        Text(
                            "Остаток: ${fertilizer.quantity} " +
                                "${fertilizer.unit}; " +
                                "минимум: ${fertilizer.minStock}"
                        )
                    },

                    trailingContent = {

                        if (fertilizer.quantity <= fertilizer.minStock) {
                            Text("⚠️")
                        } else {
                            Text("OK")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun IrrigationScreen(db: AppDb) {

    val list by db.irrigations()
        .all()
        .collectAsState(emptyList())

    val scope = rememberCoroutineScope()

    var crop by remember {
        mutableStateOf("")
    }

    Column {

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {

            OutlinedTextField(
                value = crop,

                onValueChange = {
                    crop = it
                },

                modifier = Modifier.weight(1f),

                label = {
                    Text("Культура")
                }
            )

            Button(
                onClick = {

                    if (crop.isNotBlank()) {

                        scope.launch {

                            db.irrigations().add(
                                Irrigation(
                                    crop = crop,
                                    date = "",
                                    time = "",
                                    volume = 0.0,
                                    repeatDays = 7
                                )
                            )
                        }

                        crop = ""
                    }
                },

                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text("➕")
            }
        }

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        LazyColumn {

            items(list) { irrigation ->

                ListItem(
                    headlineContent = {
                        Text(irrigation.crop)
                    },

                    supportingContent = {
                        Text(
                            "Дата: ${irrigation.date} | " +
                                "Время: ${irrigation.time} | " +
                                "Объём: ${irrigation.volume} | " +
                                "каждые ${irrigation.repeatDays} дн."
                        )
                    },

                    trailingContent = {

                        TextButton(
                            onClick = {

                                scope.launch {
                                    db.irrigations().delete(irrigation)
                                }
                            }
                        ) {
                            Text("🗑️")
                        }
                    }
                )
            }
        }
    }
}
