package com.example.raindown.finalyearproject;

import android.graphics.Color;
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

import Helper.SummaryItem;

public class SummaryAdapter extends RecyclerView.Adapter<SummaryAdapter.SummaryViewHolder> {
    private ArrayList<SummaryItem> mSummaryList;
    private boolean hideNum = false;
    private boolean hideAmount = false;
    private boolean greenAmount = false;
    private boolean redAmount = false;
    private boolean countAmount = false;

    public static class SummaryViewHolder extends RecyclerView.ViewHolder{
        public TextView itemNum;
        public ImageView itemImage;
        public TextView itemName;
        public TextView itemAmount;

        public SummaryViewHolder(View itemView) {
            super(itemView);
            itemNum = itemView.findViewById(R.id.txtSummaryItemNum);
            itemImage = itemView.findViewById(R.id.txtSummaryItemImage);
            itemName = itemView.findViewById(R.id.txtSummaryItemName);
            itemAmount = itemView.findViewById(R.id.txtSummaryItemAmount);

        }
    }

    public void hideNumTextView(boolean hide){
        hideNum = hide;
        notifyDataSetChanged();
    }

    public void hideAmountTextView(boolean hide){
        hideAmount = hide;
        notifyDataSetChanged();
    }

    public void setGreenAmount(boolean set){
        greenAmount = set;
        notifyDataSetChanged();
    }

    public void setRedAmount(boolean set){
        redAmount = set;
        notifyDataSetChanged();
    }

    public void setCountAmount(boolean set){
        countAmount = set;
        notifyDataSetChanged();
    }

    public SummaryAdapter(ArrayList<SummaryItem> itemList){
        mSummaryList = itemList;
    }

    @Override
    public SummaryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.summary_report_item, parent, false);
        SummaryViewHolder svh = new SummaryViewHolder(v);
        return svh;
    }

    @Override
    public void onBindViewHolder(SummaryViewHolder holder, int position) {
        SummaryItem currentItem = mSummaryList.get(position);

        holder.itemNum.setText(currentItem.getItemNum());
        Picasso.with(holder.itemView.getContext()).load(currentItem.getItemImage()).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).into(holder.itemImage);
        holder.itemName.setText(currentItem.getItemName());

        if (hideNum){
            holder.itemNum.setVisibility(View.INVISIBLE);
        }
        if (hideAmount) {
            holder.itemAmount.setVisibility(View.INVISIBLE);
        }
        if (greenAmount){
            holder.itemAmount.setText("+ RM " + currentItem.getItemAmount());
            holder.itemAmount.setTextColor(Color.parseColor("#32CD32"));
        } else if (redAmount){
            holder.itemAmount.setText("RM " + currentItem.getItemAmount());
            holder.itemAmount.setTextColor(Color.RED);
        } else {
            holder.itemAmount.setText("RM " + currentItem.getItemAmount());
        }
        if (countAmount){
            holder.itemAmount.setText(String.format("%.0f", currentItem.getItemAmount()));
        }
    }

    @Override
    public int getItemCount() {
        return mSummaryList.size();
    }
}
