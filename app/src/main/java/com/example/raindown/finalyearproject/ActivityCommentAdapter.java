package com.example.raindown.finalyearproject;
/*Author : Lee Thian Xin
Programme : RSD3
Year : 2018*/

import android.content.Context;
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

import Helper.ActivityCommentOB;

public class ActivityCommentAdapter extends RecyclerView.Adapter<ActivityCommentAdapter.MyviewHolder>{
    private Context mContext;
    private List<ActivityCommentOB> mData;

    public ActivityCommentAdapter(Context mContext, List<ActivityCommentOB> mData) {
        this.mContext = mContext;
        this.mData = mData;
    }

    @Override
    public MyviewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        LayoutInflater mInflater = LayoutInflater.from(mContext);
        view = mInflater.inflate(R.layout.activity_comment_cardview, parent, false);

        return new MyviewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyviewHolder holder, int position) {
        Picasso.with(mContext).load(mData.get(position).getStudentID().getPhoto()).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).transform(new CircleTransform()).into(holder.CommenterPhoto);
        holder.CommenterDate.setText(mData.get(position).getCommentDate() + " on " + mData.get(position).getCommentTime());
        holder.CommenterComment.setText(mData.get(position).getCommentText());
        holder.CommenterName.setText(mData.get(position).getStudentID().getStudentName());

    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public static class MyviewHolder extends RecyclerView.ViewHolder{

        CardView CommentCardView;
        ImageView CommenterPhoto;
        TextView CommenterName, CommenterDate, CommenterComment;

        public MyviewHolder(View itemView) {
            super(itemView);
            CommentCardView = itemView.findViewById(R.id.CommentCardView);
            CommenterPhoto = itemView.findViewById(R.id.CommenterPhoto);
            CommenterName = itemView.findViewById(R.id.CommenterName);
            CommenterDate = itemView.findViewById(R.id.CommenterDate);
            CommenterComment = itemView.findViewById(R.id.CommenterComment);
        }
    }
}
