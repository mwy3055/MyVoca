package hsk.practice.myvoca.ui.share

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
class ShareFragment : Fragment() {
    private var viewModelProvider: ViewModelProvider? = null
    private var shareViewModel: ShareViewModel? = null
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModelProvider = ViewModelProvider(this)
        shareViewModel = viewModelProvider.get(ShareViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_share, container, false)
        val textView = root.findViewById<TextView?>(R.id.text_share)
        shareViewModel.getText().observe(viewLifecycleOwner, { s -> textView.text = s })
        return root
    }
}