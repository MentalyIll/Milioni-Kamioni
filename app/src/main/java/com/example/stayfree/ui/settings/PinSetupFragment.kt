package com.example.stayfree.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.stayfree.data.local.preferences.AppPreferences
import com.example.stayfree.databinding.FragmentPinSetupBinding
import com.example.stayfree.util.PinHasher
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PinSetupFragment : Fragment() {

    @Inject lateinit var prefs: AppPreferences

    private var _binding: FragmentPinSetupBinding? = null
    private val binding get() = _binding!!
    private var firstPin: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPinSetupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnConfirm.setOnClickListener {
            val pin = binding.etPin.text.toString()
            if (pin.length < 4) {
                Toast.makeText(requireContext(), "PIN must be at least 4 digits", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (firstPin == null) {
                firstPin = pin
                binding.tvInstruction.text = "Confirm your PIN"
                binding.etPin.text?.clear()
            } else {
                if (firstPin == pin) {
                    val hash = PinHasher.hash(pin)
                    CoroutineScope(Dispatchers.IO).launch {
                        prefs.setPinHash(hash)
                        prefs.setPinEnabled(true)
                    }
                    Toast.makeText(requireContext(), "PIN set successfully", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                } else {
                    Toast.makeText(requireContext(), "PINs do not match", Toast.LENGTH_SHORT).show()
                    firstPin = null
                    binding.tvInstruction.text = "Set your PIN"
                    binding.etPin.text?.clear()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
