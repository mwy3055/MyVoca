package hsk.practice.myvoca.ui.seeall.recyclerview

import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import hsk.practice.myvoca.databinding.VocaViewBinding
import hsk.practice.myvoca.framework.RoomVocabulary
import hsk.practice.myvoca.getTimeString
import hsk.practice.myvoca.ui.seeall.recyclerview.VocaRecyclerViewAdapter.VocaViewHolder

/**
 * RecyclerAdapter for each vocabulary.
 * Implemented as Singleton to manage the view more easily.
 *
 * For further information, Please refer the comments above some methods.
 */
class VocaRecyclerViewAdapter(
    private val deleteData: DeleteData,
    private val itemListener: ItemListener
) :
    ListAdapter<RoomVocabulary, VocaViewHolder>(RoomVocabularyDiffCallback()) {

    /**
     * Interface which provides data related to delete mode.
     */
    interface DeleteData {
        val deleteMode: LiveData<Boolean>
        fun isSelected(position: Int): Boolean
    }

    /**
     * Menu item click listener for each item in RecyclerView.
     * Implementation of this class can vary by context that the adapter is used.
     */
    interface ItemListener {
        /**
         * Callback invoked when root view is clicked
         *
         * @param view Clicked view
         * @param position Position of the view
         */
        fun onRootClick(view: View, position: Int)

        /**
         * Callback invoked when delete box is clicked
         *
         * @param view Clicked view
         * @param position Position of the view
         */
        fun onDeleteCheckBoxClick(view: View, position: Int)

        /**
         * Callback invoked when context menu has to be created
         *
         * @param menu Menu item which will be shown to the user
         * @param view View which this callback is set
         * @param contextMenuInfo Extra information about the item for which the context menu should be shown.
         *                        This information will vary depending on the class of v.
         */
        fun onCreateContextMenu(
            menu: ContextMenu,
            view: View,
            contextMenuInfo: ContextMenu.ContextMenuInfo?,
            position: Int
        )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VocaViewHolder {
        val vocaBinding =
            VocaViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        val holder = VocaViewHolder(vocaBinding, itemListener)
        return holder
    }

    override fun onBindViewHolder(holder: VocaViewHolder, position: Int) {
        val vocabulary = getItem(position) ?: RoomVocabulary.nullVocabulary
        holder.bind(vocabulary, deleteData.isSelected(position))
    }

    fun notifyItemsChanged() {
        (0 until itemCount).forEach { notifyItemChanged(it) }
    }

    /**
     * ViewHolder for vocabulary object.
     * Can be a separate, not a inner, class if deleteData dependency is injected
     */
    inner class VocaViewHolder(
        private val vocaBinding: VocaViewBinding,
        private val itemListener: ItemListener
    ) : RecyclerView.ViewHolder(vocaBinding.root) {

        val viewForeground
            get() = vocaBinding.viewForeground
        val viewBackground
            get() = vocaBinding.viewBackground

        // Another position rather than absolute position may be needed
        private val pos: Int
            get() = absoluteAdapterPosition

        init {
            vocaBinding.root.setOnClickListener {
                itemListener.onRootClick(it, pos)
            }
            vocaBinding.root.setOnCreateContextMenuListener { contextMenu, view, contextMenuInfo ->
                itemListener.onCreateContextMenu(contextMenu, view, contextMenuInfo, pos)
            }
            vocaBinding.deleteCheckBox.setOnClickListener {
                itemListener.onDeleteCheckBoxClick(it, pos)
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
                if (deleteData.deleteMode.value == true) {
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