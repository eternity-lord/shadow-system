package com.shadowsystem.core

import com.shadowsystem.core.GameMath.Rank
import com.shadowsystem.core.GameMath.Stat

/** A built-in quest template (Persian). First step is always <= 2 minutes. */
data class QuestTemplate(
    val title: String,
    val description: String,
    val stat: Stat,
    val rank: Rank,
    val minutes: Int,
    val steps: List<String>,
    val lowEnergy: Boolean = false,
    val soldierKey: String? = null
)

/** 48 built-in Persian templates grouped by stat (spec section 4). */
object Templates {

    val ALL: List<QuestTemplate> = listOf(
        // STR - exercise and movement
        QuestTemplate("پیاده‌روی ۱۰ دقیقه‌ای", "دور خانه یا در خیابان قدم بزن.", Stat.STR, Rank.E, 10,
            listOf("کفش بپوش", "از در خارج شو", "۱۰ دقیقه قدم بزن", "برگرد و آب بنوش"), soldierKey = "igris"),
        QuestTemplate("کشش صبحگاهی", "بدنت را بیدار کن.", Stat.STR, Rank.E, 5,
            listOf("بایست و دست‌ها را بالا ببر", "گردن و شانه‌ها را بچرخان", "چند نفس عمیق بکش"), soldierKey = "igris"),
        QuestTemplate("۲۰ اسکوات", "در خانه، بدون وسیله.", Stat.STR, Rank.D, 5,
            listOf("پا‌ها را به عرض شانه باز کن", "۱۰ اسکوات بزن", "۱۰ اسکوات دیگر"), soldierKey = "igris"),
        QuestTemplate("پلکان به جای آسانسور", "امروز یک بار از پله‌ها استفاده کن.", Stat.STR, Rank.E, 3,
            listOf("به پله‌ها نزدیک شو", "از پله‌ها بالا برو"), soldierKey = "igris"),
        QuestTemplate("تمرین در خانه ۱۵ دقیقه", "یک ویدیوی تمرینی کوتاه دنبال کن.", Stat.STR, Rank.C, 15,
            listOf("یک ویدیوی کوتاه انتخاب کن", "شروع کن", "تا پایان همراهی کن"), soldierKey = "igris"),
        QuestTemplate("دوچرخه‌سواری کوتاه", "یک دور کوتاه با دوچرخه.", Stat.STR, Rank.C, 20,
            listOf("دوچرخه را آماده کن", "شروع کن", "برگرد"), soldierKey = "igris"),
        QuestTemplate("پیاده‌روی طولانی", "۳۰ دقیقه پیاده‌روی.", Stat.STR, Rank.B, 30,
            listOf("مسیر را انتخاب کن", "شروع کن", "در میانه آب بنوش", "برگرد"), soldierKey = "igris"),
        QuestTemplate("تمرین قدرتی", "تمرین با وزن بدن ۲۰ دقیقه.", Stat.STR, Rank.B, 20,
            listOf("گرم کن", "تمرین را شروع کن", "سرد کن و آب بنوش"), soldierKey = "igris"),
        // INT - learning, work, money
        QuestTemplate("خواندن ۵ صفحه", "هر کتابی که دوست داری.", Stat.INT, Rank.E, 10,
            listOf("کتاب را بردار", "۵ صفحه بخوان"), soldierKey = "tusk"),
        QuestTemplate("ثبت هزینه‌های امروز", "همه خرج‌های امروز را ثبت کن.", Stat.INT, Rank.E, 3,
            listOf("تب پول را باز کن", "هزینه‌های امروز را ثبت کن"), soldierKey = "iron"),
        QuestTemplate("مرور کارهای فردا", "سه کار مهم فردا را بنویس.", Stat.INT, Rank.D, 5,
            listOf("دفتر را بردار", "سه کار فردا را بنویس"), soldierKey = "tusk"),
        QuestTemplate("۲۵ دقیقه کار عمیق", "بدون گوشی، فقط یک کار.", Stat.INT, Rank.C, 25,
            listOf("گوشی را دور بگذار", "کار را شروع کن", "۲۵ دقیقه ادامه بده"), soldierKey = "beru"),
        QuestTemplate("یادگیری یک نکته جدید", "یک ویدیوی آموزشی کوتاه ببین.", Stat.INT, Rank.D, 15,
            listOf("موضوع را انتخاب کن", "ویدیو را پخش کن", "یک نکته را یادداشت کن"), soldierKey = "tusk"),
        QuestTemplate("بررسی بودجه ماه", "بودجه این ماه را مرور کن.", Stat.INT, Rank.C, 10,
            listOf("گزارش ماه را باز کن", "دسته‌های پرهزینه را ببین", "یک تصمیم کوچک بگیر"), soldierKey = "iron"),
        QuestTemplate("نوشتن برنامه هفته", "برنامه این هفته را بنویس.", Stat.INT, Rank.B, 15,
            listOf("تقویم را باز کن", "کارهای مهم هفته را بنویس"), soldierKey = "beru"),
        QuestTemplate("مطالعه عمیق ۴۵ دقیقه", "یک موضوع را عمیق بخوان.", Stat.INT, Rank.A, 45,
            listOf("منبع را آماده کن", "گوشی را دور بگذار", "۴۵ دقیقه مطالعه کن", "نکته‌ها را بنویس"), soldierKey = "tusk"),
        // AGI - on-time, low procrastination
        QuestTemplate("مرتب کردن یک میز", "فقط یک میز را مرتب کن.", Stat.AGI, Rank.E, 10,
            listOf("میز را خالی کن", "وسایل را سر جایشان بگذار", "میز را تمیز کن"), soldierKey = "tank"),
        QuestTemplate("شروع کار سخت در ۲ دقیقه", "فقط ۲ دقیقه شروع کن.", Stat.AGI, Rank.D, 2,
            listOf("کار را باز کن", "فقط ۲ دقیقه شروع کن"), soldierKey = "tank"),
        QuestTemplate("پاسخ به یک پیام معلق", "یک پیام عقب‌افتاده را جواب بده.", Stat.AGI, Rank.E, 5,
            listOf("پیام را باز کن", "جواب کوتاه بنویس و بفرست"), soldierKey = "jima"),
        QuestTemplate("مرتب کردن تختخواب", "تخت را مرتب کن.", Stat.AGI, Rank.E, 3,
            listOf("ملحفه را صاف کن", "بالش‌ها را مرتب کن"), soldierKey = "tank"),
        QuestTemplate("شستن ظرف‌ها", "ظرف‌های سینک را بشور.", Stat.AGI, Rank.D, 10,
            listOf("سینک را آماده کن", "ظرف‌ها را بشور", "خشک کن و سر جایشان بگذار"), soldierKey = "tank"),
        QuestTemplate("تمیز کردن اتاق ۱۵ دقیقه", "فقط ۱۵ دقیقه.", Stat.AGI, Rank.C, 15,
            listOf("تایمر ۱۵ دقیقه بگذار", "از یک گوشه شروع کن", "تا پایان ادامه بده"), soldierKey = "tank"),
        QuestTemplate("انجام کار عقب‌افتاده", "یک کار قدیمی را تمام کن.", Stat.AGI, Rank.B, 20,
            listOf("کار را انتخاب کن", "شروع کن", "تمام کن"), soldierKey = "tank"),
        QuestTemplate("برنامه‌ریزی زمانی", "برای امروز زمان‌بندی کن.", Stat.AGI, Rank.C, 10,
            listOf("کارهای امروز را بنویس", "برای هرکدام زمان بگذار"), soldierKey = "tank"),
        // VIT - sleep, water, food, rest
        QuestTemplate("یک لیوان آب", "همین حالا یک لیوان آب بنوش.", Stat.VIT, Rank.E, 2,
            listOf("لیوان را بردار", "آب بنوش"), lowEnergy = true, soldierKey = "fang"),
        QuestTemplate("خوابیدن سر وقت", "امشب سر وقت بخواب.", Stat.VIT, Rank.D, 5,
            listOf("گوشی را کنار بگذار", "چراغ را کم کن", "بخواب"), soldierKey = "kaisel"),
        QuestTemplate("غذای سالم", "یک وعده غذای سالم بخور.", Stat.VIT, Rank.D, 15,
            listOf("غذا را آماده کن", "آرام غذا بخور"), soldierKey = "fang"),
        QuestTemplate("تنفس عمیق ۵ دقیقه", "چند نفس عمیق و آرام.", Stat.VIT, Rank.E, 5,
            listOf("بنشین و چشم‌ها را ببند", "۵ نفس عمیق بکش"), lowEnergy = true, soldierKey = "blue"),
        QuestTemplate("استراحت بدون گوشی", "۱۵ دقیقه بدون گوشی استراحت کن.", Stat.VIT, Rank.C, 15,
            listOf("گوشی را دور بگذار", "استراحت کن"), lowEnergy = true, soldierKey = "blue"),
        QuestTemplate("پیاده‌روی بعد از غذا", "بعد از غذا ۱۰ دقیقه قدم بزن.", Stat.VIT, Rank.D, 10,
            listOf("غذا را تمام کن", "قدم بزن"), soldierKey = "fang"),
        QuestTemplate("روتین صبحگاهی", "صبح را با روتین خودت شروع کن.", Stat.VIT, Rank.C, 10,
            listOf("از تخت بلند شو", "صورت را بشور", "روتین صبح را انجام بده"), soldierKey = "kaisel"),
        QuestTemplate("خواب کامل ۷ ساعته", "امشب ۷ ساعت بخواب.", Stat.VIT, Rank.B, 5,
            listOf("زمان خواب را تنظیم کن", "گوشی را دور بگذار", "بخواب"), soldierKey = "kaisel"),
        // SENSE - focus, social, journaling
        QuestTemplate("تماس با یک دوست", "به یک دوست پیام بده یا زنگ بزن.", Stat.SENSE, Rank.E, 5,
            listOf("یک نفر را انتخاب کن", "پیام بده"), soldierKey = "jima"),
        QuestTemplate("۱۰ دقیقه بدون گوشی", "گوشی را کنار بگذار.", Stat.SENSE, Rank.D, 10,
            listOf("گوشی را در اتاق دیگر بگذار", "۱۰ دقیقه صبر کن"), soldierKey = "nova"),
        QuestTemplate("نوشتن در دفترچه", "سه خط درباره امروز بنویس.", Stat.SENSE, Rank.E, 5,
            listOf("دفتر را بردار", "سه خط بنویس"), soldierKey = "scribe"),
        QuestTemplate("مرور شبانه", "امروز را در چند خط مرور کن.", Stat.SENSE, Rank.D, 10,
            listOf("دفتر را باز کن", "خوبی‌های امروز را بنویس", "یک نکته برای فردا بنویس"), soldierKey = "scribe"),
        QuestTemplate("گوش دادن فعال", "با یک نفر بدون حواس‌پرتی حرف بزن.", Stat.SENSE, Rank.C, 10,
            listOf("گوشی را کنار بگذار", "گوش بده و سؤال بپرس"), soldierKey = "jima"),
        QuestTemplate("تنظیم اعلان‌ها", "اعلان‌های غیرضروری را خاموش کن.", Stat.SENSE, Rank.D, 10,
            listOf("تنظیمات اعلان را باز کن", "اعلان‌های غیرضروری را خاموش کن"), soldierKey = "nova"),
        QuestTemplate("مدیتیشن ۱۰ دقیقه‌ای", "آرام بنشین و ذهنت را خالی کن.", Stat.SENSE, Rank.C, 10,
            listOf("جای آرام بنشین", "تنفس کن و ذهنت را آزاد کن"), lowEnergy = true, soldierKey = "blue"),
        QuestTemplate("قدردانی", "از سه نفر قدردانی کن.", Stat.SENSE, Rank.D, 5,
            listOf("سه نفر را در ذهن بیاور", "به یکی پیام قدردانی بده"), soldierKey = "jima"),
        // Mixed / general
        QuestTemplate("مرتب کردن کیف", "کیف را خالی و مرتب کن.", Stat.AGI, Rank.E, 10,
            listOf("کیف را خالی کن", "وسایل اضافی را بردار", "کیف را مرتب کن"), soldierKey = "tank"),
        QuestTemplate("آبیاری گیاه", "به گیاه‌هایت آب بده.", Stat.VIT, Rank.E, 3,
            listOf("آب بردار", "به گیاه‌ها آب بده"), lowEnergy = true, soldierKey = "fang"),
        QuestTemplate("بررسی ایمیل‌ها", "اینباکس را بررسی و مرتب کن.", Stat.INT, Rank.D, 10,
            listOf("ایمیل را باز کن", "مهم‌ها را جواب بده", "بقیه را آرشیو کن"), soldierKey = "beru"),
        QuestTemplate("خرید هفتگی", "لیست خرید هفته را بنویس و بخر.", Stat.INT, Rank.C, 30,
            listOf("لیست را بنویس", "خرید کن"), soldierKey = "iron"),
        QuestTemplate("تمیز کردن گوشی", "گوشی را تمیز و مرتب کن.", Stat.SENSE, Rank.E, 5,
            listOf("عکس‌های اضافی را پاک کن", "برنامه‌های اضافی را حذف کن"), soldierKey = "nova"),
        QuestTemplate("خواندن اخبار ۱۰ دقیقه", "اخبار مهم روز را بخوان.", Stat.INT, Rank.D, 10,
            listOf("منبع خبری را باز کن", "۱۰ دقیقه بخوان"), soldierKey = "tusk"),
        QuestTemplate("کمک به یک نفر", "امروز به یک نفر کمک کن.", Stat.SENSE, Rank.C, 10,
            listOf("یک نفر را انتخاب کن", "کمک کن"), soldierKey = "jima"),
        QuestTemplate("هدف‌گذاری ماهانه", "هدف این ماه را بنویس.", Stat.INT, Rank.B, 15,
            listOf("دفتر را بردار", "هدف ماه را بنویس", "قدم اول را مشخص کن"), soldierKey = "beru"),
        QuestTemplate("ورزش سبک عصرگاهی", "عصر ۱۵ دقیقه حرکت کن.", Stat.STR, Rank.D, 15,
            listOf("شروع کن", "۱۵ دقیقه حرکت کن"), soldierKey = "igris"),
        QuestTemplate("خواب زودهنگام", "امشب یک ساعت زودتر بخواب.", Stat.VIT, Rank.C, 5,
            listOf("زمان خواب را جلو ببر", "بخواب"), soldierKey = "kaisel")
    )

    fun byStat(stat: Stat): List<QuestTemplate> = ALL.filter { it.stat == stat }

    fun lowEnergy(): List<QuestTemplate> = ALL.filter { it.lowEnergy }
}
