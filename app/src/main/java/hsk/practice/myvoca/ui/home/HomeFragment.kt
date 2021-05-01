package hsk.practice.myvoca.ui.home

import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import hsk.practice.myvoca.R
import hsk.practice.myvoca.databinding.FragmentHomeBinding

/**
 * First-shown fragment
 * Shows a random word. Press the button to change the word.
 * If there is no word in the database, notification text will be shown instead.
 */
@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    private val binding
        get() = _binding!!

    val button
        get() = binding.homeLoadNewVocabularyButton

    private val vocaNumber
        get() = binding.homeVocaNumber

    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View {

        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        binding.homeViewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel.vocabularySize.observe(viewLifecycleOwner) { count ->
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
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
        vocaNumber.visibility = View.VISIBLE
        vocaNumber.text = getString(R.string.home_fragment_home_kor)
    }

    private fun showVocaButton() {
        binding.homeLoadNewVocabularyButton.visibility = View.VISIBLE
    }

    private fun hideVocaButton() {
        binding.homeLoadNewVocabularyButton.visibility = View.GONE
    }
}