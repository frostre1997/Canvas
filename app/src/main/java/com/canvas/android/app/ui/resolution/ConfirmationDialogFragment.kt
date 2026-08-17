package com.canvas.android.app.ui.resolution

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.canvas.android.app.R
import com.canvas.android.app.units.ApiCaller
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ConfirmationDialogFragment : DialogFragment() {

    private lateinit var apiCaller: ApiCaller
    private lateinit var dialog: AlertDialog
    private lateinit var negativeButton: Button
    private val viewModel: ConfirmationDialogViewModel by viewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        apiCaller = ApiCaller()

        viewModel.confirmCountdown.observe(this) { countdown ->
            if (::negativeButton.isInitialized) {
                if (countdown == 0) {
                    negativeButton.performClick()
                } else {
                    negativeButton.text = getString(R.string.undo_changes, "${countdown}s")
                }
            }
        }

        isCancelable = false

        dialog = AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.success))
            .setMessage(getString(R.string.reset_hint))
            .setPositiveButton(getString(R.string.looks_fine), null)
            .setNegativeButton(getString(R.string.undo_changes, "10s"), null)
            .create()

        dialog.setOnShowListener {
            onDialogShow()
        }

        return dialog
    }

    private fun onDialogShow() {
        negativeButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE)

        negativeButton.setOnClickListener {
            negativeButton.text = getString(R.string.undo_changes, getString(R.string.undone))
            apiCaller.resetResolution()
            dismiss()
        }

        // Start countdown
        viewModel.setCountdown(10)
        CoroutineScope(Dispatchers.Main).launch {
            for (i in 9 downTo 0) {
                delay(1000)
                viewModel.setCountdown(i)
            }
        }
    }
}
