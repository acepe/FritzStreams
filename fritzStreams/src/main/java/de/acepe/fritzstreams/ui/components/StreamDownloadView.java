package de.acepe.fritzstreams.ui.components;

import java.io.File;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.widget.*;
import de.acepe.fritzstreams.R;
import de.acepe.fritzstreams.backend.DownloadInfo;

public class StreamDownloadView extends LinearLayout {

    public interface Action {
        void execute(DownloadInfo t);
    }

    public static final String TAG = "StreamDownloadView";
    private ImageView mImageView;
    private TextView mCategory;
    private TextView mGenre;
    private ImageButton mCancelButton;
    private ProgressBar mDownloadProgress;
    private DownloadInfo mDownload;

    public StreamDownloadView(Context context) {
        super(context);
        init(context, null);
    }

    public StreamDownloadView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public StreamDownloadView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        View.inflate(context, R.layout.stream_view_download, this);

        mCategory = (TextView) findViewById(R.id.tvStreamCategory);
        mGenre = (TextView) findViewById(R.id.tvStreamGenre);
        mImageView = (ImageView) findViewById(R.id.ivDownload);
        mCancelButton = (ImageButton) findViewById(R.id.btnCancelDownload);
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
        mCategory.setText(download.getTitle());
        mGenre.setText(download.getSubtitle());
        // mImageView.setImageBitmap(download.getImage());

        boolean isDownloading = download.getState() == DownloadInfo.State.downloading;
        if (isDownloading) {
            mDownloadProgress.setProgress(download.getProgressPercent());
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