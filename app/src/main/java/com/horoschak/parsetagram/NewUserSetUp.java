package com.horoschak.parsetagram;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.horoschak.parsetagram.model.Post;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;

public class NewUserSetUp extends AppCompatActivity {

    @BindView(R.id.bPhotos) ImageButton bPhotos;
    @BindView(R.id.bCamera) ImageButton bCamera;
    @BindView(R.id.ivProfileImage) ImageView ivProfileImage;
    @BindView(R.id.etHandle) EditText etHandle;
    @BindView(R.id.etBio) EditText etBio;
    @BindView(R.id.bDone) Button bDone;
    private ParseUser user;

    // Request codes
    private final static int PICK_PHOTO_CODE = 1046;
    public final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034;
    // needed values
    private File photoFile;
    public String photoFileName = "photo.jpg";
    private int width;
    private ParseFile profileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user_set_up);
        ButterKnife.bind(this);
        user = ParseUser.getCurrentUser();
        getWidth();
        loadUserData();
    }

    // TODO resize images
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_PHOTO_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                Uri photoUri = data.getData();
                photoFile = new File(getRealPathFromURI(photoUri));
                // by this point we have the camera photo on disk
                // Write the bytes of the bitmap to file
                Bitmap selectedImage = null;
                try {
                    selectedImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri);
                    // Resize Image
                    Bitmap resizedBitmap = BitmapScaler.scaleToFill(selectedImage, width, 400);
                    // Configure byte output stream
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    // Compress the image further
                    resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 40, bytes);
                    // Create a new file for the resized bitmap (`getPhotoFileUri` defined above)
                    File resizedFile = getPhotoFileUri(photoFileName + "_resized");
                    resizedFile.createNewFile();
                    FileOutputStream fos = new FileOutputStream(resizedFile);
                    fos.write(bytes.toByteArray());
                    fos.close();
                    photoFile = resizedFile;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // Load the selected image into a preview
                ivProfileImage.setImageBitmap(selectedImage);
            }
        }
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // by this point we have the camera photo on disk
                Bitmap takenImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                // RESIZE BITMAP, see section below
                Bitmap resizedBitmap = BitmapScaler.scaleToFill(takenImage, width, 400);
                // Configure byte output stream
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                // Compress the image further
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 40, bytes);
                // Create a new file for the resized bitmap (`getPhotoFileUri` defined above)
                File resizedFile = getPhotoFileUri(photoFileName + "_resized");
                try {
                    resizedFile.createNewFile();
                    FileOutputStream fos = new FileOutputStream(resizedFile);
                    fos.write(bytes.toByteArray());
                    fos.close();
                    photoFile = resizedFile;
                } catch (IOException e) {
                    e.printStackTrace();
                    //photoFile = new File(getRealPathFromURI(photoUri));
                }
                // Load the taken image into a preview
                ivProfileImage.setImageBitmap(takenImage);
                Log.d("Image", photoFile.getAbsolutePath());
            } else { // Result was a failure
                Toast.makeText(this, "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadUserData() {
        etHandle.setText(user.getString("handle"));
        etBio.setText(user.getString("bio"));
        profileImage = user.getParseFile("profileImage");
        if (profileImage != null) {
            RequestOptions options = new RequestOptions();
            options.placeholder(R.drawable.profile_placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .error(R.drawable.profile_placeholder)
                    .transform(new CenterCrop())
                    .transform(new CircleCrop());
            Glide.with(this)
                    .load(profileImage.getUrl())
                    .apply(options) // Extra: round image corners
                    .into(ivProfileImage);
        }
    }

    private void getWidth(){
        // Get screen width
        Display display = ((Activity)this).getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;
    }

    @OnClick(R.id.bDone)
    public void OnClickDone() {
        ParseFile parseFile;
        if (photoFile == null || ivProfileImage.getDrawable() == null) {
            parseFile = null;
        } else {
            parseFile = new ParseFile(photoFile);
        }
        final String bio = etBio.getText().toString();
        final String handle = etHandle.getText().toString();
        // TODO have user select or take photo
        //final File file = new File("/storage/emulated/0/DCIM/Camera/IMG_20190708_153100.jpg")
        // save first
        if (parseFile != null) {
            parseFile.saveInBackground();
        }
        // TODO runtime persmissions for file
        requestPerms();
        updateUser(handle, bio, parseFile);
//            parseFile.saveInBackground(new SaveCallback() {
//                @Override
//                public void done(ParseException e) {
//                    if (e == null) {
//                        createPost(description, parseFile, user);
//                    } else {
//                        e.printStackTrace();
//                    }
//                }
//            });

    }

    private void updateUser(String handle, String bio, ParseFile imageFile) {
        final Post newPost = new Post();
        user.put("handle", handle);
        user.put("bio", bio);
        if (imageFile != null) {
            user.put("profileImage", imageFile);
        }
        user.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.d("Update", "post success");
                    Intent intent = new Intent(NewUserSetUp.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    e.printStackTrace();
                }
            }
        });
    }

    @Optional
    @OnClick(R.id.bPhotos)
    public void OnClickPhotos() {
        // Create intent for picking a photo from the gallery
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        // So as long as the result is not null, it's safe to use the intent.
        if (intent.resolveActivity(this.getPackageManager()) != null) {
            // Bring up gallery to select a photo
            startActivityForResult(intent, PICK_PHOTO_CODE);
        }
    }

    @OnClick(R.id.bCamera)
    public void OnClickCamera() {
        // create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Create a File reference to access to future access
        photoFile = getPhotoFileUri(photoFileName);

        // wrap File object into a content provider
        // required for API >= 24
        // See https://guides.codepath.com/android/Sharing-Content-with-Intents#sharing-files-with-api-24-or-higher
        Uri fileProvider = FileProvider.getUriForFile(this, "com.codepath.fileprovider", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        // So as long as the result is not null, it's safe to use the intent.
        if (intent.resolveActivity(this.getPackageManager()) != null) {
            // Start the image capture intent to take photo
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = this.getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    // Returns the File for a photo stored on disk given the fileName
    public File getPhotoFileUri(String fileName) {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        File mediaStorageDir = new File(this.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "ImageUpload");

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
            Log.d("ImageUpload", "failed to create directory");
        }

        // Return the file target for the photo based on filename
        File file = new File(mediaStorageDir.getPath() + File.separator + fileName);

        return file;
    }

    private void requestPerms() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // No explanation needed; request the permission
            ActivityCompat.requestPermissions((Activity) this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    0);

            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant. The callback method gets the
            // result of the request.
        } else {
            //testPost();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 0) {
            //testPost();
        }
    }
}
