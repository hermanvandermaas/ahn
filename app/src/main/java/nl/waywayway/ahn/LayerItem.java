package nl.waywayway.ahn;

public class LayerItem
{
	private String ID;
    private String title;
	private String shortTitle;
	private String serviceUrl;
	private String WMSGetMapFeatureInfoQueryLayer;
	private double minx;
	private double miny;
	private double maxx;
	private double maxy;
	private boolean visibleByDefault;
	private int opacityDefault;
	private boolean queryable;
	// layerObject komt niet uit json maar wordt programmatisch toegevoegd
	private Object layerObject;

	public void setShortTitle(String shortTitle)
	{
		this.shortTitle = shortTitle;
	}

	public String getShortTitle()
	{
		return shortTitle;
	}

	public void setQueryable(boolean queryable)
	{
		this.queryable = queryable;
	}

	public boolean isQueryable()
	{
		return queryable;
	}

	public void setOpacityDefault(int opacityDefault)
	{
		this.opacityDefault = opacityDefault;
	}

	public int getOpacityDefault()
	{
		return opacityDefault;
	}

	public void setID(String iD)
	{
		ID = iD;
	}

	public String getID()
	{
		return ID;
	}

	public void setVisibleByDefault(boolean visibleByDefault)
	{
		this.visibleByDefault = visibleByDefault;
	}

	public boolean isVisibleByDefault()
	{
		return visibleByDefault;
	}

	public void setLayerObject(Object layerObject)
	{
		this.layerObject = layerObject;
	}

	public Object getLayerObject()
	{
		return layerObject;
	}

	public void setWMSGetMapFeatureInfoQueryLayer(String wMSGetMapFeatureInfoQueryLayer)
	{
		WMSGetMapFeatureInfoQueryLayer = wMSGetMapFeatureInfoQueryLayer;
	}

	public String getWMSGetMapFeatureInfoQueryLayer()
	{
		return WMSGetMapFeatureInfoQueryLayer;
	}

	public void setMinx(double minx)
	{
		this.minx = minx;
	}

	public double getMinx()
	{
		return minx;
	}

	public void setMiny(double miny)
	{
		this.miny = miny;
	}

	public double getMiny()
	{
		return miny;
	}

	public void setMaxx(double maxx)
	{
		this.maxx = maxx;
	}

	public double getMaxx()
	{
		return maxx;
	}

	public void setMaxy(double maxy)
	{
		this.maxy = maxy;
	}

	public double getMaxy()
	{
		return maxy;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public String getTitle()
	{
		return title;
	}

	public void setServiceUrl(String serviceUrl)
	{
		this.serviceUrl = serviceUrl;
	}

	public String getServiceUrl()
	{
		return serviceUrl;
	}
}
