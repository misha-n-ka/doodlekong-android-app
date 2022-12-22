package ru.mkirilkin.doodlekong.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.mkirilkin.doodlekong.databinding.ItemPlayerBinding
import com.mkirilkin.doodlekong.databinding.ItemRoomBinding
import com.plcourse.mkirilkin.data.PlayerData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.mkirilkin.doodlekong.data.remote.websocket.models.Room
import javax.inject.Inject

class PlayerAdapter @Inject constructor() :
    RecyclerView.Adapter<PlayerAdapter.PlayerViewHolder>() {

    class PlayerViewHolder(val binding: ItemPlayerBinding) : RecyclerView.ViewHolder(binding.root)

    var players = listOf<PlayerData>()
        private set

    override fun getItemCount(): Int {
        return players.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        return PlayerViewHolder(
            ItemPlayerBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        val player = players[position]
        holder.binding.apply {
            val playerTankText = "${player.rank}. "
            tvRank.text = playerTankText
            tvScore.text = player.score.toString()
            tvUsername.text = player.userName
            ivPencil.isVisible = player.isDrawig
        }
    }

    suspend fun updateDataset(newDataset: List<PlayerData>) = withContext(Dispatchers.Default) {
        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int {
                return players.size
            }

            override fun getNewListSize(): Int {
                return newDataset.size
            }

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return players[oldItemPosition] == newDataset[newItemPosition]
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return players[oldItemPosition] == newDataset[newItemPosition]
            }
        })
        withContext(Dispatchers.Main) {
            players = newDataset
            diff.dispatchUpdatesTo(this@PlayerAdapter)
        }
    }
}
