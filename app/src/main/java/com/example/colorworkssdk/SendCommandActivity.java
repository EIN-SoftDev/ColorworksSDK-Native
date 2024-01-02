package com.example.colorworkssdk;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.epson.ijprinter.esclabelsdk.EPSLabelPrinter;

public class SendCommandActivity extends AppCompatActivity {
    public static final String KEY_PRINTER = "Printer";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_command);

        EditText timeoutEditText = findViewById(R.id.editTextNumberTimeout);
        timeoutEditText.setText("10000");

        EPSLabelPrinter printer = getIntent().getParcelableExtra(KEY_PRINTER);
        printer.prepareForUSBConnection(this);

        Button statusButton = findViewById(R.id.button_send);
        statusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText commandEditText = findViewById(R.id.editTextTextCommand);
                String command = commandEditText.getText().toString();

                EditText timeoutEditText = findViewById(R.id.editTextNumberTimeout);
                Integer timeout = Integer.parseInt(timeoutEditText.getText().toString());

                Pair<Integer, byte[]> res = printer.sendCommand(command.getBytes(), timeout);

                if (res.second != null) {
                    String response = new String(res.second);

                    EditText responseEditText = findViewById(R.id.editTextTextResponse);
                    responseEditText.setText(response);
                }
            }
        });
    }
}