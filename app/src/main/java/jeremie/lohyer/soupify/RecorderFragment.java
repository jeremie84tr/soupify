package jeremie.lohyer.soupify;

import static jeremie.lohyer.soupify.MainActivity.client;
import static jeremie.lohyer.soupify.MainActivity.setupClient;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.os.HandlerCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.navigation.fragment.NavHostFragment;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;

import jeremie.lohyer.APICommunicator;
import jeremie.lohyer.ImplTextSender;
import jeremie.lohyer.WhisperClient;
import jeremie.lohyer.soupify.actionParseur.ActionParseur;
import jeremie.lohyer.soupify.databinding.FragmentRecorderBinding;

public class RecorderFragment extends Fragment {

    private FragmentRecorderBinding binding;
    private MediaRecorder mediaRecorder;
    private WhisperClient whisperClient;
    private RecorderStatus recorderStatus;
    private Handler responseHandler;
    private APICommunicator communicator;

    private MutableLiveData<String> texteAffiche;
    private ActionParseur parseur;


    private enum RecorderStatus {
        RECORDING,
        STOPPED
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentRecorderBinding.inflate(inflater, container, false);
        whisperClient = new WhisperClient();
        texteAffiche = new MutableLiveData<>();
        responseHandler = HandlerCompat.createAsync(Looper.getMainLooper());

        setupClient(requireContext());

        communicator = new APICommunicator("http://" + SettingsFragment.getNlpIpAddress() + ":" + SettingsFragment.getNlpPort() + "/v1/chat/completions",
                "you will do json formating on the next data coming, as following :  {\"action\": [the action],\"song\": [the name of the song]} : \n" +
                " - first, you will give the info if it's an action of 'playing', 'pausing' or 'stopping' a song.\n" +
                " - then if it's an action of 'playing', you will tell what song is it telling to play, or you say 'null'. with the key word 'song'.\n" +
                "Here you go : ","0.1"
        );
        parseur = new ActionParseur();

        parseur.ajouteAction("playing", value -> {
            client.select(value);
            client.play();
            return null;
        });
        parseur.ajouteAction("pausing", value -> {
            client.pause();
            return null;
        });
        parseur.ajouteAction("stopping", value -> {
            client.stop();
            return null;
        });

        texteAffiche.observe(getViewLifecycleOwner(), (newData) -> {
            binding.transcribedText.setText(newData);
        });

        texteAffiche.postValue(getContext().getString(R.string.info_transcribed_text));
        configureCallBacks(whisperClient);


        return binding.getRoot();
    }

    private void configureCallBacks(WhisperClient client) {
        ImplTextSender textSender = new ImplTextSender();

        textSender.setGetTranscriptionCallBack((transcription) -> {
            texteAffiche.postValue(transcription);
            processTranscription(transcription);
        });

        textSender.setGetCompletionCallBack(val -> {
            double percentage = (val / (double) client.getNbBlocs()) * 100;
            Log.d("TAGueule", "configureCallBacks: upload : " + String.format("%.1f", percentage) + "%");
        });

        client.initClient(SettingsFragment.getWhisperIpAddress(), textSender);
    }

    public void processTranscription(String transcription) {
        communicator.setPrompt(transcription).call(false,Executors.newSingleThreadExecutor(), (result) -> {
            responseHandler.post(() -> {
                Toast.makeText(getActivity().getApplicationContext(), result, Toast.LENGTH_SHORT).show();
                processAction(result);
            });
            return null;
        });
    }

    private void processAction(String json) {
        System.out.println(json);

        //selection action juste
        parseur.parseTexte(json);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.button.setOnClickListener(view2 -> NavHostFragment.findNavController(RecorderFragment.this)
                .navigate(R.id.action_RecorderFragment_to_playerFragment));

        binding.fab.setOnClickListener((view1) -> {
            if (recorderStatus == RecorderStatus.RECORDING) {
                recorderStatus = RecorderStatus.STOPPED;
                binding.fab.setImageResource(R.drawable.record_standby);
                mediaRecorder.stop();
                mediaRecorder.release();
                whisperClient.upload(getContext().getExternalFilesDir("").getAbsolutePath() + "/record.mp3");
            } else {
                record();
            }
        });

        if ( requestRecord() && requestInternet() ) {
            record();
        }
    }

    private void record() {
        File file = new File(getContext().getExternalFilesDir("").getAbsolutePath() + "/record.mp3");

        try {
            file.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String savePath = file.getAbsolutePath();


        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setOutputFile(savePath);
        try {
            recorderStatus = RecorderStatus.RECORDING;
            binding.fab.setImageResource(R.drawable.record_recording);
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (IOException e) {
            Log.d("TAGueule", "onViewCreated: recording failed : " + e);
        }
    }

    private boolean requestRecord() {
        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                Toast.makeText(getContext(), getResources().getString(R.string.recording_not_available), Toast.LENGTH_SHORT).show();
                Log.d("TAGueule", "requestRecord: context : " + getContext() + " audioPermission : " + (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) + " write external storage : " + (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED));
                return false;
            }
        }
    }

    private boolean requestInternet() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.INTERNET, android.Manifest.permission.ACCESS_NETWORK_STATE}, 1);
            if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                Toast.makeText(getContext(), getResources().getString(R.string.internet_not_available), Toast.LENGTH_SHORT).show();
                Log.d("TAGueule", "requestRecord: context : " + getContext() + " internet : " + (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED) + " network state : " + (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED));
                return false;
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}