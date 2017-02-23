package de.acepe.fritzstreams.ui.components;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.*;
import de.acepe.fritzstreams.R;
import de.acepe.fritzstreams.backend.DownloadInfo;
import de.acepe.fritzstreams.backend.ProgressInfo;

public class StreamDownloadView extends LinearLayout {

    public interface Action {
        void execute(DownloadInfo t, ProgressInfo.State s);
    }

    public static final String TAG = "StreamDownloadView";
    private ImageView mImageView;
    private TextView mCategory;
    private TextView mGenre;
    private ImageButton mCancelButton;
    private ProgressBar mDownloadProgress;
    private ProgressInfo mProgressInfo;
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
    }

    public void setButtonAction(final Action action) {
        mCancelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                action.execute(mDownload, mProgressInfo.getState());
            }
        });
    }

    public void setDownload(DownloadInfo download) {
        mDownload = download;
        mCategory.setText(download.getTitle());
        mGenre.setText(download.getSubtitle());
        // mImageView.setImageBitmap(download.getImage());
        mProgressInfo = new ProgressInfo(download.getStreamURL(), 0, 0, 0, ProgressInfo.State.waiting);
    }

    public void setProgress(ProgressInfo progressInfo) {
        mProgressInfo = progressInfo;
        switch (progressInfo.getState()) {
            case downloading:
                mDownloadProgress.setProgress(progressInfo.getProgressPercent());
                break;
            default:
                mDownloadProgress.setVisibility(View.INVISIBLE);
        }
    }

    public ProgressInfo getProgressInfo() {
        return mProgressInfo;
    }

    public DownloadInfo getDownload() {
        return mDownload;
    }
}