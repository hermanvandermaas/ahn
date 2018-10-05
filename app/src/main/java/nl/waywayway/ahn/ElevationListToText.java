package nl.waywayway.ahn;

import android.content.Context;

import java.util.ArrayList;

// Maak tekst string van lijst met hoogte in meters 

public class ElevationListToText
{
	public static String toText(Context context, ArrayList<Double> elevationList, ArrayList<String> shortTitles)
	{
		ArrayList<String> elevationRoundedStringList = new ArrayList<String>();
		String ahn1ShortTitle = context.getResources().getString(R.string.ahn1_short_title);
		int i = 0;
		
		// Maak lijst van Strings met hoogte, afgerond en opgemaakt
		for (Double elevation : elevationList)
		{
			String elevationRounded;

			if (elevation == null || elevation > 10000d || elevation < -10000d)
			{
				elevationRounded = context.getResources().getString(R.string.not_available_UI);
			}
			else
			{
				// AHN1 is in centimeters dus delen door 100
				if (shortTitles.get(i).equals(ahn1ShortTitle))
				{
					elevation = elevation / 100d;
				}

				elevationRounded = String.format("%.2f", elevation);
				if (elevation > 0) elevationRounded = "+" + elevationRounded;
			}

			elevationRoundedStringList.add(elevationRounded);
			i++;
		}
		
		String snippet = "";

		// Maak regels voor tekst bij marker
		for (int x = 0; x < elevationRoundedStringList.size(); x++)
		{
			String affix = elevationRoundedStringList.get(x).equals(context.getResources().getString(R.string.not_available_UI)) ? "" : " " + context.getResources().getString(R.string.unit_of_measurement_UI);
			String newLine = (x == (elevationRoundedStringList.size() - 1)) ? "" : "\n";
			String line = shortTitles.get(x) + ": " + elevationRoundedStringList.get(x) + affix + newLine;
			snippet = snippet + line;
		}

		return snippet;
	}
}
