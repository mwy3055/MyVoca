package hsk.practice.myvoca.ui.tools

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import hsk.practice.myvoca.R

/**
 * Created by Android Studio. Left for further use.
 */
class ToolsFragment : Fragment() {
    private var viewModelProvider: ViewModelProvider? = null
    private var toolsViewModel: ToolsViewModel? = null
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModelProvider = ViewModelProvider(this)
        toolsViewModel = viewModelProvider.get(ToolsViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_tools, container, false)
        val textView = root.findViewById<TextView?>(R.id.text_tools)
        toolsViewModel.getText().observe(viewLifecycleOwner, { s -> textView.text = s })
        return root
    }
}