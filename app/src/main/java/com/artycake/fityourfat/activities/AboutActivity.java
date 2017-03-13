package com.artycake.fityourfat.activities;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.TextView;

import com.artycake.fityourfat.BuildConfig;
import com.artycake.fityourfat.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AboutActivity extends AppCompatActivity {

    @BindView(R.id.app_info)
    TextView appInfo;
    @BindView(R.id.rate_text)
    TextView rateText;
    @BindView(R.id.developer_email)
    TextView developerEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        ButterKnife.bind(this);
        final String versionInfo = getResources().getString(R.string.app_info, getResources().getString(R.string.app_name), BuildConfig.VERSION_NAME);
        appInfo.setText(versionInfo);
        developerEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uriText =
                        "mailto:" + getResources().getString(R.string.developer_email) +
                                "?subject=" + Uri.encode(versionInfo);

                Uri uri = Uri.parse(uriText);

                Intent sendIntent = new Intent(Intent.ACTION_SENDTO);
                sendIntent.setData(uri);
                startActivity(Intent.createChooser(sendIntent, "Contact developer"));
            }
        });

        String before = getResources().getString(R.string.rate_text_before);
        String rate = getResources().getString(R.string.rate_text_rate);
        String after = getResources().getString(R.string.rate_text_after);
        String text = before + rate + after;
        Spannable spannable = new SpannableString(text);
        int color;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            color = getResources().getColor(R.color.colorAccent, getTheme());
        } else {
            color = getResources().getColor(R.color.colorAccent);
        }
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                ds.setUnderlineText(false);
            }
        };
        spannable.setSpan(new ForegroundColorSpan(color), before.length(), before.length() + rate.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(clickableSpan, before.length(), before.length() + rate.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        rateText.setText(spannable, TextView.BufferType.SPANNABLE);
    }
}
