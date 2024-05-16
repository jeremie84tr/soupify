package jeremie.lohyer.soupify;

import static jeremie.lohyer.soupify.MainActivity.client;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;

import jeremie.lohyer.soupify.databinding.FragmentPlayerBinding;


public class PlayerFragment extends Fragment {

    private FragmentPlayerBinding binding;


    private MutableLiveData<String> allSongsText;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentPlayerBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }


    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            binding.selectSong.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    client.select(binding.editSongName.getText().toString());
                }
            });

            binding.editTextSongName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    client.select((String) binding.editTextSongName.getAdapter().getItem(i));
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                    client.select("");
                }
            });

            binding.pauseButton.setOnClickListener(view1 -> client.pause());

            binding.playButton.setOnClickListener(view12 -> client.play());

            binding.getSongsButton.setOnClickListener(view13 -> client.getSongs());

            MainActivity.allSong.observe(getViewLifecycleOwner(), s -> {
                binding.allSongsText.setText(s);
                binding.editTextSongName.setAdapter(new ArrayAdapter<>(getContext(), R.layout.spinner_content, s.split("\n")));
            });

            client.getSongs();
        } catch (Exception e) {

        }
    }

    @Override
    public void onDestroyView() {
        new Thread(() -> {
            client.disconnect();
            System.out.println("client disconnected");
        }).start();
        super.onDestroyView();
    }
}
