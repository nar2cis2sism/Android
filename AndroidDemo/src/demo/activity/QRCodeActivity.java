package demo.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import demo.android.R;
import demo.android.util.QRCodeUtil;

public class QRCodeActivity extends Activity implements OnClickListener {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qrcode);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.image_analysis:
            startActivity(new Intent(this, QRCodeImageActivity.class));
            break;
        case R.id.scanning_analysis:
            IntentIntegrator integrator = new IntentIntegrator(this);
//            integrator.addExtra("SCAN_WIDTH", 800);
//            integrator.addExtra("SCAN_HEIGHT", 200);
//            integrator.addExtra("RESULT_DISPLAY_DURATION_MS", 3000L);
//            integrator.addExtra("PROMPT_MESSAGE", "Custom prompt to scan a product");
            integrator.initiateScan();
            break;
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null)
        {
            String contents = result.getContents();
            if (TextUtils.isEmpty(contents))
            {
                openMessageDialog("No barcode found", "The user gave up and pressed Back");
            }
            else
            {
                openMessageDialog("Found barcode", result.toString());
            }
        }
    }
    
    public final void openMessageDialog(String title, String message)
    {
        new AlertDialog.Builder(this)
        .setTitle(title)
        .setMessage(message)
        .show();
    }
    
    public static class QRCodeImageActivity extends Activity implements OnClickListener {

        private static final int QR_WIDTH = 200, QR_HEIGHT = 200;
        
        private ImageView qr_image;
        private TextView qr_text, qr_result;
        
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.qrcode_image);
            
            qr_image = (ImageView) findViewById(R.id.qr_image);
            qr_text = (EditText) findViewById(R.id.qr_text);
            qr_result = (TextView) findViewById(R.id.qr_result);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
            case R.id.qr_create_image: {
                Bitmap image = QRCodeUtil.encode(qr_text.getText().toString(), QR_WIDTH, QR_HEIGHT);
                if (image != null)
                {
                    qr_image.setImageBitmap(image);
                }
                
                break; }
            case R.id.qr_parse_image:
                Drawable d = qr_image.getDrawable();
                if (d != null)
                {
                    Bitmap image = ((BitmapDrawable) d).getBitmap();
                    qr_result.setText(QRCodeUtil.decode(image));
                }
                
                break;
            }
        }
    }
}