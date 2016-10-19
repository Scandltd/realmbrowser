package com.scand.realmbrowser;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;

/**
 * Created by Slabodeniuk on 10/15/16.
 */

public class ProgressDialogFragment extends DialogFragment {

    static final String PROGRESS_DIALOG_TAG = ProgressDialogFragment.class.getCanonicalName();

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ProgressDialog dialog = new ProgressDialog(getContext(), R.style.realm_browser_ProgressDialogStyle);
        dialog.setIndeterminate(true);
        dialog.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false);
    }
}
