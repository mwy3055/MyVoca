package hsk.practice.myvoca.ui.goblog

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import hsk.practice.myvoca.R

/**
 * Just for fun
 * Maybe some features will be added here...
 */
class GoBlogFragment : Fragment() {
    private var goBlogViewModel: GoBlogViewModel? = null
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View? {
        goBlogViewModel = ViewModelProvider(this).get(GoBlogViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_go_blog, container, false)
        val textView = root.findViewById<TextView?>(R.id.text_send)
        goBlogViewModel.getText().observe(viewLifecycleOwner, { s -> textView.text = s })
        val url = "https://thinking-face.tistory.com/"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
        return root
    }
}