package hsk.practice.myvoca.ui.goblog

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import hsk.practice.myvoca.databinding.FragmentGoBlogBinding
import kotlinx.coroutines.delay

/**
 * Just for fun.
 * Maybe some features can be added here...
 */
class GoBlogFragment : Fragment() {

    private var _binding: FragmentGoBlogBinding? = null
    private lateinit var goBlogViewModel: GoBlogViewModel

    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View {
        goBlogViewModel = ViewModelProvider(this).get(GoBlogViewModel::class.java)

        _binding = FragmentGoBlogBinding.inflate(inflater, container, false)
        binding.goBlogViewModel = goBlogViewModel
        binding.lifecycleOwner = viewLifecycleOwner

        val url = "https://thinking-face.tistory.com/"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        lifecycleScope.launchWhenResumed {
            delay(500)
            startActivity(intent)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}