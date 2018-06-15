package nl.waywayway.ahn;

import android.os.*;
import android.support.v7.app.*;
import android.support.v7.widget.*;

public class InformationActivity extends AppCompatActivity
{
	@Override
    protected void onCreate(Bundle savedInstanceState)
    {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_information);

		// Maak toolbar
		makeToolBar();
		
	}

	private void makeToolBar()
	{
		Toolbar toolbar = findViewById(R.id.toolbar_info);
		setSupportActionBar(toolbar);
		getSupportActionBar().setTitle(R.string.information_activity_title);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}
}
