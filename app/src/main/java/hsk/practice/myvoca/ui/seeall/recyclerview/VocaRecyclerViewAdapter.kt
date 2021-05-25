package hsk.practice.myvoca.ui.seeall.recyclerview

import android.view.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import hsk.practice.myvoca.databinding.VocaViewBinding
import hsk.practice.myvoca.framework.RoomVocabulary
import hsk.practice.myvoca.getTimeString
import hsk.practice.myvoca.ui.seeall.MenuCode
import hsk.practice.myvoca.ui.seeall.SeeAllViewModel
import hsk.practice.myvoca.ui.seeall.recyclerview.VocaRecyclerViewAdapter.VocaViewHolder

/**
 * RecyclerAdapter for each vocabulary.
 * Implemented as Singleton to manage the view more easily.
 *
 * For further information, Please refer the comments above some methods.
 */
class VocaRecyclerViewAdapter(
    val viewModel: SeeAllViewModel,
    private val menuItemClickListener: OnMenuItemClickListener
) :
    ListAdapter<RoomVocabulary, VocaViewHolder>(RoomVocabularyDiffCallback()) {

    /**
     * Menu item click listener for each item in RecyclerView.
     */
    interface OnMenuItemClickListener {
        /**
         * Callback invoked when each menu item is clicked
         *
         * @param itemId Id of the menu item
         * @param position Position of the RecyclerView item
         */
        fun onClick(itemId: Int, position: Int)
    }

    val deleteMode: Boolean
        get() = viewModel.deleteMode.value!!

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VocaViewHolder {
        val vocaBinding =
            VocaViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        val holder = VocaViewHolder(vocaBinding, menuItemClickListener)
        return holder
    }

    override fun onBindViewHolder(holder: VocaViewHolder, position: Int) {
        val vocabulary = getItem(position) ?: RoomVocabulary.nullVocabulary
        holder.bind(vocabulary, viewModel.isSelected(position))
    }

    fun notifyItemsChanged() {
        (0 until itemCount).forEach { notifyItemChanged(it) }
    }

    /**
     * ViewHolder for vocabulary object.
     * Manages the content of the item and action when the item is clicked or long-clicked.
     */
    inner class VocaViewHolder(
        private val vocaBinding: VocaViewBinding,
        private val onMenuItemClickListener: OnMenuItemClickListener
    ) : RecyclerView.ViewHolder(vocaBinding.root) {

        var viewForeground = vocaBinding.viewForeground
        var viewBackground = vocaBinding.viewBackground

        val realMenuItemClickListener = MenuItem.OnMenuItemClickListener { item ->
            onMenuItemClickListener.onClick(item.itemId, absoluteAdapterPosition)
            true
        }

        init {
            vocaBinding.root.setOnClickListener {
                if (deleteMode) {
                    val position = absoluteAdapterPosition
                    viewModel.switchSelectedState(position)
                    notifyItemChanged(position)
                }
            }
            /* Create Context pop-up menu when long clicked */
            vocaBinding.root.setOnCreateContextMenuListener { contextMenu, view, contextMenuInfo ->
                if (deleteMode) {
                    return@setOnCreateContextMenuListener
                }
                contextMenu?.add(Menu.NONE, MenuCode.EDIT.value, 1, "수정")?.apply {
                    setOnMenuItemClickListener(realMenuItemClickListener)
                }
                contextMenu?.add(Menu.NONE, MenuCode.DELETE.value, 2, "삭제")?.apply {
                    setOnMenuItemClickListener(realMenuItemClickListener)
                }
                contextMenu?.add(Menu.NONE, MenuCode.SHOW_ON_NOTIFICATION.value, 3, "알림에 보이기")
                    ?.apply {
                        setOnMenuItemClickListener(realMenuItemClickListener)
                    }
            }
        }

        fun bind(vocabulary: RoomVocabulary, isChecked: Boolean) {
            setVocabulary(vocabulary)
            if (vocabulary != RoomVocabulary.nullVocabulary) {
                setDeleteCheckBox(isChecked)
            }
        }

        private fun setVocabulary(vocabulary: RoomVocabulary?) {
            vocabulary?.apply {
                vocaBinding.vocaEng.text = eng
                vocaBinding.vocaKor.text = kor
                vocaBinding.lastEditTime.text = (lastEditedTime * 1000).getTimeString()
            }
        }

        private fun setDeleteCheckBox(checked: Boolean) {
            vocaBinding.deleteCheckBox.apply {
                if (deleteMode) {
                    visibility = View.VISIBLE
                    isChecked = checked
                } else {
                    visibility = View.GONE
                    isChecked = false
                }
            }
        }
    }
}

class RoomVocabularyDiffCallback : DiffUtil.ItemCallback<RoomVocabulary>() {
    override fun areContentsTheSame(oldItem: RoomVocabulary, newItem: RoomVocabulary): Boolean {
        return oldItem == newItem
    }

    override fun areItemsTheSame(oldItem: RoomVocabulary, newItem: RoomVocabulary): Boolean {
        return oldItem.eng == newItem.eng
    }
}