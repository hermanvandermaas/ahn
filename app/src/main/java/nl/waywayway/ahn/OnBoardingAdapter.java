package nl.waywayway.ahn;

import android.content.*;
import android.support.v4.view.*;
import android.view.*;
import android.widget.*;
import java.util.*;

class OnBoardingAdapter extends PagerAdapter
{
    private Context context;
    private ArrayList<OnBoardingItem> onBoardingItems = new ArrayList<>();

    public OnBoardingAdapter(Context context, ArrayList<OnBoardingItem> items)
	{
        this.context = context;
        this.onBoardingItems = items;
    }

    @Override
    public int getCount()
	{
        return onBoardingItems.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object)
	{
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position)
	{
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_onboarding, container, false);

        OnBoardingItem item = onBoardingItems.get(position);

        ImageView imageView = itemView.findViewById(R.id.iv_onboard);
        imageView.setImageResource(item.getImageID());

        TextView tv_title = itemView.findViewById(R.id.tv_header);
        tv_title.setText(item.getTitle());

        TextView tv_content = itemView.findViewById(R.id.tv_desc);
        tv_content.setText(item.getDescription());

        container.addView(itemView);

        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object)
	{
        container.removeView((LinearLayout) object);
    }
}
