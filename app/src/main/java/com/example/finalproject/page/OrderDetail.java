package com.example.finalproject.page;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.example.finalproject.Common.Common;
import com.example.finalproject.Model.Invoice;
import com.example.finalproject.Model.Option;
import com.example.finalproject.Model.Order;
import com.example.finalproject.Model.Product;
import com.example.finalproject.Model.Varian;
import com.example.finalproject.R;
import com.example.finalproject.base.BaseActivity;
import com.example.finalproject.util.PreferenceUtil;
import com.example.finalproject.util.StringUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;


public class OrderDetail extends BaseActivity {
    Toolbar toolbar;
    TextView orderId;
    TextView price;
    TextView filename;
    TextView status;
    TextView date;
    TextView totalPrice;
    TextView noResi;
    TextView address;
    TextView shippingPrice;

    private static final int PICK_IMAGE_REQUEST = 1;
    Button submitBtn;
    Button cancelBtn;

    private Uri mImageUri;
    TextView btnUpload;

    private StorageTask mUploadTask;
    private Invoice invoice;
    LinearLayout fieldLayout;
    LinearLayout orderList;
    EditText shippingField;

    private StorageReference mStorageRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);
        toolbar =  findViewById(R.id.toolbar);
        orderId =  findViewById(R.id.invoice_no);
        price =  findViewById(R.id.goods_price_text);
        filename =  findViewById(R.id.transaction_proof);
        status =  findViewById(R.id.status_text);
        date =  findViewById(R.id.date);
        totalPrice =  findViewById(R.id.price_text);
        noResi =  findViewById(R.id.noResi);
        submitBtn =  findViewById(R.id.submit_btn);
        cancelBtn =  findViewById(R.id.cancelBtn);
        btnUpload =  findViewById(R.id.btnUpload);
        fieldLayout =  findViewById(R.id.fieldLayout);
        orderList =  findViewById(R.id.orderList);
        shippingField =  findViewById(R.id.shippingInput);
        address = findViewById(R.id.address);
        shippingPrice = findViewById(R.id.shipping_price_text);

        setTitle(toolbar, "Detail Pembelian");


        submitBtn = findViewById(R.id.submit_btn);
        invoice = getIntent().getParcelableExtra("invoice");
        url = invoice.getImageTransaction();
        mStorageRef = FirebaseStorage.getInstance().getReference("uploads");
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference table = database.getReference("Invoice");
        SimpleDateFormat targetDateFormat = new SimpleDateFormat("yyyy-MM-dd");

        price.setText(StringUtil.formatToIDR(invoice.getPrice()));
        filename.setText(invoice.getImageTransaction());
        orderId.setText(invoice.getId());
        status.setText(Common.ORDER_TYPE_STRING.get(invoice.getStatus()));
        noResi.setText(invoice.getShippingReceipt());
        address.setText(PreferenceUtil.getUser().getAddress()==null?"-":PreferenceUtil.getUser().getAddress());

        date.setText(targetDateFormat.format(new Date(invoice.getDate())));
        totalPrice.setText(StringUtil.formatToIDR(String.valueOf(Integer.parseInt(invoice.getPrice())+Integer.parseInt(invoice.getShippingPrive()))));
        shippingPrice.setText(StringUtil.formatToIDR(invoice.getShippingPrive()));
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(PreferenceUtil.getUser().getRole().equalsIgnoreCase(Common.ROLE_USER)){
                    if(invoice.getStatus().equals(Common.ORDER_WAITING_PAYMENT) || invoice.getStatus().equals(Common.ORDER_PAYMENT_FAILED)){
                        invoice.setStatus(Common.ORDER_IN_REVIEW);
                    }
                    if(invoice.getStatus().equals( Common.ORDER_SHIPPING)){
                        invoice.setStatus(Common.ORDER_SUCCESS);
                    }
                }else{
                    if(invoice.getStatus().equals(Common.ORDER_PAYMENT_APPROVED)){
                        invoice.setShippingReceipt(shippingField.getText().toString());
                        invoice.setStatus(Common.ORDER_SHIPPING);
                    }else if(invoice.getStatus().equals(Common.ORDER_IN_REVIEW)){
                        invoice.setStatus(Common.ORDER_PAYMENT_APPROVED);
                    }
                }
                table.child(invoice.getId()).setValue(invoice);
                finish();
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(PreferenceUtil.getUser().getRole().equalsIgnoreCase(Common.ROLE_USER)){
                    if(invoice.getStatus().equals(Common.ORDER_WAITING_PAYMENT) || invoice.getStatus().equals(Common.ORDER_PAYMENT_FAILED)){
                        invoice.setStatus(Common.ORDER_FAILED);
                    }
                }else{
                    if(invoice.getStatus().equals(Common.ORDER_PAYMENT_APPROVED)){
                        invoice.setStatus(Common.ORDER_PAYMENT_FAILED);
                    }else if(invoice.getStatus().equals(Common.ORDER_WAITING_PAYMENT)){
                        invoice.setStatus(Common.ORDER_FAILED);
                    }
                }
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(PreferenceUtil.getUser().getRole().equalsIgnoreCase(Common.ROLE_USER)){
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(intent, PICK_IMAGE_REQUEST);
                }else{
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    startActivity(i);
                }
            }
        });

        if(PreferenceUtil.getUser().getRole().equalsIgnoreCase(Common.ROLE_USER)){
            btnUpload.setText("Upload");
            if(!invoice.getStatus().equals(Common.ORDER_PAYMENT_FAILED) || invoice.getStatus().equals(Common.ORDER_SHIPPING)){
                cancelBtn.setVisibility(View.GONE);
            }else if(!invoice.getStatus().equals(Common.ORDER_WAITING_PAYMENT)){
                cancelBtn.setVisibility(View.GONE);
                submitBtn.setVisibility(View.GONE);
            }
        }else{
            btnUpload.setText("Lihat");
            if(Objects.equals(invoice.getStatus(), Common.ORDER_PAYMENT_APPROVED)){
                fieldLayout.setVisibility(View.VISIBLE);
                cancelBtn.setVisibility(View.GONE);
            }else if(invoice.getStatus().equals(Common.ORDER_FAILED)){
                cancelBtn.setVisibility(View.GONE);
                submitBtn.setVisibility(View.GONE);
            }else if(invoice.getStatus().equals(Common.ORDER_SHIPPING)){
                submitBtn.setVisibility(View.GONE);
                cancelBtn.setVisibility(View.GONE);
            }else if(invoice.getStatus().equals(Common.ORDER_SUCCESS)){
                cancelBtn.setVisibility(View.GONE);
                submitBtn.setVisibility(View.GONE);
            }else if(invoice.getStatus().equals(Common.ORDER_PAYMENT_FAILED)){
                cancelBtn.setVisibility(View.GONE);
                submitBtn.setVisibility(View.GONE);
            }else if(invoice.getStatus().equals(Common.ORDER_WAITING_PAYMENT)){
                submitBtn.setVisibility(View.GONE);
            }
        }

        if(invoice.getStatus().equals(Common.ORDER_PAYMENT_APPROVED) ){
            submitBtn.setText("Update Nomor Resi");
            status.setText("Pembayaran Disetujui");
        }else if(invoice.getStatus().equals(Common.ORDER_FAILED)){
            status.setText("Pembelian Gagal");
            cancelBtn.setVisibility(View.GONE);
            submitBtn.setVisibility(View.GONE);
        }else if(invoice.getStatus().equals(Common.ORDER_SHIPPING)){
            submitBtn.setText("Selesaikan Pembelian");

            status.setText("Dalam Pengiriman");
        }else if(invoice.getStatus().equals(Common.ORDER_SUCCESS)){
            status.setText("Pembelian Sukses");
            cancelBtn.setVisibility(View.GONE);
            submitBtn.setVisibility(View.GONE);
        }else if(invoice.getStatus().equals(Common.ORDER_PAYMENT_FAILED)){
            submitBtn.setText("Update Pembelian");
            status.setText("Pembayaran Tidak Sesuai");
            cancelBtn.setVisibility(View.GONE);
            submitBtn.setVisibility(View.GONE);
        }else if(invoice.getStatus().equals(Common.ORDER_WAITING_PAYMENT)){
            submitBtn.setText("Update Pembelian");
            status.setText("Menunggu Pembayaran");
            submitBtn.setVisibility(View.GONE);
        }else if(invoice.getStatus().equals(Common.ORDER_IN_REVIEW)){
            submitBtn.setText("Terima Pembayaran");
            status.setText("Menunggu Review Penjual");
            cancelBtn.setVisibility(View.GONE);
        }

        optionList.add(new Option(Common.ORDER_WAITING_PAYMENT, "Menunggu Pembayaran"));
        optionList.add(new Option(Common.ORDER_IN_REVIEW, "Menunggu Review Penjual"));
        optionList.add(new Option(Common.ORDER_PAYMENT_APPROVED, "Pembayaran Disetujui"));
        optionList.add(new Option(Common.ORDER_SHIPPING, "Dalam Pengiriman"));
        optionList.add(new Option(Common.ORDER_PAYMENT_FAILED, "Pembayaran Tidak Sesuai"));
        optionList.add(new Option(Common.ORDER_FAILED, "Gagal"));
        optionList.add(new Option(Common.ORDER_SUCCESS, "Sukses"));

        for(Order order: invoice.getOrders()){
            View layout = LayoutInflater.from(this).inflate(R.layout.varian_item_layout, orderList, false);
            TextView price = layout.findViewById(R.id.price);
            TextView name = layout.findViewById(R.id.name);
            TextView qty = layout.findViewById(R.id.qty);
            ImageView image = layout.findViewById(R.id.image);
            Picasso.with(this).load(order.getProduct().getImage()).into(image);

            price.setText(order.getPrice());
            String poText = "";
            if(order.getProduct().getPoTime()>0){
                poText = " PO - " + order.getProduct().getPoTime() + "Hari";
            }
            name.setText(order.getProduct().getName()+poText);
            qty.setText(order.getQuantity() + " x " + (Integer.parseInt(order.getPrice())/Integer.parseInt(order.getQuantity())));

            orderList.addView(layout);
        }
    }
    List<Option> optionList = new ArrayList<>();
    String url = "-";
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            mImageUri = data.getData();
            if (mImageUri != null) {
                showProgress();
                StorageReference fileReference = mStorageRef.child(System.currentTimeMillis()
                        + "." + getFileExtension(mImageUri));

                mUploadTask = fileReference.putFile(mImageUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                                taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                    @Override
                                    public void onComplete( Task<Uri> task) {
                                        url = task.getResult().toString();
                                        filename.setText(url);
                                        invoice.setImageTransaction(url);
                                        Toast.makeText(OrderDetail.this, "Berhasil Mengupload Bukti Pembayaran", Toast.LENGTH_SHORT).show();
                                        disProgress();
                                    }
                                });

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure( Exception e) {
                                Toast.makeText(OrderDetail.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                                if(progress == 100.0){
                                    Log.d("finishhhh", String.valueOf(progress));
                                }
                                Log.d("progress", String.valueOf(progress));
//                                    mProgressBar.setProgress((int) progress);

                            }
                        });
            } else {
                Toast.makeText(OrderDetail.this, "No file selected", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }
}
