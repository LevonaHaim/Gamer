package com.gamerzone.app.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.gamerzone.app.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class RegisterFragment extends Fragment {
    private FirebaseAuth firebaseAuth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        EditText name = view.findViewById(R.id.name_edit_text);
        EditText email = view.findViewById(R.id.email_edit_text);
        EditText password = view.findViewById(R.id.password_edit_text);
        Button registerButton = view.findViewById(R.id.register_fragment_button);

        registerButton.setOnClickListener(v -> {
            String nameText = name.getText().toString();
            String emailText = email.getText().toString();
            String passwordText = password.getText().toString();

            // Basic input validation
            if (!nameText.isEmpty() && !emailText.isEmpty() && !passwordText.isEmpty()) {
                firebaseAuth.createUserWithEmailAndPassword(emailText, passwordText)
                        .addOnCompleteListener(requireActivity(), task -> {
                            // In case of success, updates user profile with the given user name
                            if (task.isSuccessful()) {
                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                UserProfileChangeRequest profileUpdates =
                                        new UserProfileChangeRequest.Builder()
                                                .setDisplayName(nameText).build();
                                assert user != null;
                                user.updateProfile(profileUpdates)
                                        .addOnCompleteListener(profileChangeTask -> {
                                            if (profileChangeTask.isSuccessful()) {
                                                navigateToGames();
                                                Log.d("RegisterFragment",
                                                        "User profile updated.");
                                            }
                                        });
                            } else {
                                Toast.makeText(getContext(), "Registration failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(e -> Log.e("RegisterFragment", e.getMessage()));
            } else {
                Toast.makeText(getContext(), "Wrong email or password, please try again",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToGames() {
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_registerFragment_to_gamesFragment);
    }
}