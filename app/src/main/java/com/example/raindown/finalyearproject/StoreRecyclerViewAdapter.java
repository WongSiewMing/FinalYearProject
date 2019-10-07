package com.example.raindown.finalyearproject;
/*Author : Lee Thian Xin
Programme : RSD3
Year : 2018*/

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import Helper.StoreOption;

public class StoreRecyclerViewAdapter extends RecyclerView.Adapter<StoreRecyclerViewAdapter.MyviewHolder> {

    private Context mContext;
    private List<StoreOption> mData;
    FragmentManager fragmentManager;

    public StoreRecyclerViewAdapter(Context mContext, List<StoreOption> mData) {
        this.mContext = mContext;
        this.mData = mData;
    }

    @Override
    public MyviewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view;
        LayoutInflater mInflater = LayoutInflater.from(mContext);
        view = mInflater.inflate(R.layout.store_cardview, parent, false);
        return new MyviewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyviewHolder holder, final int position) {

        holder.shop_title.setText(mData.get(position).getStoreOptionName());
        holder.shop_thumbnail.setImageResource(mData.get(position).getStoreOption());
        holder.store_cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                StoreList frag = new StoreList();
                Bundle s1 = new Bundle();
                s1.putString("Category", mData.get(position).getStoreOptionName());

                frag.setArguments(s1);

                fragmentManager = ((AppCompatActivity) view.getContext()).getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.update_fragmentHolder, frag)
                        .setTransition(android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .addToBackStack(null)
                        .commit();

            }
        });

    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public static class MyviewHolder extends RecyclerView.ViewHolder{

        TextView shop_title;
        ImageView shop_thumbnail;
        CardView store_cardView;

        public MyviewHolder(View itemView) {
            super(itemView);

            shop_title = (TextView) itemView.findViewById(R.id.store_title);
            shop_thumbnail = (ImageView) itemView.findViewById(R.id.store_img);
            store_cardView = (CardView) itemView.findViewById(R.id.storeCardView);

        }
    }
}
