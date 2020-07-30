package io.github.engsergiu.react

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton

/**
 * "About" activity page.
 * @author Sergiu
 */
class AboutActivity : AppCompatActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_about)
		setSupportActionBar(findViewById(R.id.toolbar))

		findViewById<FloatingActionButton>(R.id.fab).apply {
			setOnClickListener { mailto() }
		}
		supportActionBar!!.setDisplayHomeAsUpEnabled(true)
		this.window.statusBarColor = Color.parseColor("#BA1AEE")
	}

	private fun mailto() = startActivity(Intent(
			Intent.ACTION_SENDTO,
			Uri.fromParts("mailto", "44aaff@gmail.com", null)
	))
}