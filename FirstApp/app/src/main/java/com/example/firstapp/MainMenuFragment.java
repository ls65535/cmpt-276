package com.example.firstapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.firstapp.databinding.FragmentMainMenuBinding;

/*
 * MainMenuFragment.java
 *
 * Class Description: Main menu of the application,
 *                    having redirection to Quiz and Settings on top.
 * Class Invariant: -
 *
 */

public class MainMenuFragment extends Fragment {

private FragmentMainMenuBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      binding = FragmentMainMenuBinding.inflate(inflater, container, false);
      return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Romanisation r = new Romanisation(MyApplication.getAppContext());
        Button quizBtn = view.findViewById(R.id.quiz_button);
        quizBtn.setText(r.input(getString(R.string.start_quiz), getActivity()));
        quizBtn.setOnClickListener(view1 -> NavHostFragment.findNavController(MainMenuFragment.this)
                .navigate(R.id.action_MainMenuFragment_to_FirstFragment));
    }

@Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}