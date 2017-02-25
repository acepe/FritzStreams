package de.acepe.fritzstreams.ui.components;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import de.acepe.fritzstreams.R;
import de.acepe.fritzstreams.backend.StreamInfo;
import de.acepe.fritzstreams.util.Utilities;

public class StreamView extends LinearLayout {

    public static final String TAG = "StreamView";
    private ImageView imageView;
    private TextView category;
    private TextView genre;
    private Button downloadButton;
    private View progressOverlay;

    public StreamView(Context context) {
        super(context);
        init(context, null);
    }

    public StreamView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public StreamView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        View.inflate(context, R.layout.stream_view, this);

        progressOverlay = findViewById(R.id.progress_overlay);
        category = (TextView) findViewById(R.id.tvTitle);
        genre = (TextView) findViewById(R.id.tvSubtitle);
        imageView = (ImageView) findViewById(R.id.ivDownload);
        downloadButton = (Button) findViewById(R.id.btndownload);

        clearStream();
    }

    @Override
    public void setOnClickListener(OnClickListener listener) {
        downloadButton.setOnClickListener(listener);
    }

    public void failed() {
        imageView.setImageResource(R.drawable.sampleimage);
        category.setText(R.string.error);
        genre.setText("");
        downloadButton.setEnabled(true);
        downloadButton.setText(R.string.try_again);

        // Show progress overlay (with animation):
        Utilities.animateView(progressOverlay, View.GONE, 0, 200);
    }

    public void clearStream() {
        imageView.setImageResource(R.drawable.sampleimage);
        category.setText("");
        genre.setText("");
        downloadButton.setEnabled(false);

        // Show progress overlay (with animation):
        Utilities.animateView(progressOverlay, View.VISIBLE, 0.4f, 200);
    }

    public void setStreamInfo(StreamInfo streamInfo) {
        category.setText(streamInfo.getTitle());
        genre.setText(streamInfo.getSubtitle());
        imageView.setImageBitmap(streamInfo.getImage());
        downloadButton.setEnabled(true);
        downloadButton.setText(R.string.download);

        // Hide progress view (with animation):
        Utilities.animateView(progressOverlay, View.GONE, 0, 200);
    }

}