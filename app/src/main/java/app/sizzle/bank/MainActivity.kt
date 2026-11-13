package app.sizzle.bank

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Orange = Color(0xFFB83B00)
private val BrightOrange = Color(0xFFFF691F)
private val Ink = Color(0xFF20201E)
private val Concrete = Color(0xFFF4F3EF)
private val Muted = Color(0xFF62615B)
private val SizzleColors = lightColorScheme(
    primary = Orange, onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDBC9), onPrimaryContainer = Color(0xFF391000),
    secondaryContainer = Color(0xFFE9E7E1), onSecondaryContainer = Ink,
    background = Concrete, onBackground = Ink,
    surface = Concrete, onSurface = Ink, onSurfaceVariant = Muted,
    surfaceContainer = Color(0xFFECEBE5), outlineVariant = Color(0xFFD8D5CD)
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { MaterialTheme(colorScheme = SizzleColors) { HomeScreen() } }
    }
}

data class DemoCard(val name: String, val number: String, val balance: String)
data class Operation(val title: String, val category: String, val amount: String, val incoming: Boolean = false)
object DemoData {
    val cards = listOf(DemoCard("Щоденна", "4821", "42 850,00 ₴"), DemoCard("Віртуальна", "9037", "8 400,00 ₴"))
    val operations = listOf(
        Operation("Сільпо", "Сьогодні, 12:48 · Продукти", "− 842,50 ₴"),
        Operation("Олексій К.", "Сьогодні, 10:16 · Переказ", "+ 2 000,00 ₴", true),
        Operation("ONE LOVE", "Сьогодні, 09:05 · Кава", "− 125,00 ₴"),
        Operation("Київстар", "Учора, 18:32 · Мобільний зв’язок", "− 250,00 ₴"),
        Operation("Зарплата", "Учора, 09:00 · Надходження", "+ 32 000,00 ₴", true)
    )
}

@Composable
private fun TechnicalLabel(text: String, color: Color = Muted) {
    Text(text, fontFamily = FontFamily.Monospace, fontSize = 11.sp, letterSpacing = 1.2.sp, color = color)
}

