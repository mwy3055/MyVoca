package hsk.practice.myvoca.ui.quiz

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import hsk.practice.myvoca.databinding.FragmentQuizBinding

/**
 * Shows word quiz to user.
 * There should be at least 5 words in the database to make the quiz shown.
 * When the word is not enough, notification text will be shown instead.
 * User should choose the correct meaning of the quiz word. Four options will be provided.
 * When user selects the option, quiz word and the VersusView at the bottom of the fragment will be updated.
 *
 *
 * Numbers of correct and wrong answers are stored in the DataStore and updated in real time.
 */
@AndroidEntryPoint
class QuizFragment : Fragment() {

    private var _binding: FragmentQuizBinding? = null
    private val binding
        get() = _binding!!

    private val quizViewModel: QuizViewModel by viewModels()

    private lateinit var quizAdapter: QuizAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuizBinding.inflate(inflater, container, false)

        viewLifecycleOwner.let {
            binding.lifecycleOwner = it
            binding.versusView.binding.lifecycleOwner = it
        }
        binding.viewModel = quizViewModel

        quizViewModel.versusViewModel = binding.versusView.viewModel
        quizViewModel.loadValues(requireContext())

        quizAdapter = QuizAdapter { position: Int ->
            quizViewModel.quizItemSelected(requireContext(), position)
        }
        binding.quizRecyclerView.apply {
            adapter = quizAdapter
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        }

        // Set padding between each items
        binding.root.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            with(binding) {
                root.post {
                    val height = quizRecyclerView[0].measuredHeight
                    val size = quizRecyclerView.measuredHeight
                    val padding = (size - height * 4) / 3
                    if (quizRecyclerView.itemDecorationCount > 0) {
                        (quizRecyclerView.getItemDecorationAt(0) as ItemDecoration).setSize(padding)
                    } else {
                        quizRecyclerView.addItemDecoration(ItemDecoration(padding))
                    }
                }
            }
        }

        quizViewModel.quizData.observe(viewLifecycleOwner) {
            if (it != null) {
                showQuiz(it)
            } else {
                hideQuiz()
            }
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showQuiz(quiz: Quiz) {
        with(binding) {
            quizRecyclerView.visibility = View.VISIBLE
            noVocaText.visibility = View.GONE
            quizAdapter.submitList(quiz.quizList)
        }
    }

    private fun hideQuiz() {
        with(binding) {
            quizRecyclerView.visibility = View.GONE
            noVocaText.visibility = View.VISIBLE
        }
    }

}