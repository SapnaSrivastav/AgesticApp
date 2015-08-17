package com.agestic.flickr;

import java.util.List;
import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;


public class ImageDetails extends Activity {

	private ViewPager pager;

	private DisplayImageOptions options;
	 protected ImageLoader imageLoader = ImageLoader.getInstance();

	    
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ac_image_pager);

		//getting serialized data from MainActivity
		Bundle bundle = getIntent().getExtras();
		final List<String> imageUrls = bundle.getStringArrayList(MainActivity.IMAGE_URL);
		final List<String> titles = bundle.getStringArrayList(MainActivity.IMAGE_TITLE);
		final List<String> names = bundle.getStringArrayList(MainActivity.IMAGE_NAME);
		final List<String> updates = bundle.getStringArrayList(MainActivity.UPDATED);
		final List<String> published = bundle.getStringArrayList(MainActivity.PUBLISHED);
		
		int pagerPosition = bundle.getInt(MainActivity.IMAGE_POSITION, 0);

		//Setting DisplayImageOptions for UIL
	      options = new DisplayImageOptions.Builder()
				.showImageOnLoading(R.drawable.stub_image) //setting image to show on loading
				.showImageForEmptyUri(R.drawable.image_for_empty_url)
				.showImageOnFail(R.drawable.image_for_empty_url)
				.cacheInMemory(true)
				.cacheOnDisk(true)
				.considerExifParams(true)
				.bitmapConfig(Bitmap.Config.RGB_565)
				.build(); 

	    //initializing viewpager  
		pager = (ViewPager) findViewById(R.id.pager);
		pager.setAdapter(new ImagePagerAdapter(imageUrls,titles,names,updates,published));
		pager.setCurrentItem(pagerPosition);

	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	//Adapter for Pager
	private class ImagePagerAdapter extends PagerAdapter {
		
		private List<String> images;
		private LayoutInflater inflater;
		private List<String> titles;
		private List<String> names;
		private List<String> updates;
		private List<String> publishes;

		ImagePagerAdapter(List<String> images,List<String> titles,List<String> names,List<String> updates,List<String> publishes) {
			this.images = images;
			this.titles = titles;
			this.names = names;
			this.updates = updates;
			this.publishes = publishes;
			inflater = LayoutInflater.from(ImageDetails.this);
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}

		@Override
		public int getCount() {
			return images.size();
		}

		@Override
		public Object instantiateItem(ViewGroup view, final int position) {
			final View imageLayout = inflater.inflate(R.layout.item_pager_image, view, false);
		final TouchImageView imageView = (TouchImageView) imageLayout.findViewById(R.id.image);
			final ProgressBar spinner = (ProgressBar) imageLayout.findViewById(R.id.loading);
			 TextView title = (TextView) imageLayout.findViewById(R.id.title);
			 TextView upate = (TextView) imageLayout.findViewById(R.id.upate);
			 TextView name = (TextView) imageLayout.findViewById(R.id.name);
			 TextView publish = (TextView) imageLayout.findViewById(R.id.publish);
	
			 title.append(titles.get(position));
			 upate.append(updates.get(position));
			 name.append(names.get(position));
			 publish.append(publishes.get(position));
			 
			//displaying image using UIL
			imageLoader.displayImage(images.get(position), imageView, options, new SimpleImageLoadingListener() {
				@Override
				public void onLoadingStarted(String imageUri, View view) {
				spinner.setVisibility(View.VISIBLE);
				}
				@Override
				public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
				String message = null;
				switch (failReason.getType()) {
				case IO_ERROR:
				message = "Input/Output error";
				break;
				case DECODING_ERROR:
				message = "Image can't be decoded";
				break;
				case NETWORK_DENIED:
				message = "Downloads are denied";
				break;
				case OUT_OF_MEMORY:
				message = "Out Of Memory error";
				break;
				case UNKNOWN:
				message = "Unknown error";
				break;
				}
				Toast.makeText(ImageDetails.this, message, Toast.LENGTH_SHORT).show();
				spinner.setVisibility(View.GONE);
				}
				@Override
				public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
				
					spinner.setVisibility(View.GONE);
				
				}
				});
			view.addView(imageLayout, 0);
				return imageLayout;
				
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view.equals(object);
		}

		@Override
		public void restoreState(Parcelable state, ClassLoader loader) {
		}

		@Override
		public Parcelable saveState() {
			return null;
		}

	}
}