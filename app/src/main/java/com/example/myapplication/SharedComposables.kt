package com.example.myapplication

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class ExpenseSheet(
    val id: Int,
    val month: String,
    val year: Int
)
data class Expense(
    val id: Int,
    val description: String,
    val amount: Double,
    val category: String,
    val dayOfMonth: Int
)

sealed class Screen {
    object SheetList : Screen()
    data class SheetDetail(val sheetId: Int) : Screen()
    data class AddExpense(val sheetId: Int) : Screen()
}

@Composable
fun ExpenseTrackerApp() {
    var sheets by remember { mutableStateOf(listOf<ExpenseSheet>()) }
    var nextSheetId by remember { mutableStateOf(1) }
    var nextExpenseId by remember { mutableStateOf(1) }

    var currentScreen by remember { mutableStateOf<Screen>(Screen.SheetList) }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            when (val screen = currentScreen) {
                is Screen.SheetList -> {
                    SheetListScreen(
                        sheets = sheets,
                        onCreateSheet = { month, year ->
                            val cleanMonth = month.trim()
                            val yearInt = year.toIntOrNull()

                            if (cleanMonth.isNotEmpty() && yearInt != null) {
                                val newSheet = ExpenseSheet(
                                    id = nextSheetId,
                                    month = cleanMonth,
                                    year = yearInt
                                )
                                sheets = sheets + newSheet
                                nextSheetId++
                            }
                        },
                        onOpenSheet = { sheetId ->
                            currentScreen = Screen.SheetDetail(sheetId)
                        }
                    )
                }

                is Screen.SheetDetail -> {
                    val sheet = sheets.find { it.id == screen.sheetId }
                    if (sheet == null) {
                        currentScreen = Screen.SheetList
                    } else {
                        SheetDetailScreen(
                            sheet = sheet,
                            onBack = { currentScreen = Screen.SheetList },
                            onIncomeChange = { newIncome ->
                                sheets = sheets.map {
                                    if (it.id == sheet.id) it.copy(income = newIncome)
                                    else it
                                }
                            },
                            onAddExpenseClick = {
                                currentScreen = Screen.AddExpense(sheet.id)
                            }
                        )
                    }
                }

                is Screen.AddExpense -> {
                    val sheet = sheets.find { it.id == screen.sheetId }
                    if (sheet == null) {
                        currentScreen = Screen.SheetList
                    } else {
                        AddExpenseScreen(
                            sheet = sheet,
                            onBack = { currentScreen = Screen.SheetDetail(sheet.id) },
                            onSaveExpense = { description, amount, category, day ->
                                val newExpense = Expense(
                                    id = nextExpenseId,
                                    description = description,
                                    amount = amount,
                                    category = category,
                                    dayOfMonth = day
                                )

                                sheets = sheets.map {
                                    if (it.id == sheet.id) {
                                        it.copy(expenses = it.expenses + newExpense)
                                    } else it
                                }

                                nextExpenseId++
                                currentScreen = Screen.SheetDetail(sheet.id)
                            }
                        )
                    }
                }
            }
        }
    }
}



@Composable
fun SheetListScreen(
    sheets: List<ExpenseSheet>,
    onCreateSheet: (month: String, year: String) -> Unit,
    onOpenSheet: (sheetId: Int) -> Unit
) {

    var showNewSheetForm by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Expense Sheets",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Create one sheet per month. Tap a sheet to open it.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { showNewSheetForm = !showNewSheetForm }) {
            Text(
                text = if (showNewSheetForm) "Hide new sheet form" else "Create new sheet"
            )
        }

        if (showNewSheetForm) {
            Spacer(modifier = Modifier.height(12.dp))
            NewSheetForm(
                onCreateSheet = { month, year ->
                    onCreateSheet(month, year)
                    showNewSheetForm = false
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (sheets.isEmpty()) {
            Text(
                text = "No sheets yet. Tap \"Create new sheet\" to get started.",
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            Text(
                text = "Your sheets:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(sheets) { sheet ->
                    SheetListItem(
                        sheet = sheet,
                        onClick = { onOpenSheet(sheet.id) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewSheetForm(
    onCreateSheet: (month: String, year: String) -> Unit
) {
    var monthText by remember { mutableStateOf("") }
    var yearText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "New monthly sheet",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = monthText,
            onValueChange = { monthText = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Month (e.g. January)") }
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = yearText,
            onValueChange = { yearText = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Year (e.g. 2025)") }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                onCreateSheet(monthText, yearText)
            }
        ) {
            Text("Save sheet")
        }
    }
}

@Composable
fun SheetListItem(
    sheet: ExpenseSheet,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp)
    ) {
        Text(
            text = "${sheet.month} ${sheet.year}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Tap to view or edit expenses",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun SheetDetailScreen(
    sheet: ExpenseSheet,
    onBack: () -> Unit
) {
    val dummyExpenses = emptyList<Expense>()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = onBack) {
                Text("Back")
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "${sheet.month} ${sheet.year}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Monthly expenses",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (dummyExpenses.isEmpty()) {
            Text(
                text = "No expenses added yet for this month.",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Soon you'll be able to add items like rent, groceries, travel, etc.",
                style = MaterialTheme.typography.bodySmall
            )
        } else {
            LazyColumn {
                items(dummyExpenses) { expense ->
                    Text("${expense.description}: ${expense.amount}")
                }
            }
        }
    }
}
