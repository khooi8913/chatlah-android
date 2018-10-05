package chatlah.mobile.info;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;

import chatlah.mobile.GlideApp;
import chatlah.mobile.R;
import chatlah.mobile.info.model.Info;

public class InfoHolder extends RecyclerView.ViewHolder {

    private ImageView infoPostImage;
    private TextView infoPostTitle;
    private TextView infoPostPeriod;
    private TextView infoPostDescription;
    private LinearLayout infoPostSubItem;
    private ImageView infoPostArrow;

    public InfoHolder(@NonNull View itemView) {
        super(itemView);
        infoPostImage = itemView.findViewById(R.id.info_post_image);
        infoPostTitle = itemView.findViewById(R.id.info_post_title);
        infoPostPeriod = itemView.findViewById(R.id.info_post_period);
        infoPostDescription = itemView.findViewById(R.id.info_post_description);

        infoPostSubItem = itemView.findViewById(R.id.info_post_sub_item);
        infoPostArrow = itemView.findViewById(R.id.info_post_arrow);
    }

    public void bind(Info info, Context context) {
        // Glide
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference().getRoot().child(info.getStorage_path());

        infoPostTitle.setText(info.getTitle());
        infoPostPeriod.setText(
                new SimpleDateFormat("dd MMM yyyy").format(info.getCreated_at().toDate())
                        + " ~ "
                        +  new SimpleDateFormat("dd MMM yyyy").format(info.getExpires_on().toDate())
        );

        GlideApp.with(context)
                .load(storageReference)
                .into(infoPostImage);
        infoPostImage.setScaleType(ImageView.ScaleType.FIT_XY);
        infoPostDescription.setText(info.getDescription());

        boolean expanded = info.isExpanded();
        infoPostSubItem.setVisibility(expanded ? View.VISIBLE : View.GONE);
        infoPostArrow.setImageResource(expanded ? R.drawable.ic_arrow_up : R.drawable.ic_arrow_down);
    }

}
