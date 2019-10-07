package com.example.raindown.finalyearproject;
/*Author : Lee Thian Xin
Programme : RSD3
Year : 2018*/

import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

import Helper.StoreBasicInfoOB;

public class StoreListRecyclerViewAdapter extends RecyclerView.Adapter<StoreListRecyclerViewAdapter.MyviewHolder>{

    private Context mContext;
    private List<StoreBasicInfoOB> mData;
    FragmentManager fragmentManager;

    public StoreListRecyclerViewAdapter(Context mContext, List<StoreBasicInfoOB> mData) {
        this.mContext = mContext;
        this.mData = mData;
    }

    @Override
    public MyviewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        LayoutInflater mInfrater = LayoutInflater.from(mContext);
        view = mInfrater.inflate(R.layout.storelist_cardview, parent,false);
        return new MyviewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyviewHolder holder, final int position) {

        Picasso.with(mContext).load(mData.get(position).getStoreImg()).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).into(holder.shop_image);
        holder.shop_name.setText(mData.get(position).getStoreName());
        holder.shop_detail.setText(mData.get(position).getStoreDetail());
        holder.shop_rating.setText("");
        holder.shop_creator.setText(mData.get(position).getStoreCreator());
        holder.shoplist_cardview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                StoreProfile frag = new StoreProfile();
                Bundle bundle = new Bundle();
                bundle.putString("storeID", mData.get(position).getStoreID());
                frag.setArguments(bundle);

                fragmentManager = ((AppCompatActivity) view.getContext()).getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.update_fragmentHolder, frag)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
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

        CardView shoplist_cardview;
        ImageView shop_image;
        TextView shop_name;
        TextView shop_detail;
        TextView shop_rating;
        TextView shop_creator;

        public MyviewHolder(View itemView) {
            super(itemView);

            shoplist_cardview = (CardView) itemView.findViewById(R.id.storeListCardView);
            shop_image = (ImageView) itemView.findViewById(R.id.storeList_img);
            shop_name = (TextView) itemView.findViewById(R.id.store_name);
            shop_detail = (TextView) itemView.findViewById(R.id.store_detail);
            shop_rating = (TextView) itemView.findViewById(R.id.store_rating);
            shop_creator = (TextView) itemView.findViewById(R.id.creator_name);
        }
    }
}
