package com.agestic.flickr;

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.agestic.flickr.beans.PhotoBean;
import com.agestic.flickr.utils.AgesticContants;
import com.agestic.flickr.utils.AgesticFunctions;
import com.agestic.flickr.xml.sax.SAXXMLHandler;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class MainActivity extends Activity {

	private List<String> imageUrls;
	private List<String> imageTitles;
	private List<String> imageNames;
	private List<String> updated;
	private List<String> published;
	private TextView noConnectTextView;
	private DisplayImageOptions options;
	private ImageAdapter imageAdapter;
	private GridView gridView;
	private TextView results;
	private DownloadPreviewImagesTask downloadPreviewImagesTask;
	private int page = 0;
	int count = 0;
	protected ImageLoader imageLoader = ImageLoader.getInstance();
	public static String IMAGE_URL = "image_url";
	public static String IMAGE_TITLE = "image_title";
	public static String IMAGE_NAME = "image_name";
	public static String UPDATED = "updated";
	public static String PUBLISHED= "published";
	public static String IMAGE_POSITION = "image_position";

	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		noConnectTextView = (TextView) findViewById(R.id.no_connect_message);

		results = (TextView) findViewById(R.id.tresults);

		// check for Internet status
		if (AgesticFunctions.isWorkingInternetPersent(this)) {
			imageLoader.init(ImageLoaderConfiguration.createDefault(this));
			imageUrls = new ArrayList<String>();
			imageTitles = new ArrayList<String>();
			imageNames = new ArrayList<String>();
			updated = new ArrayList<String>();
			published = new ArrayList<String>();

			// initializing gridview
			initGridView();

			// executing async task for getting data
			downloadPreviewImagesTask = getDownloadPreviewImagesTask(
					R.id.preview_img_loading, AgesticContants.URL);
			if (downloadPreviewImagesTask != null
					&& downloadPreviewImagesTask.getStatus() == AsyncTask.Status.RUNNING) {
				downloadPreviewImagesTask.attachActivity(this);
				downloadPreviewImagesTask.setSpinnerVisible();

			} else {
				Log.d("downloadPreviewImagesTask",
						"downloadPreviewImagesTask has executed already");
			}
		} else {

			showAlertDialog(MainActivity.this, "No Internet Connection",
					"You don't have internet connection!", false);
		}

	}

	@SuppressWarnings("deprecation")
	public void showAlertDialog(Context context, String title, String message,
			Boolean status) {
		AlertDialog alertDialog = new AlertDialog.Builder(context).create();

		// Setting Dialog Title
		alertDialog.setTitle(title);

		// Setting Dialog Message
		alertDialog.setMessage(message);

		// Setting OK Button
		alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		});

		// Showing Alert Message
		alertDialog.show();
	}

	

	private DownloadPreviewImagesTask getDownloadPreviewImagesTask(
			int spinnerID, String url) {
		downloadPreviewImagesTask = new DownloadPreviewImagesTask();
		downloadPreviewImagesTask.setSpinner(spinnerID);
		downloadPreviewImagesTask.attachActivity(this);
		downloadPreviewImagesTask.execute(url); // show spinner
		return downloadPreviewImagesTask;
	}

	class EndlessListListener implements OnScrollListener {

		private int visibleThreshold = 5; // setting visible threshold for
											// loading new data
		private int previousTotal = 0;
		private boolean loading = true;

		public void onScroll(AbsListView view, int firstVisible,
				int visibleCount, int totalCount) {

			if (loading) {
				if (totalCount > previousTotal) {
					loading = false;
					previousTotal = totalCount;
				}
			}

			if (!loading
					&& (totalCount - visibleCount) <= (firstVisible + visibleThreshold)) {

				Log.d("loadData", "new data loaded");
				
				
				if (downloadPreviewImagesTask != null
						&& downloadPreviewImagesTask.getStatus() == AsyncTask.Status.RUNNING) {
					Log.d("downloadPreviewImagesTask",
							"downloadPreviewImagesTask is running");
				} else if (page < (count / 21)) // checking if there is more
												// data
				{
					downloadPreviewImagesTask = getDownloadPreviewImagesTask(
							R.id.new_photos_loading, AgesticContants.URL);
					
					page = page + 1;
				}
				loading = true;
			}

		}

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {

		}

	}

	// Async Task for downloading new data
	class DownloadPreviewImagesTask extends
			AsyncTask<String, Void, ArrayList<HashMap<String, String>>> {

		private Exception exc = null;
		private ProgressBar spinner;
		private MainActivity imageGridActivity = null;
		private int spinnerId;

		public AsyncTask<String, Void, ArrayList<HashMap<String, String>>> setSpinner(
				int spinnerId) {
			this.spinnerId = spinnerId;
			return this;
		}

		public void attachActivity(MainActivity imageGridActivity) {
			this.imageGridActivity = imageGridActivity;
		}

		public void detachActivity() {
			this.imageGridActivity = null;
		}

		public ProgressBar getSpinner() {
			return spinner;
		}

		@Override
		protected ArrayList<HashMap<String, String>> doInBackground(
				String... params) {
			ArrayList<HashMap<String, String>> data = new ArrayList<HashMap<String, String>>();

			try {

				/** Handling XML */
				SAXParserFactory spf = SAXParserFactory.newInstance();
				SAXParser sp = spf.newSAXParser();
				XMLReader xr = sp.getXMLReader();

				/** Send URL to parse XML Tags */
				URL sourceUrl = new URL(AgesticContants.URL);

				/** Create handler to handle XML Tags ( extends DefaultHandler ) */
				SAXXMLHandler myXMLHandler = new SAXXMLHandler();
				xr.setContentHandler(myXMLHandler);
				xr.parse(new InputSource(sourceUrl.openStream()));
				List<PhotoBean> parsedBean = myXMLHandler.getPhotoBeans();
				
				for (int i = 0; i < parsedBean.size(); i++) {

					// creating new HashMap
					HashMap<String, String> jpgs = new HashMap<String, String>();

					// adding each child node to HashMap key =>
					// value
					jpgs.put(IMAGE_URL, parsedBean.get(i).getIcon());
					jpgs.put(IMAGE_TITLE, parsedBean.get(i).getTitle());
					jpgs.put(IMAGE_NAME, parsedBean.get(i).getName());
					jpgs.put(UPDATED, parsedBean.get(i).getUpdated());
					jpgs.put(PUBLISHED,parsedBean.get(i).getPublished());

					// adding HashList to ArrayList

					data.add(jpgs);
				}
				count=data.size();

			} catch (Exception e) {
				System.out.println("XML Pasing Excpetion = " + e);
				e.printStackTrace();
			}
			return data;
			
			
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			setSpinnerVisible();
		}

		public void setSpinnerVisible() {
			spinner = (ProgressBar) imageGridActivity.findViewById(spinnerId);
			spinner.setVisibility(View.VISIBLE);
		}

		@Override
		protected void onPostExecute(ArrayList<HashMap<String, String>> images) {
			super.onPostExecute(images);
			spinner.setVisibility(View.GONE);
			if (exc != null) {
				imageGridActivity.showError(exc);
			} else if (images != null) {
				imageGridActivity.initOriginalAndPreviewUrls(images);

				if (gridView == null && imageAdapter == null) {
					initGridView();
				} else
					imageGridActivity.getImageAdapter().notifyDataSetChanged();
			} else {
				results.setText("Sorry! No Results Found!");
			}
		}
	}

	public ImageAdapter getImageAdapter() {
		return imageAdapter;
	}

	private void showError(Exception exc) {
		noConnectTextView.setText(exc.getMessage());
		noConnectTextView.setVisibility(View.VISIBLE);
	}

	private void initOriginalAndPreviewUrls(
			ArrayList<HashMap<String, String>> images) {

		for (int i = 0; i <= images.size() - 1; i++) {

			String image = images.get(i).get(IMAGE_URL)
					.toString();
			String title = images.get(i).get(IMAGE_TITLE)
					.toString();
			String name = images.get(i).get(IMAGE_NAME).toString();
			String up = images.get(i).get(UPDATED).toString();
			String pub = images.get(i).get(PUBLISHED).toString();
			imageUrls.add(image);
			imageTitles.add(title);
			imageNames.add(name);
			updated.add(up);
			published.add(pub);
		}

	}

	private void startImageGalleryActivity(int position) {
		Intent intent = new Intent(this, ImageDetails.class);
		intent.putExtra(IMAGE_URL, (Serializable) imageUrls);
		intent.putExtra(IMAGE_TITLE,
				(Serializable) imageTitles);
		intent.putExtra(IMAGE_NAME, (Serializable) imageNames);
		intent.putExtra(UPDATED, (Serializable) updated);
		intent.putExtra(IMAGE_POSITION, position);
		intent.putExtra(PUBLISHED, (Serializable) published);

		startActivity(intent);
	}

	private void initGridView() {
		gridView = (GridView) findViewById(R.id.gridview);
		imageAdapter = new ImageAdapter();
		gridView.setAdapter(imageAdapter);
		gridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				startImageGalleryActivity(position);
			}
		});
		gridView.setOnScrollListener(new EndlessListListener());
		options = new DisplayImageOptions.Builder()
				.showImageOnLoading(R.drawable.stub_image)
				.showImageForEmptyUri(R.drawable.image_for_empty_url)
				.showImageOnFail(R.drawable.image_for_empty_url)
				.cacheInMemory(true).cacheOnDisk(true).considerExifParams(true)
				.bitmapConfig(Bitmap.Config.RGB_565).build();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	public class ImageAdapter extends BaseAdapter {
		@Override
		public int getCount() {
			return imageUrls.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final ImageView imageView;
			if (convertView == null) {
				imageView = (ImageView) getLayoutInflater().inflate(
						R.layout.item_grid_image, parent, false);
			} else {
				imageView = (ImageView) convertView;
			}

			imageLoader.displayImage(imageUrls.get(position), imageView,
					options);

			return imageView;
		}
	}

}
