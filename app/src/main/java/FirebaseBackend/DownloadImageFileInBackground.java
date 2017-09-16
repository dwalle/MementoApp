package FirebaseBackend;

import android.app.Activity;
import android.os.AsyncTask;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.target.Target;

import java.io.File;

import static com.mementoapp.main.mementoapp.R.id.textView;

/**
 * Created by Daniel on 8/9/2017.
 */

public class DownloadImageFileInBackground  extends AsyncTask<String, Void, String> {

    public File imageFile = null;
    public Activity act = null;
    public boolean downloaded = false;

    @Override
    protected String doInBackground(String... url) {
        FutureTarget<File> future = Glide.with(act)
                .load(url[0].trim())
                .downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);
        downloaded = true;
        try {
            imageFile = future.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}