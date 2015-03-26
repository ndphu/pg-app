package ndp.pjoker.pg.dao;

import java.util.List;

import ndp.pjoker.pg.model.CachedImage;
import android.content.Context;

/**
 * @author ndphu
 *
 */
public class DaoUtils {
	private static CachedImageDao cachedImageDao;

	public static void initialize(Context context) {
		cachedImageDao = new CachedImageDao(context);
	}

	/**
	 * Check if the book is added to db or not
	 *
	 * @param id
	 * @return
	 */
	private static boolean isValidId(Long id) {
		return id != null && id > 0;
	}

	public static void saveOrUpdate(CachedImage cachedImage) {
		if (isValidId(cachedImage.getId())) {
			// update
			cachedImageDao.update(cachedImage.getId(), cachedImage);
		} else {
			// save
			cachedImageDao.create(cachedImage);
		}
	}

	public static CachedImage getCachedImageByUrl(String url) {
		List<CachedImage> cachedList = cachedImageDao.readAllWhere(CachedImage.COL_URL, url);
		if (cachedList.size() == 0) {
			return null;
		} else {
			return cachedList.get(0);
		}

	}

	public static CachedImage getCachedImageByHasedUrl(String hasedUrl) {
		List<CachedImage> cachedList = cachedImageDao.readAllWhere(CachedImage.COL_HASED_URL, hasedUrl);
		if (cachedList.size() == 0) {
			return null;
		} else {
			return cachedList.get(0);
		}
	}

}
