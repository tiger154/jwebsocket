//	---------------------------------------------------------------------------
//	jWebSocket - ImageThreadLoader
//	Copyright (c) 2011 Innotrade GmbH, jWebSocket.org
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU Lesser General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//	more details.
//	You should have received a copy of the GNU Lesser General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//	---------------------------------------------------------------------------
package org.jwebsocket.android.demo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.Log;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.Thread.State;
import java.lang.ref.SoftReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This is an object that can load images from a URL on a thread.
 * taken from http://ballardhack.wordpress.com/2010/04/10/loading-images-over-http-on-a-separate-thread-on-android/
 *
 * @author Prashant
 */
public class ImageThreadLoader {

	private static final String TAG = "ImageThreadLoader";
	// Global cache of images.
	// Using SoftReference to allow garbage collector to clean cache if needed
	private final HashMap<String, SoftReference<Bitmap>> mCache =
			new HashMap<String, SoftReference<Bitmap>>();

	private final class QueueItem {

		public URL url;
		public ImageLoadedListener listener;
	}
	private final ArrayList<QueueItem> mQueue = new ArrayList<QueueItem>();
	private final Handler mHandler = new Handler();	// Assumes that this is started from the main (UI) thread
	private Thread mThread;
	private QueueRunner mRunner = new QueueRunner();

	;

	/** Creates a new instance of the ImageThreadLoader */
	public ImageThreadLoader() {
		mThread = new Thread(mRunner);
	}

	/**
	 * Defines an interface for a callback that will handle
	 * responses from the thread loader when an image is done
	 * being loaded.
	 */
	public interface ImageLoadedListener {

		public void imageLoaded(Bitmap imageBitmap);
	}

	/**
	 * Provides a Runnable class to handle loading
	 * the image from the URL and settings the
	 * ImageView on the UI thread.
	 */
	private class QueueRunner implements Runnable {

		@Override
		public void run() {
			synchronized (this) {
				while (mQueue.size() > 0) {
					final QueueItem lItem = mQueue.remove(0);

					// If in the cache, return that copy and be done
					if (mCache.containsKey(lItem.url.toString())
							&& mCache.get(lItem.url.toString()) != null) {
						// Use a handler to get back onto the UI thread for the update
						mHandler.post(new Runnable() {

							@Override
							public void run() {
								if (lItem.listener != null) {
									// NB: There's a potential race condition here where the cache item could get
									//     garbage collected between when we post the runnable and it's executed.
									//     Ideally we would re-run the network load or something.
									SoftReference<Bitmap> ref = mCache.get(lItem.url.toString());
									if (ref != null) {
										lItem.listener.imageLoaded(ref.get());
									}
								}
							}
						});
					} else {
						final Bitmap bmp = readBitmapFromNetwork(lItem.url);
						if (bmp != null) {
							mCache.put(lItem.url.toString(), new SoftReference<Bitmap>(bmp));

							// Use a handler to get back onto the UI thread for the update
							mHandler.post(new Runnable() {

								public void run() {
									if (lItem.listener != null) {
										lItem.listener.imageLoaded(bmp);
									}
								}
							});
						}

					}

				}
			}
		}
	}

	/**
	 * Queues up a URI to load an image from for a given image view.
	 *
	 * @param aURI	The URI source of the image
	 * @param callback	The listener class to call when the image is loaded
	 * @throws MalformedURLException If the provided uri cannot be parsed
	 * @return A Bitmap image if the image is in the cache, else null.
	 */
	public Bitmap loadImage(final String aURI, final ImageLoadedListener aListener)
			throws MalformedURLException {
		// If it's in the cache, just get it and quit it
		if (mCache.containsKey(aURI)) {
			SoftReference<Bitmap> lRef = mCache.get(aURI);
			if (lRef != null) {
				return lRef.get();
			}
		}

		QueueItem lItem = new QueueItem();
		lItem.url = new URL(aURI);
		lItem.listener = aListener;
		mQueue.add(lItem);

		// start the thread if needed
		if (mThread.getState() == State.NEW) {
			mThread.start();
		} else if (mThread.getState() == State.TERMINATED) {
			mThread = new Thread(mRunner);
			mThread.start();
		}
		return null;
	}

	/**
	 * Convenience method to retrieve a bitmap image from
	 * a URL over the network. The built-in methods do
	 * not seem to work, as they return a FileNotFound
	 * exception.
	 *
	 * Note that this does not perform any threading --
	 * it blocks the call while retrieving the data.
	 *
	 * @param aURL The URL to read the bitmap from.
	 * @return A Bitmap image or null if an error occurs.
	 */
	public static Bitmap readBitmapFromNetwork(URL aURL) {
		InputStream lIS = null;
		BufferedInputStream lBIS = null;
		Bitmap lBmp = null;
		try {
			URLConnection conn = aURL.openConnection();
			conn.connect();
			lIS = conn.getInputStream();
			lBIS = new BufferedInputStream(lIS);
			lBmp = BitmapFactory.decodeStream(lBIS);
		} catch (MalformedURLException e) {
			Log.e(TAG, "Bad ad URL", e);
		} catch (IOException e) {
			Log.e(TAG, "Could not get remote ad image", e);
		} finally {
			try {
				if (lIS != null) {
					lIS.close();
				}
				if (lBIS != null) {
					lBIS.close();
				}
			} catch (IOException e) {
				Log.w(TAG, "Error closing stream.");
			}
		}
		return lBmp;
	}
}
