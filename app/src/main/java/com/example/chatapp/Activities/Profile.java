package com.example.chatapp.Activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class Profile extends AppCompatActivity {

    @BindView(R.id.visit_user_name)
    TextView visitUserName;
    @BindView(R.id.visit_profile_status)
    TextView visitProfileStatus;
    @BindView(R.id.send_message_request_button)
    Button sendMessageRequestButton;
    @BindView(R.id.decline_message_request_button)
    Button declineMessageRequestButton;
    private String receiverUserId, senderUserId, current_state;
    private CircleImageView userProfileImage;

    private DatabaseReference userRef, chatRequestRef,contactsRef,notificationRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);

        mAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        chatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        notificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications");

        receiverUserId = getIntent().getStringExtra("visit_user_id");
        senderUserId = mAuth.getCurrentUser().getUid();

        userProfileImage = findViewById(R.id.visit_profile_image);

        retrieveUserInfo();
        current_state = "new";

    }

    private void retrieveUserInfo() {
        userRef.child(receiverUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("image"))) {
                    String userImage = dataSnapshot.child("image").getValue().toString();
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();

                    visitUserName.setText(userName);
                    visitProfileStatus.setText(userStatus);
                    Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(userProfileImage);

                    manageChatRequests();
                } else {
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();

                    visitUserName.setText(userName);
                    visitProfileStatus.setText(userStatus);
                    manageChatRequests();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void manageChatRequests() {
        chatRequestRef.child(senderUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(receiverUserId)) {
                    String request_type = dataSnapshot.child(receiverUserId).child("request_type").getValue().toString();
                    if (request_type.equals("sent")) {
                        current_state = "request_sent";
                        sendMessageRequestButton.setText("Cancel Chat Request");
                    }else if(request_type.equals("received")){
                        current_state = "request_received";
                        sendMessageRequestButton.setText("Accept chat Request");
                        declineMessageRequestButton.setVisibility(View.VISIBLE);
                        declineMessageRequestButton.setEnabled(true);

                        declineMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                cancelChatRequest();
                            }
                        });
                    }
                }else{
                    contactsRef.child(senderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild(receiverUserId)){
                                current_state = "friends";
                                sendMessageRequestButton.setText("Remove this Contact");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if (!senderUserId.equals(receiverUserId)) {
            sendMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendMessageRequestButton.setEnabled(false);
                    if (current_state.equals("new")) {
                        sendChatRequest();
                    }
                    if (current_state.equals("request_sent")) {
                        cancelChatRequest();
                    }
                    if (current_state.equals("request_received")) {
                        acceptChatRequest();
                    }
                    if (current_state.equals("friends")) {
                        removeSpecificContact();
                    }
                }
            });
        } else {
            sendMessageRequestButton.setVisibility(View.INVISIBLE);
        }
    }

    private void sendChatRequest() {

        chatRequestRef.child(senderUserId).child(receiverUserId).child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            chatRequestRef.child(receiverUserId).child(senderUserId).child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                HashMap<String,String> chatNotificationMap = new HashMap<>();
                                                chatNotificationMap.put("from",senderUserId);
                                                chatNotificationMap.put("type","request");
                                                notificationRef.child(receiverUserId).push().setValue(chatNotificationMap)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful()){
                                                                    sendMessageRequestButton.setEnabled(true);
                                                                    current_state = "request_sent";
                                                                    sendMessageRequestButton.setText("Cancel Chat Request");
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void cancelChatRequest() {
        chatRequestRef.child(senderUserId).child(receiverUserId).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            chatRequestRef.child(receiverUserId).child(senderUserId).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                sendMessageRequestButton.setEnabled(true);
                                                current_state = "new";
                                                sendMessageRequestButton.setText("Send Chat Request");

                                                declineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                declineMessageRequestButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void acceptChatRequest() {

        contactsRef.child(senderUserId).child(receiverUserId).child("Contacts").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            contactsRef.child(receiverUserId).child(senderUserId).child("Contacts").setValue("Saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                chatRequestRef.child(senderUserId).child(receiverUserId)
                                                        .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()){
                                                            chatRequestRef.child(receiverUserId).child(senderUserId)
                                                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    sendMessageRequestButton.setEnabled(true);
                                                                    current_state = "friends";
                                                                    sendMessageRequestButton.setText("Remove this Contact");

                                                                    declineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                                    declineMessageRequestButton.setEnabled(false);
                                                                }
                                                            });
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void removeSpecificContact() {
        contactsRef.child(senderUserId).child(receiverUserId).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            contactsRef.child(receiverUserId).child(senderUserId).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                sendMessageRequestButton.setEnabled(true);
                                                current_state = "new";
                                                sendMessageRequestButton.setText("Send Message");

                                                declineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                declineMessageRequestButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }
}
