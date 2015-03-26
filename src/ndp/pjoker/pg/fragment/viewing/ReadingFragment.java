package ndp.pjoker.pg.fragment.viewing;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import ndp.pjoker.pg.MainActivity;
import ndp.pjoker.pg.R;
import ndp.pjoker.pg.dao.DaoUtils;
import ndp.pjoker.pg.model.CachedImage;
import ndp.pjoker.pg.runnable.DownloadFileRunnable;
import ndp.pjoker.pg.runnable.DownloadFileRunnable.DownloadFileListener;
import ndp.pjoker.pg.task.ImageLoaderTask;
import ndp.pjoker.pg.taskmanager.TaskManager;
import ndp.pjoker.pg.utils.Utils;
import ndp.pjoker.pg.view.customview.TouchImageView;
import ndp.pjoker.pg.view.customview.VerticalViewPager;

import org.apache.commons.io.IOUtils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;

public class ReadingFragment extends Fragment {
	private static final String TAG = ReadingFragment.class.getSimpleName();
	static final float MIN_SCALE = 0.7f;

	private ProgressDialog mPd;
	private PagesPagerAdapter mAdapter;

	private ViewPager mViewPager;
	private VerticalViewPager mVerticalViewPager;

	private int mSwipeMode;
	private long mAlbumId;
	private List<String> mPics;

	public void setAlbumId(long albumId) {
		mAlbumId = albumId;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (mAlbumId < 1) {
			return null;
		}
		mSwipeMode = MainActivity.SWIPE_MODE_VERTICAL;
		View view = inflater.inflate(R.layout.fragment_reading, container, false);
		switch (mSwipeMode) {
		case MainActivity.SWIPE_MODE_VERTICAL:
			mVerticalViewPager = (VerticalViewPager) view.findViewById(R.id.fragment_reading_vertical_viewpager_page_list);
			mVerticalViewPager.setPageTransformer(false, new VerticalTransformer());
			mVerticalViewPager.setVisibility(View.VISIBLE);
			break;
		case MainActivity.SWIPE_MODE_HORIZONTAL:
			mViewPager = (ViewPager) view.findViewById(R.id.fragment_reading_viewpager_page_list);
			mViewPager.setPageTransformer(false, new HorizontalTransformer());
			mViewPager.setVisibility(View.VISIBLE);
			break;
		}
		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		((ActionBarActivity) getActivity()).getSupportActionBar().hide();
		getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		new AsyncTask<Void, Void, Void>() {
			@Override
			protected void onPreExecute() {
				mPd = new ProgressDialog(getActivity());
				mPd.setCancelable(false);
				mPd.setMessage("Loading...");
				mPd.show();
			};

			@Override
			protected Void doInBackground(Void... params) {
				try {
					mPics = new ArrayList<String>();
					String pagesStr = IOUtils.toString(URI.create("http://ndphu.pythonanywhere.com/pg/album/?id=" + mAlbumId));
					for (String pageUrl : pagesStr.split(";")) {
						mPics.add(pageUrl);
						String md5Hash = Utils.getMD5Hash(pageUrl);
						CachedImage cachedImage = DaoUtils.getCachedImageByHasedUrl(md5Hash);
						if (cachedImage == null) {
							cachedImage = new CachedImage();
							cachedImage.setFilePath(null);
							cachedImage.setHasedUrl(md5Hash);
							cachedImage.setUrl(pageUrl);
							DaoUtils.saveOrUpdate(cachedImage);
						}
						if (cachedImage.getFilePath() == null) {
							DownloadFileRunnable dfr = new DownloadFileRunnable(pageUrl, getActivity().getExternalCacheDir().getAbsolutePath() + "/" + md5Hash);
							dfr.setDownloadFileListener(new DownloadFileListener() {

								@Override
								public void onCompleted(final String url, final String destination, final long fileSize) {
									final String hasedUrl = Utils.getMD5Hash(url);
									CachedImage cachedImage = DaoUtils.getCachedImageByHasedUrl(hasedUrl);
									if (cachedImage == null) {
										cachedImage = new CachedImage();
									}
									cachedImage.setFilePath(destination);
									cachedImage.setFileSize(fileSize);
									DaoUtils.saveOrUpdate(cachedImage);
									if (getActivity() != null) {
										getActivity().runOnUiThread(new Runnable() {

											@Override
											public void run() {
												TouchImageView viewToBeUpdated = (TouchImageView) (mVerticalViewPager != null ? mVerticalViewPager : mViewPager)
														.findViewWithTag(hasedUrl);
												new ImageLoaderTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, destination, hasedUrl,
														new WeakReference<TouchImageView>(viewToBeUpdated));
												if (mAdapter != null) {
													mAdapter.notifyDataSetChanged();
												}
											}
										});
									}
								}

								@Override
								public void onFailed(String url, String destination, Exception ex) {

								}
							});
							TaskManager.getInstance().downloadFile(dfr);
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				mAdapter = new PagesPagerAdapter();
				switch (mSwipeMode) {
				case MainActivity.SWIPE_MODE_VERTICAL:
					mVerticalViewPager.setAdapter(mAdapter);
					break;
				case MainActivity.SWIPE_MODE_HORIZONTAL:
					mViewPager.setAdapter(mAdapter);
					break;
				}
				mPd.cancel();
			};

		}.execute();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		((MainActivity) activity).disableDrawer();
	}

	@Override
	public void onDetach() {
		super.onDetach();
		((MainActivity) getActivity()).enableDrawer();
	}

	@Override
	public void onDestroyView() {
		super.onDestroy();
		((ActionBarActivity) getActivity()).getSupportActionBar().show();
		getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}

	private class PagesPagerAdapter extends PagerAdapter {

		@Override
		public int getCount() {
			return mPics.size();
		}

		@Override
		public View instantiateItem(ViewGroup container, int position) {
			String url = mPics.get(position);
			String hasedUrl = Utils.getMD5Hash(url);
			final TouchImageView img = new TouchImageView(container.getContext());
			img.setImageResource(R.drawable.ic_launcher);
			img.setScaleType(ScaleType.CENTER_INSIDE);
			img.setTag(hasedUrl);
			CachedImage cachedFile = getCachedImage(hasedUrl);
			if (cachedFile != null && cachedFile.getFilePath() != null) {
				Log.e(TAG, "Cache hit!!!");
				new ImageLoaderTask().execute(cachedFile.getFilePath(), hasedUrl, new WeakReference<TouchImageView>(img));
			} else {

			}
			container.addView(img, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
			return img;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}
	};

	public CachedImage getCachedImage(String key) {
		return DaoUtils.getCachedImageByHasedUrl(key);
	}

}
