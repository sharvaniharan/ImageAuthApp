package com.example.sharvani.imageauthapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;


public class MyActivity extends Activity {

    public static final String DEBUGTAG = "SHAR@@@@@@@@@@";
    public static final String TEXTFILE = "notesquirrel.txt";
    public static final String FILESAVED = "FileSaved";
    private Uri image;
    private static final int PHOTO_TAKEN_REQUEST = 0;
    private static final int BROWSE_GALLERY_REQUEST = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the XML file that will determine how this
        // activity looks
        setContentView(R.layout.activity_main);

        // Add listeners to the save and lock buttons
        addSaveButtonListener();
        addLockButtonListener();

        // Get any saved preferences
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);

        // If the user has saved a file already, try to load it.
        boolean fileSaved = prefs.getBoolean(FILESAVED, false);

        if (fileSaved) {
            loadSavedFile();
        }
    }

    /*
     * This method invokes the Image activity, telling it to get the user to
     * enter new passpoints. If an image uri is passed, the Image activity
     * should also reset the image to the given image.
     */
    private void resetPasspoints(Uri image) {
        Intent i = new Intent(this, ImageActivity.class);
        i.putExtra(ImageActivity.RESET_PASSPOINTS, true);

        if (image != null) {
            i.putExtra(ImageActivity.RESET_IMAGE, image.getPath());
        }

        startActivity(i);
    }

    @Override
	/*
	 * This method is invoked when menu items are selected.
	 */
    public boolean onMenuItemSelected(int featureId, MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_passpoints_reset:
                resetPasspoints(null);
                return true;
            case R.id.menu_replace_image:
                replaceImage();
                return true;
            default:
                return super.onMenuItemSelected(featureId, item);
        }
    }

    /*
     * This code is invoked when the user selects the menu item to replace the
     * current image.
     */
    private void replaceImage() {

        // Offer a choice of methods to replace the image in a dialog;
        // the user can either take a photo or browse the gallery.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View v = getLayoutInflater().inflate(R.layout.replace_image, null);
        builder.setTitle(R.string.replace_lock_image);
        builder.setView(v);

        final AlertDialog dlg = builder.create();
        dlg.show();

        Button takePhoto = (Button) dlg.findViewById(R.id.take_photo);
        Button browseGallery = (Button) dlg.findViewById(R.id.browse_gallery);

        takePhoto.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Invoke the camera activity.
                takePhoto();
            }
        });

        browseGallery.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Browse the gallery.
                browseGallery();
            }
        });
    }

    /*
     * This method invokes the browse gallery activity
     */
    private void browseGallery() {
        Intent i = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, BROWSE_GALLERY_REQUEST);
    }

    /*
     * This method invokes the camera activity, to take a photo
     */
    private void takePhoto() {
        // Figure out where to put the photo when it's taken.
        File picturesDirectory = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File imageFile = new File(picturesDirectory, "passpoints_image");

        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Tell the activity to save the photo to the given file.
        // (it will also be added to the gallery)
        image = Uri.fromFile(imageFile);
        i.putExtra(MediaStore.EXTRA_OUTPUT, image);
        startActivityForResult(i, PHOTO_TAKEN_REQUEST);
    }

    @Override
	/*
	 * This method is invoked upon returning from an activity invoked by
	 * startActivityForResult. We check the requestCode to see which activity we
	 * returned from.
	 */
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {

        if (requestCode == BROWSE_GALLERY_REQUEST) {
            String[] columns = { MediaStore.Images.Media.DATA };

            Uri imageUri = intent.getData();

            Cursor cursor = getContentResolver().query(imageUri, columns, null,
                    null, null);

            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(columns[0]);
            String imagePath = cursor.getString(columnIndex);

            cursor.close();

            image = Uri.parse(imagePath);
        }

        // Display an error message and return if we don't have an
        // image URI
        if (image == null) {
            Toast.makeText(this, R.string.unable_to_display_image,
                    Toast.LENGTH_LONG).show();
            return;
        }

        Log.d(DEBUGTAG, "Photo: " + image.getPath());

        // Now we can start the image activity and tell it to use
        // this new image.
        resetPasspoints(image);
    }

    /*
     * If the user clicks the "lock" menu item, go back to the image screen.
     */
    private void addLockButtonListener() {
        Button lockBtn = (Button) findViewById(R.id.lock);

        lockBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(DEBUGTAG, "start image activity");
                startActivity(new Intent(MyActivity.this, ImageActivity.class));
            }
        });
    }

    /*
     * Load a saved text file from the disk.
     */
    private void loadSavedFile() {
        try {

            FileInputStream fis = openFileInput(TEXTFILE);

            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new DataInputStream(fis)));

            EditText editText = (EditText) findViewById(R.id.text);

            String line;

            while ((line = reader.readLine()) != null) {
                editText.append(line);
                editText.append("\n");
            }

            fis.close();
        } catch (Exception e) {
            Toast.makeText(MyActivity.this,
                    getString(R.string.toast_cant_load), Toast.LENGTH_LONG)
                    .show();
        }
    }

    /*
     * Save the contents of the EditText control as a text file.
     */
    private void addSaveButtonListener() {
        Button saveBtn = (Button) findViewById(R.id.save);

        saveBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                saveText();
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();

        saveText();
    }

    private void saveText() {
        EditText editText = (EditText) findViewById(R.id.text);

        String text = editText.getText().toString();

        try {
            FileOutputStream fos = openFileOutput(TEXTFILE,
                    Context.MODE_PRIVATE);
            fos.write(text.getBytes());
            fos.close();

            // Save a "file saved" preference so that next time
            // the application runs, we'll know that this
            // file has been saved and we have to load it.
            SharedPreferences prefs = getPreferences(MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(FILESAVED, true);
            editor.commit();
        } catch (Exception e) {
            Toast.makeText(MyActivity.this,
                    getString(R.string.toast_cant_save), Toast.LENGTH_LONG)
                    .show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // TODO Auto-generated method stub
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
}
