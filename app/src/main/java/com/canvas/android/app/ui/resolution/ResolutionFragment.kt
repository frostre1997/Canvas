package com.canvas.android.app.ui.resolution

import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import com.canvas.android.app.MainViewModel
import com.canvas.android.app.R
import com.canvas.android.app.databinding.FragmentResolutionBinding
import com.canvas.android.app.units.ApiCaller
import com.google.android.material.snackbar.Snackbar
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

class ResolutionFragment : Fragment() {

    private var _binding: FragmentResolutionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ResolutionViewModel by viewModels()
    private val mainViewModel by activityViewModels<MainViewModel>()
    private lateinit var apiCaller: ApiCaller

    private var stuckScaleValue = 0

    private val textHeight: EditText get() = binding.resolutionEditor.textHeight
    private val textWidth: EditText get() = binding.resolutionEditor.textWidth
    private val textDpi: EditText get() = binding.resolutionEditor.textDpi

    private val scaledHeight get() = textHeight.text.toString().toFloatOrNull() ?: 0f
    private val scaledWidth get() = textWidth.text.toString().toFloatOrNull() ?: 0f
    private val scaledDpi get() = textDpi.text.toString().toFloatOrNull() ?: 0f
    private val scaleValue get() = binding.resolutionEditor.sliderScale.progress

    private val physical get() = viewModel.physicalResolutionMap.value

    private val physicalAdjRatio get() = physical?.let {
        sqrt(
            (scaledHeight.pow(2) + scaledWidth.pow(2)) /
                    (it["height"]?.pow(2)!! + it["width"]?.pow(2)!!)
        )
    } ?: 1f

    private val baseDpi get() = (physical?.get("dpi") ?: 240f) * physicalAdjRatio

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResolutionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        apiCaller = ApiCaller()

        mainViewModel.shizukuPermissionGranted.observe(viewLifecycleOwner) {
            if (it == true) {
                viewModel.fetchScreenResolution()
            }
        }

        if (mainViewModel.shizukuPermissionGranted.value == true) {
            viewModel.fetchScreenResolution()
        }

        viewModel.physicalResolutionMap.observe(viewLifecycleOwner) {
            if (it == null) return@observe
            binding.textResolution.text = "Physical ${it["height"]}x${it["width"]}; DPI ${it["dpi"]}"
        }

        viewModel.resolutionMap.observe(viewLifecycleOwner) {
            it?.let {
                textHeight.setText(it["height"]?.toInt()?.toString() ?: "")
                textWidth.setText(it["width"]?.toInt()?.toString() ?: "")
                textDpi.setText(it["dpi"]?.toInt()?.toString() ?: "")
            }
        }

        binding.resolutionEditor.sliderScale.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                if (physical == null) return
                if (fromUser) stuckScaleValue = progress - 50
                updateDpiEditor()
            }
            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {}
        })

        textWidth.doAfterTextChanged { editable: Editable? ->
            handleWidthTextChange(editable ?: return@doAfterTextChanged)
        }

        textDpi.doAfterTextChanged { s: Editable? ->
            if (s.isNullOrBlank()) return@doAfterTextChanged
            if (physical == null) return@doAfterTextChanged
            adjustSliderBasedOnDpi()
        }

        binding.btApply.setOnClickListener {
            if (mainViewModel.shizukuPermissionGranted.value != true) {
                Snackbar.make(binding.root, "Shizuku not ready", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!checkValidResolution(null, null)) return@setOnClickListener

            apiCaller.applyResolution(scaledHeight, scaledWidth, scaledDpi)
            val navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main)
            navController.navigate(R.id.nav_resolution_confirmation)
        }

        binding.btReset.setOnClickListener {
            if (mainViewModel.shizukuPermissionGranted.value != true) {
                Snackbar.make(binding.root, "Shizuku not ready", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            apiCaller.resetResolution()
            viewModel.fetchScreenResolution()
        }
    }

    private fun checkValidResolution(updatedScaleValue: Int?, updatedDpi: Int?): Boolean {
        val scale = updatedScaleValue ?: scaleValue
        val dpi = updatedDpi ?: scaledDpi.toInt()

        if (scale !in -50..50 || dpi !in 280..840 || scaledHeight !in 480f..4096f || scaledWidth !in 480f..4096f) {
            textDpi.error = getString(R.string.invalid)
            return false
        }
        textDpi.error = null
        return true
    }

    private fun handleWidthTextChange(editable: Editable) {
        if (editable.isBlank()) return
        if (physical == null) return

        val aspectRatio = physical!!["height"]!! / physical!!["width"]!!
        try {
            val equalRatioHeight = editable.toString().toInt() * aspectRatio
            textHeight.setText(equalRatioHeight.roundToInt().toString())
        } catch (e: NumberFormatException) {
            return
        }
        updateDpiEditor()
        checkValidResolution(null, null)
    }

    private fun updateDpiEditor() {
        val scaleRatio = (scaleValue * 0.01 + 1).toFloat()
        val updatedDpi = (baseDpi * scaleRatio).roundToInt()
        textDpi.setText(updatedDpi.toString())
        checkValidResolution(updatedScaleValue = null, updatedDpi = updatedDpi)
    }

    private fun adjustSliderBasedOnDpi() {
        val scaleRatio = scaledDpi / baseDpi
        val updatedScaleValue = (scaleRatio - 1) * 100
        if (checkValidResolution(updatedScaleValue = updatedScaleValue.toInt(), updatedDpi = null)) {
            binding.resolutionEditor.sliderScale.progress = ((updatedScaleValue / 5).roundToInt() * 5 + 50)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
