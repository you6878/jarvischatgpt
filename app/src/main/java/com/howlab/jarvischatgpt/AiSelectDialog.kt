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

class AiSelectDialog : DialogFragment() {

    private lateinit var binding: DialogAiBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogAiBinding.inflate(LayoutInflater.from(context))

        val builder = AlertDialog.Builder(requireActivity())
            .setView(binding.root)

        binding.dialogContent.setOnClickListener {
            requireActivity().supportFragmentManager.setFragmentResult(
                "KEY_CONTENT",
                bundleOf("KEY" to "CONTENT")
            )

            dismiss()
        }

        binding.dialogPrice.setOnClickListener {
            requireActivity().supportFragmentManager.setFragmentResult(
                "KEY_PRICE",
                bundleOf("KEY" to "PRICE")
            )
            dismiss()
        }

        binding.dialogConfirmButton.setOnClickListener {
            dismiss()
        }

        return builder.create()
    }
}