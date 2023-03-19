package com.gamerzone.app.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gamerzone.app.R;
import com.gamerzone.app.model.Game;
import com.gamerzone.app.model.Genre;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class GamesFragment extends Fragment {
    DatabaseReference isAdminReference = FirebaseDatabase.getInstance()
            .getReference("users/" +
                    Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid() +
                    "/isAdmin");
    ValueEventListener adminValueEventListener;
    boolean isAdmin = false;
    DatabaseReference gamesReference = FirebaseDatabase.getInstance().getReference("games");
    ValueEventListener valueEventListener;
    ArrayList<Game> games;
    ArrayList<Game> filteredSortedList = new ArrayList<>();
    GamesAdapter adapter;
    MenuProvider menuProvider;
    SearchView searchView;
    boolean searchingEnabled = true;
    List<String> activeGenreFilters = new ArrayList<>();
    List<String> activeYearFilters = new ArrayList<>();
    FilterBottomSheetDialog.SortMethod activeSortMethod = FilterBottomSheetDialog.SortMethod.NONE;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Listen to "isAdmin" indication from Firebase to allow/disallow "Long press to delete" ability
        adminValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                isAdmin = Boolean.TRUE.equals(dataSnapshot.getValue(Boolean.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };

        // Listen to changes in Games on Firebase and update the RecyclerView accordingly
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
                games = updatedGames;
                adapter.updateDataSet(games);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };

        // Creation of the MenuProvider object for the Toolbar menu
        menuProvider = new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.menu_toolbar_games, menu);
                searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();

                // Search filtering logic, get the updated text from the search view and filter the game list
                SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        if (searchingEnabled) {
                            filterGamesBySearch(newText);
                        }
                        return false;
                    }
                };
                searchView.setOnQueryTextListener(queryTextListener);
                searchView.setOnSearchClickListener(v -> searchingEnabled = true);
            }

            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                // Take care of opening the filter bottom sheet dialog when clicking on the Toolbar's filter icon
                if (menuItem.getItemId() == R.id.action_filter) {
                    searchingEnabled = false;
                    searchView.onActionViewCollapsed();
                    List<String> genres = getAvailableGenres();
                    List<String> releaseYears = getAvailableReleaseYears();
                    FilterBottomSheetDialog bottomSheet =
                            new FilterBottomSheetDialog(genres, releaseYears, activeGenreFilters,
                                    activeYearFilters, activeSortMethod,
                                    (checkedGenres, checkedYears, sortMethod) -> {
                                        // On bottom sheet closes callback, get the selected filters and filter the game list
                                        activeGenreFilters = checkedGenres;
                                        activeYearFilters = checkedYears;
                                        activeSortMethod = sortMethod;
                                        sortAndFilter(checkedGenres, checkedYears, sortMethod);
                                    });

                    bottomSheet.show(getParentFragmentManager(), "FilterBottomSheet");
                }
                return true;
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_games, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Add the menuProvider to the Toolbar
        ((MenuHost) requireActivity()).addMenuProvider(menuProvider);

        games = new ArrayList<>();
        RecyclerView rv = view.findViewById(R.id.games_rv);
        adapter = new GamesAdapter(games, new GamesAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Game item) {
                // Clear filters for future use
                activeSortMethod = FilterBottomSheetDialog.SortMethod.NONE;
                activeGenreFilters.clear();
                activeYearFilters.clear();

                // Put the needed data for the game details screen
                Bundle data = new Bundle();
                data.putString("id", item.getId());
                data.putString("name", item.getName());
                data.putString("release_date", item.getReleaseDate());
                data.putString("rating", item.getRating().toString());
                data.putString("image_url", item.getImageUrl());

                if (item.getGenres() != null) {
                    ArrayList<String> genres = new ArrayList<>();
                    for (Genre genre : item.getGenres()) {
                        genres.add(genre.getName());
                    }
                    data.putStringArrayList("genres", genres);
                }

                NavHostFragment.findNavController(requireParentFragment())
                        .navigate(R.id.gameDetailsFragment, data);
            }

            @Override
            public void onLongPress(Game item) {
                // If the logged in user is Admin, long click on a game card will pop up an AlertDialog and propose a deletion option
                if (isAdmin) {
                    AlertDialog alertDialog = new AlertDialog.Builder(requireContext())
                            .setTitle("Do you want to delete this Game?")
                            .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                            .setPositiveButton("Yes", (dialog, which) ->
                                    // In case of clicking on positive button, delete the game from Firebase
                                    gamesReference.child(item.getId()).removeValue()
                                            .addOnSuccessListener(unused -> Toast.makeText(requireContext(),
                                                    "Game deleted!", Toast.LENGTH_SHORT).show())
                                            .addOnFailureListener(e -> Toast.makeText(requireContext(),
                                                    "Failure occurred, please check your internet connection and try again",
                                                    Toast.LENGTH_SHORT).show()))
                            .create();
                    alertDialog.show();
                }
            }
        });
        rv.setAdapter(adapter);

        RecyclerView.LayoutManager lm = new GridLayoutManager(requireContext(), 3);
        rv.setLayoutManager(lm);
    }

    @Override
    public void onStart() {
        super.onStart();
        gamesReference.addValueEventListener(valueEventListener);
        isAdminReference.addValueEventListener(adminValueEventListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        gamesReference.removeEventListener(valueEventListener);
        isAdminReference.removeEventListener(adminValueEventListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ((MenuHost) requireActivity()).removeMenuProvider(menuProvider);
    }

    private void filterGamesBySearch(String searchedText) {
        List<Game> listToSearch;
        if (!filteredSortedList.isEmpty()) {
            listToSearch = filteredSortedList;
        } else {
            listToSearch = games;
        }

        ArrayList<Game> filteredList = new ArrayList<>();
        for (Game game : listToSearch) {
            if (game.getName().toLowerCase().contains(searchedText.toLowerCase())) {
                filteredList.add(game);
            }
        }
        adapter.updateDataSet(filteredList);
    }

    private List<String> getAvailableGenres() {
        ArrayList<String> genres = new ArrayList<>();
        for (Game game : games) {
            if (game.getGenres() != null) {
                for (Genre genre : game.getGenres()) {
                    if (!genres.contains(genre.getName())) {
                        genres.add(genre.getName());
                    }
                }
            }
        }
        return genres;
    }

    private List<String> getAvailableReleaseYears() {
        ArrayList<String> releaseYears = new ArrayList<>();
        for (Game game : games) {
            if (game.getReleaseDate() != null) {
                String[] splitResult = game.getReleaseDate().split("-");
                if (splitResult.length == 3) {
                    String releaseYear = splitResult[0];
                    if (!releaseYears.contains(releaseYear)) {
                        releaseYears.add(releaseYear);
                    }
                }
            }
        }
        Collections.sort(releaseYears);
        return releaseYears;
    }

    private void sortAndFilter(List<String> checkedGenres, List<String> checkedYears,
                               FilterBottomSheetDialog.SortMethod sortMethod) {
        filteredSortedList.clear();
        for (Game game : games) {
            boolean isGenreMatched = false;
            boolean isYearMatched = false;
            boolean isAllGenresMatchCriteria = checkedGenres.isEmpty();
            boolean isAllYearsMatchCriteria = checkedYears.isEmpty();
            if (!isAllGenresMatchCriteria) {
                if (game.getGenres() != null) {
                    for (Genre genre : game.getGenres()) {
                        if (checkedGenres.contains(genre.getName())) {
                            isGenreMatched = true;
                            break;
                        }
                    }
                }
            }
            if (!isAllYearsMatchCriteria) {
                if (game.getReleaseDate() != null &&
                        checkedYears.contains(game.getReleaseDate().split("-")[0])) {
                    isYearMatched = true;
                }
            }

            if (isGenreMatched && isYearMatched || isGenreMatched && isAllYearsMatchCriteria ||
                    isYearMatched && isAllGenresMatchCriteria ||
                    isAllGenresMatchCriteria && isAllYearsMatchCriteria) {
                filteredSortedList.add(game);
            }
        }

        if (sortMethod == FilterBottomSheetDialog.SortMethod.A_Z) {
            filteredSortedList.sort(Comparator.comparing(Game::getName));
        } else if (sortMethod == FilterBottomSheetDialog.SortMethod.Z_A) {
            filteredSortedList.sort(Comparator.comparing(Game::getName));
            Collections.reverse(filteredSortedList);
        }

        adapter.updateDataSet(filteredSortedList);
    }
}
