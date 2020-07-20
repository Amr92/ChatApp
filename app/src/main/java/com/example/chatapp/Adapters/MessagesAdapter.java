package com.example.chatapp.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.Activities.ImageViewer;
import com.example.chatapp.Activities.MainActivity;
import com.example.chatapp.Models.Messages;
import com.example.chatapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessagesViewHolder> {

    private List<Messages> userMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    public MessagesAdapter(List<Messages> userMessagesList) {
        this.userMessagesList = userMessagesList;
    }

    @NonNull
    @Override
    public MessagesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_messages_layout,parent,false);
        mAuth = FirebaseAuth.getInstance();
        return new MessagesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessagesViewHolder holder, final int position) {
        String messageSenderId = mAuth.getCurrentUser().getUid();
        Messages messages = userMessagesList.get(position);
        String fromUserId = messages.getFrom();
        String fromMessageType = messages.getType();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserId);
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("image")){
                    String receiverImage = dataSnapshot.child("image").getValue().toString();
                    Picasso.get().load(receiverImage).placeholder(R.drawable.profile_image).into(holder.receiverProfileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        holder.receiverMessageText.setVisibility(View.GONE);
        holder.receiverProfileImage.setVisibility(View.GONE);
        holder.senderMessageText.setVisibility(View.GONE);
        holder.messageSenderImage.setVisibility(View.GONE);
        holder.messageReceiverImage.setVisibility(View.GONE);

        if(fromMessageType.equals("text")){
            if(fromUserId.equals(messageSenderId)){
                holder.senderMessageText.setVisibility(View.VISIBLE);
                holder.senderMessageText.setBackgroundResource(R.drawable.sender_messages_layout);
                holder.senderMessageText.setTextColor(Color.BLACK);
                holder.senderMessageText.setText(messages.getMessage() +"\n \n" + messages.getTime() + " - " + messages.getDate());
            }
            else{
                holder.receiverProfileImage.setVisibility(View.VISIBLE);
                holder.receiverMessageText.setVisibility(View.VISIBLE);

                holder.receiverMessageText.setBackgroundResource(R.drawable.receiver_messages_layout);
                holder.receiverMessageText.setTextColor(Color.BLACK);
                holder.receiverMessageText.setText(messages.getMessage() +"\n \n" + messages.getTime() + " - " + messages.getDate());
            }
        }
        else if(fromMessageType.equals("image")){
            if(fromUserId.equals(messageSenderId)){
                holder.messageSenderImage.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage()).into(holder.messageSenderImage);
            }
            else{
                holder.messageReceiverImage.setVisibility(View.VISIBLE);
                holder.receiverProfileImage.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage()).into(holder.messageReceiverImage);
            }

        }
        else if(fromMessageType.equals("pdf") || fromMessageType.equals("docx")){
            if(fromUserId.equals(messageSenderId)){
                holder.messageSenderImage.setVisibility(View.VISIBLE);
                holder.messageSenderImage.setBackgroundResource(R.drawable.file);

            }
            else {
                holder.messageReceiverImage.setVisibility(View.VISIBLE);
                holder.receiverProfileImage.setVisibility(View.VISIBLE);
                holder.messageReceiverImage.setBackgroundResource(R.drawable.file);

            }
        }

        if(fromUserId.equals(messageSenderId)){
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(userMessagesList.get(position).getType().equals("pdf") || userMessagesList.get(position).getType().equals("docx")){
                        CharSequence options[] = new CharSequence[]{
                                "Delete for me",
                                "Download and view this document",
                                "Cancel","Delete for everyone"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                if(i == 0){
                                    deleteSentMessage(position,holder);
                                    Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }
                                else if(i == 1){
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                                    holder.itemView.getContext().startActivity(intent);
                                }
                                else if(i == 3){
                                    deleteMessageForEveryone(position,holder);
                                    Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });

                        builder.show();
                    }
                    else if(userMessagesList.get(position).getType().equals("text")){
                        CharSequence options[] = new CharSequence[]{
                                "Delete for me",
                                "Cancel","Delete for everyone"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                if(i == 0){
                                    deleteSentMessage(position,holder);
                                    Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }
                                else if(i == 2){
                                    deleteMessageForEveryone(position,holder);
                                    Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });

                        builder.show();
                    }
                    else if(userMessagesList.get(position).getType().equals("image")){
                        CharSequence options[] = new CharSequence[]{
                                "Delete for me",
                                "View this image",
                                "Cancel","Delete for everyone"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                if(i == 0){
                                    deleteSentMessage(position,holder);
                                    Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }
                                else if(i == 1){
                                    Intent intent = new Intent(holder.itemView.getContext(), ImageViewer.class);
                                    intent.putExtra("url",userMessagesList.get(position).getMessage());
                                    holder.itemView.getContext().startActivity(intent);
                                }
                                else if(i == 3){
                                    deleteMessageForEveryone(position,holder);
                                    Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });

                        builder.show();
                    }
                }

            });
        }
        else {

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(userMessagesList.get(position).getType().equals("pdf") || userMessagesList.get(position).getType().equals("docx")){
                        CharSequence options[] = new CharSequence[]{
                                "Delete for me",
                                "Download and view this document",
                                "Cancel"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                if(i == 0){
                                    deleteReceivedMessage(position,holder);
                                    Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }
                                else if(i == 1){
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                                    holder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });

                        builder.show();
                    }
                    else if(userMessagesList.get(position).getType().equals("text")){
                        CharSequence options[] = new CharSequence[]{
                                "Delete for me",
                                "Cancel"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                if(i == 0){
                                    deleteReceivedMessage(position,holder);
                                    Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });

                        builder.show();
                    }
                    else if(userMessagesList.get(position).getType().equals("image")){
                        CharSequence options[] = new CharSequence[]{
                                "Delete for me",
                                "View this image",
                                "Cancel"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                if(i == 0){
                                    deleteReceivedMessage(position,holder);
                                    Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                    holder.itemView.getContext().startActivity(intent);
                                }
                                else if(i == 1){
                                    Intent intent = new Intent(holder.itemView.getContext(), ImageViewer.class);
                                    intent.putExtra("url",userMessagesList.get(position).getMessage());
                                    holder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });

                        builder.show();
                    }
                }

            });
        }
    }

    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }

    private void deleteSentMessage(final int position,final MessagesViewHolder holder){
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages").child(userMessagesList.get(position).getFrom()).child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getMessageID()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(holder.itemView.getContext(), "Deleted Successfully..", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(holder.itemView.getContext(), "Error occurred..", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void deleteReceivedMessage(final int position,final MessagesViewHolder holder){
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages").child(userMessagesList.get(position).getTo()).child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getMessageID()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(holder.itemView.getContext(), "Deleted Successfully..", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(holder.itemView.getContext(), "Error occurred..", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void deleteMessageForEveryone(final int position,final MessagesViewHolder holder){
        final DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages").child(userMessagesList.get(position).getTo()).child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getMessageID()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    rootRef.child("Messages").child(userMessagesList.get(position).getFrom()).child(userMessagesList.get(position).getTo())
                            .child(userMessagesList.get(position).getMessageID()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(holder.itemView.getContext(), "Deleted Successfully..", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }
                else{
                    Toast.makeText(holder.itemView.getContext(), "Error occurred..", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public class MessagesViewHolder extends RecyclerView.ViewHolder{

        TextView senderMessageText,receiverMessageText;
        CircleImageView receiverProfileImage;
        ImageView messageSenderImage,messageReceiverImage;

        public MessagesViewHolder(@NonNull View itemView) {
            super(itemView);

            senderMessageText = itemView.findViewById(R.id.sender_message_text);
            receiverMessageText = itemView.findViewById(R.id.receiver_message_text);
            receiverProfileImage = itemView.findViewById(R.id.message_profile_image);
            messageSenderImage = itemView.findViewById(R.id.message_sender_image);
            messageReceiverImage = itemView.findViewById(R.id.message_receiver_image);

        }
    }
}
