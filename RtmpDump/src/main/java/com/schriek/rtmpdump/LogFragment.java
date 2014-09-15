package com.schriek.rtmpdump;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

@SuppressLint("ValidFragment")
public class LogFragment extends Fragment {

    private ListView list;
    private static AppendingAdapter adapter;

    public LogFragment(Context con) {
        adapter = new AppendingAdapter(con);
    }

    public static void Append(String s) {
        if (adapter == null) {
            return;
        }

        adapter.add(s);
        adapter.notifyDataSetChanged();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (container == null) {
            return null;
        }
        String dir = Environment.getExternalStorageDirectory().getAbsolutePath();

        Rtmpdump dump = new Rtmpdump();
        Log.i("DUMP", "trying help:");
        dump.parseString("rtmpdump -h ");

        View commandfragment = inflater.inflate(R.layout.commandfragment, container, false);

        list = (ListView) commandfragment.findViewById(R.id.commandLog);

        list.setAdapter(adapter);

        return commandfragment;
    }
}
