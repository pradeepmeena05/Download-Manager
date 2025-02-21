package download_manager;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownLoad_MainActivity extends AppCompatActivity {

    private EditText UrlId;
    private Button downloadButton;
    private String user_url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_down_load_main);

        UrlId = findViewById(R.id.UrlId);
        downloadButton = findViewById(R.id.downloadButton);

        downloadButton.setOnClickListener(v -> {
            user_url = UrlId.getText().toString().trim();

            if (user_url.isEmpty()) {
                Toast.makeText(DownLoad_MainActivity.this, "Enter a valid URL!", Toast.LENGTH_SHORT).show();
                return;
            }else
            {
                askToSave();
            }

            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (inputMethodManager != null) {
                inputMethodManager.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
            }

        });
    }

    private void askToSave() {
        String file_original_name = URLUtil.guessFileName(user_url, null, null);

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_TITLE, file_original_name);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        startActivityForResult(intent, 10);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 10 && resultCode == RESULT_OK && data != null) {
            Uri fileUri = data.getData();
            if (fileUri != null) {
                UrlId.setText("");
                Intent serviceIntent = new Intent(DownLoad_MainActivity.this, ForGroundService.class);
                serviceIntent.setAction("START_DOWNLOAD");
                startForegroundService(serviceIntent);
                new Thread(() -> downloadData(user_url, fileUri)).start();
            }
        }
    }

    public void downloadData(String fileUrl, Uri saveUri) {

        new Thread(()->{

        HttpURLConnection connection = null;
        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            URL url = new URL(fileUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Toast.makeText(getApplicationContext(), "Connection Failded", Toast.LENGTH_SHORT).show();
                return;
            }

            inputStream = connection.getInputStream();
            outputStream = getContentResolver().openOutputStream(saveUri);

            if (outputStream == null) {
                Toast.makeText(getApplicationContext(), "OutputStream null", Toast.LENGTH_SHORT).show();
                return;
            }

            byte[] buffer = new byte[8192];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);

            }

            Intent intent = new Intent(DownLoad_MainActivity.this, ForGroundService.class);
            intent.setAction("DOWNLOAD_COMPLETE");
            startForegroundService(intent);

            outputStream.close();
            inputStream.close();
            connection.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }
        }).start();


    }
}