package com.example.finalproject.page.scanner;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.example.finalproject.Common.Common;
import com.example.finalproject.Model.Invoice;
import com.example.finalproject.Model.Order;
import com.example.finalproject.Model.Product;
import com.example.finalproject.Model.Varian;
import com.example.finalproject.R;
import com.example.finalproject.base.BaseActivity;
import com.example.finalproject.page.Cart;
import com.example.finalproject.page.OrderDetail;
import com.example.finalproject.page.ProductDetail;
import com.example.finalproject.page.ProductList;
import com.example.finalproject.page.SignUp;
import com.example.finalproject.util.PreferenceUtil;
import com.example.finalproject.util.StringUtil;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class OrderItem extends BaseActivity {
    Spinner linenOption;
    Spinner glassOption;
    Spinner mattboardOption;

    EditText height;
    CardView placeHolder;
    ImageView image;
    EditText width;
    Product result;
    Varian size;
    Varian mattboard;
    Varian linen;
    Varian glass;
    Toolbar toolbar;
    TextView totalPrice;
    TextView weightTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_add_to_cart);
        PreferenceUtil.setContext(this);
        setTitle("Tambah Produk");
        result = getIntent().getParcelableExtra("product");
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Beli Produk");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        List<Order> orders = PreferenceUtil.getOrders();
        width = findViewById(R.id.width);
        image = findViewById(R.id.image);
        placeHolder = findViewById(R.id.image_card);
        height = findViewById(R.id.height);
        mattboardOption = findViewById(R.id.mattboard_option);
        glassOption = findViewById(R.id.glass_option);
        linenOption = findViewById(R.id.linen_option);
        glassOption = findViewById(R.id.glass_option);
        totalPrice = findViewById(R.id.totalPrice);
        weightTxt = findViewById(R.id.weight);

        height.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                calculateTotalPrice();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        width.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                calculateTotalPrice();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        for(Order order : orders){
            if(order.getProduct().getProductId().equals(result.getProductId())){
                orderQuantityCart = Integer.parseInt(order.getQuantity());
            }
        }
        if(!result.getImage().equals("") && !result.getImage().equals(" ")){
            Picasso.with(this).load(result.getImage()).into(image);
            placeHolder.setVisibility(View.VISIBLE);
        }

        Button cancelBtn = findViewById(R.id.cancel_button);
        Button addBtn = findViewById(R.id.add_btn);

        TextView name = findViewById(R.id.product_name);
        TextView desc = findViewById(R.id.desc);

        final EditText stockEdt = findViewById(R.id.product_stock_edt);
        RelativeLayout minusStock = findViewById(R.id.minus_stock);
        RelativeLayout plusStock = findViewById(R.id.plus_stock);



        plusStock.setOnClickListener(view -> {
            int stockValue = Integer.parseInt(stockEdt.getText().toString())+1;
            stockEdt.setText(String.valueOf(stockValue));
        });

        minusStock.setOnClickListener(view -> {
            if(Integer.parseInt(stockEdt.getText().toString())>0){
                int stockValue = Integer.parseInt(stockEdt.getText().toString())-1;
                stockEdt.setText(String.valueOf(stockValue));
            }
        });


        name.setText(result.getName());
        desc.setText(result.getDesc());

        addBtn.setOnClickListener(view -> {
            if(width.getText().toString().isEmpty() || height.getText().toString().isEmpty() && stockEdt.getText().toString().isEmpty()){
                Toast.makeText(OrderItem.this, "Mohon mengisi data yang diperlukan.", Toast.LENGTH_SHORT).show();
                return;
            }
            List<Order> orders1 = PreferenceUtil.getOrders();
            boolean found = false;
            for(int i = 0; i< orders1.size(); i++){
                if(orders1.get(i).getProduct().getProductId().equals(result.getProductId()) &&
                        orders1.get(i).getHeight() == Integer.parseInt(height.getText().toString())&&
                        orders1.get(i).getHeight() == Integer.parseInt(width.getText().toString())&&
                        orders1.get(i).getLinen().getId() == linen.getId()&&
                        orders1.get(i).getMattboard().getId() == mattboard.getId()&&
                        orders1.get(i).getGlass().getId() == glass.getId()){
                    found = true;

                    orders1.get(i).setQuantity(String.valueOf(
                            Integer.parseInt(orders1.get(i).getQuantity())
                                    + Integer.parseInt(stockEdt.getText().toString())
                    ));
                }
            }
            Date date = new Date();
            if(size == null && linen == null && mattboard == null && glass == null){
                return;
            }

            if(!found){
                orders1.add(new Order(
                        "ORD-"+System.currentTimeMillis(),
                        result,
                        stockEdt.getText().toString(),
                        String.valueOf(total),
                        date.getTime(),
                        size,
                        mattboard,
                        linen,glass,
                        weight,
                        Integer.parseInt(width.getText().toString()),
                        Integer.parseInt(height.getText().toString()),
                        result.getPoTime()
                ));
            }
            PreferenceUtil.setOrders(orders1);
            finish();
        });

        cancelBtn.setOnClickListener(view -> {
            AlertDialog.Builder alertDialog2 = new AlertDialog.Builder(
                    OrderItem.this);

            alertDialog2.setTitle("Konfirmasi");

            alertDialog2.setMessage("Apakah pesanan sudah sesuai?");


            alertDialog2.setPositiveButton("Sesuai",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Date date = new Date();
                            Order order = new Order(
                                    "ORD-"+System.currentTimeMillis(),
                                    result,
                                    stockEdt.getText().toString(),
                                    String.valueOf(total),
                                    date.getTime(),
                                    size,
                                    mattboard,
                                    linen,glass,
                                    weight,
                                    Integer.parseInt(width.getText().toString()),
                                    Integer.parseInt(height.getText().toString()),
                                    result.getPoTime()
                            );
                            List<Order> orders12 = new ArrayList<>();
                            orders12.add(order);
                            Invoice invoice = new Invoice(
                                    "INV-"+System.currentTimeMillis(),
                                    PreferenceUtil.getUser(),
                                    orders12,
                                    "",
                                    String.valueOf(total),
                                    date.getTime(),
                                    Common.ORDER_WAITING_PAYMENT,
                                    PreferenceUtil.getUser().getAddress()
                            );

                            DatabaseReference invoiceRef = database.getReference("Invoice");
                            DatabaseReference orderRef = database.getReference("Order");

                            invoiceRef.child(invoice.getId()).setValue(invoice);
                            orderRef.child(order.getId()).setValue(order);

                            Intent intent = new Intent(OrderItem.this, OrderDetail.class);
                            intent.putExtra("invoice", invoice);
                            startActivity(intent);
                        }
                    });

            alertDialog2.setNegativeButton("Tidak",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

            alertDialog2.show();
        });
        database = FirebaseDatabase.getInstance();

        varianList = database.getReference("Varian");
        loadVarian(varianList);


    }
    int total = 0;
    double weight = 0;

    private void initOption(List<Varian> list, Spinner spinner, final String type){
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(type.equals(Common.VARIAN_LINEN)){
                    linen = linenList.get(position);
                }else if(type.equals(Common.VARIAN_GLASS)){
                    glass = glassList.get(position);
                }else if(type.equals(Common.VARIAN_SIZE)){
                    size = sizeList.get(position);
                }else if(type.equals(Common.VARIAN_MATBOARD)){
                    mattboard = mattboardList.get(position);
                }
                calculateTotalPrice();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ArrayAdapter<Varian> glassAdapter = new ArrayAdapter<Varian>(this, android.R.layout.simple_spinner_item, list);

        glassAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(glassAdapter);
    }

    int orderQuantityCart = 0;
    DatabaseReference varianList;
    FirebaseDatabase database;
    List<Varian> glassList = new ArrayList<>();
    List<Varian> mattboardList = new ArrayList<>();
    List<Varian> linenList = new ArrayList<>();
    List<Varian> sizeList = new ArrayList<>();
    private void loadVarian(final Query query){
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ssn : dataSnapshot.getChildren()) {
                    Varian varian = ssn.getValue(Varian.class);
                    if(varian.getType().equals(Common.VARIAN_GLASS)){
                        glassList.add(varian);
                    }else if(varian.getType().equals(Common.VARIAN_MATBOARD)){
                        mattboardList.add(varian);

                    }else if(varian.getType().equals(Common.VARIAN_LINEN)){
                        linenList.add(varian);

                    }

                }
                glass = glassList.get(0);
                mattboard = mattboardList.get(0);
                linen = linenList.get(0);

                initOption(glassList, glassOption, Common.VARIAN_GLASS);
                initOption(mattboardList, mattboardOption, Common.VARIAN_MATBOARD);
                initOption(linenList, linenOption, Common.VARIAN_LINEN);
                query.removeEventListener(this);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        }) ;

    }

    private void calculateTotalPrice(){
        if(width.getText().toString().isEmpty() || height.getText().toString().isEmpty()){
            return;
        }
        weight = (Double.parseDouble(height.getText().toString()) * Double.parseDouble(height.getText().toString()) * 5) / 6000;
        int unitSize = Integer.parseInt(height.getText().toString()) * Integer.parseInt(height.getText().toString()) * 2;
        int glassPrice = (unitSize * glass.getBasePrice());
        int basePrice = (unitSize * Integer.parseInt(result.getPrice()));
        int linenPrice = (unitSize * linen.getBasePrice());
        int mattboardPrice = (unitSize * mattboard.getBasePrice());

        total = glassPrice + basePrice + linenPrice + mattboardPrice;
        totalPrice.setText(StringUtil.formatToIDR(String.valueOf(total)));
        weightTxt.setText(String.valueOf(weight));
    }
    private static int resolveDialogTheme(Context context) {
        TypedValue outValue = new TypedValue();
        return outValue.resourceId;
    }
}
