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

package com.esri.android.mapbook.map;

import com.esri.android.mapbook.BasePresenter;
import com.esri.android.mapbook.BaseView;
import com.esri.android.mapbook.data.DataManagerCallbacks;
import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.LayerList;
import com.esri.arcgisruntime.tasks.geocode.SuggestResult;

import java.util.List;

public interface MapContract {
  interface Presenter extends BasePresenter {
    void geoCodeAddress(String address);
    void getSuggestions(Geometry geometry, String query);
    boolean hasLocatorTask();
    void loadMap(String path, int mapIndex);
    void queryForFeatures(Geometry geometry, LayerList layers);

  }
  interface View extends BaseView<Presenter>{
    void showMap(ArcGISMap map);
    void displaySearchResult(Point resultpoint, String address, boolean zoomOut);
    void displayBookmarks();
    void setUpMap();
    void showSuggestedPlaceNames(List<SuggestResult> suggestResultList);
    void showMessage(String message);
    void getSuggestions(Geometry geometry, String query);
  }
}