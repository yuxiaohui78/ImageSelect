package com.aries.image;

import android.camerautil.lib.ImageSelectActivity;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ImageSelectActivity implements View.OnClickListener{
    Button btnOpen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setCacheFolder (".myPicture");
        setPictureWidth(400);
        btnOpen = (Button) findViewById(R.id.btnOpen);
        btnOpen.setOnClickListener(this);

        registerForContextMenu(btnOpen);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btnOpen){
            btnOpen.showContextMenu();
        }
    }

    @Override
    public void outputImage(Bitmap bmp, String imagePath) {
        ImageView tvImage = (ImageView)findViewById(R.id.ivImage);
        tvImage.setImageBitmap(bmp);
        TextView tvImagePath = (TextView)findViewById(R.id.tvImagePath);
        tvImagePath.setText("Image path=" + imagePath);
    }
}
