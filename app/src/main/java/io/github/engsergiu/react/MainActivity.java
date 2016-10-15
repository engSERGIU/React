package io.github.engSERGIU.react;


import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    final long MAX = 4_000; //the latest time when the screen may turn from set to go
    final long MIN = 1_000; //the earliest time when the screen may turn from set to go

    private String state;   //the current state of the buttom
    Handler h;  //used for scheduling a screen color change
    Runnable r; //used for describing the color change
    long initialTime;  //used for storing the initial and the final times (when the screen changes and when the user taps)

    //declarations used for sound play
    private static final int MAX_STREAMS = 5;
    private AudioManager audioManager;
    private SoundPool soundPool;
    private float volume;
    private static final int streamType = AudioManager.STREAM_MUSIC;
    private boolean loaded;
    private int shootSound;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        this.state = "ready";   //initialize the state
        Button button = (Button) findViewById(R.id.button);

        changeColor(button, Color.parseColor("#ffcc00"), "Ready"); //initialize the color
        this.h = new Handler();
        this.r = new Runnable() {
            public void run() {
                state = "go";
                playShootSound();
                changeColor(Color.GREEN, "GO!");
                initialTime = SystemClock.uptimeMillis();
            }
        };

        button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    tap(v);
                    return true;
                }
                return false;
            }
        });

        loadSound();
    }


    /**
     * Load sounds
     */
    private void loadSound() {
        // AudioManager audio settings for adjusting the volume
        this.audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        float currentVolumeIndex = (float) this.audioManager.getStreamVolume(streamType); // Current volumn Index of particular stream type.
        float maxVolumeIndex = (float) this.audioManager.getStreamMaxVolume(streamType); // Get the maximum volume index for a particular stream type
        this.volume = currentVolumeIndex / maxVolumeIndex;    // Volume (0 --> 1)

        // Suggests an audio stream whose volume should be changed by
        // the hardware volume controls.
        this.setVolumeControlStream(streamType);

        AudioAttributes audioAttrib = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        SoundPool.Builder builder = new SoundPool.Builder();
        builder.setAudioAttributes(audioAttrib).setMaxStreams(MAX_STREAMS);

        this.soundPool = builder.build();

        // When Sound Pool load complete.
        this.soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                loaded = true;
            }
        });

        // Load sound file (shoot.wav) into SoundPool.
        this.shootSound = this.soundPool.load(this, R.raw.shoot, 1);
    }


    /**
     * Play sounds
     */
    public void playShootSound()  {
        if(loaded)  {
            float leftVolume = volume;
            float rightVolume = volume;
            int streamId = this.soundPool.play(this.shootSound, leftVolume, rightVolume, 1, 0, 1f);  // Play sound of gunfire. Returns the ID of the new stream.
        }
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT)
            Toast.makeText(MainActivity.this, "- portrait -", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(MainActivity.this, "- landscape -", Toast.LENGTH_SHORT).show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            //case R.id.action_settings:
            //    return true;
            case R.id.action_about:
                Intent intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * Define what to do when the user taps the button
     *
     * @param view the tapped view (in this case a button)
     */
    public void tap(View view) {
        Button button = (Button) view;  // the button is R.id.button

        switch (this.state) {
            case "ready":
                this.state = "set";
                changeColor(button, Color.RED, "Set");
                this.h.postDelayed(this.r, this.MIN + (long) (Math.random() * (this.MAX - this.MIN)));
                break;
            case "set":
                this.h.removeCallbacks(this.r);
                this.state = "tooFast";
                changeColor(button, Color.parseColor("#0066ff"), "Too fast !");
                break;
            case "go":
                this.state = "ready";
                String message = String.format("Ready\n%d ms", SystemClock.uptimeMillis() - initialTime);
                changeColor(button, Color.parseColor("#ffcc00"), message);
                break;
            case "tooFast":
                state = "ready";
                changeColor(button, Color.parseColor("#ffcc00"), "Ready");
                break;
            default:
                break;
        }
    }


    /**
     * Change the color of the statusbar, actionbar, button and navigation bar
     *
     * @param button the button whose color is changed
     * @param color  the new color
     * @param text   the new text of the button
     */
    public void changeColor(Button button, int color, String text) {
        this.getWindow().setStatusBarColor(color);   //change the status bar color
        this.getSupportActionBar().setBackgroundDrawable(new ColorDrawable(color));   //change the actionbar color
        button.setBackgroundColor(color);    //change the button color
        this.getWindow().setNavigationBarColor(color);   //change the navigation bar color

        button.setText(text);    //change button text
    }


    /**
     * Change the color of the statusbar, actionbar, button and navigation bar
     *
     * @param color the new color
     * @param text  the new text of R.id.button button
     */
    public void changeColor(int color, String text) {
        Button button = (Button) findViewById(R.id.button);

        this.getWindow().setStatusBarColor(color);   //change the status bar color
        this.getSupportActionBar().setBackgroundDrawable(new ColorDrawable(color));   //change the actionbar color
        button.setBackgroundColor(color);    //change the button color
        this.getWindow().setNavigationBarColor(color);   //change the navigation bar color

        button.setText(text);    //change button text
    }
}
