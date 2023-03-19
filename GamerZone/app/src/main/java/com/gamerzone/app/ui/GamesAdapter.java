package com.gamerzone.app.ui;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.gamerzone.app.model.Game;
import com.gamerzone.app.R;

import java.util.ArrayList;

public class GamesAdapter extends RecyclerView.Adapter<GamesAdapter.GameViewHolder> {
    private ArrayList<Game> dataSet;
    private final OnItemClickListener listener;

    public GamesAdapter(ArrayList<Game> dataSet, OnItemClickListener itemClickListener) {
        this.dataSet = dataSet;
        this.listener = itemClickListener;
    }

    @NonNull
    @Override
    public GameViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_game, viewGroup, false);
        return new GameViewHolder(view);
    }

    @Override
    public void onBindViewHolder(GameViewHolder viewHolder, final int position) {
        Game game = dataSet.get(position);
        viewHolder.getNameTv().setText(game.getName());

        ImageView imageView = viewHolder.getImageView();

        // Use the Glide library to show images from a URL
        RequestOptions options = new RequestOptions()
                .placeholder(R.mipmap.ic_launcher_round)
                .error(R.mipmap.ic_launcher_round)
                .transform(new CenterCrop(),new RoundedCorners(20))
                .override(310, 400);
        Glide.with(imageView).load(game.getImageUrl()).apply(options).into(imageView);

        imageView.setOnClickListener(v -> listener.onItemClick(game));
        imageView.setOnLongClickListener(v -> {
            listener.onLongPress(game);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateDataSet(ArrayList<Game> newDataset) {
        this.dataSet = newDataset;
        notifyDataSetChanged();
    }

    public static class GameViewHolder extends RecyclerView.ViewHolder {
        private final TextView name;
        private final ImageView image;

        public GameViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.game_name);
            image = view.findViewById(R.id.game_image);
        }

        public TextView getNameTv() {
            return name;
        }

        public ImageView getImageView() {
            return image;
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Game item);
        void onLongPress(Game item);
    }
}
