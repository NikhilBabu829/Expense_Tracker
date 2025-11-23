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
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SheetDetailScreen(
    sheet: ExpenseSheet,
    onBack: () -> Unit,
    onIncomeChange: (Double) -> Unit,
    onAddExpenseClick: () -> Unit
) {
    var incomeText by remember(sheet.id, sheet.income) {
        mutableStateOf(if (sheet.income == 0.0) "" else sheet.income.toString())
    }

    val totalExpenses = sheet.expenses.sumOf { it.amount }
    val surplus = sheet.income - totalExpenses

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top bar
        Row(verticalAlignment = Alignment.CenterVertically) {
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
                    text = "Monthly overview",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Income input
        Text(
            text = "Monthly income",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(4.dp))

        TextField(
            value = incomeText,
            onValueChange = { incomeText = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Enter your salary for this month") }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                val newIncome = incomeText.toDoubleOrNull()
                if (newIncome != null) {
                    onIncomeChange(newIncome)
                }
            }
        ) {
            Text("Save income")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Summary
        Text(
            text = "Summary",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(4.dp))

        Text("Income: ${"%.2f".format(sheet.income)}")
        Text("Total expenses: ${"%.2f".format(totalExpenses)}")

        if (sheet.income == 0.0 && sheet.expenses.isEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Enter your monthly income and add expenses to see your balance.",
                style = MaterialTheme.typography.bodySmall
            )
        } else {
            Spacer(modifier = Modifier.height(4.dp))
            if (surplus >= 0) {
                Text("You have ${"%.2f".format(surplus)} left this month.")
            } else {
                Text("You are short of ${"%.2f".format(-surplus)} this month.")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Add expense button
        Button(onClick = onAddExpenseClick) {
            Text("Add expense")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // List of expenses for this month
        if (sheet.expenses.isEmpty()) {
            Text(
                text = "No expenses added yet for this month.",
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            Text(
                text = "Expenses:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn {
                items(sheet.expenses) { expense ->
                    ExpenseListItem(expense = expense)
                }
            }
        }
    }
}

@Composable
fun ExpenseListItem(expense: Expense) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = "${expense.dayOfMonth} - ${expense.description}",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Category: ${expense.category}",
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = "Amount: ${"%.2f".format(expense.amount)}",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    sheet: ExpenseSheet,
    onBack: () -> Unit,
    onSaveExpense: (description: String, amount: Double, category: String, day: Int) -> Unit
) {
    var descriptionText by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var categoryText by remember { mutableStateOf("") }
    var dayText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = onBack) {
                Text("Cancel")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "Add expense",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "For ${sheet.month} ${sheet.year}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = dayText,
            onValueChange = { dayText = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Day of month (1–31)") }
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = descriptionText,
            onValueChange = { descriptionText = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Description (e.g. Groceries)") }
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = categoryText,
            onValueChange = { categoryText = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Category (e.g. Food, Rent)") }
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = amountText,
            onValueChange = { amountText = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Amount") }
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        Button(
            onClick = {
                val day = dayText.toIntOrNull()
                val amount = amountText.toDoubleOrNull()
                val description = descriptionText.trim()
                val category = categoryText.trim()

                if (day == null || day !in 1..31) {
                    errorMessage = "Please enter a valid day between 1 and 31."
                } else if (amount == null || amount <= 0.0) {
                    errorMessage = "Please enter a valid amount greater than 0."
                } else if (description.isEmpty()) {
                    errorMessage = "Please enter a description."
                } else {
                    errorMessage = null
                    onSaveExpense(description, amount, category, day)
                }
            }
        ) {
            Text("Save expense")
        }
    }
}

@Composable
fun IncomeExpensesChart(
    sheets: List<ExpenseSheet>
) {
    val incomes = sheets.map { it.income }
    val expenses = sheets.map { it.expenses.sumOf { e -> e.amount } }

    val maxValue = (incomes + expenses).maxOrNull() ?: 0.0
    val safeMax = if (maxValue <= 0.0) 1.0 else maxValue

    val incomeColor = Color(0xFF1565C0)
    val expenseColor = Color(0xFFC62828)
    val axisColor = Color.Black

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Title
        Text(
            text = "Income/Expenses",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(8.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height

                val leftPadding = 60f
                val rightPadding = 24f
                val topPadding = 24f
                val bottomPadding = 60f

                val chartLeft = leftPadding
                val chartRight = width - rightPadding
                val chartTop = topPadding
                val chartBottom = height - bottomPadding

                drawLine(
                    color = axisColor,
                    start = Offset(chartLeft, chartTop),
                    end = Offset(chartLeft, chartBottom),
                    strokeWidth = 3f
                )

                drawLine(
                    color = axisColor,
                    start = Offset(chartLeft, chartBottom),
                    end = Offset(chartRight, chartBottom),
                    strokeWidth = 3f
                )

                val count = sheets.size
                if (count >= 1) {
                    val chartWidth = chartRight - chartLeft
                    val chartHeight = chartBottom - chartTop

                    val stepX = if (count > 1) chartWidth / (count - 1) else 0f

                    val incomePath = Path()
                    val expensePath = Path()

                    for (i in 0 until count) {
                        val x = chartLeft + stepX * i
                        val incomeY = chartBottom - (incomes[i] / safeMax).toFloat() * chartHeight
                        val expenseY = chartBottom - (expenses[i] / safeMax).toFloat() * chartHeight

                        if (i == 0) {
                            incomePath.moveTo(x, incomeY)
                            expensePath.moveTo(x, expenseY)
                        } else {
                            incomePath.lineTo(x, incomeY)
                            expensePath.lineTo(x, expenseY)
                        }
                    }

                    drawPath(
                        path = incomePath,
                        color = incomeColor,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f)
                    )
                    drawPath(
                        path = expensePath,
                        color = expenseColor,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f)
                    )

                    for (i in 0 until count) {
                        val x = chartLeft + stepX * i
                        val incomeY = chartBottom - (incomes[i] / safeMax).toFloat() * chartHeight
                        val expenseY = chartBottom - (expenses[i] / safeMax).toFloat() * chartHeight

                        drawCircle(
                            color = incomeColor,
                            radius = 6f,
                            center = Offset(x, incomeY)
                        )
                        drawCircle(
                            color = expenseColor,
                            radius = 6f,
                            center = Offset(x, expenseY)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            sheets.forEach { sheet ->
                val label = "${sheet.month.take(3)}\n${sheet.year}"
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Y-axis: amount (0 to ~${"%.2f".format(maxValue)})",
            style = MaterialTheme.typography.bodySmall
        )
    }
}




