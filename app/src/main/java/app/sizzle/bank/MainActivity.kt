package app.sizzle.bank

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme(colorScheme = lightColorScheme(primary = Color(0xFF384D27), background = Color(0xFFF5F6F0))) { HomeScreen() } }
    }
}

data class DemoCard(val name: String, val number: String, val balance: String)
data class Operation(val title: String, val category: String, val amount: String, val incoming: Boolean = false)
object DemoData {
    val cards = listOf(DemoCard("Основная", "4821", "128 450,00 ₽"), DemoCard("Для покупок", "9037", "24 800,00 ₽"))
    val operations = listOf(Operation("Кофейня", "Сегодня, 10:24 · Кафе", "− 320,00 ₽"), Operation("Алексей К.", "Сегодня, 09:10 · Перевод", "+ 5 000,00 ₽", true), Operation("Супермаркет", "Вчера, 18:42 · Продукты", "− 2 180,50 ₽"), Operation("Подписка", "Вчера, 12:00 · Сервисы", "− 299,00 ₽"), Operation("Зарплата", "11 ноября · Пополнение", "+ 95 000,00 ₽", true))
}

@Composable
fun HomeScreen() {
    var hidden by rememberSaveable { mutableStateOf(false) }
    var action by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedCard by rememberSaveable { mutableStateOf<String?>(null) }
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        LazyColumn(modifier = Modifier.safeDrawingPadding(), contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
            item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("sizzle", fontSize = 32.sp, fontWeight = FontWeight.Black)
                SuggestionChip(onClick = { action = "Демо-режим" }, label = { Text("Демо-режим") })
            } }
            item { Column {
                Text("Общий баланс", color = Color(0xFF62685E))
                Text(if (hidden) "••• ••• ₽" else "153 250,00 ₽", fontSize = 36.sp, fontWeight = FontWeight.SemiBold)
                TextButton(onClick = { hidden = !hidden }, contentPadding = PaddingValues(0.dp)) { Text(if (hidden) "Показать баланс" else "Скрыть баланс") }
            } }
            item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Пополнить", "Перевести", "Оплатить").forEach { title ->
                    FilledTonalButton(onClick = { action = title }, modifier = Modifier.weight(1f), contentPadding = PaddingValues(vertical = 18.dp, horizontal = 2.dp), shape = RoundedCornerShape(18.dp)) { Text(title, fontSize = 12.sp) }
                }
            } }
            item { Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Мои карты", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) { items(DemoData.cards) { card ->
                    Card(onClick = { selectedCard = card.number }, modifier = Modifier.width(270.dp), colors = CardDefaults.cardColors(containerColor = if (card.number == "4821") Color(0xFF202B23) else Color(0xFFE0E9D6)), shape = RoundedCornerShape(24.dp)) {
                        val ink = if (card.number == "4821") Color.White else Color(0xFF202B23)
                        Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(card.name, color = ink); Text("S", color = ink, fontWeight = FontWeight.Black) }
                            Text(if (hidden) "••• ••• ₽" else card.balance, color = ink, fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
                            Text("••••  ${card.number}     ·     ДЕМО", color = ink, fontSize = 12.sp)
                        }
                    }
                } }
            } }
            item { Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("История операций", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Тестовые данные · ноябрь", color = Color(0xFF62685E), fontSize = 13.sp)
            } }
            items(DemoData.operations) { operation ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.size(42.dp).background(Color(0xFFE4E9DC), RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) { Text(if (operation.incoming) "↙" else "↗", fontSize = 22.sp) }
                    Column(Modifier.weight(1f)) { Text(operation.title, fontWeight = FontWeight.Medium); Text(operation.category, color = Color(0xFF62685E), fontSize = 11.sp) }
                    Text(if (hidden) "•••• ₽" else operation.amount, color = if (operation.incoming) Color(0xFF316239) else Color(0xFF202B23), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            item { Text("Sizzle 0.1 · Деньги и операции вымышлены", fontSize = 12.sp, color = Color(0xFF62685E)) }
        }
    }
    action?.let { title -> DemoActionDialog(title) { action = null } }
    selectedCard?.let { number -> AlertDialog(onDismissRequest = { selectedCard = null }, title = { Text("Карта •••• $number") }, text = { Text("Демонстрационная карта Sizzle. Реквизиты вымышлены; банковский API не подключён.") }, confirmButton = { TextButton(onClick = { selectedCard = null }) { Text("Закрыть") } }) }
}

@Composable
private fun DemoActionDialog(title: String, onDismiss: () -> Unit) {
    var amount by rememberSaveable(title) { mutableStateOf("") }
    var recipient by rememberSaveable(title) { mutableStateOf("") }
    var preview by rememberSaveable(title) { mutableStateOf(false) }
    val information = title == "Демо-режим"
    val value = amount.replace(',', '.').toBigDecimalOrNull()
    val valid = value != null && value > java.math.BigDecimal.ZERO && value <= java.math.BigDecimal("1000000") && value.scale() <= 2 && (title == "Пополнить" || recipient.isNotBlank())
    AlertDialog(onDismissRequest = onDismiss, title = { Text(title) }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Демо-режим. Реальные деньги не списываются, данные никуда не отправляются.")
            if (!information && !preview) {
                if (title != "Пополнить") OutlinedTextField(value = recipient, onValueChange = { recipient = it.take(80) }, label = { Text(if (title == "Перевести") "Получатель" else "Услуга или получатель") }, singleLine = true)
                OutlinedTextField(value = amount, onValueChange = { amount = it.take(12) }, label = { Text("Сумма в рублях") }, singleLine = true, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal))
                Text("От 0,01 до 1 000 000 ₽", fontSize = 12.sp)
            }
            if (preview) Text("Предпросмотр: $amount ₽${if (recipient.isNotBlank()) " · $recipient" else ""}. Операция не выполнена.")
        }
    }, confirmButton = { TextButton(onClick = { if (information || preview) onDismiss() else preview = true }, enabled = information || preview || valid) { Text(if (information || preview) "Закрыть" else "Предпросмотр") } }, dismissButton = { if (!information && !preview) TextButton(onClick = onDismiss) { Text("Отмена") } })
}
