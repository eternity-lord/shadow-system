package com.shadowsystem.core

import com.shadowsystem.core.GameMath.Rank

/** Shop items, titles and achievements (spec sections 7 and 10). */
data class ShopItem(
    val key: String,
    val name: String,
    val category: String, // outfit | weapon | aura | frame | hair | headgear | background
    val priceGold: Int,
    val heavy: Boolean = false, // unlocked via boss/trial, not gold
    val description: String
)

data class TitleDef(val key: String, val name: String, val hint: String)
data class AchievementDef(val key: String, val name: String, val hint: String)

object AvatarData {

    val SHOP_ITEMS: List<ShopItem> = listOf(
        ShopItem("outfit_street", "لباس خیابانی", "outfit", 30, description = "لباس ساده و راحت"),
        ShopItem("outfit_sport", "لباس ورزشی", "outfit", 50, description = "لباس ورزشی سبک"),
        ShopItem("outfit_leather", "زره چرمی سبک", "outfit", 90, description = "زره چرمی شکارچی"),
        ShopItem("outfit_armor", "زره کامل", "outfit", 160, description = "زره کامل جنگجو"),
        ShopItem("outfit_shadow", "زره سایه پادشاه", "outfit", 0, heavy = true, description = "فقط از پاداش باس یا محاکمه رتبه"),
        ShopItem("weapon_dagger", "خنجر سایه", "weapon", 40, description = "خنجر سبک و سریع"),
        ShopItem("weapon_sword", "شمشیر شکارچی", "weapon", 80, description = "شمشیر قابل اعتماد"),
        ShopItem("weapon_blade", "تیغ تاریک", "weapon", 0, heavy = true, description = "فقط از پاداش باس"),
        ShopItem("aura_blue", "هاله آبی", "aura", 60, description = "هاله آبی ملایم"),
        ShopItem("aura_purple", "هاله بنفش", "aura", 110, description = "هاله بنفش سایه"),
        ShopItem("aura_gold", "هاله طلایی", "aura", 180, description = "هاله طلایی درخشان"),
        ShopItem("aura_dark", "هاله تاریک زنده", "aura", 0, heavy = true, description = "فقط از محاکمه رتبه S"),
        ShopItem("frame_basic", "قاب ساده", "frame", 20, description = "قاب ساده پروفایل"),
        ShopItem("frame_neon", "قاب نئونی", "frame", 70, description = "قاب نئونی بنفش"),
        ShopItem("frame_sovereign", "قاب فرمانروا", "frame", 0, heavy = true, description = "فقط از تکمیل هدف پس‌انداز"),
        ShopItem("hair_short", "مو کوتاه مرتب", "hair", 20, description = "مو کوتاه و مرتب"),
        ShopItem("hair_spiky", "مو سیخی سایه", "hair", 55, description = "موی سیخی و وحشی"),
        ShopItem("headgear_hood", "کلاه کاپشن سایه", "headgear", 70, description = "کلاه با لبه طلایی"),
        ShopItem("headgear_crown", "تاج سایه", "headgear", 150, description = "تاج تیز سایه"),
        ShopItem("bg_gate", "پس‌زمینه دروازه", "background", 50, description = "پس‌زمینه دروازه تاریک"),
        ShopItem("bg_throne", "پس‌زمینه تخت سایه", "background", 0, heavy = true, description = "فقط از باس هفتگی")
    )

    val TITLES: List<TitleDef> = listOf(
        TitleDef("awakened", "بیدارشده", "به سطح ۵ برس"),
        TitleDef("unbroken", "نشکسته", "۱۰ روز فعال شو"),
        TitleDef("shadow_accountant", "حسابدار سایه", "۳۰ تراکنش ثبت کن"),
        TitleDef("dungeon_runner", "دوندگان سیاه‌چال", "۱۰ جلسه سیاه‌چال کامل کن"),
        TitleDef("early_bird", "سحرخیز", "۵ بار قبل از ساعت ۷ صبح فعال شو"),
        TitleDef("monarch", "پادشاه سایه", "به رتبه S برس")
    )

    val ACHIEVEMENTS: List<AchievementDef> = listOf(
        AchievementDef("first_quest", "اولین مأموریت", "اولین مأموریت را کامل کن"),
        AchievementDef("first_dungeon", "اولین سیاه‌چال", "اولین جلسه سیاه‌چال را کامل کن"),
        AchievementDef("first_soldier", "اولین سرباز", "اولین سرباز سایه را احضار کن"),
        AchievementDef("score_80", "روز عالی", "امتیاز روزانه ۸۰ یا بیشتر بگیر"),
        AchievementDef("beat_yesterday", "سایه دیروز", "امتیاز دیروز را شکست بده"),
        AchievementDef("goal_done", "هدف کامل", "یک هدف پس‌انداز را کامل کن"),
        AchievementDef("boss_done", "غول شکست‌خورده", "باس هفتگی را شکست بده"),
        AchievementDef("rank_c", "رتبه C", "به رتبه C برس"),
        AchievementDef("rank_s", "رتبه S", "به رتبه S برس"),
        AchievementDef("hundred_days", "صد روز", "۱۰۰ روز فعال شو")
    )

    /** Base avatar look description per rank (spec section 7). */
    fun rankLook(rank: Rank): String = when (rank) {
        Rank.E -> "لباس معمولی و چهره خسته، بدون هاله"
        Rank.D -> "لباس ورزشی ساده و خط سفید روی شانه"
        Rank.C -> "زره چرمی سبک، دستکش و هاله آبی کم‌رنگ"
        Rank.B -> "زره کامل، شنل کوتاه، سلاح اصلی و هاله بنفش"
        Rank.A -> "زره سیاه با نقش‌های درخشان، شنل بلند، چشم‌های روشن و هاله طلایی"
        Rank.S -> "سبک پادشاه سایه: سیاه و بنفش، شنل پرانرژی، هاله تاریک زنده و سربازان پشت سر"
    }

    fun rankAssetName(rank: Rank): String = "avatar_rank_${rank.key.lowercase()}"
}
