package nl.waywayway.ahn;

import android.content.*;
import android.util.*;
import java.util.*;

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
	
	// Hoogste zichtbare laag uit de lijst
	// kan null zijn
	// uitgangspunt: layerList is in juiste volgorde gesorteerd
	public LayerItem getTopVisibleLayer()
	{
		LayerItem returnLayerItem = null;

		for (LayerItem layerItem : layerList)
		{
			if (layerItem.isQueryable() == false) continue;

			int[] preferences = LayersSaveAndRestore.getInstance(context, layerItem.getID()).restore();

			if (preferences == null)
			{
				boolean visible = layerItem.isVisibleByDefault();
				int opacity = layerItem.getOpacityDefault();
				if (visible && opacity > 0) returnLayerItem = layerItem;
				return returnLayerItem;
			}
			else
			{
				boolean visible = preferences[0] == 1 ? true : false;
				int opacity = preferences[1];
				if (visible && opacity > 0) returnLayerItem = layerItem;
				return returnLayerItem;
			}
		}
		
		return null;
	}
	
	// Lijst met zichtbare lagen met hoogte
	// kan null zijn
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
}
