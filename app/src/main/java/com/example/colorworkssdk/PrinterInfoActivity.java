package com.example.colorworkssdk;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.epson.ijprinter.esclabelsdk.EPSLabelPrinter;
import com.epson.ijprinter.esclabelsdk.EPSLabelPrinterStatus;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PrinterInfoActivity extends AppCompatActivity {
    private static final int REQUEST_PICK_IMAGE = 100;
    private final ExecutorService mExecutorService = Executors.newSingleThreadExecutor();
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private EPSLabelPrinter mPrinter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_printer_info);

        mPrinter = getIntent().getParcelableExtra("Printer");
        if ( mPrinter != null ) {
            mPrinter.prepareForUSBConnection(this);
            TextView modelNameView = findViewById(R.id.text_model_name);
            modelNameView.setText(mPrinter.getModelName());
            TextView locationView = findViewById(R.id.text_location);
            locationView.setText(mPrinter.getLocation());
            TextView serialNumberView = findViewById(R.id.text_serial_number);
            serialNumberView.setText(mPrinter.getSerialNumber());
            TextView printerIDView = findViewById(R.id.text_printer_id);
            printerIDView.setText(mPrinter.getPrinterID());
        }

        Button sendCommandButton = findViewById(R.id.button_send_command);
        sendCommandButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSendCommandActivity();
            }
        });

        Button statusButton = findViewById(R.id.button_printer_status);
        statusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPrinterStatusAndStartActivity();
            }
        });

        Button selectImageButton = findViewById(R.id.button_select_image);
        selectImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_PICK_IMAGE);
            }
        });
    }

    private void startSendCommandActivity() {
        if ( mPrinter == null ) {
            return;
        }
        Intent intent = new Intent(PrinterInfoActivity.this, SendCommandActivity.class);
        intent.putExtra(SendCommandActivity.KEY_PRINTER, mPrinter);
        startActivity(intent);
    }

    private void requestPrinterStatusAndStartActivity() {
        if ( mPrinter == null ) {
            return;
        }
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                EPSLabelPrinterStatus status = mPrinter.requestStatus();
                if ( status != null ) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(PrinterInfoActivity.this, PrinterStatusActivity.class);
                            intent.putExtra("PrinterStatus", status);
                            startActivity(intent);
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ( requestCode == REQUEST_PICK_IMAGE && resultCode == RESULT_OK && data != null ) {
            Uri uri = data.getData();
            Intent intent = new Intent(PrinterInfoActivity.this, ImagePreviewActivity.class);
            intent.putExtra(ImagePreviewActivity.KEY_IMAGE_URI, uri);
            intent.putExtra(ImagePreviewActivity.KEY_PRINTER, mPrinter);
            startActivity(intent);
        }
    }
}