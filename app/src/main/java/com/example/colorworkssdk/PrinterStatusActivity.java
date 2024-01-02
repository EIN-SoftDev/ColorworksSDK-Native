package com.example.colorworkssdk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.epson.ijprinter.esclabelsdk.EPSLabelPrinterStatus;

import java.util.Locale;

public class PrinterStatusActivity extends AppCompatActivity {
    private RecyclerView mStatusItemListView;
    private static final String[] StatusTexts = {
            "NO_ERROR",
            "CONNECTION_ERROR",
            "GENERAL_ERROR",
            "PRINTER_OPERATING",
            "PAPER_JAM_ERROR",
            "INK_END_ERROR",
            "CARTRIDGE_ERROR",
            "PAPER_OUT_ERROR",
            "MAINTENANCE_ERROR",
            "PAPER_TYPE_ERROR",
            "PRINTING_IS_PAUSED",
            "INVALID_PRINT_SETTINGS_ERROR",
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_printer_status);

        EPSLabelPrinterStatus status = getIntent().getParcelableExtra("PrinterStatus");

        mStatusItemListView = findViewById(R.id.view_status_items);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mStatusItemListView.setLayoutManager(layoutManager);
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(this, layoutManager.getOrientation());
        mStatusItemListView.addItemDecoration(itemDecoration);
        StatusItemAdapter adapter = new StatusItemAdapter(status);
        mStatusItemListView.setAdapter(adapter);
    }

    private static final class StatusItemViewHolder extends RecyclerView.ViewHolder {
        public final TextView titleTextView;
        public final TextView detailTextView;

        public StatusItemViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.text_title);
            detailTextView = itemView.findViewById(R.id.text_detail);
        }
    }

    private static final class StatusItemAdapter extends RecyclerView.Adapter<StatusItemViewHolder> {
        private EPSLabelPrinterStatus mPrinterStatus;

        public StatusItemAdapter(EPSLabelPrinterStatus status) {
            mPrinterStatus = status;
        }

        @NonNull
        @Override
        public StatusItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_printer_status_item, parent, false);
            return new StatusItemViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull StatusItemViewHolder holder, int position) {
            if ( position == 0 ) {
                holder.titleTextView.setText(R.string.label_status);
                holder.detailTextView.setText(StatusTexts[mPrinterStatus.getState()]);
            } else {
                EPSLabelPrinterStatus.SupplyInfo supplyInfo = mPrinterStatus.getSupplyLevels()[position - 1];
                int titleId = 0;
                switch (supplyInfo.getSupplyType() ) {
                    case EPSLabelPrinterStatus.SUPPLY_TYPE_BLACK_INK:
                        titleId = R.string.label_black_ink;
                        break;
                    case EPSLabelPrinterStatus.SUPPLY_TYPE_MAGENTA_INK:
                        titleId = R.string.label_magenta_ink;
                        break;
                    case EPSLabelPrinterStatus.SUPPLY_TYPE_CYAN_INK:
                        titleId = R.string.label_cyan_ink;
                        break;
                    case EPSLabelPrinterStatus.SUPPLY_TYPE_YELLOW_INK:
                        titleId = R.string.label_yellow_ink;
                        break;
                    case EPSLabelPrinterStatus.SUPPLY_TYPE_MAINTENANCE_BOX:
                        titleId = R.string.label_maintenance_box;
                        break;
                }
                holder.titleTextView.setText(titleId);
                holder.detailTextView.setText(String.format(Locale.getDefault(),"%d", supplyInfo.getSupplyLevel()));
            }
        }

        @Override
        public int getItemCount() {
            return mPrinterStatus.getSupplyLevels().length + 1;
        }
    }
}