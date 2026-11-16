package com.shadowsystem.core

/** Persian digit conversion and number formatting for display. */
object PersianText {

    private const val FA_DIGITS = "۰۱۲۳۴۵۶۷۸۹"

    fun toFa(value: Any?): String {
        if (value == null) return ""
        return value.toString().map { c ->
            if (c in '0'..'9') FA_DIGITS[c - '0'] else c
        }.joinToString("")
    }

    /** Parse user input that may contain Persian or English digits. */
    fun parseDigits(input: String): String =
        input.map { c ->
            when {
                c in '۰'..'۹' -> ('0'.code + (c.code - '۰'.code)).toChar()
                c in '٠'..'٩' -> ('0'.code + (c.code - '٠'.code)).toChar()
                else -> c
            }
        }.joinToString("")

    fun parseLong(input: String): Long? {
        val cleaned = parseDigits(input).replace(",", "").replace("٬", "").trim()
        return cleaned.toLongOrNull()
    }

    /** Thousands-separated number with Persian digits: ۱۲٬۳۴۵ */
    fun grouped(value: Long): String {
        val negative = value < 0
        val digits = kotlin.math.abs(value).toString()
        val sb = StringBuilder()
        for ((i, c) in digits.withIndex()) {
            if (i > 0 && (digits.length - i) % 3 == 0) sb.append('٬')
            sb.append(c)
        }
        return toFa((if (negative) "-" else "") + sb.toString())
    }

    /** Rials (stored) -> Toman display (default). */
    fun rialToToman(rials: Long): Long = rials / 10

    fun formatMoney(rials: Long, showToman: Boolean = true): String {
        val amount = if (showToman) rialToToman(rials) else rials
        val unit = if (showToman) "تومان" else "ریال"
        return "${grouped(amount)} $unit"
    }

    fun percent(value: Float): String = "${toFa(kotlin.math.round(value).toInt())}٪"

    fun durationMinutes(totalMinutes: Long): String {
        val h = totalMinutes / 60
        val m = totalMinutes % 60
        return when {
            h > 0 && m > 0 -> "${toFa(h)} ساعت و ${toFa(m)} دقیقه"
            h > 0 -> "${toFa(h)} ساعت"
            else -> "${toFa(m)} دقیقه"
        }
    }
}
