package com.boswelja.contactringtonegenerator.common.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.ThresholdConfig
import androidx.compose.material.contentColorFor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@ExperimentalAnimationApi
@ExperimentalMaterialApi
@Composable
fun <T> LazyItemScope.SwipeDismissItem(
    modifier: Modifier = Modifier,
    item: T,
    icon: ImageVector = Icons.Default.Delete,
    backgroundColor: Color = MaterialTheme.colors.background,
    directions: Set<DismissDirection> =
        setOf(DismissDirection.StartToEnd, DismissDirection.EndToStart),
    dismissThresholds: (DismissDirection) -> ThresholdConfig = { FractionalThreshold(0.5f) },
    onStateChanged: ((isDismissing: Boolean) -> Unit)? = null,
    onItemDismissed: (T) -> Unit,
    content: @Composable (LazyItemScope.(T) -> Unit)
) {
    val dismissState = rememberDismissState {
        if (it != DismissValue.Default) {
            onItemDismissed(item)
        }
        true
    }

    SwipeToDismiss(
        modifier = modifier,
        state = dismissState,
        directions = directions,
        dismissThresholds = dismissThresholds,
        background = {
            val direction = dismissState.dismissDirection

            if (direction == null) {
                onStateChanged?.invoke(false)
                return@SwipeToDismiss
            }
            onStateChanged?.invoke(true)

            val iconAlignment = when (direction) {
                DismissDirection.StartToEnd -> Alignment.CenterStart
                DismissDirection.EndToStart -> Alignment.CenterEnd
            }

            Box(
                Modifier
                    .fillMaxSize()
                    .background(backgroundColor)
                    .padding(horizontal = 32.dp),
                contentAlignment = iconAlignment
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    tint = MaterialTheme.colors.contentColorFor(backgroundColor)
                )
            }
        }
    ) {
        content(item)
    }
}

/**
 * A Composable to handle animating LazyColumn/LazyRow item visibility.
 * @param modifier A [Modifier] to be applied to the [AnimatedVisibility] wrapping your content.
 * @param enterAnim The [EnterTransition] to show when animating your content in.
 * @param exitAnim The [ExitTransition] to show when animating your content out.
 * @param remove A State to control whether the item is being removed. This should default to false,
 * and be set to true to start animating your content out.
 * @param item Your item of type [T].
 * @param onItemRemoved Called when [remove] has been set to true, and your content has finished
 * animating out.
 * @param content Your Composable content.
 */
@ExperimentalAnimationApi
@Composable
fun <T> LazyItemScope.AnimatedVisibilityItem(
    modifier: Modifier = Modifier,
    enterAnim: EnterTransition = fadeIn() + expandVertically(),
    exitAnim: ExitTransition = fadeOut() + shrinkVertically(),
    remove: Boolean,
    item: T,
    onItemRemoved: (T) -> Unit,
    content: @Composable LazyItemScope.(T) -> Unit
) {
    val visible = remember {
        MutableTransitionState(false)
    }

    if (remove && visible.isIdle && !visible.currentState) {
        onItemRemoved(item)
    }

    AnimatedVisibility(
        modifier = modifier,
        visibleState = visible,
        enter = enterAnim,
        exit = exitAnim
    ) {
        content(item)
    }

    LaunchedEffect(key1 = remove) {
        visible.targetState = !remove
    }
}
