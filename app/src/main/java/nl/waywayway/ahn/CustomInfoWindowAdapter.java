package nl.waywayway.ahn;

import android.app.*;
import android.content.*;
import android.view.*;
import android.widget.*;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;

class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter
{
	private Context context;
	private final View contents;

	public CustomInfoWindowAdapter(Context context)
	{
		this.context = context;
		contents = ((Activity) context).getLayoutInflater().inflate(R.layout.custom_info_window_contents, null);
	}

	@Override
	public View getInfoWindow(Marker marker)
	{
		return null;
	}

	@Override
	public View getInfoContents(Marker marker)
	{
		render(marker, contents);
		return contents;
	}

	private void render(Marker marker, View view)
	{
		String title = marker.getTitle();
		TextView titleUi = view.findViewById(R.id.title);
		titleUi.setText(title);

		String snippet = marker.getSnippet();
		TextView snippetUi = view.findViewById(R.id.snippet);
		snippetUi.setText(snippet);
	}
}
