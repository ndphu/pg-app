package ndp.pjoker.pg.fragment;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import ndp.pjoker.pg.MainActivity;
import ndp.pjoker.pg.R;
import ndp.pjoker.pg.adapter.AlbumAdapter;
import ndp.pjoker.pg.model.Album;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

public class HomeFragment extends Fragment implements OnScrollListener, OnItemClickListener {
	private static final String TAG = HomeFragment.class.getSimpleName();
	private static final int PAGE_SIZE = 15;
	private GridView mGrid;
	private AlbumAdapter mAdapter;
	private boolean mIsLoading;
	private int mStartIdx;
	private boolean mEnd = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mAdapter = new AlbumAdapter(getActivity());
		mStartIdx = 0;
		mEnd = false;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_home, container, false);
		mGrid = (GridView) v.findViewById(R.id.fragment_home_gv_grid);
		mGrid.setAdapter(mAdapter);
		mGrid.setOnScrollListener(this);
		mGrid.setOnItemClickListener(this);
		return v;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		Log.i(TAG, "Home Fragment loaded");
		new LoadHomeTask().execute();
	}

	private class LoadHomeTask extends AsyncTask<Void, Void, List<Album>> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			Log.i(TAG, "Load home task begin!");
		}

		@Override
		protected List<Album> doInBackground(Void... params) {
			List<Album> albums = new ArrayList<Album>();
			try {
				JSONArray arr = new JSONArray(IOUtils.toString(URI.create("http://ndphu.pythonanywhere.com/pg/home/?size=" + PAGE_SIZE + "&start="
						+ mStartIdx)));
				for (int i = 0; i < arr.length(); ++i) {
					albums.add(new Album(arr.getJSONObject(i)));
				}
				mStartIdx += PAGE_SIZE;
				if (arr.length() < PAGE_SIZE) {
					mEnd = true;
				}

			} catch (JSONException | IOException e) {
				e.printStackTrace();
			}
			return albums;
		}

		@Override
		protected void onPostExecute(List<Album> result) {
			super.onPostExecute(result);
			mAdapter.addAll(result);
			mIsLoading = false;
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		if (totalItemCount > 0 && firstVisibleItem + visibleItemCount >= totalItemCount) {
			if (!mIsLoading && !mEnd) {
				Log.i(TAG, "Loading when scrolling to end");
				mIsLoading = true;
				new LoadHomeTask().execute();
			}
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {

	}

	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position, long arg3) {
		Album album = mAdapter.getItem(position);
		Intent intent = new Intent(getActivity(), MainActivity.class);
		intent.putExtra(MainActivity.EXTRA_ALBUM_ID, album.getId());
		startActivity(intent);
	}
}
