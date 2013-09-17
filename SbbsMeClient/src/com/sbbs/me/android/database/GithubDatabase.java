package com.sbbs.me.android.database;

import java.io.File;

import com.sbbs.me.android.consts.PathDefine;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class GithubDatabase {

	private SQLiteDatabase database;
	private static String databasePath = "";

	private static final String TABLE_GITHUB_CACHE = "github_cache";
	private static final String SQL_GITHUB_CACHE = "create table github_cache (sha text primary key, parent_sha text not null, path text not null, type text not null, repo text not null)";

	public static boolean isDatabaseFileExists() {
		return new File(databasePath).exists();
	}

	public GithubDatabase(Context context) throws Exception {
		databasePath = "/data/data/"+context.getPackageName()+"/databases/";
		if (!new File(databasePath).exists()) {
			new File(databasePath).mkdirs();
		}
		databasePath += PathDefine.GITHUB_DATA_NAME;
		Log.e("GithubDatabase", databasePath);
		if (!isDatabaseFileExists()) {
			database = SQLiteDatabase.openOrCreateDatabase(
					databasePath, null);
			database.execSQL(SQL_GITHUB_CACHE);
		} else {
			database = SQLiteDatabase.openDatabase(
					databasePath, null,
					SQLiteDatabase.OPEN_READWRITE);
		}
	}

	public void closeDatabase() {
		if (database != null) {
			database.close();
		}
	}

	public Cursor queryGithubCache(String selection, String[] selectionArgs) {
		Cursor c = null;
		if (database != null) {
			c = database.query(TABLE_GITHUB_CACHE, null, selection,
					selectionArgs, null, null, null);
		}
		return c;
	}

	public void insertOrUpdateGithubCache(ContentValues cv) {
		if (database != null) {
			String sha = cv.getAsString("sha");
			String parentSha = cv.getAsString("parent_sha");
			if (!isGithubCacheExists(sha, parentSha)) {
				database.insert(TABLE_GITHUB_CACHE, null, cv);
			} else {
				database.update(TABLE_GITHUB_CACHE, cv,
						"sha=? and parent_sha=?",
						new String[] { sha, parentSha });
			}
		}
	}

	private boolean isGithubCacheExists(String sha, String parentSha) {
		boolean ret = false;
		if (database != null) {
			Cursor c = database.query(TABLE_GITHUB_CACHE, null,
					"sha=? and parent_sha=?", new String[] { sha, parentSha },
					null, null, null);
			if (c != null) {
				c.moveToFirst();
				while (!c.isAfterLast()) {
					ret = true;
					break;
				}
				c.close();
			}
		}
		return ret;
	}
}
