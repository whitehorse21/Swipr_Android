package dk.techtify.swipr.activity.store;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;

import dk.techtify.swipr.R;
import dk.techtify.swipr.adapter.store.PhotoDetailsAdapter;
import dk.techtify.swipr.helper.GlideApp;
import dk.techtify.swipr.view.ActionView;

/**
 * Created by Pavel on 12/25/2016.
 */

public class ProductDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_TITLE = "dk.techtify.swipr.activity.store.ProductDetailsActivity.EXTRA_TITLE";
    public static final String EXTRA_DESCRIPTION = "dk.techtify.swipr.activity.store.ProductDetailsActivity.EXTRA_DESCRIPTION";
    public static final String EXTRA_PHOTOS = "dk.techtify.swipr.activity.store.ProductDetailsActivity.EXTRA_PHOTOS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        final FirebaseStorage storage = FirebaseStorage.getInstance();

        ActionView actionView = findViewById(R.id.action_view);
        actionView.removeMenuButton();
        actionView.setTitle(getIntent().getStringExtra(EXTRA_TITLE).toUpperCase());
        actionView.setActionButton(R.drawable.ic_close, this::onBackPressed);

        final ArrayList<String> list = getIntent().getStringArrayListExtra(EXTRA_PHOTOS);

        final ImageView photo = findViewById(R.id.photo);
        if (!TextUtils.isEmpty(list.get(0))) {
            GlideApp.with(this)
                    .load(storage.getReferenceFromUrl(list.get(0)))
                    .into(photo);
        }

        RecyclerView recycler = findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recycler.setAdapter(new PhotoDetailsAdapter(this, list, v -> {
            if (!TextUtils.isEmpty(list.get((Integer) v.getTag()))) {
                GlideApp.with(ProductDetailsActivity.this)
                        .load(storage.getReferenceFromUrl(list.get((Integer) v.getTag())))
                        .into(photo);
            }
        }));

        TextView description = findViewById(R.id.description);
        description.setText(getIntent().getStringExtra(EXTRA_DESCRIPTION));
        description.setMovementMethod(new ScrollingMovementMethod());
    }
}
