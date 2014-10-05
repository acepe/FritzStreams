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
import android.widget.Toast;
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
    public View getView(int position, View convertView, ViewGroup parent) {
        StreamDownload downloader = App.downloaders.get(position);
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

        downloadViewHolder.cancel.setOnClickListener(oclCancel);
        downloadViewHolder.cancel.setTag(downloader);

        switch (downloader.getState()) {
            case downloading:
                downloadViewHolder.progress.setIndeterminate(true);
                break;
            case converting:
                downloadViewHolder.progress.setIndeterminate(false);
                downloadViewHolder.progress.setMax(100);
                downloadViewHolder.progress.setProgress(downloader.getCurrentProgress());
                break;
            default:
                downloadViewHolder.progress.setVisibility(View.INVISIBLE);
        }

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

    private View.OnClickListener oclCancel = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Toast.makeText(mContext, R.string.download_noti_canceled, Toast.LENGTH_SHORT).show();

            StreamDownload downloader = (StreamDownload) v.getTag();
            downloader.cancel();
            notifyDataSetChanged();
        }
    };
}
