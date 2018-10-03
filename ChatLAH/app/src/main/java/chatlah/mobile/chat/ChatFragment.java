package chatlah.mobile.chat;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import chatlah.mobile.R;
import chatlah.mobile.chat.model.ChatMessage;

public class ChatFragment extends Fragment {

    private Context mContext;
    private String TAG = getClass().getSimpleName();

    private EditText userMessage;
    private ImageView sendMessage;

    private FirebaseUser firebaseUser;
    private FirebaseFirestore firestore;

    private RecyclerView chatRecords;
    private FirestoreRecyclerOptions<ChatMessage> options;
    private FirestoreRecyclerAdapter chatRecordsAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity().getApplicationContext();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firestore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        firestore.setFirestoreSettings(settings);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_chat, container, false);
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            boolean isOpened = false;
            @Override
            public void onGlobalLayout() {
                int heightDiff = view.getRootView().getHeight() - view.getHeight();
                if (heightDiff > 100) { // 99% of the time the height diff will be due to a keyboard.
                    if (!isOpened) {
                        chatRecords.scrollToPosition(chatRecordsAdapter.getItemCount());
                    }
                    isOpened = true;
                } else if (isOpened) {
                    isOpened = false;
                }
            }
        });
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        chatRecords = getActivity().findViewById(R.id.recycler_view_chat_messages);

        chatRecords = getActivity().findViewById(R.id.recycler_view_chat_messages);
        userMessage = getActivity().findViewById(R.id.edit_text_user_message);
        sendMessage = getActivity().findViewById(R.id.button_send_message);
        sendMessage.setClickable(true);
        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (userMessage.getText().toString().equals("") || userMessage.toString().isEmpty()) {
                    return;
                }

                ChatMessage chatMessage = new ChatMessage(
                        firebaseUser.getUid(),
                        userMessage.getText().toString().trim(),
                        new Timestamp(System.currentTimeMillis() / 1000L, 0)
                );

                // TODO: Send message
                firestore.collection("chatRooms")
                        .document("MV")
                        .collection("messages")
                        .add(chatMessage);

                Log.d(TAG, "Message sent!");

                // Clear EditText
                userMessage.setText("");
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        getChatMessages();
    }

    private void getChatMessages() {
        Query query = firestore.collection("chatRooms")
                .document("MV")
                .collection("messages")
                .orderBy("timestamp");

        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                chatRecords.scrollToPosition(chatRecordsAdapter.getItemCount());
            }
        });

        options = new FirestoreRecyclerOptions.Builder<ChatMessage>()
                .setQuery(query, ChatMessage.class)
                .setLifecycleOwner(this)
                .build();

        chatRecordsAdapter = new FirestoreRecyclerAdapter<ChatMessage, ChatMessageHolder>(options) {

            private final int VIEW_TYPE_MESSAGE_RECEIVED = 0;
            private final int VIEW_TYPE_MESSAGE_SENT = 1;

            @Override
            public int getItemViewType(int position) {
                ChatMessage chatMessage = this.getItem(position);
                if(chatMessage.getSender().equals(firebaseUser.getUid()))
                    return VIEW_TYPE_MESSAGE_SENT;
                else return VIEW_TYPE_MESSAGE_RECEIVED;
            }

            @NonNull
            @Override
            public ChatMessageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view;

                if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
                    view = LayoutInflater.from(parent.getContext()).inflate(
                            R.layout.message_received,
                            parent,
                            false
                    );
                    return new ReceivedChatMessageHolder(view);
                }else {
                    view = LayoutInflater.from(parent.getContext()).inflate(
                            R.layout.message_sent,
                            parent,
                            false
                    );
                    return new SentChatMessageHolder(view);
                }
            }

            @Override
            protected void onBindViewHolder(@NonNull ChatMessageHolder holder, int position, @NonNull ChatMessage chatMessage) {
                switch (holder.getItemViewType()) {
                    case VIEW_TYPE_MESSAGE_RECEIVED:
                        ((ReceivedChatMessageHolder)holder).bind(chatMessage);
                        break;
                    case VIEW_TYPE_MESSAGE_SENT:
                        ((SentChatMessageHolder)holder).bind(chatMessage);
                        break;
                    default:
                        break;
                }
            }
        };

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        linearLayoutManager.setReverseLayout(false);
        linearLayoutManager.setStackFromEnd(true);
        chatRecords.setLayoutManager(linearLayoutManager);
        chatRecords.setAdapter(chatRecordsAdapter);
        chatRecords.scrollToPosition(chatRecordsAdapter.getItemCount());
    }
}
