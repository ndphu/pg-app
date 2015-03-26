package ndp.pjoker.pg;

import garin.artemiy.sqlitesimple.library.SQLiteSimple;
import ndp.pjoker.pg.model.CachedImage;
import android.app.Application;

public class MainApplication extends Application {
	private final static int DATABASE_VERSION = 1;
	public static boolean isLandscape;

	@Override
	public void onCreate() {
		super.onCreate();
		SQLiteSimple databaseSimple = new SQLiteSimple(this, DATABASE_VERSION);
		databaseSimple.create(CachedImage.class);
	}
}
