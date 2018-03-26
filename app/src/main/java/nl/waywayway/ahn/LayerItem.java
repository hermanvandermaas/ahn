package nl.waywayway.ahn;

public class LayerItem
{
    private String title;
	private String serviceUrl;
	private String WMSGetMapFeatureInfoQueryLayer;
	private double minx;
	private double miny;
	private double maxx;
	private double maxy;

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
