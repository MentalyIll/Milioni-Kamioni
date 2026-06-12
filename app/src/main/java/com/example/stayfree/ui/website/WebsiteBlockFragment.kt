package com.example.stayfree.ui.website

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.stayfree.databinding.FragmentWebsiteBlockBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WebsiteBlockFragment : Fragment() {

    private var _binding: FragmentWebsiteBlockBinding? = null
    private val binding get() = _binding!!
    private val viewModel: WebsiteBlockViewModel by viewModels()
    private lateinit var adapter: WebsiteBlockAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentWebsiteBlockBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = WebsiteBlockAdapter(
            onToggle = { entity -> viewModel.toggleWebsite(entity) },
            onDelete = { id -> viewModel.deleteWebsite(id) }
        )
        binding.rvWebsites.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = this@WebsiteBlockFragment.adapter
        }

        binding.fab.setOnClickListener { showAddDialog() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.websites.collectLatest { list ->
                adapter.submitList(list)
            }
        }
    }

    private fun showAddDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(android.R.layout.simple_list_item_1, null)
        val etDomain = EditText(requireContext()).apply { hint = "e.g. youtube.com" }
        val etCap = EditText(requireContext()).apply { hint = "Daily cap (minutes, 0 = always block)" }

        AlertDialog.Builder(requireContext())
            .setTitle("Add Website Block")
            .setView(LinearLayout(requireContext()).apply {
                orientation = android.widget.LinearLayout.VERTICAL
                addView(etDomain)
                addView(etCap)
            })
            .setPositiveButton("Add") { _, _ ->
                val domain = etDomain.text.toString().trim()
                val capMinutes = etCap.text.toString().toLongOrNull() ?: 0L
                if (domain.isNotEmpty()) {
                    val capMs = if (capMinutes <= 0) null else capMinutes * 60_000L
                    viewModel.addWebsite(domain, capMs)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
