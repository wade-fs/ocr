package com.wade.ocr.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.wade.ocr.R
import com.wade.ocr.data.local.AppDatabase
import com.wade.ocr.databinding.FragmentHistoryBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: CardAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val dao = AppDatabase.getInstance(requireContext()).cardDao()

        adapter = CardAdapter { clickedCard ->
            val bundle = Bundle().apply { putLong("cardId", clickedCard.id) }
            findNavController().navigate(R.id.action_historyFragment_to_editCardFragment, bundle)
        }
        
        binding.recyclerViewHistory.adapter = adapter

        lifecycleScope.launch {
            dao.getAll().collectLatest { cards ->
                adapter.submitList(cards)
            }
        }
        
        // Swipe to Delete
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val cardToDelete = adapter.currentList[position]
                
                lifecycleScope.launch(Dispatchers.IO) {
                    dao.delete(cardToDelete)
                    
                    Snackbar.make(binding.root, "已刪除名片", Snackbar.LENGTH_LONG)
                        .setAction("復原") {
                            lifecycleScope.launch(Dispatchers.IO) {
                                dao.insert(cardToDelete)
                            }
                        }.show()
                }
            }
        }
        
        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(binding.recyclerViewHistory)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


