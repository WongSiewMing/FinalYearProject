package com.example.raindown.finalyearproject;
/*Author : Lee Thian Xin
Programme : RSD3
Year : 2018*/

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.List;
import Helper.MenuOption;
import Helper.Student;

import android.support.v4.app.FragmentManager;
import android.widget.ImageView;
import android.widget.TextView;

public class MenuRecycleViewAdapter extends RecyclerView.Adapter<MenuRecycleViewAdapter.MyviewHolder>{

    private Context mContext;
    private List<MenuOption> mData;
    private Student userData;
    FragmentManager fragmentManager;
    View view;

    public MenuRecycleViewAdapter(Context mContext, List<MenuOption> mData, Student userData) {
        this.mContext = mContext;
        this.mData = mData;
        this.userData = userData;
    }

    @Override
    public MyviewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater mInflater = LayoutInflater.from(mContext);
        view = mInflater.inflate(R.layout.menu_cardview, parent, false);
        return new MyviewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyviewHolder holder, final int position) {
        holder.menuImage.setImageResource(mData.get(position).getMenuImage());
        holder.menuName.setText(mData.get(position).getMenuName());
        holder.menu_CardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                loadSelection(position);
            }
        });

    }

    private void loadSelection(int i) {
        switch (i) {
            case 0:
                Home home = new Home();
                Bundle bundle1 = new Bundle();
                bundle1.putSerializable("Home", userData);
                home.setArguments(bundle1);

                fragmentManager = ((AppCompatActivity) view.getContext()).getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.fragmentHolder, home)
                        .addToBackStack(null)
                        .commit();
                break;

            case 1:
                MyProfile myProfile = new MyProfile();
                Bundle bundle2 = new Bundle();
                bundle2.putSerializable("MyProfile", userData);
                myProfile.setArguments(bundle2);
                fragmentManager = ((AppCompatActivity) view.getContext()).getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.fragmentHolder, myProfile)
                        .addToBackStack(null)
                        .commit();
                break;

            case 2:
                FavouriteList favouriteList = new FavouriteList();
                Bundle bundle3 = new Bundle();
                bundle3.putSerializable("FavouriteList", userData);
                favouriteList.setArguments(bundle3);
                fragmentManager = ((AppCompatActivity) view.getContext()).getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.fragmentHolder, favouriteList)
                        .addToBackStack(null)
                        .commit();
                break;

            case 3:
                FollowList followingList = new FollowList();
                Bundle bundle4 = new Bundle();
                bundle4.putSerializable("FollowList", userData);
                followingList.setArguments(bundle4);
                fragmentManager = ((AppCompatActivity) view.getContext()).getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.fragmentHolder, followingList)
                        .addToBackStack(null)
                        .commit();
                break;

            case 4:
                ChatRoomList_V2 chatRoomList_v2 = new ChatRoomList_V2();
                Bundle bundle = new Bundle();
                bundle.putSerializable("chatRoomList_v2", userData);
                chatRoomList_v2.setArguments(bundle);
                fragmentManager = ((AppCompatActivity) view.getContext()).getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.fragmentHolder, chatRoomList_v2)
                        .addToBackStack(null)
                        .commit();
                break;

            case 5:
                PublicList publicList = new PublicList();
                Bundle bundle6 = new Bundle();
                bundle6.putSerializable("PublicList", userData);
                publicList.setArguments(bundle6);
                fragmentManager = ((AppCompatActivity) view.getContext()).getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.fragmentHolder, publicList)
                        .addToBackStack(null)
                        .commit();
                break;
            case 6:
                UserActivity userActivity = new UserActivity();
                Bundle bundle7 = new Bundle();
                bundle7.putSerializable("UserActivity", userData);
                userActivity.setArguments(bundle7);
                fragmentManager = ((AppCompatActivity) view.getContext()).getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.fragmentHolder, userActivity)
                        .addToBackStack(null)
                        .commit();
                break;


            case 7:
                ManageAppointment manageAppointment = new ManageAppointment();
                Bundle bundle8 = new Bundle();
                bundle8.putSerializable("manageAppointment", userData);
                manageAppointment.setArguments(bundle8);
                fragmentManager = ((AppCompatActivity) view.getContext()).getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.fragmentHolder, manageAppointment)
                        .addToBackStack(null)
                        .commit();

                break;

            case 8:
                ManageRequest manageRequest = new ManageRequest();
                fragmentManager = ((AppCompatActivity) view.getContext()).getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.fragmentHolder, manageRequest)
                        .addToBackStack(null)
                        .commit();

                break;

            case 9:
                StoreCategory storeList = new StoreCategory();
                fragmentManager = ((AppCompatActivity) view.getContext()).getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.fragmentHolder, storeList)
                        .addToBackStack(null)
                        .commit();
                break;

            case 10:
                FeedbackForm feedbackForm = new FeedbackForm();
                fragmentManager = ((AppCompatActivity) view.getContext()).getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.fragmentHolder, feedbackForm)
                        .addToBackStack(null)
                        .commit();
                break;



//            case 10:
//                PublicGroupList_V2 publicGroupList_v2 = new PublicGroupList_V2();
//                Bundle bundle5 = new Bundle();
//                bundle5.putSerializable("publicGroupList_v2", userData);
//                publicGroupList_v2.setArguments(bundle5);
//                fragmentManager = ((AppCompatActivity) view.getContext()).getSupportFragmentManager();
//                fragmentManager.beginTransaction()
//                        .replace(R.id.fragmentHolder, publicGroupList_v2)
//                        .addToBackStack(null)
//                        .commit();
//                break;


//            case 6:
//                AboutUs aboutUs = new AboutUs();
//                fragmentManager = ((AppCompatActivity) view.getContext()).getSupportFragmentManager();
//                fragmentManager.beginTransaction()
//                        .replace(R.id.fragmentHolder, aboutUs)
//                        .addToBackStack(null)
//                        .commit();
//                break;



        }
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public static class MyviewHolder extends RecyclerView.ViewHolder{
        ImageView menuImage;
        TextView menuName;
        CardView menu_CardView;

        public MyviewHolder(View itemView) {
            super(itemView);

            menuImage = itemView.findViewById(R.id.menu_image);
            menuName = itemView.findViewById(R.id.menu_name);
            menu_CardView = itemView.findViewById(R.id.menuCardView);
        }
    }
}
