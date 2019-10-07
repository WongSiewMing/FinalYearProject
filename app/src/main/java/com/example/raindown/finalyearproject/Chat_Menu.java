package com.example.raindown.finalyearproject;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import Helper.ChatOption;
import Helper.Student;

public class Chat_Menu extends Fragment {

    public final static List<ChatOption> arrayChatOption = new ArrayList<>();
    FragmentManager fragmentManager;
    View view;
    Student s = null;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity a;
        if (context instanceof Activity) {
            a = (Activity) context;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle("Chat Menu");
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.chat_menu, container, false);
        Bundle bundle = getArguments();
        s = (Student) bundle.getSerializable("ChatMenu");
        populateArrayChatOption();
        registerClickCallBack();
        return view;
    }

    public void populateArrayChatOption() {
        arrayChatOption.clear();
        arrayChatOption.add(0, new ChatOption(R.mipmap.icon_private, "Private Chat"));
        arrayChatOption.add(1, new ChatOption(R.mipmap.icon_public, "Public Chat"));
        populateListView();
    }

    public void populateListView() {
        ArrayAdapter<ChatOption> adapter = new MyListAdapter();
        ListView list = (ListView) view.findViewById(R.id.chatOptionList);
        list.setAdapter(adapter);
    }

    public class MyListAdapter extends ArrayAdapter<ChatOption> {
        public MyListAdapter() {
            super(getActivity(), R.layout.templatechat, arrayChatOption);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View itemView = convertView;
            if (itemView == null) {
                itemView = getActivity().getLayoutInflater().inflate(R.layout.templatechat, parent, false);
            }

            ChatOption currentOption = arrayChatOption.get(position);

            ImageView optionIcon = (ImageView) itemView.findViewById(R.id.chatOptionIcon);
            optionIcon.setImageResource(currentOption.getChatOption());

            TextView optionName = (TextView) itemView.findViewById(R.id.chatOptionName);
            optionName.setText(currentOption.getChatOptionName());

            return itemView;

        }
    }

    public void registerClickCallBack() {
        ListView list = (ListView) view.findViewById(R.id.chatOptionList);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked, int position, long id) {

                if (arrayChatOption.get(position).getChatOptionName().equals("Private Chat")) {
                    ChatRoomList_V2 chatRoomList_v2 = new ChatRoomList_V2();
                    Bundle bundle1 = new Bundle();
                    bundle1.putSerializable("chatRoomList_v2", s);
                    chatRoomList_v2.setArguments(bundle1);
                    fragmentManager = getFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.update_fragmentHolder, chatRoomList_v2)
                            .addToBackStack(null)
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .commit();

                } else if (arrayChatOption.get(position).getChatOptionName().equals("Public Chat")) {
                    PublicList publicList = new PublicList();
                    Bundle bundle2 = new Bundle();
                    bundle2.putSerializable("PublicList", s);
                    publicList.setArguments(bundle2);
                    fragmentManager = getFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.update_fragmentHolder, publicList)
                            .addToBackStack(null)
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .commit();
                }
            }
        });
    }
}
