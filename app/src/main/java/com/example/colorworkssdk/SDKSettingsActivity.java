package com.example.colorworkssdk;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.epson.ijprinter.esclabelsdk.EPSLog;

public class SDKSettingsActivity extends AppCompatActivity {
    private Button mOKButton;
    private CheckBox mOutputLogCheckBox;
    private EditText mMaxLogSizeEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sdksettings);

        mOKButton = findViewById(R.id.button_ok);
        mOutputLogCheckBox = findViewById(R.id.check_output_log);
        mMaxLogSizeEdit = findViewById(R.id.edit_log_size_max);

        int logOutputType = EPSLog.getOutputType(this);
        mOutputLogCheckBox.setChecked(logOutputType == EPSLog.OUTPUT_TYPE_STORAGE);
        int logMaxSize = EPSLog.getMaxSize(this);
        mMaxLogSizeEdit.setText(String.valueOf(logMaxSize));
        mOKButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int logOutputType = mOutputLogCheckBox.isChecked() ? EPSLog.OUTPUT_TYPE_STORAGE : EPSLog.OUTPUT_TYPE_DISABLED;
                EPSLog.setOutputType(SDKSettingsActivity.this, logOutputType);
                String maxSizeText = mMaxLogSizeEdit.getText().toString();
                int maxSize = Integer.parseInt(maxSizeText);
                EPSLog.setMaxSize(SDKSettingsActivity.this, maxSize);
                finish();
            }
        });
    }
}