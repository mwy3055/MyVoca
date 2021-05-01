package hsk.practice.myvoca.ui.tools

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import hsk.practice.myvoca.databinding.FragmentToolsBinding

/**
 * Created by Android Studio. Left for further use.
 */
class ToolsFragment : Fragment() {
    private var _binding: FragmentToolsBinding? = null

    private val binding
        get() = _binding!!

    private val toolsViewModel: ToolsViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentToolsBinding.inflate(inflater, container, false)

        val textView = binding.textTools
        toolsViewModel.getText()?.observe(viewLifecycleOwner, { s -> textView.text = s })
        return binding.root
    }
}