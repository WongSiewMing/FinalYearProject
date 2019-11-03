package com.example.raindown.finalyearproject;
/*Author : Lee Thian Xin
Programme : RSD3
Year : 2018*/

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

public class SearchSellerRecyclerViewAdapter extends RecyclerView.Adapter<SearchSellerRecyclerViewAdapter.MyViewHolder>{
    private Context mContext;
    private List<SearchOB> mData;
    private Student student;

    FragmentManager fragmentManager;


    public SearchSellerRecyclerViewAdapter(Context mContext, List<SearchOB> mData, Student student) {
        this.mContext = mContext;
        this.mData = mData;
        this.student = student;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        LayoutInflater mInflater = LayoutInflater.from(mContext);
        view = mInflater.inflate(R.layout.search_seller_cardview, parent, false);
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
                    ChooseSellerStuff chooseSellerStuff = new ChooseSellerStuff();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("UserData", student);//own data
                    bundle.putString("ClickedUserID", mData.get(position).getUserID());
                    chooseSellerStuff.setArguments(bundle);
                    fragmentManager = ((AppCompatActivity) v.getContext()).getSupportFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.update_fragmentHolder, chooseSellerStuff)
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
            user = itemView.findViewById(R.id.SearchSellerCardView);
            searchUser_photo = itemView.findViewById(R.id.search_seller_photo);
            searchUser_name = itemView.findViewById(R.id.search_seller_name);
        }
    }
}
