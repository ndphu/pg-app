package ndp.pjoker.pg.adapter;

import ndp.pjoker.pg.R;
import ndp.pjoker.pg.model.Album;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class AlbumAdapter extends ArrayAdapter<Album> {

	private LayoutInflater mInflater;

	public AlbumAdapter(Context context) {
		super(context, 0);
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.gridview_item_model, parent, false);
			ViewHolder vh = new ViewHolder();
			vh.cover = (ImageView) convertView.findViewById(R.id.gridview_item_model_iv_cover);
			vh.name = (TextView) convertView.findViewById(R.id.gridview_item_model_tv_name);
			convertView.setTag(vh);
		}

		ViewHolder vh = (ViewHolder) convertView.getTag();
		Album m = getItem(position);
		vh.name.setText(m.getName());
		Picasso.with(getContext()).load(Uri.parse(m.getCover())).error(R.drawable.ic_launcher).into(vh.cover);
		return convertView;
	}

	private static class ViewHolder {
		ImageView cover;
		TextView name;
	}

}
