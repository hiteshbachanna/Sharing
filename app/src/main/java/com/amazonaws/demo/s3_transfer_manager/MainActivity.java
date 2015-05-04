/*
 * Copyright 2010-2014 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.amazonaws.demo.s3_transfer_manager;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.amazonaws.demo.s3_transfer_manager.models.TransferModel;
import com.amazonaws.demo.s3_transfer_manager.network.TransferController;
import com.amazonaws.services.s3.AmazonS3Client;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;

/* 
 * Activity where the user can see the history of transfers, go to the downloads
 * page, or upload images/videos.
 *
 * The reason we separate image and videos is for compatibility with Android versions
 * that don't support multiple MIME types. We only allow videos and images because
 * they are nice for demonstration
 */
public class MainActivity extends Activity {
    private boolean exists = false;
    private boolean checked = false;
    private static final String TAG = "MainActivity";
    private static final int REFRESH_DELAY = 500;
    public static final int PHOTO_SELECT = 1;

    private Timer mTimer;
    private LinearLayout mLayout;
    private TransferModel[] mModels = new TransferModel[0];

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        setContentView(R.layout.activity_main);

        mLayout = (LinearLayout) findViewById(R.id.transfers_layout);
        new CheckBucketExists().execute();
        findViewById(R.id.create_bucket).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                new CreateBucket().execute();
            }
        });
        findViewById(R.id.delete_bucket).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                new DeleteBucket().execute();
            }
        });
        findViewById(R.id.download).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checked) {
                    Toast.makeText(getApplicationContext(), "Please wait a moment...",
                            Toast.LENGTH_SHORT).show();
                } else if (!exists) {
                    Toast.makeText(getApplicationContext(), "You must first create the bucket",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(
                            MainActivity.this, DownloadActivity.class);
                    startActivity(intent);
                }
            }
        });

        findViewById(R.id.upload_image).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checked) {
                    Toast.makeText(getApplicationContext(), "Please wait a moment...",
                            Toast.LENGTH_SHORT).show();
                } else if (!exists) {
                    Toast.makeText(getApplicationContext(), "You must first create the bucket",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    startActivityForResult(intent, 0);
                }
            }
        });

        findViewById(R.id.upload_video).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checked) {
                    Toast.makeText(getApplicationContext(), "Please wait a moment...",
                            Toast.LENGTH_SHORT).show();
                } else if (!exists) {
                    Toast.makeText(getApplicationContext(), "You must first create the bucket",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("video/*");
                    startActivityForResult(intent, 0);
                }
            }
        });

        final ArrayList<Bitmap> bitmaps = new ArrayList<>();

        findViewById(R.id.camera).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                ArrayList<Uri> filePaths = getAllShownImagesPath();

                //ArrayList<String> filePaths = getAllShownImagesPath();

//
//
                for(Uri uri:filePaths){
                    TransferController.upload(getApplicationContext(), uri);
                }

//                Intent shareIntent = new Intent();
//                shareIntent.setAction(Intent.ACTION_CHOOSER);
//                shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, filePaths);
//                shareIntent.setType("image/*");
//                shareIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
//                //startActivity(Intent.createChooser(shareIntent, "Share images to.."));
//                startActivityForResult(Intent.createChooser(shareIntent, "Select Picture"), PHOTO_SELECT);


//                try{
//                    for(String filePath:filePaths){
//
//                        bitmaps.add(ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(filePath), 96, 96));
//
////                        ExifInterface exif = new ExifInterface(filePath);
////                        byte[] imageData=exif.getThumbnail();
////                        if (imageData!=null)
////                            bitmaps.add(BitmapFactory.decodeByteArray(imageData, 0, imageData.length));
//
//                        //bitmaps.add(decodeFile(filePath));
//                    }
//                }catch (Exception e){
//                    e.printStackTrace();
//                }



//                Intent intent = new Intent(MainActivity.this, DisplayImagesActivity.class);
//                intent.putExtra("BitmapList",bitmaps);
//                startActivity(intent);

                //opens gallery with all images
