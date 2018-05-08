package nl.waywayway.ahn;

import android.app.*;
import android.content.*;
import android.os.*;
import android.support.v4.app.*;
import android.view.*;

import android.support.v4.app.DialogFragment;

public class WelcomeDialogFragment extends DialogFragment
{
    /*
	 @Override
	 public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	 {
	 // Inflate the layout to use as dialog or embedded fragment
	 return inflater.inflate(R.layout.welcome_dialog_fragment, container, false);
	 }*/

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();

        builder
			.setTitle(R.string.welcome_dialog_subtitle)
			.setView(inflater.inflate(R.layout.welcome_dialog_fragment, null))
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id)
				{
					// FIRE ZE MISSILES!
				}
			});

        return builder.create();
    }
}

