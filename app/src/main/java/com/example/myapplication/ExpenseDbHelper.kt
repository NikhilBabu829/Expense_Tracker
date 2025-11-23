package com.example.myapplication

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ExpenseDbHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "expenses.db"
        private const val DATABASE_VERSION = 1

        private const val TABLE_SHEETS = "sheets"
        private const val TABLE_EXPENSES = "expenses"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createSheets = """
            CREATE TABLE $TABLE_SHEETS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                month_name TEXT NOT NULL,
                month_index INTEGER NOT NULL,
                year INTEGER NOT NULL,
                income REAL NOT NULL DEFAULT 0
            );
        """.trimIndent()

        val createExpenses = """
            CREATE TABLE $TABLE_EXPENSES (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                sheet_id INTEGER NOT NULL,
                description TEXT NOT NULL,
                category TEXT,
                amount REAL NOT NULL,
                day_of_month INTEGER NOT NULL,
                FOREIGN KEY(sheet_id) REFERENCES $TABLE_SHEETS(id)
            );
        """.trimIndent()

        db.execSQL(createSheets)
        db.execSQL(createExpenses)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_EXPENSES")
        onCreate(db)
    }


    fun getAllSheetsWithExpenses(): List<ExpenseSheet> {
        val result = mutableListOf<ExpenseSheet>()
        val db = readableDatabase

        val cursor = db.query(
            TABLE_SHEETS,
            arrayOf("id", "month_name", "month_index", "year", "income"),
            null,
            null,
            null,
            null,
            "year ASC, month_index ASC"
        )

        cursor.use { c ->
            while (c.moveToNext()) {
                val id = c.getInt(0)
                val monthName = c.getString(1)
                val monthIndex = c.getInt(2)
                val year = c.getInt(3)
                val income = c.getDouble(4)

                val expenses = getExpensesForSheetInternal(db, id)

                result.add(
                    ExpenseSheet(
                        id = id,
                        month = monthName,
                        year = year,
                        income = income,
                        expenses = expenses,
                        monthIndex = monthIndex
                    )
                )
            }
        }

        return result
    }

    fun insertSheet(
        monthName: String,
        monthIndex: Int,
        year: Int,
        income: Double = 0.0
    ): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("month_name", monthName)
            put("month_index", monthIndex)
            put("year", year)
            put("income", income)
        }
        return db.insert(TABLE_SHEETS, null, values)
    }

    fun updateSheetIncome(sheetId: Int, income: Double): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("income", income)
        }
        return db.update(TABLE_SHEETS, values, "id = ?", arrayOf(sheetId.toString()))
    }

    fun insertExpense(
        sheetId: Int,
        description: String,
        category: String,
        amount: Double,
        dayOfMonth: Int
    ): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("sheet_id", sheetId)
            put("description", description)
            put("category", category)
            put("amount", amount)
            put("day_of_month", dayOfMonth)
        }
        return db.insert(TABLE_EXPENSES, null, values)
    }

    fun updateExpense(sheetId: Int, expense: Expense): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("sheet_id", sheetId)
            put("description", expense.description)
            put("category", expense.category)
            put("amount", expense.amount)
            put("day_of_month", expense.dayOfMonth)
        }
        return db.update(
            TABLE_EXPENSES,
            values,
            "id = ?",
            arrayOf(expense.id.toString())
        )
    }

    fun deleteExpense(expenseId: Int): Int {
        val db = writableDatabase
        return db.delete(TABLE_EXPENSES, "id = ?", arrayOf(expenseId.toString()))
    }


    private fun getExpensesForSheetInternal(
        db: SQLiteDatabase,
        sheetId: Int
    ): List<Expense> {
        val result = mutableListOf<Expense>()
        val cursor = db.query(
            TABLE_EXPENSES,
            arrayOf("id", "description", "category", "amount", "day_of_month"),
            "sheet_id = ?",
            arrayOf(sheetId.toString()),
            null,
            null,
            "day_of_month ASC"
        )

        return result
    }
}