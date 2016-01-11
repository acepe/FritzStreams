package de.acepe.fritzstreams.ui.fragments;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import de.acepe.fritzstreams.App;
import de.acepe.fritzstreams.R;
import de.acepe.fritzstreams.backend.StreamDownload;
import de.acepe.fritzstreams.ui.DownloadAdapter;
import de.acepe.fritzstreams.util.Utilities;

public class DownloadFragment extends Fragment {

    private Timer mUpdateTimer;
    private ListView mList;
    private View mEmptyDownloads;
    private DownloadAdapter mAdapter;
    private TextView mFreeSpace;

    @Override
    public void onResume() {
        super.onResume();

        mUpdateTimer = new Timer();
        mUpdateTimer.scheduleAtFixedRate(createTimerTask(), 250, 250);
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mUpdateTimer != null) {
            mUpdateTimer.cancel();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.download_fragment, container, false);

        mList = (ListView) view.findViewById(R.id.downloads);
        mEmptyDownloads = view.findViewById(R.id.downloads_empty);
        mFreeSpace = (TextView) view.findViewById(R.id.downloads_freespace);

        // Create the adapter
        mAdapter = new DownloadAdapter(getActivity(), R.layout.download_row, App.downloader.getDownloads());
        mList.setAdapter(mAdapter);

        setHasOptionsMenu(true);

        return view;
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mList.setFastScrollEnabled(true);
        mList.setTextFilterEnabled(true);
        mList.setSelector(android.R.color.transparent);
        mList.setOnItemClickListener(oiclDownload);
    }

    private AdapterView.OnItemClickListener oiclDownload = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (App.downloader.getState(position) == StreamDownload.State.finished) {
                Uri outFile = App.downloader.getOutFileUri(position);

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
    };

    /**
     * Creates a timer task for refeshing the scheduleDownload list
     *
     * @return Task to update scheduleDownload list
     */
    private TimerTask createTimerTask() {
        return new TimerTask() {

            @Override
            public void run() {
                if (mAdapter == null || getActivity() == null)
                    return;

                getActivity().runOnUiThread(updateFreespace);
            }

            private Runnable updateFreespace = new Runnable() {
                @Override
                public void run() {
                    if (getActivity() == null) {
                        return;
                    }

                    mAdapter.notifyDataSetChanged();
                    if (App.downloader.isEmpty()) {
                        mList.setEmptyView(mEmptyDownloads);
                    }

                    long freeSpaceExternal = (long) Utilities.getFreeSpaceExternal();
                    mFreeSpace.setText(getActivity().getString(R.string.download_freespace,
                                                               Utilities.humanReadableBytes(freeSpaceExternal, false)));
                }
            };
        };
    }
}
