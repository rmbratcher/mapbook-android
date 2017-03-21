/*
 *  Copyright 2017 Esri
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  *
 *  * For additional information, contact:
 *  * Environmental Systems Research Institute, Inc.
 *  * Attn: Contracts Dept
 *  * 380 New York Street
 *  * Redlands, California, USA 92373
 *  *
 *  * email: contracts@esri.com
 *  *
 *
 */

package com.esri.android.mapbook.mapbook;

import android.util.Log;
import com.esri.android.mapbook.data.DataManagerCallbacks;
import com.esri.android.mapbook.data.FileManager;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Item;
import com.esri.arcgisruntime.mapping.MobileMapPackage;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MapbookPresenter implements MapbookContract.Presenter {

  private final FileManager mFileManager;

  private final MapbookContract.View mView;

  private final String TAG = MapbookPresenter.class.getSimpleName();

  @Inject
  MapbookPresenter (FileManager manager, MapbookContract.View view) {
    mFileManager = manager;
    mView = view;
  }

  /**
   * Method injection is used here to safely reference {@code this} after the object is created.
   * For more information, see Java Concurrency in Practice.
   */
  @Inject
  void setupListeners() {
    mView.setPresenter(this);
  }

  @Override public void start() {
    checkForMapbook();

  }

  /**
   * Check that the mapbook file exists on the device
   */
  @Override public void checkForMapbook() {

    if (mFileManager.fileExists()){

      loadMapbook(new DataManagerCallbacks.MapbookCallback() {
        @Override public void onMapbookLoaded(MobileMapPackage mobileMapPackage) {
          List<ArcGISMap> maps = mobileMapPackage.getMaps();
          mView.setMaps(maps);
          Item item = mobileMapPackage.getItem();
          mView.populateMapbookLayout(item);

          // Get the file size and date
          long mapbookSize = mFileManager.getSize();
          long mapbookModified = mFileManager.getModifiedDate();

          mView.setMapbookMetatdata(mapbookSize, mapbookModified, maps.size());

          final byte[] thumbnailData = item.getThumbnailData();
          if (thumbnailData != null && thumbnailData.length > 0) {
            mView.setThumbnailBitmap(thumbnailData);
          }else{
            final ListenableFuture<byte[]> futureThumbnail = item.fetchThumbnailAsync();
            futureThumbnail.addDoneListener(new Runnable() {
              @Override public void run() {
                try {
                  final byte[] itemThumbnailData = futureThumbnail.get();
                  mView.setThumbnailBitmap(itemThumbnailData);
                } catch (InterruptedException e) {
                  e.printStackTrace();
                } catch (ExecutionException e) {
                  e.printStackTrace();
                }
              }
            });
          }
        }

        @Override public void onMapbookNotLoaded(Throwable error) {
          Log.e(TAG, "Problem loading map book " + error.getMessage());
          mView.showMapbookNotFound();
          mView.showMessage("There was a problem loading the mapbook");
        }
      });

    }else{
      // Mapbook file isn't found, try downloading it...
      mView.downloadMapbook(mFileManager.createMobileMapPackageFilePath());
    }
  }

  /**
   * Load the mobile map package
   */
  @Override public void loadMapbook(final DataManagerCallbacks.MapbookCallback callback) {
    String mmpkPath = mFileManager.createMobileMapPackageFilePath();

    final MobileMapPackage mmp = new MobileMapPackage(mmpkPath);

    mmp.addDoneLoadingListener(new Runnable() {
      @Override public void run() {
        if (mmp.getLoadStatus() == LoadStatus.LOADED) {
          List<ArcGISMap> maps = mmp.getMaps();
          callback.onMapbookLoaded(mmp);

        }else{
          callback.onMapbookNotLoaded(mmp.getLoadError());
        }
      }
    });
    mmp.loadAsync();
  }
}
