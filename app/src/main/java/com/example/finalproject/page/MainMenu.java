package com.example.finalproject.page;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.finalproject.Common.Common;
import com.example.finalproject.MainActivity;
import com.example.finalproject.R;
import com.example.finalproject.util.PreferenceUtil;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;



public class MainMenu extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    CardView addProduct;
    CardView shop;
    CardView updateProduct;
    CardView createQR;
    CardView recap;
    Toolbar toolbar;
    TextView txtFullName, editProfile;
    ImageView myImage;
    CardView transactionHistory;

    DatabaseReference table_stock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        table_stock = database.getReference("test");
        initView();
    }

    private void initView(){
        toolbar = findViewById(R.id.toolbar);
        addProduct = findViewById(R.id.add_product);
        createQR = findViewById(R.id.create_qr);
        shop = findViewById(R.id.shop);
        updateProduct = findViewById(R.id.update_product);
        recap = findViewById(R.id.recap);
        transactionHistory = findViewById(R.id.transactionHistory);
        toolbar.setTitle("Main Menu");
        setSupportActionBar(toolbar);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View headerView = navigationView.getHeaderView(0);
        txtFullName = headerView.findViewById(R.id.user_name);
        txtFullName.setText(PreferenceUtil.getUser().getName());
        editProfile = headerView.findViewById(R.id.edit_profile);
        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainMenu.this, EditProfile.class);
                startActivity(intent);
            }
        });
        transactionHistory.setOnClickListener(view -> {
            Intent intent = new Intent(MainMenu.this, TransactionHistory.class);
            startActivity(intent);
        });
        myImage = headerView.findViewById(R.id.my_image);
        if(!PreferenceUtil.getUser().getImage().isEmpty() && !PreferenceUtil.getUser().getImage().equals(" ")){
            Picasso.with(this).load(PreferenceUtil.getUser().getImage()).into(myImage);
        }

        addProduct.setOnClickListener(view -> {
            Intent intent = new Intent(MainMenu.this, AddProduct.class);
            startActivity(intent);
        });

        recap.setOnClickListener(view -> {
            Intent intent = new Intent(MainMenu.this, RekapOption.class);
            startActivity(intent);
        });

        createQR.setOnClickListener(view -> {
            Intent intent = new Intent(MainMenu.this, ProductList.class);
            intent.putExtra("page",Common.PAGE_CREATE_QR);
            startActivity(intent);
        });


        shop.setOnClickListener(view -> {
            Intent intent = new Intent(MainMenu.this, Cart.class);
            intent.putExtra("page",Common.PAGE_SHOP);
            startActivity(intent);
        });


        updateProduct.setOnClickListener(view -> {
            Intent intent = new Intent(MainMenu.this, ProductList.class);
            intent.putExtra("page",Common.PAGE_UPDATE_PRODUCT);
            startActivity(intent);
        });

    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_log_out) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            PreferenceUtil.clearAll();
            startActivity(intent);
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
