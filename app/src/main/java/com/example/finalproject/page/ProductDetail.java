package com.example.finalproject.page;


import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.example.finalproject.Model.Product;
import com.example.finalproject.R;
import com.example.finalproject.base.BaseActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;


public class ProductDetail extends BaseActivity {
    CardView imageCard;
    ImageView imageProduct;
    EditText productPrice;
    EditText productName, desc;
    Button submitBtn;
    RelativeLayout mButtonChooseImage;
    RelativeLayout minusStock;
    RelativeLayout plusStock;
    EditText poTime;
    Toolbar toolbar;

    FirebaseDatabase database;
    DatabaseReference productTable;
    Product product;
    String productId;
    private StorageReference mStorageRef;
    private StorageTask mUploadTask;

    private Uri mImageUri;

    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);


        initData();
        initEnv();
        initView();

    }

    public void initData(){
        product =(Product) getIntent().getParcelableExtra("product");
        productId = getIntent().getStringExtra("productId");
    }

    public void initEnv(){
        database = FirebaseDatabase.getInstance();
        productTable = database.getReference("Product");
        mStorageRef = FirebaseStorage.getInstance().getReference("uploads");
    }

    public void initView(){
        toolbar =  findViewById(R.id.toolbar);
        poTime =  findViewById(R.id.poTime);
        plusStock =  findViewById(R.id.plus_stock);
        minusStock =  findViewById(R.id.minus_stock);
        mButtonChooseImage =  findViewById(R.id.button_choose_image);
        submitBtn =  findViewById(R.id.submit_btn);
        productName =  findViewById(R.id.productName);
        productPrice =  findViewById(R.id.productPrice);
        imageProduct =  findViewById(R.id.image_product);
        imageCard =  findViewById(R.id.image_card);
        desc =  findViewById(R.id.desc);

        setTitle(toolbar, "Ubah Produk");

        plusStock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int stockValue = Integer.parseInt(poTime.getText().toString())+1;
                poTime.setText(String.valueOf(stockValue));
            }
        });

        minusStock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Integer.parseInt(poTime.getText().toString())>0){
                    int stockValue = Integer.parseInt(poTime.getText().toString())-1;
                    poTime.setText(String.valueOf(stockValue));
                }
            }
        });
        if(product.getImage()!=null && !product.getImage().isEmpty()){
            imageCard.setVisibility(View.VISIBLE);
            Picasso.with(getBaseContext()).load(product.getImage()).into(imageProduct);
        }
        poTime.setText(String.valueOf(product.getPoTime()));
        productName.setText(product.getName());
        productPrice.setText(product.getPrice());
        desc.setText(product.getDesc());

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showProgress();
                productTable.child(productId).child("poTime").setValue(Integer.parseInt(poTime.getText().toString()));
                productTable.child(productId).child("name").setValue(productName.getText().toString());
                productTable.child(productId).child("price").setValue(productPrice.getText().toString());
                productTable.child(productId).child("desc").setValue(desc.getText().toString());

                if (mImageUri != null) {
                    StorageReference fileReference = mStorageRef.child(System.currentTimeMillis()
                            + "." + getFileExtension(mImageUri));

                    mUploadTask = fileReference.putFile(mImageUri)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                                    taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                        @Override
                                        public void onComplete( Task<Uri> task) {
                                            String url = task.getResult().toString();
                                            productTable.child(productId).child("image").setValue(url);
                                            disProgress();
                                            Toast.makeText(ProductDetail.this, "Berhasil Merubah Produk", Toast.LENGTH_SHORT).show();
                                            finish();
                                        }
                                    });

                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure( Exception e) {
                                    Toast.makeText(ProductDetail.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
//                                    mProgressBar.setProgress((int) progress);

                                }
                            });
                }else{
                    disProgress();
                    Toast.makeText(ProductDetail.this, "Berhasil Merubah Produk", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });

        mButtonChooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, PICK_IMAGE_REQUEST);
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            mImageUri = data.getData();
            Picasso.with(this).load(mImageUri).into(imageProduct);
        }
    }
    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

}
