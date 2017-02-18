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

public class StreamView extends LinearLayout {

    public static final String TAG = "StreamView";
    ImageView imageView;
    TextView category;
    TextView genre;
    Button downloadButton;

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
        setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
        category = (TextView) findViewById(R.id.tvStreamCategory);
        genre = (TextView) findViewById(R.id.tvStreamGenre);
        imageView = (ImageView) findViewById(R.id.ivDownload);
        downloadButton = (Button) findViewById(R.id.btndownload);
    }

    public void setOnClickListener(OnClickListener listener) {
        downloadButton.setOnClickListener(listener);
    }

    public void clearStream() {
        imageView.setImageResource(R.drawable.sampleimage);
        category.setText("");
        genre.setText("");
        downloadButton.setEnabled(false);
    }

    public void setEnabled(boolean enabled) {
        downloadButton.setEnabled(enabled);
    }

    public void setStreamInfo(StreamInfo streamInfo) {
        category.setText(streamInfo.getTitle());
        genre.setText(streamInfo.getSubtitle());
        imageView.setImageBitmap(streamInfo.getImage());
        downloadButton.setEnabled(true);
    }
}