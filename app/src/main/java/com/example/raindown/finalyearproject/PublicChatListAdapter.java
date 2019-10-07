package com.example.raindown.finalyearproject;
/*Author : Lee Thian Xin
Programme : RSD3
Year : 2018*/

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
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

import Helper.Room;
import Helper.Student;

public class PublicChatListAdapter extends RecyclerView.Adapter<PublicChatListAdapter.MyViewHolder> {
    private Context mContext;
    private List<Room> mData;
    private Student student;
    private FragmentManager fragmentManager;

    public PublicChatListAdapter(Context mContext, List<Room> mData, Student student) {
        this.mContext = mContext;
        this.mData = mData;
        this.student = student;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        LayoutInflater mInflater = LayoutInflater.from(mContext);
        view = mInflater.inflate(R.layout.publicchatlist_cardview, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        Picasso.with(mContext).load(mData.get(position).getPhoto()).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).into(holder.roomPhoto);
        holder.roomSubject.setText(mData.get(position).getSubject());
        if (!mData.get(position).getCheckParticipant().equals("0")){
            holder.participationStatus.setText("Joining");
        }

        holder.publicListCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Room clickedRoom = mData.get(position);

                PublicChatRoom_V2 frag = new PublicChatRoom_V2();
                Bundle bundle = new Bundle();
                bundle.putSerializable("clickedRoom", clickedRoom);
                bundle.putSerializable("myInfo", student);// my own data
                frag.setArguments(bundle);
                fragmentManager = ((AppCompatActivity) v.getContext()).getSupportFragmentManager();
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

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView roomPhoto;
        TextView roomSubject, participationStatus;
        CardView publicListCardView;

        public MyViewHolder(View itemView) {
            super(itemView);
            roomPhoto = itemView.findViewById(R.id.roomPhoto);
            roomSubject = itemView.findViewById(R.id.roomSubject);
            participationStatus = itemView.findViewById(R.id.participationStatus);
            publicListCardView = itemView.findViewById(R.id.publicChatListCardView);
        }
    }
}
