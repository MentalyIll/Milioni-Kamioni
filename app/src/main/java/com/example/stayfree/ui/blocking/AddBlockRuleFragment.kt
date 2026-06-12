package com.example.stayfree.ui.blocking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.stayfree.databinding.FragmentAddBlockRuleBinding
import com.example.stayfree.util.AppInfoUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddBlockRuleFragment : Fragment() {

    private var _binding: FragmentAddBlockRuleBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AddBlockRuleViewModel by viewModels()

    private var selectedPackage: String = ""
    private val installedApps by lazy { AppInfoUtils.getInstalledApps(requireContext()) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddBlockRuleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Populate app picker
        val appNames = installedApps.map { it.appName }
        val appAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, appNames)
        binding.spinnerApp.adapter = appAdapter
        binding.spinnerApp.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>, v: View?, pos: Int, id: Long) {
                selectedPackage = installedApps[pos].packageName
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>) {}
        }

        // Block type radio group
        binding.rgBlockType.setOnCheckedChangeListener { _, checkedId ->
            binding.layoutDailyLimit.visibility = if (checkedId == binding.rbDailyLimit.id) View.VISIBLE else View.GONE
            binding.layoutSession.visibility = if (checkedId == binding.rbSession.id) View.VISIBLE else View.GONE
            binding.layoutSchedule.visibility = if (checkedId == binding.rbSchedule.id) View.VISIBLE else View.GONE
        }

        binding.btnSave.setOnClickListener { saveRule() }
        binding.btnCancel.setOnClickListener { findNavController().popBackStack() }
    }

    private fun saveRule() {
        if (selectedPackage.isEmpty()) return
        val isPinLocked = binding.switchPinLock.isChecked

        when (binding.rgBlockType.checkedRadioButtonId) {
            binding.rbBlockNow.id -> {
                viewModel.saveBlockNow(selectedPackage, isPinLocked)
            }
            binding.rbDailyLimit.id -> {
                val hours = binding.npLimitHours.value
                val minutes = binding.npLimitMinutes.value
                val limitMs = (hours * 3600L + minutes * 60L) * 1000L
                viewModel.saveDailyLimit(selectedPackage, limitMs, isPinLocked)
            }
            binding.rbSession.id -> {
                val sessionHours = binding.npSessionHours.value
                val sessionMins = binding.npSessionMinutes.value
                val breakMins = binding.npBreakMinutes.value
                val sessionMs = (sessionHours * 3600L + sessionMins * 60L) * 1000L
                val breakMs = breakMins * 60_000L
                viewModel.saveSessionLimit(selectedPackage, sessionMs, breakMs, isPinLocked)
            }
            binding.rbSchedule.id -> {
                val days = buildSelectedDays()
                val startH = binding.tpStart.hour
                val startM = binding.tpStart.minute
                val endH = binding.tpEnd.hour
                val endM = binding.tpEnd.minute
                viewModel.saveSchedule(
                    selectedPackage, days,
                    startH * 60 + startM,
                    endH * 60 + endM,
                    isPinLocked
                )
            }
        }
        findNavController().popBackStack()
    }

    private fun buildSelectedDays(): String {
        val selected = mutableListOf<String>()
        if (binding.cbMon.isChecked) selected.add("MON")
        if (binding.cbTue.isChecked) selected.add("TUE")
        if (binding.cbWed.isChecked) selected.add("WED")
        if (binding.cbThu.isChecked) selected.add("THU")
        if (binding.cbFri.isChecked) selected.add("FRI")
        if (binding.cbSat.isChecked) selected.add("SAT")
        if (binding.cbSun.isChecked) selected.add("SUN")
        return selected.joinToString(",")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
