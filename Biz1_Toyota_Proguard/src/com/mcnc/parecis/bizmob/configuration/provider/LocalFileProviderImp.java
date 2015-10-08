package com.mcnc.parecis.bizmob.configuration.provider;

import java.io.File;
import java.io.FileNotFoundException;

import com.mcnc.hsmart.core.log.Logger;
import com.mcnc.hsmart.util.FileUtil;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

public class LocalFileProviderImp extends ContentProvider {

	// FIXME NEW_PROJECT 프로바이더 Name(template) 변경
	public static String URI_PREFIX = "/data/data/com.mcnc.parecis.toyota/"; 
	public static String URI_LOCALFILE = "provider.load.localfile.toyota";

	public static String constructUri(String url) {
		Uri uri = Uri.parse(url);
		return uri.isAbsolute() ? url : URI_PREFIX + url;
	}

	@Override
	public ParcelFileDescriptor openFile(Uri uri, String mode)
			throws FileNotFoundException {
		String filePath = constructUri(uri.getPath());
		File file = new File(filePath);
		ParcelFileDescriptor parcel = ParcelFileDescriptor.open(file,
				ParcelFileDescriptor.MODE_READ_ONLY);
		return parcel;
	}

	@Override
	public boolean onCreate() {
		return true;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		String mime_type = FileUtil.getMimetypeString (uri.toString());
		if ( FileUtil.getExt(uri.toString()).toLowerCase().equals("hwp")) {
			mime_type = "application/hwp";
		}
		return mime_type;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		return null;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		return 0;
	}

	/*
	@Override
	public int delete(Uri uri, String s, String[] as) {
		throw new UnsupportedOperationException(
				"Not supported by this provider");
	}

	@Override
	public String getType(Uri uri) {
		throw new UnsupportedOperationException(
				"Not supported by this provider");
	}

	@Override
	public Uri insert(Uri uri, ContentValues contentvalues) {
		throw new UnsupportedOperationException(
				"Not supported by this provider");
	}

	@Override
	public Cursor query(Uri uri, String[] as, String s, String[] as1, String s1) {
		throw new UnsupportedOperationException(
				"Not supported by this provider");
	}

	@Override
	public int update(Uri uri, ContentValues contentvalues, String s,
			String[] as) {
		throw new UnsupportedOperationException(
				"Not supported by this provider");
	}*/
}