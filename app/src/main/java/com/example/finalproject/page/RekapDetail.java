package com.example.finalproject.page;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalproject.Common.Common;
import com.example.finalproject.Interface.ItemClickListener;
import com.example.finalproject.MainActivity;
import com.example.finalproject.Model.Invoice;
import com.example.finalproject.Model.Order;
import com.example.finalproject.Model.Product;
import com.example.finalproject.R;
import com.example.finalproject.adapter.CartAdapter;
import com.example.finalproject.adapter.OrderAdapter;
import com.example.finalproject.base.BaseActivity;
import com.example.finalproject.holder.ShoeViewHolder;
import com.example.finalproject.page.scanner.OrderItem;
import com.example.finalproject.util.PreferenceUtil;
import com.example.finalproject.util.StringUtil;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import com.github.mikephil.charting.data.Entry;
public class RekapDetail extends BaseActivity {
    RecyclerView recyclerView;
    LineChart lineChart;
    Toolbar toolbar;
    TextView totalTxt;
    RecyclerView.LayoutManager layoutManager;
    OrderAdapter adapter;


    FirebaseDatabase database;
    DatabaseReference requests;
    List<Invoice> orderList = new ArrayList<>();
    List<Invoice> prevOrderList = new ArrayList<>();
    List<Entry> chartList = new ArrayList<>();
    List<Entry> prevChartList = new ArrayList<>();
    Date startDate = new Date();
    Date endDate = new Date();
    SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
    String typeRecap = "Individual";
    String productId = "";
    int total = 0;
    int prevTotal = 0;
    int max = 0;
    Calendar calendar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recap_detail);
        initEnv();
        iniView();
    }

    private void iniView(){
        toolbar =  findViewById(R.id.toolbar);
        lineChart =  findViewById(R.id.lineChart);
        recyclerView =  findViewById(R.id.recycler_recap);
        totalTxt =  findViewById(R.id.total_txt);

        toolbar.setTitle("Rekap");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        loadOrder(requests.orderByChild("date").startAt(calendar.getTime().getTime()).endAt(endDate.getTime()));

//        List<Entry> kasus = new  ArrayList<Entry>();
//        kasus.add(new Entry(1, 149));
//        kasus.add(new Entry(2, 149));
//        kasus.add(new Entry(3, 196));
//        kasus.add(new Entry(4, 106));
//        kasus.add(new Entry(5, 181));
//        kasus.add(new Entry(6, 218));
//
//
//
//        List<Entry> sembuh = new ArrayList<Entry>();
//        sembuh.add(new Entry(1, 149));
//        sembuh.add(new Entry(2, 22));
//        sembuh.add(new Entry(3, 9));
//        sembuh.add(new Entry(4, 16));
//        sembuh.add(new Entry(5, 14));
//        sembuh.add(new Entry(6, 28));



    }

    private void initEnv(){
        long diffStart = endDate.getTime() - startDate.getTime();
        long dayStart = TimeUnit.DAYS.convert(diffStart, TimeUnit.MILLISECONDS);
        calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        calendar.add(Calendar.DAY_OF_MONTH,-(int) (dayStart+1));

        typeRecap = getIntent().getStringExtra("type");
        database = FirebaseDatabase.getInstance();
        requests = database.getReference("Order");
        productId = getIntent().getStringExtra("productId");
        try {
            startDate = dateFormatter.parse(getIntent().getStringExtra("startDate"));
            endDate = dateFormatter.parse(getIntent().getStringExtra("endDate"));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        long diff = endDate.getTime() - startDate.getTime();
        long day = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        calendar.add(Calendar.DAY_OF_MONTH,-(int) (day+1));
        for(int i = 1;i<=(int) (day+1);i++){
            chartList.add(new Entry(i, 0));
            prevChartList.add(new Entry(i, 0));
        }
    }

    private void loadOrder(Query query){
        recyclerView.setAdapter(adapter);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ssn : dataSnapshot.getChildren()) {
                    Invoice sale = ssn.getValue(Invoice.class);
                    if(!productId.isEmpty()){
                        if(!productId.isEmpty()){
                            for(Order order : sale.getOrders()){
                                if(order.getProduct().getProductId().equals(productId)){
                                    addData(sale);
                                }
                            }
                        }else{
                            addData(sale);
                        }
                    }else{
                        addData(sale);
                    }


                }
                Calendar endPrevDate = Calendar.getInstance();
                endPrevDate.setTime(startDate);
                endPrevDate.add(Calendar.DAY_OF_MONTH,-1);
//                prevTotalTxt.setText(dateFormatter.format(calendar.getTime())+" - "+dateFormatter.format(endPrevDate.getTime())+": "+StringUtil.formatToIDR(String.valueOf(prevTotal)));
                totalTxt.setText(dateFormatter.format(startDate)+" - "+dateFormatter.format(endDate)+": "+StringUtil.formatToIDR(String.valueOf(total)));

                LineDataSet kasusLineDataSet = new LineDataSet(chartList, dateFormatter.format(startDate)+" - "+dateFormatter.format(endDate));
                kasusLineDataSet.setMode( LineDataSet.Mode.CUBIC_BEZIER);
                kasusLineDataSet.setColor(Color.BLUE);
                kasusLineDataSet.setCircleRadius(5f);
                kasusLineDataSet.setCircleColor(Color.BLUE);

                LineDataSet sembuhLineDataSet = new LineDataSet(prevChartList, dateFormatter.format(calendar.getTime())+" - "+dateFormatter.format(endPrevDate.getTime()));
                sembuhLineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                sembuhLineDataSet.setColor(Color.GREEN);
                sembuhLineDataSet.setCircleRadius(5f);
                sembuhLineDataSet.setCircleColor(Color.GREEN);


                Legend legend = lineChart.getLegend();
                legend.setEnabled(true);
                legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
                legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
                legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
                legend.setDrawInside(false);

                YAxis leftAxis = lineChart.getAxisLeft();
                leftAxis.removeAllLimitLines();
                leftAxis.setAxisMaximum(max);
                leftAxis.setAxisMinimum(0f);
                leftAxis.enableGridDashedLine(10f, 10f, 0f);
                leftAxis.setDrawZeroLine(false);
                leftAxis.setDrawLimitLinesBehindData(false);


                lineChart.getDescription().setEnabled(false);
                lineChart.getXAxis().setPosition( XAxis.XAxisPosition.BOTTOM);
                lineChart.setData(new LineData(kasusLineDataSet, sembuhLineDataSet));
                lineChart.animateXY(100, 500);
                adapter = new OrderAdapter(orderList, RekapDetail.this);
                recyclerView.setAdapter(adapter);
                List<String> date =new  ArrayList<String>();
                String[] stockArr = new String[date.size()];
                stockArr = date.toArray(stockArr);
                ValueFormatter tanggal = new AxisDateFormatter(date);
                lineChart.getXAxis().setValueFormatter(tanggal);

                adapter = new OrderAdapter(orderList, RekapDetail.this);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        }) ;

    }


    private void addData(Invoice sale){
        Date date = new Date(sale.getDate());
        if(max<Integer.parseInt(sale.getPrice())){
            max = Integer.parseInt(sale.getPrice());
        }
        if(date.equals(startDate)||date.after(startDate)){
            long diff = date.getTime() - startDate.getTime();
            long day = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
            chartList.get((int) day).setY((chartList.get((int) day).getY()+Float.parseFloat(sale.getPrice())));
            orderList.add(sale);
            total += Integer.parseInt(sale.getPrice());
        }else{
            prevOrderList.add(sale);
            prevTotal += Integer.parseInt(sale.getPrice());

            long diff = date.getTime() - calendar.getTime().getTime();
            long day = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
            prevChartList.get((int) day).setY((prevChartList.get((int) day).getY()+Integer.parseInt(sale.getPrice())));
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
