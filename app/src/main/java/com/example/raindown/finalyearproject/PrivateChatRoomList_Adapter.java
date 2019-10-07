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

import Helper.PrivateChatOB;
import Helper.Student;

public class PrivateChatRoomList_Adapter extends RecyclerView.Adapter<PrivateChatRoomList_Adapter.MyviewHolder>{

    private Context mcontext;
    private List<PrivateChatOB> mData;
    private Student student;
    private FragmentManager fragmentManager;

    public PrivateChatRoomList_Adapter(Context mcontext, List<PrivateChatOB> mData, Student student) {
        this.mcontext = mcontext;
        this.mData = mData;
        this.student = student;
    }

    @Override
    public MyviewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        LayoutInflater mInflater = LayoutInflater.from(mcontext);
        view = mInflater.inflate(R.layout.chatlist_cardview, parent, false);
        return new MyviewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyviewHolder holder, final int position) {
       Picasso.with(mcontext).load(mData.get(position).getRecipient2().getPhoto()).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).transform(new CircleTransform()).into(holder.studentPhoto);
       holder.studentName.setText(mData.get(position).getRecipient2().getStudentName());
       holder.studentMsg.setText(mData.get(position).getMessage());
       holder.studentTime.setText(mData.get(position).getPostTime());
       holder.studentDate.setText(mData.get(position).getPostDate());
       holder.chatListCardView.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               if (mData.get(position).getRecipient2().getStudentID().equals(student.getStudentID())){
                   Toast.makeText(mcontext.getApplicationContext(),"This is your profile", Toast.LENGTH_LONG).show();
               }else {
                   ChatRoom_V2 frag = new ChatRoom_V2();
                   Bundle bundle = new Bundle();
                   bundle.putSerializable("UserData", student);//own data
                   bundle.putString("ClickedUserID", mData.get(position).getRecipient2().getStudentID());
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

    public static class MyviewHolder extends RecyclerView.ViewHolder{
        CardView chatListCardView;
        ImageView studentPhoto;
        TextView studentName;
        TextView studentMsg;
        TextView studentTime;
        TextView studentDate;

        public MyviewHolder(View itemView) {
            super(itemView);
            chatListCardView = itemView.findViewById(R.id.chatList_cardView);
            studentPhoto = itemView.findViewById(R.id.studentPhoto);
            studentName = itemView.findViewById(R.id.studentName);
            studentMsg = itemView.findViewById(R.id.studentMsg);
            studentTime = itemView.findViewById(R.id.studentTime);
            studentDate = itemView.findViewById(R.id.studentDate);
        }
    }
}
