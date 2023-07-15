package com.example.finalproject.page;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.widget.Toolbar;

import java.util.Calendar;

import com.example.finalproject.Common.Common;
import com.example.finalproject.Model.Order;
import com.example.finalproject.Model.Product;
import com.example.finalproject.R;
import com.example.finalproject.base.BaseActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RekapOption extends BaseActivity {
    TextView pickStartDate;
    TextView chooseProduct;
    TextView productName;
    TextView pickEndDate;
    Button viewRecap;
    Spinner recapOption;
    TextView dateEndText;
    TextView dateStartText;
    Toolbar toolbar;

    Spinner sellTypeOption;
    public static final int REQUEST_CODE = 1;

    FirebaseDatabase database;
    DatabaseReference requests;
    List<Order> orderList = new ArrayList<>();
    Date startDate = new Date();
    Date endDate = new Date();
    SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
    DatePickerDialog startPickerDialog;
    DatePickerDialog endPickerDialog;
    Product product;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recap_option);
        initEnv();
        iniView();
    }

    private void iniView(){
        toolbar = findViewById(R.id.toolbar);
        pickStartDate = findViewById(R.id.pick_start_date);
        chooseProduct = findViewById(R.id.choose_product);
        productName = findViewById(R.id.product_name);
        pickEndDate = findViewById(R.id.pick_end_date);
        viewRecap = findViewById(R.id.view_recap);
        recapOption = findViewById(R.id.recap_option);
        dateEndText = findViewById(R.id.date_end);
        dateStartText = findViewById(R.id.date_start);
        sellTypeOption = findViewById(R.id.sell_type_option);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        chooseProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RekapOption.this, ProductList.class);
                intent.putExtra("page", Common.PAGE_RECAP);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });

        setTitle(toolbar, "Pengaturan Rekap");
        viewRecap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RekapOption.this, RekapDetail.class);
                intent.putExtra("startDate",dateFormatter.format(startDate));
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(endDate);
                calendar.add(Calendar.MINUTE,59);
                calendar.add(Calendar.HOUR_OF_DAY, 23);
                intent.putExtra("endDate",dateFormatter.format(calendar.getTime()));
//                if(dateFormatter.format(startDate).equals(dateFormatter.format(endDate))){
//                    Calendar calendar = Calendar.getInstance();
//                    calendar.setTime(endDate);
//                    calendar.add(Calendar.DAY_OF_MONTH,1);
//                    intent.putExtra("endDate",dateFormatter.format(calendar.getTime()));
//                }else{
//                    intent.putExtra("endDate",dateFormatter.format(endDate));
//                }
                if(product==null){
                    intent.putExtra("productId","");
                }else{
                    intent.putExtra("productId",product.getProductId());
                }
                intent.putExtra("type",recapOption.getSelectedItem().toString());
                intent.putExtra("sell_type",sellTypeOption.getSelectedItem().toString());
                startActivity(intent);
            }
        });

        recapOption.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        List<String> categories = new ArrayList<String>();
        categories.add("Individual");
        categories.add("Keseluruhan");

        List<String> sellType = new ArrayList<String>();
        sellType.add("Dijual");
        sellType.add("Konsumsi Pribadi");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);

        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        recapOption.setAdapter(dataAdapter);

        ArrayAdapter<String> sellAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, sellType);

        sellAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        sellTypeOption.setAdapter(sellAdapter);

        final Calendar cldr = Calendar.getInstance();
        int day = cldr.get(Calendar.DAY_OF_MONTH);
        int month = cldr.get(Calendar.MONTH);
        int year = cldr.get(Calendar.YEAR);
        // date picker dialog
        startPickerDialog = new DatePickerDialog(RekapOption.this,
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

        endPickerDialog = new DatePickerDialog(RekapOption.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(year, monthOfYear, dayOfMonth, 0, 0);
                        endDate = calendar.getTime();
                        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
                        dateEndText.setText(dateFormatter.format(endDate));
                    }
                }, year, month, day);

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
    }

    private void initEnv(){
        database = FirebaseDatabase.getInstance();
        requests = database.getReference("Order");
//        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy");
//        Date start = null;
//        Date end = null;
//        try {
//            start = dateFormatter.parse("08-05-2019");
//            end = dateFormatter.parse("07-03-2021");
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
        Query query = requests.orderByChild("date").startAt(startDate.getTime()).endAt(endDate.getTime());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ssn : dataSnapshot.getChildren()) {

                    Order sale = ssn.getValue(Order.class);
                    orderList.add(sale);

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        }) ;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem logout = menu.findItem(R.id.logout);
        logout.setVisible(false);
        return true;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == REQUEST_CODE  && resultCode  == RESULT_OK) {
                product =(Product) data.getParcelableExtra("product");
                productName.setText(product.getName());
            }
        } catch (Exception ex) {
            Toast.makeText(RekapOption.this, ex.toString(),
                    Toast.LENGTH_SHORT).show();
        }

    }

}
