package dk.techtify.swipr.activity;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.TextView;

import dk.techtify.swipr.R;
import dk.techtify.swipr.helper.IntentHelper;

/**
 * Created by Pavel on 12/25/2016.
 */

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        findViewById(R.id.click).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IntentHelper.sendEmailViaGmail(SettingsActivity.this, getString(R.string.owner_email), "");
            }
        });

        SpannableString ss = new SpannableString("#KeepThingsSimple");
        ss.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.colorPrimary)), 0,
                ss.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        ((TextView) findViewById(R.id.text)).setText(TextUtils.concat(getResources().getString(R.string.settings_text),
                "\n\n", ss));
    }

    @Override
    public void onBackPressed() {
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        super.onBackPressed();
    }
}
