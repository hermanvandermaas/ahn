package nl.waywayway.ahn;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

// Listener: is aangeklikte marker een stip in lijn van hoogteprofiel?
// dan geen standaard actie uitvoeren bij klik (tonen info window en centreren aangeklikte stip marker)

public class CustomOnMarkerClickListener
{
	private CustomOnMarkerClickListener(){}

	public static GoogleMap.OnMarkerClickListener getListener(final String isDot)
	{
		return new GoogleMap.OnMarkerClickListener()
		{

			@Override
			public boolean onMarkerClick(Marker marker)
			{
				String tag = (String) marker.getTag();
				boolean defaultMarkerBehaviour = false;
				if (tag != null) defaultMarkerBehaviour = (tag.equals(isDot)) ? true : false;
				return defaultMarkerBehaviour;
			}
		};
	}
}
