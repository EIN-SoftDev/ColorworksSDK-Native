package com.example.colorworkssdk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.epson.ijprinter.esclabelsdk.EPSLabelPrinter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PrintSettingsActivity extends AppCompatActivity implements CustomSizeDialogFragment.OnResultListener {
    public static final String KEY_PRINTER = "Printer";
    public static final String KEY_CURRENT_SETTINGS = "CurrentSettings";
    public static final String KEY_NUMBER_OF_PAGES = "number_of_pages";

    private static final String[] AllKeys = {
            EPSLabelPrinter.KEY_PAPER_SIZE_TYPE,
            EPSLabelPrinter.KEY_MEDIA_TYPE,
            EPSLabelPrinter.KEY_QUALITY,
            EPSLabelPrinter.KEY_MEDIA_FORM,
            EPSLabelPrinter.KEY_MEDIA_SAVING,
            EPSLabelPrinter.KEY_COLLATE,
            EPSLabelPrinter.KEY_ACTION_MODE,
            EPSLabelPrinter.KEY_CUT_INTERVAL,
            EPSLabelPrinter.KEY_BUZZER,
            EPSLabelPrinter.KEY_PAUSE,
            EPSLabelPrinter.KEY_COLOR_ADJUSTMENT,
            EPSLabelPrinter.KEY_INK_PROFILE,
            EPSLabelPrinter.KEY_BRIGHTNESS,
            EPSLabelPrinter.KEY_CONTRAST,
            EPSLabelPrinter.KEY_SATURATION,
            EPSLabelPrinter.KEY_CYAN,
            EPSLabelPrinter.KEY_MAGENTA,
            EPSLabelPrinter.KEY_YELLOW,
            EPSLabelPrinter.KEY_BI_DIRECTION,
            EPSLabelPrinter.KEY_BLACK_RATIO,
            EPSLabelPrinter.KEY_DRYING_TIME,
            EPSLabelPrinter.KEY_HORIZONTAL_POSITION,
            EPSLabelPrinter.KEY_VERTICAL_POSITION,
            EPSLabelPrinter.KEY_CUT_POSITION,
            EPSLabelPrinter.KEY_GAP_BETWEEN_LABELS,
            EPSLabelPrinter.KEY_LEFT_AND_RIGHT_GAP,
            EPSLabelPrinter.KEY_PEEL_POSITION,
    };
    private RecyclerView mSettingItemsView;
    private SettingItemAdapter mAdapter;
    private Map<String, Object> mCurrentSettings;
    private int mCustomSizeUnit = CustomSizeDialogFragment.UNIT_TYPE_MM;
    private List<String> mSupportedKeyList;
    private List<Object[]> mSupportedValuesList;
    private Integer mPreviousPaperSizeType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print_settings);

        mSettingItemsView = findViewById(R.id.recycler_setting_items);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mSettingItemsView.setLayoutManager(layoutManager);
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(this, layoutManager.getOrientation());
        mSettingItemsView.addItemDecoration(itemDecoration);

        mCurrentSettings = (Map<String, Object>)getIntent().getSerializableExtra(KEY_CURRENT_SETTINGS);
        EPSLabelPrinter printer = getIntent().getParcelableExtra(KEY_PRINTER);
        if (printer != null) {
            if (mCurrentSettings == null) {
                mCurrentSettings = printer.getDefaultPrintSettings();
            }
            mSupportedValuesList = new ArrayList<>();
            mSupportedKeyList = new ArrayList<>();
            for (String key : AllKeys) {
                Object[] supportedValues = printer.getSupportedPrintSettings(key);
                if (supportedValues != null && supportedValues.length > 0) {
                    mSupportedKeyList.add(key);
                    mSupportedValuesList.add(supportedValues);
                }
            }
        }
        mSupportedKeyList.add(KEY_NUMBER_OF_PAGES);
        Object[] numberOfPagesValues = new Object[10];
        for ( int i=0; i < numberOfPagesValues.length; i++ ) {
            numberOfPagesValues[i] = i + 1;
        }
        mSupportedValuesList.add(numberOfPagesValues);

        mSupportedKeyList.add(EPSLabelPrinter.KEY_COPIES);
        Object[] copiesValues = new Object[10];
        for ( int i = 0; i < copiesValues.length; i++ ) {
            copiesValues[i] = i + 1;
        }
        mSupportedValuesList.add(copiesValues);
        if ( mCurrentSettings.get(EPSLabelPrinter.KEY_COPIES) == null ) {
            mCurrentSettings.put(EPSLabelPrinter.KEY_COPIES, 1);
        }
        int numberOfPages = getIntent().getIntExtra(KEY_NUMBER_OF_PAGES, 1);

        mAdapter = new SettingItemAdapter(mSupportedKeyList, mSupportedValuesList, mCurrentSettings, numberOfPages);
        mAdapter.setOnSelectItemListener(new SettingItemAdapter.OnSelectItemListener() {
            @Override
            public void onSelectItem(SettingItemAdapter adapter, String itemKey, Object itemValue, Object previousItemValue) {
                if ( TextUtils.equals(itemKey, EPSLabelPrinter.KEY_PAPER_SIZE_TYPE) ) {
                    Integer intValue = (Integer)itemValue;
                    if ( intValue == EPSLabelPrinter.PAPER_SIZE_TYPE_CUSTOM ) {
                        CustomSizeDialogFragment dialog = new CustomSizeDialogFragment();
                        Integer width = (Integer)mCurrentSettings.get(EPSLabelPrinter.KEY_CUSTOM_PAPER_WIDTH);
                        if ( width != null ) {
                            dialog.setWidth(width);
                        }
                        Integer height = (Integer)mCurrentSettings.get(EPSLabelPrinter.KEY_CUSTOM_PAPER_HEIGHT);
                        if ( height != null ) {
                            dialog.setHeight(height);
                        }
                        dialog.setUnitType(mCustomSizeUnit);
                        dialog.show(getSupportFragmentManager(), dialog.getClass().getName());
                        mPreviousPaperSizeType = (Integer)previousItemValue;
                    }
                }
            }
        });
        mSettingItemsView.setAdapter(mAdapter);

        Button okButton = findViewById(R.id.button_ok);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int numberOfPages = mAdapter.getNumberOfPages();
                Intent intent = new Intent();
                intent.putExtra(KEY_CURRENT_SETTINGS, (Serializable) mCurrentSettings);
                intent.putExtra(KEY_NUMBER_OF_PAGES, numberOfPages);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    @Override
    public void onResult(CustomSizeDialogFragment dialog, int unitType, int width, int height) {
        mCustomSizeUnit = unitType;
        mCurrentSettings.put(EPSLabelPrinter.KEY_CUSTOM_PAPER_WIDTH, width);
        mCurrentSettings.put(EPSLabelPrinter.KEY_CUSTOM_PAPER_HEIGHT, height);
    }

    @Override
    public void onCancel(CustomSizeDialogFragment dialog) {
        mCurrentSettings.put(EPSLabelPrinter.KEY_PAPER_SIZE_TYPE, mPreviousPaperSizeType);
        mAdapter.notifyDataSetChanged();
    }

    private static final class SettingItemViewHolder extends RecyclerView.ViewHolder {
        public final TextView nameTextView;
        public final Spinner itemsSpinner;

        public SettingItemViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.text_setting_name1);
            itemsSpinner = itemView.findViewById(R.id.spinner_setting_items1);
        }
    }

    private static final class SettingItemAdapter extends RecyclerView.Adapter<PrintSettingsActivity.SettingItemViewHolder> {
        private static final String DEF_TYPE_STRING = "string";
        private List<String> mKeyList;
        private List<Object[]> mValuesList;
        private Map<String, Object> mCurrentSettings;
        private int mNumberOfPages = 0;
        private OnSelectItemListener mSelectItemListener = null;

        public interface OnSelectItemListener {
            void onSelectItem(SettingItemAdapter adapter, String itemKey, Object itemValue, Object previousItemValue);
        }

        public SettingItemAdapter(List<String> keyList, List<Object[]> valuesList, Map<String, Object> currentSettings, int numberOfPages) {
            mKeyList = keyList;
            mValuesList = valuesList;
            mCurrentSettings = currentSettings;
            mNumberOfPages = numberOfPages;
        }

        public void setOnSelectItemListener(OnSelectItemListener listener) {
            mSelectItemListener = listener;
        }

        public int getNumberOfPages() {
            return mNumberOfPages;
        }

        @Override
        public SettingItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_print_setting_item, parent, false);
            return new SettingItemViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull PrintSettingsActivity.SettingItemViewHolder holder, int position) {
            String key = mKeyList.get(position);
            String localizeLabelKey = String.format("%s_Label", key).toLowerCase();
            Context context = holder.itemView.getContext();
            int keyId = context.getResources().getIdentifier(localizeLabelKey, DEF_TYPE_STRING, context.getPackageName());
            holder.nameTextView.setText(localizeLabelKey);

            Object[] itemValues = mValuesList.get(position);
            List<String> localizeItemNames = new ArrayList<>();
            for ( Object itemValue : itemValues ) {
                int intValue = 0;
                if ( itemValue instanceof Integer ) {
                    intValue = (Integer) itemValue;
                } else if ( itemValue instanceof Boolean ) {
                    intValue = (Boolean) itemValue ? 1 : 0;
                }
                String localizeValueKey = String.format(Locale.getDefault(),"%s_%d", key, intValue).toLowerCase();
                keyId = context.getResources().getIdentifier(localizeValueKey, DEF_TYPE_STRING, context.getPackageName());
                String itemName = itemValue.toString();
                if ( keyId != 0 ) {
                    itemName = context.getString(keyId);
                }
                localizeItemNames.add(itemName);
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, localizeItemNames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            holder.itemsSpinner.setAdapter(adapter);

            Object currentItemValue = mCurrentSettings.get(key);
            if ( TextUtils.equals(key, KEY_NUMBER_OF_PAGES) ) {
                currentItemValue = mNumberOfPages;
            }
            int currentIndex = 0;
            if ( currentItemValue != null ) {
                currentIndex = Arrays.binarySearch(itemValues, currentItemValue);
            }
            holder.itemsSpinner.setSelection(currentIndex);
            holder.itemsSpinner.setTag(R.id.spinner_setting_items1, currentIndex);
            holder.itemsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Integer previousPosition = (Integer) parent.getTag(R.id.spinner_setting_items1);
                    if ( previousPosition != position ) {
                        Object itemValue = itemValues[position];
                        if (TextUtils.equals(key, KEY_NUMBER_OF_PAGES)) {
                            mNumberOfPages = (Integer) itemValue;
                        } else {
                            mCurrentSettings.put(key, itemValue);
                        }

                        Object previousItemValue = null;
                        if ( previousPosition != null ) {
                            previousItemValue = itemValues[previousPosition];
                        }

                        if (mSelectItemListener != null) {
                            mSelectItemListener.onSelectItem(SettingItemAdapter.this, key, itemValue, previousItemValue);
                        }
                        parent.setTag(R.id.spinner_setting_items1, position);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }

        @Override
        public int getItemCount() {
            return mKeyList.size();
        }
    }
}