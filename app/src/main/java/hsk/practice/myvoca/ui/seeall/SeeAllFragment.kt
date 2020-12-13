package hsk.practice.myvoca.ui.seeall

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.*
import android.content.res.Resources
import android.graphics.Color
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
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import database.Vocabulary
import hsk.practice.myvoca.Constants
import hsk.practice.myvoca.R
import hsk.practice.myvoca.VocaViewModel
import hsk.practice.myvoca.databinding.FragmentSeeAllBinding
import hsk.practice.myvoca.services.notification.ShowNotificationService
import hsk.practice.myvoca.ui.activity.EditVocaActivity
import hsk.practice.myvoca.ui.seeall.VocabularyTouchHelper.VocabularyTouchHelperListener
import hsk.practice.myvoca.ui.seeall.recyclerview.VocaRecyclerViewAdapter
import hsk.practice.myvoca.ui.seeall.recyclerview.VocaRecyclerViewAdapter.*

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
        OnSelectModeListener, ShowVocaOnNotification, VocabularyTouchHelperListener, OnEditVocabularyListener {

    private var _binding: FragmentSeeAllBinding? = null

    private val binding
        get() = _binding!!

    private lateinit var parentActivity: AppCompatActivity
    private lateinit var viewModelProvider: ViewModelProvider
    private lateinit var seeAllViewModel: SeeAllViewModel
    private lateinit var vocaViewModel: VocaViewModel

    private lateinit var toolbar: Toolbar
    private lateinit var drawer: DrawerLayout

    private var isSearchMode = false
    private var vocaRecyclerViewAdapter: VocaRecyclerViewAdapter? = null

    private val handler: Handler = Handler()
    private lateinit var updateWordSizeRunnable: Runnable

    private val loadAdapterDelay = 10
    private var isFragmentShown = false

    private val seeAllLayout
        get() = binding.layoutSeeAll

    private val deleteLayout
        get() = binding.layoutDelete

    private val deleteVocabularyButton
        get() = binding.buttonDeleteVocabulary

    private val deleteCancelButton
        get() = binding.addButtonCancel

    private val vocaNumberText
        get() = binding.textVocaNumber

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
                              container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModelProvider = ViewModelProvider(this)
        seeAllViewModel = viewModelProvider.get(SeeAllViewModel::class.java)
        vocaViewModel = viewModelProvider.get(VocaViewModel::class.java)
        isFragmentShown = true

        _binding = FragmentSeeAllBinding.inflate(inflater, container, false)

        toolbar = parentActivity.findViewById(R.id.toolbar)
        drawer = parentActivity.findViewById(R.id.drawer_layout)
        setHasOptionsMenu(true)

        // For delete method 1
        deleteVocabularyButton.setOnClickListener(View.OnClickListener {
            val selectedItems = vocaRecyclerViewAdapter?.getSelectedItems()
            val builder = AlertDialog.Builder(parentActivity)
            builder.setTitle("삭제")
            if (selectedItems != null) {
                builder.setMessage("${selectedItems.size}개의 단어를 삭제합니다.")
            }
            builder.setIcon(android.R.drawable.ic_dialog_alert)
            builder.setPositiveButton("확인") { _, _ -> vocaRecyclerViewAdapter?.deleteVocabulary() }
            builder.setNegativeButton("취소", DialogInterface.OnClickListener { _, _ -> return@OnClickListener })
            val dialog = builder.create()
            dialog.show()
        })
        deleteCancelButton.setOnClickListener { vocaRecyclerViewAdapter?.disableDeleteMode() }
        updateWordSizeRunnable = Runnable {
            if (isFragmentShown) {
                val vocaSize = vocaViewModel.getVocabularyCount()
                vocaSize.observe(viewLifecycleOwner, { integer -> vocaNumberText.text = integer?.toString() })
            }
        }
        showVocaSize()

        showSpinner()

        // Loading vocabulary from the database is costly, so execute it asynchronously
        val task = LoadAdapterTask()
        handler.postDelayed({ task.execute() }, loadAdapterDelay.toLong())
        vocaRecyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        vocaRecyclerView.addItemDecoration(DividerItemDecoration(vocaRecyclerView.context, LinearLayoutManager(parentActivity).orientation))
        val callback: ItemTouchHelper.SimpleCallback = VocabularyTouchHelper(0, ItemTouchHelper.LEFT, this)
        ItemTouchHelper(callback).attachToRecyclerView(vocaRecyclerView)

        return binding.root
    }

    /**
     * Update the number of the vocabulary in the database.
     * Why postDelayed(): Considered the delay the adapter is shown
     */
    private fun showVocaSize() {
        handler.postDelayed(updateWordSizeRunnable, 50)
    }

    override fun onResume() {
        isFragmentShown = true
        super.onResume()
    }

    override fun onPause() {
        isFragmentShown = false
        super.onPause()
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
                animateSearchToolbar(1, true, true)
                vocaRecyclerViewAdapter?.enableSearchMode()
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                // Called when SearchView is collapsing
                if (searchMenuItem.isActionViewExpanded) {
                    animateSearchToolbar(1, false, false)
                    vocaRecyclerViewAdapter?.disableSearchMode()
                    vocaNumberText.text = vocaRecyclerViewAdapter?.itemCount?.toString()
                }
                return true
            }
        })

        searchView = searchMenuItem.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchVocabulary(query)
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
    private fun searchVocabulary(query: String?) {
        vocaRecyclerViewAdapter?.searchVocabulary(query)
        vocaRecyclerViewAdapter?.getCurrentVocabulary()?.observe(viewLifecycleOwner, { vocabularies ->
            if (isSearchMode) {
                if (vocabularies != null) {
                    vocaNumberText.text = vocabularies.size.toString()
                }
            }
        })
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
                val translateAnimation: Animation = TranslateAnimation(0.0f, 0.0f, 0.0f, -toolbar.getHeight() as Float)
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
        view.setOnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && vocaRecyclerViewAdapter?.isDeleteMode() == true &&
                    !drawer.isDrawerOpen(GravityCompat.START)) {
                vocaRecyclerViewAdapter?.disableDeleteMode()
                true
            } else {
                false
            }
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
            val deletedVocabulary = vocaRecyclerViewAdapter?.getItem(position) as Vocabulary
            val eng = deletedVocabulary.eng
            Log.d("HSK APP", "pos: $position")
            vocaRecyclerViewAdapter!!.removeItem(position)
            vocaNumberText.text = vocaRecyclerViewAdapter!!.itemCount.toString()
            val snackBar = Snackbar.make(seeAllLayout, eng + "이(가) 삭제되었습니다.", Snackbar.LENGTH_LONG)
            snackBar.setAction("실행 취소") {
                vocaRecyclerViewAdapter!!.restoreItem(deletedVocabulary, position)
                vocaNumberText.text = vocaRecyclerViewAdapter!!.itemCount.toString()
            }
            snackBar.setActionTextColor(Color.YELLOW)
            snackBar.show()
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
     * Call EditVocaActivity to edit the vocabulary.
     * @param position position of the item in the adapter.
     * @param vocabulary actual vocabulary object at the position.
     */
    override fun editVocabulary(position: Int, vocabulary: Vocabulary?) {
        val intent = Intent(parentActivity.applicationContext, EditVocaActivity::class.java)
        intent.putExtra(Constants.POSITION, position)
        intent.putExtra(Constants.EDIT_VOCA, vocabulary)
        startActivityForResult(intent, Constants.CALL_EDIT_VOCA_ACTIVITY)
    }

    /**
     * Show a vocabulary at the notification.
     * @param vocabulary vocabulary to show at the notification
     */
    override fun showVocabularyOnNotification(vocabulary: Vocabulary?) {
        val intent = Intent(context, ShowNotificationService::class.java)
        intent.putExtra(ShowNotificationService.Companion.SHOW_VOCA, vocabulary)
        parentActivity.startService(intent)
        if (vocabulary != null) {
            Snackbar.make(seeAllLayout, "알림에 보임: ${vocabulary.eng}", Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d("HSK APP", "requestCode = $requestCode, resultCode = $resultCode")
        if (requestCode == Constants.CALL_EDIT_VOCA_ACTIVITY && resultCode == Constants.EDIT_NEW_VOCA_OK) {
            vocaRecyclerViewAdapter?.notifyItemsChanged()
        } else if (requestCode == Constants.CALL_ADD_VOCA_ACTIVITY && resultCode == Constants.ADD_NEW_VOCA_OK) {
            return
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    /**
     * Show adapter when LoadAdapterTask is complete
     */
    private fun setAdapter() {
        vocaRecyclerViewAdapter?.apply {
            setOnEditVocabularyListener(this@SeeAllFragment)
            setOnDeleteModeListener(this@SeeAllFragment)
            setShowVocaOnNotificationListener(this@SeeAllFragment)
            setVocaClickListener(object : OnVocaClickListener {
                override fun onVocaClick(holder: VocaViewHolder?, view: View?, position: Int) {
                    Log.d("HSK APP", "$position clicked.")
                    if (isDeleteMode()) {
                        switchSelectedState(position)
                    } else {
                        // do nothing
                    }
                }

                override fun onVocaLongClick(holder: VocaViewHolder?, view: View?, position: Int): Boolean {
                    return false
                }
            })
            getCurrentVocabulary()?.observeForever {
                Log.d("HSK APP", "setAdapter() -> onChanged()")
                showVocaSize()
                observe()
            }
            vocaRecyclerView.adapter = this
        }
    }

    /**
     * AsyncTask which shows all vocabulary in the database.
     * Operate asynchronously to prevent the main thread from blocking for a long time.
     */
    private inner class LoadAdapterTask : AsyncTask<Void?, Void?, Void?>() {
        override fun doInBackground(vararg voids: Void?): Void? {
            // TODO: 로딩화면 표시?
            vocaRecyclerViewAdapter = VocaRecyclerViewAdapter.getInstance(parentActivity)
            return null
        }

        override fun onPostExecute(aVoid: Void?) {
            super.onPostExecute(aVoid)
            setAdapter()
        }
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