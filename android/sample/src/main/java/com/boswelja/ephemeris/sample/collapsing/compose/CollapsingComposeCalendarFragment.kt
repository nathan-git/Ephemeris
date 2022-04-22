package com.boswelja.ephemeris.sample.collapsing.compose

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewmodel.compose.viewModel
import com.boswelja.ephemeris.compose.EphemerisCalendar
import com.boswelja.ephemeris.compose.rememberCalendarState
import com.boswelja.ephemeris.core.data.CalendarMonthPageSource
import com.boswelja.ephemeris.core.model.CalendarDay
import com.boswelja.ephemeris.sample.R
import com.boswelja.ephemeris.sample.collapsing.CollapsingCalendarViewModel
import com.boswelja.ephemeris.sample.ui.theme.EphemerisTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.datetime.DayOfWeek

class ComposeCalendarFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                EphemerisTheme {
                    CalendarScreen()
                }
            }
        }
    }
}

@Composable
fun CalendarScreen(
    modifier: Modifier = Modifier,
    viewModel: CollapsingCalendarViewModel = viewModel()
) {
    val calendarState = rememberCalendarState {
        CalendarMonthPageSource(
            DayOfWeek.SUNDAY
        )
    }

    val headerText by viewModel.headerText.collectAsState()

    LaunchedEffect(calendarState) {
        calendarState.displayedDateRange.collectLatest {
            viewModel.handlePageChanged(it)
        }
    }
    LaunchedEffect(viewModel) {
        viewModel.calendarPageSource.collectLatest {
            calendarState.pageSource = it
        }
    }

    Column(modifier) {
        Text(
            text = headerText,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(top = 24.dp)
        )
        EphemerisCalendar(
            contentPadding = PaddingValues(horizontal = 16.dp),
            calendarState = calendarState
        ) { calendarDay ->
            CollapsingCalendarDate(
                calendarDay = calendarDay,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.Center)
            )
        }
        Button(
            onClick = viewModel::toggleCalendarExpanded
        ) {
            Text(stringResource(R.string.toggle_calendar_view))
        }
    }
}

@Composable
internal fun CollapsingCalendarDate(
    calendarDay: CalendarDay,
    modifier: Modifier = Modifier
) {
    Text(
        modifier = modifier,
        text = calendarDay.date.dayOfMonth.toString(),
        color = if (calendarDay.isFocusedDate)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.onSurface,
        fontWeight = if (calendarDay.isFocusedDate) FontWeight.Bold else FontWeight.Normal
    )
}
