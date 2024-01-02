package com.example.colorworkssdk;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.epson.ijprinter.esclabelsdk.EPSLabelPrinter;
import com.epson.ijprinter.esclabelsdk.EPSLabelPrinterDiscovery;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private EPSLabelPrinterDiscovery mDiscovery;
    private final ExecutorService mExecutorService = Executors.newSingleThreadExecutor();
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private RecyclerView mPrinterListView;
    private View mProgressContainerView_main;
    private Button mSearchButton;
    private Button mStopButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Intent intent = getIntent();
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            // Extract data from the URI
            Uri data = intent.getData();
            if (data != null) {
                String param1 = data.getQueryParameter("param1");
                String param2 = data.getQueryParameter("param2");

                // Display the data using a Toast
                showToast("Data received: Param1=" + param1 + ", Param2=" + param2);
            }
        }
        //mProgressContainerView_main.setVisibility(View.VISIBLE);
        mDiscovery = new EPSLabelPrinterDiscovery(this);
        mPrinterListView = findViewById(R.id.view_printer_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mPrinterListView.setLayoutManager(layoutManager);
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(this, layoutManager.getOrientation());
        mPrinterListView.addItemDecoration(itemDecoration);
        PrinterListAdapter adapter = new PrinterListAdapter();
        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(EPSLabelPrinter printer) {
                mDiscovery.stop();
                //Intent intent = new Intent(MainActivity.this, PrinterInfoActivity.class);
                Uri data = intent.getData();
                String param2 = data.getQueryParameter("param2");
                Intent intent = new Intent(MainActivity.this, ImagePreviewActivity.class);
                intent.putExtra("Printer", printer);
                intent.putExtra("Img",param2);
                //showToast("Data printer : " +printer);
                startActivity(intent);
            }
        });
        mPrinterListView.setAdapter(adapter);


        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (adapter.getItemCount() > 0) {
                    //mProgressContainerView_main.setVisibility(View.VISIBLE);
                    RecyclerView.ViewHolder viewHolder = mPrinterListView.findViewHolderForAdapterPosition(0);
                    if (viewHolder != null){
                        viewHolder.itemView.performClick();
                        finish();
                    }
                }
            }
        }, 2000); // Delay in milliseconds

        mSearchButton = findViewById(R.id.button_search);
        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDiscover();
            }
        });

        mStopButton = findViewById(R.id.button_stop);
        mStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDiscovery.stop();
            }
        });

        Button sdkSettingsButton = findViewById(R.id.button_sdk_settings);
        sdkSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SDKSettingsActivity.class);
                startActivity(intent);
            }
        });
        startDiscover();

    }

    private void showToast(String message) {
        // The first parameter is the context (usually, use 'this' for an activity)
        // The second parameter is the message you want to display
        // The third parameter is the duration of the Toast (either Toast.LENGTH_SHORT or Toast.LENGTH_LONG)
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void priorityToWiFiDirect(){

        final ConnectivityManager connectivityManager =
                (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager == null){
            return;
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //Android 6 or later
            if (connectivityManager.getBoundNetworkForProcess() != null) {
                return;
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //Android 5
            if (connectivityManager.getProcessDefaultNetwork() != null) {
                return;
            }
        }else{
            //Android4 or before
            //DO NOTHING
            return;
        }

        //Request WiFi communication without internet
        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        builder.removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        builder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI);

        try {
            //Need android.permission.CHANGE_NETWORK_STATE
            connectivityManager.requestNetwork(builder.build(), new ConnectivityManager.NetworkCallback(){
                @Override
                public void onAvailable(@NonNull Network network) {
                    super.onAvailable(network);
                    //Android 6
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (connectivityManager.getBoundNetworkForProcess() == null) {
                            connectivityManager.bindProcessToNetwork(network);
                        }
                    }else{
                        if (connectivityManager.getProcessDefaultNetwork() == null) {
                            connectivityManager.setProcessDefaultNetwork(network);
                        }
                    }
                }

                @Override
                public void onLost(@NonNull Network network) {
                    super.onLost(network);

                    //Android 6
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (connectivityManager.getBoundNetworkForProcess() != null) {
                            connectivityManager.bindProcessToNetwork(null);
                        }
                        //Android 5
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        if (connectivityManager.getProcessDefaultNetwork() != null) {
                            connectivityManager.setProcessDefaultNetwork(null);
                        }
                    }else{ //Android4 or later
                        //DO NOTHING
                        return;
                    }

                }
            });
        }catch (SecurityException e) {
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void startDiscover() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            priorityToWiFiDirect();
        }

        mSearchButton.setVisibility(View.GONE);
        mStopButton.setVisibility(View.VISIBLE);
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                mDiscovery.start(EPSLabelPrinterDiscovery.CONNECTION_TYPE_ALL, new EPSLabelPrinterDiscovery.PrinterListUpdateListener() {
                    @Override
                    public void onUpdatePrinterList(List<EPSLabelPrinter> printerList) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if ( printerList == null ) return;
                                PrinterListAdapter adapter = (PrinterListAdapter) mPrinterListView.getAdapter();
                                if ( adapter == null ) return;
                                adapter.setPrinterList(printerList);
                                adapter.notifyDataSetChanged();
                            }
                        });
                    }
                }, 10000);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mSearchButton.setVisibility(View.VISIBLE);
                        mStopButton.setVisibility(View.GONE);
                    }
                });
            }
        });
    }

    private static final class PrinterInfoViewHolder extends RecyclerView.ViewHolder {
        public TextView modelNameView;
        public TextView locationView;

        public PrinterInfoViewHolder(@NonNull View itemView) {
            super(itemView);
            modelNameView = itemView.findViewById(R.id.text_model_name);
            locationView = itemView.findViewById(R.id.text_location);
        }
    }

    private interface OnItemClickListener {
        void onItemClick(EPSLabelPrinter printer);
    }

    private static final class PrinterListAdapter extends RecyclerView.Adapter<PrinterInfoViewHolder> {
        private List<EPSLabelPrinter> mPrinterList = new ArrayList<>();
        private OnItemClickListener mItemClickListener = null;

        public void setPrinterList(List<EPSLabelPrinter> printerList) {
            mPrinterList = printerList;
        }

        public void setOnItemClickListener(OnItemClickListener listener) {
            mItemClickListener = listener;
        }

        @NonNull
        @Override
        public PrinterInfoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_printer_info, parent, false);
            return new PrinterInfoViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull MainActivity.PrinterInfoViewHolder holder, int position) {
            EPSLabelPrinter printer = mPrinterList.get(position);
            holder.modelNameView.setText(printer.getModelName());
            holder.locationView.setText(printer.getLocation());
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if ( mItemClickListener != null ) {
                        mItemClickListener.onItemClick(printer);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mPrinterList.size();
        }
    }
}