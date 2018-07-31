package nl.waywayway.ahn;

import android.app.*;
import android.content.*;
import android.os.*;
import android.support.v4.app.*;
import android.support.v7.app.*;

import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

// Dialoogvenster voor bevestigen actie

public class CancelOrProceedDialogFragment extends DialogFragment
{
	// Interface voor aanroepen callbacks in Activity
	public interface YesNoDialog
	{
		public void onYes(DialogInterface dialog, int id);
		public void onNo(DialogInterface dialog, int id);
	}

	private YesNoDialog yesNoDialog;
	private int bodyText;
	private int yesText;
	private int noText;

	// Variabelen instellen bij Fragment instantiatie
	static CancelOrProceedDialogFragment newInstance(int bodyText, int yesText, int noText)
	{
        CancelOrProceedDialogFragment f = new CancelOrProceedDialogFragment();

        Bundle args = new Bundle();
		args.putInt("bodyText", bodyText);
		args.putInt("yesText", yesText);
		args.putInt("noText", noText);
        f.setArguments(args);

        return f;
    }

	@Override
	public void onAttach(Context context)
	{
		super.onAttach(context);
		yesNoDialog = (YesNoDialog) context;
	}

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		bodyText = getArguments().getInt("bodyText");
		yesText = getArguments().getInt("yesText");
		noText = getArguments().getInt("noText");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(bodyText)
			.setPositiveButton(yesText, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id)
				{
					yesNoDialog.onYes(dialog, id);
				}
			})
			.setNegativeButton(noText, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id)
				{
					yesNoDialog.onNo(dialog, id);
				}
			});

        return builder.create();
    }
}
