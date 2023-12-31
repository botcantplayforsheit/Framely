package com.example.finalproject.page;

import android.app.DatePickerDialog;
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

import com.example.finalproject.Model.User;
import com.example.finalproject.R;
import com.google.android.material.textfield.TextInputLayout;
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


public class EditProfile extends BaseActivity {
    Toolbar toolbar;
    CardView imageHolder;
    RelativeLayout mButtonChooseImage;
    ImageView mImageView;
    RadioButton male;
    RadioButton female;
    MaterialEditText etPhone;
    MaterialEditText etName;
    MaterialEditText etPassword;
    MaterialEditText etDate;
    MaterialEditText etAddress;
    RadioButton radio;
    RadioGroup groups;
    Button btnSignUp;
    TextInputLayout textInputLayout;

    private static final String TAG = "PhoneAuth";

    private Uri mImageUri;

    private static final int PICK_IMAGE_REQUEST = 1;
    FirebaseDatabase database;
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

    private void initView(){
        imageHolder =  findViewById(R.id.image_card);
        mButtonChooseImage =  findViewById(R.id.button_choose_image);
        male =  findViewById(R.id.radioButton);
        mImageView =  findViewById(R.id.image_view);
        female =  findViewById(R.id.radioButton2);
        etPhone =  findViewById(R.id.etPhone);
        etName =  findViewById(R.id.etName);
        etPassword =  findViewById(R.id.etPassword);
        etDate =  findViewById(R.id.etTanngal);
        etAddress =  findViewById(R.id.etAddress);
        groups =  findViewById(R.id.radioGroup);
        btnSignUp =  findViewById(R.id.btnSignUp);
        textInputLayout =  findViewById(R.id.inputLayout);
        toolbar =  findViewById(R.id.toolbar);

        mButtonChooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, PICK_IMAGE_REQUEST);
            }
        });

        setTitle(toolbar, "Ubah Akun");
        etPhone.setText(PreferenceUtil.getUser().getPhone());
        etName.setText(PreferenceUtil.getUser().getName());
        etDate.setText(PreferenceUtil.getUser().getTanggalLahir());
        etAddress.setText(PreferenceUtil.getUser().getAddress());
        etPhone.setEnabled(false);
        if(PreferenceUtil.getUser().getImage()!=null&&!PreferenceUtil.getUser().getImage().equals(" ")&&!PreferenceUtil.getUser().getImage().isEmpty()){
            imageHolder.setVisibility(View.VISIBLE);
            Picasso.with(getBaseContext()).load(PreferenceUtil.getUser().getImage()).into(mImageView);
        }
        btnSignUp.setText("Daftar Akun");
        if(PreferenceUtil.getUser().getGender().equals("Female")){
            male.setChecked(false);
            female.setChecked(true);
        }else{
            male.setChecked(true);
            female.setChecked(false);
        }
        etPassword.setVisibility(View.GONE);
        textInputLayout.setVisibility(View.GONE);
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
        btnSignUp.setText("Ubah");
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

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
                                            disProgress();
                                            createUser(taskSnapshot.getMetadata().getReference().getDownloadUrl().toString());
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
                                    Toast.makeText(EditProfile.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
                    createUser(" ");
                }


            }
        });

    }

    private void initEnv(){
        database = FirebaseDatabase.getInstance();
        table_user = database.getReference("User");
        mStorageRef = FirebaseStorage.getInstance().getReference("uploads");
        dateFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
    }

    private void createUser (String image){
        int select = groups.getCheckedRadioButtonId();
        radio= (RadioButton)findViewById(select);
        User user = new User(etName.getText().toString(), PreferenceUtil.getUser().getPassword(), PreferenceUtil.getUser().getStatus(),etPhone.getText().toString(),etAddress.getText().toString(),radio.getText().toString().equals("Perempuan")?"Female":"Male",
                etDate.getText().toString()," "," ", image," ","unVerified");
        table_user.child(PreferenceUtil.getUser().getPhone()).removeValue();
        table_user.child(user.getPhone()).setValue(user);
        PreferenceUtil.setUser(user);
        Toast.makeText(EditProfile.this, "Akun berhasil diubah!", Toast.LENGTH_SHORT).show();
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
