package de.acepe.fritzstreams.ui.fragments;

import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import de.acepe.fritzstreams.R;
import de.acepe.fritzstreams.backend.DownloadInfo;
import de.acepe.fritzstreams.backend.DownloadServiceAdapter;
import de.acepe.fritzstreams.ui.components.StreamDownloadView;

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

        return view;
    }

    @Override
    public void onPause() {
        mDownloaderSupplier.getDownloader().removeResultReceiver(this);
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mDownloaderSupplier.getDownloader().registerResultReceiver(this);
        mDownloaderSupplier.getDownloader().queryDownloadInfos();
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
    public void currentProgress(DownloadInfo downloadInfo) {
        if (!downloadViews.containsKey(downloadInfo)) {
            addDownload(downloadInfo);
        }
        StreamDownloadView view = downloadViews.get(downloadInfo);
        view.setDownload(downloadInfo);

        // long freeSpaceExternal = (long) Utilities.getFreeSpaceExternal();
        // mFreeSpace.setText(getActivity().getString(R.string.download_freespace,
        // Utilities.humanReadableBytes(freeSpaceExternal, false)));
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
        public void execute(DownloadInfo downloadInfo) {
            switch (downloadInfo.getState()) {
                case waiting:
                case failed:
                case cancelled:
                case finished:
                    mDownloaderSupplier.getDownloader().removeDownload(downloadInfo);
                case downloading:
                    mDownloaderSupplier.getDownloader().cancelDownload(downloadInfo);
                    break;
            }
        }
    }

}
