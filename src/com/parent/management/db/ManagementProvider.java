package com.parent.management.db;

import java.io.File;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDiskIOException;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Environment;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

import com.parent.management.ManagementApplication;

public class ManagementProvider extends ContentProvider {
	
	/**
	 * ParentManagement authority for content URIs
	 */
	public static final String AUTHORITY = "com.parent.provider.management";
	public static final String DATABASE_NAME = "MANAGEMENT";
	public static final int DATABASE_VERSION = 1;
	public static final String TAG = ManagementApplication.getApplicationTag()
	        + "." + ManagementProvider.class.getSimpleName();
	public static final String EXTERNAL_STORAGE_PATH = Environment.getExternalStorageDirectory() + "/" + ".management";
	
	private static final UriMatcher sUriMatcher;
    private static final int MATCHER_RESET = 1;
    private static final String RESET_PATH = ".reset";
    public static final int IS_SENT_NO = 0;
    public static final int IS_SENT_YES = 1;

	/**
	 * BrowserHistory wrapper class for content provider
	 */
	public static final class BrowserHistory implements BaseColumns {
        public static final String TABLE_NAME = "BrowserHistory";
    	public static final String PATH = "browserhistory";
    	private static final int MATCHER      = 100;
        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + AUTHORITY + "/" + PATH);
        public static final String ID = "Id";
        public static final String URL = "URL";
        public static final String TITLE = "Title";
        public static final String VISIT_COUNT = "VisitCount";
	    public static final String LAST_VISIT = "Lastvisit";
		public static final String IS_SENT = "IsSend";
		
