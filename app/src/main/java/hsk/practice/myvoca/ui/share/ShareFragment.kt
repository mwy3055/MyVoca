package hsk.practice.myvoca.ui.share

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import hsk.practice.myvoca.databinding.FragmentShareBinding

/**
 * Created by Android Studio. Left for further use.
 */
class ShareFragment : Fragment() {
    private var _binding: FragmentShareBinding? = null

    private val binding
        get() = _binding!!


    private lateinit var viewModelProvider: ViewModelProvider
    private lateinit var shareViewModel: ShareViewModel
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentShareBinding.inflate(inflater, container, false)
        viewModelProvider = ViewModelProvider(this)
        shareViewModel = viewModelProvider.get(ShareViewModel::class.java)

        val textView = binding.textShare
        shareViewModel.getText()?.observe(viewLifecycleOwner, { s -> textView.text = s })
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}