package chatlah.mobile.info;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import chatlah.mobile.R;
import chatlah.mobile.SharedPreferencesSingleton;
import chatlah.mobile.info.model.Info;

public class InfoFragment extends Fragment {

    private Context mContext;
    private String TAG = getClass().getSimpleName();

    private LinearLayout emptyView;
    private FirebaseFirestore firestore;

    private RecyclerView infoPosts;
    private FirestoreRecyclerOptions<Info> options;
    private FirestoreRecyclerAdapter infoPostsAdapter;

    private BroadcastReceiver broadcastReceiver;
    private IntentFilter intentFilter;

    private final int SHOW_INFO = 0;
    private final int NO_INFO_AVAILABLE = 1;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity().getApplicationContext();
        firestore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        firestore.setFirestoreSettings(settings);

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle bundle = intent.getExtras();
                int geofenceEvent = bundle.getInt("GeofenceEvent");

                if(geofenceEvent == 0) {    // SAME ZONE

                    if(infoPostsAdapter.getItemCount()>0){
                        setViewToDisplay(SHOW_INFO);
                    }

                } else if(geofenceEvent == 1) { // NEW ZONE
                    infoPosts.setAdapter(null);
                    getInfoPosts();

                } else if(geofenceEvent == 2) { // OUT OF ZONE
                    infoPosts.setAdapter(null);
                    setViewToDisplay(NO_INFO_AVAILABLE);
                } else if(geofenceEvent == 3) { // OUT OF ZONE
                    infoPosts.setAdapter(null);
                    setViewToDisplay(NO_INFO_AVAILABLE);
                }
            }
        };
        intentFilter = new IntentFilter();
        intentFilter.addAction("chatlah.mobile.LOCATION_CHANGED");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_info, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        infoPosts = getActivity().findViewById(R.id.recycler_info);
        emptyView = getActivity().findViewById(R.id.empty_view);
    }

    private void getInfoPosts() {
//        if(SharedPreferencesSingleton.getSharedPrefStringVal(SharedPreferencesSingleton.CONVERSATION_ZONE) == null){
//            setViewToDisplay(NO_INFO_AVAILABLE);
//            infoPostsAdapter = null;
//            infoPosts.setAdapter(null);
//            return;
//        }

        Query query = firestore.collection("chatRooms")
                .document(SharedPreferencesSingleton.getSharedPrefStringVal(SharedPreferencesSingleton.CONVERSATION_ZONE))
                .collection("info")
                .orderBy("expires_on", Query.Direction.DESCENDING);

        options = new FirestoreRecyclerOptions.Builder<Info>()
                .setQuery(query, Info.class)
                .setLifecycleOwner(this)
                .build();

        infoPostsAdapter = new FirestoreRecyclerAdapter<Info, InfoHolder>(options) {

            @NonNull
            @Override
            public InfoHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
                View view = LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.info_post,
                        parent,
                        false
                );
                return new InfoHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull InfoHolder holder, final int position, @NonNull Info info) {
                final Info infoPost = getItem(position);

                holder.bind(info, getContext());

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        boolean expanded = infoPost.isExpanded();
                        infoPost.setExpanded(!expanded);
                        notifyItemChanged(position);
                    }
                });
            }

            @Override
            public void onDataChanged() {
                super.onDataChanged();
                if(getItemCount() > 0){
                    setViewToDisplay(SHOW_INFO);
                }else {
                    setViewToDisplay(NO_INFO_AVAILABLE);
                }
            }
        };

        LinearLayoutManager infoPostLayout = new LinearLayoutManager(getActivity());
        infoPostLayout.setOrientation(LinearLayoutManager.VERTICAL);

        ((SimpleItemAnimator) infoPosts.getItemAnimator()).setSupportsChangeAnimations(true);

        infoPosts.setLayoutManager(infoPostLayout);
        infoPosts.setAdapter(infoPostsAdapter);
    }

    private void setViewToDisplay(int viewNumber) {
        switch (viewNumber) {
            default:
            case SHOW_INFO:
                emptyView.setVisibility(View.INVISIBLE);
                infoPosts.setVisibility(View.VISIBLE);
                break;
            case NO_INFO_AVAILABLE:
                emptyView.setVisibility(View.VISIBLE);
                infoPosts.setVisibility(View.INVISIBLE);
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        if(broadcastReceiver!=null){
            getActivity().unregisterReceiver(broadcastReceiver);
        }
    }
}
