package de.acepe.fritzstreams.ui.fragments;

import java.io.File;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v4.preference.PreferenceFragment;
import android.widget.Toast;
import de.acepe.fritzstreams.App;
import de.acepe.fritzstreams.R;
import de.acepe.fritzstreams.ui.DirectoryDialog;

public class SettingsFragment extends PreferenceFragment {

    private DirectoryDialog mDirectoryDialog;
    private Preference mDownloadDir;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        // Create dialog
        String defaultPath = Environment.DIRECTORY_MUSIC;
        String path = sharedPreferences.getString(App.SP_DOWNLOAD_DIR, defaultPath);

        mDirectoryDialog = new DirectoryDialog(getActivity(), new File(path));
        mDirectoryDialog.addDirectoryListener(new DirectoryDialog.DirectorySelectedListener() {
            public void directorySelected(File directory) {
                SharedPreferences.Editor editor = getPreferenceScreen().getSharedPreferences().edit();
                editor.putString(App.SP_DOWNLOAD_DIR, directory.getAbsolutePath());
                editor.commit();

                mDownloadDir.setSummary(directory.getAbsolutePath());
            }
        });

        mDownloadDir = getPreferenceScreen().findPreference(App.SP_DOWNLOAD_DIR);
        mDownloadDir.setOnPreferenceClickListener(opclDownloadDir);
        mDownloadDir.setSummary(path);
    }

    private Preference.OnPreferenceClickListener opclDownloadDir = new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                mDirectoryDialog.showDialog();
            } else {
                Toast.makeText(SettingsFragment.this.getActivity(),
                               R.string.download_noti_not_mounted,
                               Toast.LENGTH_SHORT).show();
            }
            return true;
        }
    };
}