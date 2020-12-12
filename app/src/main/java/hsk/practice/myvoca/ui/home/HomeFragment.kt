package hsk.practice.myvoca.ui.home

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import database.Vocabulary
import hsk.practice.myvoca.R
import hsk.practice.myvoca.VocaViewModel
import java.util.*

/**
 * First-shown fragment
 * Shows a random word. Press the button to change the word.
 * If there is no word in the database, notification text will be shown instead.
 */
class HomeFragment : Fragment() {
    private var viewModelProvider: ViewModelProvider? = null
    private var homeViewModel: HomeViewModel? = null
    private var vocaViewModel: VocaViewModel? = null
    private var allVocabulary: LiveData<MutableList<Vocabulary?>?>? = null
    private var showVocaWhenFragmentPause = true
    private val random: Random? = Random()
    private var vocaNumber: TextView? = null
    private var homeEng: TextView? = null
    private var homeKor: TextView? = null
    private var button: Button? = null
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.d("HSK APP", "HomeFragment onCreateView(), " + java.lang.Boolean.toString(showVocaWhenFragmentPause))
        viewModelProvider = ViewModelProvider(this)
        homeViewModel = viewModelProvider.get(HomeViewModel::class.java)
        vocaViewModel = viewModelProvider.get(VocaViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        vocaNumber = root.findViewById(R.id.home_voca_number)
        homeEng = root.findViewById(R.id.home_eng)
        homeKor = root.findViewById(R.id.home_kor)
        button = root.findViewById(R.id.home_load_new_vocabulary_button)
        allVocabulary = vocaViewModel.getAllVocabulary()
        allVocabulary.observe(viewLifecycleOwner, Observer { vocabularies ->
            if (vocabularies.size > 0) {
                showVocaNumber(vocabularies.size)
                tryShowRandomVocabulary()
            }
        })
        button.setOnClickListener(View.OnClickListener { v ->
            val isEmpty = vocaViewModel.isEmpty()
            isEmpty.observe(viewLifecycleOwner, { aBoolean ->
                if (aBoolean) {
                    Snackbar.make(v, "버튼을 눌러 단어를 추가해 주세요.", Snackbar.LENGTH_LONG).show()
                } else {
                    showRandomVocabulary()
                }
            })
        })
        return root
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    private fun tryShowRandomVocabulary() {
        val isEmpty = vocaViewModel.isEmpty()
        isEmpty.observeForever(object : Observer<Boolean?> {
            override fun onChanged(aBoolean: Boolean?) {
                if (showVocaWhenFragmentPause && !aBoolean) {
                    showRandomVocabulary()
                    showVocaWhenFragmentPause = false
                    button.setVisibility(View.VISIBLE)
                }
                isEmpty.removeObserver(this)
            }
        })
    }

    private fun showRandomVocabulary() {
        val randomVocabulary = vocaViewModel.getRandomVocabulary()
        randomVocabulary.observeForever(object : Observer<Vocabulary?> {
            override fun onChanged(vocabulary: Vocabulary?) {
                homeEng.setText(vocabulary.eng)
                homeKor.setText(vocabulary.kor)
                randomVocabulary.removeObserver(this)
            }
        })
    }

    private fun showVocaNumber(number: Int) {
        vocaNumber.setVisibility(View.VISIBLE)
        vocaNumber.setText(Integer.toString(number) + "단어")
    }
}