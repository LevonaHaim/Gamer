package com.gamerzone.app.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gamerzone.app.R;
import com.gamerzone.app.data.api.GamesClient;
import com.gamerzone.app.data.api.GamesService;
import com.gamerzone.app.model.Game;
import com.gamerzone.app.model.Result;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminPanelFragment extends Fragment {
    DatabaseReference dbReference = FirebaseDatabase.getInstance().getReference("games");
    ValueEventListener valueEventListener;
    TextView instructionText;
    GamesAdapter adapter;
    GamesService gamesService;
    ArrayList<Game> games;
    ArrayList<Game> gamesInDb;

    final String API_KEY = "19ed962a6d464a8b8b3f770dbc9fbcc9";
    final String MAX_GAMES_RESULT = "15";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        games = new ArrayList<>();

        // Creation of a retrofit client to fetch Games from the API
        gamesService = GamesClient.getClient().create(GamesService.class);

        // Listen to the current games in the DB to disallow adding the same game twice
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<Game> updatedGames = new ArrayList<>();
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    Game game = data.getValue(Game.class);
                    if (game != null) {
                        updatedGames.add(game);
                    }
                }
                if (!updatedGames.isEmpty()) {
                    gamesInDb = updatedGames;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_pannel, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        instructionText = view.findViewById(R.id.instruction);
        TextInputEditText searchGameEditText = view.findViewById(R.id.search_game_edit_text);

        Button searchGameButton = view.findViewById(R.id.search_button);
        RecyclerView rv = view.findViewById(R.id.games_result_rv);
        adapter = new GamesAdapter(new ArrayList<>(), new GamesAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Game item) {
                // When clicking on a game, check if it already exists - show a Toast to indicate that
                // If not, pop up an AlertDialog to propose adding this game to the available games on Firebase
                if (gameAlreadyExist(item)) {
                    Toast.makeText(requireContext(), "This game already exists", Toast.LENGTH_SHORT).show();
                } else {
                    AlertDialog alertDialog = new AlertDialog.Builder(requireContext())
                            .setTitle("Do you want to add this game?")
                            .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                            .setPositiveButton("Yes", (dialog, which) -> saveGameToFirebase(item))
                            .create();
                    alertDialog.show();
                }
            }

            @Override
            public void onLongPress(Game item) {}
        });
        rv.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        rv.setAdapter(adapter);

        searchGameButton.setOnClickListener(v -> {
            if (!Objects.requireNonNull(searchGameEditText.getText()).toString().isEmpty()) {
                searchGameByName(searchGameEditText.getText().toString());
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        dbReference.addValueEventListener(valueEventListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        dbReference.removeEventListener(valueEventListener);
    }

    private boolean gameAlreadyExist(Game item) {
        boolean exist = false;
        if (gamesInDb != null) {
            for (Game game : gamesInDb) {
                if (game.getName().equals(item.getName())) {
                    exist = true;
                    break;
                }
            }
        }
        return exist;
    }

    private void saveGameToFirebase(Game game) {
        // Create a UUID and set it a the child name to be able to delete and check for existence later
        game.setId(UUID.randomUUID().toString());
        dbReference.child(game.getId()).setValue(game)
                .addOnSuccessListener(unused -> Toast.makeText(requireContext(),
                        "Game saved successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(requireContext(),
                        "Failure occurred, please check your internet connection and try again",
                        Toast.LENGTH_SHORT).show());
    }

    private void searchGameByName(String searchedString) {
        games = new ArrayList<>();

        // Calling the API with the searched string
        Call<Result> call = gamesService.searchGames(API_KEY,
                searchedString, MAX_GAMES_RESULT);
        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(@NonNull Call<Result> call, @NonNull Response<Result> response) {
                // Get the results from the API and update the list
                Result result = response.body();
                assert result != null;
                games.addAll(result.getResults());
                adapter.updateDataSet(games);

                if (!games.isEmpty()) {
                    instructionText.setVisibility(View.VISIBLE);
                } else {
                    instructionText.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Result> call, @NonNull Throwable t) {
                call.cancel();
            }
        });
    }
}