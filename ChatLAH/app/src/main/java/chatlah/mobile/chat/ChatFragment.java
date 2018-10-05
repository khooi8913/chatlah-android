package chatlah.mobile.chat;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
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
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONException;
import org.json.JSONObject;

import chatlah.mobile.R;
import chatlah.mobile.SharedPreferencesSingleton;
import chatlah.mobile.chat.model.ChatMessage;

public class ChatFragment extends Fragment {

    private Context mContext;
    private String TAG = getClass().getSimpleName();

    private EditText userMessage;
    private ImageView sendMessage;
    private LinearLayout startChatting;
    private LinearLayout chatlahNotAvailable;
    private LinearLayout locationDisabled;

    private FirebaseUser firebaseUser;
    private FirebaseFirestore firestore;

    private RecyclerView chatRecords;
    private FirestoreRecyclerOptions<ChatMessage> options;
    private FirestoreRecyclerAdapter chatRecordsAdapter;

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;

    private boolean requestingLocation = false;

    private final int SHOW_CHAT_MESSAGES = 0;
    private final int CHAT_LAH_UNAVAILABLE = 1;
    private final int LOCATION_SERVICES_DISABLED = 2;
    private final int NO_CHAT_MESSAGES = 3;

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

        SharedPreferencesSingleton.getInstance(mContext);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        setUpLocationCallback();
        createLocationRequest();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_chat, container, false);
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            // Scroll chat to last message when keyboard is activated
            boolean isOpened = false;

            @Override
            public void onGlobalLayout() {
                int heightDiff = view.getRootView().getHeight() - view.getHeight();
                if (heightDiff > 100) { // 99% of the time the height diff will be due to a keyboard.
                    if (!isOpened) {
                        if (chatRecordsAdapter != null)
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

        chatRecords = getActivity().findViewById(R.id.recycler_chat_messages);
        startChatting = getActivity().findViewById(R.id.view_start_chatting);
        chatlahNotAvailable = getActivity().findViewById(R.id.view_chatlah_not_available);
        locationDisabled = getActivity().findViewById(R.id.view_location_disabled);

        userMessage = getActivity().findViewById(R.id.edit_text_user_message);
        sendMessage = getActivity().findViewById(R.id.button_send_message);
        sendMessage.setClickable(true); // Need to solve this
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

                firestore.collection("chatRooms")
                        .document(SharedPreferencesSingleton.getSharedPrefStringVal(SharedPreferencesSingleton.CONVERSATION_ZONE))
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
    }

    private void getChatMessages() {

        // Load up to 30 minutes before
        Query query = firestore.collection("chatRooms")
                .document(SharedPreferencesSingleton.getSharedPrefStringVal(SharedPreferencesSingleton.CONVERSATION_ZONE))
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .startAt(new Timestamp(
                        Long.parseLong(SharedPreferencesSingleton.getSharedPrefStringVal(SharedPreferencesSingleton.CHAT_SESSION_START))- 1800, 0));

        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                if (chatRecordsAdapter != null)
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
                if (chatMessage.getSender().equals(firebaseUser.getUid()))
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
                } else {
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
                        ((ReceivedChatMessageHolder) holder).bind(chatMessage);
                        break;
                    case VIEW_TYPE_MESSAGE_SENT:
                        ((SentChatMessageHolder) holder).bind(chatMessage);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onDataChanged() {
                super.onDataChanged();
                if (this.getItemCount() > 0) {
                    setViewToDisplay(SHOW_CHAT_MESSAGES);
                }
            }
        };

        LinearLayoutManager chatRecordsLayout = new LinearLayoutManager(getActivity());
        chatRecordsLayout.setOrientation(LinearLayoutManager.VERTICAL);
        chatRecordsLayout.setReverseLayout(false);
        chatRecordsLayout.setStackFromEnd(true);
        chatRecords.setLayoutManager(chatRecordsLayout);
        chatRecords.setAdapter(chatRecordsAdapter);
        if (chatRecordsAdapter != null){
            chatRecords.scrollToPosition(chatRecordsAdapter.getItemCount() - 1);
        }

        if (chatRecordsAdapter.getItemCount() == 0) {
            setViewToDisplay(NO_CHAT_MESSAGES);
        }
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(180000);
        mLocationRequest.setFastestInterval(180000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void startLocationUpdates() throws SecurityException {
        if(!requestingLocation){
            requestingLocation = true;

            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                    mLocationCallback,
                    null /* Looper */);
        }
    }

    private void stopLocationUpdates() {
        requestingLocation = false;
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    private void setUpLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {

                requestingLocation = false;

                if (locationResult == null) {
                    Toast.makeText(mContext, "Location information not available...", Toast.LENGTH_SHORT).show();
                    return;
                }

                Location lastKnwonLocation = locationResult.getLastLocation();
                double latitude = lastKnwonLocation.getLatitude();
                double longitude = lastKnwonLocation.getLongitude();

                Log.d(TAG, latitude + "," + longitude);
                requestGeofenceInfo(latitude, longitude);
            }

        };
    }

    private void requestGeofenceInfo(double latitude, double longitude) {
        String geoFenceApiUrl = getString(R.string.geofence_api_url) + latitude + "," + longitude;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, geoFenceApiUrl, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.d(TAG, response.toString());

                    if (response.getBoolean("in_fence")) {


                        String fence_id = response.getString("fence_id");

                        // Compare with the current one
                        String current_fence_id = SharedPreferencesSingleton.getSharedPrefStringVal(SharedPreferencesSingleton.CONVERSATION_ZONE);

                        // Trying not to let it be null
                        if (current_fence_id == null) current_fence_id = "";

                        if (current_fence_id.equals(fence_id)) {
                            // Do nothing since location not changed
                            if(chatRecordsAdapter.getItemCount()>0) setViewToDisplay(SHOW_CHAT_MESSAGES);
                        } else {
                            // Have to clear the chat messages
                            chatRecords.setAdapter(null);
                            sendMessage.setClickable(false);
                            SharedPreferencesSingleton.setSharedPrefStringVal(SharedPreferencesSingleton.CONVERSATION_ZONE, fence_id);
                            SharedPreferencesSingleton.setSharedPrefStringVal(SharedPreferencesSingleton.CHAT_SESSION_START, System.currentTimeMillis() / 1000L + "");

                            // Get Messages Here
                            getChatMessages();
                            sendMessage.setClickable(true);

                            // Broadcast to notify change
                            Intent intent = new Intent();
                            intent.setAction("chatlah.mobile.LOCATION_CHANGED");
                            if (getActivity() != null){
                                getActivity().sendBroadcast(intent);
                            }
                        }
                    } else {
                        // Not in fence
                        sendMessage.setClickable(false);
                        chatRecords.setAdapter(null);
                        SharedPreferencesSingleton.clearSharedPrefs();

                        // Tell user that ChatLAH is not available.
                        setViewToDisplay(CHAT_LAH_UNAVAILABLE);

                        // Broadcast to notify change
                        Intent intent = new Intent();
                        intent.setAction("chatlah.mobile.LOCATION_CHANGED");
                        getActivity().sendBroadcast(intent);
                    }
                } catch (JSONException e) {
                    Log.d(TAG, e.toString());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // What should we do here a?
                Log.e(TAG, error.toString());
            }
        });
        Volley.newRequestQueue(mContext).add(jsonObjectRequest);
    }

    private void setViewToDisplay(int viewNumber) {
        switch (viewNumber) {
            default:
            case SHOW_CHAT_MESSAGES:
                chatRecords.setVisibility(View.VISIBLE);
                chatlahNotAvailable.setVisibility(View.INVISIBLE);
                startChatting.setVisibility(View.INVISIBLE);
                locationDisabled.setVisibility(View.INVISIBLE);
                break;
            case CHAT_LAH_UNAVAILABLE:
                chatRecords.setVisibility(View.INVISIBLE);
                chatlahNotAvailable.setVisibility(View.VISIBLE);
                startChatting.setVisibility(View.INVISIBLE);
                locationDisabled.setVisibility(View.INVISIBLE);
                break;
            case LOCATION_SERVICES_DISABLED:
                chatRecords.setVisibility(View.INVISIBLE);
                chatlahNotAvailable.setVisibility(View.INVISIBLE);
                startChatting.setVisibility(View.INVISIBLE);
                locationDisabled.setVisibility(View.VISIBLE);
                break;
            case NO_CHAT_MESSAGES:
                chatRecords.setVisibility(View.INVISIBLE);
                chatlahNotAvailable.setVisibility(View.INVISIBLE);
                startChatting.setVisibility(View.VISIBLE);
                locationDisabled.setVisibility(View.INVISIBLE);
                break;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    public void onResume() {
        super.onResume();
        startLocationUpdates();
        if (chatRecordsAdapter != null){
            chatRecords.scrollToPosition(chatRecordsAdapter.getItemCount() - 1);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
