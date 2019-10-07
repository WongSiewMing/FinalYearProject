package com.example.raindown.finalyearproject;
/*Author : Lee Thian Xin
Programme : RSD3
Year : 2018*/

import android.content.Context;
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
import android.widget.Toast;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;


import java.util.List;
import Helper.SearchOB;
import Helper.Student;

public class SearchUserRecyclerViewAdapter extends RecyclerView.Adapter<SearchUserRecyclerViewAdapter.MyViewHolder>{
    private Context mContext;
    private List<SearchOB> mData;
    private Student student;

    FragmentManager fragmentManager;


    public SearchUserRecyclerViewAdapter(Context mContext, List<SearchOB> mData, Student student) {
        this.mContext = mContext;
        this.mData = mData;
        this.student = student;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        LayoutInflater mInflater = LayoutInflater.from(mContext);
        view = mInflater.inflate(R.layout.search_user_cardview, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        Picasso.with(mContext).load(mData.get(position).getUserImage()).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).into(holder.searchUser_photo);
        holder.searchUser_name.setText(mData.get(position).getUserName());
        holder.user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mData.get(position).getUserID().equals(student.getStudentID())){
                    Toast.makeText(mContext.getApplicationContext(),"This is your profile", Toast.LENGTH_LONG).show();
                }else {
                    ChatRoom_V2 frag = new ChatRoom_V2();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("UserData", student);//own data
                    bundle.putString("ClickedUserID", mData.get(position).getUserID());
                    frag.setArguments(bundle);
                    fragmentManager = ((AppCompatActivity) v.getContext()).getSupportFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.update_fragmentHolder, frag)
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .addToBackStack(null)
                            .commit();
                }

            }
        });

    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{

        CardView user;
        ImageView searchUser_photo;
        TextView searchUser_name;

        public MyViewHolder(View itemView) {
            super(itemView);
            user = itemView.findViewById(R.id.SearchUserCardView);
            searchUser_photo = itemView.findViewById(R.id.search_user_photo);
            searchUser_name = itemView.findViewById(R.id.search_user_name);
        }
    }
}
