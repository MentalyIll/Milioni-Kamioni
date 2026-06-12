package com.example.stayfree.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.stayfree.databinding.FragmentDashboardBinding
import com.example.stayfree.util.TimeUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DashboardViewModel by viewModels()
    private lateinit var topAppsAdapter: TopAppsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupDateNav()
        observeData()
    }

    private fun setupRecyclerView() {
        topAppsAdapter = TopAppsAdapter()
        binding.rvTopApps.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = topAppsAdapter
        }
    }

    private fun setupDateNav() {
        binding.btnToday.setOnClickListener { viewModel.selectToday() }
        binding.btnYesterday.setOnClickListener { viewModel.selectYesterday() }
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            launch {
                viewModel.totalScreenTime.collectLatest { ms ->
                    binding.tvTotalScreenTime.text = TimeUtils.formatDuration(ms)
                }
            }
            launch {
                viewModel.totalUnlocks.collectLatest { count ->
                    binding.tvUnlocks.text = count.toString()
                }
            }
            launch {
                viewModel.topApps.collectLatest { apps ->
                    topAppsAdapter.submitList(apps)
                }
            }
            launch {
                viewModel.activeBlockCount.collectLatest { count ->
                    binding.tvActiveBlocks.text = count.toString()
                }
            }
            launch {
                viewModel.selectedDate.collectLatest { date ->
                    binding.tvSelectedDate.text = date
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
