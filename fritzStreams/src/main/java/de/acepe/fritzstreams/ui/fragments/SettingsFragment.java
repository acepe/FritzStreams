package de.acepe.fritzstreams.ui.fragments;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.widget.Toast;

import java.io.File;

import de.acepe.fritzstreams.Config;
import de.acepe.fritzstreams.R;
import de.acepe.fritzstreams.ui.FileDialog;

public class SettingsFragment extends PreferenceFragment {

    private FileDialog mFileDialog;
    private Preference mDownloadDir;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        mDownloadDir = getPreferenceScreen().findPreference(Config.SP_DOWNLOAD_DIR);
        mDownloadDir.setOnPreferenceClickListener(opclDownloadDir);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        // Create dialog
        String defaultPath = Environment.getExternalStorageDirectory() + File.separator + Config.DEFAULT_DOWNLOAD_DIR;
        String path = sharedPreferences.getString(Config.SP_DOWNLOAD_DIR, defaultPath);

        mFileDialog = new FileDialog(getActivity(), new File(path));
        mFileDialog.addDirectoryListener(new FileDialog.DirectorySelectedListener() {
            public void directorySelected(File directory) {
                SharedPreferences.Editor editor = getPreferenceScreen().getSharedPreferences()
                        .edit();
                editor.putString(Config.SP_DOWNLOAD_DIR, directory.getAbsolutePath());
                editor.commit();
                mDownloadDir.setSummary(directory.getAbsolutePath());
            }
        });
        mFileDialog.setSelectDirectoryOption(true);
        mDownloadDir.setSummary(path);

    }

    private Preference.OnPreferenceClickListener opclDownloadDir = new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                mFileDialog.showDialog();
            } else {
                Toast.makeText(SettingsFragment.this.getActivity(), R.string.download_noti_not_mounted,
                        Toast.LENGTH_SHORT).show();
            }
            return true;
        }
    };
}
