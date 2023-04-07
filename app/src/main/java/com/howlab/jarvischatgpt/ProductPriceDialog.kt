package com.howlab.jarvischatgpt

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.howlab.jarvischatgpt.chat.ChatActivity
import com.howlab.jarvischatgpt.databinding.DialogAiBinding
import com.howlab.jarvischatgpt.databinding.DialogPriceBinding

class ProductPriceDialog : DialogFragment() {

    private lateinit var binding: DialogPriceBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogPriceBinding.inflate(LayoutInflater.from(context))

        val builder = AlertDialog.Builder(requireActivity())
            .setView(binding.root)

        binding.dialogConfirmButton.setOnClickListener {
            requireActivity().supportFragmentManager.setFragmentResult(
                "KEY_PRICE",
                bundleOf("KEY" to binding.nameText.text.toString())
            )
            dismiss()
        }

        return builder.create()
    }
}