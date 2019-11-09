package com.example.raindown.finalyearproject;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import Helper.Stuff;

public class StoreStuffAdapter extends RecyclerView.Adapter<StoreStuffAdapter.StoreStuffViewHolder> {
    private ArrayList<Stuff> mStuffList;
    private OnItemClickListener mListener;
    private Boolean hide = false;

    public interface OnItemClickListener {
        void onItemClick(int position);
        void onDeleteClick(int position);
        void onEditClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        mListener = listener;
    }

    public static class StoreStuffViewHolder extends RecyclerView.ViewHolder{
        public TextView txtStuffNum;
        public ImageView StuffImageView;
        public TextView txtStuffName;
        public TextView txtStuffPrice;
        public TextView txtStuffQuantity;
        public Button btnEdit;
        public Button btnRemove;

        public StoreStuffViewHolder(View itemView, final OnItemClickListener listener) {
            super(itemView);

            txtStuffNum = itemView.findViewById(R.id.txtStuffNum);
            StuffImageView = itemView.findViewById(R.id.StuffImageView);
            txtStuffName= itemView.findViewById(R.id.txtStuffName);
            txtStuffPrice = itemView.findViewById(R.id.txtStuffPrice);
            txtStuffQuantity = itemView.findViewById(R.id.txtStuffQuantity);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnRemove = itemView.findViewById(R.id.btnRemove);

            btnEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null){
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION){
                            listener.onEditClick(position);
                        }
                    }
                }
            });

            btnRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null){
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION){
                            listener.onDeleteClick(position);
                        }
                    }
                }
            });

            itemView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view){
                    if (listener != null){
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION){
                            listener.onItemClick(position);
                        }
                    }
                }
            });
        }
    }

    public void hideButton(boolean hide){
        this.hide = hide;
        notifyDataSetChanged();
    }

    public StoreStuffAdapter(ArrayList<Stuff> stuffList){
        mStuffList = stuffList;
    }

    @Override
    public StoreStuffViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.storestufflist, parent, false);
        StoreStuffViewHolder ssvh = new StoreStuffViewHolder(v, mListener);
        return ssvh;
    }

    @Override
    public void onBindViewHolder(StoreStuffViewHolder holder, int position) {
        Stuff currentStuff = mStuffList.get(position);

        holder.txtStuffNum.setText(String.format("%d.", position + 1));
        Picasso.with(holder.itemView.getContext()).load(currentStuff.getStuffImage()).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).into(holder.StuffImageView);
        holder.txtStuffName.setText(currentStuff.getStuffName());
        holder.txtStuffPrice.setText(String.format("RM %.2f", currentStuff.getStuffPrice()));
        holder.txtStuffQuantity.setText(String.format("%d" + " left", currentStuff.getStuffQuantity()));

        if (hide){
            holder.btnRemove.setVisibility(View.INVISIBLE);
            holder.btnRemove.setEnabled(false);
            holder.btnEdit.setVisibility(View.INVISIBLE);
            holder.btnEdit.setEnabled(false);
        }

    }

    @Override
    public int getItemCount() {
        return mStuffList.size();
    }
}
