package nl.waywayway.ahn;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

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
		
		TextView textView3 = findViewById(R.id.activity_info_kaart_delen);
		textView3.setMovementMethod(LinkMovementMethod.getInstance());
		
	}

	private void makeToolBar()
	{
		Toolbar toolbar = findViewById(R.id.toolbar_info);
		setSupportActionBar(toolbar);
		getSupportActionBar().setTitle(R.string.information_activity_title);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}
}
