package com.example.chatapp.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.chatapp.Activities.GroupChat;
import com.example.chatapp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class GroupsFragment extends Fragment {

    private View groupFragmentView;
    private ListView listView;
    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String> listOfGroups = new ArrayList<>();
    private DatabaseReference groupRef;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        groupFragmentView = inflater.inflate(R.layout.fragment_groups, container, false);

        groupRef = FirebaseDatabase.getInstance().getReference().child("Groups");

        listView = groupFragmentView.findViewById(R.id.list_view);
        arrayAdapter = new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1,listOfGroups);
        listView.setAdapter(arrayAdapter);

        retrieveAndDisplayGroups();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String currentGroupName = parent.getItemAtPosition(position).toString();
                Intent groupChatIntent = new Intent(getContext(), GroupChat.class);
                groupChatIntent.putExtra("groupName",currentGroupName);
                startActivity(groupChatIntent);
            }
        });
        return groupFragmentView;
    }

    private void retrieveAndDisplayGroups() {
        groupRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Set<String> set = new HashSet<>();
                Iterator iterator = dataSnapshot.getChildren().iterator();
                while (iterator.hasNext()){
                    set.add(((DataSnapshot)iterator.next()).getKey());
                }
                listOfGroups.clear();
                listOfGroups.addAll(set);
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}