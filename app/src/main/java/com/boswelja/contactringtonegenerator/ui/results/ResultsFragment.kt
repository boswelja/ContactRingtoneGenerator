package com.boswelja.contactringtonegenerator.ui.results

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.boswelja.contactringtonegenerator.databinding.FragmentResultsBinding

class ResultsFragment : Fragment() {

    private val args: ResultsFragmentArgs by navArgs()
    private val viewModel: ResultsViewModel by viewModels { ResultViewModelFactory(args.successCount, args.failureCount) }

    private lateinit var binding: FragmentResultsBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentResultsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        binding.finishButton.setOnClickListener {
            activity?.finish()
        }
    }
}
