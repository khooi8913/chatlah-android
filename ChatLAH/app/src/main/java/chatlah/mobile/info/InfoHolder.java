package chatlah.mobile.info;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
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

    public InfoHolder(@NonNull View itemView) {
        super(itemView);
        infoPostImage = itemView.findViewById(R.id.info_post_image);
        infoPostTitle = itemView.findViewById(R.id.info_post_title);
        infoPostPeriod = itemView.findViewById(R.id.info_post_period);
    }

    public void bind(Info info, Context context) {
        // Glide
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReferenceFromUrl("gs://chatlah-464f2.appspot.com/banner/11b49c9a-7448-4bea-a8c3-bcc7d04c1148");

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
    }

}
