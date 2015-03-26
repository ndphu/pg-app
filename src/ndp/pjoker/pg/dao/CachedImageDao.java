package ndp.pjoker.pg.dao;

import garin.artemiy.sqlitesimple.library.SQLiteSimpleDAO;
import ndp.pjoker.pg.model.CachedImage;
import android.content.Context;

public class CachedImageDao extends SQLiteSimpleDAO<CachedImage> {
	public CachedImageDao(Context context) {
		super(CachedImage.class, context);
	}
}
