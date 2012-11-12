package org.commcare.dalvik.activities;

import org.commcare.dalvik.R;
import org.javarosa.core.services.locale.Localization;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class ReportProblemActivity extends Activity implements OnClickListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_problem);
        Button submitButton = (Button)findViewById(R.id.ReportButton01);
        submitButton.setText(Localization.get("report.problem.button"));
        submitButton.setOnClickListener(this);
        ((TextView)findViewById(R.id.ReportPrompt01)).setText("report.problem.prompt");
    }

	@Override
	public void onClick(View v) {
		EditText mEdit = (EditText)findViewById(R.id.ReportText01);
		String reportEntry = mEdit.getText().toString();
		Intent mIntent = new Intent();
		mIntent.putExtra("result",reportEntry);
		setResult(1,mIntent);
		finish();
	}

}
