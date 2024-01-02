package com.example.colorworkssdk;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Locale;

public class CustomSizeDialogFragment extends DialogFragment {
    private static final String KEY_BASE_DPI = "KEY_BASE_DPI";
    private static final String KEY_UNIT_TYPE = "KEY_UNIT_TYPE";
    private static final String KEY_WIDTH = "KEY_WIDTH";
    private static final String KEY_HEIGHT = "KEY_HEIGHT";
    private static final int DEFAULT_BASE_DPI = 300;
    private static final float MM_PER_INCH = 25.4f;
    public static final int UNIT_TYPE_MM = 0;
    public static final int UNIT_TYPE_INCH = 1;
    private RadioGroup mUnitGroup;
    private EditText mWidthEdit;
    private EditText mHeightEdit;

    public interface OnResultListener {
        void onResult(CustomSizeDialogFragment dialog, int unitType, int width, int height);
        void onCancel(CustomSizeDialogFragment dialog);
    }

    public CustomSizeDialogFragment() {
        setArguments(new Bundle());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_custom_size, container, false);//super.onCreateView(inflater, container, savedInstanceState);
        mUnitGroup = view.findViewById(R.id.group_unit);
        int unitType = getUnitType();
        if ( unitType == UNIT_TYPE_MM ) {
            mUnitGroup.check(R.id.radio_unit_mm);
        } else {
            mUnitGroup.check(R.id.radio_unit_inch);
        }
        mWidthEdit = view.findViewById(R.id.edit_width);
        if ( requireArguments().containsKey(KEY_WIDTH) ) {
            int width = requireArguments().getInt(KEY_WIDTH);
            String widthText = null;
            if ( unitType == UNIT_TYPE_MM ) {
                float widthMM = width / (float)getBaseDpi() * MM_PER_INCH;
                widthText = String.format(Locale.getDefault(), "%.1f", widthMM);
            } else {
                float widthInch = width / (float)getBaseDpi();
                widthText = String.format(Locale.getDefault(), "%.3f", widthInch);
            }
            mWidthEdit.setText(widthText);
        }
        mHeightEdit = view.findViewById(R.id.edit_height);
        if ( requireArguments().containsKey(KEY_HEIGHT) ) {
            int height = requireArguments().getInt(KEY_HEIGHT);
            String heightText = null;
            if ( unitType == UNIT_TYPE_MM ) {
                float heightMM = height / (float)getBaseDpi() * MM_PER_INCH;
                heightText = String.format(Locale.getDefault(), "%.1f", heightMM);
            } else {
                float heightInch = height / (float)getBaseDpi();
                heightText = String.format(Locale.getDefault(), "%.3f", heightInch);
            }
            mHeightEdit.setText(heightText);
        }

        Button okButton = view.findViewById(R.id.button_ok);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int checkedId = mUnitGroup.getCheckedRadioButtonId();
                String widthText = mWidthEdit.getText().toString();
                if (TextUtils.isEmpty(widthText) ) {
                    return;
                }
                float widthF = Float.parseFloat(widthText);
                String heightText = mHeightEdit.getText().toString();
                if ( TextUtils.isEmpty(heightText) ) {
                    return;
                }
                float heightF = Float.parseFloat(heightText);
                int baseDpi = getBaseDpi();
                int width = 0, height = 0, unitType = 0;
                if ( checkedId == R.id.radio_unit_mm ) {
                    width = Math.round(widthF / MM_PER_INCH * baseDpi);
                    height = Math.round(heightF / MM_PER_INCH * baseDpi);
                    unitType = UNIT_TYPE_MM;
                } else {
                    width = Math.round(widthF * baseDpi);
                    height = Math.round(heightF * baseDpi);
                    unitType = UNIT_TYPE_INCH;
                }

                Activity activity = getActivity();
                if ( activity instanceof OnResultListener ) {
                    OnResultListener listener = (OnResultListener)activity;
                    listener.onResult(CustomSizeDialogFragment.this, unitType, width, height);
                }
                dismiss();
            }
        });

        Button cancelButton = view.findViewById(R.id.button_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                Activity activity = getActivity();
                if ( activity instanceof OnResultListener ) {
                    OnResultListener listener = (OnResultListener)activity;
                    listener.onCancel(CustomSizeDialogFragment.this);
                }
            }
        });

        requireDialog().setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                Activity activity = getActivity();
                if ( activity instanceof OnResultListener ) {
                    OnResultListener listener = (OnResultListener)activity;
                    listener.onCancel(CustomSizeDialogFragment.this);
                }
            }
        });

        return view;
    }

    public void setBaseDpi(int baseDpi) {
        requireArguments().putInt(KEY_BASE_DPI, baseDpi);
    }

    public int getBaseDpi() {
        return requireArguments().getInt(KEY_BASE_DPI, DEFAULT_BASE_DPI);
    }

    public void setUnitType(int unitType) {
        requireArguments().putInt(KEY_UNIT_TYPE, unitType);
    }

    public int getUnitType() {
        return requireArguments().getInt(KEY_UNIT_TYPE, UNIT_TYPE_MM);
    }

    public void setWidth(int width) {
        requireArguments().putInt(KEY_WIDTH, width);
    }

    public void setHeight(int height) {
        requireArguments().putInt(KEY_HEIGHT, height);
    }
}