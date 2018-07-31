package nl.waywayway.ahn;

public class ElevationProfileVertex
{
	private double horizontalDistanceMeters;
	private double elevationMeters;

	public void setElevationMeters(double elevationMeters)
	{
		this.elevationMeters = elevationMeters;
	}

	public double getElevationMeters()
	{
		return elevationMeters;
	}
	
	public void setHorizontalDistanceMeters(double horizontalDistanceMeters)
	{
		this.horizontalDistanceMeters = horizontalDistanceMeters;
	}

	public double getHorizontalDistanceMeters()
	{
		return horizontalDistanceMeters;
	}
}
