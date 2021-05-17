package hsk.practice.myvoca.ui.home

import android.os.Bundle
import android.view.*
import androidx.core.view.isGone
import androidx.core.view.isVisible
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

        viewModel.vocabularyNotEmpty.observe(viewLifecycleOwner) {
            if (it) {
                showVocaLayout()
                viewModel.loadRandomVocabulary()
            } else {
                hideVocaLayout()
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

    private fun showVocaLayout() {
        with(binding) {
            homeLayout.isVisible = true
            noVocaLayout.isGone = true
        }
    }

    private fun hideVocaLayout() {
        with(binding) {
            homeLayout.isGone = true
            noVocaLayout.isVisible = true
        }
    }
}