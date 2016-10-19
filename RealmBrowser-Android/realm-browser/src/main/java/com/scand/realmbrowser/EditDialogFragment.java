package com.scand.realmbrowser;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.TimePicker;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmObject;

/**
 * Created by Slabodeniuk on 6/29/15.
 */
public class EditDialogFragment extends DialogFragment {

    public interface OnFieldEditDialogInteraction {
        void onRowWasEdit(int position);
    }

    private static final String ARG_POSITION = "ream object position";

    private RealmObject mRealmObject;
    private Field mField;
    private int mPosition;

    private OnFieldEditDialogInteraction mListener;

    // for text edit dialog
    private EditText mEditText;
    private TextView mErrorText;

    // for date edit dialog
    private TabHost mTabHost;
    private DatePicker mDatePicker;
    private TimePicker mTimePicker;

    // for boolean edit dialog
    private RadioGroup mRadioGroup;

    // for byte[] edit dialog
    private TextView mByteTextView;

    public static EditDialogFragment createInstance(RealmObject obj, Field field, int position) {
        RealmObjectHolder realmObjectHolder = RealmObjectHolder.getInstance();
        realmObjectHolder.setObject(obj);
        realmObjectHolder.setField(field);

        Bundle args = new Bundle();
        args.putInt(ARG_POSITION, position);

        EditDialogFragment fragment = new EditDialogFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mListener = (OnFieldEditDialogInteraction) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        mRealmObject = RealmObjectHolder.getInstance().getObject();
        mField = RealmObjectHolder.getInstance().getField();
        mPosition = getArguments().getInt(ARG_POSITION);

        if (mRealmObject == null || mField == null) {
            throw new IllegalArgumentException("Use RealmObjectHolder to store data");
        }

        int layoutId = -1;
        Class<?> type = mField.getType();
        if (type == String.class
                || type == Short.class || type == short.class
                || type == Integer.class || type == int.class
                || type == Long.class || type == long.class
                || type == Float.class || type == float.class
                || type == Double.class || type == double.class) {
            layoutId = R.layout.realm_browser_text_edit_layout;
        } else if (type == Boolean.class || type == boolean.class) {
            layoutId = R.layout.realm_browser_boolean_edit_layout;
        } else if (type == Date.class) {
            layoutId = R.layout.realm_browser_date_edit_layout;
        } else if (type == Byte[].class || type == byte[].class) {
            layoutId = R.layout.realm_browser_byte_array_edit_layout;
        }

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View root = inflater.inflate(layoutId, null);

        findViews(root);
        initUI(mRealmObject, mField, type);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        if (layoutId == -1) {
            builder.setMessage("Unknown field type.");
        } else {

            builder.setView(root);
        }


        builder.setPositiveButton(R.string.realm_browser_ok, null);
        if (type != Byte[].class && type != byte[].class) {
            builder.setNegativeButton(R.string.realm_browser_cancel, mCancelClickListener);
        }
        if (isTypeNullable(type)) {
            builder.setNeutralButton(R.string.realm_browser_reset_to_null, null);
        }

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button positiveButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                positiveButton.setOnClickListener(mOkClickListener);

                Button resetToNull = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEUTRAL);
                if (resetToNull != null) {
                    resetToNull.setOnClickListener(mResetToNullClickListener);
                }
            }
        });

        return dialog;
    }

    private void findViews(View root) {
        mEditText = (EditText) root.findViewById(R.id.text_edit_dialog);
        mErrorText = (TextView) root.findViewById(R.id.error_message);

        mRadioGroup = (RadioGroup) root.findViewById(R.id.edit_boolean_group);

        mTabHost = (TabHost) root.findViewById(R.id.tabHost);
        mDatePicker = (DatePicker) root.findViewById(R.id.tab_date);
        mTimePicker = (TimePicker) root.findViewById(R.id.tab_time);

        mByteTextView = (TextView) root.findViewById(R.id.array);
    }

    private void initUI(RealmObject obj, Field field, Class<?> type) {
        if (type == String.class
                || type == Short.class || type == short.class
                || type == Integer.class || type == int.class
                || type == Long.class || type == long.class
                || type == Float.class || type == float.class
                || type == Double.class || type == double.class) {

            Object valueObj = RealmUtils.getNotParamFieldValue(obj, field);
            mEditText.setText(valueObj == null ? "" : valueObj.toString());

            int inputType;
            if (type == String.class) {
                inputType = InputType.TYPE_CLASS_TEXT;
            } else if (type == Float.class || type == float.class
                    || type == Double.class || type == double.class) {
                inputType = InputType.TYPE_CLASS_NUMBER
                        | InputType.TYPE_NUMBER_FLAG_DECIMAL
                        | InputType.TYPE_NUMBER_FLAG_SIGNED;
            } else {
                inputType = InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED;
            }

            mEditText.setInputType(inputType);

        } else if (type == Boolean.class || type == boolean.class) {
            Boolean valueObj = (Boolean) RealmUtils.getNotParamFieldValue(obj, field);

            int checkedId;
            if (valueObj == null) {
                checkedId = -1;
            } else if (valueObj) {
                checkedId = R.id.edit_boolean_true;
            } else {
                checkedId = R.id.edit_boolean_false;
            }

            if (checkedId != -1)
                ((RadioButton) mRadioGroup.findViewById(checkedId)).setChecked(true);

        } else if (type == Date.class) {
            mTabHost.setup();

            // create date tab
            TabHost.TabSpec specDate = mTabHost.newTabSpec("Date");
            specDate.setIndicator("Date");
            specDate.setContent(R.id.tab_date);
            mTabHost.addTab(specDate);

            // create time tab
            TabHost.TabSpec specTime = mTabHost.newTabSpec("Time");
            specTime.setIndicator("Time");
            specTime.setContent(R.id.tab_time);
            mTabHost.addTab(specTime);

            Date valueObj = (Date) RealmUtils.getNotParamFieldValue(obj, field);

            Calendar c = Calendar.getInstance();
            c.setTime(valueObj != null ? valueObj : new Date());
            mDatePicker.updateDate(c.get(Calendar.YEAR),
                    c.get(Calendar.MONTH),
                    c.get(Calendar.DAY_OF_MONTH));

            mTimePicker.setCurrentHour(c.get(Calendar.HOUR));
            mTimePicker.setCurrentMinute(c.get(Calendar.MINUTE));
            mTimePicker.setIs24HourView(true);

        } else if (type == Byte[].class || type == byte[].class) {
            byte[] valueObj = (byte[]) RealmUtils.getNotParamFieldValue(obj, field);
            if (valueObj == null) {
                mByteTextView.setText(R.string.realm_browser_byte_array_is_null);
            } else {
                for (byte b : valueObj) {
                    mByteTextView.append(String.format("0x%02X", b) + " ");
                }
            }
        }
    }

    private Boolean isTypeNullable(Class type) {
        return (type == Date.class
                || type == Boolean.class
                || type == String.class
                || type == Short.class
                || type == Integer.class
                || type == Long.class
                || type == Float.class
                || type == Double.class);
    }

    private final View.OnClickListener mResetToNullClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            saveNewValue(null);
            mListener.onRowWasEdit(mPosition);
            EditDialogFragment.this.dismiss();
        }
    };

    private final View.OnClickListener mOkClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Class<?> type = mField.getType();
            Object newValue;
            if (type == String.class) {
                newValue = mEditText.getText().toString();
            } else if (type == Boolean.class || type == boolean.class) {
                newValue = mRadioGroup.getCheckedRadioButtonId() == R.id.edit_boolean_true;
            } else if (type == Short.class || type == short.class) {
                try {
                    newValue = Short.valueOf(mEditText.getText().toString());
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    newValue = null;
                }
            } else if (type == Integer.class || type == int.class) {
                try {
                    newValue = Integer.valueOf(mEditText.getText().toString());
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    newValue = null;
                }
            } else if (type == Long.class || type == long.class) {
                try {
                    newValue = Long.valueOf(mEditText.getText().toString());
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    newValue = null;
                }
            } else if (type == Float.class || type == float.class) {
                try {
                    newValue = Float.valueOf(mEditText.getText().toString());
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    newValue = null;
                }
            } else if (type == Double.class || type == double.class) {
                try {
                    newValue = Double.valueOf(mEditText.getText().toString());
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    newValue = null;
                }
            } else if (type == Date.class) {
                Class objClass = mRealmObject.getClass().getSuperclass();
                Realm realm = Realm.getInstance(RealmBrowser.getInstance().getRealmConfig(objClass));
                Date currentValue = (Date) RealmUtils.getNotParamFieldValue(mRealmObject, mField);
                realm.close();
                Calendar calendar = Calendar.getInstance();
                if (currentValue != null)
                    calendar.setTime(currentValue);
                calendar.set(Calendar.YEAR, mDatePicker.getYear());
                calendar.set(Calendar.MONTH, mDatePicker.getMonth());
                calendar.set(Calendar.DATE, mDatePicker.getDayOfMonth());
                calendar.set(Calendar.HOUR_OF_DAY, mTimePicker.getCurrentHour());
                calendar.set(Calendar.MINUTE, mTimePicker.getCurrentMinute());
                newValue = calendar.getTime();
            } else if (type == Byte[].class || type == byte[].class) {
                EditDialogFragment.this.dismiss();
                return;
            } else {
                newValue = null;
            }

            if (newValue != null) {
                saveNewValue(newValue);
                mListener.onRowWasEdit(mPosition);
                EditDialogFragment.this.dismiss();
            } else {
                showError(type);
            }
        }
    };

    private final DialogInterface.OnClickListener mCancelClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            // nothing to do here
        }
    };

    private void saveNewValue(Object newValue) {
        Class objClass = mRealmObject.getClass().getSuperclass();
        Realm realm = Realm.getInstance(RealmBrowser.getInstance().getRealmConfig(objClass));
        realm.beginTransaction();
        RealmUtils.setNotParamFieldValue(mRealmObject, mField, newValue);
        realm.commitTransaction();
        realm.close();
    }

    private void showError(Class<?> clazz) {
        String notFormatted = getString(R.string.realm_browser_value_edit_error);
        String error = String.format(notFormatted, mEditText.getText().toString(),
                clazz.getSimpleName());
        mErrorText.setText(error);
    }
}
