package hsk.practice.myvoca.ui.goblog

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import hsk.practice.myvoca.databinding.FragmentGoBlogBinding

/**
 * Just for fun
 * Maybe some features will be added here...
 */
class GoBlogFragment : Fragment() {

    private var _binding: FragmentGoBlogBinding? = null
    private var goBlogViewModel: GoBlogViewModel? = null

    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View? {
        goBlogViewModel = ViewModelProvider(this).get(GoBlogViewModel::class.java)

        _binding = FragmentGoBlogBinding.inflate(inflater, container, false)

        goBlogViewModel!!.getText()?.observe(viewLifecycleOwner, { s -> binding.textSend.text = s })
        val url = "https://thinking-face.tistory.com/"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}