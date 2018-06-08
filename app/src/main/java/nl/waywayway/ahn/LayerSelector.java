package nl.waywayway.ahn;

import android.content.*;
import java.util.*;

public class LayerSelector
{
	private ArrayList<LayerItem> layerList;
	private Context context;

	public LayerSelector(ArrayList<LayerItem> layerList, Context context)
	{
		this.layerList = layerList;
		this.context = context;
	}
	
	// Hoogste zichtbare laag uit de lijst
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
			}
			else
			{
				boolean visible = preferences[0] == 1 ? true : false;
				int opacity = preferences[1];
				if (visible && opacity > 0) returnLayerItem = layerItem;
			}
		}

		return returnLayerItem;
	}
}
