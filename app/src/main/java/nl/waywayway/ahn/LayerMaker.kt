package nl.waywayway.ahn

import android.content.Context
import android.content.res.Resources
import android.util.Log
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.TileOverlay
import com.google.android.gms.maps.model.TileOverlayOptions
import com.google.android.gms.maps.model.TileProvider

// Class voor maken en toevoegen van kaartlagen

class LayerMaker(val context: Context) {
    fun addLayersToMap(layerList: List<LayerItem>, gMap: GoogleMap) {
        for (layerItem in layerList) {
            // Voeg (deels) zichtbare lagen toe aan kaart
            // Zichtbaarheid en dekkendheid laag instellen uit SharedPreference of default
            val preferences = getSharedPreferences(context, layerItem)
            val visible = getVisibility(layerItem, preferences)
            val opacity = getOpacity(layerItem, preferences)

            // Voeg laag toe
            if (visible && opacity > 0) createLayer(layerItem, opacity, gMap)
        }
    }

    fun getSharedPreferences(context: Context, layerItem: LayerItem): IntArray? {
        return LayersSaveAndRestore.getInstance(context, layerItem.getID()).restore()
    }

    fun getVisibility(layerItem: LayerItem, preferences: IntArray?): Boolean {
        if (preferences == null) {
            return layerItem.isVisibleByDefault()
            //Log.i("HermLog", "isVisibleByDefault: " + layerItem.isVisibleByDefault());
        } else {
            //Log.i("HermLog", "Instellen uit SharedPreferences (laag/visible): " + layerItem.getTitle() + " / " + (preferences[0] == 1))
            return preferences[0] == 1
        }
    }

    fun getOpacity(layerItem: LayerItem, preferences: IntArray?): Int {
        if (preferences == null) {
            return layerItem.getOpacityDefault()
            //Log.i("HermLog", "opacityDefault: " + layerItem.getOpacityDefault());
        } else {
            //Log.i("HermLog", "Instellen uit SharedPreferences (laag/opacity): " + layerItem.getTitle() + " / " + preferences[1])
            return preferences[1]
        }
    }

    fun createLayer(layerItem: LayerItem, opacity: Int, gMap: GoogleMap): TileOverlay {
        // Maak TileOverlay,
        // zIndex is gelijk aan ID van de laag
        // hoogste zIndex ligt bovenop
        //Log.i("HermLog", "createLayer: " + layerItem.shortTitle)
        val zIndex = java.lang.Float.parseFloat(layerItem.id)
        val myTileProvider: TileProvider

        // Kies WMS of WMTS UrlTileProvider
        if (layerItem.serviceType == "wms") {
            myTileProvider = WMSTileProvider.getTileProvider(
                    256,
                    256,
                    layerItem.serviceUrl,
                    layerItem.minx,
                    layerItem.miny,
                    layerItem.maxx,
                    layerItem.maxy
            )
        } else {
            myTileProvider = WMTSTileProvider(
                    256,
                    256,
                    layerItem.serviceUrl,
                    layerItem.minZoom,
                    layerItem.maxZoom
            )
        }

        // Bij zichtbaar maken van andere achtergrondkaart met labels (BGT), verberg features van Google basiskaart
        if (layerItem.isBaseMap) {
            try {
                val success = gMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style_empty))
                if (!success) Log.e("HermLog", "Style parsing failed.")
            } catch (e: Resources.NotFoundException) {
                Log.e("HermLog", "Can't find style. Error: ", e)
            }
        }

        // Voeg laag toe
        val tileOverlay = gMap.addTileOverlay(TileOverlayOptions().zIndex(zIndex).tileProvider(myTileProvider))
        tileOverlay.setTransparency(1f - opacity / 100f)

        // Zet referentie naar kaartlaag in lijst
        layerItem.layerObject = tileOverlay

        return tileOverlay
    }
}