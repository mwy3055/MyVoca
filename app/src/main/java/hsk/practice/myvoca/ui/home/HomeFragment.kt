package hsk.practice.myvoca.ui.home

import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
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

    private val binding: FragmentHomeBinding
        get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        binding.homeViewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        loadFirstVocabulary()

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

    /**
     * Observe only once before XML observer is set.
     * After the vocabulary is loaded first, button click will show the random vocabulary.
     */
    private fun loadFirstVocabulary() {
        viewModel.vocabularyNotEmpty.observe(viewLifecycleOwner, object : Observer<Boolean> {
            override fun onChanged(value: Boolean?) {
                if (value == true) {
                    viewModel.loadRandomVocabulary()
                }
                viewModel.vocabularyNotEmpty.removeObserver(this)
            }
        })
    }
}