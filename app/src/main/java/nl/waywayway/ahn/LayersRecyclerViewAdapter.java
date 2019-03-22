package nl.waywayway.ahn;

import android.content.Context;
import androidx.core.view.GestureDetectorCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.TileOverlay;

import java.util.List;

public class LayersRecyclerViewAdapter extends RecyclerView.Adapter<LayersRecyclerViewAdapter.CustomViewHolder>
{
	// Interface voor aanroepen methods in Activity
	public interface AdapterCallbacks
	{
		public TileOverlay createLayer(LayerItem layerItem, int opacity);
	}

    private Context context;
    private List<LayerItem> layerList;
    private  GoogleMap gMap;

    public LayersRecyclerViewAdapter(Context context, List<LayerItem> layerList, GoogleMap gMap)
	{
        this.context = context;
        this.layerList = layerList;
        this.gMap = gMap;
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
		Log.i("HermLog", "LayersRecyclerViewAdapter.onBindViewHolder: i: " + i);

		final LayerItem layerItem = layerList.get(i);
		final CustomViewHolder mCustomViewHolder = customViewHolder;
		Log.i("HermLog", "LayersRecyclerViewAdapter.onBindViewHolder: layerItem getShortTitle / getID: " + layerItem.getShortTitle() + " / " + layerItem.getID());

		//Set text views
        customViewHolder.checkBoxView.setText(layerItem.getTitle());

		// NavigationView (drawer), niet laten meeschuiven met SeekBar
		customViewHolder.seekBarView.setOnTouchListener(new SeekBarTouchListener(context));

		LayerMaker layerMaker = new LayerMaker(context);
		int[] preferences = layerMaker.getSharedPreferences(context, layerItem);
		boolean visible = layerMaker.getVisibility(layerItem, preferences);
		int opacity = layerMaker.getOpacity(layerItem, preferences);

		customViewHolder.checkBoxView.setChecked(visible);
		customViewHolder.seekBarView.setProgress(opacity);

		// Instellen percentage dekkendheid in tekstlabel bij SeekBar
		String description = context.getResources().getString(R.string.opacity_seekbar_description);
		customViewHolder.seekBarLabelView.setText(description + " " + opacity + "%");

		// Transparantie aanpassen listener
		customViewHolder.seekBarView.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
			{

				@Override       
				public void onStopTrackingTouch(SeekBar seekBar)
				{}       

				@Override       
				public void onStartTrackingTouch(SeekBar seekBar)
				{}       

				// Onzichtbare laag verwijderen, voorkomt onnodig downloaden van onzichtbare laag
				@Override
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
				{
					TileOverlay layer = (TileOverlay) layerItem.getLayerObject();

					if (progress > 0)
					{
						if (layer == null && mCustomViewHolder.checkBoxView.isChecked())
						{
							LayerMaker layerMaker = new LayerMaker(context);
							layer = layerMaker.createLayer(layerItem, progress, gMap);
							layerItem.setLayerObject(layer);
						}
					}
					else
					{
						if (layer != null) layer.remove();
						layerItem.setLayerObject(null);
					}

					if (layer != null)
					{
						float transp = 1f - progress / 100f;
						layer.setTransparency(transp);
						//Log.i("HermLog", "seekBar verschoven, transp: " + transp);
					}

					// Opslaan in SharedPreferences
					int visible = mCustomViewHolder.checkBoxView.isChecked() ? 1 : 0;
					LayersSaveAndRestore.getInstance(context, layerItem.getID()).save(visible, progress);

					// Aanpassen percentage dekkendheid in tekst label bij SeekBar
					String description = context.getResources().getString(R.string.opacity_seekbar_description);
					mCustomViewHolder.seekBarLabelView.setText(description + " " + progress + "%");
				}
			});

		// Laag aan/uit listener
		// verwijdert laag van, of voegt toe aan lagenlijst en kaart
		customViewHolder.checkBoxView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
			{
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
				{
					TileOverlay layer = (TileOverlay) layerItem.getLayerObject();
					//Log.i("HermLog", "onCheckedChangeListener layer: " + layer);

					if (buttonView.isChecked())
					{
						int progress = mCustomViewHolder.seekBarView.getProgress();

						if (layer == null && progress > 0)
						{
                            LayerMaker layerMaker = new LayerMaker(context);
                            layer = layerMaker.createLayer(layerItem, progress, gMap);
							layerItem.setLayerObject(layer);
						}

						if (layer != null) layer.setVisible(true);
					}
					else 
					{
						if (layer != null) layer.remove();
						layerItem.setLayerObject(null);
					}

					// Opslaan in SharedPreferences
					int visible = mCustomViewHolder.checkBoxView.isChecked() ? 1 : 0;
					int opacity = mCustomViewHolder.seekBarView.getProgress();
					LayersSaveAndRestore.getInstance(context, layerItem.getID()).save(visible, opacity);
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
		protected TextView seekBarLabelView;

        public CustomViewHolder(View view)
		{
            super(view);
            this.checkBoxView = view.findViewById(R.id.layer_checkbox);
			this.seekBarView = view.findViewById(R.id.layer_seekbar);
			this.seekBarLabelView = view.findViewById(R.id.seekbar_label);
        }
    }
}
