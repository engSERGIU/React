package io.github.engsergiu.react

import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.content.getSystemService
import io.github.engsergiu.react.MainActivity.States.*
import android.media.AudioAttributes.Builder as AAB
import android.media.SoundPool.Builder as SPB

/**
 * @author Sergiu
 */
class MainActivity : AppCompatActivity() {
	enum class States { READY, SET, GO, TOO_FAST }
	companion object {
		const val MAX: Long = 4000 //the latest time when the screen may turn from set to go
		const val MIN: Long = 1000 //the earliest time when the screen may turn from set to go
		private const val SLOTH_RT: Long = 999999999 //the presumed reaction time of a sloth
		const val PREFS = "prefs"
		const val RECORD = "record"
		private const val MAX_STREAMS = 5   //declarations used for sound play
		private const val streamType = AudioManager.STREAM_MUSIC


		@Deprecated("UnknownUseCase")
		private val logFile by lazy { FileHandler("Android/data/io.github.engsergiu.react", "log.csv") }
	}


	private var state = READY //the current state of the buttom
	private var h: Handler = Handler() //used for scheduling a screen color change
	private val r: Runnable by lazy {
		Runnable {
			state = GO
			playShootSound()
			changeColor(color = Color.GREEN, textID = R.string.go)
			initialTime = SystemClock.uptimeMillis()
		}
	} //used for describing the color change

	//time zone
	private var initialTime: Long = 0 //used for storing the initial and the final times (when the screen changes and when the user taps)
	private var record = SLOTH_RT
	private var redDuration: Long = 0 //duration of the red screen
	private var audioManager: AudioManager? = null
	private var soundPool: SoundPool? = null
	private var volume = 0f
	private var loaded = false

	private val shootSound by lazy { // Load sound file (shoot.wav) into SoundPool.
		soundPool!!.load(this, R.raw.shoot, 1)
	}

	@Throws(IllegalArgumentException::class)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
		setSupportActionBar(findViewById(R.id.toolbar))

		// read record from shared preference
		val storedRecord = getSharedPreferences(PREFS, 0).getLong(RECORD, SLOTH_RT)
		if (storedRecord != SLOTH_RT) {
			record = storedRecord
			title = getString(R.string.bar_best).format(record)
		}
		findViewById<Button>(R.id.button).apply {
			changeColor(this, getResColor(R.color.colorPrimary), R.string.ready) //initialize the color
			setOnClickListener { tap(it) }
		}
		loadSound()
	}

	public override fun onStop() {
		super.onStop()
		getSharedPreferences(PREFS, 0).edit {
			putLong(RECORD, record)
		}
	}

	/**
	 * Load sounds
	 */
	@Throws(IllegalArgumentException::class)
	private fun loadSound() {
		// AudioManager audio settings for adjusting the volume
		audioManager = getSystemService()
		val currentVolumeIndex = audioManager!!.getStreamVolume(streamType).toFloat() // Current volumn Index of particular stream type.
		val maxVolumeIndex = audioManager!!.getStreamMaxVolume(streamType).toFloat() // Get the maximum volume index for a particular stream type
		volume = currentVolumeIndex / maxVolumeIndex // Volume (0 --> 1)

		// Suggests an audio stream whose volume should be changed by
		// the hardware volume controls.
		this.volumeControlStream = streamType

		soundPool = SPB().apply {
			setAudioAttributes(
					AAB().apply {
						setUsage(AudioAttributes.USAGE_GAME)
						setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
					}.build()
			).setMaxStreams(MAX_STREAMS)
		}.build()

		// When Sound Pool load complete.
		soundPool?.setOnLoadCompleteListener { _, _, _ -> loaded = true }
		shootSound.let { }
	}

	/**
	 * Play sounds
	 */
	private fun playShootSound() {
		if (loaded) {
			soundPool!!.play(
					shootSound,
					volume,
					volume,
					1,
					0,
					1f
			) // Play sound of gunfire. Returns the ID of the new stream.
		}
	}

	override fun onConfigurationChanged(newConfig: Configuration) {
		super.onConfigurationChanged(newConfig)
		toastID {
			if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT)
				R.string.portrait
			else R.string.landscape
		}
	}

	override fun onCreateOptionsMenu(menu: Menu): Boolean {
		// Inflate the menu; this adds items to the action bar if it is present.
		menuInflater.inflate(R.menu.menu_main, menu)
		return true
	}


	/**
	Handle action bar item clicks here. The action bar will
	automatically handle clicks on the Home/Up button, so long
	as you specify a parent activity in AndroidManifest.xml.
	 */
	override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
		R.id.action_about -> {
			startActivity(Intent(this, AboutActivity::class.java))
			true
		}
		R.id.action_resetscore -> {
			record = SLOTH_RT
			title = getString(R.string.react)
			true
		}
		else -> super.onOptionsItemSelected(item)
	}

	/**
	 * Define what to do when the user taps the button
	 *
	 * @param view the tapped view (in this case a button)
	 */
	private fun tap(view: View) {
		val button = view as Button // the button is R.id.button
		when (state) {
			READY -> {
				state = SET
				changeColor(button, Color.RED, (R.string.set))
				h.postDelayed(
						r,
						MIN + (Math.random() * (MAX - MIN)).toLong().also {
							redDuration = it
						}
				)
			}
			SET -> {
				h.removeCallbacks(r)
				state = TOO_FAST
				changeColor(button, Color.parseColor("#0066ff"), R.string.too_fast)
			}
			GO -> {
				state = READY
				val reactionTime = SystemClock.uptimeMillis() - initialTime
				//logFile.write(String.format("\"%s\",\"%d\",\"%d\"\n", tellMeTheTime(), reactionTime, redDuration))
				if (reactionTime < record) {
					record = reactionTime
					try {
						title = getString(R.string.bar_best).format(reactionTime.toInt())
					} catch (e: Resources.NotFoundException) {
						Log.e(logID(), "", e)
					}
				}
				try {
					changeColor(
							button,
							getResColor(R.color.colorPrimary),
							stringText = getString(
									R.string.ready_timed
							).format(reactionTime.toInt())
					)
				} catch (e: Resources.NotFoundException) {
					Log.e(logID(), "", e)
				}
			}
			TOO_FAST -> {
				state = READY
				//logFile.write(String.format("\"%s\",\"%d\",\"%d\"\n", tellMeTheTime(), -1, redDuration))
				changeColor(button, getResColor(R.color.colorPrimary), R.string.ready)
			}
		}
	}

	/**
	 * Change the color of the statusbar, actionbar, button and navigation bar
	 *
	 * @param button the button whose color is changed, Default from view
	 * @param color  the new color
	 * @param textID   the new text of the button
	 */
	private fun changeColor(
			button: Button = findViewById<View>(R.id.button) as Button,
			color: Int,
			@StringRes textID: Int = -1,
			stringText: String = getString(textID)
	) {
		this.window.apply {
			statusBarColor = color //change the status bar color
			navigationBarColor = color //change the navigation bar color
		}
		button.apply {
			setBackgroundColor(color) //change the button color
			text = stringText //change button text
		}
		supportActionBar?.setBackgroundDrawable(ColorDrawable(color)) //change the actionbar color
	}

	/*
	/**
	 * @return the current date and time
	 */
	@SuppressLint("SimpleDateFormat")
	private fun tellMeTheTime(): String = SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Date())
	 */
}