package com.example.stayfree.ui.inapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.stayfree.databinding.FragmentInAppBlockBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class InAppBlockFragment : Fragment() {

    private var _binding: FragmentInAppBlockBinding? = null
    private val binding get() = _binding!!
    private val viewModel: InAppBlockViewModel by viewModels()
    private lateinit var adapter: InAppBlockAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentInAppBlockBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.initDefaultTargets()

        adapter = InAppBlockAdapter { entity -> viewModel.toggleTarget(entity) }
        binding.rvInAppTargets.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = this@InAppBlockFragment.adapter
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.allTargets.collectLatest { list ->
                adapter.submitList(list)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
