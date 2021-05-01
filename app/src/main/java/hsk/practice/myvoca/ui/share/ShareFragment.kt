package hsk.practice.myvoca.ui.share

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import hsk.practice.myvoca.databinding.FragmentShareBinding

/**
 * Created by Android Studio. Left for further use.
 */
@AndroidEntryPoint
class ShareFragment : Fragment() {
    private var _binding: FragmentShareBinding? = null

    private val binding
        get() = _binding!!

    private val shareViewModel: ShareViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentShareBinding.inflate(inflater, container, false)

        val textView = binding.textShare
        shareViewModel.getText()?.observe(viewLifecycleOwner, { s -> textView.text = s })
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}