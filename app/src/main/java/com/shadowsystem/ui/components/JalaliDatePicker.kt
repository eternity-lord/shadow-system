package com.shadowsystem.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shadowsystem.core.Jalali
import com.shadowsystem.core.PersianText
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.heightIn

/** Persian (Jalali) date picker with +/- steppers for day, month and year. Always RTL-safe. */
@Composable
fun JalaliDatePicker(date: Jalali.Date, onChange: (Jalali.Date) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        Text(Jalali.format(date), style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Stepper("روز", PersianText.toFa(date.day), { onChange(step(date, day = -1)) }, { onChange(step(date, day = 1)) })
            Stepper("ماه", Jalali.monthName(date.month), { onChange(step(date, month = -1)) }, { onChange(step(date, month = 1)) })
            Stepper("سال", PersianText.toFa(date.year), { onChange(step(date, year = -1)) }, { onChange(step(date, year = 1)) })
        }
    }
}

private fun step(date: Jalali.Date, day: Int = 0, month: Int = 0, year: Int = 0): Jalali.Date {
    var d = date
    if (year != 0) d = d.copy(year = d.year + year)
    if (month != 0) d = Jalali.addMonths(d, month)
    val len = Jalali.monthLength(d.year, d.month)
    var nd = d.day + day
    if (nd < 1) nd = len
    if (nd > len) nd = 1
    return d.copy(day = nd.coerceAtMost(len))
}

@Composable
private fun Stepper(label: String, value: String, onMinus: () -> Unit, onPlus: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.widthIn(min = 80.dp)) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(onClick = onPlus, modifier = Modifier.heightIn(min = 48.dp)) { Text("+") }
            Text(value, modifier = Modifier.padding(horizontal = 6.dp))
            OutlinedButton(onClick = onMinus, modifier = Modifier.heightIn(min = 48.dp)) { Text("−") }
        }
    }
}
