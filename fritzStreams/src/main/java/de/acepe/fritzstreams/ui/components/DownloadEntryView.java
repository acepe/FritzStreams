package de.acepe.fritzstreams.ui.components;

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

        mTitle = (TextView) findViewById(R.id.tvStreamCategory);
        mSubTitle = (TextView) findViewById(R.id.tvStreamGenre);
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

        boolean isDownloading = download.getState() == DownloadInfo.State.downloading;
        if (isDownloading) {
            mDownloadProgress.setProgress(download.getProgressPercent());
        } else if (download.getState() != DownloadInfo.State.waiting) {
            mCancelButton.setText(R.string.icon_remove);
        }
        mDownloadProgress.setVisibility(isDownloading ? View.VISIBLE : View.INVISIBLE);
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