package com.example.raindown.finalyearproject;
/*Author : Lee Thian Xin
Programme : RSD3
Year : 2018*/

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

import Helper.StoreRatingOB;

public class ReviewListRecyclerViewAdapter extends RecyclerView.Adapter<ReviewListRecyclerViewAdapter.MyviewHolder>{
    private Context mContext;
    private List<StoreRatingOB> mData;

    public ReviewListRecyclerViewAdapter(Context mContext, List<StoreRatingOB> mData) {
        this.mContext = mContext;
        this.mData = mData;
    }

    @Override
    public MyviewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        LayoutInflater mInflater = LayoutInflater.from(mContext);
        view = mInflater.inflate(R.layout.reviewlist_cardview, parent, false);
        return new MyviewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyviewHolder holder, int position) {

        Picasso.with(mContext).load(mData.get(position).getStudentImage()).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).transform(new CircleTransform()).into(holder.respondentPhoto);
        holder.respondentName.setText(mData.get(position).getStudentName());
        holder.respondentTime.setText(mData.get(position).getRateTime());
        holder.respondentDate.setText(mData.get(position).getRateDate());
        holder.respondentRate.setRating(Float.parseFloat(mData.get(position).getRateValue()));
        holder.respondentComment.setText(mData.get(position).getComments());

    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public static class MyviewHolder extends RecyclerView.ViewHolder{

        ImageView respondentPhoto;
        TextView respondentName;
        TextView respondentTime;
        TextView respondentDate;
        RatingBar respondentRate;
        TextView respondentComment;

        public MyviewHolder(View itemView) {
            super(itemView);

            respondentPhoto = (ImageView) itemView.findViewById(R.id.respondentPhoto);
            respondentName = (TextView) itemView.findViewById(R.id.respondentName);
            respondentTime = (TextView) itemView.findViewById(R.id.respondentTime);
            respondentDate = (TextView) itemView.findViewById(R.id.respondentDate);
            respondentRate = (RatingBar) itemView.findViewById(R.id.respondentRate);
            respondentComment = (TextView) itemView.findViewById(R.id.respondentComment);
        }
    }
}
