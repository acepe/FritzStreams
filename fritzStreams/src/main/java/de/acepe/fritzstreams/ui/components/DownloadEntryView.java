package de.acepe.fritzstreams.ui.components;

import static de.acepe.fritzstreams.backend.DownloadInfo.State.downloading;

import java.io.File;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.widget.*;
import de.acepe.fritzstreams.R;
import de.acepe.fritzstreams.backend.DownloadInfo;

public class DownloadEntryView extends LinearLayout {

    public interface Action {
        void execute(DownloadInfo t);
    }

    public static final String TAG = "StreamDownloadView";
    private TextView mTitle;
    private TextView mSubTitle;
    private TextView mState;
    private Button mCancelButton;
    private ProgressBar mDownloadProgress;
    private DownloadInfo mDownload;

    public DownloadEntryView(Context context) {
        super(context);
        init(context, null);
    }

    public DownloadEntryView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public DownloadEntryView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        Typeface font = Typeface.createFromAsset(context.getAssets(), "fontawesome-webfont.ttf");
        View.inflate(context, R.layout.download_entry_view, this);

        mTitle = (TextView) findViewById(R.id.tvTitle);
        mSubTitle = (TextView) findViewById(R.id.tvSubtitle);
        mState = (TextView) findViewById(R.id.tvState);
        mCancelButton = (Button) findViewById(R.id.btnCancelDownload);
        mCancelButton.setTypeface(font);
        mDownloadProgress = (ProgressBar) findViewById(R.id.pbDownloadProgress);

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDownload.getState() == DownloadInfo.State.finished) {
                    openWithMusicApp();
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

        DownloadInfo.State state = download.getState();
        switch (state) {
            case waiting:
                mCancelButton.setText(R.string.icon_remove);
                mState.setText(R.string.waiting);
                break;
            case downloading:
                mDownloadProgress.setProgress(download.getProgressPercent());
                break;
            case cancelled:
                mState.setText(R.string.cancelled);
                mCancelButton.setText(R.string.icon_retry);
                break;
            case failed:
                mState.setText(R.string.failed);
                mCancelButton.setText(R.string.icon_retry);
                break;
            case finished:
                mState.setText(R.string.finished);
                mCancelButton.setText(R.string.icon_accept);
                break;
        }
        mDownloadProgress.setVisibility(state == downloading ? View.VISIBLE : View.INVISIBLE);
        mState.setVisibility(state == downloading ? View.INVISIBLE : View.VISIBLE);
    }

    public DownloadInfo getDownload() {
        return mDownload;
    }

    private void openWithMusicApp() {
        Uri outFile = Uri.fromFile(new File(mDownload.getFilename()));

        Intent mediaIntent = new Intent();
        mediaIntent.setAction(Intent.ACTION_VIEW);
        mediaIntent.setDataAndType(outFile, "audio/*");
        mediaIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        if (mediaIntent.resolveActivity(getContext().getPackageManager()) != null) {
            getContext().startActivity(mediaIntent);
        } else {
            Toast.makeText(getContext(), R.string.app_not_available, Toast.LENGTH_LONG).show();
        }
    }
}