package com.example.finalproject.page;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalproject.Common.Common;
import com.example.finalproject.Database.Database;
import com.example.finalproject.Model.Invoice;
import com.example.finalproject.Model.Order;
import com.example.finalproject.Model.Product;
import com.example.finalproject.R;
import com.example.finalproject.adapter.CartAdapter;
import com.example.finalproject.base.BaseActivity;
import com.example.finalproject.page.scanner.CodeScannerActivity;
import com.example.finalproject.page.scanner.OrderItem;
import com.example.finalproject.util.PreferenceUtil;
import com.example.finalproject.util.StringUtil;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class Cart extends BaseActivity {
    RecyclerView recyclerView;
    Toolbar toolbar;
    TextView txtTotalPrice;
    Button btnPlace;
    Button btnClear;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference requests;
    DatabaseReference productRef;
    DatabaseReference invoiceRef;

    List<Order> cart = new ArrayList<>();
    CartAdapter adapter;
    public static final int REQUEST_CODE = 1;
    private SimpleDateFormat dateFormatter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        new Database(getBaseContext()).clearCart();
        initEnv();
        initView();
    }

    private void initEnv(){
        database = FirebaseDatabase.getInstance();
        requests = database.getReference("Order");
        productRef = database.getReference("Product");
        invoiceRef = database.getReference("Invoice");

//        DatabaseReference cities = requests.child("cities")
//        Query citiesQuery = requests.orderByKey().startAt(input).endAt(input+"\uf8ff");
    }


    private void initView(){
        recyclerView =  findViewById(R.id.listCart);
        toolbar =  findViewById(R.id.toolbar);
        txtTotalPrice =  findViewById(R.id.total);
        btnPlace =  findViewById(R.id.btnPlaceOrder);
        btnClear =  findViewById(R.id.btnClearCart);
        toolbar.setTitle("Keranjang");
        setSupportActionBar(toolbar);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        btnPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialog2 = new AlertDialog.Builder(
                        Cart.this);

                alertDialog2.setTitle("Konfirmasi");

                alertDialog2.setMessage("Apakah pesanan sudah sesuai?");


                alertDialog2.setPositiveButton("Sesuai",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Date date = new Date();

                                final List<Order> orders =  PreferenceUtil.getOrders();
                                Invoice invoice = new Invoice(
                                        "INV-"+System.currentTimeMillis(),
                                        PreferenceUtil.getUser(),
                                        orders,
                                        "",
                                        String.valueOf(total),
                                        date.getTime(),
                                        Common.ORDER_WAITING_PAYMENT,
                                        PreferenceUtil.getUser().getAddress()
                                );
                                invoiceRef.child(invoice.getId()).setValue(invoice);

                                for(final Order order : orders){
                                    requests.child(order.getId()).setValue(order);
                                    productRef.child(order.getProduct().getProductId()).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            final Product product = dataSnapshot.getValue(Product.class);
                                            productRef.child(order.getProduct().getProductId()).child("Stock").setValue(String.valueOf(Integer.parseInt(product.getStock())-Integer.parseInt(order.getQuantity())));
                                            productRef.removeEventListener(this);
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                                }
                                Toast.makeText(Cart.this,"Order Terbuat",Toast.LENGTH_LONG).show();
                                PreferenceUtil.clearOrder();
                                Intent intent = new Intent(Cart.this, OrderDetail.class);
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

            }
        });

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               PreferenceUtil.clearOrder();
                Toast.makeText(Cart.this, "Cart Cleared!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        loadListProduct();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.shop, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menuSearch:
                Intent intent = new Intent(this, ProductList.class);
                intent.putExtra("page", Common.PAGE_SHOP);
                startActivityForResult(intent, REQUEST_CODE);
                return true;
            case R.id.scanBtn:
                Intent  intent1 = new Intent(this, CodeScannerActivity.class);
                intent1.putExtra("page", Common.PAGE_SHOP);
                startActivityForResult(intent1, REQUEST_CODE);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    int total;
    private void loadListProduct() {
        cart = PreferenceUtil.getOrders();
        adapter = new CartAdapter(cart, this);
        recyclerView.setAdapter(adapter);

        for (Order order:cart)
            total+=(Integer.parseInt(order.getPrice())*Integer.parseInt(order.getQuantity()));
        Locale locale = new Locale("en", "US");
        NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);

        txtTotalPrice.setText(StringUtil.formatToIDR(String.valueOf(total)));

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == REQUEST_CODE  && resultCode  == RESULT_OK) {
                loadListProduct();
            }
        } catch (Exception ex) {
            Toast.makeText(Cart.this, ex.toString(),
                    Toast.LENGTH_SHORT).show();
        }

    }
}
