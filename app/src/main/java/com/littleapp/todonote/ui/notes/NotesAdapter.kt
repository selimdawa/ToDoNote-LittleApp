package com.littleapp.todonote.ui.notes

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.littleapp.todonote.databinding.ItemNoteBinding
import com.littleapp.todonote.data.Notes

class NotesAdapter(
    private val listener: OnItemClickListener,
) : RecyclerView.Adapter<NotesAdapter.NotesViewHolder>() {

    private val differCallback = object : DiffUtil.ItemCallback<Notes>() {
        override fun areItemsTheSame(oldItem: Notes, newItem: Notes): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Notes, newItem: Notes): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    inner class NotesViewHolder(private val binding: ItemNoteBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(differ.currentList[position])
                }
            }

            binding.deleteNoteButton.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onDeleteNoteClick(differ.currentList[position])
                }
            }
        }

        fun bind(note: Notes) {
            binding.apply {
                titleTextView.text = note.title
                contentTextView.text = note.content
                dateTextView.text = note.createdDateFormatted
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotesViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemNoteBinding.inflate(inflater, parent, false)
        return NotesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotesViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size

    interface OnItemClickListener {
        fun onItemClick(note: Notes)
        fun onDeleteNoteClick(note: Notes)
    }
}