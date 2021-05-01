package hsk.practice.myvoca.ui.gogithub

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import hsk.practice.myvoca.databinding.FragmentGoGithubBinding

/**
 * Just for fun.
 * Maybe some features can be added here...
 */
@AndroidEntryPoint
class GoGitHubFragment : Fragment() {

    private var _binding: FragmentGoGithubBinding? = null
    private val goGitHubViewModel: GoGitHubViewModel by viewModels()

    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentGoGithubBinding.inflate(inflater, container, false)
        binding.goGitHubViewModel = goGitHubViewModel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.buttonGoGithub.setOnClickListener {
            val url = "https://github.com/mwy3055/MyVoca/issues"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}