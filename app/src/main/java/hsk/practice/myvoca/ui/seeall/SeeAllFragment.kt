package hsk.practice.myvoca.ui.seeall

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.*
import android.content.res.Resources
import android.os.*
import android.util.Log
import android.view.*
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.TranslateAnimation
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import hsk.practice.myvoca.Constants
import hsk.practice.myvoca.R
import hsk.practice.myvoca.databinding.FragmentSeeAllBinding
import hsk.practice.myvoca.framework.RoomVocabulary
import hsk.practice.myvoca.framework.VocaPersistenceDatabase
import hsk.practice.myvoca.services.notification.ShowNotificationService
import hsk.practice.myvoca.ui.VocaViewModelFactory
import hsk.practice.myvoca.ui.activity.EditVocaActivity
import hsk.practice.myvoca.ui.seeall.VocabularyTouchHelper.VocabularyTouchHelperListener
import hsk.practice.myvoca.ui.seeall.listeners.ShowVocaOnNotification
import hsk.practice.myvoca.ui.seeall.recyclerview.VocaRecyclerViewAdapter
import hsk.practice.myvoca.ui.seeall.recyclerview.VocaRecyclerViewAdapter.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Most important fragment in this application!
 * This fragment shows all vocabularies in the database with RecyclerView.
 *
 *
 * A user can edit the vocabulary.
 * To edit the vocabulary, long-click the item and select '수정' option.
 * Then EditVocabulary will be shown.
 *
 *
 * A user can delete vocabulary in two ways.
 * 1. To delete the vocabulary, long-click the item and select '삭제' option.
 * Then the delete-mode is enabled.
 * When delete-mode is enabled, user can select items to delete.
 * Press the '삭제' button at the bottom to delete the item permanently.
 * 2. Swipe the item you want to delete.
 * Then the item will be deleted and a SnackBar will be shown in the bottom.
 * If you want to restore the item, click the '실행 취소' button at the SnackBar.
 * Note that the SnackBar fades away in a few seconds.
 *
 *
 * A user can search the word. Supports both english word search and korean meaning search.
 * Click the search button at the right top, type a word to search, and press the button at the keyboard.
 * Then the result will be shown at the RecyclerView.
 * You can edit and delete vocabularies at the result, same as above.
 *
 *
 * A user can sort vocabularies in two ways.
 * Click the TextView at the right to choose the criteria.
 * 1. Sort vocabularies in an alphabetic order of the field 'eng'. This is the default sorting method.
 * 2. Sort vocabularies by latest edited time.
 */
