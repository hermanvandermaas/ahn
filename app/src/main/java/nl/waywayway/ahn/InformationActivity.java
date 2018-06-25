package nl.waywayway.ahn;

import android.os.*;
import android.support.v7.app.*;
import android.support.v7.widget.*;
import android.text.method.*;
import android.widget.*;

import android.support.v7.widget.Toolbar;

public class InformationActivity extends AppCompatActivity
{
	@Override
    protected void onCreate(Bundle savedInstanceState)
    {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_information);

		setLinks();
		
		// Maak toolbar
		makeToolBar();
	}

	private void setLinks()
	{
		TextView textView1 = findViewById(R.id.activity_info_over_ahn);
		textView1.setMovementMethod(LinkMovementMethod.getInstance());
		
		TextView textView2 = findViewById(R.id.activity_info_bronvermelding);
		textView2.setMovementMethod(LinkMovementMethod.getInstance());
	}

	private void makeToolBar()
	{
		Toolbar toolbar = findViewById(R.id.toolbar_info);
		setSupportActionBar(toolbar);
		getSupportActionBar().setTitle(R.string.information_activity_title);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}
}
