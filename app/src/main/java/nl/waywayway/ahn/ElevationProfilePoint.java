package nl.waywayway.ahn;

public class ElevationProfilePoint
{
	private double distanceMeters;
	private double elevationMeters;

	public ElevationProfilePoint(double distanceMeters, double elevationMeters)
	{
		this.distanceMeters = distanceMeters;
		this.elevationMeters = elevationMeters;
	}
	
	public void setElevationMeters(double elevationMeters)
	{
		this.elevationMeters = elevationMeters;
	}

	public double getElevationMeters()
	{
		return elevationMeters;
	}
	
	public void setDistanceMeters(double distanceMeters)
	{
		this.distanceMeters = distanceMeters;
	}

	public double getDistanceMeters()
	{
		return distanceMeters;
	}
}
