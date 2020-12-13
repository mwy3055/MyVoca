package hsk.practice.myvoca.ui.home

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import database.Vocabulary
import hsk.practice.myvoca.VocaViewModel
import hsk.practice.myvoca.databinding.FragmentHomeBinding

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
    private var homeViewModel: HomeViewModel? = null
    private var vocaViewModel: VocaViewModel? = null
    private var allVocabulary: LiveData<MutableList<Vocabulary?>?>? = null
    private var showVocaWhenFragmentPause = true

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View {
        Log.d("HSK APP", "HomeFragment onCreateView(), " + java.lang.Boolean.toString(showVocaWhenFragmentPause))
        viewModelProvider = ViewModelProvider(this)
        homeViewModel = viewModelProvider!!.get(HomeViewModel::class.java)
        vocaViewModel = viewModelProvider!!.get(VocaViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        allVocabulary = vocaViewModel!!.getAllVocabulary()
        allVocabulary?.observe(viewLifecycleOwner, Observer { vocabularies ->
            if (vocabularies != null) {
                if (vocabularies.size > 0) {
                    showVocaNumber(vocabularies.size)
                    tryShowRandomVocabulary()
                }
            }
        })
        button.setOnClickListener { v ->
            val isEmpty = vocaViewModel!!.isEmpty()
            isEmpty?.observe(viewLifecycleOwner, { aBoolean ->
                if (aBoolean == true) {
                    Snackbar.make(v, "버튼을 눌러 단어를 추가해 주세요.", Snackbar.LENGTH_LONG).show()
                } else {
                    showRandomVocabulary()
                }
            })
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
        val isEmpty = vocaViewModel?.isEmpty()
        isEmpty?.observeForever(object : Observer<Boolean?> {
            override fun onChanged(aBoolean: Boolean?) {
                if (showVocaWhenFragmentPause && aBoolean == false) {
                    showRandomVocabulary()
                    showVocaWhenFragmentPause = false
                    button.visibility = View.VISIBLE
                }
                isEmpty.removeObserver(this)
            }
        })
    }

    private fun showRandomVocabulary() {
        val randomVocabulary = vocaViewModel?.getRandomVocabulary()
        randomVocabulary?.observeForever(object : Observer<Vocabulary?> {
            override fun onChanged(vocabulary: Vocabulary?) {
                if (vocabulary != null) {
                    binding.homeEng.text = vocabulary.eng
                    binding.homeKor.text = vocabulary.kor
                }
                randomVocabulary.removeObserver(this)
            }
        })
    }

    private fun showVocaNumber(number: Int) {
        vocaNumber.visibility = View.VISIBLE
        vocaNumber.text = "${number}단어"
    }
}