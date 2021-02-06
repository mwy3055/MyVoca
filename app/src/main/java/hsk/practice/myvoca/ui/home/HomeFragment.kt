package hsk.practice.myvoca.ui.home

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import hsk.practice.myvoca.AppHelper
import hsk.practice.myvoca.R
import hsk.practice.myvoca.databinding.FragmentHomeBinding
import hsk.practice.myvoca.framework.VocaPersistenceDatabase
import hsk.practice.myvoca.ui.NewVocaViewModel
import hsk.practice.myvoca.ui.NewVocaViewModelFactory
import kotlinx.coroutines.delay

/**
 * First-shown fragment
 * Shows a random word. Press the button to change the word.
 * If there is no word in the database, notification text will be shown instead.
 */
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    private val binding
        get() = _binding!!

    val button
        get() = binding.homeLoadNewVocabularyButton

    val vocaNumber
        get() = binding.homeVocaNumber

    private lateinit var viewModel: HomeViewModel

    private lateinit var newVocaViewModel: NewVocaViewModel

    //    private var allVocabulary: LiveData<MutableList<Vocabulary?>?>? = null
    private var showVocaWhenFragmentPause = true

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View {
        Log.d("HSK APP", "HomeFragment onCreateView(), $showVocaWhenFragmentPause")

        newVocaViewModel = ViewModelProvider(this, NewVocaViewModelFactory(VocaPersistenceDatabase.getInstance(requireContext()))).get(NewVocaViewModel::class.java)
        viewModel = ViewModelProvider(this, HomeViewModelFactory(VocaPersistenceDatabase.getInstance(requireContext()))).get(HomeViewModel::class.java)

        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        binding.homeViewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        newVocaViewModel.getVocabularyCount().observe(viewLifecycleOwner) { count ->
            Log.d(AppHelper.LOG_TAG, "get: $count")
            if (count > 0) {
                showVocaNumber(count)
                showVocaButton()
                viewModel.loadRandomVocabulary()
            } else {
                showNoVocaText()
                hideVocaButton()
            }
        }

        return binding.root
    }

    override fun onResume() {
        Log.d("HSK APP", "HomeFragment onResume()")
        refreshVocaNumber()
        super.onResume()
    }

    override fun onPause() {
        Log.d("HSK APP", "HomeFragment onPause()")
        super.onPause()
        showVocaWhenFragmentPause = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
    }

//    private fun tryShowRandomVocabulary() {
//        newVocaViewModel.isEmpty().observe(viewLifecycleOwner) { isEmpty ->
//            if (showVocaWhenFragmentPause && !isEmpty) {
//                showRandomVocabulary()
//                showVocaWhenFragmentPause = false
//                button.visibility = View.VISIBLE
//            }
//        }
//    }
//
//    private fun showRandomVocabulary() {
//        viewModel.loadRandomVocabulary()
////        newVocaViewModel.getRandomVocabulary().observe(viewLifecycleOwner) {
////            it?.let {
////                binding.homeEng.text = it.eng
////                binding.homeKor.text = it.kor
////            } ?: run {
////                Snackbar.make(button, "버튼을 눌러 단어를 추가해 주세요.", Snackbar.LENGTH_LONG).show()
////            }
////        }
//    }

    private fun refreshVocaNumber() = lifecycleScope.launchWhenResumed {
        delay(100)
        newVocaViewModel.getVocabularyCount().observe(this@HomeFragment) {
            showVocaNumber(it)
        }
    }

    private fun showVocaNumber(number: Int) {
        vocaNumber.visibility = View.VISIBLE
        vocaNumber.text = getString(R.string.home_fragment_word_count, number)
    }

    private fun hideVocaNumber() {
        vocaNumber.visibility = View.GONE
    }

    private fun showNoVocaText() {
        hideVocaNumber()
        vocaNumber.text = getString(R.string.home_fragment_home_kor)
    }

    private fun showVocaButton() {
        binding.homeLoadNewVocabularyButton.visibility = View.VISIBLE
    }

    private fun hideVocaButton() {
        binding.homeLoadNewVocabularyButton.visibility = View.GONE
    }
}