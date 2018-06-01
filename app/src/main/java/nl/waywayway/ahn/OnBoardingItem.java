package nl.waywayway.ahn;

// POJO voor een item in ViewPager

public class OnBoardingItem
{
    private int imageID;
    private String title;
    private String description;

    public OnBoardingItem()
	{
    }

    public int getImageID()
	{
        return imageID;
    }

    public void setImageID(int imageID)
	{
        this.imageID = imageID;
    }

    public String getTitle()
	{
        return title;
    }

    public void setTitle(String title)
	{
        this.title = title;
    }

    public String getDescription()
	{
        return description;
    }

    public void setDescription(String description)
	{
        this.description = description;
    }
}
