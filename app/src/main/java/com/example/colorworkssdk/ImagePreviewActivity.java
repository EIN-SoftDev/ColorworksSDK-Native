package com.example.colorworkssdk;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.epson.ijprinter.esclabelsdk.EPSLabelPrinter;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImagePreviewActivity extends AppCompatActivity {
    public static final String KEY_IMAGE_URI = "ImageUri";
    private static final int DEFAULT_BASE_DPI = 300;
    private static final float MM_PER_INCH = 25.4f;
    public static final String KEY_PRINTER = "Printer";
    private static final int REQUEST_PRINT_SETTINGS = 100;
    private final ExecutorService mExecutorService = Executors.newSingleThreadExecutor();
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private Map<String, Object> mPrintSettings = null;
    private int mNumberOfPages = 1;
    private boolean mIsPrinting = false;
    private View mProgressContainerView;
    private TextView mProgressMessageView;
    private Button mPrintButton;
    private Button mPrintSettingsButton;

    private EPSLabelPrinter mPrinter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_preview);

        mPrinter = getIntent().getParcelableExtra(KEY_PRINTER);
        mPrinter.prepareForUSBConnection(this);
        mPrintSettings = mPrinter.getDefaultPrintSettings();

        float widthF = Float.parseFloat("106.0");
        float heightF = Float.parseFloat("76.0");
        int width = Math.round(widthF / MM_PER_INCH * DEFAULT_BASE_DPI);
        int height = Math.round(heightF / MM_PER_INCH * DEFAULT_BASE_DPI);
        mPrintSettings.put(EPSLabelPrinter.KEY_CUSTOM_PAPER_WIDTH, width);
        mPrintSettings.put(EPSLabelPrinter.KEY_CUSTOM_PAPER_HEIGHT, height);

        ImageView imageView = findViewById(R.id.image_preview);
        String url = getIntent().getStringExtra("Img");

        String newURL = "https://solver-squad.com/visitor_card.png";

        //String newURL = "https://insatsu.solver-squad.com/assets/images/marketing/kiosk_icon.png";
        showToast(url);
        Picasso.get().load(newURL).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                // Now you have the Bitmap, and you can use it as needed
                imageView.setImageBitmap(bitmap);
                if ( bitmap == null || mIsPrinting ) {
                    return;
                }

                mProgressContainerView.setVisibility(View.VISIBLE);
                mPrintButton.setEnabled(false);
                mPrintSettingsButton.setEnabled(false);
                mIsPrinting = true;
                mExecutorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        int result = printBitmap(bitmap);
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                String resultStr = getResultString(result);
                                Toast toast = Toast.makeText(ImagePreviewActivity.this, resultStr, Toast.LENGTH_LONG);
                                toast.setGravity(Gravity.CENTER, 0, 0);
                                toast.show();

                                mIsPrinting = false;
                                mProgressContainerView.setVisibility(View.GONE);
                                mPrintButton.setEnabled(true);
                                mPrintSettingsButton.setEnabled(true);
                                finish();
                            }
                        });
                    }
                });
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                // Handle failure here
                showToast("failed :"+e.getMessage());
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                // Handle loading preparation here
                //showToast(placeHolderDrawable.toString());
            }
        });


        //Uri uri = getIntent().getParcelableExtra(KEY_IMAGE_URI);
        Bitmap bitmap = null;
        /*try {
            ParcelFileDescriptor parcelFileDescriptor = getContentResolver().openFileDescriptor(uri, "r");
            bitmap = BitmapFactory.decodeFileDescriptor(parcelFileDescriptor.getFileDescriptor());
            imageView.setImageBitmap(bitmap);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }*/

        mPrintButton = findViewById(R.id.button_print);
        Bitmap finalBitmap = bitmap;
        mPrintButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( finalBitmap == null || mIsPrinting ) {
                    return;
                }

                mProgressContainerView.setVisibility(View.VISIBLE);
                mPrintButton.setEnabled(false);
                mPrintSettingsButton.setEnabled(false);
                mIsPrinting = true;
                mExecutorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        int result = printBitmap(finalBitmap);
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                String resultStr = getResultString(result);
                                Toast toast = Toast.makeText(ImagePreviewActivity.this, resultStr, Toast.LENGTH_LONG);
                                toast.setGravity(Gravity.CENTER, 0, 0);
                                toast.show();

                                mIsPrinting = false;
                                mProgressContainerView.setVisibility(View.GONE);
                                mPrintButton.setEnabled(true);
                                mPrintSettingsButton.setEnabled(true);

                            }
                        });
                    }
                });
            }
        });

        mPrintSettingsButton = findViewById(R.id.button_print_settings);
        mPrintSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( !mIsPrinting ) {
                    Intent intent = new Intent(ImagePreviewActivity.this, PrintSettingsActivity.class);
                    intent.putExtra(PrintSettingsActivity.KEY_PRINTER, mPrinter);
                    intent.putExtra(PrintSettingsActivity.KEY_CURRENT_SETTINGS, (Serializable) mPrintSettings);
                    intent.putExtra(PrintSettingsActivity.KEY_NUMBER_OF_PAGES, mNumberOfPages);
                    startActivityForResult(intent, REQUEST_PRINT_SETTINGS);
                }
            }
        });

        mProgressContainerView = findViewById(R.id.view_progress_container);
        mProgressContainerView.setVisibility(View.GONE);

        Button cancelButton = findViewById(R.id.button_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPrinter.cancelPrint();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PRINT_SETTINGS && resultCode == RESULT_OK && data != null) {
            mPrintSettings = (Map<String, Object>)data.getSerializableExtra(PrintSettingsActivity.KEY_CURRENT_SETTINGS);
            mNumberOfPages = data.getIntExtra(PrintSettingsActivity.KEY_NUMBER_OF_PAGES, 1);
        }
    }

    private int printBitmap(Bitmap bitmap) {
        RectF bitmapRect = new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight());
        Paint bitmapPaint = new Paint();
        int result = mPrinter.print(mPrintSettings, new EPSLabelPrinter.Renderer() {
            @Override
            public boolean draw(Canvas canvas, int pageIndex, int pageWidth, int pageHeight, Rect targetRect) {
                RectF pageRect = new RectF(0, 0, pageWidth, pageHeight);
                Matrix matrix = new Matrix();
                matrix.setRectToRect(bitmapRect, pageRect, Matrix.ScaleToFit.CENTER);
                canvas.drawBitmap(bitmap, matrix, bitmapPaint);
                String pageNoText = String.format(Locale.getDefault(),"Page %d", pageIndex + 1);
                //drawTextAtCenter(canvas, pageWidth, pageHeight, pageNoText);
                return pageIndex < mNumberOfPages - 1;
            }
        }, new EPSLabelPrinter.ProgressListener() {
            @Override
            public void onProgress(int pageIndex) {

            }
        });
        return result;
    }

    private String getResultString(int result) {
        String str = null;
        switch ( result ) {
            case EPSLabelPrinter.PRINT_RESULT_SUCCESS:
                str = "SUCCESS";
                break;
            case EPSLabelPrinter.PRINT_RESULT_CONNECTION_ERROR:
                str = "CONNECTION_ERROR";
                break;
            case EPSLabelPrinter.PRINT_RESULT_USER_CANCEL:
                str = "USER_CANCEL";
                break;
            case EPSLabelPrinter.PRINT_RESULT_TIMEOUT_ERROR:
                str = "TIMEOUT_ERROR";
                break;
            case EPSLabelPrinter.PRINT_RESULT_INVALID_PARAMETER_ERROR:
                str = "INVALID_PARAMETER_ERROR";
                break;
            case EPSLabelPrinter.PRINT_RESULT_OTHER_ERROR:
                str = "OTHER_ERROR";
                break;
        }
        return str;
    }

    private void drawTextAtCenter(Canvas canvas, int width, int height, String text) {
        Paint rectPaint = new Paint();
        rectPaint.setColor(Color.WHITE);
        rectPaint.setAlpha(128);
        Paint textPaint = new Paint();
        textPaint.setColor(Color.BLACK);

        textPaint.setTextSize(height * 0.02f);
        Paint.FontMetrics metrics = textPaint.getFontMetrics();
        float textHeight = metrics.bottom - metrics.top + metrics.leading;
        float textWidth = textPaint.measureText(text);
        float rectMargin = textHeight * 0.25f;
        float left = (width - (textWidth + rectMargin * 2)) / 2;
        float right = left + textWidth + rectMargin * 2;
        float top = (height - (textHeight + rectMargin * 2)) / 2;
        float bottom = top + textHeight + rectMargin * 2;
        canvas.drawRect(left, top, right, bottom, rectPaint);

        float textX = (width - textWidth) / 2;
        float textY = (height - textHeight) / 2 - metrics.top;
        canvas.drawText(text, textX, textY, textPaint);
    }
    private void showToast(String message) {
        // The first parameter is the context (usually, use 'this' for an activity)
        // The second parameter is the message you want to display
        // The third parameter is the duration of the Toast (either Toast.LENGTH_SHORT or Toast.LENGTH_LONG)
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}