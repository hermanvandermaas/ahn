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
		final CustomViewHolder customViewHolderCopy = customViewHolder;
		
        //Set text views
        customViewHolder.checkBoxView.setText(layerItem.getTitle());

		// NavigationView (drawer), niet laten meeschuiven met SeekBar
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
					
					// Handle seekbar touch events
					v.onTouchEvent(event);
					
					return true;
				}
			});

		// Transparantie aanpassen listener
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
					//Log.i("HermLog", "seekBar verschoven, transp: " + transp);

					// Opslaan in SharedPreferences
					int visible = customViewHolderCopy.checkBoxView.isChecked() ? 1 : 0;
					LayersSaveAndRestore.getInstance(context, layerItem.getID()).save(visible, progress);
				}
			});

		// Laag aan/uit listener
		customViewHolder.checkBoxView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
			{
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
				{
					TileOverlay layer = (TileOverlay) layerItem.getLayerObject();
					
					if (buttonView.isChecked()) layer.setVisible(true);
					else layer.setVisible(false);
					
					// Opslaan in SharedPreferences
					int visible = customViewHolderCopy.checkBoxView.isChecked() ? 1 : 0;
					int opacity = customViewHolderCopy.seekBarView.getProgress();
					LayersSaveAndRestore.getInstance(context, layerItem.getID()).save(visible, opacity);
				}
			});
			
		// Zichtbaarheid en dekkendheid laag instellen uit SharedPreferences
		// of default instellen
		int[] preferences = LayersSaveAndRestore.getInstance(context, layerItem.getID()).restore();
		
		if (preferences == null)
		{
			Log.i("HermLog", "isVisibleByDefault: " + layerItem.isVisibleByDefault());
			Log.i("HermLog", "opacityDefault: " + layerItem.getOpacityDefault());
			Log.i("HermLog", "preferences opgeslagen: " + preferences);
			customViewHolder.checkBoxView.setChecked(layerItem.isVisibleByDefault());
			customViewHolder.seekBarView.setProgress(layerItem.getOpacityDefault());
		}
		else
		{
			boolean visible = preferences[0] == 1 ? true : false;
			int opacity = preferences[1];
			customViewHolder.checkBoxView.setChecked(visible);
			customViewHolder.seekBarView.setProgress(opacity);
			Log.i("HermLog", "Instellen uit SharedPreferences (visible/opacity): " + visible + "/" + opacity);
		}
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
