package nl.waywayway.ahn;

import android.app.*;
import android.content.*;
import android.os.*;
import android.support.v4.app.*;
import android.util.*;
import android.view.*;

import android.support.v4.app.DialogFragment;
import android.widget.*;

public class WelcomeDialogFragment extends DialogFragment
{
	private static final String PREFERENCES_FILENAME = "ahn_preferences";
	private static final String PREFERENCES_KEY_WELCOME_DIALOG_SHOWED = "dialog_showed";
	private Context context;

	@Override
	public void onAttach(Context context)
	{
		super.onAttach(context);
		this.context = context;
	}

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View view = inflater.inflate(R.layout.welcome_dialog_fragment, null);

		// Checkbox: niet meer laten zien
		final CheckBox checkBox = view.findViewById(R.id.welcome_dialog_checkbox);

        builder
			.setTitle(R.string.welcome_dialog_subtitle)
			.setView(view)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id)
				{
					if (checkBox.isChecked())
					{
						SharedPreferences pref = context.getSharedPreferences(PREFERENCES_FILENAME, context.MODE_PRIVATE);
						SharedPreferences.Editor edit = pref.edit();
						edit.putBoolean(PREFERENCES_KEY_WELCOME_DIALOG_SHOWED, true);
						edit.commit();
						//Log.i("HermLog", "dialog niet meer tonen: opgeslagen in sharedpreferences");
					}
				}
			});

        return builder.create();
    }
}

