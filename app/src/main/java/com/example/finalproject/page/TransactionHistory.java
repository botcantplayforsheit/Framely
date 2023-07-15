package com.example.finalproject.page;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Insets;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.WindowMetrics;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalproject.Common.Common;
import com.example.finalproject.Model.Invoice;
import com.example.finalproject.Model.Order;
import com.example.finalproject.Model.Varian;
import com.example.finalproject.R;
import com.example.finalproject.adapter.OrderAdapter;
import com.example.finalproject.base.BaseActivity;
import com.example.finalproject.util.PreferenceUtil;
import com.example.finalproject.util.StringUtil;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


public class TransactionHistory extends BaseActivity {
    RecyclerView recyclerView;
    Toolbar toolbar;
    LinearLayout filterBtn;
    RecyclerView.LayoutManager layoutManager;
    OrderAdapter adapter;
    View bottom_sheet;
    BottomSheetBehavior sheetBehavior;
    BottomSheetDialog sheetDialog;

    FirebaseDatabase database;
    DatabaseReference requests;
    List<Invoice> orderList = new ArrayList<>();
    List<Invoice> prevOrderList = new ArrayList<>();
    Date startDate = new Date();
    Date endDate = new Date();
    SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_history);
        initEnv();
        iniView();
    }



    private void iniView(){
        recyclerView = findViewById(R.id.recycler_recap);
        toolbar = findViewById(R.id.toolbar);
        filterBtn = findViewById(R.id.filterBtn);

        bottom_sheet = findViewById(R.id.bottom_sheet);
        sheetBehavior = BottomSheetBehavior.from(bottom_sheet);
        toolbar.setTitle("History Transaksi");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        filterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBottomSheetDialog();
            }
        });
        loadOrder(requests.orderByChild("date").startAt(startDate.getTime()).endAt(endDate.getTime()));
    }
    String status = "ALL";
    public void showBottomSheetDialog() {
        View view = getLayoutInflater().inflate(R.layout.filter_popup, null);

        if (sheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
        final Calendar cldr = Calendar.getInstance();
        int day = cldr.get(Calendar.DAY_OF_MONTH);
        int month = cldr.get(Calendar.MONTH);
        int year = cldr.get(Calendar.YEAR);


        final TextView dateEndText = view.findViewById(R.id.date_end);
        final TextView dateStartText = view.findViewById(R.id.date_start);;
        final DatePickerDialog startPickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(year, monthOfYear, dayOfMonth);
                        startDate = calendar.getTime();
                        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
                        dateStartText.setText(dateFormatter.format(startDate));
                    }
                }, year, month, day);

        final DatePickerDialog endPickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(year, monthOfYear, dayOfMonth);
                        endDate = calendar.getTime();
                        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
                        dateEndText.setText(dateFormatter.format(endDate));
                    }
                }, year, month, day);
        TextView pickStartDate = view.findViewById(R.id.pick_start_date);;
        TextView pickEndDate = view.findViewById(R.id.pick_end_date);;
        pickStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPickerDialog.show();
            }
        });
        pickEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endPickerDialog.show();
            }
        });

        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
        dateStartText.setText(dateFormatter.format(startDate));
        dateEndText.setText(dateFormatter.format(endDate));

        ImageView closeBtn = view.findViewById(R.id.closeSheet);
        RadioGroup rGroup = (RadioGroup)view.findViewById(R.id.radioGroup1);
// This will get the radiobutton in the radiogroup that is checked
        RadioButton checkedRadioButton = (RadioButton)rGroup.findViewById(rGroup.getCheckedRadioButtonId());
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sheetDialog.dismiss();
            }
        });

        rGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                // This will get the radiobutton that has changed in its check state
                RadioButton checkedRadioButton = (RadioButton)group.findViewById(checkedId);
                // This puts the value (true/false) into the variable
                boolean isChecked = checkedRadioButton.isChecked();
                // If the radiobutton that has changed in check state is now checked...
                if (isChecked)
                {
                    if(checkedId == R.id.success){
                        status = Common.ORDER_SUCCESS;
                    }else if(checkedId == R.id.failed){
                        status = Common.ORDER_FAILED;
                    }else if(checkedId == R.id.pending){
                        status = Common.ORDER_WAITING_PAYMENT;
                    }else if(checkedId == R.id.waiting){
                        status = Common.ORDER_IN_REVIEW;
                    }else if(checkedId == R.id.shipping){
                        status = Common.ORDER_SHIPPING;
                    }else if(checkedId == R.id.reject){
                        status = Common.ORDER_PAYMENT_FAILED;
                    }else if(checkedId == R.id.approved){
                        status = Common.ORDER_PAYMENT_APPROVED;
                    }else if(checkedId == R.id.all){
                        status = Common.ORDER_ALL;
                    }
                }
            }
        });
        TextView okBtn = view.findViewById(R.id.okBtn);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadOrder(requests.orderByChild("date").startAt(startDate.getTime()).endAt(endDate.getTime()));
                sheetDialog.dismiss();
            }
        });
        sheetDialog = new BottomSheetDialog(this);
        sheetDialog.setContentView(view);
        sheetDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialog;
                setupFullHeight(bottomSheetDialog);
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            sheetDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        sheetDialog.show();
        sheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                sheetDialog = null;
            }
        });
    }

    private void setupFullHeight(BottomSheetDialog bottomSheetDialog) {
        FrameLayout bottomSheet = (FrameLayout) bottomSheetDialog.findViewById(R.id.design_bottom_sheet);
        BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomSheet);
        ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();

        int windowHeight = getWindowHeight(this);
        if (layoutParams != null) {
            layoutParams.height = windowHeight;
        }
        bottomSheet.setLayoutParams(layoutParams);
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    public static int getWindowHeight(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowMetrics windowMetrics = activity.getWindowManager().getCurrentWindowMetrics();
            Insets insets = windowMetrics.getWindowInsets()
                    .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars());
            return windowMetrics.getBounds().height() - insets.top - insets.bottom;
        } else {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            return displayMetrics.heightPixels;
        }
    }

    private void initEnv(){
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -30);
        database = FirebaseDatabase.getInstance();
        requests = database.getReference("Invoice");
        startDate = cal.getTime();
        endDate = new Date();
    }

    private void loadOrder(Query query){
        orderList.clear();
        recyclerView.setAdapter(adapter);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange( DataSnapshot dataSnapshot) {
                for (DataSnapshot ssn : dataSnapshot.getChildren()) {
                    Invoice sale = ssn.getValue(Invoice.class);
                    if(status.equals(Common.ORDER_ALL)){
                        addData(sale);
                    }else{
                        if(sale.getStatus().equals(status));
                    }
                }
                adapter = new OrderAdapter(orderList, TransactionHistory.this);
                recyclerView.setAdapter(adapter);
                query.removeEventListener(this);
            }

            @Override
            public void onCancelled( DatabaseError databaseError) {

            }
        }) ;

    }

    private void addData(Invoice sale){
        if(PreferenceUtil.getUser().getRole().equalsIgnoreCase(Common.ROLE_ADMIN)){
            orderList.add(sale);
        }else{
            if(sale.getBuyer().getPhone().equals(PreferenceUtil.getUser().getPhone())){
                orderList.add(sale);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem logout = menu.findItem(R.id.logout);
        logout.setVisible(false);
        return true;
    }
}
