package jeremie.lohyer.soupify;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import org.example.Client;
import org.example.ImplMusiqueSender;
import org.example.VLCAdapter;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.MediaPlayer;

import jeremie.lohyer.soupify.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    public static int RQ_SPEECH_REC = 102;

    public static Client client;
    private static MediaPlayer mediaPlayer = null;

    public static MutableLiveData<String> allSong = new MutableLiveData<>();
    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    public MainActivity() {
    }

    public static void setupClient(Context context) {
        client = new Client(new VLCAdapter() {

            private LibVLC mLibVlc = new LibVLC(context.getApplicationContext());
            private MediaPlayer mediaPlayer = new org.videolan.libvlc.MediaPlayer(mLibVlc);

            String option = "";

            public void listen(String ip) {
                option = "rtsp://" + ip + ":32470/";
                Log.d("TAGueule", "play: " + option);
            }

            public void play() {
                mediaPlayer.play(Uri.parse(option));
            }

            public void pause() {
                if (mediaPlayer != null) {
                    mediaPlayer.pause();
                }
            }

            public void stop() {
                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                }
            }
        });

        ImplMusiqueSender musiqueSender = new ImplMusiqueSender();

        musiqueSender.setGetSongsCallBack(s -> allSong.postValue(s));

        musiqueSender.setGetCompletionCallBack(val -> {
            double percentage = (val / (double) client.getNbBlocs()) * 100;
            Log.d("TAGueule", "configureCallBacks: upload : " + String.format("%.1f", percentage) + "%");
        });

        try {
            client.initClient(SettingsFragment.getSoupIpAddress(), musiqueSender);
        } catch (Exception e) {
            Toast.makeText(context.getApplicationContext(), "oh non, problème de connection ! : " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        Log.d("TAGueule", "onCreate: configured client");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        initSettings();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

    }

    private void initSettings() {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);

        SettingsFragment.setParams(
            sharedPref.getString(getString(R.string.settings_name_whisper),"127.0.0.1"),
            sharedPref.getString(getString(R.string.settings_name_nlp) + " ip","127.0.0.1"),
            sharedPref.getString(getString(R.string.settings_name_nlp) + " port","5000"),
            sharedPref.getString(getString(R.string.settings_name_soup),"127.0.0.1")
        );
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
            navController.navigateUp();
            navController.navigate(R.id.settingsFragment);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}