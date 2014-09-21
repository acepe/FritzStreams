package de.acepe.fritzstreams.ui;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Environment;
import android.util.Log;
import de.acepe.fritzstreams.R;

public class DirectoryDialog {

    public interface FileSelectedListener {

        void fileSelected(File file);
    }

    public interface DirectorySelectedListener {

        void directorySelected(File directory);
    }

    private static final String TAG = DirectoryDialog.class.getName();
    private static final String PARENT_DIR = "..";

    private final Activity activity;

    private ListenerList<FileSelectedListener> fileListenerList = new ListenerList<>();
    private ListenerList<DirectorySelectedListener> dirListenerList = new ListenerList<>();

    private String[] fileList;
    private File currentPath;

    public DirectoryDialog(Activity activity, File path) {
        this.activity = activity;

        if (!path.exists()) {
            path = Environment.getExternalStorageDirectory();
        }
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            loadFileList(path);
        }
    }

    /**
     * @return file dialog
     */
    public Dialog createFileDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        builder.setTitle(currentPath.getPath());
        builder.setPositiveButton(activity.getString(R.string.file_dialog_set_dir), new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG, currentPath.getPath());
                fireDirectorySelectedEvent(currentPath);
            }
        });
        builder.setNegativeButton(activity.getString(R.string.dialog_close), new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        builder.setItems(fileList, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String fileChosen = fileList[which];
                File chosenFile = getChosenFile(fileChosen);
                if (chosenFile.isDirectory()) {
                    loadFileList(chosenFile);
                    dialog.cancel();
                    dialog.dismiss();
                    showDialog();
                } else {
                    fireFileSelectedEvent(chosenFile);
                }
            }
        });

        AlertDialog dialog = builder.show();

        if (currentPath.canWrite()) {
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
        } else {
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
        }

        return dialog;
    }

    public void addDirectoryListener(DirectorySelectedListener listener) {
        dirListenerList.add(listener);
    }

    public void showDialog() {
        createFileDialog();
    }

    private void fireFileSelectedEvent(final File file) {
        fileListenerList.fireEvent(new ListenerList.FireHandler<FileSelectedListener>() {
            public void fireEvent(FileSelectedListener listener) {
                listener.fileSelected(file);
            }
        });
    }

    private void fireDirectorySelectedEvent(final File directory) {
        dirListenerList.fireEvent(new ListenerList.FireHandler<DirectorySelectedListener>() {
            public void fireEvent(DirectorySelectedListener listener) {
                listener.directorySelected(directory);
            }
        });
    }

    private void loadFileList(File path) {
        this.currentPath = path;
        List<String> r = new ArrayList<>();
        if (path.exists()) {
            if (path.getParentFile() != null) {
                r.add(PARENT_DIR);
            }
            FilenameFilter filter = new FilenameFilter() {
                public boolean accept(File dir, String filename) {
                    File sel = new File(dir, filename);
                    return sel.canRead() && sel.isDirectory();
                }
            };
            String[] fileList1 = path.list(filter);
            if (fileList1 != null) {
                Collections.addAll(r, fileList1);
            }
        }
        Collections.sort(r);
        fileList = r.toArray(new String[r.size()]);
    }

    private File getChosenFile(String fileChosen) {
        if (fileChosen.equals(PARENT_DIR)) {
            return currentPath.getParentFile();
        } else {
            return new File(currentPath, fileChosen);
        }
    }

}

class ListenerList<L> {

    private List<L> listenerList = new ArrayList<>();

    public interface FireHandler<L> {

        void fireEvent(L listener);
    }

    public void add(L listener) {
        listenerList.add(listener);
    }

    public void fireEvent(FireHandler<L> fireHandler) {
        List<L> copy = new ArrayList<>(listenerList);
        for (L l : copy) {
            fireHandler.fireEvent(l);
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public void remove(L listener) {
        listenerList.remove(listener);
    }

    @SuppressWarnings("UnusedDeclaration")
    public List<L> getListenerList() {
        return listenerList;
    }
}
