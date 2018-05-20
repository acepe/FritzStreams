package de.acepe.fritzstreams.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import de.acepe.fritzstreams.R;
import de.acepe.fritzstreams.backend.DownloadInfo;
import de.acepe.fritzstreams.backend.DownloadServiceAdapter;
import de.acepe.fritzstreams.ui.components.DownloadEntryView;
import de.acepe.fritzstreams.util.Utilities;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class DownloadFragment extends Fragment implements DownloadServiceAdapter.ResultReceiver {

    private static final long UPDATE_INTERVAL_IN_MS = 3000;

    public interface DownloadServiceAdapterSupplier {
        DownloadServiceAdapter getDownloader();
    }

    private final HashMap<DownloadInfo, DownloadEntryView> downloadViews = new HashMap<>();

    private LinearLayout mDownloadsContainer;
    private TextView mFreeSpace;
    private View view;
    private DownloadServiceAdapterSupplier mDownloaderSupplier;
    private long lastUpdateFreeSpace;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mDownloaderSupplier = (DownloadServiceAdapterSupplier) context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.download_fragment, container, false);

        mDownloadsContainer = view.findViewById(R.id.llDownloadsContainer);
        mFreeSpace = view.findViewById(R.id.downloads_freespace);

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
        updateFreeSpace();
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
    public void updateProgress(DownloadInfo downloadInfo) {
        if (!downloadViews.containsKey(downloadInfo)) {
            addDownload(downloadInfo);
        }
        DownloadEntryView view = downloadViews.get(downloadInfo);
        view.setDownload(downloadInfo);

        long now = Calendar.getInstance().getTimeInMillis();
        if (lastUpdateFreeSpace == 0 || lastUpdateFreeSpace < now - UPDATE_INTERVAL_IN_MS) {
            updateFreeSpace();
            lastUpdateFreeSpace = now;
        }
    }

    private void updateFreeSpace() {
        long freeSpaceExternal = Utilities.getFreeSpaceExternal();
        mFreeSpace.setText(getActivity().getString(R.string.download_freespace,
                Utilities.humanReadableBytes(freeSpaceExternal, false)));
    }

    private void addDownload(DownloadInfo download) {
        DownloadEntryView downloadView = new DownloadEntryView(view.getContext());
        downloadView.setDownload(download);
        mDownloadsContainer.addView(downloadView);

        downloadViews.put(download, downloadView);
        downloadView.setButtonAction(new CancelOrOpenAction());
    }

    private class CancelOrOpenAction implements DownloadEntryView.Action {
        @Override
        public void execute(DownloadInfo downloadInfo) {
            DownloadServiceAdapter downloader = mDownloaderSupplier.getDownloader();
            switch (downloadInfo.getState()) {
                case waiting:
                case finished:
                    downloader.removeDownload(downloadInfo);
                case downloading:
                    downloader.cancelDownload(downloadInfo);
                    break;
                case failed:
                case cancelled:
                    downloader.removeDownload(downloadInfo);
                    downloadInfo.setState(DownloadInfo.State.waiting);
                    downloader.addDownload(downloadInfo);
                    break;
            }
        }
    }

}
