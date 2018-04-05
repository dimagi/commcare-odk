package org.commcare.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ShareActionProvider;
import android.widget.TextView;

import org.commcare.dalvik.R;
import org.commcare.logging.DataChangeLogger;

public class DataChangeLogsActivity extends Activity {


    private ShareActionProvider mShareActionProvider;
    private String mlogs;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_change_logs);
        mlogs = DataChangeLogger.getLogs();
        ((TextView)findViewById(R.id.logs_tv)).setText(mlogs);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            setUpShareActionProvider(menu);
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void setUpShareActionProvider(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_share, menu);
        MenuItem item = menu.findItem(R.id.menu_item_share);
        mShareActionProvider = (ShareActionProvider)item.getActionProvider();
        setShareIntent();
    }

    // Call to update the share intent
    @RequiresApi(api = Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void setShareIntent() {
        if (mShareActionProvider != null) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, mlogs);
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }
}
