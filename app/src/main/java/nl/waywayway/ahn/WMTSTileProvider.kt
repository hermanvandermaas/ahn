package nl.waywayway.ahn

import android.util.Log
import com.google.android.gms.maps.model.UrlTileProvider
import java.net.MalformedURLException
import java.net.URL
import java.util.*

// Class voor WMTS tiles in Web Mercator projectie

// Url formaat WMTS voorbeeld: http://geodata.nationaalgeoregister.nl/wmts?SERVICE=WMTS&REQUEST=GetTile&VERSION=1.0.0&LAYER=brtachtergrondkaartpastel&STYLE=default&TILEMATRIXSET=EPSG:3857&TILEMATRIX=16&TILECOL=33824&TILEROW=21358&FORMAT=image/png
// x = tilecol, y = tilerow, z (zoomniveau) = tilematrix

class WMTSTileProvider(val xTileSize: Int, val yTileSize: Int, val urlFormat: String, val minZoom: Int, val maxZoom: Int) : UrlTileProvider(xTileSize, yTileSize) {

    override fun getTileUrl(x: Int, y: Int, zoom: Int): URL? {
        val s = String.format(
                Locale.US,
                urlFormat,
                zoom,
                x,
                y)

        if (!checkTileExists(x, y, zoom)) {
            return null
        }

        try {
            Log.i("HermLog", "WMTS url: " + s)
            return URL(s)
        } catch (e: MalformedURLException) {
            throw AssertionError(e)
        }
    }

    fun checkTileExists(x: Int, y: Int, zoom: Int): Boolean {
        return if (zoom < minZoom || zoom > maxZoom) {
            false
        } else true
    }
}