package com.example.raindown.finalyearproject;
/*Author : Lee Thian Xin
Programme : RSD3
Year : 2018*/

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

import Helper.StuffBasicInfoOB;

public class StuffBasicInfoRecycleViewAdapter extends RecyclerView.Adapter<StuffBasicInfoRecycleViewAdapter.MyviewHolder>{
    private Context mContext;
    private List<StuffBasicInfoOB> mData;
    private Dialog dialog;
    private TextView selectedStuffName, selectedStuffDescription;
    private ImageView selectedStuffImage;
    private RelativeLayout selectedStuffField;
    private TextView selectedStuffID;
    private static final String TAG = "testData";

    public StuffBasicInfoRecycleViewAdapter(Context mContext, List<StuffBasicInfoOB> mData, Dialog dialog, TextView selectedStuffName, TextView selectedStuffDescription, ImageView selectedStuffImage, RelativeLayout selectedStuffField, TextView selectedStuffID) {
        this.mContext = mContext;
        this.mData = mData;
        this.dialog = dialog;
        this.selectedStuffName = selectedStuffName;
        this.selectedStuffDescription = selectedStuffDescription;
        this.selectedStuffImage = selectedStuffImage;
        this.selectedStuffField = selectedStuffField;
        this.selectedStuffID = selectedStuffID;
    }

    @Override
    public MyviewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        LayoutInflater mInfrater = LayoutInflater.from(mContext);
        view = mInfrater.inflate(R.layout.attach_stuff_cardview, parent, false);
        return new MyviewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyviewHolder holder, final int position) {

        Picasso.with(mContext).load(mData.get(position).getStuffImage()).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).into(holder.attachStuffImage);
        holder.attachStuffName.setText(mData.get(position).getStuffName());
        holder.attachStuffDescription.setText(mData.get(position).getStuffDescription());
        holder.attachStuffCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Selected Stuff Namelalala =" + mData.get(position).getStuffName());
                selectedStuffName.setText(mData.get(position).getStuffName());
                selectedStuffDescription.setText(mData.get(position).getStuffDescription());
                Picasso.with(mContext).load(mData.get(position).getStuffImage()).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).into(selectedStuffImage);
                selectedStuffID.setText(mData.get(position).getStuffID());
                selectedStuffField.setVisibility(View.VISIBLE);


                dialog.dismiss();
            }
        });

    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public static class MyviewHolder extends RecyclerView.ViewHolder{

        CardView attachStuffCardView;
        ImageView attachStuffImage;
        TextView attachStuffName;
        TextView attachStuffDescription;

        public MyviewHolder(View itemView) {
            super(itemView);

            attachStuffCardView = itemView.findViewById(R.id.attachStuffCardView);
            attachStuffImage = itemView.findViewById(R.id.attachStuffImage);
            attachStuffName = itemView.findViewById(R.id.attachStuffName);
            attachStuffDescription = itemView.findViewById(R.id.attachStuffDescription);
        }
    }
}
