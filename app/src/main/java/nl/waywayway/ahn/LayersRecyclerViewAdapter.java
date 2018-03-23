package nl.waywayway.ahn;

import android.content.*;
import android.support.v7.widget.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import java.util.*;

public class LayersRecyclerViewAdapter extends RecyclerView.Adapter<LayersRecyclerViewAdapter.CustomViewHolder>
{
    private List<LayerItem> layerList;
    private Context context;

    public LayersRecyclerViewAdapter(Context context, List<LayerItem> layerList)
	{
        this.layerList = layerList;
        this.context = context;
	}

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i)
	{		
		int layerItemLayout = R.layout.recyclerview_item_listlayout;
		View view = LayoutInflater.from(viewGroup.getContext()).inflate(layerItemLayout, null);
		CustomViewHolder viewHolder = new CustomViewHolder(view);

		return viewHolder;
    }

    @Override
    public void onBindViewHolder(CustomViewHolder customViewHolder, int i)
	{
        final LayerItem layerItem = layerList.get(i);

        //Set text views
        customViewHolder.checkBoxView.setText(layerItem.getTitle());
    }

    @Override
    public int getItemCount()
	{
        return (null != layerList ? layerList.size() : 0);
    }

    class CustomViewHolder extends RecyclerView.ViewHolder
	{
        protected CheckBox checkBoxView;
		
        public CustomViewHolder(View view)
		{
            super(view);
            this.checkBoxView = (CheckBox) view.findViewById(R.id.layer_checkbox);
        }
    }
}
