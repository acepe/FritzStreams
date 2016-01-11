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
import de.acepe.fritzstreams.backend.StreamDownload;

public class DownloadAdapter extends ArrayAdapter<StreamDownload> {

    private final Context mContext;

    public DownloadAdapter(Context context, int resource, List<StreamDownload> data) {
        super(context, resource, data);
        mContext = context;
    }

    @Override
    public View getView(int position, View downloadRow, ViewGroup parent) {
        StreamDownload downloader = App.downloader.getDownload(position);
        DownloadViewHolder downloadViewHolder;

        if (downloadRow == null) {
            downloadRow = ((Activity) mContext).getLayoutInflater().inflate(R.layout.download_row, parent, false);

            downloadViewHolder = new DownloadViewHolder();

            downloadViewHolder.title = (TextView) downloadRow.findViewById(R.id.tvDlTitle);
            downloadViewHolder.subtitle = (TextView) downloadRow.findViewById(R.id.tvDlSubtitle);
            downloadViewHolder.progress = (ProgressBar) downloadRow.findViewById(R.id.pbDlProgress);
            downloadViewHolder.progress.setIndeterminate(false);
            downloadViewHolder.progress.setMax(100);

            downloadViewHolder.cancel = (ImageButton) downloadRow.findViewById(R.id.ibCancelDl);

            downloadRow.setTag(downloadViewHolder);
        } else {
            downloadViewHolder = (DownloadViewHolder) downloadRow.getTag();
        }

        downloadRow.setBackgroundResource(R.drawable.selector_selected);

        downloadViewHolder.cancel.setOnClickListener(oclCancel);
        downloadViewHolder.cancel.setTag(downloader);

        switch (downloader.getState()) {
            case downloading:
                downloadViewHolder.progress.setProgress(downloader.getCurrentProgress());
                break;
            default:
                downloadViewHolder.progress.setVisibility(View.INVISIBLE);
        }

        downloadViewHolder.title.setText(downloader.getTitle());
        downloadViewHolder.subtitle.setText(downloader.getSubtitle());

        return downloadRow;
    }

    private class DownloadViewHolder {
        TextView title;
        TextView subtitle;
        ProgressBar progress;
        ImageButton cancel;
    }

    private View.OnClickListener oclCancel = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            StreamDownload download = (StreamDownload) v.getTag();
            App.downloader.cancelDownload(download);
            notifyDataSetChanged();
        }
    };
}
