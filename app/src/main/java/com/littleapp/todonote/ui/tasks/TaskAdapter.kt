package com.littleapp.todonote.ui.tasks

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.littleapp.todonote.databinding.ItemTaskBinding
import com.littleapp.todonote.data.Task

class TaskAdapter(
    private val listener: OnItemClickListener,
) : ListAdapter<Task, TaskAdapter.TaskViewHolder>(DiffCallBack()) {

    inner class TaskViewHolder(private val binding: ItemTaskBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(getItem(position))
                }
            }

            binding.taskCheckBox.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val task = getItem(position)
                    listener.onCheckBoxClick(task, binding.taskCheckBox.isChecked)
                }
            }
        }

        fun bind(task: Task) {
            binding.apply {
                taskCheckBox.isChecked = task.completed
                taskName.text = task.name
                taskName.paint.isStrikeThruText = task.completed
                taskPriority.isVisible = task.important
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemTaskBinding.inflate(inflater, parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    interface OnItemClickListener {
        fun onItemClick(task: Task)
        fun onCheckBoxClick(task: Task, isChecked: Boolean)
    }

    class DiffCallBack : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean =
            oldItem == newItem
    }
}