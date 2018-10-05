package nl.waywayway.ahn;

import android.content.Context;
import android.widget.Toast;

import java.util.ArrayList;

// Factory voor selecties van kaartlagen

public class LayerSelector
{
	private ArrayList<LayerItem> layerList;
	private Context context;

	public static LayerSelector getLayerSelector(ArrayList<LayerItem> cLayerList, Context cContext)
	{
		return new LayerSelector(cLayerList, cContext);
	}
	
	private LayerSelector(ArrayList<LayerItem> layerList, Context context)
	{
		this.layerList = layerList;
		this.context = context;
	}
	
	// Bovenste zichtbare laag met hoogte (queryable) uit de lijst
	// kan null zijn
	// uitgangspunt: layerList is in juiste volgorde gesorteerd
	public LayerItem getTopVisibleLayer()
	{
		//Log.i("HermLog", "layerList.size(): " + layerList.size());

		for (LayerItem layerItem : layerList)
		{
			//Log.i("HermLog", "layerItem: " + layerItem.getShortTitle());
			//Log.i("HermLog", "layerItem.isQueryable(): " + layerItem.isQueryable());
			if (layerItem.isQueryable() == false) continue;

			int[] preferences = LayersSaveAndRestore.getInstance(context, layerItem.getID()).restore();
			//Log.i("HermLog", "preferences: " + preferences);
			
			if (preferences == null)
			{
				boolean visible = layerItem.isVisibleByDefault();
				//Log.i("HermLog", "layerItem.isVisibleByDefault(): " + layerItem.isVisibleByDefault());
				int opacity = layerItem.getOpacityDefault();
				//Log.i("HermLog", "layerItem.getOpacityDefault(): " + layerItem.getOpacityDefault());
				//Log.i("HermLog", "layerItem: " + layerItem.getShortTitle());
				//Log.i("HermLog", "visible / opacity: " + visible + " / " + opacity);
				if (visible && opacity > 0) return layerItem;
			}
			else
			{
				boolean visible = preferences[0] == 1 ? true : false;
				int opacity = preferences[1];
				if (visible && opacity > 0) return layerItem;
			}
		}
		
		return null;
	}
	
	// Lijst met zichtbare lagen met hoogte
	public ArrayList<LayerItem> getVisibleQueryableLayers()
	{
		ArrayList<LayerItem> visibleLayers = new ArrayList<LayerItem>();

		for (LayerItem layerItem : layerList)
		{
			if (!layerItem.isQueryable()) continue;

			int[] preferences = LayersSaveAndRestore.getInstance(context, layerItem.getID()).restore();

			if (preferences == null)
			{
				boolean visible = layerItem.isVisibleByDefault();
				int opacity = layerItem.getOpacityDefault();
				if (visible && opacity > 0) visibleLayers.add(layerItem);
			}
			else
			{
				boolean visible = preferences[0] == 1 ? true : false;
				int opacity = preferences[1];
				if (visible && opacity > 0) visibleLayers.add(layerItem);
			}
		}
		
		//Log.i("HermLog", "Aantal zichtbare lagen:" + visibleLayers.size());
		return visibleLayers;
	}
	
	// Toon boodschap als geen queryable laag zichtbaar is
	public boolean showMessageNoVisibleQueryableLayers()
	{
		//ArrayList<LayerItem> visibleLayers = getLayerSelector(layerList, context).getVisibleQueryableLayers();
		if (getTopVisibleLayer() == null)
		{
			Toast.makeText(context, context.getResources().getString(R.string.make_layer_with_altitude_visible_message), Toast.LENGTH_LONG).show();
			return true;
		}
		else
		{
			return false;
		}
	}
}
