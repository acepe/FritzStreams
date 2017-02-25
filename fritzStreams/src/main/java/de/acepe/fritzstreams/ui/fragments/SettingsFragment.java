package de.acepe.fritzstreams.ui.fragments;

import java.io.File;

import com.nononsenseapps.filepicker.FilePickerActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v4.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import de.acepe.fritzstreams.R;
import de.acepe.fritzstreams.backend.Constants;

public class SettingsFragment extends PreferenceFragment {

    private static final int FILE_CODE = 1;

    private Preference mDownloadDir;
    private Intent dirChooserIntent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        // Create dialog
        String defaultPath = Environment.DIRECTORY_DOWNLOADS;
        String path = sharedPreferences.getString(Constants.SP_DOWNLOAD_DIR, defaultPath);
        mDownloadDir = getPreferenceScreen().findPreference(Constants.SP_DOWNLOAD_DIR);
        mDownloadDir.setSummary(path);

        configureDirChooser(path);
        mDownloadDir.setOnPreferenceClickListener(opclDownloadDir);
    }

    private void configureDirChooser(String path) {
        // This always works
        dirChooserIntent = new Intent(getContext(), FilePickerActivity.class);
        // This works if you defined the intent filter
        // Intent dirChooserIntent = new Intent(Intent.ACTION_GET_CONTENT);

        // Set these depending on your use case. These are the defaults.
        dirChooserIntent.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
        dirChooserIntent.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false);
        dirChooserIntent.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR);
        dirChooserIntent.putExtra(FilePickerActivity.EXTRA_START_PATH, path);

        // Configure initial directory by specifying a String.
        // You could specify a String like "/storage/emulated/0/", but that can
        // dangerous. Always use Android's API calls to get paths to the SD-card or
        // internal memory.
        dirChooserIntent.putExtra(FilePickerActivity.EXTRA_START_PATH,
                Environment.getExternalStorageDirectory().getPath());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FILE_CODE && resultCode == Activity.RESULT_OK) {
            if (!data.getBooleanExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false)) {
                // The URI will now be something like content://PACKAGE-NAME/root/path/to/file
                Uri uri = data.getData();
                // A utility method is provided to transform the URI to a File object
                File targetDir = com.nononsenseapps.filepicker.Utils.getFileForUri(uri);

                // Do something with the result...
                SharedPreferences.Editor editor = getPreferenceScreen().getSharedPreferences().edit();
                String targetDirAbsolutePath = targetDir.getAbsolutePath();
                editor.putString(Constants.SP_DOWNLOAD_DIR, targetDirAbsolutePath);
                editor.apply();
                mDownloadDir.setSummary(targetDirAbsolutePath);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        view.setBackgroundColor(getResources().getColor(R.color.yellow_fritz));
        return view;
    }

    private final Preference.OnPreferenceClickListener opclDownloadDir = new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                // mDirectoryDialog.showDialog();
                startActivityForResult(dirChooserIntent, FILE_CODE);
            } else {
                Toast.makeText(SettingsFragment.this.getActivity(),
                        R.string.download_noti_not_mounted,
                        Toast.LENGTH_SHORT)
                        .show();
            }
            return true;
        }
    };
}
