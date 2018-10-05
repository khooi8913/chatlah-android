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

    private FirebaseUser firebaseUser;
    private FirebaseFirestore firestore;

    private RecyclerView infoPosts;
    private FirestoreRecyclerOptions<Info> options;
    private FirestoreRecyclerAdapter infoPostsAdapter;

    private BroadcastReceiver broadcastReceiver;
    private IntentFilter intentFilter;

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

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "Get Info!");
                getInfoPosts();
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
        Query query = firestore.collection("chatRooms")
                .document(SharedPreferencesSingleton.getSharedPrefStringVal(SharedPreferencesSingleton.CONVERSATION_ZONE))
                .collection("info")
                .orderBy("expires_on", Query.Direction.DESCENDING);

//        Query query = firestore.collection("chatRooms")
//                .document("FSKTM")
//                .collection("info")
//                .orderBy("expires_on", Query.Direction.DESCENDING);

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
                emptyView.setVisibility(View.GONE);
            }
        };

        LinearLayoutManager infoPostLayout = new LinearLayoutManager(getActivity());
        infoPostLayout.setOrientation(LinearLayoutManager.VERTICAL);

        ((SimpleItemAnimator) infoPosts.getItemAnimator()).setSupportsChangeAnimations(true);

        infoPosts.setLayoutManager(infoPostLayout);
        infoPosts.setAdapter(infoPostsAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "Info Loaded");
        getActivity().registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(broadcastReceiver);
    }
}
