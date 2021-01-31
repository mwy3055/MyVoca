package hsk.practice.myvoca.ui.home

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import hsk.practice.myvoca.R
import hsk.practice.myvoca.databinding.FragmentHomeBinding
import hsk.practice.myvoca.framework.VocaPersistenceDatabase
import hsk.practice.myvoca.ui.NewVocaViewModel
import hsk.practice.myvoca.ui.NewVocaViewModelFactory

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

    private var viewModelProvider: ViewModelProvider? = null
    private var newVocaViewModel: NewVocaViewModel? = null

    //    private var allVocabulary: LiveData<MutableList<Vocabulary?>?>? = null
    private var showVocaWhenFragmentPause = true

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View {
        Log.d("HSK APP", "HomeFragment onCreateView(), " + java.lang.Boolean.toString(showVocaWhenFragmentPause))

        newVocaViewModel = ViewModelProvider(this, NewVocaViewModelFactory(VocaPersistenceDatabase(context))).get(NewVocaViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        newVocaViewModel!!.getAllVocabulary()?.observe(viewLifecycleOwner) { allVocabulary ->
            allVocabulary?.let {
                if (it.isNotEmpty()) {
                    showVocaNumber(it.size)
                    tryShowRandomVocabulary()
                } else {
                    showNoVocaText()
                }
            }
        }

        button.setOnClickListener { v ->
            newVocaViewModel?.isEmpty()?.observe(viewLifecycleOwner) {
                if (it) {
                    showRandomVocabulary()
                } else {
                    Snackbar.make(v, "버튼을 눌러 단어를 추가해 주세요.", Snackbar.LENGTH_LONG).show()
                }
            }
        }
        return binding.root
    }

    override fun onResume() {
        Log.d("HSK APP", "HomeFragment onResume()")
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

    private fun tryShowRandomVocabulary() {
        newVocaViewModel?.isEmpty()?.observe(viewLifecycleOwner) { isEmpty ->
            if (showVocaWhenFragmentPause && !isEmpty) {
                showRandomVocabulary()
                showVocaWhenFragmentPause = false
                button.visibility = View.VISIBLE
            }
        }
    }

    private fun showRandomVocabulary() {
        newVocaViewModel?.getRandomVocabulary()?.observe(viewLifecycleOwner) {
            binding.homeEng.text = it?.eng
            binding.homeKor.text = it?.kor
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
}