@Composable
fun HomeScreen() {
    var selectedTab by rememberSaveable { mutableStateOf("Головна") }
    var hidden by rememberSaveable { mutableStateOf(false) }
    var action by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedCard by rememberSaveable { mutableStateOf<String?>(null) }
    Surface(Modifier.fillMaxSize(), color = Concrete) {
        LazyColumn(
            modifier = Modifier.safeDrawingPadding(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(Modifier.size(34.dp).background(BrightOrange, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                            Text("S", color = Ink, fontWeight = FontWeight.Black, fontSize = 25.sp)
                        }
                        Text("sizzle", fontSize = 29.sp, letterSpacing = (-1).sp, fontWeight = FontWeight.Black)
                    }
                    AssistChip(onClick = { action = "Демо-режим" }, label = { TechnicalLabel("ДЕМО / UA", Ink) })
                }
            }
            item {
                SectionTabs(selectedTab = selectedTab, onSelect = { selectedTab = it })
            }
            if (selectedTab == "Послуги") {
                item { Text("Послуги", fontSize = 28.sp, fontWeight = FontWeight.Bold) }
                item { TechnicalLabel("ЩОДЕННІ ПЛАТЕЖІ / ДЕМО") }
                items(listOf("Мобільний зв’язок", "Комунальні послуги", "Інтернет", "Платіж за IBAN")) { service ->
                    Card(onClick = { action = service }, colors = CardDefaults.cardColors(containerColor = Color(0xFFE7E5DF)), shape = RoundedCornerShape(20.dp)) {
                        Row(Modifier.fillMaxWidth().padding(20.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(service, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Orange)
                        }
                    }
                }
            }
            if (selectedTab != "Послуги") {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    TechnicalLabel("ТВОЇ ГРОШІ. ТВІЙ РИТМ.")
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Загальний баланс", style = MaterialTheme.typography.titleMedium)
                        TextButton(onClick = { hidden = !hidden }, contentPadding = PaddingValues(horizontal = 8.dp)) { Text(if (hidden) "Показати" else "Сховати", fontSize = 12.sp) }
                    }
                    Text(if (hidden) "••• ••• ₴" else "51 250,00 ₴", fontSize = 40.sp, lineHeight = 46.sp, letterSpacing = (-1.5).sp, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        Box(Modifier.size(6.dp).background(Orange, CircleShape))
                        Text("Українська гривня · 2 картки", fontSize = 12.sp, color = Muted)
                    }
                }
            }
            }
            if (selectedTab == "Головна") {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    QuickAction("Поповнити", Icons.Default.Add, Modifier.weight(1f), true) { action = "Поповнити" }
                    QuickAction("Переказати", Icons.AutoMirrored.Filled.Send, Modifier.weight(1f)) { action = "Переказати" }
                    QuickAction("Сплатити", Icons.Default.ShoppingCart, Modifier.weight(1f)) { action = "Сплатити" }
                }
            }
            }
            if (selectedTab != "Послуги") {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Картки", fontSize = 23.sp, fontWeight = FontWeight.Bold)
                        TechnicalLabel("02 / UAH")
                    }
                    BoxWithConstraints {
                        val cardWidth = minOf(maxWidth - 18.dp, 340.dp)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(DemoData.cards, key = { it.number }) { card ->
                                val primary = card.number == "4821"
                                val foreground = if (primary) Color(0xFFF6F4ED) else Ink
                                Card(onClick = { selectedCard = card.number }, modifier = Modifier.width(cardWidth), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = if (primary) Ink else Color(0xFFE4E1D9))) {
                                    Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                            TechnicalLabel("SIZZLE / ${if (primary) "01" else "02"}", if (primary) BrightOrange else Orange)
                                            Text(card.name, color = foreground, fontSize = 12.sp)
                                        }
                                        Text(if (hidden) "••• ••• ₴" else card.balance, color = foreground, fontWeight = FontWeight.Medium, fontSize = 29.sp, letterSpacing = (-0.5).sp)
                                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                                            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                                TechnicalLabel("ДЕМО-КАРТКА", if (primary) Color(0xFFB9B8B0) else Muted)
                                                Text("••••  ${card.number}", color = foreground, fontFamily = FontFamily.Monospace, fontSize = 15.sp)
                                            }
                                            Box(Modifier.width(42.dp).height(26.dp).background(if (primary) BrightOrange else Ink, RoundedCornerShape(5.dp)), contentAlignment = Alignment.Center) {
                                                Text("S /", color = if (primary) Ink else Color.White, fontWeight = FontWeight.Black, fontSize = 15.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            }
            if (selectedTab == "Гаманець") {
                item {
                    OutlinedCard(shape = RoundedCornerShape(20.dp)) {
                        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            TechnicalLabel("РАХУНКИ / UAH")
                            Text("2 картки у гривні", fontWeight = FontWeight.SemiBold)
                            Text("Обидві картки демонстраційні. Реквізити та банківський API поки не підключено.", fontSize = 14.sp, color = Muted)
                        }
                    }
                }
            }
            if (selectedTab == "Головна") {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Рух коштів", fontSize = 23.sp, fontWeight = FontWeight.Bold)
                        TechnicalLabel("ЛИСТОПАД")
                    }
                    TechnicalLabel("ОСТАННІ ОПЕРАЦІЇ / ДЕМО")
                }
            }
            items(DemoData.operations, key = { it.title }) { operation ->
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Surface(shape = RoundedCornerShape(14.dp), color = if (operation.incoming) Color(0xFFFFDBC9) else Color(0xFFE7E5DF), modifier = Modifier.size(44.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                if (operation.incoming) Icon(Icons.Default.Add, contentDescription = null, tint = Orange, modifier = Modifier.size(20.dp))
                                else Text(operation.title.take(1), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                        }
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(operation.title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                            Text(operation.category, color = Muted, fontSize = 11.sp)
                        }
                        Text(if (hidden) "•••• ₴" else operation.amount, color = if (operation.incoming) Orange else Ink, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                    HorizontalDivider(color = Color(0xFFDEDBD3))
                }
            }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TechnicalLabel("SIZZLE / 0.2.1")
                    TechnicalLabel("УСІ ДАНІ ВИГАДАНІ")
                }
            }
        }
    }
    action?.let { title -> DemoActionDialog(title) { action = null } }
    selectedCard?.let { number ->
        AlertDialog(onDismissRequest = { selectedCard = null }, title = { Text("Картка •••• $number") },
            text = { Text("Демонстраційна картка Sizzle у гривні. Реквізити вигадані. Банківський API не підключено.") },
            confirmButton = { TextButton(onClick = { selectedCard = null }) { Text("Зрозуміло") } })
    }
}

