package com.example.finalproject.page;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.example.finalproject.Common.Common;
import com.example.finalproject.Model.Product;
import com.example.finalproject.Model.User;
import com.example.finalproject.R;
import com.example.finalproject.base.BaseActivity;
import com.example.finalproject.util.PreferenceUtil;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


public class SignUp extends BaseActivity {
    Toolbar toolbar;
    CardView imageHolder;
    RelativeLayout mButtonChooseImage;
    ImageView mImageView;
    MaterialEditText etPhone;
    MaterialEditText etName;
    MaterialEditText etPassword;
    MaterialEditText etDate;
    MaterialEditText etAddress;
    RadioButton radio;
    RadioGroup groups;
    Button btnSignUp;
    private Uri mImageUri;

    private static final int PICK_IMAGE_REQUEST = 1;

    private StorageTask mUploadTask;
    private StorageReference mStorageRef;
    private DatePickerDialog datePickerDialog;
    private SimpleDateFormat dateFormatter;
    DatabaseReference table_user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        initEnv();
        initView();
    }

    private void initEnv(){
        dateFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        table_user = database.getReference("User");
        mStorageRef = FirebaseStorage.getInstance().getReference("uploads");
    }

    private void initView(){
        etPhone = findViewById(R.id.etPhone);
        toolbar = findViewById(R.id.toolbar);
        imageHolder = findViewById(R.id.image_card);
        mButtonChooseImage = findViewById(R.id.button_choose_image);
        mImageView = findViewById(R.id.image_view);
        etName = findViewById(R.id.etName);
        etPassword = findViewById(R.id.etPassword);
        etDate = findViewById(R.id.etTanngal);
        etAddress = findViewById(R.id.etAddress);
        radio = findViewById(R.id.radioButton);
        groups = findViewById(R.id.radioGroup);
        btnSignUp = findViewById(R.id.btnSignUp);

        setTitle(toolbar, "Buat Akun");
        mButtonChooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, PICK_IMAGE_REQUEST);
            }
        });
        etDate.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                showDateDialog();
            }
        });
        etDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateDialog();
            }
        });
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if(etName.getText().toString().isEmpty()||etAddress.getText().toString().isEmpty()||etDate.getText().toString().isEmpty()||etPassword.getText().toString().isEmpty()||etPhone.getText().toString().isEmpty()){
                    Toast.makeText(SignUp.this, "Mohon isi semua data untuk melakukan registrasi akun", Toast.LENGTH_SHORT).show();
                    return;
                }
                showProgress();


                if (mImageUri != null) {
                    StorageReference fileReference = mStorageRef.child(System.currentTimeMillis()
                            + "." + getFileExtension(mImageUri));

                    mUploadTask = fileReference.putFile(mImageUri)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
//
                                    table_user.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if(dataSnapshot.child(etPhone.getText().toString()).exists()){
                                                disProgress();

                                                Toast.makeText(SignUp.this, "Nomor telepon sudah terdaftar", Toast.LENGTH_SHORT).show();
                                            }else {
                                                disProgress();
                                                createUser(taskSnapshot.getMetadata().getReference().getDownloadUrl().toString());
                                            }
                                            table_user.removeEventListener(this);
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });

                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(Exception e) {
                                    Toast.makeText(SignUp.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
                    table_user.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.child(etPhone.getText().toString()).exists()){
                                disProgress();

                                Toast.makeText(SignUp.this, "Nomor telepon sudah terdaftar", Toast.LENGTH_SHORT).show();
                            }else {
                                disProgress();
                                createUser(" ");
                            }
                            table_user.removeEventListener(this);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }


            }
        });
    }

    private void createUser (String image){
        int select = groups.getCheckedRadioButtonId();
        radio= (RadioButton)findViewById(select);
        User user = new User(etName.getText().toString(), etPassword.getText().toString(), "Costumer",etPhone.getText().toString(),etAddress.getText().toString(), radio.getText().toString().equals("Perempuan")?"Female":"Male",
                etDate.getText().toString()," "," ", image," ","unVerified");
        user.setRole(Common.ROLE_USER);
        table_user.child(user.getPhone()).setValue(user);
        PreferenceUtil.setUser(user);
        Toast.makeText(SignUp.this, "Berhasil Membuat Akun", Toast.LENGTH_SHORT).show();

        finish();
    }

    private void showDateDialog(){
        Calendar newCalendar = Calendar.getInstance();

        datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);

                etDate.setText(dateFormatter.format(newDate.getTime()));
            }

        },newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.show();
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
