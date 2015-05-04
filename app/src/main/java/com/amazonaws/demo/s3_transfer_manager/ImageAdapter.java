package com.amazonaws.demo.s3_transfer_manager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.ArrayList;

/**
 * Created by hg032812 on 4/29/15.
 */
//public class ImageAdapter extends BaseAdapter {
//    private Context mContext;
//
//    public ImageAdapter(Context c) {
//        mContext = c;
//    }
//
//    public int getCount() {
//        return mThumbIds.length;
//    }
//
//    public Object getItem(int position) {
//        return null;
//    }
//
//    public long getItemId(int position) {
//        return 0;
//    }
//
//    // create a new ImageView for each item referenced by the Adapter
//    public View getView(int position, View convertView, ViewGroup parent) {
//        ImageView imageView;
//        if (convertView == null) {
//            // if it's not recycled, initialize some attributes
//            imageView = new ImageView(mContext);
//            imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
//            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
//            imageView.setPadding(8, 8, 8, 8);
//        } else {
//            imageView = (ImageView) convertView;
//        }
//
//        imageView.setImageResource(mThumbIds[position]);
//        return imageView;
//    }
//
//    // references to our images
//    private Integer[] mThumbIds = {
//            R.drawable.down, R.drawable.pause,
//            R.drawable.play, R.drawable.up
//    };
//}

public class ImageAdapter extends BaseAdapter
{
    private Context mContext;
    ArrayList<Bitmap> bitmapList;

    public ImageAdapter(Context c, ArrayList<Bitmap> bitmaps)
    {
        mContext = c;
        bitmapList = bitmaps;
    }

    public int getCount()
    {
        return bitmapList.size();
    }
    public Object getItem(int position)
    {
        return position;
    }
    public long getItemId(int position)
    {
        return position;
    }

    public View getView(int position,View convertView,ViewGroup parent)
    {
        System.gc();
        ImageView i = null ;

        if (convertView == null )
        {
            i = new ImageView(mContext);
            i.setLayoutParams(new GridView.LayoutParams(92,92));
            i.setScaleType(ImageView.ScaleType.CENTER_CROP);
            i.setImageBitmap(bitmapList.get(position));
        }
        else
            i = (ImageView) convertView;
        return i;
    }
}