class SeeAllFragment : Fragment(),
        OnSelectModeListener, VocabularyTouchHelperListener, ShowVocaOnNotification {

    private var _binding: FragmentSeeAllBinding? = null

    private val binding
        get() = _binding!!

    private lateinit var parentActivity: AppCompatActivity
    private lateinit var seeAllViewModel: SeeAllViewModel

    private lateinit var toolbar: Toolbar
    private lateinit var drawer: DrawerLayout

    private var isSearchMode = false
    private var vocaRecyclerViewAdapter: VocaRecyclerViewAdapter? = null

    private val seeAllLayout
        get() = binding.layoutSeeAll

    private val deleteLayout
        get() = binding.layoutDelete

    private val deleteVocabularyButton
        get() = binding.buttonDeleteVocabulary

    private val deleteCancelButton
        get() = binding.addButtonCancel

    private val sortSpinner
        get() = binding.spinnerSort

    private val vocaRecyclerView
        get() = binding.recyclerViewVoca

    private lateinit var searchMenuItem: MenuItem
    private lateinit var searchView: SearchView

    private var window: Window? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        parentActivity = activity as AppCompatActivity
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View {
//        isFragmentShown = true

        seeAllViewModel = ViewModelProvider(this, VocaViewModelFactory(VocaPersistenceDatabase.getInstance(requireContext()))).get(SeeAllViewModel::class.java)
        seeAllViewModel.currentVocabulary.observe(viewLifecycleOwner) {
            it?.let { vocaRecyclerViewAdapter?.submitList(it) }
            vocaRecyclerViewAdapter?.notifyDataSetChanged()
        }

        _binding = FragmentSeeAllBinding.inflate(inflater, container, false)
        binding.viewModel = seeAllViewModel
        binding.lifecycleOwner = viewLifecycleOwner

        toolbar = parentActivity.findViewById(R.id.toolbar)
        drawer = parentActivity.findViewById(R.id.drawer_layout)
        setHasOptionsMenu(true)

        // Delete one or many items (with checkbox)
        deleteVocabularyButton.setOnClickListener {
            val selectedItems = vocaRecyclerViewAdapter?.getSelectedItems()
            val builder = AlertDialog.Builder(parentActivity)
            builder.setTitle("삭제")
            if (selectedItems != null) {
                builder.setMessage("${selectedItems.size}개의 단어를 삭제합니다.")
            }
            builder.setIcon(android.R.drawable.ic_dialog_alert)
            builder.setPositiveButton("확인") { _, _ ->
                vocaRecyclerViewAdapter?.deleteVocabularies()
                seeAllViewModel.onDeleteModeChange(false)
            }
            builder.setNegativeButton("취소") { _, _ -> }
            val dialog = builder.create()
            dialog.show()
        }
        deleteCancelButton.setOnClickListener { seeAllViewModel.onDeleteModeChange(false) }

        showSpinner()

        // Load adapter asynchronously
        lifecycleScope.launch(Dispatchers.IO) {
            setAdapter()
        }

        vocaRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            addItemDecoration(DividerItemDecoration(context, LinearLayoutManager(parentActivity).orientation))

            val callback = VocabularyTouchHelper(0, ItemTouchHelper.LEFT, this@SeeAllFragment)
            ItemTouchHelper(callback).attachToRecyclerView(this)
        }

        // what to do when update request is given
        seeAllViewModel.eventVocabularyUpdated.observe(viewLifecycleOwner) { position ->
            position?.let {
                val target = seeAllViewModel.currentVocabulary.value?.get(position)
                Timber.d("Update: $target")
                val intent = Intent(parentActivity.applicationContext, EditVocaActivity::class.java)
                intent.putExtra(Constants.POSITION, position)
                intent.putExtra(Constants.EDIT_VOCA, target)
                startActivityForResult(intent, Constants.CALL_EDIT_VOCA_ACTIVITY)
                seeAllViewModel.onVocabularyUpdateComplete()
            }
        }
        // what to do when delete mode is changed at the adapter
        seeAllViewModel.eventDeleteModeChanged.observe(viewLifecycleOwner) { mode ->
            Timber.d("Delete mode: $mode")
            mode?.let {
                if (mode) {
                    deleteLayout.visibility = View.VISIBLE
                    vocaRecyclerViewAdapter?.notifyItemsChanged()
                } else {
                    deleteLayout.visibility = View.GONE
                    vocaRecyclerViewAdapter?.clearSelectedState()
                }
                seeAllViewModel.onDeleteModeUpdateComplete()
            }
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Inflates options menu.
     * Here, only a search button will be shown.
     *
     * @param menu menu object where the menu bar is shown
     * @param inflater MenuInflater
     */
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main, menu)
        Log.d("HSK APP", "onCreateOptionsMenu() in SeeAllFragment")
        searchMenuItem = menu.findItem(R.id.action_search)
        searchMenuItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                // Called when SearchView is expanding
                animateSearchToolbar(1, containsOverflow = true, show = true)
                vocaRecyclerViewAdapter?.enableSearchMode()
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                // Called when SearchView is collapsing
                if (searchMenuItem.isActionViewExpanded) {
                    animateSearchToolbar(1, containsOverflow = true, show = false)
                    vocaRecyclerViewAdapter?.disableSearchMode()
                }
                return true
            }
        })

        searchView = searchMenuItem.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { searchVocabulary(it) }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
    }

    /**
     * Search the vocabulary and show the result
     *
     * @param query query string to search, only english supported.
     */
    private fun searchVocabulary(query: String) {
        vocaRecyclerViewAdapter?.searchVocabulary(query)
    }

    /**
     * Open and close the search view at the ActionBar.
     *
     * @param show Show search layout if true, otherwise hide the layout
     */
    fun animateSearchToolbar(numberOfMenuIcon: Int, containsOverflow: Boolean, show: Boolean) {
        toolbar.setBackgroundColor(ContextCompat.getColor(parentActivity, R.color.design_default_color_primary))
        if (window == null) {
            window = parentActivity.window
        }
        // set status bar color
        // window.setStatusBarColor(ContextCompat.getColor(parentActivity, android.R.color.white));
        val animationDuration = 500
        if (show) {
            isSearchMode = true
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val width = toolbar.width -
                        (if (containsOverflow) resources.getDimensionPixelSize(R.dimen.abc_action_button_min_width_overflow_material) else 0) -
                        resources.getDimensionPixelSize(R.dimen.abc_action_button_min_width_material) * numberOfMenuIcon / 2
                val createCircularReveal = ViewAnimationUtils.createCircularReveal(toolbar,
                        if (isRtl(resources)) toolbar.width - width else width, toolbar.height / 2, 0.0f, width.toFloat())
                createCircularReveal.duration = animationDuration.toLong()
                createCircularReveal.start()
            } else {
                val translateAnimation = TranslateAnimation(0.0f, 0.0f, (-toolbar.height).toFloat(), 0.0f)
                translateAnimation.duration = animationDuration.toLong()
                toolbar.clearAnimation()
                toolbar.startAnimation(translateAnimation)
            }
        } else {
            isSearchMode = false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val width = toolbar.width -
                        (if (containsOverflow) resources.getDimensionPixelSize(R.dimen.abc_action_button_min_width_overflow_material) else 0) -
                        resources.getDimensionPixelSize(R.dimen.abc_action_button_min_width_material) * numberOfMenuIcon / 2
                val createCircularReveal = ViewAnimationUtils.createCircularReveal(toolbar,
                        if (isRtl(resources)) toolbar.width - width else width, toolbar.height / 2, width.toFloat(), 0.0f)
                createCircularReveal.duration = animationDuration.toLong()
                createCircularReveal.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)
                        toolbar.setBackgroundColor(getThemeColor(parentActivity, R.attr.colorPrimary))
                        //window.setStatusBarColor(getThemeColor(parentActivity, R.attr.colorPrimaryDark));
                    }
                })
                createCircularReveal.start()
            } else {
                val alphaAnimation = AlphaAnimation(1.0f, 0.0f)
                val translateAnimation: Animation = TranslateAnimation(0.0f, 0.0f, 0.0f, -toolbar.height.toFloat())
                val animationSet = AnimationSet(true)
                animationSet.addAnimation(alphaAnimation)
                animationSet.addAnimation(translateAnimation)
                animationSet.duration = animationDuration.toLong()
                animationSet.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation?) {}
                    override fun onAnimationEnd(animation: Animation?) {
                        toolbar.setBackgroundColor(getThemeColor(parentActivity, R.attr.colorPrimary))
                    }

                    override fun onAnimationRepeat(animation: Animation?) {}
                })
                toolbar.startAnimation(animationSet)
            }
            //window.setStatusBarColor(getThemeColor(parentActivity, R.attr.colorPrimaryDark));
        }
    }

    /**
     * Check if direction of the swipe is right-to-left
     *
     * @param resources resource swiped
     * @return true if direction is right-to-left, false otherwise
     */
    private fun isRtl(resources: Resources?): Boolean {
        return if (resources != null) {
            return resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL
        } else false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.isFocusableInTouchMode = true
        view.requestFocus()
        view.setOnKeyListener { _, keyCode, _ ->
            keyCode == KeyEvent.KEYCODE_BACK &&
                    seeAllViewModel.deleteMode &&
                    !drawer.isDrawerOpen(GravityCompat.START)
        }
    }

    /**
     * Callback method when a item is swiped.
     * Only supports right-to-left swipe(deletes a word).
     *
     * @param viewHolder ViewHolder of the item
     * @param direction direction of the swipe action
     * @param position position of the item in the RecyclerView
     */
    override fun onSwiped(viewHolder: RecyclerView.ViewHolder?, direction: Int, position: Int) {
        if (viewHolder is VocaViewHolder) {
            vocaRecyclerViewAdapter?.showDeleteSnackbar(requireView(), position)
        }
    }

    /**
     * Shows sort options at the dialog menu
     */
    private fun showSpinner() {
        val items = resources.getStringArray(R.array.sort_method)
        val sortAdapter = ArrayAdapter(parentActivity.applicationContext, R.layout.spinner_item, items)
        sortSpinner.adapter = sortAdapter
        sortSpinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (sortState == position) {
                    return
                }
                sortState = position
                vocaRecyclerViewAdapter?.sortItems(sortState)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                sortState = 0
                vocaRecyclerViewAdapter?.sortItems(sortState)
            }
        }
        sortSpinner.prompt = "정렬 방법"
        sortSpinner.setSelection(sortState)
        sortSpinner.gravity = Gravity.CENTER
    }

    // Implements interfaces
    override fun onDeleteModeEnabled() {
        deleteLayout.visibility = View.VISIBLE
    }

    override fun onDeleteModeDisabled() {
        vocaRecyclerViewAdapter?.clearSelectedState()
        deleteLayout.visibility = View.GONE
    }

    /**
     * Show a vocabulary at the notification.
     * @param target vocabulary to show at the notification
     */
    override fun showVocabularyOnNotification(target: RoomVocabulary) {
        val intent = Intent(context, ShowNotificationService::class.java)
        intent.putExtra(ShowNotificationService.SHOW_VOCA, target)
        parentActivity.startService(intent)
        Snackbar.make(seeAllLayout, "알림에 보임: ${target.eng}", Snackbar.LENGTH_LONG).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d("HSK APP", "requestCode = $requestCode, resultCode = $resultCode")
        if (requestCode == Constants.CALL_EDIT_VOCA_ACTIVITY && resultCode == Constants.EDIT_NEW_VOCA_OK) {
            vocaRecyclerViewAdapter?.notifyItemsChanged()
        } else if (requestCode == Constants.CALL_ADD_VOCA_ACTIVITY && resultCode == Constants.ADD_NEW_VOCA_OK) {
            vocaRecyclerViewAdapter?.notifyDataSetChanged()
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    /**
     * Show adapter when loaded
     */
    private fun setAdapter() {
        vocaRecyclerViewAdapter = VocaRecyclerViewAdapter(seeAllViewModel,
                showVocaOnNotification = this)
        vocaRecyclerView.adapter = vocaRecyclerViewAdapter
    }

    companion object {
        private var sortState = 0

        /**
         * Finds color of the current theme.
         *
         * @return Appropriate background color for the theme
         */
        private fun getThemeColor(context: Context?, id: Int): Int {
            val theme = context?.theme
            val a = theme?.obtainStyledAttributes(intArrayOf(id))
            val result = a?.getColor(0, 0)
            a?.recycle()
            return result ?: 0
        }
    }
}