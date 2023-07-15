package com.example.finalproject.page;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;


public class AddProduct extends BaseActivity {
    CardView imageHolder;
    RelativeLayout minusStock;
    RelativeLayout plusStock;
    EditText poTime;
    Toolbar toolbar;

    private static final int PICK_IMAGE_REQUEST = 1;
    EditText  name, price, desc;
    private RelativeLayout mButtonChooseImage;
    Button btnSignUp;
    private Uri mImageUri;

    private StorageTask mUploadTask;
    private ImageView mImageView;

    private StorageReference mStorageRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);
        mButtonChooseImage = findViewById(R.id.button_choose_image);
        name = findViewById(R.id.nameProduct);
        price =  findViewById(R.id.Price);
        btnSignUp = findViewById(R.id.btnSignUp);
        mImageView =  findViewById(R.id.image_view);
        poTime =  findViewById(R.id.product_stock_edt);
        plusStock =  findViewById(R.id.plus_stock);
        minusStock =  findViewById(R.id.minus_stock);
        imageHolder =  findViewById(R.id.image_card);
        desc =  findViewById(R.id.desc);

        toolbar =  findViewById(R.id.toolbar);
        setTitle(toolbar, "Tambah Produk");

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

        mStorageRef = FirebaseStorage.getInstance().getReference("uploads");
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference table_user = database.getReference("Product");


        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgress();
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
                                            Product products = new Product( "PROD-"+ System.currentTimeMillis(), name.getText().toString(), url, price.getText().toString(), Integer.parseInt(poTime.getText().toString()), desc.getText().toString());
                                            table_user.child(products.getProductId()).setValue(products);
                                            disProgress();
                                            Toast.makeText(AddProduct.this, "Berhasil Menambahkan Produk", Toast.LENGTH_SHORT).show();

                                            AddProduct.this.finish();
                                        }
                                    });


                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure( Exception e) {
                                    Toast.makeText(AddProduct.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
//                                    mProgressBar.setProgress((int) progress);

                                }
                            });
                } else {
                    Toast.makeText(AddProduct.this, "No file selected", Toast.LENGTH_SHORT).show();
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
            imageHolder.setVisibility(View.VISIBLE);
            Picasso.with(this).load(mImageUri).into(mImageView);
        }
    }
    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }
}
