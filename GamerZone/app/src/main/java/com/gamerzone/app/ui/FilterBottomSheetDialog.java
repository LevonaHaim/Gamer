package com.gamerzone.app.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.gamerzone.app.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipDrawable;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class FilterBottomSheetDialog extends BottomSheetDialogFragment {
    private final List<String> activeGenreFilters;
    private final List<String> activeYearFilters;
    private final SortMethod activeSortMethod;
    private Chip aToZSortChip;
    private Chip zToASortChip;
    private ChipGroup genresChipGroup;
    private ChipGroup releaseYearsChipGroup;
    private final List<String> releaseYears;
    private final List<String> genres;
    private final OnFilterBottomSheetActionListener listener;

    public FilterBottomSheetDialog(List<String> genres,
                                   List<String> releaseYears,
                                   List<String> activeGenreFilters,
                                   List<String> activeYearFilters,
                                   SortMethod activeSortMethod,
                                   OnFilterBottomSheetActionListener bottomSheetActionListener) {
        this.genres = genres;
        this.releaseYears = releaseYears;
        this.activeGenreFilters = activeGenreFilters;
        this.activeYearFilters = activeYearFilters;
        this.activeSortMethod = activeSortMethod;
        this.listener = bottomSheetActionListener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_filter, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ChipGroup sortingChipGroup = view.findViewById(R.id.alphabetic_sort_chip_group);
        aToZSortChip = view.findViewById(R.id.a_to_z_chip);
        zToASortChip = view.findViewById(R.id.z_to_a_chip);
        genresChipGroup = view.findViewById(R.id.genres_chip_group);
        releaseYearsChipGroup = view.findViewById(R.id.release_years_chip_group);

        // Check if there are already applied filters from previous uses and apply them if so
        if (activeSortMethod == SortMethod.A_Z) {
            aToZSortChip.setChecked(true);
        } else if (activeSortMethod == SortMethod.Z_A) {
            zToASortChip.setChecked(true);
        }

        sortingChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (aToZSortChip.isChecked()) {
                zToASortChip.setChecked(false);
            } else if (zToASortChip.isChecked()) {
                aToZSortChip.setChecked(false);
            }
        });

        // Initiate filters according to the received data
        initGenres();
        initReleaseYears();
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);

        // When user navigate out of the filter bottom sheet, check the selected values and pass then through a callback
        SortMethod sortMethod;
        if (aToZSortChip.isChecked()) {
            sortMethod = SortMethod.A_Z;
        } else if (zToASortChip.isChecked()) {
            sortMethod = SortMethod.Z_A;
        } else {
            sortMethod = SortMethod.NONE;
        }
        List<String> checkedGenres = getCheckedChips(genresChipGroup);
        List<String> checkedYears = getCheckedChips(releaseYearsChipGroup);
        listener.onDismiss(checkedGenres, checkedYears, sortMethod);
    }

    public void initGenres() {
        for (String genre : genres) {
            Chip chip = new Chip(genresChipGroup.getContext());
            chip.setText(genre);
            ChipDrawable chipDrawable = ChipDrawable
                    .createFromAttributes(genresChipGroup.getContext(), null, 0,
                            com.google.android.material.R.style.Widget_MaterialComponents_Chip_Filter);
            chip.setChipDrawable(chipDrawable);
            if (activeGenreFilters.contains(genre)) {
                chip.setChecked(true);
            }
            genresChipGroup.addView(chip);
        }
    }

    public void initReleaseYears() {
        for (String year : releaseYears) {
            Chip chip = new Chip(releaseYearsChipGroup.getContext());
            chip.setText(year);
            ChipDrawable chipDrawable = ChipDrawable
                    .createFromAttributes(releaseYearsChipGroup.getContext(), null, 0,
                            com.google.android.material.R.style.Widget_MaterialComponents_Chip_Filter);
            chip.setChipDrawable(chipDrawable);
            if (activeYearFilters.contains(year)) {
                chip.setChecked(true);
            }
            releaseYearsChipGroup.addView(chip);
        }
    }

    private List<String> getCheckedChips(ChipGroup chipGroup) {
        ArrayList<String> checked = new ArrayList<>();
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            Chip chip = (Chip) chipGroup.getChildAt(i);
            if (chip.isChecked()) {
                checked.add(chip.getText().toString());
            }
        }
        return checked;
    }

    public interface OnFilterBottomSheetActionListener {
        void onDismiss(List<String> checkedGenres, List<String> checkedYears, SortMethod sortMethod);
    }

    enum SortMethod {
        NONE,
        A_Z,
        Z_A
    }
}
