package com.example.shoppy;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    Button select_btn,upload_btn;
    TextView show_txt;
    EditText edt_name,edt_price,edt_Real_price,edt_offer;
    Uri imageUri;
    ImageView imageView;
    StorageReference mStorageRef;
    DatabaseReference mDatabaseRef;
    StorageTask mUploadTask;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //id
        select_btn=findViewById(R.id.choose_btn);
        upload_btn=findViewById(R.id.upload_btn);
        show_txt=findViewById(R.id.show_txt);
        edt_name=findViewById(R.id.edt_name);
        edt_price=findViewById(R.id.edt_price);
        edt_offer=findViewById(R.id.edt_offer);
        edt_Real_price=findViewById(R.id.edt_Real_price);
        imageView=findViewById(R.id.img);

        //define fire base
        mStorageRef= FirebaseStorage.getInstance().getReference("uploads");
        mDatabaseRef= FirebaseDatabase.getInstance().getReference("uploads");

        //choose file
        select_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SelectImage();
            }
        });

        //upload file
        upload_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*if(mUploadTask !=null && !mUploadTask.isInProgress()){
                    Toast.makeText(MainActivity.this, "upload in progress", Toast.LENGTH_SHORT).show();
                }

                else{*/
                    uploadImage();
                //}
            }
        });

    }




    //to get image extension
    private String getFileExtension(Uri uri){
        ContentResolver contentResolver=getContentResolver();
        MimeTypeMap mimeTypeMap=MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage() {
        final ProgressDialog pd=new ProgressDialog(this);
        pd.setMessage("uploading...");
        pd.show();
        if(imageUri!=null){
            final StorageReference fileReference=mStorageRef.child(System.currentTimeMillis()+"."+getFileExtension(imageUri));
            mUploadTask=fileReference.putFile(imageUri)
                    .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    pd.dismiss();
                                    Toast.makeText(MainActivity.this, "Upload successful", Toast.LENGTH_SHORT).show();
                                    Upload upload=new Upload(edt_Real_price.getText().toString().trim(),uri.toString(),edt_name.getText().toString().trim(),edt_offer.getText().toString().trim() ,edt_price.getText().toString().trim() );
                                    String uploadId=mDatabaseRef.push().getKey();
                                    mDatabaseRef.child(uploadId).setValue(upload);
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    // Error, Image not uploaded
                    pd.dismiss();
                    Toast.makeText(MainActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {

                // Progress Listener for loading
                // percentage on the dialog box
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot)
                {
                    double progress
                            = (100.0
                            * taskSnapshot.getBytesTransferred()
                            / taskSnapshot.getTotalByteCount());
                    pd.setMessage("Uploaded " + (int)progress + "%");
                }
            });


        }
        else {
            Toast.makeText(this, "No file Selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void SelectImage() {

        // Defining Implicit Intent to mobile gallery
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 2);
    }


    // Override onActivityResult method
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        // checking request code and result code
        // if request code is PICK_IMAGE_REQUEST and
        // resultCode is RESULT_OK
        // then set image in the image view
        if (requestCode == 2 && resultCode == RESULT_OK && data != null && data.getData() != null) {

            // Get the Uri of data
            imageUri = data.getData();
            Picasso.with(this).load(imageUri).into(imageView);
        }
    }

}