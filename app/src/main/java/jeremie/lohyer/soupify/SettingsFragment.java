package jeremie.lohyer.soupify;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import jeremie.lohyer.soupify.databinding.FragmentSettingsBinding;

public class SettingsFragment extends Fragment {

    private static String[] WHISPER_IP_ADDRESS = new String[]{"127.0.0.1"};
    private static String[] NLP_IP_ADDRESS = new String[]{"127.0.0.1"};
    private static String[] NLP_PORT = new String[]{"5000"};
    private static String[] SOUP_IP_ADDRESS = new String[]{"127.0.0.1"};

    public static void setParams(String whisperIp, String nlpIp, String nlpPort, String soupIp) {
        WHISPER_IP_ADDRESS[0] = whisperIp;
        NLP_IP_ADDRESS[0] = nlpIp;
        NLP_PORT[0] = nlpPort;
        SOUP_IP_ADDRESS[0] = soupIp;
    }

    private FragmentSettingsBinding binding;

    public static String getWhisperIpAddress() {
        return WHISPER_IP_ADDRESS[0];
    }

    public static String getNlpIpAddress() {
        return NLP_IP_ADDRESS[0];
    }

    public static String getNlpPort() {
        return NLP_PORT[0];
    }

    public static String getSoupIpAddress() {
        return SOUP_IP_ADDRESS[0];
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {


        configField(binding.soupAddress, SOUP_IP_ADDRESS);

        configField(binding.whisperAddress, WHISPER_IP_ADDRESS);

        configField(binding.nlpAddress, NLP_IP_ADDRESS);

        configField(binding.nlpPort, NLP_PORT);

        binding.buttonSaveSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString(getString(R.string.settings_name_soup), SOUP_IP_ADDRESS[0]);
                editor.putString(getString(R.string.settings_name_whisper), WHISPER_IP_ADDRESS[0]);
                editor.putString(getString(R.string.settings_name_nlp) + " ip", NLP_IP_ADDRESS[0]);
                editor.putString(getString(R.string.settings_name_nlp) + " port", NLP_PORT[0]);
                editor.apply();

                NavHostFragment.findNavController(SettingsFragment.this).navigateUp();
            }
        });


        super.onViewCreated(view, savedInstanceState);
    }

    private void configField(EditText editor, String[] destinationValue) {
        editor.setText(destinationValue[0]);

        editor.setOnFocusChangeListener((focusView, isFocusing) -> {
            if (!isFocusing) {
                destinationValue[0] = editor.getText().toString();
            }
        });
    }
}
