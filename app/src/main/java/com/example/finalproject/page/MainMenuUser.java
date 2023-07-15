package com.example.finalproject.page;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SearchView;

import com.example.finalproject.Interface.ItemClickListener;
import com.example.finalproject.MainActivity;
import com.example.finalproject.Model.Product;
import com.example.finalproject.R;
import com.example.finalproject.holder.ShoeViewHolder;
import com.example.finalproject.page.scanner.OrderItem;
import com.example.finalproject.util.PreferenceUtil;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;



public class MainMenuUser extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    Toolbar toolbar;
    ImageView myImage;
    FirebaseRecyclerAdapter<Product, ShoeViewHolder> adapter;

    DatabaseReference table_stock;

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference productList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu_user);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        table_stock = database.getReference("test");
        iniEnv();
        initView();
    }

    private void iniEnv(){
        database = FirebaseDatabase.getInstance();

        productList = database.getReference("Product");

    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.main, menu);
//        return true;
//    }


    private void initView(){
        toolbar = findViewById(R.id.toolbar);

        toolbar.setTitle("Daftar Produk");
        setSupportActionBar(toolbar);

        recyclerView = (RecyclerView)findViewById(R.id.recycler_shoe);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        loadListProduct(productList);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View headerView = navigationView.getHeaderView(0);

        myImage = headerView.findViewById(R.id.my_image);
        if(!PreferenceUtil.getUser().getImage().isEmpty() && !PreferenceUtil.getUser().getImage().equals(" ")){
            Picasso.with(this).load(PreferenceUtil.getUser().getImage()).into(myImage);
        }

    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.logout:
//                Intent intent = new Intent(this, MainActivity.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//                PreferenceUtil.clearAll();
//                startActivity(intent);
//                finish();
//            default:
//                return super.onOptionsItemSelected(item);
//        }
//    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

//        if (id == R.id.nav_edit_profile) {
//
//        } else
        if (id == R.id.nav_log_out) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            PreferenceUtil.clearAll();
            startActivity(intent);
            finish();
        }
        if (id == R.id.cart) {
            Intent intent = new Intent(this, Cart.class);
            startActivity(intent);
        }

        if (id == R.id.history) {
            Intent intent = new Intent(this, TransactionHistory.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void loadListProduct(Query query) {
        adapter = new FirebaseRecyclerAdapter<Product, ShoeViewHolder>(Product.class,
                R.layout.shoe_item,
                ShoeViewHolder.class,
                query) {
            @Override
            protected void populateViewHolder(ShoeViewHolder viewHolder, final Product model, final int position) {
                viewHolder.shoe_name.setText(model.getName());
                if(!model.getImage().equals("") && !model.getImage().equals(" ")){
                    Picasso.with(getBaseContext()).load(model.getImage()).into(viewHolder.shoe_image);
                }

                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, final int position, boolean isLongCLick) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent(MainMenuUser.this, OrderItem.class);
                                intent.putExtra("productId", adapter.getRef(position).getKey());
                                intent.putExtra("product", (Parcelable) model);
                                startActivity(intent);
                            }
                        });
                    }
                });
            }
        };

        recyclerView.setAdapter(adapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.scan, menu);
        MenuItem item = menu.findItem(R.id.menuSearch);
//        MenuItem scan = menu.findItem(R.id.scanBtn);
//        scan.setVisible(false);

        SearchView searchView = (SearchView)item.getActionView();
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                loadListProduct(productList);
                return false;
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                loadListProduct(productList.orderByChild("name").startAt(query).endAt(query+"\uf8ff"));
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                loadListProduct(productList.orderByChild("name").startAt(newText).endAt(newText+"\uf8ff"));
                return false;
            }
        });
        return true;
    }
}