//                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//                intent.setType("image/*");
//                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
//                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PHOTO_SELECT);
            }
        });





       // SimpleAdapter adapter = new SimpleAdapter(getBaseContext(), filePaths, R.layout.display_images, from, to);
        // make timer that will refresh all the transfer views
        mTimer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        syncModels();
                        for (int i = 0; i < mLayout.getChildCount(); i++) {
                            ((TransferView) mLayout.getChildAt(i)).refresh();
                        }
                    }
                });
            }
        };
        mTimer.schedule(task, 0, REFRESH_DELAY);
    }

    public Bitmap decodeFile(String filePath)
    {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, o);

        // The new size we want to scale to
        final int REQUIRED_SIZE = 100;
        final int H = 50;

        // Find the correct scale value. It should be the power of 2.
        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 3;
        while (true) {
            if (width_tmp < REQUIRED_SIZE && height_tmp < H)
                break;
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }
        // Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        System.out.println("decode file ........... "+filePath);
        return BitmapFactory.decodeFile(filePath, o2);
    }
    /*
     * When we get a Uri back from the gallery, upload the associated
     * image/video
     */
    @Override
    protected void onActivityResult(int reqCode, int resCode, Intent data) {
        //ArrayList<String> filePaths = getFilePaths();


        if (resCode == Activity.RESULT_OK && data != null && reqCode == 0) {
            Uri uri = data.getData();
            if (uri != null) {
                TransferController.upload(this, uri);
            }
        }



        //code to send images to amazon s3
        if (reqCode == PHOTO_SELECT && resCode == RESULT_OK && data != null) {

            //if multiple images are selected
            if (data.getClipData() != null) {
                ClipData clipData = data.getClipData();
                for (int i = 0; i < clipData.getItemCount(); i++) {
                    Uri uri = clipData.getItemAt(i).getUri();
                    if (uri != null) {
                        TransferController.upload(this, uri);
                    }

                }
                sendNotification();
            } else {
                Uri uri = data.getData();
                if (uri != null) {
                    TransferController.upload(this, uri);
                }
            }

        }
    }

    private void sendNotification() {
        Context context = getApplicationContext();
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                new Intent(context, MainActivity.class), 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.down)
                        .setContentTitle("My notification")
                        .setContentText("UPhoto upload complete");
        mBuilder.setContentIntent(contentIntent);
        mBuilder.setDefaults(Notification.DEFAULT_SOUND);
        mBuilder.setAutoCancel(true);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, mBuilder.build());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTimer.cancel();
    }

    @Override
    protected void onResume() {
        super.onResume();
        syncModels();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mTimer.purge();
    }

    /* makes sure that we are up to date on the transfers */
    private void syncModels() {
        TransferModel[] models = TransferModel.getAllTransfers();
        if (mModels.length != models.length) {
            // add the transfers we haven't seen yet
            for (int i = mModels.length; i < models.length; i++) {
                mLayout.addView(new TransferView(this, models[i]), 0);
            }
            mModels = models;
        }
    }

    private class CreateBucket extends AsyncTask<Object, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Object... params) {
            AmazonS3Client sS3Client = Util.getS3Client(getApplicationContext());
            if (!Util.doesBucketExist()) {
                Util.createBucket();
                return true;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (!result) {
                Toast.makeText(getApplicationContext(), "Bucket already exists", Toast.LENGTH_SHORT)
                        .show();
            } else {
                Toast.makeText(getApplicationContext(), "Bucket successfully created!",
                        Toast.LENGTH_SHORT).show();
            }
            exists = true;
        }
    }

    private class CheckBucketExists extends AsyncTask<Object, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Object... params) {
            AmazonS3Client sS3Client = Util.getS3Client(getApplicationContext());
            return Util.doesBucketExist();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            checked = true;
            exists = result;
        }
    }

    private class DeleteBucket extends AsyncTask<Object, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Object... params) {
            AmazonS3Client sS3Client = Util.getS3Client(getApplicationContext());
            if (Util.doesBucketExist()) {
                Util.deleteBucket();
                return true;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (!result) {
                Toast.makeText(getApplicationContext(), "Bucket does not exist", Toast.LENGTH_SHORT)
                        .show();
            } else {
                Toast.makeText(getApplicationContext(), "Bucket successfully deleted!",
                        Toast.LENGTH_SHORT).show();
            }
            exists = false;
        }
    }

    public ArrayList<String> getFilePaths()
    {


        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Images.ImageColumns.DATA, MediaStore.MediaColumns.DATE_ADDED};
        String selection = "date_added=1429730728";
        Date date = new Date(1429730728000l);

        Cursor c = null;
        SortedSet<String> dirList = new TreeSet<String>();
        ArrayList<String> resultIAV = new ArrayList<String>();
        ArrayList<Integer> dateList = new ArrayList<>();

        String[] directories = null;
        if (uri != null)
        {
            c = managedQuery(uri, projection, MediaStore.MediaColumns.DATE_ADDED + "=?", new String[]{"" + 1429730728}, MediaStore.MediaColumns.DATE_ADDED + " DESC");
        }

        if ((c != null) && (c.moveToFirst()))
        {
            do
            {
                String tempDir = c.getString(0);
                Integer dateDate = c.getInt(1);
                tempDir = tempDir.substring(0, tempDir.lastIndexOf("/"));
                try{

                    dirList.add(tempDir);
                    dateList.add(dateDate);
                }
                catch(Exception e)
                {

                }
            }
            while (c.moveToNext());
            directories = new String[dirList.size()];
            dirList.toArray(directories);

        }

        for(int i=0;i<dirList.size();i++)
        {
            File imageDir = new File(directories[i]);
            File[] imageList = imageDir.listFiles();
            if(imageList == null)
                continue;
            for (File imagePath : imageList) {
                try {

                    if(imagePath.isDirectory())
                    {
                        imageList = imagePath.listFiles();

                    }
                    if ( imagePath.getName().contains(".jpg")|| imagePath.getName().contains(".JPG")
                            || imagePath.getName().contains(".jpeg")|| imagePath.getName().contains(".JPEG")
                            || imagePath.getName().contains(".png") || imagePath.getName().contains(".PNG")
                            || imagePath.getName().contains(".gif") || imagePath.getName().contains(".GIF")
                            || imagePath.getName().contains(".bmp") || imagePath.getName().contains(".BMP")
                            )
                    {



                        String path= imagePath.getAbsolutePath();
                        resultIAV.add(path);

                    }
                }
                //  }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return resultIAV;


    }


    public  ArrayList<Uri> getAllShownImagesPath() {
        Uri uri;
        Cursor cursor;
        int column_index;
        StringTokenizer st1;
        ArrayList<String> listOfAllImages = new ArrayList<String>();
        ArrayList<Integer> listOfImageId = new ArrayList<>();
        ArrayList<Uri> uris = new ArrayList<>();
        String absolutePathOfImage = null;
        uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = { MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.DATE_ADDED, MediaStore.MediaColumns._ID };

//        cursor = getContentResolver().query(uri, projection, null,
//                null, null);

// 1430370000 - 1430416799
        cursor = getContentResolver().query(uri, projection, MediaStore.MediaColumns.DATE_ADDED + ">? and "+ MediaStore.MediaColumns.DATE_ADDED + "<?", new String[]{"" + 1430370000, "1430416799"}, MediaStore.MediaColumns.DATE_ADDED + " DESC");
        //cursor = getContentResolver().query(uri, projection, null, null,null);

        // column_index = cursor.getColumnIndexOrThrow(MediaColumns.DATA);
        column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        while (cursor.moveToNext()) {
            absolutePathOfImage = cursor.getString(column_index);
            cursor.getInt(column_index+1);
            listOfAllImages.add(absolutePathOfImage);
            listOfImageId.add(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));

            Uri.fromFile(new File(absolutePathOfImage));
            uris.add(Uri.fromFile(new File(absolutePathOfImage)));
        }
        return uris;
    }
}
