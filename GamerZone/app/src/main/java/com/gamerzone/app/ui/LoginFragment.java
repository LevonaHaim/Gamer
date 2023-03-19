package com.gamerzone.app.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.gamerzone.app.R;
import com.google.firebase.auth.FirebaseAuth;

public class LoginFragment extends Fragment {
    private FirebaseAuth firebaseAuth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        EditText email = view.findViewById(R.id.email_edit_text);
        EditText password = view.findViewById(R.id.password_edit_text);
        Button loginButton = view.findViewById(R.id.login_fragment_button);

        loginButton.setOnClickListener(v -> {
            String emailText = email.getText().toString();
            String passwordText = password.getText().toString();

            if (!emailText.isEmpty() && !passwordText.isEmpty()) {
                firebaseAuth.signInWithEmailAndPassword(emailText, passwordText)
                        .addOnCompleteListener(requireActivity(), task -> {
                            if (task.isSuccessful()) {
                                navigateToGames();
                            } else {
                                Toast.makeText(getContext(), "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                Toast.makeText(getContext(), "Wrong email or password, please try again",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToGames() {
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_loginFragment_to_gamesFragment);
    }
}