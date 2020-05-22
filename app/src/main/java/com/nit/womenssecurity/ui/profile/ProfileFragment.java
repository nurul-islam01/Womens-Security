package com.nit.womenssecurity.ui.profile;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.SyncStateContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nit.womenssecurity.R;
import com.nit.womenssecurity.pojos.Contact;
import com.nit.womenssecurity.pojos.User;
import com.nit.womenssecurity.ui.adapter.ContactAdapter;
import com.nit.womenssecurity.utils.WSFirebase;
import com.nit.womenssecurity.utils.WSPreference;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.pedant.SweetAlert.SweetAlertDialog;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;


public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";
    public static final int ACTION_REQUEST_GALLERY = 254;
    private Context context;

    private TextView addContactTV, nameTV, emailTV, phoneTV;
    private SweetAlertDialog alertDialog;
    private WSPreference preference;
    private User mainUser;
    private ContactAdapter adapter;
    private RecyclerView contactRC;
    private TextView noContactTV;
    private CircleImageView imageCIV;
    private Activity activity;
    private Uri imageUri;


    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);
        alertDialog = new SweetAlertDialog(context);
        preference = new WSPreference(context);
        mainUser = preference.getUser();

        addContactTV = root.findViewById(R.id.addContactTV);
        contactRC = root.findViewById(R.id.contactRC);
        noContactTV = root.findViewById(R.id.noContactTV);
        imageCIV = root.findViewById(R.id.imageCIV);
        phoneTV = root.findViewById(R.id.phoneTV);
        nameTV = root.findViewById(R.id.nameTV);
        emailTV = root.findViewById(R.id.emailTV);
        noContactTV.setVisibility(View.GONE);

        imageCIV.setDrawingCacheEnabled(true);
        imageCIV.buildDrawingCache();

        if (mainUser.getPhoto() != null) {
            Picasso.get().load(mainUser.getPhoto()).placeholder(R.drawable.avater).error(R.drawable.avater)
                    .into(imageCIV);
        }

        if (mainUser != null) {
            nameTV.setText(mainUser.getFullName());
            emailTV.setText("Email : " + mainUser.getFullName());
            phoneTV.setText("Phone : " + mainUser.getPhone());
        }

        addContactTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = getLayoutInflater().inflate(R.layout.add_contact_layout, null);
                EditText nameET = view.findViewById(R.id.nameET);
                EditText numberEt = view.findViewById(R.id.numberET);

                alertDialog.changeAlertType(SweetAlertDialog.NORMAL_TYPE);
                alertDialog.setCustomView(view);
                alertDialog.setTitle("Add Family Contact");
                alertDialog.setConfirmButton("Save", new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {

                        String name = nameET.getText().toString().trim();
                        String number = numberEt.getText().toString().trim();
                        String pushKey = WSFirebase.contacts(mainUser.getId()).push().getKey();
                        Contact contact = new Contact(pushKey, name, number);
                        WSFirebase.contacts(mainUser.getId()).child(pushKey).setValue(contact)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }).addOnCanceledListener(new OnCanceledListener() {
                            @Override
                            public void onCanceled() {
                                Toast.makeText(context, "Error saving", Toast.LENGTH_SHORT).show();
                            }
                        });
                        alertDialog.dismiss();
                    }
                }).setCancelButton("Cancel", new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        Toast.makeText(context, "Cancel", Toast.LENGTH_SHORT).show();
                        alertDialog.dismiss();
                    }
                }).show();
            }
        });

        familyContacts();

        imageCIV.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (isStoragePermissionGranted()) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    Intent chooser = Intent.createChooser(intent, "Choose a Picture");
                    startActivityForResult(chooser, ACTION_REQUEST_GALLERY);
                }
                return false;
            }
        });

        imageCIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Long click for change image", Toast.LENGTH_SHORT).show();
            }
        });

        return root;
    }

    private void uploadImage(String url) {
        alertDialog.changeAlertType(SweetAlertDialog.PROGRESS_TYPE);
        alertDialog.setTitle("Loading...");
        alertDialog.show();
        File file = new File(url);
        Uri uri = Uri.fromFile(file);
        Bitmap bmp = null;
        try {
            bmp = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
        } catch (IOException e) {
            Log.d(TAG,"*****     "+e.toString());
            e.printStackTrace();
        }

        Log.d(TAG, "uploadImage: " + url);
        Log.d(TAG, "uploadImage: " + uri.getPath());
        Log.d(TAG, "uploadImage: " + imageUri);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 40, baos);
        byte[] data = baos.toByteArray();

        String extension = getExt(url);

        StorageReference storage = WSFirebase.photoSaveOnStorage().child(mainUser.getId() + "." + extension);
        UploadTask uploadTask = storage.putBytes(data);

        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    Log.d(TAG, "then: " + task.getException());
                    throw task.getException();
                }
                return storage.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    try {
                        String downloadUri = task.getResult().toString();
                        WSFirebase.user().child(mainUser.getId()).child("photo").setValue(downloadUri)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Toast.makeText(context, "Uploaded", Toast.LENGTH_SHORT).show();
                                        mainUser.setPhoto(downloadUri);
                                        preference.setUser(mainUser);
                                        alertDialog.dismiss();
                                    }
                                });
                    }catch (Exception e) {
                        Log.d(TAG, "onComplete: " + e.getMessage());
                    }



                } else {
                    alertDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                    alertDialog.setTitle("Upload failed");
                    alertDialog.setConfirmButton("Ok", new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismiss();
                        }
                    });
                    Log.d(TAG, "onComplete: " + task.getException());
                }

                Log.d(TAG, "onComplete: " + task.getResult());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                alertDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                alertDialog.setTitle("Upload failed");
                alertDialog.setConfirmButton("Ok", new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismiss();
                    }
                });
                Log.d(TAG, "onComplete: " + e.getMessage());
            }
        }).addOnCanceledListener(new OnCanceledListener() {
            @Override
            public void onCanceled() {
                Log.d(TAG, "onCanceled: uploaded canceled");
                Toast.makeText(context, "upload canceled", Toast.LENGTH_SHORT).show();
            }
        });

        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                        .getTotalByteCount());
                alertDialog.setTitle("Uploaded "+(int)progress+"%");
            }
        });

        Log.d(TAG, "uploadImage: " + urlTask.getResult().getPath());

    }

    public String getExt(String filePath){
        int strLength = filePath.lastIndexOf(".");
        if(strLength > 0)
            return filePath.substring(strLength + 1).toLowerCase();
        return null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK)    {
            switch (requestCode) {
                case ACTION_REQUEST_GALLERY:
                    Uri galleryImageUri = data.getData();
                    Log.d(TAG, "onActivityResult: " + galleryImageUri.getPath());
                    this.imageUri = galleryImageUri;
                    try{
                        Picasso.get().load(galleryImageUri).placeholder(R.drawable.avater).error(R.drawable.avater)
                                .into(imageCIV);
                        Log.d(TAG, "onActivityResult: " + getRealPath(galleryImageUri));
                        String url = getRealPath(galleryImageUri);
                        uploadImage(url);
                    } catch (Exception ex){
                        Log.d(TAG, "onActivityResult: " + ex.getMessage());
                    }
                    break;

            }
        }
    }

    public String getRealPath(Uri uri) {
        String filePath = "";

        Pattern p = Pattern.compile("(\\d+)$");
        Matcher m = p.matcher(uri.toString());
        if (!m.find()) {
            Log.e(TAG, "ID for requested image not found: " + uri.toString());
            return filePath;
        }
        String imgId = m.group();

        String[] column = { MediaStore.Images.Media.DATA };
        String sel = MediaStore.Images.Media._ID + "=?";

        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                column, sel, new String[]{ imgId }, null);

        int columnIndex = cursor.getColumnIndex(column[0]);

        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex);
        }
        cursor.close();

        return filePath;
    }


    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
    }

    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (context.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            return true;
        }
    }

    private void familyContacts() {
        WSFirebase.contacts(mainUser.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    List<Contact> contacts = new ArrayList<>();
                    for (DataSnapshot a: dataSnapshot.getChildren()) {
                        Contact contact = a.getValue(Contact.class);
                        contacts.add(contact);
                    }
                    if (contacts.size() > 0) {
                        preference.saveContacts(contacts);
                        adapter = new ContactAdapter(context, contacts);
                        contactRC.setAdapter(adapter);
                        noContactTV.setVisibility(View.GONE);
                    } else {
                        noContactTV.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: " + databaseError.getMessage());
            }
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }
}
