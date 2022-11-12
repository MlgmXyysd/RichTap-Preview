package mobi.meow.richtap;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.apprichtap.haptic.RichTapUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import mobi.meow.richtap.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    @SuppressLint("StaticFieldLeak")
    static Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getBaseContext();
        RichTapUtils.getInstance().init(mContext);

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        String path = mContext.getCacheDir().getAbsolutePath();
        extractAssets(path);

        File[] resources = new File(path + "/resources").listFiles();
        for (File haptic : Objects.requireNonNull(resources)) {
            if (haptic.getName().endsWith(".he")) {
                Button btn = new Button(this);
                btn.setText(haptic.getName().replace(".he", ""));
                btn.setOnClickListener(v -> RichTapUtils.getInstance().playHaptic(haptic, 1));
                binding.layout.addView(btn, FrameLayout.LayoutParams.WRAP_CONTENT);
            }
        }
    }

    @Override
    public void finish() {
        RichTapUtils.getInstance().quit();
        super.finish();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void extractAssets(String path) {
        try {
            InputStream dataSource = mContext.getAssets().open("resources.zip");
            ZipInputStream in = new ZipInputStream(dataSource);
            ZipEntry entry = in.getNextEntry();
            while (entry != null) {
                File file = new File(path);
                if (!file.exists()) {
                    file.mkdirs();
                }
                if (entry.isDirectory()) {
                    String name = entry.getName();
                    name = name.substring(0, name.length() - 1);
                    file = new File(path + File.separator + name);
                    if (!file.exists()) {
                        file.mkdir();
                    }
                } else {
                    file = new File(path + File.separator + entry.getName());
                    if (!file.exists()) {
                        file.createNewFile();
                        FileOutputStream out = new FileOutputStream(file);
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = in.read(buffer)) > 0) {
                            out.write(buffer, 0, length);
                        }
                        out.close();
                    }
                }
                entry = in.getNextEntry();
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}