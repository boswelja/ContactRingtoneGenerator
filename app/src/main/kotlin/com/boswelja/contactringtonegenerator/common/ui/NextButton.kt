package com.boswelja.contactringtonegenerator.common.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExtendedFloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.boswelja.contactringtonegenerator.R

@ExperimentalAnimationApi
@Composable
fun BoxScope.NextButton(
    modifier: Modifier = Modifier,
    enabled: Boolean,
    onClick: () -> Unit
) {
    AnimatedVisibility(
        modifier = modifier
            .padding(16.dp)
            .align(Alignment.BottomEnd),
        visible = enabled
    ) {
        ExtendedFloatingActionButton(
            text = {
                Text(stringResource(R.string.next))
            },
            icon = {
                Icon(Icons.Default.NavigateNext, null)
            },
            shape = MaterialTheme.shapes.small,
            onClick = onClick
        )
    }
}
