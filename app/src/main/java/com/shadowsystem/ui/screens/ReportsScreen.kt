package com.shadowsystem.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shadowsystem.R
import com.shadowsystem.core.PersianText
import com.shadowsystem.ui.components.BackHeader
import com.shadowsystem.ui.components.BarChart
import com.shadowsystem.ui.components.DonutChart
import com.shadowsystem.ui.components.LineChart
import com.shadowsystem.ui.components.SectionTitle
import com.shadowsystem.ui.components.ShadowCard
import com.shadowsystem.ui.components.chartColor
import com.shadowsystem.ui.theme.ShadowColors
import com.shadowsystem.ui.vm.ReportsViewModel

/** Expense reports: donut by category, six-month bars and a 30-day line. */
@Composable
fun ReportsScreen(vm: ReportsViewModel, onBack: () -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        BackHeader(stringResource(R.string.reports_title), onBack)
        ShadowCard {
            SectionTitle(stringResource(R.string.reports_by_category, state.monthLabel))
            if (state.categories.isEmpty()) {
                Text(stringResource(R.string.reports_empty), color = ShadowColors.TextMuted)
            } else {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DonutChart(state.categories)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
                        state.categories.forEachIndexed { i, (name, value) ->
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(Modifier.size(12.dp).background(chartColor(i)))
                                Text("$name · ${PersianText.formatMoney(value)}", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }
        ShadowCard {
            SectionTitle(stringResource(R.string.reports_compare_title))
            val pct = state.changePercent
            Text(
                if (pct == null) stringResource(R.string.reports_compare_none)
                else stringResource(R.string.reports_compare, (if (pct > 0) "+" else "") + PersianText.percent(pct.toFloat())),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(stringResource(R.string.reports_daily_avg, PersianText.formatMoney(state.dailyAverage)), style = MaterialTheme.typography.bodyMedium)
            Text(stringResource(R.string.reports_year_total, PersianText.formatMoney(state.yearTotal)), style = MaterialTheme.typography.bodyMedium)
            if (state.topExpenses.isNotEmpty()) {
                Text(stringResource(R.string.reports_top), style = MaterialTheme.typography.titleSmall)
                state.topExpenses.forEach { (label, value) ->
                    Text("$label · ${PersianText.formatMoney(value)}", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        ShadowCard {
            SectionTitle(stringResource(R.string.reports_six_months))
            BarChart(state.monthTotals.map { it.first }, state.monthTotals.map { it.second })
        }
        ShadowCard {
            SectionTitle(stringResource(R.string.reports_thirty_days))
            LineChart(state.daily)
        }
    }
}
