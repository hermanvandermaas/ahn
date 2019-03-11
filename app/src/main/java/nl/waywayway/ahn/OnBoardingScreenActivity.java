package nl.waywayway.ahn;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;

public class OnBoardingScreenActivity extends AppCompatActivity
{
	private Context context;
	private LinearLayout pagerIndicator;
    private int dotsCount;
    private ImageView[] dots;
    private ViewPager onboardPager;
    private OnBoardingAdapter adapter;
    private Button btnGetStarted;
	private int currentPos = 0;
    private int previousPos = 0;
    private ArrayList<OnBoardingItem> onBoardItems = new ArrayList<>();
	private static final String CURRENT_POS_KEY = "current_pos_key";

    @Override
    protected void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

		context = this;
        btnGetStarted = findViewById(R.id.btn_get_started);
        onboardPager = findViewById(R.id.pager_introduction);
        pagerIndicator = findViewById(R.id.viewPagerCountDots);

		// Herstel savedInstanceState
		if (savedInstanceState != null)
		{
			currentPos = savedInstanceState.getInt(CURRENT_POS_KEY);
			//Log.i("HermLog", "currentPos restored: " + currentPos);
		}

        initializeViewPager();
		initializeButton();
        setUiPageViewController();
    }

	private void initializeViewPager()
	{
		loadData();

        adapter = new OnBoardingAdapter(this, onBoardItems);
        onboardPager.setAdapter(adapter);
        onboardPager.setCurrentItem(0);
        onboardPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
				@Override
				public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
				{
				}

				@Override
				public void onPageSelected(int position)
				{
					//Log.i("HermLog", "onPageSelected()");

					// Zet volgende stip op geselecteerd
					for (int i = 0; i < dotsCount; i++)
					{
						dots[i].setImageDrawable(ContextCompat.getDrawable(OnBoardingScreenActivity.this, R.drawable.non_selected_item_dot));
					}

					dots[position].setImageDrawable(ContextCompat.getDrawable(OnBoardingScreenActivity.this, R.drawable.selected_item_dot));
					// positie beginnend met 1
					int pos = position + 1;
					// positie beginnend met 0
					currentPos = position;
					setButtonVisibility(pos);
					previousPos = pos;
				}

				@Override
				public void onPageScrollStateChanged(int state)
				{
				}
			});
	}

	private void initializeButton()
	{
		btnGetStarted.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v)
				{
					// Onboarding niet nog eens tonen
					SharedPreferences pref = context.getSharedPreferences(getResources().getString(R.string.SHARED_PREFERENCES_FILENAME), context.MODE_PRIVATE);
					SharedPreferences.Editor edit = pref.edit();
					edit.putBoolean(getResources().getString(R.string.PREFERENCES_KEY_SHOW_ONBOARDING_SCREEN), false);
					edit.commit();

					// Start activity
					Intent intent = new Intent(context, MainActivity.class);
					context.startActivity(intent);
					((Activity) context).finish();
				}
			});
	}

	private void setButtonVisibility(int pos)
	{
		//Log.i("HermLog", "pos: " + pos);
		//Log.i("HermLog", "dotscount: " + dotsCount);
		//Log.i("HermLog", "previousPos: " + previousPos);

		if (pos == dotsCount && previousPos == (dotsCount - 1))
			show_animation();
		else if (pos == (dotsCount - 1) && previousPos == dotsCount)
			hide_animation();
	}

    // Load data into the viewpager
    public void loadData()
    {
        int[] header = {
                R.string.ob_header1,
                R.string.ob_header2,
                R.string.ob_header3,
                R.string.ob_header4,
				R.string.ob_header5
        };

        int[] desc = {
                R.string.ob_desc1,
                R.string.ob_desc2,
                R.string.ob_desc3,
                R.string.ob_desc4,
				R.string.ob_desc5
        };

        int[] imageId = {
                R.drawable.onboard_page1,
                R.drawable.onboard_page2,
                R.drawable.onboard_page3,
                R.drawable.onboard_page4,
                R.drawable.onboard_page5
        };

        for (int i=0; i < imageId.length; i++)
        {
            OnBoardingItem item=new OnBoardingItem();
            item.setImageID(imageId[i]);
            item.setTitle(getResources().getString(header[i]));
            item.setDescription(getResources().getString(desc[i]));

            onBoardItems.add(item);
        }
    }

    // Button bottomUp animation
    public void show_animation()
    {
        Animation show = AnimationUtils.loadAnimation(this, R.anim.slide_up_anim);
        btnGetStarted.startAnimation(show);

        show.setAnimationListener(new Animation.AnimationListener() {

				@Override
				public void onAnimationStart(Animation animation)
				{
					btnGetStarted.setVisibility(View.VISIBLE);
				}

				@Override
				public void onAnimationRepeat(Animation animation)
				{
				}

				@Override
				public void onAnimationEnd(Animation animation)
				{
					btnGetStarted.clearAnimation();
				}
			});
    }

    // Button Topdown animation
    public void hide_animation()
    {
        Animation hide = AnimationUtils.loadAnimation(this, R.anim.slide_down_anim);
        btnGetStarted.startAnimation(hide);

        hide.setAnimationListener(new Animation.AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation)
				{
				}

				@Override
				public void onAnimationRepeat(Animation animation)
				{
				}

				@Override
				public void onAnimationEnd(Animation animation)
				{
					btnGetStarted.clearAnimation();
					btnGetStarted.setVisibility(View.GONE);
				}
			});
    }

    private void setUiPageViewController()
	{
		//Log.i("HermLog", "setUiPageViewController()");

        dotsCount = adapter.getCount();
        dots = new ImageView[dotsCount];

        for (int i = 0; i < dotsCount; i++)
		{
            dots[i] = new ImageView(this);
            dots[i].setImageDrawable(ContextCompat.getDrawable(context, R.drawable.non_selected_item_dot));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT
            );

            params.setMargins(6, 0, 6, 0);
            pagerIndicator.addView(dots[i], params);
        }

        dots[0].setImageDrawable(ContextCompat.getDrawable(context, R.drawable.selected_item_dot));
    }

	@Override
	public void onBackPressed()
	{
		if (onboardPager.getCurrentItem() == 0)
		{
			super.onBackPressed();
		}
		else
		{
			onboardPager.setCurrentItem(onboardPager.getCurrentItem() - 1);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		outState.putInt(CURRENT_POS_KEY, currentPos);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onStart()
	{
		if ((currentPos + 1) == dotsCount) show_animation();
		//setButtonVisibility(currentPos + 1);
		super.onStart();
	}
}
