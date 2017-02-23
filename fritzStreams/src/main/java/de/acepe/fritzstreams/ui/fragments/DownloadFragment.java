package de.acepe.fritzstreams.ui.fragments;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import de.acepe.fritzstreams.R;
import de.acepe.fritzstreams.backend.DownloadInfo;
import de.acepe.fritzstreams.backend.DownloadServiceAdapter;
import de.acepe.fritzstreams.backend.ProgressInfo;
import de.acepe.fritzstreams.ui.components.StreamDownloadView;
import de.acepe.fritzstreams.util.Utilities;

public class DownloadFragment extends Fragment implements DownloadServiceAdapter.ResultReceiver {

    public interface DownloadServiceAdapterSupplier {
        DownloadServiceAdapter getDownloader();
    }

    private final HashMap<DownloadInfo, StreamDownloadView> downloadViews = new HashMap<>();

    private LinearLayout mDownloadsContainer;
    private TextView mFreeSpace;
    private View view;
    private DownloadServiceAdapterSupplier mDownloaderSupplier;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mDownloaderSupplier = (DownloadServiceAdapterSupplier) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.download_fragment, container, false);

        mDownloadsContainer = (LinearLayout) view.findViewById(R.id.llDownloadsContainer);
        mFreeSpace = (TextView) view.findViewById(R.id.downloads_freespace);

        setHasOptionsMenu(true);

        mDownloaderSupplier.getDownloader().registerResultReceiver(this);
        mDownloaderSupplier.getDownloader().queryDownloadInfos();
        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mDownloaderSupplier = null;
    }

    @Override
    public void downloadsInQueue(List<DownloadInfo> downloads) {
        mDownloadsContainer.removeAllViews();
        for (DownloadInfo download : downloads) {
            addDownload(download);
        }
    }

    @Override
    public void currentProgress(ProgressInfo progressInfo) {
        for (DownloadInfo downloadInfo : downloadViews.keySet()) {
            if (downloadInfo.getStreamURL().equals(progressInfo.getUrl())) {
                StreamDownloadView view = downloadViews.get(downloadInfo);
                view.setProgress(progressInfo);

//                long freeSpaceExternal = (long) Utilities.getFreeSpaceExternal();
//                mFreeSpace.setText(getActivity().getString(R.string.download_freespace,
//                                                           Utilities.humanReadableBytes(freeSpaceExternal, false)));
                return;
            }
        }
    }

    private void addDownload(DownloadInfo download) {
        StreamDownloadView downloadView = new StreamDownloadView(view.getContext());
        downloadView.setDownload(download);
        mDownloadsContainer.addView(downloadView);

        downloadViews.put(download, downloadView);
        downloadView.setButtonAction(new CancelOrOpenAction());
    }

    private class CancelOrOpenAction implements StreamDownloadView.Action {
        @Override
        public void execute(DownloadInfo downloadInfo, ProgressInfo.State state) {
            if (state == ProgressInfo.State.downloading || state == ProgressInfo.State.waiting) {
                mDownloaderSupplier.getDownloader().cancelDownload(downloadInfo);
            } else if (state == ProgressInfo.State.finished) {

                Uri outFile = Uri.fromFile(new File(downloadInfo.getFilename()));

                Intent mediaIntent = new Intent();
                mediaIntent.setAction(Intent.ACTION_VIEW);
                mediaIntent.setDataAndType(outFile, "audio/*");
                mediaIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                if (mediaIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(mediaIntent);
                } else {
                    Toast.makeText(getActivity(), R.string.app_not_available, Toast.LENGTH_LONG).show();
                }

            }
        }
    }

}
