package com.boswelja.ephemeris.compose

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import dev.chrisbanes.snapper.ExperimentalSnapperApi
import dev.chrisbanes.snapper.rememberLazyListSnapperLayoutInfo
import dev.chrisbanes.snapper.rememberSnapperFlingBehavior
import kotlinx.coroutines.flow.filterNot

@OptIn(ExperimentalSnapperApi::class)
@Composable
internal fun InfiniteHorizontalPager(
    state: InfinitePagerState,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    maxItemFling: Int = 1,
    content: @Composable (page: Int) -> Unit
) {
    val layoutInfo = rememberLazyListSnapperLayoutInfo(lazyListState = state.lazyListState)

    // Update the pager state page
    LaunchedEffect(state.lazyListState) {
        snapshotFlow { state.lazyListState.isScrollInProgress }
            .filterNot { it }
            .collect {
                state.page = layoutInfo.currentItem?.index?.let { position ->
                    state.calculatePageFromPosition(position)
                } ?: 0
            }
    }

    var pagerHeight by remember { mutableStateOf(0) }

    // Switch the Modifier used to animate height once pagerHeight has had it's initial update
    val layoutModifier = if (pagerHeight > 0 && state.animateHeight) {
        Modifier
            .animateContentSize()
            .then(modifier)
    } else modifier

    LazyRow(
        modifier = layoutModifier,
        state = state.lazyListState,
        flingBehavior = rememberSnapperFlingBehavior(
            layoutInfo = layoutInfo,
            snapIndex = { _, startIndex, targetIndex ->
                targetIndex.coerceIn(startIndex - maxItemFling, startIndex + maxItemFling)
            }
        )
    ) {
        items(
            count = state.pageCount,
            key = state.calculatePageFromPosition::invoke
        ) { index ->
            var pageHeight by remember { mutableStateOf(0) }
            Box(
                Modifier
                    .fillParentMaxWidth()
                    .wrapContentHeight()
                    .layout { measurable, constraints ->
                        // We do our own measurement here so we can change the pager height according
                        // to the current page
                        val placeable = measurable.measure(constraints)
                        pageHeight = placeable.height
                        // Lay out the page, but restrict the height to the pager height
                        layout(constraints.maxWidth, pagerHeight) {
                            placeable.placeRelative(0, 0)
                        }
                    }
                    .onGloballyPositioned {
                        // Make sure to update the pager height when a new page is snapped
                        if (it.positionInParent().x == 0f) {
                            pagerHeight = pageHeight
                        }
                    }
                    .padding(contentPadding)
            ) {
                content(state.calculatePageFromPosition(index))
            }
        }
    }
}

internal class InfinitePagerState internal constructor(
    startPage: Int,
    val pageCount: Int,
    internal val lazyListState: LazyListState,
    internal val calculatePageFromPosition: (position: Int) -> Int = { it - (pageCount / 2) },
    internal val calculatePositionFromPage: (page: Int) -> Int = { it + (pageCount / 2) }
) {
    var page: Int by mutableStateOf(startPage)

    var animateHeight: Boolean = true

    suspend fun scrollToPage(page: Int) {
        lazyListState.scrollToItem(calculatePositionFromPage(page))
    }

    suspend fun animateScrollToPage(page: Int) {
        lazyListState.animateScrollToItem(calculatePositionFromPage(page))
    }
}

@Composable
internal fun rememberInfinitePagerState(
    pageCount: Int,
    startPage: Int = 0,
    calculatePageFromPosition: (position: Int) -> Int = { it - (pageCount / 2) },
    calculatePositionFromPage: (page: Int) -> Int = { it + (pageCount / 2) }
): InfinitePagerState {
    val lazyListState: LazyListState = rememberLazyListState(
        initialFirstVisibleItemIndex = calculatePositionFromPage(startPage)
    )
    return remember(startPage) {
        InfinitePagerState(startPage, pageCount, lazyListState, calculatePageFromPosition, calculatePositionFromPage)
    }
}
