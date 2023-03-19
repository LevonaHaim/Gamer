package com.gamerzone.app.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.gamerzone.app.R;
import com.gamerzone.app.model.Game;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class GameDetailsFragment extends Fragment {
    ImageView favoriteButton;
    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    DatabaseReference favoritesReference = FirebaseDatabase.getInstance()
            .getReference("users/" + currentUser.getUid() + "/favorites");
    ValueEventListener valueEventListener;
    ArrayList<Game> games;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        games = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_game_details, container, false);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ImageView gameImage = view.findViewById(R.id.game_details_image);
        favoriteButton = view.findViewById(R.id.game_details_favorite_button);
        TextView gameName = view.findViewById(R.id.games_details_name);
        TextView gameReleaseDate = view.findViewById(R.id.games_details_release_date);
        TextView gameRating = view.findViewById(R.id.games_details_rating);
        TextView gameGenres = view.findViewById(R.id.games_details_genres);

        // Get game details from args
        assert getArguments() != null;
        String id = getArguments().getString("id");
        String name = getArguments().getString("name");
        String releaseDate = getArguments().getString("release_date");
        String rating = getArguments().getString("rating");
        String imageUrl = getArguments().getString("image_url");
        ArrayList<String> genres = getArguments().getStringArrayList("genres");

        // Listen to the favorite games under current user in Firebase and check if the current game is a favorite
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Set button as selected
                favoriteButton.setSelected(snapshot.hasChild(id));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };

        if (name != null) {
            gameName.setText(name);
        }

        if (releaseDate != null) {
            gameReleaseDate.setText(releaseDate);
        }

        if (rating != null) {
            gameRating.setText(rating);
        }

        if (genres != null) {
            StringBuilder stringBuilder = new StringBuilder();
            for (String genre : genres) {
                stringBuilder.append(genre);
                if (!genre.equals(genres.get(genres.size() - 1))) {
                    stringBuilder.append(", ");
                }
            }
            gameGenres.setText(stringBuilder.toString());
        }

        if (imageUrl != null) {
            // Use the Glide library to show images from a URL
            RequestOptions options = new RequestOptions()
                    .placeholder(R.mipmap.ic_launcher_round)
                    .error(R.mipmap.ic_launcher_round)
                    .centerCrop()
                    .override(310, 400);
            Glide.with(gameImage).load(imageUrl).apply(options).into(gameImage);
        }

        favoriteButton.setOnClickListener(v -> {
            // Save the current game to the favorites in Firebase under the current user
            String gameId;
            if (id != null) {
                gameId = id;
            } else {
                gameId = "";
            }
            Game game = new Game(gameId, name, imageUrl);
            saveGameToUserFavorites(game);
        });
    }

    private void saveGameToUserFavorites(Game game) {
        if (!favoriteButton.isSelected()) {
            favoritesReference.child(game.getId()).setValue(game)
                    .addOnSuccessListener(unused -> Toast.makeText(requireContext(),
                            "Game saved to Favorites", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(requireContext(),
                            "Failure occurred, please check your internet connection and try again",
                            Toast.LENGTH_SHORT).show());
        } else {
            favoritesReference.child(game.getId()).removeValue()
                    .addOnSuccessListener(unused -> Toast.makeText(requireContext(),
                            "Game removed from Favorites", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(requireContext(),
                            "Failure occurred, please check your internet connection and try again",
                            Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        favoritesReference.addValueEventListener(valueEventListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        favoritesReference.removeEventListener(valueEventListener);
    }
}