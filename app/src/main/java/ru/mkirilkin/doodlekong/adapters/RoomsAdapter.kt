package ru.mkirilkin.doodlekong.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.mkirilkin.doodlekong.databinding.ItemRoomBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.mkirilkin.doodlekong.data.remote.websocket.models.Room
import javax.inject.Inject

class RoomsAdapter @Inject constructor() :
    RecyclerView.Adapter<RoomsAdapter.RoomViewHolder>() {

    class RoomViewHolder(val binding: ItemRoomBinding) : RecyclerView.ViewHolder(binding.root)

    var rooms = listOf<Room>()
        private set

    private var onRoomClickListener: ((Room) -> Unit)? = null

    override fun getItemCount(): Int {
        return rooms.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        return RoomViewHolder(
            ItemRoomBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        val room = rooms[position]
        holder.binding.apply {
            tvRoomName.text = room.name
            val playerCount = "${room.playerCount} / ${room.maxPlayers}"
            tvRoomPersonCount.text = playerCount

            root.setOnClickListener {
                onRoomClickListener?.let { click ->
                    click(room)
                }
            }
        }
    }

    fun setOnRoomClickListener(listener: (Room) -> Unit) {
        onRoomClickListener = listener
    }

    suspend fun updateDataset(newDataset: List<Room>) = withContext(Dispatchers.Default) {
        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int {
                return rooms.size
            }

            override fun getNewListSize(): Int {
                return newDataset.size
            }

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return rooms[oldItemPosition] == newDataset[newItemPosition]
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return rooms[oldItemPosition] == newDataset[newItemPosition]
            }
        })
        withContext(Dispatchers.Main) {
            rooms = newDataset
            diff.dispatchUpdatesTo(this@RoomsAdapter)
        }
    }
}
