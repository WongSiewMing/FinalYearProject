package com.example.raindown.finalyearproject;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.ArrayList;

import Helper.Stuff;

public class StoreStuffAdapter extends RecyclerView.Adapter<StoreStuffAdapter.StoreStuffViewHolder> {
    private ArrayList<Stuff> mStuffList;

    public static class StoreStuffViewHolder extends RecyclerView.ViewHolder{
        public TextView txtStuffNum;
        public ImageView StuffImageView;
        public TextView txtStuffName;
        public TextView txtStuffPrice;

        public StoreStuffViewHolder(View itemView) {
            super(itemView);

            txtStuffNum = itemView.findViewById(R.id.txtStuffNum);
            StuffImageView = itemView.findViewById(R.id.StuffImageView);
            txtStuffName= itemView.findViewById(R.id.txtStuffName);
            txtStuffPrice = itemView.findViewById(R.id.txtStuffPrice);
        }
    }

    public StoreStuffAdapter(ArrayList<Stuff> stuffList){
        mStuffList = stuffList;
    }

    @Override
    public StoreStuffViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.storestufflist, parent, false);
        StoreStuffViewHolder ssvh = new StoreStuffViewHolder(v);
        return ssvh;
    }

    @Override
    public void onBindViewHolder(StoreStuffViewHolder holder, int position) {
        Stuff currentStuff = mStuffList.get(position);

        holder.txtStuffNum.setText(String.format("%d.", position + 1));
        Picasso.with(holder.itemView.getContext()).load(currentStuff.getStuffImage()).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).into(holder.StuffImageView);
        holder.txtStuffName.setText(currentStuff.getStuffName());
        holder.txtStuffPrice.setText(String.format("RM %.2f", currentStuff.getStuffPrice()));
    }

    @Override
    public int getItemCount() {
        return mStuffList.size();
    }
}
