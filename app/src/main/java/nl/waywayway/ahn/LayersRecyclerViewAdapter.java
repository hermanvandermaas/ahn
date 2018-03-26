package nl.waywayway.ahn;

import android.content.*;
import android.support.v7.widget.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import com.google.android.gms.maps.model.*;
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

		// NavigationView (drawer) niet laten meeschuiven met SeekBar
		customViewHolder.seekBarView.setOnTouchListener(new View.OnTouchListener() 
			{
				@Override
				public boolean onTouch(View v, MotionEvent event) 
				{
					int action = event.getAction();

					switch (action) 
					{
						case MotionEvent.ACTION_DOWN:
							v.getParent().requestDisallowInterceptTouchEvent(true);
							break;

						case MotionEvent.ACTION_UP:
							v.getParent().requestDisallowInterceptTouchEvent(true);
							break;
					}

					// Transparantie aanpassen
					v.onTouchEvent(event);
					return true;
				}
			});

		// Laag zichtbaar/onzichtbaar
		customViewHolder.seekBarView.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
			{

				@Override       
				public void onStopTrackingTouch(SeekBar seekBar)
				{}       

				@Override       
				public void onStartTrackingTouch(SeekBar seekBar)
				{}       

				@Override       
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
				{
					TileOverlay layer = (TileOverlay) layerItem.getLayerObject();
					float transp = 1f - progress / 100f;
					layer.setTransparency(transp);
					Log.i("HermLog", "seekBar verschoven, transp: " + transp);

				}
			});

		// Laag aan/uit
		customViewHolder.checkBoxView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
			{
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
				{
					TileOverlay layer = (TileOverlay) layerItem.getLayerObject();
					
					if (buttonView.isChecked()) layer.setVisible(true);
					else layer.setVisible(false);
				}
			});
    }

    @Override
    public int getItemCount()
	{
        return (null != layerList ? layerList.size() : 0);
    }

    class CustomViewHolder extends RecyclerView.ViewHolder
	{
        protected CheckBox checkBoxView;
		protected SeekBar seekBarView;

        public CustomViewHolder(View view)
		{
            super(view);
            this.checkBoxView = (CheckBox) view.findViewById(R.id.layer_checkbox);
			this.seekBarView = (SeekBar) view.findViewById(R.id.layer_seekbar);
        }
    }
}
