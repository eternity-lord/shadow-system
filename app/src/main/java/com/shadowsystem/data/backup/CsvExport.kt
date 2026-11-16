package com.shadowsystem.data.backup

import com.shadowsystem.data.db.TransactionEntity
import java.time.LocalDate

/** Transactions as CSV (UTF-8 with BOM so spreadsheet apps show Persian text). Amounts in toman. */
object CsvExport {
    fun transactions(rows: List<TransactionEntity>): String {
        val sb = StringBuilder("\uFEFF")
        sb.append("date,type,category,amount_toman,note\n")
        rows.sortedBy { it.epochDay }.forEach { t ->
            sb.append(LocalDate.ofEpochDay(t.epochDay)).append(',')
                .append(if (t.isIncome) "income" else "expense").append(',')
                .append(escape(t.category)).append(',')
                .append(t.amountRial / 10).append(',')
                .append(escape(t.note)).append('\n')
        }
        return sb.toString()
    }

    fun escape(s: String): String =
        if (s.any { it == ',' || it == '"' || it == '\n' }) "\"" + s.replace("\"", "\"\"") + "\"" else s
}
