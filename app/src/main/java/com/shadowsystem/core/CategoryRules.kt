package com.shadowsystem.core

/** Money categories (spec section 8): the default set, and checks for adding or renaming one. */
object CategoryRules {
    const val MAX_NAME_LENGTH = 20

    data class Default(val name: String, val isIncome: Boolean, val colorArgb: Long)

    val DEFAULTS: List<Default> = listOf(
        Default("خوراک", false, 0xFFE57373L),
        Default("حمل‌ونقل", false, 0xFF64B5F6L),
        Default("خانه", false, 0xFF81C784L),
        Default("قبوض", false, 0xFFFFB74DL),
        Default("سلامت", false, 0xFF4DB6ACL),
        Default("آموزش", false, 0xFF9575CDL),
        Default("تفریح", false, 0xFFF06292L),
        Default("پوشاک", false, 0xFFA1887FL),
        Default("هدیه", false, 0xFFFFD54FL),
        Default("پس‌انداز", false, 0xFF7986CBL),
        Default("سایر", false, 0xFF90A4AEL),
        Default("حقوق", true, 0xFF66BB6AL),
        Default("درآمد متفرقه", true, 0xFF26A69AL)
    )

    enum class Check { OK, EMPTY, TOO_LONG, EXISTS }

    /** Validates a name. [renamingFrom] is the current name when renaming; keeping the same name is allowed. */
    fun check(name: String, existing: Set<String>, renamingFrom: String? = null): Check {
        val clean = name.trim()
        return when {
            clean.isEmpty() -> Check.EMPTY
            clean.length > MAX_NAME_LENGTH -> Check.TOO_LONG
            clean == renamingFrom -> Check.OK
            clean in existing -> Check.EXISTS
            else -> Check.OK
        }
    }
}
