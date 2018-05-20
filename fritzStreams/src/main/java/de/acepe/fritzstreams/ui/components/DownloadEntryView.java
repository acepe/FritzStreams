package de.acepe.fritzstreams.ui.components;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.util.AttributeSet;
import android.view.View;
import android.widget.*;
import de.acepe.fritzstreams.BuildConfig;
import de.acepe.fritzstreams.R;
import de.acepe.fritzstreams.backend.DownloadInfo;
import de.acepe.fritzstreams.backend.DownloadState;

import java.io.File;

import static de.acepe.fritzstreams.backend.DownloadState.DOWNLOADING;
import static de.acepe.fritzstreams.backend.DownloadState.FINISHED;

public class DownloadEntryView extends LinearLayout {

    public interface Action {
        void execute(DownloadInfo t);
    }

    public static final String TAG = "StreamDownloadView";
    private TextView mTitle;
    private TextView mSubTitle;
    private TextView mState;
    private TextView mPlay;
    private Button mCancelButton;
    private ProgressBar mDownloadProgress;
    private DownloadInfo mDownload;

    public DownloadEntryView(Context context) {
        super(context);
        init(context);
    }

    public DownloadEntryView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DownloadEntryView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        Typeface font = Typeface.createFromAsset(context.getAssets(), "fontawesome-webfont.ttf");
        View.inflate(context, R.layout.download_entry_view, this);

        mTitle = findViewById(R.id.tvTitle);
        mSubTitle = findViewById(R.id.tvSubtitle);
        mState = findViewById(R.id.tvState);
        mPlay = findViewById(R.id.tvPlay);
        mPlay.setTypeface(font);
        mCancelButton = findViewById(R.id.btnCancelDownload);
        mCancelButton.setTypeface(font);
        mDownloadProgress = findViewById(R.id.pbDownloadProgress);
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDownload.getState() == DownloadState.FINISHED) {
                    DownloadEntryView.this.openWithMusicApp();
                }
            }
        });
    }

    public void setButtonAction(final Action action) {
        mCancelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                action.execute(mDownload);
            }
        });
    }

    public void setDownload(DownloadInfo download) {
        mDownload = download;
        mTitle.setText(download.getTitle());
        mSubTitle.setText(download.getSubtitle());

        DownloadState state = download.getState();
        switch (state) {
            case WAITING:
                mCancelButton.setText(R.string.icon_remove);
                mState.setText(R.string.waiting);
                break;
            case DOWNLOADING:
                mDownloadProgress.setProgress(download.getProgressPercent());
                break;
            case CANCELLED:
                mState.setText(R.string.cancelled);
                mCancelButton.setText(R.string.icon_retry);
                break;
            case FAILED:
                mState.setText(R.string.failed);
                mCancelButton.setText(R.string.icon_retry);
                break;
            case FINISHED:
                mState.setText(R.string.finished);
                mCancelButton.setText(R.string.icon_accept);
                break;
        }
        mDownloadProgress.setVisibility(state == DOWNLOADING ? View.VISIBLE : View.INVISIBLE);
        mState.setVisibility(state == DOWNLOADING ? View.INVISIBLE : View.VISIBLE);
        mPlay.setVisibility(state == FINISHED ? View.VISIBLE : View.INVISIBLE);
    }

    public DownloadInfo getDownload() {
        return mDownload;
    }

    private void openWithMusicApp() {
        Uri outFile = FileProvider.getUriForFile(getContext(), BuildConfig.APPLICATION_ID + ".provider", new File(mDownload.getFilename()));

        Intent mediaIntent = new Intent();
        mediaIntent.setAction(Intent.ACTION_VIEW);
        mediaIntent.setDataAndType(outFile, "audio/*");
        mediaIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_GRANT_READ_URI_PERMISSION);

        if (mediaIntent.resolveActivity(getContext().getPackageManager()) != null) {
            getContext().startActivity(mediaIntent);
        } else {
            Toast.makeText(getContext(), R.string.app_not_available, Toast.LENGTH_LONG).show();
        }
    }
}