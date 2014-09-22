package de.acepe.fritzstreams.ui;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import de.acepe.fritzstreams.App;
import de.acepe.fritzstreams.R;
import de.acepe.fritzstreams.backend.StreamDownloader;

public class DownloadAdapter extends ArrayAdapter<StreamDownloader> {

    private final Context mContext;

    public DownloadAdapter(Context context, int resource, List<StreamDownloader> data) {
        super(context, resource, data);
        mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        StreamDownloader downloader = App.downloaders.get(position);
        DownloadViewHolder downloadViewHolder;

        if (convertView == null) {
            convertView = ((Activity) mContext).getLayoutInflater().inflate(R.layout.download_row, parent, false);

            downloadViewHolder = new DownloadViewHolder();

            downloadViewHolder.title = (TextView) convertView.findViewById(R.id.tvDlTitle);
            downloadViewHolder.subtitle = (TextView) convertView.findViewById(R.id.tvDlSubtitle);
            downloadViewHolder.progress = (ProgressBar) convertView.findViewById(R.id.pbDlProgress);
            downloadViewHolder.cancel = (ImageButton) convertView.findViewById(R.id.ibCancelDl);

            convertView.setTag(downloadViewHolder);
        } else {
            downloadViewHolder = (DownloadViewHolder) convertView.getTag();
        }

        convertView.setBackgroundResource(R.drawable.selector_selected);

        // downloadViewHolder.cancel.setOnClickListener(oclCancel);
        downloadViewHolder.cancel.setTag(downloader);

        downloadViewHolder.progress.setMax(100);
        downloadViewHolder.progress.setProgress(downloader.getCurrentProgress());
        downloadViewHolder.title.setText(downloader.getTitle());
        downloadViewHolder.subtitle.setText(downloader.getSubtitle());

        return convertView;
    }

    private class DownloadViewHolder {
        TextView title;
        TextView subtitle;
        ProgressBar progress;
        ImageButton cancel;
    }

}
