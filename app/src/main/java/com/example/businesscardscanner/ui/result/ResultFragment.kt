package com.example.businesscardscanner.ui.result

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.businesscardscanner.data.model.BusinessCard
import com.example.businesscardscanner.databinding.FragmentResultBinding

class ResultFragment : Fragment() {

    private var _binding: FragmentResultBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val card = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable("businessCard", BusinessCard::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable("businessCard")
        }
        if (card != null) {
            displayCard(card)
        }

        binding.saveButton.setOnClickListener {
            // Save to history (not implemented yet)
            // findNavController().navigate(R.id.action_resultFragment_to_historyFragment)
        }
    }

    private fun displayCard(card: BusinessCard) {
        val info = StringBuilder().apply {
            append("Name: ${card.name ?: "N/A"}\n")
            append("Title: ${card.title ?: "N/A"}\n")
            append("Company: ${card.company ?: "N/A"}\n")
            append("Address: ${card.address ?: "N/A"}\n")
            append("Website: ${card.website ?: "N/A"}\n")
            append("Email: ${card.emails?.joinToString() ?: "N/A"}\n")
            append("Phones:\n")
            card.phones?.forEach {
                append(" - ${it.type}: ${it.number}\n")
            }
            append("WeChat: ${card.wechat ?: "N/A"}\n")
            append("Line: ${card.line ?: "N/A"}\n")
            append("Note: ${card.note ?: "N/A"}")
        }.toString()

        binding.textViewResult.text = info
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