@Composable
private fun SectionTabs(selectedTab: String, onSelect: (String) -> Unit) {
    Row(Modifier.fillMaxWidth().selectableGroup(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("Головна", "Послуги", "Гаманець").forEach { tab ->
            val selected = selectedTab == tab
            Surface(
                modifier = Modifier.weight(1f).selectable(selected = selected, role = Role.Tab, onClick = { onSelect(tab) }),
                shape = CircleShape,
                color = if (selected) Orange else Color(0xFFE7E5DF),
                contentColor = if (selected) Color.White else Ink
            ) {
                Box(Modifier.heightIn(min = 48.dp).padding(horizontal = 6.dp, vertical = 12.dp), contentAlignment = Alignment.Center) {
                    Text(tab, fontSize = 13.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun QuickAction(label: String, icon: ImageVector, modifier: Modifier, accent: Boolean = false, onClick: () -> Unit) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        FilledTonalIconButton(onClick = onClick, modifier = Modifier.fillMaxWidth().height(58.dp), shape = RoundedCornerShape(18.dp),
            colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = if (accent) Orange else Color(0xFFE7E5DF), contentColor = if (accent) Color.White else Ink)) {
            Icon(icon, contentDescription = label, modifier = Modifier.size(24.dp))
        }
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun DemoActionDialog(title: String, onDismiss: () -> Unit) {
    var amount by rememberSaveable(title) { mutableStateOf("") }
    var recipient by rememberSaveable(title) { mutableStateOf("") }
    var preview by rememberSaveable(title) { mutableStateOf(false) }
    val information = title == "Демо-режим"
    val value = amount.replace(',', '.').toBigDecimalOrNull()
    val valid = value != null && value > java.math.BigDecimal.ZERO && value <= java.math.BigDecimal("1000000") && value.scale() <= 2 && (title == "Поповнити" || recipient.isNotBlank())
    AlertDialog(onDismissRequest = onDismiss, title = { Text(title) }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Демо-режим. Справжні кошти не списуються, дані нікуди не надсилаються.")
            if (!information && !preview) {
                if (title != "Поповнити") OutlinedTextField(value = recipient, onValueChange = { recipient = it.take(80) }, label = { Text(if (title == "Переказати") "Одержувач" else "Послуга або одержувач") }, singleLine = true)
                OutlinedTextField(value = amount, onValueChange = { amount = it.take(12) }, label = { Text("Сума у гривнях") }, singleLine = true, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal))
                Text("Від 0,01 до 1 000 000 ₴", fontSize = 12.sp)
            }
            if (preview) Text("Перегляд: $amount ₴${if (recipient.isNotBlank()) " · $recipient" else ""}. Операцію не виконано.")
        }
    }, confirmButton = { TextButton(onClick = { if (information || preview) onDismiss() else preview = true }, enabled = information || preview || valid) { Text(if (information || preview) "Закрити" else "Перегляд") } }, dismissButton = { if (!information && !preview) TextButton(onClick = onDismiss) { Text("Скасувати") } })
}