		/**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = ID + " DESC";
	}
	
	/**
	 * GpsInfo wrapper class for content provider
	 */
	public static final class Gps implements BaseColumns {
        public static final String TABLE_NAME = "Gps";
        public static final String PATH = "gps";
        private static final int MATCHER      = 101;
        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + AUTHORITY + "/" + PATH);
        public static final String LATIDUDE = "Latitude";
        public static final String LONGITUDE = "Longitude";
        public static final String SPEED = "Speed";
        public static final String TIME = "Time";
        public static final String IS_SENT = "IsSend";
        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = _ID + " DESC";
	}
	
	/**
	 * InstalledApps wrapper class for content provider
	 */
	public static final class AppsInstalled implements BaseColumns {
        public static final String TABLE_NAME = "AppsInstalled";
        public static final String PATH = "appsinstalled";
        private static final int MATCHER      = 102;
        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + AUTHORITY + "/" + PATH);
        
        public static final String APP_NAME = "AppName";
        public static final String PACKAGE_NAME = "PackageName";
        public static final String URL = "Url";
        public static final String VERSION_CODE = "VersionCode";
        public static final String VERSION_NAME = "VersionName";
        public static final String IS_SENT = "IsSend";
        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = _ID + " DESC";
	}
	
	/**
	 * AppsUsedInfo wrapper class for content provider
	 */
	public static final class AppsUsed implements BaseColumns {
        public static final String TABLE_NAME = "AppsUsed";
	
	}
	
	/**
	 * Contacts wrapper class for content provider
	 */
	public static final class Contacts implements BaseColumns {
	
	}

	/**
	 * CallLog wrapper class for content provider
	 */
	public static final class CallLog implements BaseColumns {
	
	}

	private static class ManagementDatabaseHelper extends SQLiteOpenHelper {
		// Set to false to fall back to internal db
		private final static boolean EXTERNAL_DB = true;

		private SQLiteDatabase mDatabase = null;
		private boolean mIsInitializing = false;

		ManagementDatabaseHelper(final Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
			if (! EXTERNAL_DB) {
				return;
			}
		}

		private static final String INTEGER = " INTEGER ";
		private static final String TEXT = " TEXT ";
		private static final String COMMA = ",";

		@Override
		public void onCreate(final SQLiteDatabase db) {

			try {
			    createBrowserHistoryTable(db);
			    createGpsTable(db);
			    createAppsInstalledTable(db);
			} catch (SQLException sqle) {
				Log.e(TAG, "unable to create Message content provider : "
						+ sqle.getMessage());
				throw sqle;
			}
		}

        private void createBrowserHistoryTable(final SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + BrowserHistory.TABLE_NAME + " ("
                    + BrowserHistory.ID + INTEGER + "PRIMARY KEY,"
                    + BrowserHistory.URL + TEXT + COMMA
                    + BrowserHistory.TITLE + TEXT + COMMA
                    + BrowserHistory.VISIT_COUNT + INTEGER + COMMA 
                    + BrowserHistory.LAST_VISIT + INTEGER + COMMA                
                    + BrowserHistory.IS_SENT + INTEGER
                    + ");");
        }
        
        private void createGpsTable(final SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + Gps.TABLE_NAME + " ("
                    + Gps._ID + INTEGER + "PRIMARY KEY,"
                    + Gps.LATIDUDE  + INTEGER + COMMA 
                    + Gps.LONGITUDE + TEXT + COMMA 
                    + Gps.SPEED + TEXT + COMMA 
                    + Gps.TIME + INTEGER + COMMA 
                    + Gps.IS_SENT + INTEGER
                    + ");");
        }

        private void createAppsInstalledTable(final SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + AppsInstalled.TABLE_NAME + " ("
                    + AppsInstalled._ID + INTEGER + "PRIMARY KEY,"
                    + AppsInstalled.APP_NAME + TEXT + COMMA 
                    + AppsInstalled.PACKAGE_NAME + TEXT + COMMA 
                    + AppsInstalled.URL + TEXT + COMMA 
                    + AppsInstalled.VERSION_CODE + TEXT + COMMA 
                    + AppsInstalled.VERSION_NAME + TEXT + COMMA 
                    + AppsInstalled.IS_SENT + INTEGER
                    + ");");
        }

        private void clearDB(final SQLiteDatabase db) {
            db.execSQL("DROP TABLE IF EXISTS " + BrowserHistory.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + Gps.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + AppsInstalled.TABLE_NAME);
        }
        
        @Override
        public void onUpgrade(final SQLiteDatabase db, final int oldVersion,
                final int newVersion) {
//            if (newVersion < oldVersion) {
//                // DOWNGRADE:
//                Log.w(TAG, "Downgrading database from version " + oldVersion
//                    + "to version " + newVersion);
//                // lets drop all
//                clearDB(db);
//                // and recreate everything
//                onCreate(db);
//            } else if (oldVersion < newVersion) {
//                // UPGRADE:
//                Log.w(TAG, "Upgrading database from version " + oldVersion
//                    + "to version " + newVersion);
//                // Database version 4+: added greeting active status (on upgrade, keep messages)
//                Log.w(TAG, "Upgrading to DBv4+, re-creating greeting table");
//                // drop greetings table
//                db.execSQL("DROP TABLE IF EXISTS " + GREETINGS_TABLE_NAME);
//                // create new greetings table
//                createGreetingsTable(db);
//                
//             // Database version 5+: added media library database
//                Log.w(TAG, "Upgrading to DBv5+, creating media library table");
//                db.execSQL("DROP TABLE IF EXISTS " + MEDIALIB_TABLE_NAME);
//                createMediaLibTable(db);
//            }
        	
            // lets drop all
            clearDB(db);
            // and recreate everything
            onCreate(db);

        }
        
		synchronized void reset() {
			Log.i(TAG, "OpenHelper: reset");
			mDatabase = null;
			mIsInitializing = false;
		}
		/*
		 * (non-Javadoc)
		 * 
		 * @see android.database.sqlite.SQLiteOpenHelper#getReadableDatabase()
		 */
		@Override
		public synchronized SQLiteDatabase getReadableDatabase() {
			if (! EXTERNAL_DB) {
				return super.getReadableDatabase();
			}

			final String mount = Environment.getExternalStorageState();
			if (ManagementApplication.DEBUG) {
				Log.d(TAG, 
						"getReadableDatabase, checking external storage: " + mount);
			}
			if (!Environment.MEDIA_MOUNTED.equals(mount)) {
				// lost our storage, reset everything
				reset();
				return null;
			}

			if (mDatabase != null && mDatabase.isOpen()) {
				return mDatabase; // The database is already open for business
			}

			if (mIsInitializing) {
				throw new IllegalStateException("getReadableDatabase called recursively");
			}

			try {
				return getWritableDatabase();
			} catch (SQLiteException e) {
				Log.e(TAG, "Couldn't open " + " for writing (will try read-only):", e);
			}

			SQLiteDatabase db = null;
			try {
				mIsInitializing = true;
				// Create external storage path if needed
				final File target = new File(ManagementProvider.EXTERNAL_STORAGE_PATH);
				target.mkdirs();
				db = SQLiteDatabase.openDatabase(getDatabaseName(), null, SQLiteDatabase.OPEN_READONLY);
				if (db.getVersion() != DATABASE_VERSION) {
					Log.e(TAG, "Can't upgrade read-only database from version " + db.getVersion() + " to "
							+ DATABASE_VERSION + ": " + getDatabaseName());
					return null;
				}
				onOpen(db);
				Log.w(TAG, "Opened " + getDatabaseName() + " in read-only mode");
				mDatabase = db;
			} catch (SQLiteException sqle) {
				reset();
			} finally {
				mIsInitializing = false;
			}
			if (db != null && ! db.equals(mDatabase)) {
				db.close();
			}
			return mDatabase;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.database.sqlite.SQLiteOpenHelper#getWritableDatabase()
		 */
		@Override
		public synchronized SQLiteDatabase getWritableDatabase() {
			if (! EXTERNAL_DB) {
				return super.getWritableDatabase();
			}

			final String mount = Environment.getExternalStorageState();
			if (ManagementApplication.DEBUG) {
				Log.d(TAG, "getWritableDatabase, checking external storage: " + mount);
			}
			if (!Environment.MEDIA_MOUNTED.equals(mount)) {
				// lost our storage, reset everything
				reset();
				return null;
			}

			if (mDatabase != null && mDatabase.isOpen() && !mDatabase.isReadOnly()) {
				return mDatabase; // The database is already open for business
			}

			if (mIsInitializing) {
				throw new IllegalStateException("getWritableDatabase called recursively");
			}

			// If we have a read-only database open, someone could be using it
			// (though they shouldn't), which would cause a lock to be held on
			// the file, and our attempts to open the database read-write would
			// fail waiting for the file lock. To prevent that, we acquire the
			// lock on the read-only database, which shuts out other users.

			boolean success = false;
			SQLiteDatabase db = null;
			try {
				mIsInitializing = true;
				// Create external storage path if needed
				final File target = new File(ManagementProvider.EXTERNAL_STORAGE_PATH);
				boolean rtn = target.mkdirs();
				db = SQLiteDatabase.openOrCreateDatabase(ManagementProvider.getStoragePath() + "/" + DATABASE_NAME +".sqlite", null);
				final int version = db.getVersion();
				if (version != DATABASE_VERSION) {
					db.beginTransaction();
					try {
						if (version == 0) {
							onCreate(db);
						} else {
							onUpgrade(db, version, DATABASE_VERSION);
						}
						db.setVersion(DATABASE_VERSION);
						db.setTransactionSuccessful();
					} finally {
						db.endTransaction();
					}
				}

				onOpen(db);
				success = true;
				
				return db;
			} finally {
				mIsInitializing = false;
				if (success) {
					if (mDatabase != null) {
						mDatabase.close();
					}
					mDatabase = db;
				} else {
					if (db != null) {
						db.close();
					}
				}
			}
		}
	}
	
	private ManagementDatabaseHelper mOpenHelper;

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
        final String tableName;
        switch (sUriMatcher.match(uri)) {
        case BrowserHistory.MATCHER:
            tableName = BrowserHistory.TABLE_NAME;
            break;
        default: 
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        try {
            final int count = mOpenHelper.getWritableDatabase().delete(
                    tableName, selection, selectionArgs);
            Log.i(TAG, "deleted " + count + " entries in " + tableName +" database");
            getContext().getContentResolver().notifyChange(uri, null);
            return count;
        } catch (NullPointerException npe) {
            // error, probably the external storage is not mounted
            Log.e(TAG, "NullPointerException while trying to delete entries in " + tableName +" database");
            return 0;
        } catch (SQLiteException sqlioe) {
            //we catch disk io that may happen if SD card full or faulty
            Log.e(TAG, "SQLiteException  while trying to delete entry in " + tableName +" database");
            // arm flag indicating that the application cannot write the db
            return 0;
        }
	}

	public static String getStoragePath() {
		// TODO Select Internal or external storage 
//		if("internal".equals(
//				ManagementApplication.getServiceConfiguration().getParameter("storage_type"))) {
//			// Use internal storage
//			return ManagementApplication.getInternalPath();
//		} else {
//			return EXTERNAL_STORAGE_PATH;
//		}
	    return EXTERNAL_STORAGE_PATH;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(final Uri uri, final ContentValues values) {
		switch (sUriMatcher.match(uri)){ // NOPMD
		case BrowserHistory.MATCHER:
			return insertInBrowserHistory(values);
        case Gps.MATCHER:
            return insertInGps(values);
        case AppsInstalled.MATCHER:
            return insertInAppsInstalled(values);
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		} 
	}

	private Uri insertInBrowserHistory(final ContentValues initialValues){
		ContentValues values;
		if (null == initialValues  || null == mOpenHelper ) {
			return null;
		} else {
			values = new ContentValues(initialValues);
		}

        if (!values.containsKey(BrowserHistory.IS_SENT)) {
            values.put(BrowserHistory.IS_SENT, IS_SENT_NO); 
        }

		try {
			final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
			final long rowId = db.insert(BrowserHistory.TABLE_NAME, null, values);
			if (rowId > 0) {
				final Uri insertUri = ContentUris.withAppendedId(BrowserHistory.CONTENT_URI, rowId);
				getContext().getContentResolver().notifyChange(insertUri, null);
				return insertUri;
			}
		} catch (NullPointerException npe) {
			//we catch npe that may happen if the sd card is not present
			Log.e(TAG, "NullPointerException while trying to insert entry in " + BrowserHistory.TABLE_NAME +" database");
        } catch (SQLiteException sqlioe) {
            //we catch disk io that may happen if SD card full or faulty
            Log.e(TAG, "SQLiteException  while trying to insert entry in " + BrowserHistory.TABLE_NAME +" database");
            // arm flag indicating that the application cannot write the db
        }
		return null;
	}

    private Uri insertInGps(final ContentValues initialValues){
        ContentValues values;
        if (null == initialValues  || null == mOpenHelper ) {
            return null;
        } else {
            values = new ContentValues(initialValues);
        }

        if (!values.containsKey(Gps.IS_SENT)) {
            values.put(Gps.IS_SENT, IS_SENT_NO); 
        }

        try {
            final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
            final long rowId = db.insert(Gps.TABLE_NAME, null, values);
            if (rowId > 0) {
                final Uri insertUri = ContentUris.withAppendedId(Gps.CONTENT_URI, rowId);
                getContext().getContentResolver().notifyChange(insertUri, null);
                return insertUri;
            }
        } catch (NullPointerException npe) {
            //we catch npe that may happen if the sd card is not present
            Log.e(TAG, "NullPointerException while trying to insert entry in " + Gps.TABLE_NAME +" database");
        } catch (SQLiteException sqlioe) {
            //we catch disk io that may happen if SD card full or faulty
            Log.e(TAG, "SQLiteException  while trying to insert entry in " + Gps.TABLE_NAME +" database");
            // arm flag indicating that the application cannot write the db
        }
        return null;
    }	


    private Uri insertInAppsInstalled(final ContentValues initialValues){
        ContentValues values;
        if (null == initialValues  || null == mOpenHelper ) {
            return null;
        } else {
            values = new ContentValues(initialValues);
        }

        if (!values.containsKey(AppsInstalled.URL)) {
            values.put(AppsInstalled.URL, ""); 
        }
        if (!values.containsKey(AppsInstalled.IS_SENT)) {
            values.put(AppsInstalled.IS_SENT, IS_SENT_NO); 
        }

        try {
            final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
            final long rowId = db.insert(Gps.TABLE_NAME, null, values);
            if (rowId > 0) {
                final Uri insertUri = ContentUris.withAppendedId(AppsInstalled.CONTENT_URI, rowId);
                getContext().getContentResolver().notifyChange(insertUri, null);
                return insertUri;
            }
        } catch (NullPointerException npe) {
            //we catch npe that may happen if the sd card is not present
            Log.e(TAG, "NullPointerException while trying to insert entry in " + AppsInstalled.TABLE_NAME +" database");
        } catch (SQLiteException sqlioe) {
            //we catch disk io that may happen if SD card full or faulty
            Log.e(TAG, "SQLiteException  while trying to insert entry in " + AppsInstalled.TABLE_NAME +" database");
            // arm flag indicating that the application cannot write the db
        }
        return null;
    }       
	
	@Override
	public boolean onCreate() {
		try {
			mOpenHelper = new ManagementDatabaseHelper(getContext());
		} catch (SQLiteDiskIOException e) {
			return false;
		}
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
        final SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        final String tableName;
        String orderBy;       
        switch (sUriMatcher.match(uri)) {
        case BrowserHistory.MATCHER:
            tableName = BrowserHistory.TABLE_NAME;
            orderBy = BrowserHistory.DEFAULT_SORT_ORDER;
            break;
        case Gps.MATCHER:
            tableName = Gps.TABLE_NAME;
            orderBy = Gps.DEFAULT_SORT_ORDER;
            break;
        case AppsInstalled.MATCHER:
            tableName = AppsInstalled.TABLE_NAME;
            orderBy = AppsInstalled.DEFAULT_SORT_ORDER;
            break;
        case MATCHER_RESET:
            // special uri used to invalidate and reset the provider when
            // the external storage gets unavailable
            if (null != mOpenHelper) {
                mOpenHelper.reset();
            }
            return null;
        default:
            return null;
        }
        qb.setTables(tableName);

        if (!TextUtils.isEmpty(sortOrder)) {
            orderBy = sortOrder;
        }
        // Get the database and run the query
        final Cursor queryResult;
        try {
            final SQLiteDatabase db = this.mOpenHelper.getReadableDatabase();
            queryResult = qb.query(db, projection, selection,
                    selectionArgs, null, null, orderBy);
            // Tell the cursor what uri to watch, so it knows when its source data
            // changes
            queryResult.setNotificationUri(getContext().getContentResolver(), uri);
            return queryResult;
        } catch (NullPointerException npe) {
            //this may happen if SD card is mounted
            Log.w(TAG, "NullPointerException while trying to query " + tableName +" database");
            return null;
        } catch (SQLiteException sqlioe) {
            //we catch disk io that may happen if SD card full or faulty
            Log.e(TAG, "SQLiteException  while trying to query " + tableName +" database");
            // arm flag indicating that the application cannot write the db
//            ManagementApplication.readOnlyMode(true);
            return null;
        }
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
        final String tableName;
        switch (sUriMatcher.match(uri)) {
        case BrowserHistory.MATCHER:
            tableName = BrowserHistory.TABLE_NAME;
            break;
        case Gps.MATCHER:
            tableName = Gps.TABLE_NAME;
            break;
        case AppsInstalled.MATCHER:
            tableName = AppsInstalled.TABLE_NAME;
            break;
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        try {
            final SQLiteDatabase db = this.mOpenHelper.getWritableDatabase();
            final int count = db.update(
                    tableName, values, selection, selectionArgs);
            getContext().getContentResolver().notifyChange(uri, null);
            // We were able to modify the database, so we may be in read/write
            // mode
//            tryRestoreWriteMode();
            return count;
        } catch (NullPointerException npe) {
            Log.w(TAG, "NullPointerException while trying to update " + tableName +" database");
            return 0;
        } catch (SQLiteException sqlioe) {
            //we catch disk io that may happen if SD card full or faulty
            Log.e(TAG, "SQLiteException  while trying to update " + tableName +" database" + sqlioe.getMessage());
            // arm flag indicating that the application cannot write the db
            return 0;
        }
	}

	// initialize URI matcher
	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(ManagementProvider.AUTHORITY, BrowserHistory.PATH, BrowserHistory.MATCHER);
        sUriMatcher.addURI(ManagementProvider.AUTHORITY, Gps.PATH, Gps.MATCHER);
        sUriMatcher.addURI(ManagementProvider.AUTHORITY, AppsInstalled.PATH, AppsInstalled.MATCHER);
		sUriMatcher.addURI(ManagementProvider.AUTHORITY, RESET_PATH, MATCHER_RESET);
	}
}