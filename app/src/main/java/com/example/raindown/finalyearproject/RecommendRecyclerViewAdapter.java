package com.example.raindown.finalyearproject;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import Helper.Stuff;

public class RecommendRecyclerViewAdapter extends RecyclerView.Adapter<RecommendRecyclerViewAdapter.RecommendViewHolder> {
    private ArrayList<Stuff> stuffList;

    public static class RecommendViewHolder extends RecyclerView.ViewHolder {
        public ImageView stuffImageView;
        public TextView stuffTextView;

        public RecommendViewHolder(View itemView) {
            super(itemView);

            stuffImageView = itemView.findViewById(R.id.recommendStuffImage);
            stuffTextView = itemView.findViewById(R.id.recommendStuffName);
        }
    }

    public RecommendRecyclerViewAdapter(ArrayList<Stuff> stuffList){
        this.stuffList = stuffList;
    }

    @Override
    public RecommendViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recommendstuff_cardview, parent, false);
        RecommendViewHolder rvh = new RecommendViewHolder(v);
        return rvh;
    }

    @Override
    public void onBindViewHolder(RecommendViewHolder holder, int position) {
        Stuff currentStuff = stuffList.get(position);

        Picasso.with(holder.itemView.getContext()).load(stuffList.get(position).getStuffImage()).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).into(holder.stuffImageView);
        holder.stuffTextView.setText(currentStuff.getStuffName());

    }

    @Override
    public int getItemCount() {
        return stuffList.size();
    }
}
