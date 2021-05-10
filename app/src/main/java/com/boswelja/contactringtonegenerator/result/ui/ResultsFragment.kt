package com.boswelja.contactringtonegenerator.result.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.ExtendedFloatingActionButton
import androidx.compose.material.FabPosition
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.boswelja.contactringtonegenerator.R
import com.boswelja.contactringtonegenerator.common.ui.AppTheme
import com.boswelja.contactringtonegenerator.ringtonegen.Result

class ResultsFragment : Fragment() {

    private val args: ResultsFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val result = args.result
                AppTheme {
                    ResultScreen(result) { activity?.finish() }
                }
            }
        }
    }
}

@Composable
fun ResultScreen(
    result: Result?,
    onFinishClick: () -> Unit
) {
    Scaffold(
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text(stringResource(R.string.finish)) },
                icon = { Icon(Icons.Outlined.Check, null) },
                onClick = onFinishClick
            )
        }
    ) {
        Result(
            result = result,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun Result(
    result: Result?,
    modifier: Modifier = Modifier
) {
    // Get result display info
    val (icon, resultTitle, resultText) = when (result) {
        Result.FAILED ->
            Triple(
                Icons.Outlined.ErrorOutline,
                R.string.result_failed_title,
                R.string.result_failed_status
            )
        Result.MIXED ->
            Triple(
                Icons.Outlined.Warning,
                R.string.result_mixed_title,
                R.string.result_mixed_status
            )
        Result.SUCCESSFUL ->
            Triple(
                Icons.Outlined.CheckCircleOutline,
                R.string.result_success_title,
                R.string.result_success_status
            )
        else ->
            Triple(
                Icons.Outlined.HelpOutline,
                R.string.result_unknown_title,
                R.string.result_unknown_status
            )
    }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(180.dp)
        )
        Text(
            text = stringResource(resultTitle),
            style = MaterialTheme.typography.h4,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(resultText),
            style = MaterialTheme.typography.h5,
            textAlign = TextAlign.Center
        )
    }
}
