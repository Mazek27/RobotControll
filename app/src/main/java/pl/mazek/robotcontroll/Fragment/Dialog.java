package pl.mazek.robotcontroll.Fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Created by Mazek on 07.03.2017.
 */

public class Dialog extends DialogFragment {

    String solve;

    @SuppressLint("ValidFragment")
    public Dialog(String solve) {
        this.solve = solve;
    }

    public Dialog() {
    }

    @Override
    public android.app.Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(solve).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        return builder.create();
    }
}
