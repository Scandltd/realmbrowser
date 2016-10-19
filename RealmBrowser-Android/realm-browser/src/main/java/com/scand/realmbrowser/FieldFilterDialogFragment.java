package com.scand.realmbrowser;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import java.lang.reflect.Field;
import java.util.List;

import io.realm.RealmObject;

/**
 * Created by Slabodeniuk on 7/2/15.
 */
public class FieldFilterDialogFragment extends DialogFragment {

    interface FieldFilterDialogInteraction {
        void onFieldListChange();
    }

    private static final String ARG_CLASS_NAME = "canonical class name";

    private Class mClass;
    private List<Field> mFields;
    private boolean[] mCheckedItems = null;
    private FieldFilterDialogInteraction mListener;

    private String mDeselectAllText;
    private String mSelectAllText;

    public static FieldFilterDialogFragment createInstance(Class<? extends RealmObject> clazz) {
        Bundle args = new Bundle();
        args.putString(ARG_CLASS_NAME, clazz.getCanonicalName());

        FieldFilterDialogFragment fragment = new FieldFilterDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mListener = (FieldFilterDialogInteraction) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDeselectAllText = getResources().getString(R.string.realm_browser_deselect_all);
        mSelectAllText = getResources().getString(R.string.realm_browser_select_all);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String className = getArguments().getString(ARG_CLASS_NAME);

        FieldFilterPreferences prefs = FieldFilterPreferences.getInstance(getActivity());

        CharSequence[] items = null;

        try {
            mClass = Class.forName(className);
            mFields = RealmUtils.getFields(mClass);

            items = new CharSequence[mFields.size()];
            mCheckedItems = new boolean[mFields.size()];
            for (int i = 0; i < mFields.size(); i++) {
                Field f = mFields.get(i);
                items[i] = f.getName();
                mCheckedItems[i] = prefs.isFieldDisplayed(mClass, f);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.realm_browser_field_filter_dialog_title)
                .setMultiChoiceItems(items, mCheckedItems, mChoiceListener)
                .setPositiveButton(R.string.realm_browser_ok, mOkButtonClickListener)
                .setNegativeButton(R.string.realm_browser_cancel, mCancelClickListener)
                .setNeutralButton(R.string.realm_browser_deselect_all, null)
                .create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button selectionButton = ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_NEUTRAL);
                selectionButton.setOnClickListener(mChangeSelectionClickListener);
                updateSelectionButtonText();
            }
        });

        return dialog;
    }


    private void updateSelectionButtonText() {
        boolean isSelectedAll = true;
        for (int i = 0; i < mCheckedItems.length; i++) {
            if (!mCheckedItems[i]) {
                isSelectedAll = false;
                break;
            }
        }
        Button selectionButton = ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_NEUTRAL);
        selectionButton.setText(isSelectedAll ? mDeselectAllText : mSelectAllText);
    }

    private final DialogInterface.OnMultiChoiceClickListener mChoiceListener = new DialogInterface.OnMultiChoiceClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
            mCheckedItems[which] = isChecked;
            updateSelectionButtonText();
        }
    };

    private final DialogInterface.OnClickListener mOkButtonClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            Context c = FieldFilterDialogFragment.this.getActivity();
            FieldFilterPreferences prefs = FieldFilterPreferences.getInstance(c);
            for (int i = 0; i < mCheckedItems.length; i++) {
                prefs.setFieldDisplayed(mClass, mFields.get(i), mCheckedItems[i]);
            }

            mListener.onFieldListChange();
        }
    };

    private final DialogInterface.OnClickListener mCancelClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            // nothing to do here
        }
    };

    private final View.OnClickListener mChangeSelectionClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Button btn = (Button) view;
            String btnText = btn.getText().toString();

            boolean newValue = !btnText.equals(mDeselectAllText);

            AlertDialog dialog = (AlertDialog) getDialog();
            ListView list = dialog.getListView();
            for (int i = 0; i < mCheckedItems.length; i++) {
                mCheckedItems[i] = newValue;
                list.setItemChecked(i, newValue);
            }
            updateSelectionButtonText();
        }
    };
}
