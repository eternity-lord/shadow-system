package com.shadowsystem.data.backup

import com.shadowsystem.data.db.TransactionEntity
import com.shadowsystem.data.db.WalletEntity
import java.time.LocalDate

/** Transactions as CSV (UTF-8 with BOM so spreadsheet apps show Persian text). Amounts in toman. */
object CsvExport {
    fun transactions(rows: List<TransactionEntity>, wallets: List<WalletEntity> = emptyList()): String {
        val sb = StringBuilder("\uFEFF")
        val names = wallets.associate { it.id to it.name }
        val firstName = wallets.minByOrNull { it.id }?.name ?: ""
        sb.append("date,type,category,amount_toman,note,wallet\n")
        rows.sortedBy { it.epochDay }.forEach { t ->
            sb.append(LocalDate.ofEpochDay(t.epochDay)).append(',')
                .append(if (t.isIncome) "income" else "expense").append(',')
                .append(escape(t.category)).append(',')
                .append(t.amountRial / 10).append(',')
                .append(escape(t.note)).append(',').append(escape(t.walletId?.let { names[it] } ?: firstName)).append('\n')
        }
        return sb.toString()
    }

    fun escape(s: String): String =
        if (s.any { it == ',' || it == '"' || it == '\n' }) "\"" + s.replace("\"", "\"\"") + "\"" else s
}
