package com.example.finalproject.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.example.finalproject.Common.Common;
import com.example.finalproject.Interface.ItemClickListener;
import com.example.finalproject.Model.Invoice;
import com.example.finalproject.Model.Order;
import com.example.finalproject.R;
import com.example.finalproject.page.OrderDetail;
import com.example.finalproject.util.StringUtil;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


class OrderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView txt_name, txt_price, other, qty, date, status;
    public ImageView image;
    public CardView parent;

    private ItemClickListener itemClickListener;


    public OrderViewHolder(View itemView) {
        super(itemView);
        txt_name = (TextView)itemView.findViewById(R.id.name);
        txt_price = (TextView)itemView.findViewById(R.id.price);
        other = (TextView)itemView.findViewById(R.id.others);
        qty = (TextView)itemView.findViewById(R.id.qty);
        date = (TextView)itemView.findViewById(R.id.date);
        status = (TextView)itemView.findViewById(R.id.status);
        image = itemView.findViewById(R.id.image);
        parent = itemView.findViewById(R.id.parent);

    }

    @Override
    public void onClick(View v) {

    }
}

public class OrderAdapter extends RecyclerView.Adapter<OrderViewHolder>{

    private List<Invoice> listData = new ArrayList<>();
    private Context context;
    private ActivityResultLauncher<Intent> someActivityResultLauncher;
    public OrderAdapter(List<Invoice> listData, Context context, ActivityResultLauncher<Intent> someActivityResultLauncher) {
        this.listData = listData;
        this.context = context;
        this.someActivityResultLauncher = someActivityResultLauncher;
    }

    public OrderAdapter(List<Invoice> listData, Context context) {
        this.listData = listData;
        this.context = context;
        this.someActivityResultLauncher = someActivityResultLauncher;
    }

    @Override
    public OrderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.order_item_layout, parent, false);
        return new OrderViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(OrderViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        SimpleDateFormat targetDateFormat = new SimpleDateFormat("yyyy-MM-dd");

        holder.txt_price.setText(listData.get(position).getPrice());
        holder.date.setText(targetDateFormat.format(new Date(listData.get(position).getDate())));
        holder.qty.setText(listData.get(position).getOrders().get(0).getQuantity() + "Barang");
        if(listData.get(position).getOrders().size() > 1){
            holder.other.setText("+"+ (listData.get(position).getOrders().size()-1) +" Barang lainnya.");
        }else{
            holder.other.setVisibility(View.GONE);
        }
        if(listData.get(position).getStatus().equals(Common.ORDER_SUCCESS)){
            holder.status.setTextColor(context.getColor(R.color.success));
        }else if(listData.get(position).getStatus().equals(Common.ORDER_FAILED) && listData.get(position).getStatus().equals(Common.ORDER_PAYMENT_FAILED)){
            holder.status.setTextColor(context.getColor(R.color.failed));
        }else{
            holder.status.setTextColor(context.getColor(R.color.pending));
        }

        holder.status.setText(Common.ORDER_TYPE_STRING.get(listData.get(position).getStatus()));
        holder.txt_name.setText(listData.get(position).getOrders().get(0).getProduct().getName());
        holder.txt_price.setText(StringUtil.formatToIDR(listData.get(position).getPrice()));
        if(listData.get(position).getOrders().get(0).getProduct().getImage() != null &&
                !listData.get(position).getOrders().get(0).getProduct().getImage().isEmpty() &&
                !listData.get(position).getOrders().get(0).getProduct().getImage().equals(" ")){
            Picasso.with(context).load(listData.get(position).getOrders().get(0).getProduct().getImage()).into(holder.image);
        }


        holder.parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, OrderDetail.class);
                intent.putExtra("invoice", listData.get(position));
                intent.putExtra("type", 1);
                someActivityResultLauncher.launch(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return listData.size();
    }
}
