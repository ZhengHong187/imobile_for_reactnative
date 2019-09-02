/**
 *
 */
package com.supermap.interfaces.mapping;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Dynamic;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.ReadableType;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.uimanager.events.NativeGestureUtil;
import com.supermap.RNUtils.FileUtil;
import com.supermap.analyst.TopologyProcessing;
import com.supermap.analyst.TopologyProcessingOptions;
import com.supermap.analyst.networkanalyst.NetworkBuilder;
import com.supermap.analyst.networkanalyst.NetworkSplitMode;
import com.supermap.component.MapWrapView;
import com.supermap.containts.EventConst;
import com.supermap.data.*;
import com.supermap.data.Enum;
import com.supermap.data.Maps;
import com.supermap.data.Point;
import com.supermap.data.Point2D;
import com.supermap.data.Point2Ds;
import com.supermap.data.Rectangle2D;
import com.supermap.data.PrjCoordSys;
import com.supermap.data.PrjCoordSysType;
import com.supermap.data.Resources;
import com.supermap.data.Workspace;
import com.supermap.interfaces.utils.SMFileUtil;
import com.supermap.interfaces.utils.POISearchHelper2D;
import com.supermap.interfaces.utils.ScaleViewHelper;
import com.supermap.map3D.toolKit.PoiGsonBean;
import com.supermap.mapping.Action;
import com.supermap.mapping.CalloutAlignment;
import com.supermap.mapping.ColorLegendItem;
import com.supermap.mapping.EditHistoryType;
import com.supermap.mapping.GeometryAddedListener;
import com.supermap.mapping.GeometryEvent;
import com.supermap.mapping.GeometrySelectedEvent;
import com.supermap.mapping.GeometrySelectedListener;
import com.supermap.mapping.Layer;
import com.supermap.mapping.LayerSettingVector;
import com.supermap.mapping.Layers;
import com.supermap.mapping.LayerGroup;
import com.supermap.mapping.Legend;
import com.supermap.mapping.LegendContentChangeListener;
import com.supermap.mapping.LegendItem;
import com.supermap.mapping.LegendView;
import com.supermap.mapping.MapColorMode;
import com.supermap.mapping.MapControl;
import com.supermap.mapping.MapParameterChangedListener;
import com.supermap.mapping.MeasureListener;
import com.supermap.mapping.ScaleView;
import com.supermap.mapping.Selection;
import com.supermap.mapping.ThemeGridRange;
import com.supermap.mapping.ThemeRange;
import com.supermap.mapping.ThemeType;
import com.supermap.mapping.ThemeUnique;
import com.supermap.mapping.collector.Collector;
import com.supermap.navi.NaviInfo;
import com.supermap.navi.NaviListener;
import com.supermap.navi.Navigation2;
import com.supermap.navi.Navigation3;
import com.supermap.onlineservices.CoordinateType;
import com.supermap.onlineservices.NavigationOnline;
import com.supermap.onlineservices.NavigationOnlineData;
import com.supermap.onlineservices.NavigationOnlineParameter;
import com.supermap.onlineservices.PathInfo;
import com.supermap.onlineservices.RouteType;
import com.supermap.plot.AnimationAttribute;
import com.supermap.plot.AnimationBlink;
import com.supermap.plot.AnimationDefine;
import com.supermap.plot.AnimationGO;
import com.supermap.plot.AnimationGroup;
import com.supermap.plot.AnimationGrow;
import com.supermap.plot.AnimationManager;
import com.supermap.plot.AnimationRotate;
import com.supermap.plot.AnimationScale;
import com.supermap.plot.AnimationShow;
import com.supermap.plot.AnimationWay;
import com.supermap.plot.GeoGraphicObject;
import com.supermap.plot.GraphicObjectType;
import com.supermap.plugin.LocationManagePlugin;
import com.supermap.rnsupermap.R;
import com.supermap.smNative.SMMapRender;
import com.supermap.smNative.collector.SMCollector;
import com.supermap.smNative.SMLayer;
import com.supermap.smNative.SMMapWC;
import com.supermap.smNative.SMSymbol;
import com.supermap.data.Color;
import com.supermap.smNative.components.InfoCallout;


import org.apache.http.cookie.SM;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import static com.supermap.interfaces.utils.SMFileUtil.copyFiles;
import static com.supermap.RNUtils.FileUtil.homeDirectory;

public class SMap extends ReactContextBaseJavaModule implements LegendContentChangeListener {
    public static final String REACT_CLASS = "SMap";
    private static SMap sMap;
    private static ReactApplicationContext context;
    private static MeasureListener mMeasureListener;
    private GestureDetector mGestureDetector;
    private GeometrySelectedListener mGeometrySelectedListener;
    private ScaleViewHelper scaleViewHelper;
    private static Boolean hasBigCallout = false;
    private static final int curLocationTag = 118081;
    public static int fillNum;
    public static Color[] fillColors;
    public static Random random;// 用于保存产生随机的线风格颜色的Random对象
    private Point2D navistart, naviend;
    private InfoCallout m_callout;
    private Datasource IndoorDatasource;
    private String IncrementRoadName;
    Point2Ds GpsPoint2Ds = new Point2Ds();
    String rootPath = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();


    private ScaleViewHelper getScaleViewHelper() {
        if (scaleViewHelper == null) {
            MapControl mapControl = SMap.getInstance().smMapWC.getMapControl();
            scaleViewHelper = new ScaleViewHelper(context, mapControl);
        }
        if (scaleViewHelper.mapParameterChangedListener == null) {
            scaleViewHelper.addScaleChangeListener(new MapParameterChangedListener() {
                public void scaleChanged(double newScale) {
                    if (scaleViewHelper == null)
                        return;
                    scaleViewHelper.mScaleLevel = scaleViewHelper.getScaleLevel();
                    scaleViewHelper.mScaleText = scaleViewHelper.getScaleText(scaleViewHelper.mScaleLevel);
                    scaleViewHelper.mScaleWidth = scaleViewHelper.getScaleWidth(scaleViewHelper.mScaleLevel);
                    WritableMap map = Arguments.createMap();
                    map.putDouble("width", scaleViewHelper.mScaleWidth);
                    map.putString("title", scaleViewHelper.mScaleText);
                    context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                            .emit(EventConst.SCALEVIEW_CHANGE, map);
                }

                public void boundsChanged(Point2D newMapCenter) {
                }

                public void angleChanged(double newAngle) {
                }

                public void sizeChanged(int width, int height) {
                }
            });
        }
        return scaleViewHelper;
    }

    public Selection getSelection() {
        return selection;
    }

    public void setSelection(Selection selection) {
        this.selection = selection;
    }

    private Selection selection;

    public SMMapWC getSmMapWC() {
        return smMapWC;
    }

    public void setSmMapWC(SMMapWC smMapWC) {
        this.smMapWC = smMapWC;
    }

    private SMMapWC smMapWC;

    private Point2D defaultMapCenter = null;

    public SMap(ReactApplicationContext context) {
        super(context);
        this.context = context;
        SMCollector.openGPS(context);
    }

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    public static SMap getInstance() {
        if (sMap == null) {
            sMap = new SMap(context);
        }
        if (sMap.smMapWC == null) {
            sMap.smMapWC = new SMMapWC();
        }
        setWorkspace(null);
        return sMap;
    }

    public static SMap getInstance(ReactApplicationContext context) {
        if (sMap == null) {
            sMap = new SMap(context);
        }
        if (sMap.smMapWC == null) {
            sMap.smMapWC = new SMMapWC();
        }
        setWorkspace(null);
        return sMap;
    }

    public static void setInstance(MapControl mapControl) {
        sMap = getInstance();
        sMap.smMapWC.setMapControl(mapControl);
        setWorkspace(null);
        if (sMap.smMapWC.getMapControl().getMap() != null && sMap.smMapWC.getMapControl().getMap().getWorkspace() == null) {
            sMap.smMapWC.getMapControl().getMap().setWorkspace(sMap.smMapWC.getWorkspace());
        }
    }

    public static void setWorkspace(Workspace workspace) {
        if (sMap.smMapWC.getWorkspace() == null) {
            if (workspace == null) {
                Workspace _workspace = new Workspace();
                sMap.smMapWC.setWorkspace(_workspace);
            } else {
                sMap.smMapWC.setWorkspace(workspace);
            }
        }
    }

    public static SMMapWC getSMWorkspace() {
        return getInstance().smMapWC;
    }

    public Activity getActivity() {
        return getCurrentActivity();
    }

    public String getPackageName() {
        return context.getPackageName();
    }

    public String getNativeLibraryDir() {
        return context.getApplicationInfo().nativeLibraryDir;
    }

    public AssetManager getAssets() {
        return context.getAssets();
    }


    //判断坐标系Type是否相等，避免不支持的type转Enum抛异常
    public static boolean safeGetType(PrjCoordSys coordSys1, PrjCoordSys coordSys2) {
        try {
            if (coordSys1.getType() == coordSys2.getType()) {
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    //判断坐标系Type是否相等，避免不支持的type转Enum抛异常
    public static boolean safeGetType(PrjCoordSys coordSys1, PrjCoordSysType prjCoordSysType) {
        try {
            if (coordSys1.getType() == prjCoordSysType) {
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取许可文件状态
     *
     * @param promise
     */
    @ReactMethod
    public void getEnvironmentStatus(Promise promise) {
        try {
            LicenseStatus status = Environment.getLicenseStatus();
            WritableMap statusMap = Arguments.createMap();
            statusMap.putBoolean("isActivated", status.isActivated());
            statusMap.putBoolean("isLicenseValid", status.isLicenseValid());
            statusMap.putBoolean("isLicenseExist", status.isLicenseExsit());
            statusMap.putBoolean("isTrailLicense", status.isTrailLicense());
            statusMap.putString("startDate", status.getStartDate().toString());
            statusMap.putString("expireDate", status.getExpireDate().toString());
            statusMap.putString("version", status.getVersion() + "");
            promise.resolve(statusMap);
        } catch (Exception e) {
            promise.reject(e);
        }
    }


    private void showMarkerHelper(Point2D pt, int tag) {
        final Point2D mapPt = pt;//new Point2D(11584575.605042318,3573118.555091877);
        final String tagStr = tag + "";
        getCurrentActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                    final  Point2D mapPt = new Point2D(11584575.605042318,3573118.555091877);
                GeoPoint point = new GeoPoint(mapPt.getX(), mapPt.getY());
                GeoStyle style = new GeoStyle();
                style.setMarkerSymbolID(118081);
                style.setMarkerSize(new Size2D(6, 6));
                style.setLineColor(new Color(255, 0, 0, 255));
                point.setStyle(style);

                sMap.smMapWC.getMapControl().getMap().getTrackingLayer().add(point, tagStr);

//                sMap.smMapWC.getMapControl().getMap().getMapView().getContext();
//                CallOut callout = new CallOut(sMap.smMapWC.getMapControl().getMap().getMapView().getContext());
//                callout.setLocation(mapPt.getX(), mapPt.getY());
//                sMap.smMapWC.getMapControl().getMap().getMapView().addCallout(callout,tagStr);
//                sMap.smMapWC.getMapControl().getMap().getMapView().showCallOut();
                sMap.smMapWC.getMapControl().getMap().setCenter(mapPt);
                if (sMap.smMapWC.getMapControl().getMap().getScale() < 0.000011947150294723098)
                    sMap.smMapWC.getMapControl().getMap().setScale(0.000011947150294723098);
                sMap.smMapWC.getMapControl().getMap().refresh();
            }
        });
    }

    /**
     * 添加marker
     *
     * @param longitude
     * @param latitude
     * @param promise
     */
    @ReactMethod
    public void showMarker(double longitude, double latitude, int tag, Promise promise) {
        try {
            sMap = getInstance();
            sMap.smMapWC.getMapControl().getMap().refresh();

            Point2D pt = new Point2D(longitude, latitude);
            if (!safeGetType(sMap.smMapWC.getMapControl().getMap().getPrjCoordSys(), PrjCoordSysType.PCS_EARTH_LONGITUDE_LATITUDE)) {
                Point2Ds point2Ds = new Point2Ds();
                point2Ds.add(pt);
                PrjCoordSys prjCoordSys = new PrjCoordSys();
                prjCoordSys.setType(PrjCoordSysType.PCS_EARTH_LONGITUDE_LATITUDE);
                CoordSysTransParameter parameter = new CoordSysTransParameter();

                CoordSysTranslator.convert(point2Ds, prjCoordSys, sMap.smMapWC.getMapControl().getMap().getPrjCoordSys(), parameter, CoordSysTransMethod.MTH_GEOCENTRIC_TRANSLATION);
                pt = point2Ds.getItem(0);
                showMarkerHelper(pt, tag);
            }

            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    private void deleteMarkerHelper(int tag) {
        final String tagStr = tag + "";
        getCurrentActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //  sMap.smMapWC.getMapControl().getMap().getMapView().removeCallOut(tagStr);
                int n = sMap.smMapWC.getMapControl().getMap().getTrackingLayer().indexOf(tagStr);
                if (n != -1) {
                    sMap.smMapWC.getMapControl().getMap().getTrackingLayer().remove(n);
                    sMap.smMapWC.getMapControl().getMap().refresh();
                }
            }
        });
    }

    /**
     * 移除marker
     *
     * @param promise
     */
    @ReactMethod
    public void deleteMarker(int tag, Promise promise) {
        try {
            deleteMarkerHelper(tag);
            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }


    /**
     * 刷新地图
     *
     * @param promise
     */
    @ReactMethod
    public void refreshMap(Promise promise) {
        try {
            sMap = getInstance();
            sMap.smMapWC.getMapControl().getMap().refresh();

            getCurrentActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((MapWrapView) sMap.smMapWC.getMapControl().getMap().getMapView()).requestLayout();
                }
            });

            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 打开工作空间
     *
     * @param data
     * @param promise
     */
    @ReactMethod
    public void openWorkspace(ReadableMap data, Promise promise) {
        try {
            sMap = getInstance();
            Map params = data.toHashMap();
            boolean result = sMap.smMapWC.openWorkspace(params);
            if (result) {
                if (sMap.getSmMapWC().getMapControl() != null && sMap.getSmMapWC().getMapControl().getMap() != null && !sMap.getSmMapWC().getMapControl().getMap().getName().equals("")) {
//                    sMap.getSmMapWC().getMapControl().getMap().close();
//                    sMap.getSmMapWC().getMapControl().getMap().setWorkspace(sMap.getSmMapWC().getWorkspace());

                    sMap.getSmMapWC().getMapControl().getMap().setVisibleScalesEnabled(false);
                    // sMap.getSmMapWC().getMapControl().setMagnifierEnabled(true);
                    sMap.getSmMapWC().getMapControl().getMap().setAntialias(true);
                    sMap.getSmMapWC().getMapControl().getMap().refresh();
                }
            }

            promise.resolve(result);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 仅用于判断在线数据是否可请求到数据
     *
     * @param data
     */
    @ReactMethod
    public void isDatasourceOpen(ReadableMap data, Promise promise) {
        try {
            sMap = getInstance();
            Map params = data.toHashMap();
            Datasource datasource = sMap.smMapWC.openDatasource(params);
            if (datasource != null) {
                promise.resolve(true);
            } else {
                promise.resolve(false);
            }
        } catch (Exception e) {
            promise.reject(e);
        }
    }


    /**
     * 判断当前数据源别名是否可用，返回可用别名
     * @param alias
     * @param promise
     */
    @ReactMethod
    public void isAvilableAlias(String alias, Promise promise){
        try {
            sMap = SMap.getInstance();
            Datasources datasources = sMap.smMapWC.getWorkspace().getDatasources();
            int index = 1;
            while (datasources.indexOf(alias) != -1){
                alias += "_" + index;
                index++;
            }
            promise.resolve(alias);
        }catch (Exception e){
            promise.reject(e);
        }
    }
    /**
     * 以数据源形式打开工作空间setLayerFieldInfo
     *
     * @param data
     * @param defaultIndex 默认显示Map 图层索引
     * @param promise
     */
    @ReactMethod
    public void openDatasourceWithIndex(ReadableMap data, int defaultIndex, boolean toHead, boolean visable, Promise promise) {
        try {
            sMap = getInstance();
            Map params = data.toHashMap();
            Datasource datasource = sMap.smMapWC.openDatasource(params);
            sMap.smMapWC.getMapControl().getMap().setWorkspace(sMap.smMapWC.getWorkspace());

            if (datasource != null && defaultIndex >= 0 && datasource.getDatasets().getCount() > 0) {
                Dataset ds = datasource.getDatasets().get(defaultIndex);
                com.supermap.mapping.Map map = sMap.smMapWC.getMapControl().getMap();
                map.setDynamicProjection(true);
                Layer layer = map.getLayers().add(ds, toHead);
                layer.setVisible(visable);
                if (ds.getType() == DatasetType.REGION) {
                    LayerSettingVector setting = (LayerSettingVector) layer.getAdditionalSetting();
                    setting.getStyle().setLineSymbolID(5);
                }
                if (ds.getType() == DatasetType.REGION || ds.getType() == DatasetType.REGION3D) {
                    LayerSettingVector setting = (LayerSettingVector) layer.getAdditionalSetting();
                    setting.getStyle().setFillForeColor(this.getFillColor());
                    setting.getStyle().setLineColor(this.getLineColor());
                } else if (ds.getType() == DatasetType.LINE || ds.getType() == DatasetType.NETWORK || ds.getType() == DatasetType.NETWORK3D
                        || ds.getType() == DatasetType.LINE3D) {
                    LayerSettingVector setting = (LayerSettingVector) layer.getAdditionalSetting();
                    setting.getStyle().setLineColor(this.getLineColor());
                    if (ds.getType() == DatasetType.NETWORK || ds.getType() == DatasetType.NETWORK3D) {
                        map.getLayers().add(((DatasetVector) ds).getChildDataset(), true);
                    }
                } else if (ds.getType() == DatasetType.POINT || ds.getType() == DatasetType.POINT3D) {
                    LayerSettingVector setting = (LayerSettingVector) layer.getAdditionalSetting();
                    setting.getStyle().setLineColor(this.getLineColor());
                }
            }
            getScaleViewHelper();
            sMap.smMapWC.getMapControl().getMap().setVisibleScalesEnabled(false);
            sMap.smMapWC.getMapControl().getMap().refresh();

            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 以数据源形式打开工作空间
     *
     * @param data
     * @param defaultName 默认显示Map 图层名称
     * @param promise
     */
    @ReactMethod
    public void openDatasourceWithName(ReadableMap data, String defaultName, boolean toHead, boolean visable, Promise promise) {
        try {
            sMap = getInstance();
            Map params = data.toHashMap();
            Datasource datasource = sMap.smMapWC.openDatasource(params);
            sMap.smMapWC.getMapControl().getMap().setWorkspace(sMap.smMapWC.getWorkspace());

            if (datasource != null && !defaultName.equals("")) {
                Dataset ds = datasource.getDatasets().get(defaultName);
                sMap.smMapWC.getMapControl().getMap().setDynamicProjection(true);
                Layer layer = sMap.smMapWC.getMapControl().getMap().getLayers().add(ds, toHead);
                layer.setVisible(visable);
            }
            getScaleViewHelper();
            sMap.smMapWC.getMapControl().getMap().refresh();

            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 不同于上次选用的填充颜色的颜色
     *
     * @return
     */
    public static Color getFillColor() {

        Color result = new Color(255, 192, 203);
        if (fillNum >= getFillColors().length) {
            fillNum = 0;
        }
        result = getFillColors()[fillNum];
        fillNum++;
        return result;

    }

    /**
     * 获取随机的用于线风格的颜色
     *
     * @return
     */
    public static Color getLineColor() {
        return getRandomLineColor();
    }

    /**
     * 产生随机的用于线风格的颜色 经过初步试验，新产生的线颜色，饱和度【0-240】最好在30-100之间 亮度【0-240】最好在75-120之间
     *
     * @return
     */
    private static Color getRandomLineColor() {
        Color result = new Color(255, 192, 203);
        try {
            if (random == null) {
                random = new Random();
            }
            result = new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255));
        } catch (Exception ex) {
        }
        return result;
    }

    private static Color[] getFillColors() {
        if (fillColors == null) {
            fillColors = new Color[10];
            fillColors[0] = new Color(224, 207, 226);
            fillColors[1] = new Color(151, 191, 242);
            fillColors[2] = new Color(242, 242, 186);
            fillColors[3] = new Color(190, 255, 232);
            fillColors[4] = new Color(255, 190, 232);
            fillColors[5] = new Color(255, 190, 190);
            fillColors[6] = new Color(255, 235, 175);
            fillColors[7] = new Color(233, 255, 190);
            fillColors[8] = new Color(234, 225, 168);
            fillColors[9] = new Color(174, 241, 176);
        }
        return fillColors;
    }

    /**
     * 根据名称关闭数据源，datasourceName为空则全部关闭
     *
     * @param datasourceName
     * @param promise
     */
    @ReactMethod
    public void closeDatasourceWithName(String datasourceName, Promise promise) {
        try {
            sMap = getInstance();
            Datasources datasources = sMap.smMapWC.getWorkspace().getDatasources();
            Boolean isClose = true;
            if (datasourceName.equals("")) {
                for (int i = 0; i < datasources.getCount(); i++) {
                    if (datasources.get(i) != null && datasources.get(i).isOpened()) {
                        isClose = datasources.close(i) && isClose;
                    }
                }
            } else {
                if (datasources.get(datasourceName) != null) {
                    isClose = datasources.close(datasourceName);
                }
            }

            promise.resolve(isClose);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 根据序号关闭数据源，index = -1 则全部关闭
     *
     * @param index
     * @param promise
     */
    @ReactMethod
    public void closeDatasourceWithIndex(int index, Promise promise) {
        try {
            sMap = getInstance();
            Datasources datasources = sMap.smMapWC.getWorkspace().getDatasources();
            Boolean isClose = true;
            if (index == -1) {
                for (int i = 0; i < datasources.getCount(); i++) {
                    if (datasources.get(i) != null && datasources.get(i).isOpened()) {
                        isClose = datasources.close(i) && isClose;
                    }
                }
            } else {
                if (datasources.get(index) != null) {
                    isClose = datasources.close(index);
                }
            }

            promise.resolve(isClose);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 工作空间是否被修改
     *
     * @param promise
     */
    @ReactMethod
    public void workspaceIsModified(Promise promise) {
        try {
            sMap = getInstance();
            boolean result = sMap.smMapWC.getWorkspace().isModified();
            promise.resolve(result);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 保存工作空间
     *
     * @param promise
     */
    @ReactMethod
    public void saveWorkspace(Promise promise) {
        try {
            sMap = getInstance();
            boolean result = sMap.smMapWC.saveWorkspace();
            promise.resolve(result);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 根据工作空间连接信息保存工作空间
     *
     * @param data
     * @param promise
     */
    @ReactMethod
    public void saveWorkspaceWithInfo(ReadableMap data, Promise promise) {
        try {
            sMap = getInstance();
            Map info = data.toHashMap();
            boolean result = sMap.smMapWC.saveWorkspaceWithInfo(info);
            promise.resolve(result);
        } catch (Exception e) {
            promise.reject(e);
        }
    }


    /**
     * 获取UDB中数据集名称
     *
     * @param path    UDB在内存中路径
     * @param promise
     */
    @ReactMethod
    public void getUDBName(String path, Promise promise) {
        try {
            File tempFile = new File(path.trim());
            String[] strings = tempFile.getName().split("\\.");
            String udbName = strings[0];
            Datasource datasource;
            Workspace workspace = null;
            sMap = getInstance();
            DatasourceConnectionInfo datasourceconnection = new DatasourceConnectionInfo();

//            if (sMap.smMapWC.getMapControl().getMap().getWorkspace().getDatasources().indexOf(udbName) != -1) {
//                sMap.smMapWC.getMapControl().getMap().getWorkspace().getDatasources().close(udbName);
//            }
            if (sMap.smMapWC.getMapControl() == null) {
                workspace = new Workspace();
                datasourceconnection.setEngineType(EngineType.UDB);
                datasourceconnection.setServer(path);
                datasourceconnection.setAlias(udbName);
                datasource = workspace.getDatasources().open(datasourceconnection);
            } else {
                sMap.smMapWC.getMapControl().getMap().setWorkspace(sMap.smMapWC.getWorkspace());
                if (sMap.smMapWC.getMapControl().getMap().getWorkspace().getDatasources().indexOf(udbName) != -1) {
                    datasource = sMap.smMapWC.getMapControl().getMap().getWorkspace().getDatasources().get(udbName);
                } else {
                    datasourceconnection.setEngineType(EngineType.UDB);
                    datasourceconnection.setServer(path);
                    datasourceconnection.setAlias(udbName);
                    datasource = sMap.smMapWC.getMapControl().getMap().getWorkspace().getDatasources().open(datasourceconnection);
                }
            }
            Datasets datasets = datasource.getDatasets();
            int count = datasets.getCount();

            WritableArray arr = Arguments.createArray();
            for (int i = 0; i < count; i++) {
                Dataset dataset = datasets.get(i);
                String name = dataset.getName();
                WritableMap writeMap = Arguments.createMap();
                writeMap.putString("title", name);
                String description = dataset.getDescription();
                if (description.equals("NULL")) {
                    description = "";
                }
                writeMap.putString("description", description);
                arr.pushMap(writeMap);
            }
            if (workspace != null) {
                workspace.getDatasources().closeAll();
                workspace.close();
                workspace.dispose();
            }
            datasourceconnection.dispose();
            promise.resolve(arr);
        } catch (Exception e) {
            promise.reject(e);
        }
    }


    /**
     * 获取UDB中数据集名称
     *
     * @param path    UDB在内存中路径
     * @param promise
     */
    @ReactMethod
    public void getUDBNameOfLabel(String path, Promise promise) {
        try {
            File tempFile = new File(path.trim());
            String[] strings = tempFile.getName().split("\\.");
            String udbName = strings[0];
            Datasource datasource;
            sMap = getInstance();
            DatasourceConnectionInfo datasourceconnection = new DatasourceConnectionInfo();
            Workspace workspace = new Workspace();
            datasourceconnection.setEngineType(EngineType.UDB);
            datasourceconnection.setServer(path);
            datasourceconnection.setAlias(udbName);
            datasource = workspace.getDatasources().open(datasourceconnection);
            Datasets datasets = datasource.getDatasets();
            int count = datasets.getCount();
            WritableArray arr = Arguments.createArray();
            for (int i = 0; i < count; i++) {
                Dataset dataset = datasets.get(i);
                String name = dataset.getName();
                WritableMap writeMap = Arguments.createMap();
                writeMap.putString("title", name);
                arr.pushMap(writeMap);
            }
            if (workspace != null) {
                workspace.getDatasources().closeAll();
                workspace.close();
                workspace.dispose();
            }
            datasourceconnection.dispose();
            promise.resolve(arr);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 根据名字显示图层
     *
     * @param name
     * @param viewEntire
     * @param center
     * @param promise
     */
    @ReactMethod
    public void openMapByName(String name, boolean viewEntire, ReadableMap center, Promise promise) {
        try {
            sMap = getInstance();
            com.supermap.mapping.Map map = sMap.smMapWC.getMapControl().getMap();
            Maps maps = sMap.smMapWC.getWorkspace().getMaps();


            Boolean isOpen = false;

            if (maps.getCount() > 0) {
                String mapName = name;

                if (name.equals("")) {
                    mapName = maps.get(0);
                }

                isOpen = map.open(mapName);

                if (isOpen) {
                    getScaleViewHelper();
                    if (viewEntire) {
                        map.viewEntire();
                    }

                    if (center != null && center.hasKey("x") && center.hasKey("y")) {
                        Double x = center.getDouble("x");
                        Double y = center.getDouble("y");
                        Point2D point2D = new Point2D(x, y);
                        map.setCenter(point2D);
                    }

                    defaultMapCenter = new Point2D(map.getCenter());
                    sMap.smMapWC.getMapControl().setAction(Action.PAN);
                    map.setVisibleScalesEnabled(false);
                    map.refresh();
                }
            }

            promise.resolve(isOpen);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 根据序号显示图层
     *
     * @param index
     * @param viewEntire
     * @param center
     * @param promise
     */
    @ReactMethod
    public void openMapByIndex(int index, boolean viewEntire, ReadableMap center, Promise promise) {
        try {
            sMap = getInstance();
            com.supermap.mapping.Map map = sMap.smMapWC.getMapControl().getMap();
            Maps maps = sMap.smMapWC.getWorkspace().getMaps();

            Boolean isOpen = index < 0;

            if (maps.getCount() > 0 && index >= 0) {
                if (index >= maps.getCount()) index = maps.getCount() - 1;
                String name = maps.get(index);

                isOpen = map.open(name);

                if (isOpen) {
                    getScaleViewHelper();

                    if (viewEntire) {
                        map.viewEntire();
                    }

                    if (center != null && center.hasKey("x") && center.hasKey("y")) {
                        Double x = center.getDouble("x");
                        Double y = center.getDouble("y");
                        Point2D point2D = new Point2D(x, y);
                        map.setCenter(point2D);
                    }
                    defaultMapCenter = new Point2D(map.getCenter());
                    sMap.smMapWC.getMapControl().setAction(Action.PAN);
                    map.setVisibleScalesEnabled(false);
                    map.refresh();
                }
            }
            promise.resolve(isOpen);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 获取工作空间地图列表
     *
     * @param promise
     */
    @ReactMethod
    public void getMaps(Promise promise) {
        try {
            sMap = getInstance();
            Maps maps = sMap.smMapWC.getWorkspace().getMaps();
            WritableArray mapList = Arguments.createArray();
            for (int i = 0; i < maps.getCount(); i++) {
                WritableMap mapInfo = Arguments.createMap();
                String mapName = maps.get(i);
                mapInfo.putString("title", mapName);
                mapList.pushMap(mapInfo);
            }
            promise.resolve(mapList);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 获取工作空间地图列表
     *
     * @param promise
     */
    @ReactMethod
    public void getMapInfo(Promise promise) {
        try {
            sMap = getInstance();
            com.supermap.mapping.Map map = sMap.smMapWC.getMapControl().getMap();

            WritableMap mapInfo = Arguments.createMap();
            mapInfo.putString("name", map.getName());
            mapInfo.putString("description", map.getDescription());
            mapInfo.putBoolean("isModified", map.isModified());

            promise.resolve(mapInfo);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 关闭工作空间及地图控件
     *
     * @param promise
     */
    @ReactMethod
    public void closeWorkspace(Promise promise) {
        try {
//            getCurrentActivity().runOnUiThread(new DisposeThread(promise));
            sMap = getInstance();
            MapControl mapControl = sMap.smMapWC.getMapControl();
            Workspace workspace = sMap.smMapWC.getWorkspace();
            com.supermap.mapping.Map map = mapControl.getMap();
            defaultMapCenter = null;
            map.close();
            map.dispose();
//                mapControl.dispose();
            workspace.close();
//            workspace.dispose();

//            sMap.smMapWC.setMapControl(null);
//            sMap.smMapWC.setWorkspace(null);
            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 关闭地图
     *
     * @param promise
     */
    @ReactMethod
    public void closeMap(Promise promise) {
        try {
            sMap = getInstance();
            if (scaleViewHelper != null) {
                if (scaleViewHelper.mapParameterChangedListener != null) {
                    scaleViewHelper.removeScaleChangeListener();
//                    scaleViewHelper.mapParameterChangedListener = null;
                }
                scaleViewHelper = null;
            }
            MapControl mapControl = sMap.smMapWC.getMapControl();
            if (mapControl != null) {
                com.supermap.mapping.Map map = mapControl.getMap();
                defaultMapCenter = null;
                deleteMarkerHelper(curLocationTag);
                map.close();
            }
            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 设置Selection样式
     *
     * @param layerPath
     * @param styleJson
     * @param promise
     */
    @ReactMethod
    public void setSelectionStyle(String layerPath, String styleJson, Promise promise) {
        try {
            sMap = getInstance();
            MapControl mapControl = sMap.smMapWC.getMapControl();

            Layer layer = SMLayer.findLayerByPath(layerPath);
            Selection selection = layer.getSelection();
            GeoStyle style = new GeoStyle();
            style.fromJson(styleJson);
            selection.setStyle(style);

            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    private void clearLayerSelection(LayerGroup layerGroup) {
        for (int i = 0; i < layerGroup.getCount(); i++) {
            Layer layer = layerGroup.get(i);
            if (layer instanceof LayerGroup) {
                clearLayerSelection((LayerGroup) layer);
            } else {
                Selection selection = layer.getSelection();
                if (selection != null) {
                    selection.clear();
//                    selection.dispose();
                }
            }
        }
    }

    /**
     * 清除Selection
     *
     * @param promise
     */
    @ReactMethod
    public void clearSelection(Promise promise) {
        try {
            sMap = getInstance();
            MapControl mapControl = sMap.smMapWC.getMapControl();

            Layers layers = mapControl.getMap().getLayers();
            for (int i = 0; i < layers.getCount(); i++) {

                Layer layer = layers.get(i);
                if (layer instanceof LayerGroup) {
                    clearLayerSelection((LayerGroup) layer);
                } else {
                    Selection selection = layer.getSelection();
                    if (selection != null) {
                        selection.clear();
//                        selection.dispose();
                    }
                }
            }
            mapControl.getMap().refresh();
            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }


    class DisposeThread implements Runnable {

        private Promise promise;

        public DisposeThread(Promise promise) {
            this.promise = promise;
        }

        @Override
        public void run() {
            try {
                sMap = getInstance();
                MapControl mapControl = sMap.smMapWC.getMapControl();
                Workspace workspace = sMap.smMapWC.getWorkspace();
                com.supermap.mapping.Map map = mapControl.getMap();
                mapControl.getEditHistory().dispose();

                map.close();
                map.dispose();
//                mapControl.dispose();
                workspace.close();
                workspace.dispose();

//                sMap.smMapWC.setMapControl(null);
                sMap.smMapWC.setWorkspace(null);
                promise.resolve(true);
            } catch (Exception e) {
                promise.resolve(e);
            }
        }
    }

    @ReactMethod
    public void setAction(int actionType, Promise promise) {
        try {
            sMap = getInstance();
            Action action = (Action) Enum.parse(Action.class, actionType);
            sMap.smMapWC.getMapControl().setAction(action);

            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }


    @ReactMethod
    public void getAction(Promise promise) {
        try {
            sMap = getInstance();
            Action action = sMap.smMapWC.getMapControl().getAction();
            int actionType = action.value();

            promise.resolve(actionType);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    @ReactMethod
    public void undo(Promise promise) {
        try {
            sMap = getInstance();
            sMap.smMapWC.getMapControl().undo();
            sMap.smMapWC.getMapControl().getMap().refresh();

            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    @ReactMethod
    public void redo(Promise promise) {
        try {
            sMap = getInstance();
            sMap.smMapWC.getMapControl().redo();
            sMap.smMapWC.getMapControl().getMap().refresh();

            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    @ReactMethod
    public void addMeasureListener(Promise promise) {
        try {
            sMap = getInstance();
            mMeasureListener = new MeasureListener() {
                @Override
                public void lengthMeasured(double curResult, Point curPoint) {
                    WritableMap map = Arguments.createMap();
                    map.putDouble("curResult", curResult);
                    WritableMap point = Arguments.createMap();
                    point.putDouble("x", curPoint.getX());
                    point.putDouble("y", curPoint.getY());
                    map.putMap("curPoint", point);

                    context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                            .emit(EventConst.MEASURE_LENGTH, map);
                }

                @Override
                public void areaMeasured(double curResult, Point curPoint) {
                    WritableMap map = Arguments.createMap();
                    map.putDouble("curResult", curResult);
                    WritableMap point = Arguments.createMap();
                    point.putDouble("x", curPoint.getX());
                    point.putDouble("y", curPoint.getY());
                    map.putMap("curPoint", point);

                    context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                            .emit(EventConst.MEASURE_AREA, map);
                }

                @Override
                public void angleMeasured(double curAngle, Point curPoint) {
                    WritableMap map = Arguments.createMap();
                    map.putDouble("curAngle", curAngle);
                    WritableMap point = Arguments.createMap();
                    point.putDouble("x", curPoint.getX());
                    point.putDouble("y", curPoint.getY());
                    map.putMap("curPoint", point);

                    context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                            .emit(EventConst.MEASURE_ANGLE, map);
                }
            };

            sMap.smMapWC.getMapControl().addMeasureListener(mMeasureListener);
            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    @ReactMethod
    public void removeMeasureListener(Promise promise) {
        try {
            sMap = getInstance();
            sMap.smMapWC.getMapControl().removeMeasureListener(mMeasureListener);
            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /******************************************** 地图工具 *****************************************************/
    /**
     * 放大缩小
     *
     * @param scale
     * @param promise
     */
    @ReactMethod
    public void zoom(double scale, Promise promise) {
        try {
            sMap = getInstance();
            com.supermap.mapping.Map map = sMap.smMapWC.getMapControl().getMap();
            map.zoom(scale);
            map.refresh();
            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 设置比例尺
     *
     * @param scale
     * @param promise
     */
    @ReactMethod
    public void setScale(double scale, Promise promise) {
        try {
            sMap = getInstance();
            com.supermap.mapping.Map map = sMap.smMapWC.getMapControl().getMap();
            map.setScale(scale);
            map.refresh();
            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 设置地图手势旋转是否可用
     *
     * @param enable
     * @param promise
     */
    @ReactMethod
    public void enableRotateTouch(boolean enable, Promise promise) {
        try {
            sMap = getInstance();
            sMap.smMapWC.getMapControl().enableRotateTouch(enable);
            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 设置地图手势俯仰是否可用
     *
     * @param enable
     * @param promise
     */
    @ReactMethod
    public void enableSlantTouch(boolean enable, Promise promise) {
        try {
            sMap = getInstance();
            sMap.smMapWC.getMapControl().enableSlantTouch(enable);
            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }


    /**
     * 地图裁剪
     *
     * @param points
     * @param layersInfo
     * @param mapName
     * @param nModule
     * @param addition
     * @param isPrivate
     * @param promise
     */
    @ReactMethod
    public void clipMap(ReadableArray points, ReadableArray layersInfo, String mapName, String nModule, ReadableMap addition, boolean isPrivate, Promise promise) {
        try {
            if (points.size() == 0) {
                promise.reject("points can not be empty!");
            } else {
                sMap = getInstance();

                Point2Ds point2Ds = new Point2Ds();
                for (int i = 0; i < points.size(); i++) {
                    ReadableMap p = points.getMap(i);
                    Point point = new Point((int) p.getDouble("x"), (int) p.getDouble("y"));
                    Point2D point2D = sMap.smMapWC.getMapControl().getMap().pixelToMap(point);

                    point2Ds.add(point2D);
                }

                GeoRegion region = new GeoRegion(point2Ds);

                if (mapName.equals("")) {
                    mapName = null;
                }
                String[] args = new String[1];
                args[0] = mapName;
                if (sMap.smMapWC.clipMap(sMap.smMapWC.getMapControl().getMap(), region, layersInfo, args)) {
                    WritableMap writeMap = Arguments.createMap();
                    writeMap.putBoolean("result", true);

                    String resultName = args[0];
                    if (resultName != null && !resultName.equals("")) {
                        if (addition.hasKey("filterLayers")) {
                            WritableMap additionMap = Arguments.createMap();
                            ReadableMapKeySetIterator keySetIterator = addition.keySetIterator();
                            while (keySetIterator.hasNextKey()) {
                                String key = keySetIterator.nextKey();
                                if (key.equals("filterLayers")) {
                                    ReadableArray filterLayers = addition.getArray(key);
                                    WritableArray arr = Arguments.createArray();
                                    for (int i = 0; i < filterLayers.size(); i++) {
                                        arr.pushString(filterLayers.getString(i));
                                    }
                                    additionMap.putArray(key, arr);
                                } else {
                                    additionMap.putString(key, addition.getString(key));
                                }
                            }
                            resultName = sMap.smMapWC.saveMapName(resultName, sMap.smMapWC.getWorkspace(), nModule, additionMap, true, true, isPrivate);
                        } else {
                            Map<String, String> additionMap = new HashMap<>();
                            ReadableMapKeySetIterator keySetIterator = addition.keySetIterator();
                            while (keySetIterator.hasNextKey()) {
                                String key = keySetIterator.nextKey();
                                additionMap.put(key, addition.getString(key));
                            }
                            resultName = sMap.smMapWC.saveMapName(resultName, sMap.smMapWC.getWorkspace(), nModule, additionMap, true, true, isPrivate);
                        }

                    }
                    sMap.smMapWC.getMapControl().getMap().refresh();
                    writeMap.putString("mapName", resultName);
                    promise.resolve(writeMap);
                } else {
                    promise.reject(null, "Clip map failed!");
                }

            }
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /******************************************** 地图工具 END*****************************************************/


    @ReactMethod
    public void submit(Promise promise) {
        try {
            sMap = SMap.getInstance();
            sMap.smMapWC.getMapControl().submit();

            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    @ReactMethod
    public void cancel(Promise promise) {
        try {
            sMap = SMap.getInstance();
            sMap.smMapWC.getMapControl().cancel();

            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 保存地图
     *
     * @param name
     * @param autoNaming 为true的话若有相同名字的地图则自动命名
     * @param promise
     */
    @ReactMethod
    public void saveMap(String name, Boolean autoNaming, Promise promise) {
        try {
            sMap = getInstance();
            com.supermap.mapping.Map map = sMap.smMapWC.getMapControl().getMap();
            boolean mapSaved = false;
            boolean wsSaved = false;
            String _name = name;
            if (_name == null || _name.equals("")) {
                if (map.getName() != null && !map.getName().equals("")) {
                    mapSaved = map.save();
                } else if (map.getLayers().getCount() > 0) {
                    _name = map.getLayers().get(0).getName();
                    int i = 0;
                    if (autoNaming) {
                        while (!mapSaved) {
                            _name = i == 0 ? name : (name + i);
                            try {
                                mapSaved = map.save(_name);
                            } catch (Exception e) {
                                mapSaved = false;
                            }
                            i++;
                        }
                    } else {
                        mapSaved = map.save(_name);
                    }
                }
            } else {
                mapSaved = map.save(_name);
            }
            wsSaved = sMap.smMapWC.getWorkspace().save();
//            wsSaved = true;

            if (mapSaved && wsSaved) {
                promise.resolve(_name);
            } else {
                promise.resolve(mapSaved && wsSaved);
            }
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 移除指定位置的地图
     *
     * @param index
     * @param promise
     */
    @ReactMethod
    public void removeMapByIndex(int index, Promise promise) {
        try {
            sMap = getInstance();
            Maps maps = sMap.smMapWC.getWorkspace().getMaps();
            boolean result = false;
            if (maps.getCount() > 0 && index < maps.getCount()) {
                if (index == -1) {
                    for (int i = maps.getCount() - 1; i >= 0; i--) {
                        String name = maps.get(i);
                        result = maps.remove(i) && result;
                        sMap.smMapWC.getWorkspace().getResources().getMarkerLibrary().getRootGroup().getChildGroups().remove(name, false);
                        sMap.smMapWC.getWorkspace().getResources().getLineLibrary().getRootGroup().getChildGroups().remove(name, false);
                        sMap.smMapWC.getWorkspace().getResources().getFillLibrary().getRootGroup().getChildGroups().remove(name, false);
                    }
                } else {
                    String name = maps.get(index);
                    result = maps.remove(index);
                    sMap.smMapWC.getWorkspace().getResources().getMarkerLibrary().getRootGroup().getChildGroups().remove(name, false);
                    sMap.smMapWC.getWorkspace().getResources().getLineLibrary().getRootGroup().getChildGroups().remove(name, false);
                    sMap.smMapWC.getWorkspace().getResources().getFillLibrary().getRootGroup().getChildGroups().remove(name, false);
                }
            }

            promise.resolve(result);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 移除指定名称的地图
     *
     * @param name
     * @param promise
     */
    @ReactMethod
    public void removeMapByName(String name, Promise promise) {
        try {
            sMap = getInstance();
            Maps maps = sMap.smMapWC.getWorkspace().getMaps();
            boolean result = false;
            if (maps.getCount() > 0 && (name == null || name.equals(""))) {
                for (int i = 0; i < maps.getCount(); i++) {
                    String _name = maps.get(i);
                    result = maps.remove(i) && result;
                    sMap.smMapWC.getWorkspace().getResources().getMarkerLibrary().getRootGroup().getChildGroups().remove(_name, false);
                    sMap.smMapWC.getWorkspace().getResources().getLineLibrary().getRootGroup().getChildGroups().remove(_name, false);
                    sMap.smMapWC.getWorkspace().getResources().getFillLibrary().getRootGroup().getChildGroups().remove(_name, false);
                }
            } else if (maps.getCount() > 0 && maps.indexOf(name) >= 0) {
                result = maps.remove(name);
                sMap.smMapWC.getWorkspace().getResources().getMarkerLibrary().getRootGroup().getChildGroups().remove(name, false);
                sMap.smMapWC.getWorkspace().getResources().getLineLibrary().getRootGroup().getChildGroups().remove(name, false);
                sMap.smMapWC.getWorkspace().getResources().getFillLibrary().getRootGroup().getChildGroups().remove(name, false);
            }

            promise.resolve(result);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 地图另存为
     *
     * @param name
     * @param promise
     */
    @ReactMethod
    public void saveAsMap(String name, Promise promise) {
        try {
            sMap = getInstance();
            com.supermap.mapping.Map map = sMap.smMapWC.getMapControl().getMap();
            boolean result = false;
            if (name != null && !name.equals("")) {
                result = map.saveAs(name);
                result = result && sMap.smMapWC.getWorkspace().save();
            }

            promise.resolve(result);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 检查地图是否有改动
     *
     * @param promise
     */
    @ReactMethod
    public void mapIsModified(Promise promise) {
        try {
            sMap = getInstance();
            com.supermap.mapping.Map map = sMap.smMapWC.getMapControl().getMap();
            boolean idModified = map.isModified();

            promise.resolve(idModified);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 检查地图是否有改动
     *
     * @param promise
     */
    @ReactMethod
    public void getMapIndex(String name, Promise promise) {
        try {
            int index = -1;
            sMap = getInstance();
            com.supermap.mapping.Map map = sMap.smMapWC.getMapControl().getMap();
            Maps maps = sMap.smMapWC.getWorkspace().getMaps();

            if (name == null || name.equals("")) {
                if (map != null) {
                    index = maps.indexOf(map.getName());
                }
            } else {
                index = maps.indexOf(name);
            }

            promise.resolve(index);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 移动到当前位置
     *
     * @param promise
     */
    @ReactMethod
    public void moveToCurrent(Promise promise) {
        try {
            MoveToCurrentThread moveToCurrentThread = new MoveToCurrentThread(promise);
            moveToCurrentThread.run();

            sMap.smMapWC.getMapControl().getMap().setAngle(0);
            sMap.smMapWC.getMapControl().getMap().SetSlantAngle(0);
//            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 移动到当前位置
     *
     * @param promise
     */
    @ReactMethod
    public void moveToPoint(ReadableMap point, Promise promise) {
        try {
            if (point.hasKey("x") && point.hasKey("y")) {
                Point2D point2D = new Point2D(point.getDouble("x"), point.getDouble("y"));
                MoveToCurrentThread moveToCurrentThread = new MoveToCurrentThread(point2D, false, promise);
                moveToCurrentThread.run();
//                promise.resolve(true);
            } else {
                promise.resolve(false);
            }

        } catch (Exception e) {
            promise.reject(e);
        }
    }

    class MoveToCurrentThread implements Runnable {

        private Promise promise;
        private Point2D point2D;
        private boolean showMarker = true;

        public MoveToCurrentThread(Promise promise) {
            this.promise = promise;
        }

        public MoveToCurrentThread(Point2D point2D, Promise promise) {
            this.promise = promise;
            this.point2D = point2D;
        }

        public MoveToCurrentThread(Point2D point2D, boolean showMarker, Promise promise) {
            this.promise = promise;
            this.point2D = point2D;
            this.showMarker = showMarker;
        }

        @Override
        public void run() {
            try {
                sMap = getInstance();
                MapControl mapControl = sMap.smMapWC.getMapControl();
                Collector collector = mapControl.getCollector();

                Point2D pt;
                if (this.point2D == null) {
                    LocationManagePlugin.GPSData gpsDat = SMCollector.getGPSPoint();
                    pt = new Point2D(gpsDat.dLongitude, gpsDat.dLatitude);
//                    pt = collector.getGPSPoint();
                } else {
                    pt = this.point2D;
                }

                Boolean isMove = true;
                if (pt != null) {
                    // Point2D point2D = new Point2D(pt);

                    if (pt.getX() <= 180 && pt.getX() >= -180 && pt.getY() >= -90 && pt.getY() <= 90) {
                        if (!safeGetType(mapControl.getMap().getPrjCoordSys(), PrjCoordSysType.PCS_EARTH_LONGITUDE_LATITUDE)) {
                            Point2Ds point2Ds = new Point2Ds();
                            point2Ds.add(pt);
                            PrjCoordSys prjCoordSys = new PrjCoordSys();
                            prjCoordSys.setType(PrjCoordSysType.PCS_EARTH_LONGITUDE_LATITUDE);
                            CoordSysTransParameter parameter = new CoordSysTransParameter();

                            CoordSysTranslator.convert(point2Ds, prjCoordSys, mapControl.getMap().getPrjCoordSys(), parameter, CoordSysTransMethod.MTH_GEOCENTRIC_TRANSLATION);
                            pt = point2Ds.getItem(0);
                        }
                    } else {
                        if (!safeGetType(mapControl.getMap().getPrjCoordSys(), PrjCoordSysType.PCS_SPHERE_MERCATOR)) {
                            Point2Ds point2Ds = new Point2Ds();
                            point2Ds.add(pt);
                            PrjCoordSys prjCoordSys = new PrjCoordSys();
                            prjCoordSys.setType(PrjCoordSysType.PCS_SPHERE_MERCATOR);
                            CoordSysTransParameter parameter = new CoordSysTransParameter();

                            CoordSysTranslator.convert(point2Ds, prjCoordSys, mapControl.getMap().getPrjCoordSys(), parameter, CoordSysTransMethod.MTH_GEOCENTRIC_TRANSLATION);
                            pt = point2Ds.getItem(0);
                        }
                    }

//                    if (mapControl.getMap().getPrjCoordSys().getType() != PrjCoordSysType.PCS_EARTH_LONGITUDE_LATITUDE) {
//                        Point2Ds point2Ds = new Point2Ds();
//                        point2Ds.add(pt);
//                        PrjCoordSys prjCoordSys = new PrjCoordSys();
//                        prjCoordSys.setType(PrjCoordSysType.PCS_EARTH_LONGITUDE_LATITUDE);
//                        CoordSysTransParameter parameter = new CoordSysTransParameter();
//
//                        CoordSysTranslator.convert(point2Ds, prjCoordSys, mapControl.getMap().getPrjCoordSys(), parameter, CoordSysTransMethod.MTH_GEOCENTRIC_TRANSLATION);
//                        pt = point2Ds.getItem(0);
//                    }
                }
                deleteMarkerHelper(curLocationTag);
                Point2D mapCenter = pt;
                if (pt != null && !mapControl.getMap().getBounds().contains(pt)) {
                    if (defaultMapCenter != null) {
                        mapCenter = defaultMapCenter;
                    }
                } else {
                    if (this.showMarker) {
                        showMarkerHelper(mapCenter, curLocationTag);
                    }
                }
                if (mapCenter != null) {
                    mapControl.getMap().setCenter(mapCenter);
                    isMove = true;
                    mapControl.getMap().refresh();
                }
                promise.resolve(isMove);
            } catch (Exception e) {
                promise.resolve(e);
            }
        }
    }

    /**
     * 监听长按动作和滚动动作
     *
     * @param promise
     */
    @ReactMethod
    public void setGestureDetector(final Promise promise) {
        try {
            mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {

                public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                        float distanceX, float distanceY) {
                    WritableMap mapE1 = Arguments.createMap();
                    mapE1.putInt("x", (int) e1.getX());
                    mapE1.putInt("y", (int) e1.getY());

                    WritableMap mapE2 = Arguments.createMap();
                    mapE2.putInt("x", (int) e2.getX());
                    mapE2.putInt("y", (int) e2.getY());

                    WritableMap map = Arguments.createMap();
                    map.putMap("start", mapE1);
                    map.putMap("end", mapE2);
                    map.putDouble("dx", distanceX);
                    map.putDouble("dy", distanceY);


                    context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                            .emit(EventConst.MAP_SCROLL, map);
                    return false;
                }

                public boolean onDown(MotionEvent event) {
                    WritableMap screenPoint = Arguments.createMap();
                    screenPoint.putInt("x", (int) event.getX());
                    screenPoint.putInt("y", (int) event.getY());

                    Point2D point2D = SMap.getInstance().getSmMapWC().getMapControl().getMap()
                            .pixelToMap(new Point((int) event.getX(), (int) event.getY()));
                    WritableMap mapPoint = Arguments.createMap();
                    mapPoint.putInt("x", (int) point2D.getX());
                    mapPoint.putInt("y", (int) point2D.getY());

                    WritableMap map = Arguments.createMap();
                    map.putMap("screenPoint", screenPoint);
                    map.putMap("mapPoint", mapPoint);

                    context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                            .emit(EventConst.MAP_TOUCH_BEGAN, map);
                    return false;
                }

                public boolean onSingleTapUp(MotionEvent event) {
                    WritableMap screenPoint = Arguments.createMap();
                    screenPoint.putInt("x", (int) event.getX());
                    screenPoint.putInt("y", (int) event.getY());

                    Point2D point2D = SMap.getInstance().getSmMapWC().getMapControl().getMap()
                            .pixelToMap(new Point((int) event.getX(), (int) event.getY()));
                    WritableMap mapPoint = Arguments.createMap();
                    mapPoint.putInt("x", (int) point2D.getX());
                    mapPoint.putInt("y", (int) point2D.getY());

                    WritableMap map = Arguments.createMap();
                    map.putMap("screenPoint", screenPoint);
                    map.putMap("mapPoint", mapPoint);

                    context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                            .emit(EventConst.MAP_SINGLE_TAP, map);
                    return false;
                }

                public void onLongPress(MotionEvent event) {
                    WritableMap screenPoint = Arguments.createMap();
                    screenPoint.putInt("x", (int) event.getX());
                    screenPoint.putInt("y", (int) event.getY());

                    Point2D point2D = SMap.getInstance().getSmMapWC().getMapControl().getMap()
                            .pixelToMap(new Point((int) event.getX(), (int) event.getY()));
                    WritableMap mapPoint = Arguments.createMap();
                    mapPoint.putDouble("x", point2D.getX());
                    mapPoint.putDouble("y", point2D.getY());

                    WritableMap map = Arguments.createMap();
                    map.putMap("screenPoint", screenPoint);
                    map.putMap("mapPoint", mapPoint);

                    context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                            .emit(EventConst.MAP_LONG_PRESS, map);
                }

                public boolean onSingleTapConfirmed(MotionEvent event) {
                    WritableMap screenPoint = Arguments.createMap();
                    screenPoint.putInt("x", (int) event.getX());
                    screenPoint.putInt("y", (int) event.getY());

                    Point2D point2D = SMap.getInstance().getSmMapWC().getMapControl().getMap()
                            .pixelToMap(new Point((int) event.getX(), (int) event.getY()));
                    WritableMap mapPoint = Arguments.createMap();
                    mapPoint.putInt("x", (int) point2D.getX());
                    mapPoint.putInt("y", (int) point2D.getY());

                    WritableMap map = Arguments.createMap();
                    map.putMap("screenPoint", screenPoint);
                    map.putMap("mapPoint", mapPoint);

                    context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                            .emit(EventConst.MAP_SINGLE_TAP_CONFIR, map);
                    return false;
                }

                public boolean onDoubleTap(MotionEvent event) {
                    WritableMap screenPoint = Arguments.createMap();
                    screenPoint.putInt("x", (int) event.getX());
                    screenPoint.putInt("y", (int) event.getY());

                    Point2D point2D = SMap.getInstance().getSmMapWC().getMapControl().getMap()
                            .pixelToMap(new Point((int) event.getX(), (int) event.getY()));
                    WritableMap mapPoint = Arguments.createMap();
                    mapPoint.putInt("x", (int) point2D.getX());
                    mapPoint.putInt("y", (int) point2D.getY());

                    WritableMap map = Arguments.createMap();
                    map.putMap("screenPoint", screenPoint);
                    map.putMap("mapPoint", mapPoint);

                    context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                            .emit(EventConst.MAP_DOUBLE_TAP, map);
                    return false;
                }
            });
            sMap = getInstance();
            MapControl mapControl = sMap.smMapWC.getMapControl();
            mapControl.setGestureDetector(mGestureDetector);
            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }


    @ReactMethod
    public void deleteGestureDetector(Promise promise) {
        try {
            sMap = getInstance();
            MapControl mapControl = sMap.smMapWC.getMapControl();
            mapControl.deleteGestureDetector();

            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    @ReactMethod
    public void addGeometrySelectedListener(Promise promise) {
        try {
            mGeometrySelectedListener = new GeometrySelectedListener() {
                @Override
                public void geometrySelected(GeometrySelectedEvent event) {
                    int id = event.getGeometryID();
                    Layer layer = event.getLayer();

                    WritableMap map = Arguments.createMap();
                    WritableMap layerInfo = Arguments.createMap();
                    layerInfo.putString("name", layer.getName());
                    layerInfo.putString("caption", layer.getCaption());
                    layerInfo.putBoolean("editable", layer.isEditable());
                    layerInfo.putBoolean("visible", layer.isVisible());
                    layerInfo.putBoolean("selectable", layer.isSelectable());
                    layerInfo.putInt("type", layer.getDataset().getType().value());
                    layerInfo.putString("path", SMLayer.getLayerPath(layer));

                    map.putMap("layerInfo", layerInfo);
                    map.putInt("id", id);

                    SMap.getInstance().setSelection(layer.getSelection());

                    context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                            .emit(EventConst.MAP_GEOMETRY_SELECTED, map);
                }

                @Override
                public void geometryMultiSelected(ArrayList<GeometrySelectedEvent> events) {
                    try {
                        ArrayList<Map> arr = new ArrayList();
                        WritableArray array = Arguments.createArray();
                        for (int i = 0; i < events.size(); i++) {
                            GeometrySelectedEvent event = events.get(i);
                            int id = event.getGeometryID();
                            Layer layer = event.getLayer();

                            boolean isExist = false;
                            for (int j = 0; j < arr.size(); j++) {
                                String name = ((WritableMap) arr.get(j).get("layerInfo")).getString("name");
                                if (layer.getName().equals(name)) {
                                    isExist = true;
                                    WritableArray ids = ((WritableArray) arr.get(j).get("ids"));
                                    ids.pushInt(id);
                                }
                            }


                            if (!isExist) {
                                Map<String, Object> layerSelection = new HashMap<>();

//                                WritableMap layerSelection = Arguments.createMap();

                                WritableArray ids = Arguments.createArray();
                                WritableMap layerInfo = Arguments.createMap();

                                layerInfo.putString("name", layer.getName());
                                layerInfo.putString("caption", layer.getCaption());
                                layerInfo.putBoolean("editable", layer.isEditable());
                                layerInfo.putBoolean("visible", layer.isVisible());
                                layerInfo.putBoolean("selectable", layer.isSelectable());
                                layerInfo.putInt("type", layer.getDataset().getType().value());
                                layerInfo.putString("path", SMLayer.getLayerPath(layer));

//                                layerSelection.putMap("layerInfo", layerInfo);
//                                layerSelection.putArray("ids", ids);

                                ids.pushInt(id);

                                layerSelection.put("layerInfo", layerInfo);
                                layerSelection.put("ids", ids);

                                arr.add(layerSelection);
                            }
                        }

                        for (int k = 0; k < arr.size(); k++) {
                            WritableMap map = Arguments.createMap();
                            map.putMap("layerInfo", (WritableMap) arr.get(k).get("layerInfo"));
                            map.putArray("ids", (WritableArray) arr.get(k).get("ids"));
                            array.pushMap(map);
                        }

                        WritableMap geometries = Arguments.createMap();
                        geometries.putArray("geometries", array);
                        context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                                .emit(EventConst.MAP_GEOMETRY_MULTI_SELECTED, geometries);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            };
            sMap = getInstance();
            MapControl mapControl = sMap.smMapWC.getMapControl();
            mapControl.addGeometrySelectedListener(mGeometrySelectedListener);
            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    @ReactMethod
    public void removeGeometrySelectedListener(Promise promise) {
        try {
            sMap = getInstance();
            MapControl mapControl = sMap.smMapWC.getMapControl();
            mapControl.removeGeometrySelectedListener(mGeometrySelectedListener);
            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 指定编辑几何对象
     *
     * @param geoID
     * @param layerName
     * @param promise
     */
    @ReactMethod
    public void appointEditGeometry(int geoID, String layerName, Promise promise) {
        try {
            sMap = getInstance();
            MapControl mapControl = sMap.smMapWC.getMapControl();
            Layer layer = SMLayer.findLayerByPath(layerName);//mapControl.getMap().getLayers().get(layerName);
            boolean result = mapControl.appointEditGeometry(geoID, layer);
            layer.setEditable(true);
            promise.resolve(result);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 获取指定SymbolGroup中所有的group
     *
     * @param type
     * @param path
     * @param promise
     */
    @ReactMethod
    public void getSymbolGroups(String type, String path, Promise promise) {
        try {
            sMap = getInstance();
            Resources resources = sMap.smMapWC.getWorkspace().getResources();
            WritableArray groups = SMSymbol.getSymbolGroups(resources, type, path);

            promise.resolve(groups);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 获取指定SymbolGroup中所有的symbol
     *
     * @param type
     * @param path
     * @param promise
     */
    @ReactMethod
    public void findSymbolsByGroups(String type, String path, Promise promise) {
        try {
            sMap = getInstance();
            Resources resources = sMap.smMapWC.getWorkspace().getResources();
            WritableArray symbols = SMSymbol.findSymbolsByGroups(resources, type, path);

            promise.resolve(symbols);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 导入工作空间
     *
     * @param wInfo
     * @param strFilePath
     * @param breplaceDatasource
     * @param promise
     */
    @ReactMethod
    public void importWorkspace(ReadableMap wInfo, String strFilePath, boolean breplaceDatasource, Promise promise) {
        try {
            sMap = SMap.getInstance();
            boolean result = sMap.smMapWC.importWorkspaceInfo(wInfo.toHashMap(), strFilePath, breplaceDatasource, true);
            promise.resolve(result);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 导出工作空间
     *
     * @param arrMapNames
     * @param strFileName
     * @param isFileReplace
     * @param promise
     */
    @ReactMethod
    public void exportWorkspace(ReadableArray arrMapNames, String strFileName, boolean isFileReplace, ReadableMap extraMap, Promise promise) {
        try {

            sMap = getInstance();
            boolean result = sMap.smMapWC.exportMapNames(arrMapNames, strFileName, isFileReplace, extraMap);
            promise.resolve(result);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    // mapName 地图名字（不含后缀）
    // ofModule 模块名（默认传空）
    // isPrivate 是否是用户数据
    // exportWorkspacePath 导出的工作空间绝对路径（含后缀）
    @ReactMethod
    public void exportWorkspaceByMap(String mapName, String exportWorkspacePath, ReadableMap mapParam, Promise promise) {
        try {
            sMap = getInstance();
            WritableMap param = Arguments.createMap();
            param.merge(mapParam);
            param.putBoolean("IsReplaceSymbol", true);
            boolean openResult = sMap.getSmMapWC().openMapName(mapName, sMap.getSmMapWC().getWorkspace(), param);
            boolean exportResult = false;
            if (openResult) {
                WritableArray array = Arguments.createArray();
                ((WritableArray) array).pushString(mapName);
                exportResult = sMap.getSmMapWC().exportMapNames(array, exportWorkspacePath, true, null);
                Maps maps = sMap.getSmMapWC().getWorkspace().getMaps();
                maps.clear();
                SMap.getInstance().getSmMapWC().getWorkspace().getResources().getMarkerLibrary().getRootGroup().getChildGroups().remove(mapName, false);
                SMap.getInstance().getSmMapWC().getWorkspace().getResources().getLineLibrary().getRootGroup().getChildGroups().remove(mapName, false);
                SMap.getInstance().getSmMapWC().getWorkspace().getResources().getFillLibrary().getRootGroup().getChildGroups().remove(mapName, false);
                sMap.getSmMapWC().getWorkspace().getDatasources().closeAll();
            }
            promise.resolve(exportResult);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 获取图层标题列表及对应的数据集类型
     *
     * @param promise
     */
    @ReactMethod
    public void getLayersNames(Promise promise) {
        try {
            sMap = getInstance();
            Layers layers = sMap.smMapWC.getMapControl().getMap().getLayers();
            int count = layers.getCount();
            WritableArray arr = Arguments.createArray();
            for (int i = 0; i < count; i++) {
                //获取图层标题（区别于图层的名称）
                String caption = layers.get(i).getCaption();
                WritableMap writeMap = Arguments.createMap();

                //获取数据集类型
                DatasetType type = layers.get(i).getDataset().getType();
                String datasetType = "";
                if (type == DatasetType.POINT) {
                    datasetType = "POINT";
                } else if (type == DatasetType.LINE) {
                    datasetType = "LINE";
                } else if (type == DatasetType.REGION) {
                    datasetType = "REGION";
                } else if (type == DatasetType.GRID) {
                    datasetType = "GRID";
                } else if (type == DatasetType.TEXT) {
                    datasetType = "TEXT";
                } else if (type == DatasetType.IMAGE) {
                    datasetType = "IMAGE";
                } else {
                    datasetType = type.toString();
                }

                writeMap.putString("title", caption);
                writeMap.putString("datasetType", datasetType);
                arr.pushMap(writeMap);
            }
            promise.resolve(arr);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    @ReactMethod
    public void isModified(Promise promise) {
        try {
            sMap = getInstance();
            boolean bWorspaceModified = sMap.smMapWC.getWorkspace().isModified();
            boolean bMapModified = sMap.smMapWC.getMapControl().getMap().isModified();
            if (!bWorspaceModified && !bMapModified)
                promise.resolve(false);
            else
                promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    @ReactMethod
    public void getMapName(Promise promise) {
        try {
            sMap = getInstance();
            String mapName = sMap.smMapWC.getMapControl().getMap().getName();
            promise.resolve(mapName);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 保存地图为XML
     */
    @ReactMethod
    public void saveMapToXML(String filePath, Promise promise) {
        try {
            sMap = getInstance();
            String mapName = filePath.substring(filePath.lastIndexOf('/') + 1, filePath.lastIndexOf('.'));

            int count = sMap.smMapWC.getWorkspace().getMaps().getCount();
            for (int i = 0; i < count; i++) {
                String name = sMap.smMapWC.getWorkspace().getMaps().get(i);
                if (mapName.equals(name)) {
                    sMap.smMapWC.getMapControl().getMap().save();
                    break;
                }
                if (i == count - 1) {
                    sMap.smMapWC.getMapControl().getMap().saveAs(mapName);
                }
            }

            if (count == 0) {
                sMap.smMapWC.getMapControl().getMap().saveAs(mapName);
            }
            String mapXML = sMap.smMapWC.getMapControl().getMap().toXML();


            if (!mapXML.equals("")) {
                File file = new File(filePath);

                FileWriter fileWritter = new FileWriter(file);
                fileWritter.write(mapXML);
                fileWritter.close();
            }

            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 加载地图XML，显示地图
     *
     * @param
     */
    @ReactMethod
    public void openMapFromXML(String filePath, Promise promise) {
        try {
            sMap = getInstance();
            String mapName = filePath.substring(filePath.lastIndexOf('/') + 1, filePath.lastIndexOf('.'));
            File file = new File(filePath);
            Reader reader = null;

            reader = new InputStreamReader(new FileInputStream(file));
            char[] buffer = new char[1024];
            int index = 0;
            String strXML = "";
            while ((index = reader.read(buffer)) != -1) {
                strXML += String.valueOf(buffer);
            }

            int count = sMap.smMapWC.getWorkspace().getMaps().getCount();
            for (int i = 0; i < count; i++) {
                String name = sMap.smMapWC.getWorkspace().getMaps().get(i);
                if (mapName.equals(name)) {
                    break;
                }
                if (i == count - 1) {
                    sMap.smMapWC.getWorkspace().getMaps().add(mapName, strXML);
                }
            }
            if (count == 0) {
                sMap.smMapWC.getWorkspace().getMaps().add(mapName, strXML);
            }
            sMap.smMapWC.getMapControl().getMap().open(mapName);
            sMap.smMapWC.getMapControl().getMap().refresh();
            promise.resolve(true);

        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 获取地图对应的数据源
     *
     * @param
     */
    @ReactMethod
    public void getMapDatasourcesAlias(Promise promise) {
        try {
            sMap = getInstance();
            Layers layers = sMap.smMapWC.getMapControl().getMap().getLayers();
            int count = layers.getCount();

            String datasourceName = "";
            ArrayList<String> datasourceNamelist = new ArrayList<String>();
            WritableArray arr = Arguments.createArray();
            for (int i = 0; i < count; i++) {
                Dataset dataset = layers.get(i).getDataset();
                if (dataset != null) {
                    String dataSourceAlias = dataset.getDatasource().getAlias();

                    if (!datasourceNamelist.contains(dataSourceAlias)) {
                        datasourceNamelist.add(dataSourceAlias);

                        WritableMap writeMap = Arguments.createMap();
                        writeMap.putString("title", dataSourceAlias + ".udb");
                        arr.pushMap(writeMap);
                    }
                }

            }

//            datasourceName.substring( 2,datasourceName.length()-2 );
            promise.resolve(arr);

        } catch (Exception e) {
            promise.reject(e);
        }

    }

    /**
     * 添加数据集到当前地图
     *
     * @param readableMap
     * @param promise
     */
    @ReactMethod
    public void addDatasetToMap(ReadableMap readableMap, Promise promise) {
        try {
            sMap = SMap.getInstance();
            HashMap<String, Object> data = readableMap.toHashMap();
            String datastourceName = null;
            String datasetName = null;

            if (data.containsKey("DatasourceName")) {
                datastourceName = data.get("DatasourceName").toString();
            }
            if (data.containsKey("DatasetName")) {
                datasetName = data.get("DatasetName").toString();
            }

            Workspace workspace = sMap.smMapWC.getWorkspace();
            if (datastourceName != null && datasetName != null) {
                Datasource datasource = workspace.getDatasources().get(datastourceName);
                Dataset dataset = datasource.getDatasets().get(datasetName);

                com.supermap.mapping.Map map = sMap.smMapWC.getMapControl().getMap();
                Layer layer = map.getLayers().add(dataset, true);
                if (dataset.getType() == DatasetType.REGION) {
                    LayerSettingVector setting = (LayerSettingVector) layer.getAdditionalSetting();
                    setting.getStyle().setLineSymbolID(5);
                }
                if (dataset.getType() == DatasetType.REGION || dataset.getType() == DatasetType.REGION3D) {
                    LayerSettingVector setting = (LayerSettingVector) layer.getAdditionalSetting();
                    setting.getStyle().setFillForeColor(getFillColor());
                    setting.getStyle().setLineColor(getLineColor());
                } else if (dataset.getType() == DatasetType.LINE || dataset.getType() == DatasetType.NETWORK || dataset.getType() == DatasetType.NETWORK3D
                        || dataset.getType() == DatasetType.LINE3D) {
                    LayerSettingVector setting = (LayerSettingVector) layer.getAdditionalSetting();
                    setting.getStyle().setLineColor(getLineColor());
                    if (dataset.getType() == DatasetType.NETWORK || dataset.getType() == DatasetType.NETWORK3D) {
                        map.getLayers().add(((DatasetVector) dataset).getChildDataset(), true);
                    }
                } else if (dataset.getType() == DatasetType.POINT || dataset.getType() == DatasetType.POINT3D) {
                    LayerSettingVector setting = (LayerSettingVector) layer.getAdditionalSetting();
                    setting.getStyle().setLineColor(getLineColor());
                }

                map.setVisibleScalesEnabled(false);
                map.refresh();

                promise.resolve(layer != null);
            } else {
                promise.resolve(false);
            }
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 导出地图为xml
     *
     * @param name
     * @param nModule
     * @param addition
     * @param isNew
     * @param promise
     */
    @ReactMethod
    public void saveMapName(String name, String nModule, ReadableMap addition, boolean isNew, boolean bResourcesModified, boolean bPrivate, Promise promise) {
        try {
            sMap = SMap.getInstance();
            boolean mapSaved = false;
            boolean bNew = true;

            com.supermap.mapping.Map map = sMap.smMapWC.getMapControl().getMap();
            if (map.getName() != null && !map.getName().equals("")) {
                bNew = false;
            }

            String oldName = map.getName();

            if (name == null || name.equals("")) {
                if (map.getName() != null && !map.getName().equals("")) {
                    bNew = false;
                    mapSaved = map.save();
                    name = map.getName();
                } else if (map.getLayers().getCount() > 0) {
                    bNew = true;
                    Layers layers = map.getLayers();
                    Layer layer = layers.get(layers.getCount() - 1);
                    name = layer.getName();
                    int i = 0;
                    while (!mapSaved) {
                        name = i == 0 ? name : (name + i);
                        mapSaved = map.save(name);
                        i++;
                    }
                }
            } else {
                if (name.equals(map.getName())) {
                    bNew = false;
                    mapSaved = map.save();
                    name = map.getName();
                } else {
                    bNew = true;
                    mapSaved = isNew ? map.saveAs(name) : map.save(name);
                }
            }

//            boolean bResourcesModified = sMap.smMapWC.getWorkspace().getMaps().getCount() > 1;
            String mapName = "";

            Map<String, String> additionInfo = new HashMap<>();
            ReadableMapKeySetIterator keys = addition.keySetIterator();
            WritableMap writableMap = Arguments.createMap();
            boolean needFilter = false;
            if (addition.hasKey("filterLayers")) {
                needFilter = true;
                while (keys.hasNextKey()) {
                    String key = keys.nextKey();
                    if (key.equals("filterLayers")) {
                        WritableArray array = Arguments.createArray();
                        for (int i = 0; i < addition.getArray(key).size(); i++) {
                            array.pushString(addition.getArray(key).getString(i));
                        }
                        writableMap.putArray(key, array);
                    } else {
                        writableMap.putString(key, addition.getString(key));
                    }
                }
            } else {
                while (keys.hasNextKey()) {
                    String key = keys.nextKey();
                    additionInfo.put(key, addition.getString(key));
                }
            }

            if (mapSaved) {
                if (needFilter) {
                    mapName = sMap.smMapWC.saveMapName(name, sMap.smMapWC.getWorkspace(), nModule, writableMap, (isNew || bNew), bResourcesModified, bPrivate);
                } else {
                    mapName = sMap.smMapWC.saveMapName(name, sMap.smMapWC.getWorkspace(), nModule, additionInfo, (isNew || bNew), bResourcesModified, bPrivate);
                }

                //保存地图后拷贝推演动画xml文件
                if(mapName!=null){
                    String strUserName = null;
                    if (!bPrivate) {
                        strUserName = "Customer";
                    }else{
                        strUserName = SMap.getInstance().smMapWC.getUserName();
                    }
                    String strRootPath = homeDirectory + "/iTablet/User/";
                    String strAnimationPath = strRootPath+strUserName+"/Data/Animation/";
                    String fromPath=strAnimationPath+oldName;
                    String toPath=strAnimationPath+mapName+"/";
                    if((new File(fromPath)).exists()){
                        SMap.getInstance().smMapWC.copyAnimationFile(fromPath,toPath,mapName);
                    }
                }
            }

            Maps maps = sMap.getSmMapWC().getWorkspace().getMaps();
            // isNew为true，另存为后自动打开另存的地图
            boolean isOpen = false;
            if (oldName != null && !oldName.equals("") && !oldName.equals(mapName) && isNew) {
                if (maps.indexOf(mapName) >= 0) {
                    isOpen = map.open(mapName);
                } else {
                    map.saveAs(mapName);
                }
                if (isOpen && maps.indexOf(oldName) >= 0) {
                    maps.remove(oldName);
                }
            }

            map.refresh();
            promise.resolve(mapName);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 导入文件工作空间到程序目录
     *
     * @param infoMap
     * @param nModule
     * @param promise
     */
    @ReactMethod
    public void importWorkspaceInfo(ReadableMap infoMap, String nModule, boolean bPrivate, Promise promise) {
        try {
            sMap = SMap.getInstance();
            List<String> list = sMap.smMapWC.importWorkspaceInfo(infoMap.toHashMap(), nModule, bPrivate);
            WritableArray mapsInfo = Arguments.createArray();
            if (list == null) {
                promise.resolve(mapsInfo);
            } else {
                if (list.size() > 0) {
                    for (int i = 0; i < list.size(); i++) {
                        mapsInfo.pushString(list.get(i));
                    }
                }
                promise.resolve(mapsInfo);
            }
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 导入数据源到程序目录
     *
     * @param strFile
     * @param strModule
     * @param promise
     */
    @ReactMethod
    public void importDatasourceFile(String strFile, String strModule, Promise promise) {
        try {
//            sMap = SMap.getInstance();
//            DatasourceConnectionInfo datasourceConnectionInfo = new DatasourceConnectionInfo();
//            datasourceConnectionInfo.setServer(strFile);
//            datasourceConnectionInfo.setEngineType(EngineType.UDB);
//            Datasource datasource = sMap.smMapWC.getWorkspace().getDatasources().open(datasourceConnectionInfo);
//            if(datasource.getDescription().equals("Label")){
//                String todatasource=rootPath+"/iTablet/User/"+sMap.smMapWC.getUserName()+"/Data/Label/Label.udb";
//                File udb=new File(todatasource);
//                if(udb.exists()){
//                    sMap.getSmMapWC().copyDataset(strFile,todatasource);
//                }
//            }else {
//                String result = sMap.smMapWC.importDatasourceFile(strFile, strModule);
//                promise.resolve(result);
//            }
//            datasourceConnectionInfo.dispose();
            String result = sMap.smMapWC.importDatasourceFile(strFile, strModule);
            promise.resolve(result);
        } catch (Exception e) {
            promise.reject(e);
        }
    }


    /**
     * 大工作空间打开本地地图
     *
     * @param strMapName
     * @param promise
     */
    @ReactMethod
    public void openMapName(String strMapName, ReadableMap mapParam, Promise promise) {
        try {
            sMap = SMap.getInstance();
            boolean result = sMap.smMapWC.openMapName(strMapName, sMap.smMapWC.getWorkspace(), mapParam);

            promise.resolve(result);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 设置地图反走样
     *
     * @param value
     * @param promise
     */
    @ReactMethod
    public void setAntialias(boolean value, Promise promise) {
        try {
            sMap = SMap.getInstance();
            com.supermap.mapping.Map map = sMap.getSmMapWC().getMapControl().getMap();
            map.setAntialias(value);
            map.refresh();

            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 获取是否反走样
     *
     * @param promise
     */
    @ReactMethod
    public void isAntialias(Promise promise) {
        try {
            sMap = SMap.getInstance();
            com.supermap.mapping.Map map = sMap.getSmMapWC().getMapControl().getMap();
            boolean result = map.isAntialias();

            promise.resolve(result);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 设置固定比例尺
     *
     * @param value
     * @param promise
     */
    @ReactMethod
    public void setVisibleScalesEnabled(boolean value, Promise promise) {
        try {
            sMap = SMap.getInstance();
            com.supermap.mapping.Map map = sMap.getSmMapWC().getMapControl().getMap();
            map.setVisibleScalesEnabled(value);
            map.refresh();

            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 获取是否固定比例尺
     *
     * @param promise
     */
    @ReactMethod
    public void isVisibleScalesEnabled(Promise promise) {
        try {
            sMap = SMap.getInstance();
            com.supermap.mapping.Map map = sMap.getSmMapWC().getMapControl().getMap();
            boolean result = map.isVisibleScalesEnabled();

            promise.resolve(result);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 检查是否有打开的地图
     *
     * @param promise
     */
    @ReactMethod
    public void isAnyMapOpened(Promise promise) {
        try {
            sMap = getInstance();
            com.supermap.mapping.Map map = sMap.smMapWC.getMapControl().getMap();
            int count = map.getLayers().getCount();
            boolean isAny = true;
            if (count <= 0) {
                isAny = false;
            }

            promise.resolve(isAny);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 把指定地图中的图层添加到当前打开地图中
     *
     * @param srcMapName 要添加的地图
     * @param promise
     */
    @ReactMethod
    public void addMap(String srcMapName, ReadableMap mapParam, Promise promise) {
        try {
            sMap = getInstance();
            com.supermap.mapping.Map map = sMap.smMapWC.getMapControl().getMap();

            sMap.smMapWC.getMapControl().getEditHistory().addMapHistory();
            boolean result = sMap.smMapWC.addLayersFromMap(srcMapName, map, mapParam);

            promise.resolve(result);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 批量添加图层
     *
     * @param datasetNames
     * @param promise
     */
    @ReactMethod
    public void addLayers(ReadableArray datasetNames, String datastourceName, Promise promise) {
        try {
            if (datasetNames == null || datasetNames.size() == 0 || datastourceName == null || datastourceName.isEmpty()) {
                promise.resolve(false);
                return;
            }

            Workspace workspace = sMap.smMapWC.getWorkspace();
            Datasource datasource = workspace.getDatasources().get(datastourceName);
            com.supermap.mapping.Map map = sMap.smMapWC.getMapControl().getMap();
            Layers layers = map.getLayers();

            ArrayList<Dataset> datasets_point = new ArrayList<>();
            ArrayList<Dataset> datasets_line = new ArrayList<>();
            ArrayList<Dataset> datasets_region = new ArrayList<>();
            ArrayList<Dataset> datasets_text = new ArrayList<>();
            ArrayList<Dataset> datasets_else = new ArrayList<>();
            for (int i = 0; i < datasetNames.size(); i++) {
                String datasetName = datasetNames.getString(i);
                Dataset dataset = datasource.getDatasets().get(datasetName);


                if (dataset.getType() == DatasetType.REGION || dataset.getType() == DatasetType.REGION3D) {
                    datasets_region.add(dataset);
                } else if (dataset.getType() == DatasetType.LINE || dataset.getType() == DatasetType.NETWORK || dataset.getType() == DatasetType.NETWORK3D
                        || dataset.getType() == DatasetType.LINE3D) {
                    datasets_line.add(dataset);
                } else if (dataset.getType() == DatasetType.POINT || dataset.getType() == DatasetType.POINT3D) {
                    datasets_point.add(dataset);
                } else if (dataset.getType() == DatasetType.TEXT) {
                    datasets_text.add(dataset);
                } else {
                    datasets_else.add(dataset);
                }
            }

            ArrayList<Dataset> datasets = new ArrayList<>();
            datasets.addAll(datasets_else);
            datasets.addAll(datasets_region);
            datasets.addAll(datasets_line);
            datasets.addAll(datasets_point);
            datasets.addAll(datasets_text);

            WritableArray resultArr = Arguments.createArray();

            if (datasets.size() > 0) {
                MapControl mapControl = SMap.getSMWorkspace().getMapControl();
                mapControl.getEditHistory().addMapHistory();

                for (int i = 0; i < datasets.size(); i++) {
                    Dataset dataset = datasets.get(i);

                    Layer layer = layers.add(dataset, true);
                    if (dataset.getType() == DatasetType.REGION) {
                        LayerSettingVector setting = (LayerSettingVector) layer.getAdditionalSetting();
                        setting.getStyle().setLineSymbolID(5);
                    }
                    if (dataset.getType() == DatasetType.REGION || dataset.getType() == DatasetType.REGION3D) {
                        LayerSettingVector setting = (LayerSettingVector) layer.getAdditionalSetting();
                        setting.getStyle().setFillForeColor(getFillColor());
                        setting.getStyle().setLineColor(getLineColor());
                    } else if (dataset.getType() == DatasetType.LINE || dataset.getType() == DatasetType.NETWORK || dataset.getType() == DatasetType.NETWORK3D
                            || dataset.getType() == DatasetType.LINE3D) {
                        LayerSettingVector setting = (LayerSettingVector) layer.getAdditionalSetting();
                        setting.getStyle().setLineColor(getLineColor());
                        if (dataset.getType() == DatasetType.NETWORK || dataset.getType() == DatasetType.NETWORK3D) {
                            map.getLayers().add(((DatasetVector) dataset).getChildDataset(), true);
                        }
                    } else if (dataset.getType() == DatasetType.POINT || dataset.getType() == DatasetType.POINT3D) {
                        LayerSettingVector setting = (LayerSettingVector) layer.getAdditionalSetting();
                        setting.getStyle().setLineColor(getLineColor());
                    }

                    if (layer != null) {
                        WritableMap layerInfo = Arguments.createMap();
                        layerInfo.putString("layerName", layer.getName());
                        layerInfo.putString("datasetName", layer.getDataset().getName());
                        layerInfo.putInt("datasetType", layer.getDataset().getType().value());
                        layerInfo.putString("description", layer.getDataset().getDescription());

                        resultArr.pushMap(layerInfo);
                    }
                }
                map.setVisibleScalesEnabled(false);
                map.refresh();
            }

            promise.resolve(resultArr);
        } catch (Exception e) {
            Log.e(REACT_CLASS, e.getMessage());
            e.printStackTrace();
            promise.reject(e);
        }
    }

    /**
     * 导入符号库
     *
     * @param path
     * @param isReplace 是否替换
     * @param promise
     */
    @ReactMethod
    public void importSymbolLibrary(String path, boolean isReplace, Promise promise) {
        try {
            sMap = SMap.getInstance();
            boolean result = sMap.smMapWC.appendFromFile(SMap.getInstance().smMapWC.getWorkspace().getResources(), path, isReplace);

            promise.resolve(result);
        } catch (Exception e) {
            Log.e(REACT_CLASS, e.getMessage());
            e.printStackTrace();
            promise.reject(e);
        }
    }

    /**
     * 设置是否压盖
     *
     * @param value
     * @param promise
     */
    @ReactMethod
    public void setOverlapDisplayed(boolean value, Promise promise) {
        try {
            sMap = SMap.getInstance();
            com.supermap.mapping.Map map = sMap.getSmMapWC().getMapControl().getMap();
            map.setOverlapDisplayed(value);
            map.refresh();

            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 是否已经开启压盖
     *
     * @param promise
     */
    @ReactMethod
    public void isOverlapDisplayed(Promise promise) {
        try {
            sMap = SMap.getInstance();
            com.supermap.mapping.Map map = sMap.getSmMapWC().getMapControl().getMap();
            boolean result = map.isOverlapDisplayed();

            promise.resolve(result);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    @ReactMethod
    public void getMapsByFile(String path, Promise promise) {
        try {
            WorkspaceType type = null;
            if (path.contains("sxwu")) {
                type = WorkspaceType.SXWU;
            } else if (path.contains("smwu")) {
                type = WorkspaceType.SMWU;
            } else if (path.contains("sxw")) {
                type = WorkspaceType.SXW;
            } else if (path.contains("smw")) {
                type = WorkspaceType.SMW;
            }
            Workspace workspace = new Workspace();
            WorkspaceConnectionInfo wsInfo = new WorkspaceConnectionInfo();
            wsInfo.setServer(path);
            wsInfo.setType(type);
            boolean result = workspace.open(wsInfo);
            WritableArray arr = Arguments.createArray();
            if (result) {
                for (int i = 0; i < workspace.getMaps().getCount(); i++) {
                    arr.pushString(workspace.getMaps().get(i));
                }
            }

            workspace.close();
            wsInfo.dispose();
            workspace.dispose();

            promise.resolve(arr);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 显示全幅
     *
     * @param promise
     */
    @ReactMethod
    public void viewEntire(Promise promise) {

        try {
            sMap = SMap.getInstance();
            com.supermap.mapping.Map map = sMap.getSmMapWC().getMapControl().getMap();

            Layer layerWeb = null;
            int nLayerCount = map.getLayers().getCount();
            if (nLayerCount > 1) {
                Layer layerTemp = map.getLayers().get(nLayerCount - 1);
                if (layerTemp.isVisible() && layerTemp.getDataset() != null && layerTemp.getDataset().getDatasource() != null) {
                    EngineType engineType = layerTemp.getDataset().getDatasource().getConnectionInfo().getEngineType();
                    if (engineType == EngineType.OGC ||
                            engineType == EngineType.SuperMapCloud ||
                            engineType == EngineType.GoogleMaps ||
                            engineType == EngineType.Rest ||
                            engineType == EngineType.BaiDu ||
                            engineType == EngineType.BingMaps ||
                            engineType == EngineType.OpenStreetMaps
                            ) {
                        layerWeb = layerTemp;
                    }
                }
            }

            if (layerWeb != null) {
                layerWeb.setVisible(false);
                map.viewEntire();
                layerWeb.setVisible(true);
            } else {
                map.viewEntire();
            }


            map.refresh();

            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 开启动态投影
     *
     * @param promise
     */
    @ReactMethod
    public void setDynamicProjection(Promise promise) {
        try {
            sMap = SMap.getInstance();
            com.supermap.mapping.Map map = sMap.getSmMapWC().getMapControl().getMap();
            map.setDynamicProjection(true);
            map.refresh();

            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    // 添加指定字段到数据集
    private void addFieldInfo(DatasetVector dv, String name, FieldType type, boolean required, String value, int maxLength) {
        FieldInfos infos = dv.getFieldInfos();
        if (infos.indexOf(name) != -1) {//exists
            infos.remove(name);
        }
        FieldInfo newInfo = new FieldInfo();
        newInfo.setName(name);
        newInfo.setType(type);
        newInfo.setMaxLength(maxLength);
        newInfo.setDefaultValue(value);
        newInfo.setRequired(required);
        infos.add(newInfo);
    }

    /**
     * 新建标注数据集
     *
     * @param promise
     */
    @ReactMethod
    public void newTaggingDataset(String name, String userpath, boolean editable, String type, Promise promise) {
        try {
            sMap = SMap.getInstance();
            Workspace workspace = sMap.smMapWC.getMapControl().getMap().getWorkspace();
            Datasource opendatasource = workspace.getDatasources().get("Label_" + userpath + "#");
//            sMap.smMapWC.getWorkspace().getConnectionInfo().getServer();
            String datasetName = "";
            Layer layer = null;
            if (opendatasource == null) {
                DatasourceConnectionInfo info = new DatasourceConnectionInfo();
                info.setAlias("Label_" + userpath + "#");
                info.setEngineType(EngineType.UDB);
                info.setServer(rootPath + "/iTablet/User/" + userpath + "/Data/Datasource/Label_" + userpath + "#.udb");
                Datasource datasource = workspace.getDatasources().open(info);
                if (datasource != null) {
                    Datasets datasets = datasource.getDatasets();
                    datasetName = datasets.getAvailableDatasetName(name);
                    DatasetVectorInfo datasetVectorInfo = new DatasetVectorInfo();
                    datasetVectorInfo.setType(DatasetType.CAD);
                    datasetVectorInfo.setEncodeType(EncodeType.NONE);
                    datasetVectorInfo.setName(datasetName);
                    DatasetVector datasetVector = datasets.create(datasetVectorInfo);
                    datasetVector.setPrjCoordSys(sMap.getSmMapWC().getMapControl().getMap().getPrjCoordSys());

                    //创建数据集时创建好字段
                    addFieldInfo(datasetVector, "name", FieldType.TEXT, false, "", 255);
                    addFieldInfo(datasetVector, "remark", FieldType.TEXT, false, "", 255);
                    addFieldInfo(datasetVector, "address", FieldType.TEXT, false, "", 255);

                    Dataset ds = datasets.get(datasetName);
                    ds.setDescription("{\"type\":\"" + type + "\"}");
                    com.supermap.mapping.Map map = sMap.smMapWC.getMapControl().getMap();
                    layer = map.getLayers().add(ds, true);
                    layer.setSnapable(editable);
                    layer.setSnapable(false);
                    datasetVectorInfo.dispose();
                    datasetVector.close();
                    info.dispose();
                }
            } else {
                Datasets datasets = opendatasource.getDatasets();
                datasetName = datasets.getAvailableDatasetName(name);
                DatasetVectorInfo datasetVectorInfo = new DatasetVectorInfo();
                datasetVectorInfo.setType(DatasetType.CAD);
                datasetVectorInfo.setEncodeType(EncodeType.NONE);
                datasetVectorInfo.setName(datasetName);
                DatasetVector datasetVector = datasets.create(datasetVectorInfo);
                datasetVector.setPrjCoordSys(sMap.getSmMapWC().getMapControl().getMap().getPrjCoordSys());
                //创建数据集时创建好字段
                addFieldInfo(datasetVector, "name", FieldType.TEXT, false, "", 255);
                addFieldInfo(datasetVector, "remark", FieldType.TEXT, false, "", 255);
                addFieldInfo(datasetVector, "address", FieldType.TEXT, false, "", 255);

                Dataset ds = datasets.get(datasetName);
                ds.setDescription("{\"type\":\"" + type + "\"}");
                com.supermap.mapping.Map map = sMap.smMapWC.getMapControl().getMap();
                layer = map.getLayers().add(ds, true);
                layer.setEditable(editable);
                layer.setSnapable(editable);
                datasetVectorInfo.dispose();
                datasetVector.close();
            }
            WritableMap map = Arguments.createMap();
            map.putString("datasetName", datasetName);
            map.putString("layerName", layer != null ? layer.getName() : "");
            promise.resolve(map);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 删除标注数据集
     *
     * @param promise
     */
    @ReactMethod
    public void removeTaggingDataset(String name, String userpath, Promise promise) {
        try {
            sMap = SMap.getInstance();
            Workspace workspace = sMap.smMapWC.getMapControl().getMap().getWorkspace();
            Datasource opendatasource = workspace.getDatasources().get("Label_" + userpath + "#");
            if (opendatasource == null) {
                DatasourceConnectionInfo info = new DatasourceConnectionInfo();
                info.setAlias("Label_" + userpath + "#");
                info.setEngineType(EngineType.UDB);
                info.setServer(rootPath + "/iTablet/User/" + userpath + "/Data/Datasource/Label_" + userpath + "#.udb");
                Datasource datasource = workspace.getDatasources().open(info);
                if (datasource != null) {
                    Datasets datasets = datasource.getDatasets();
                    datasets.delete(name);
                }
                promise.resolve(true);
            } else {
                Datasets datasets = opendatasource.getDatasets();
                datasets.delete(name);
                promise.resolve(true);
            }
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 导入标注数据集
     *
     * @param promise
     */
    @ReactMethod
    public void openTaggingDataset(String userpath, Promise promise) {
        try {
            sMap = SMap.getInstance();
            Workspace workspace = sMap.smMapWC.getMapControl().getMap().getWorkspace();
            Datasource opendatasource = workspace.getDatasources().get("Label_" + userpath + "#");
            if (opendatasource == null) {
                DatasourceConnectionInfo info = new DatasourceConnectionInfo();
                info.setAlias("Label_" + userpath + "#");
                info.setEngineType(EngineType.UDB);
                info.setServer(rootPath + "/iTablet/User/" + userpath + "/Data/Datasource/Label_" + userpath + "#.udb");
                Datasource datasource = workspace.getDatasources().open(info);
                if (datasource != null) {
                    Datasets datasets = datasource.getDatasets();
                    com.supermap.mapping.Map map = sMap.smMapWC.getMapControl().getMap();
                    for (int i = 0; i < datasets.getCount(); i++) {
                        Dataset ds = datasets.get(i);
                        String addname = ds.getName() + "@Label_" + userpath + "#";
                        boolean add = true;
                        Layers maplayers = map.getLayers();
                        for (int j = 0; j < maplayers.getCount(); j++) {
                            if (maplayers.get(j).getCaption().equals(addname)) {
                                add = false;
                            }
                        }
                        if (add) {
                            Layer layer = map.getLayers().add(ds, true);
                            layer.setEditable(false);
                            layer.setVisible(false);
                        }
                    }
                }
                promise.resolve(true);
            } else {
                Datasets datasets = opendatasource.getDatasets();
                com.supermap.mapping.Map map = sMap.smMapWC.getMapControl().getMap();
                for (int i = 0; i < datasets.getCount(); i++) {
                    Dataset ds = datasets.get(i);
                    String addname = ds.getName() + "@Label_" + userpath + "#";
                    boolean add = true;
                    Layers maplayers = map.getLayers();
                    for (int j = 0; j < maplayers.getCount(); j++) {
                        if (maplayers.get(j).getCaption().equals(addname)) {
                            add = false;
                        }
                    }
                    if (add) {
                        Layer layer = map.getLayers().add(ds, true);
                        layer.setEditable(false);
                        layer.setVisible(false);
                    }
                }
                promise.resolve(true);
            }
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 获取默认标注
     *
     * @param promise
     */
    @ReactMethod
    public void getDefaultTaggingDataset(String userpath, Promise promise) {
        try {
            sMap = SMap.getInstance();
            Workspace workspace = sMap.smMapWC.getMapControl().getMap().getWorkspace();
            Datasource opendatasource = workspace.getDatasources().get("Label_" + userpath + "#");
            if (opendatasource != null) {
                String datasetname = "";
                Datasets datasets = opendatasource.getDatasets();
                Layers layers = sMap.smMapWC.getMapControl().getMap().getLayers();
                boolean isEditable = false;
                for (int i = 0; i < layers.getCount(); i++) {
                    if (!isEditable) {
                        Layer layer = layers.get(i);
                        for (int j = 0; j < datasets.getCount(); j++) {
                            Dataset dataset = datasets.get(j);
                            if (layer.getDataset() == dataset) {
                                if (layer.isEditable()) {
                                    isEditable = true;
                                    datasetname = dataset.getName();
                                    break;
                                }
                            }
                        }
                    }
                }
                promise.resolve(datasetname);
            }
        } catch (Exception e) {
            promise.reject(e);
        }
    }


    /**
     * 判断是否有标注图层
     *
     * @param promise
     */
    @ReactMethod
    public void isTaggingLayer(String userpath, Promise promise) {
        try {
            sMap = SMap.getInstance();
            Workspace workspace = sMap.smMapWC.getMapControl().getMap().getWorkspace();
            Datasource opendatasource = workspace.getDatasources().get("Label_" + userpath + "#");
            if (opendatasource != null) {
                Datasets datasets = opendatasource.getDatasets();
                Layers layers = sMap.smMapWC.getMapControl().getMap().getLayers();
                boolean isEditable = false;
                for (int i = 0; i < layers.getCount(); i++) {
                    if (!isEditable) {
                        Layer layer = layers.get(i);
                        for (int j = 0; j < datasets.getCount(); j++) {
                            Dataset dataset = datasets.get(j);
                            if (layer.getDataset() == dataset) {
                                if (layer.isEditable()) {
                                    isEditable = true;
                                    break;
                                }
                            }
                        }
                        if (isEditable) break;
                    }
                }
                promise.resolve(isEditable);
            }
        } catch (Exception e) {
            promise.reject(e);
        }
    }


    /**
     * 判断是否有标注图层，并获取当前标注图层信息
     *
     * @param promise
     */
    @ReactMethod
    public void getCurrentTaggingLayer(String userpath, Promise promise) {
        try {
            sMap = SMap.getInstance();
            Workspace workspace = sMap.smMapWC.getMapControl().getMap().getWorkspace();
            Datasource opendatasource = workspace.getDatasources().get("Label_" + userpath + "#");

            WritableMap map = Arguments.createMap();

            boolean isEditable = false;
            if (opendatasource != null) {
                Datasets datasets = opendatasource.getDatasets();
                Layers layers = sMap.smMapWC.getMapControl().getMap().getLayers();

                Layer layer = null;
                for (int i = 0; i < layers.getCount(); i++) {
                    layer = layers.get(i);
                    for (int j = 0; j < datasets.getCount(); j++) {
                        Dataset dataset = datasets.get(j);
                        if (layer.getDataset() == dataset) {
                            if (layer.isEditable()) {
                                isEditable = true;
                                break;
                            }
                        }
                    }
                    if (isEditable) break;
                }
                map.putBoolean("isTaggingLayer", isEditable);
                if (isEditable) {
                    WritableMap layerInfo = Arguments.createMap();
                    layerInfo.putBoolean("isEditable", isEditable);
                    layerInfo.putBoolean("isVisible", layer.isVisible());
                    layerInfo.putString("name", layer.getName());
                    layerInfo.putString("datasetName", layer.getDataset().getName());
                    layerInfo.putString("description", layer.getDescription());
                    layerInfo.putString("datasetDescription", layer.getDataset().getDescription());

                    map.putMap("layerInfo", layerInfo);
                }
            } else {
                map.putBoolean("isTaggingLayer", isEditable);
            }
            promise.resolve(map);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 获取标注图层个数
     *
     * @param promise
     */
    @ReactMethod
    public void getTaggingLayerCount(String userpath, Promise promise) {
        try {
            sMap = SMap.getInstance();
            Workspace workspace = sMap.smMapWC.getMapControl().getMap().getWorkspace();
            Datasource opendatasource = workspace.getDatasources().get("Label_" + userpath + "#");
            if (opendatasource != null) {
                Datasets datasets = opendatasource.getDatasets();
                int count = datasets.getCount();
                promise.resolve(count);
            } else {
                promise.resolve(0);
            }
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 获取当前标注
     *
     * @param promise
     */
    @ReactMethod
    public void getCurrentTaggingDataset(String name, Promise promise) {
        try {
            sMap = SMap.getInstance();
            com.supermap.mapping.Map map = sMap.smMapWC.getMapControl().getMap();
            String datasetname = "";
            Layer layer = map.getLayers().get(name);
            layer.setVisible(true);
            layer.setEditable(true);
            map.refresh();
            datasetname = layer.getDataset().getName();
            promise.resolve(datasetname);
        } catch (Exception e) {
            promise.reject(e);
        }
    }


    /**
     * 获取标注图层
     *
     * @param promise
     */
    @ReactMethod
    public void getTaggingLayers(String userpath, Promise promise) {
        try {
            sMap = SMap.getInstance();
            Workspace workspace = sMap.smMapWC.getMapControl().getMap().getWorkspace();
            Datasource opendatasource = workspace.getDatasources().get("Label_" + userpath + "#");
            WritableArray arr = Arguments.createArray();
            if (opendatasource == null) {
                DatasourceConnectionInfo info = new DatasourceConnectionInfo();
                info.setAlias("Label_" + userpath + "#");
                info.setEngineType(EngineType.UDB);
                info.setServer(rootPath + "/iTablet/User/" + userpath + "/Data/Datasource/Label_" + userpath + "#.udb");
                Datasource datasource = workspace.getDatasources().open(info);

                if (datasource != null) {
                    Datasets datasets = datasource.getDatasets();
                    com.supermap.mapping.Map map = sMap.smMapWC.getMapControl().getMap();
                    Layers layers = map.getLayers();

                    for (int i = 0; i < datasets.getCount(); i++) {
                        Dataset ds = datasets.get(i);
                        for (int j = 0; j < layers.getCount(); j++) {
                            Layer layer = layers.get(j);
                            if (layer.getDataset() == ds) {
                                WritableMap writeMap = SMLayer.getLayerInfo(layer, "");
                                arr.pushMap(writeMap);
                            }
                        }
                    }
                }
            } else {
                Datasets datasets = opendatasource.getDatasets();
                com.supermap.mapping.Map map = sMap.smMapWC.getMapControl().getMap();
                Layers layers = map.getLayers();

                for (int i = 0; i < datasets.getCount(); i++) {
                    Dataset ds = datasets.get(i);
                    for (int j = 0; j < layers.getCount(); j++) {
                        Layer layer = layers.get(j);
                        if (layer.getDataset() == ds) {
                            WritableMap writeMap = SMLayer.getLayerInfo(layer, "");
                            arr.pushMap(writeMap);
                        }
                    }
                }
            }
            promise.resolve(arr);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 添加数据集属性字段
     *
     * @param promise
     */
    @ReactMethod
    public void addRecordset(String datasourceName, String datasetName, String filedInfoName, String value, String userpath, Promise promise) {
        try {
            sMap = SMap.getInstance();
            Workspace workspace = sMap.smMapWC.getMapControl().getMap().getWorkspace();
            Datasource opendatasource = workspace.getDatasources().get(datasourceName);
            if (opendatasource == null) {
                DatasourceConnectionInfo info = new DatasourceConnectionInfo();
                info.setAlias(datasourceName);
                info.setEngineType(EngineType.UDB);
                info.setServer(rootPath + "/iTablet/User/" + userpath + "/Data/Datasource/" + datasourceName + ".udb");
                Datasource datasource = workspace.getDatasources().open(info);
                if (datasource != null) {
                    Datasets datasets = datasource.getDatasets();
                    DatasetVector dataset = (DatasetVector) datasets.get(datasetName);
                    modifyLastAttribute(dataset, filedInfoName, value);
                }
                sMap.smMapWC.getMapControl().getMap().refresh();
                promise.resolve(true);
            } else {
                Datasets datasets = opendatasource.getDatasets();
                DatasetVector dataset = (DatasetVector) datasets.get(datasetName);
                modifyLastAttribute(dataset, filedInfoName, value);
                sMap.smMapWC.getMapControl().getMap().refresh();
                promise.resolve(true);
            }
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    // 修改最新的属性值
    private void modifyLastAttribute(Dataset dataset, String filedInfoName, String value) {
        if (dataset == null) {
            return;
        }
        if (filedInfoName == null) {
            return;
        }
        if (value == null || value.isEmpty()) {
            return;
        }

        DatasetVector dtVector = (DatasetVector) dataset;
        Recordset recordset = dtVector.getRecordset(false, CursorType.DYNAMIC);
        if (recordset == null) {
            return;
        }
        recordset.moveLast();
        recordset.edit();

        //the dataset didn't have '' fieldinfo
        FieldInfos fieldInfos = recordset.getFieldInfos();
        if (fieldInfos.indexOf(filedInfoName) == -1) {
            return;
        }
        recordset.setFieldValue(filedInfoName, value);

        recordset.update();
        recordset.close();
        recordset.dispose();
    }

    /**
     * 设置当前图层全副
     *
     * @param promise
     */
    @ReactMethod
    public void setLayerFullView(String name, Promise promise) {
        try {
            sMap = SMap.getInstance();
            Layer layer = SMLayer.findLayerByPath(name);
//            Layer layer = sMap.getSmMapWC().getMapControl().getMap().getLayers().get(name);
            Rectangle2D bounds = layer.getDataset().getBounds();

            if (!safeGetType(layer.getDataset().getPrjCoordSys(), sMap.smMapWC.getMapControl().getMap().getPrjCoordSys())) {
                Point2Ds point2Ds = new Point2Ds();
                point2Ds.add(new Point2D(bounds.getLeft(), bounds.getTop()));
                point2Ds.add(new Point2D(bounds.getRight(), bounds.getBottom()));
                PrjCoordSys prjCoordSys = new PrjCoordSys();
//                prjCoordSys.setType(PrjCoordSysType.PCS_EARTH_LONGITUDE_LATITUDE);
                prjCoordSys.setType(layer.getDataset().getPrjCoordSys().getType());
                CoordSysTransParameter parameter = new CoordSysTransParameter();

                CoordSysTranslator.convert(point2Ds, prjCoordSys, sMap.smMapWC.getMapControl().getMap().getPrjCoordSys(), parameter, CoordSysTransMethod.MTH_GEOCENTRIC_TRANSLATION);
                Point2D pt1 = point2Ds.getItem(0);
                Point2D pt2 = point2Ds.getItem(1);

                bounds = new Rectangle2D(pt1.getX(), pt2.getY(), pt2.getX(), pt1.getY());
            }
            if (bounds.getWidth() <= 0 || bounds.getHeight() <= 0) {
                sMap.getSmMapWC().getMapControl().getMap().setCenter(bounds.getCenter());
            } else {
                sMap.getSmMapWC().getMapControl().getMap().setViewBounds(bounds);
                sMap.getSmMapWC().getMapControl().zoomTo(sMap.getSmMapWC().getMapControl().getMap().getScale() * 0.8, 200);
            }

            sMap.getSmMapWC().getMapControl().getMap().refresh();

            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 设置最小比例尺范围
     *
     * @param promise
     */
    @ReactMethod
    public void setMinVisibleScale(String name, double number, Promise promise) {
        try {
            sMap = SMap.getInstance();
            Layer layer = SMLayer.findLayerByPath(name);
            double scale = 1 / number;
            layer.setMinVisibleScale(scale);
            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 设置最小比例尺范围
     *
     * @param promise
     */
    @ReactMethod
    public void setMaxVisibleScale(String name, double number, Promise promise) {
        try {
            sMap = SMap.getInstance();
            Layer layer = SMLayer.findLayerByPath(name);//sMap.getSmMapWC().getMapControl().getMap().getLayers().get(name);
            double scale = 1 / number;
            layer.setMaxVisibleScale(scale);
            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 添加文字标注
     *
     * @param promise
     */
    @ReactMethod
    public void addTextRecordset(String datasourceName, String datasetName, String name, int x, int y, Promise promise) {
        try {
            sMap = SMap.getInstance();
            Point2D p = sMap.smMapWC.getMapControl().getMap().pixelToMap(new Point(x, y));
            Workspace workspace = sMap.smMapWC.getMapControl().getMap().getWorkspace();
            Datasource opendatasource = workspace.getDatasources().get(datasourceName);
            Datasets datasets = opendatasource.getDatasets();
            DatasetVector dataset = (DatasetVector) datasets.get(datasetName);
            if (dataset != null) {
                dataset.setReadOnly(false);
            }
            Recordset recordset = dataset.getRecordset(false, CursorType.DYNAMIC);
            TextPart textPart = new TextPart();
            textPart.setAnchorPoint(p);
            textPart.setText(name);
            GeoText geoText = new GeoText();
            geoText.addPart(textPart);
            recordset.addNew(geoText);
            recordset.update();
            int id[] = new int[1];
            id[0] = recordset.getID();
            recordset.close();
            geoText.dispose();
            recordset.dispose();
            Recordset recordset1 = dataset.query(id, CursorType.DYNAMIC);
            sMap.smMapWC.getMapControl().getEditHistory().batchBegin();
            sMap.smMapWC.getMapControl().getEditHistory().addHistoryType(EditHistoryType.ADDNEW, recordset1, true);
            sMap.smMapWC.getMapControl().getEditHistory().batchEnd();
            recordset1.close();
            recordset1.dispose();
            sMap.smMapWC.getMapControl().getMap().refresh();
            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 获取点击坐标
     *
     * @param promise
     */
    @ReactMethod
    public void getGestureDetector(final Promise promise) {
        try {
            sMap = SMap.getInstance();
            final float[] x = new float[1];
            final float[] y = new float[1];
            sMap.smMapWC.getMapControl().setGestureDetector(new GestureDetector(context, new GestureDetector.OnGestureListener() {
                @Override
                public boolean onDown(MotionEvent e) {
                    return false;
                }

                @Override
                public void onShowPress(MotionEvent e) {

                }

                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    x[0] = e.getX();
                    y[0] = e.getY();
                    WritableMap writeMap = Arguments.createMap();
                    writeMap.putDouble("x", x[0]);
                    writeMap.putDouble("y", y[0]);
                    promise.resolve(writeMap);
                    sMap.smMapWC.getMapControl().deleteGestureDetector();

                    return true;
                }

                @Override
                public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                    return false;
                }

                @Override
                public void onLongPress(MotionEvent e) {

                }

                @Override
                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                    return false;
                }
            }));
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    private GeometryAddedListener delegate = null;

    /**
     * 设置标注面随机色
     *
     * @param promise
     */
    @ReactMethod
    public void setTaggingGrid(String name, String userpath, Promise promise) {
        try {
            sMap = SMap.getInstance();
            MapControl mapControl = sMap.smMapWC.getMapControl();
            Workspace workspace = sMap.smMapWC.getMapControl().getMap().getWorkspace();
            if (delegate == null) {
                delegate = new GeometryAddedListener() {
                    @Override
                    public void geometryAdded(GeometryEvent event) {
                        int id[] = new int[1];
                        id[0] = event.getID();
                        DatasetVector dataset = (DatasetVector) event.getLayer().getDataset();
                        Recordset recordset = dataset.query(id, CursorType.DYNAMIC);
                        if (recordset != null) {
                            recordset.moveFirst();
                            recordset.edit();
                            Geometry geometry = recordset.getGeometry();
                            if (geometry != null) {

                                GeoStyle geoStyle = new GeoStyle();
                                geoStyle.setFillForeColor(SMap.getFillColor());
                                geoStyle.setFillBackColor(SMap.getFillColor());
                                geoStyle.setMarkerSize(new Size2D(10, 10));
                                geoStyle.setLineColor(new Color(80, 80, 80));
                                geoStyle.setFillOpaqueRate(50);//加透明度更美观
                                geometry.setStyle(geoStyle);
                                recordset.setGeometry(geometry);
                                recordset.update();
                            }
                            recordset.dispose();
                        }
                        sMap.smMapWC.getMapControl().removeGeometryAddedListener(delegate);
                        delegate = null;
                    }
                };
            }
            mapControl.addGeometryAddedListener(delegate);

            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 设置MapControl 画笔样式
     *
     * @param style
     * @param promise
     */
    @ReactMethod
    public void setMapControlStyle(ReadableMap style, Promise promise) {
        try {
            sMap = SMap.getInstance();
            MapControl mapControl = sMap.smMapWC.getMapControl();

            if (style.hasKey("nodeStyle")) {
                String nodeStyleJson = style.getString("nodeStyle");
                GeoStyle nodeStyle = new GeoStyle();
                nodeStyle.fromJson(nodeStyleJson);
                mapControl.setNodeStyle(nodeStyle);
            }

            if (style.hasKey("nodeColor")) {
                int nodeColor = style.getInt("nodeColor");
                mapControl.setNodeColor(nodeColor);
            }

            if (style.hasKey("nodeSize")) {
                double nodeSize = style.getDouble("nodeSize");
                mapControl.setNodeSize(nodeSize);
            }

            if (style.hasKey("strokeColor")) {
                int strokeColor = style.getInt("strokeColor");
                mapControl.setStrokeColor(strokeColor);
            }

            if (style.hasKey("strokeWidth")) {
                int strokeWidth = style.getInt("strokeWidth");
                mapControl.setStrokeWidth(strokeWidth);
            }

            if (style.hasKey("strokeFillColor")) {
                int strokeFillColor = style.getInt("strokeFillColor");
                mapControl.setStrokeFillColor(strokeFillColor);
            }

            if (style.hasKey("objectColor")) {
                int objectColor = style.getInt("objectColor");
                mapControl.setObjectColor(objectColor);
            }

            if (style.hasKey("objectWidth")) {
                double objectWidth = style.getDouble("objectWidth");
                mapControl.setObjectWidth(objectWidth);
            }

            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 设置标注默认的结点，线，面颜色
     *
     * @param promise
     */
    @ReactMethod
    public void setLabelColor(/*ReadableMap readableMap,*/ Promise promise) {
        try {
            sMap = SMap.getInstance();
            MapControl mapControl = sMap.smMapWC.getMapControl();
//            mapControl.setStrokeColor(0x3999FF);
            mapControl.setStrokeColor(0x3999FF);
//            mapControl.setStrokeFillColor();
            mapControl.setStrokeWidth(1);

            GeoStyle geoStyle_P = new GeoStyle();
//            geoStyle_P.setMarkerAngle(14.0);
//            geoStyle_P.setFillForeColor(new Color(0,133,255));
//            geoStyle_P.setLineColor(new Color(0,133,255));
//            geoStyle_P.setMarkerSize(new Size2D(10, 10));
//            geoStyle_P.setPointColor(new Color(0,133,255));
//            geoStyle_P.setMarkerSymbolID(322);
//            mapControl.setNodeStyle(geoStyle_P);

            Workspace workspace = mapControl.getMap().getWorkspace();
            Resources m_resources = workspace.getResources();
            SymbolMarkerLibrary symbol_M = m_resources.getMarkerLibrary();
            if (symbol_M.contains(322)) {
                geoStyle_P.setMarkerSymbolID(322);
                mapControl.setNodeStyle(geoStyle_P);
            } else if (symbol_M.contains(313)) {
                geoStyle_P.setMarkerSymbolID(313);
                mapControl.setNodeStyle(geoStyle_P);
            } else if (symbol_M.contains(321)) {
                geoStyle_P.setMarkerSymbolID(321);
                mapControl.setNodeStyle(geoStyle_P);
            } else {
                mapControl.setNodeColor(0x3999FF);
                mapControl.setNodeSize(2.0);
            }

            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }


//    /**
//     * 更新图例
//     *
//     * @param promise
//     */
//    @ReactMethod
//    public void updateLegend(Promise promise){
//        try {
//            sMap = SMap.getInstance();
//            MapControl mapControl = sMap.smMapWC.getMapControl();
//
//            Layers layers = mapControl.getMap().getLayers();
//            ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();
//
//            for (int i = 0; i < layers.getCount(); i++) {
//                Layer layer = layers.get(i);
//                if (layer.getTheme() != null) {
//                    if (layer.getTheme().getType() == ThemeType.RANGE) {
//                        ThemeRange themeRange = (ThemeRange) layer.getTheme();
//                        for (int j = 0; j < themeRange.getCount(); j++) {
//                            GeoStyle GeoStyle = themeRange.getItem(j).getStyle();
////                        map.put(themeRange.getItem(j).getCaption(), GeoStyle.getFillForeColor().toColorString());
//
//                            HashMap<String, String> map = new HashMap<String, String>();
//                            map.put("Caption", themeRange.getItem(j).getCaption());
//                            map.put("Color", GeoStyle.getFillForeColor().toColorString());
//                            arrayList.add(map);
//                        }
//                    }
//                }
//            }
//
//            Legend lengend = mapControl.getMap().getCreateLegend();
//            if(lengend!=null){
//                lengend.dispose();
//
//                for (int i = 0; i < arrayList.size(); i++) {
//                    HashMap<String, String> hashMap = arrayList.get(i);
//                    String caption = hashMap.get("Caption");
//                    String colorString = hashMap.get("Color");
//
//                    int color = android.graphics.Color.parseColor(colorString);
////                ColorLegendItem colorLegendItem = new ColorLegendItem();
////                colorLegendItem.setColor(color);
////                colorLegendItem.setCaption(caption);
////                lengend.addColorLegendItem(2, colorLegendItem);
//
//                    LegendItem legendItem = new LegendItem();
//                    legendItem.setColor(color);
//                    legendItem.setCaption(caption);
//                    lengend.addUserDefinedLegendItem(legendItem);
//                }
//                mapControl.getMap().refresh();
//            }
//            promise.resolve(true);
//
//        } catch (Exception e) {
//            promise.reject(e);
//        }
//    }


    /**
     * 标绘动画
     *
     * @param promise
     */
    @ReactMethod
    public void plotAnimation(Promise promise) {

    }

    /**
     * 初始化标绘符号库
     *
     * @param plotSymbolPaths 标号路径列表
     * @param isFirst         是否是第一次初始化，第一次初始化需要新建一个点标号再删掉
     * @param newName         创建默认地图的地图名
     * @param isDefaultNew    是否是创建默认地图，创建默认地图不能从mapControl获取地图名，地图名由参数newName传入
     * @param promise
     */
    @ReactMethod
    public void initPlotSymbolLibrary(ReadableArray plotSymbolPaths, boolean isFirst, String newName, boolean isDefaultNew, Promise promise) {
        try {
            sMap = SMap.getInstance();
            final MapControl mapControl = sMap.smMapWC.getMapControl();

            Dataset dataset = null;
            Layer cadLayer = null;
            String userpath = null, name = "PlotEdit_" + (isDefaultNew ? newName : mapControl.getMap().getName());
            if (plotSymbolPaths.size() > 0) {
                String[] strArr = plotSymbolPaths.getString(0).split("/");
                for (int index = 0; index < strArr.length; index++) {
                    if (strArr[index].equals("User") && (index + 1) < strArr.length) {
                        userpath = strArr[index + 1];
                        break;
                    }
                }
            }

//            String plotDatasourceName="Plotting_" + userpath + "#";
            String plotDatasourceName = "Plotting_" + name + "#";
            plotDatasourceName.replace(".", "");
            Workspace workspace = mapControl.getMap().getWorkspace();
            Datasource opendatasource = workspace.getDatasources().get(plotDatasourceName);
            Datasource datasource = null;
            if (opendatasource == null) {
                DatasourceConnectionInfo info = new DatasourceConnectionInfo();
                info.setAlias(plotDatasourceName);
                info.setEngineType(EngineType.UDB);
                String server = rootPath + "/iTablet/User/" + userpath + "/Data/Datasource/" + plotDatasourceName + ".udb";
                info.setServer(server);

                datasource = workspace.getDatasources().open(info);
                if (datasource == null) {
                    String serverUDD = rootPath + "/iTablet/User/" + userpath + "/Data/Datasource/" + plotDatasourceName + ".udd";
                    info.setServer(server);
                    File file = new File(server);
                    if (file.exists()) {
                        file.delete();
                    }
                    File fileUdd = new File(serverUDD);
                    if (fileUdd.exists()) {
                        fileUdd.delete();
                    }
                    datasource = workspace.getDatasources().create(info);
                }
                if (datasource == null) {
                    datasource = workspace.getDatasources().open(info);
                }
                info.dispose();
            } else {
                datasource = opendatasource;
            }

            if (datasource == null) {
                promise.resolve(null);
                return;
            }
            Datasets datasets = datasource.getDatasets();

            for (int i = 0; i < mapControl.getMap().getLayers().getCount(); i++) {
                Layer tempLayer = mapControl.getMap().getLayers().get(i);
                if (tempLayer.getName().startsWith("PlotEdit_") && tempLayer.getDataset() != null) {
                    if (tempLayer.getDataset().getType() == DatasetType.CAD) {
                        dataset = tempLayer.getDataset();
                        cadLayer = tempLayer;
                    }
                } else {
                    tempLayer.setEditable(false);
                }
            }
            DatasetVector datasetVector;
            String datasetName;
            if (dataset == null) {
                datasetName = datasets.getAvailableDatasetName(name);
                DatasetVectorInfo datasetVectorInfo = new DatasetVectorInfo();
                datasetVectorInfo.setType(DatasetType.CAD);
                datasetVectorInfo.setEncodeType(EncodeType.NONE);
                datasetVectorInfo.setName(datasetName);
                datasetVector = datasets.create(datasetVectorInfo);
                //创建数据集时创建好字段
                addFieldInfo(datasetVector, "name", FieldType.TEXT, false, "", 255);
                addFieldInfo(datasetVector, "remark", FieldType.TEXT, false, "", 255);
                addFieldInfo(datasetVector, "address", FieldType.TEXT, false, "", 255);

                dataset = datasets.get(datasetName);
                com.supermap.mapping.Map map = sMap.smMapWC.getMapControl().getMap();
                Layer layer = map.getLayers().add(dataset, true);
                layer.setEditable(true);
                datasetVectorInfo.dispose();
                datasetVector.close();
            } else {
                cadLayer.setEditable(true);
//                Layers layers = sMap.smMapWC.getMapControl().getMap().getLayers();
////                Layer editLayer = layers.get(name + "@" + datasource.getAlias());
//                Layer editLayer = layers.get(dataset.getName());
//                if (editLayer != null) {
//                    editLayer.setEditable(true);
//                } else {
//
//                    Dataset ds = dataset;
//                    com.supermap.mapping.Map map = sMap.smMapWC.getMapControl().getMap();
//                    Layer layer = map.getLayers().add(ds, true);
//                    layer.setEditable(true);
//                }
            }


            WritableMap writeMap = Arguments.createMap();
            for (int i = 0; i < plotSymbolPaths.size(); i++) {
                int libId = (int) mapControl.addPlotLibrary(plotSymbolPaths.getString(i));
                String libName = mapControl.getPlotSymbolLibName((long) libId);
                writeMap.putInt(libName, libId);
//                if (isFirst && libName.equals("警用标号")) {
//                    Point2Ds point2Ds = new Point2Ds();
//                    Point2D point2D=new Point2D(mapControl.getMap().getViewBounds().getLeft()-100,mapControl.getMap().getViewBounds().getTop()-100);
//                    point2Ds.add(point2D);
//                    mapControl.addPlotObject(libId, 20100, point2Ds);
//                    mapControl.cancel();
//                    final Dataset finalDataset = dataset;
//                    new Thread() {
//                        @Override
//                        public void run() {
//                            super.run();
//                            try {
//                                Thread.sleep(100);
//                                Recordset recordset = ((DatasetVector) finalDataset).getRecordset(false, CursorType.DYNAMIC);
//                                recordset.moveLast();
//                                recordset.delete();
//                                recordset.update();
//                                recordset.dispose();
//                                mapControl.getMap().refresh();
//                                mapControl.setAction(Action.PAN);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }.start();
//                }
            }

            promise.resolve(writeMap);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 移除标绘库
     *
     * @param plotSymbolIds
     * @param promise
     */
    @ReactMethod
    public void removePlotSymbolLibraryArr(ReadableArray plotSymbolIds, Promise promise) {
        try {
            sMap = SMap.getInstance();
            MapControl mapControl = sMap.smMapWC.getMapControl();
            for (int i = 0; i < plotSymbolIds.size(); i++) {
                mapControl.removePlotLibrary(plotSymbolIds.getInt(i));
            }
            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 设置标绘符号
     *
     * @param promise
     */
    @ReactMethod
    public void setPlotSymbol(int libID, int symbolCode, Promise promise) {
        try {
            sMap = SMap.getInstance();
            MapControl mapControl = sMap.smMapWC.getMapControl();

            for (int i = 0; i < mapControl.getMap().getLayers().getCount(); i++) {
                Layer tempLayer = mapControl.getMap().getLayers().get(i);
                if (tempLayer.getName().startsWith("PlotEdit_") && tempLayer.getDataset() != null) {
                    if (tempLayer.getDataset().getType() == DatasetType.CAD) {
                        tempLayer.setEditable(true);
                    }
                } else {
                    tempLayer.setEditable(false);
                }
            }

            mapControl.setPlotSymbol(libID, symbolCode);
            mapControl.setAction(Action.CREATEPLOT);

            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 添加cad图层
     *
     * @param layerName
     * @param promise
     */
    @ReactMethod
    public void addCadLayer(String layerName, Promise promise) {
        try {
            sMap = SMap.getInstance();
            MapControl mapControl = sMap.smMapWC.getMapControl();
            Layer cadLayer = mapControl.getMap().getLayers().get(layerName);
            if (cadLayer == null) {
                DatasetVectorInfo datasetVectorInfo = new DatasetVectorInfo();
                datasetVectorInfo.setType(DatasetType.CAD);
                datasetVectorInfo.setName(layerName);
                DatasetVector datasetVector = (DatasetVector) sMap.smMapWC.getWorkspace().getDatasources().get(0).getDatasets().get(layerName);
                if (datasetVector == null) {
                    datasetVector = sMap.smMapWC.getWorkspace().getDatasources().get(0).getDatasets().create(datasetVectorInfo);
                }
                cadLayer = mapControl.getMap().getLayers().add(datasetVector, true);
            }
            cadLayer.setEditable(true);
            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 导入标绘模板库
     *
     * @param fromPath
     */
    @ReactMethod
    public static void importPlotLibData(String fromPath, Promise promise) {
        try {
            promise.resolve(importPlotLibDataMethod(fromPath));
        } catch (Exception e) {
            promise.resolve(false);
        }
    }

    /**
     * 导入标绘模板库
     *
     * @param fromPath
     */
    public static boolean importPlotLibDataMethod(String fromPath) {
        String toPath = homeDirectory + "/iTablet/User/" + SMap.getInstance().smMapWC.getUserName() + "/Data" + "/Plotting/";
        boolean result = copyFiles(fromPath, toPath, "plot", "Symbol", "SymbolIcon", false);
        return result;
    }

    /**
     * 根据标绘库id获取标绘库名称
     *
     * @param libId
     */
    @ReactMethod
    public static void getPlotSymbolLibNameById(int libId, Promise promise) {
        try {

            sMap = SMap.getInstance();
            MapControl mapControl = sMap.smMapWC.getMapControl();
            String libName = mapControl.getPlotSymbolLibName((long) libId);
            promise.resolve(libName);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    private static Timer m_timer;
//    private static AnimationManager am;

    /**
     * 初始化态势推演
     */
    @ReactMethod
    public static void initAnimation(Promise promise) {
        try {

            sMap = SMap.getInstance();
            MapControl mapControl = sMap.smMapWC.getMapControl();
//            am = AnimationManager.getInstance();
            //开启定时器
            if (m_timer == null) {
                m_timer = new Timer();
            }
            m_timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    AnimationManager.getInstance().excute();
                }
            }, 0, 100);
            mapControl.setAnimations();
            promise.resolve(true);
        } catch (Exception e) {
            promise.resolve(false);
        }

    }

    /**
     * 读取态势推演xml文件
     */
    @ReactMethod
    public static void readAnimationXmlFile(String filePath, Promise promise) {
        try {
            sMap = SMap.getInstance();
            MapControl mapControl = sMap.smMapWC.getMapControl();

            if (m_timer == null) {
                m_timer = new Timer();
            }
            //开启定时器
            m_timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    AnimationManager.getInstance().excute();
                }
            }, 0, 100);
            Layers layers = mapControl.getMap().getLayers();
            int count = layers.getCount();
            for (int i = 0; i < count; i++) {
                if (layers.get(i).getDataset().getType() == DatasetType.CAD) {
                    layers.get(i).setEditable(true);
                }
            }

            mapControl.setAnimations();
            AnimationManager.getInstance().deleteAll();
            AnimationManager.getInstance().getAnimationFromXML(filePath);


//            开始推演时定位到推演图层，获取的推演图层范围有错误
//            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//            try{
//                DocumentBuilder db = dbf.newDocumentBuilder();
//                Document document = db.parse("file:///"+filePath);
//                NodeList booklist = document.getElementsByTagName("LAYERNAME");
//                if(booklist.getLength()>0){
//                    Element element= (Element) booklist.item(0);
//                    if(element.getChildNodes().getLength()>0) {
//                        String layerName=element.getChildNodes().item(0).getNodeValue();
//                        Layer layer=mapControl.getMap().getLayers().get(layerName);
//                        if(layer!=null) {
//                            mapControl.getMap().setViewBounds(layer.getDataset().getBounds());
//                            mapControl.getMap().refresh();
//                        }
//                    }
//                }
//            }catch (ParserConfigurationException e){
//                e.printStackTrace();
//            }catch (IOException e){
//                e.printStackTrace();
//            }


            promise.resolve(true);
        } catch (Exception e) {
            promise.resolve(false);
        }
    }

    /**
     * 播放态势推演动画
     */
    @ReactMethod
    public static void animationPlay(Promise promise) {
        try {
            sMap = SMap.getInstance();
            MapControl mapControl = sMap.smMapWC.getMapControl();
//            double scale = mapControl.getMap().getScale();
//            mapControl.zoomTo(mapControl.getMap().getScale()+0.1,100);
////            mapControl.getMap().setScale( mapControl.getMap().getScale()+0.1);
//            mapControl.getMap().refresh();
//            mapControl.zoomTo(scale,100);
////            mapControl.getMap().setScale( scale);
//            mapControl.getMap().refresh();

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    AnimationManager.getInstance().play();
                }
            }, 0);//3秒后执行Runnable中的run方法
            promise.resolve(true);
        } catch (Exception e) {
            promise.resolve(false);
        }
    }

    /**
     * 暂停态势推演动画
     */
    @ReactMethod
    public static void animationPause(Promise promise) {
        try {
            AnimationManager.getInstance().pause();

            promise.resolve(true);
        } catch (Exception e) {
            promise.resolve(false);
        }
    }

    /**
     * 复位态势推演动画
     */
    @ReactMethod
    public static void animationReset(Promise promise) {
        try {
            AnimationManager.getInstance().reset();

            promise.resolve(true);
        } catch (Exception e) {
            promise.resolve(false);
        }
    }

    /**
     * 停止态势推演动画
     */
    @ReactMethod
    public static void animationStop(Promise promise) {
        try {
            AnimationManager.getInstance().stop();

            promise.resolve(true);
        } catch (Exception e) {
            promise.resolve(false);
        }
    }

    /**
     * 关闭态势推演
     */
    @ReactMethod
    public static void animationClose(Promise promise) {
        try {
            m_timer.cancel();
            m_timer = null;
            AnimationManager.getInstance().stop();
            AnimationManager.getInstance().reset();
            AnimationManager.getInstance().deleteAll();
            promise.resolve(true);
        } catch (Exception e) {
            promise.resolve(false);
        }
    }

    private Point2Ds point2Ds;
    private Point2Ds savePoint2Ds;

    /**
     * 创建推演动画对象
     */
    @ReactMethod
    public void createAnimationGo(ReadableMap createInfo, String newPlotMapName, Promise promise) {
        //顺序：路径、闪烁、属性、显隐、旋转、比例、生长
        try {
            if (!createInfo.hasKey("animationMode")) {
                promise.resolve(false);
                return;
            }
            sMap = SMap.getInstance();
            MapControl mapControl = sMap.smMapWC.getMapControl();

            String animationGroupName = "Create_Animation_Instance_#"; //默认创建动画分组的名称，名称特殊一点，保证唯一
            AnimationGroup animationGroup = AnimationManager.getInstance().getGroupByName(animationGroupName);
            if (animationGroup == null) {
                animationGroup = AnimationManager.getInstance().addAnimationGroup(animationGroupName);
            }

            int animationMode = createInfo.getInt("animationMode");
            AnimationGO animationGO = AnimationManager.getInstance().createAnimation(new AnimationDefine.AnimationType(animationMode, animationMode));
            switch (animationMode) {
                case 0:
                    AnimationWay animationWay = (AnimationWay) animationGO;
                    Point3Ds point3Ds = new Point3Ds();
                    if (createInfo.hasKey("wayPoints")) {
                        ReadableArray array = createInfo.getArray("wayPoints");
                        for (int i = 0; i < array.size(); i++) {
                            ReadableMap map = array.getMap(i);
                            double x = map.getDouble("x");
                            double y = map.getDouble("y");
                            point3Ds.add(new Point3D(x, y, 0));
                        }
                    }
                    animationWay.addPathPts(point3Ds);
                    animationWay.setTrackLineWidth(0.5);
                    animationWay.setPathType(AnimationDefine.PathType.POLYLINE);
                    animationWay.setTrackLineColor(new com.supermap.data.Color(255, 0, 0, 255));
                    animationWay.setPathTrackDir(true);
                    animationWay.showPathTrack(true);
                    animationGO = animationWay;
                    break;
                case 1:
                    AnimationBlink animationBlink = (AnimationBlink) animationGO;
                    animationBlink.setBlinkNumberofTimes(20);
                    animationBlink.setBlinkStyle(AnimationDefine.BlinkAnimationBlinkStyle.NumberBlink);
                    animationBlink.setReplaceStyle(AnimationDefine.BlinkAnimationReplaceStyle.ColorReplace);
                    animationBlink.setBlinkAnimationReplaceColor(new com.supermap.data.Color(0, 0, 255, 255));
                    animationGO = animationBlink;
                    break;
                case 2:
                    AnimationAttribute animationAttribute = (AnimationAttribute) animationGO;
                    animationAttribute.setStartLineColor(new com.supermap.data.Color(255, 0, 0, 255));
                    animationAttribute.setEndLineColor(new com.supermap.data.Color(0, 0, 255, 255));
                    animationAttribute.setLineColorAttr(true);
                    animationAttribute.setStartLineWidth(0);
                    animationAttribute.setEndLineWidth(1);
                    animationAttribute.setLineWidthAttr(true);
                    animationGO = animationAttribute;
                    break;
                case 3:
                    AnimationShow animationShow = (AnimationShow) animationGO;
                    animationShow.setShowEffect(0);
                    animationShow.setShowState(true);
                    animationGO = animationShow;
                    break;
                case 4:
                    AnimationRotate animationRotate = (AnimationRotate) animationGO;
                    animationRotate.setStartAngle(new Point3D(0, 0, 0));
                    animationRotate.setEndAngle(new Point3D(720, 720, 0));
                    animationGO = animationRotate;
                    break;
                case 5:
                    AnimationScale animationScale = (AnimationScale) animationGO;
                    animationScale.setStartScaleFactor(0);
                    animationScale.setEndScaleFactor(1);
                    animationGO = animationScale;
                    break;
                case 6:
                    AnimationGrow animationGrow = (AnimationGrow) animationGO;
                    animationGrow.setStartLocation(0);
                    animationGrow.setEndLocation(1);
                    animationGO = animationGrow;
                    break;
            }
            //清空创建路径动画时的数据
            mapControl.getMap().getTrackingLayer().clear();
            point2Ds = null;
            savePoint2Ds = null;

            if (createInfo.hasKey("startTime") && animationGroup.getAnimationCount() > 0) {
                int startTime = createInfo.getInt("startTime");
                if (createInfo.hasKey("startMode")) {
                    int startMode = createInfo.getInt("startMode");
                    AnimationGO lastAnimationGo = animationGroup.getAnimationByIndex(animationGroup.getAnimationCount() - 1);
                    switch (startMode) {
                        case 1:         //上一动作播放之后
                            double lastEndTime = lastAnimationGo.getStartTime() + lastAnimationGo.getDuration();
                            startTime += lastEndTime;
                            break;
                        case 2:         //点击开始
                            break;
                        case 3:         //上一动作同时播放
                            double lastStartTime = lastAnimationGo.getStartTime();
                            startTime += lastStartTime;
                            break;
                    }
                }
                animationGO.setStartTime(startTime);
            } else if (createInfo.hasKey("startTime") && animationGroup.getAnimationCount() == 0) {
                int startTime = createInfo.getInt("startTime");
                animationGO.setStartTime(startTime);
            }
            if (createInfo.hasKey("durationTime")) {
                int durationTime = createInfo.getInt("durationTime");
                animationGO.setDuration(durationTime);
            }
            if (createInfo.hasKey("startMode")) {
                int startMode = createInfo.getInt("startMode");

            }

            String mapName = mapControl.getMap().getName();
            if (mapName == null || mapName.equals("")) {
                if (newPlotMapName != null && !newPlotMapName.equals("")) {
                    mapName = newPlotMapName;
                } else {
                    int layerCount = mapControl.getMap().getLayers().getCount();
                    if (layerCount > 0) {
                        mapName = mapControl.getMap().getLayers().get(layerCount - 1).getName();
                    }
                }
                mapControl.getMap().save(mapName);
            }


            String animationGoName = "动画_" + AnimationManager.getInstance().getGroupByName(animationGroupName).getAnimationCount();
            if (createInfo.hasKey("layerName") && createInfo.hasKey("geoId")) {
                String layerName = createInfo.getString("layerName");
                int geoId = createInfo.getInt("geoId");
                Layer layer = mapControl.getMap().getLayers().get(layerName);
                if (layer != null) {
                    DatasetVector dataset = (DatasetVector) mapControl.getMap().getLayers().get(layerName).getDataset();
                    Recordset recordset = dataset.query("SmID=" + geoId, CursorType.STATIC);
                    Geometry geometry = recordset.getGeometry();
                    if (geometry != null) {
                        animationGO.setName(animationGoName);
//                            String name=mapControl.getMap().getName();
//                            if(name==null||name.equals("")){
//                                mapControl.getMap().save();
//                            }
                        animationGO.setGeometry((GeoGraphicObject) geometry, mapControl.getHandle(), layer.getName());
                        animationGroup.addAnimation(animationGO);
                    }
                }
            }

            promise.resolve(true);
        } catch (Exception e) {
            promise.resolve(false);
        }
    }

    /**
     * 保存推演动画
     */
    @ReactMethod
    public static void animationSave(String savePath, String fileName, Promise promise) {
        try {
//        String path=sdcard+"/supermap/demos/plotdata/qdwj/强渡乌江_2.xml";
            sMap = SMap.getInstance();
            MapControl mapControl = sMap.smMapWC.getMapControl();
            File file = new File(savePath);
            if (!file.exists()) {
                file.mkdirs();
            }
//            String mapName=mapControl.getMap().getName();
            String tempPath = savePath + "/" + fileName + ".xml";
            String path = SMFileUtil.formateNoneExistFileName(tempPath, false);
            boolean result = AnimationManager.getInstance().saveAnimationToXML(path);
            AnimationManager.getInstance().reset();
            AnimationManager.getInstance().deleteAll();


            promise.resolve(result);
        } catch (Exception e) {
            promise.resolve(false);
        }
    }

    /**
     * 获取标绘对象type
     */
    @ReactMethod
    public static void getGeometryTypeById(String layerName, int geoId, Promise promise) {
        try {
            sMap = SMap.getInstance();
            MapControl mapControl = sMap.smMapWC.getMapControl();

            int type = -1;
            Layer layer = mapControl.getMap().getLayers().get(layerName);
            if (layer != null) {
                DatasetVector dataset = (DatasetVector) mapControl.getMap().getLayers().get(layerName).getDataset();
                Recordset recordset = dataset.query("SmID=" + geoId, CursorType.STATIC);
                Geometry geometry = recordset.getGeometry();
                Geometry geometry1 = (GeoGraphicObject) geometry;
                if (geometry != null) {
                    GeoGraphicObject geoGraphicObject = (GeoGraphicObject) geometry;
                    GraphicObjectType graphicObjectType = geoGraphicObject.getSymbolType();
                    type = graphicObjectType.value();
                }
            }


            promise.resolve(type);
        } catch (Exception e) {
            promise.resolve(-1);
        }
    }


    /**
     * 添加路径动画点获取回退路径动画点
     *
     * @param point
     * @param promise
     */
    @ReactMethod
    public void addAnimationWayPoint(ReadableMap point, boolean isAdd, Promise promise) {
        try {
            sMap = SMap.getInstance();
            MapControl mapControl = sMap.smMapWC.getMapControl();

            if (!isAdd) {
                if (point2Ds == null || point2Ds.getCount() == 0) {
                    promise.resolve(false);
                    return;
                } else {
                    point2Ds.remove(point2Ds.getCount() - 1);
                }
            } else {
                Point point1 = new Point((int) point.getDouble("x"), (int) point.getDouble("y"));
                Point2D point2D = mapControl.getMap().pixelToMap(point1);
                if (point2Ds == null) {
                    point2Ds = new Point2Ds();
                }
                point2Ds.add(point2D);
            }
            GeoStyle style = new GeoStyle();
            style.setMarkerSize(new Size2D(10, 10));
            style.setLineColor(new Color(255, 105, 0));
            style.setMarkerSymbolID(3614);
            {

                if (point2Ds.getCount() == 0) {
                    mapControl.getMap().getTrackingLayer().clear();
                } else if (point2Ds.getCount() == 1) {
                    mapControl.getMap().getTrackingLayer().clear();
                    GeoPoint geoPoint = new GeoPoint(point2Ds.getItem(0));
                    geoPoint.setStyle(style);
                    mapControl.getMap().getTrackingLayer().add(geoPoint, "point");
                } else if (point2Ds.getCount() > 1) {
                    mapControl.getMap().getTrackingLayer().clear();
                    GeoLine geoLine = new GeoLine(point2Ds);
                    geoLine.setStyle(style);
                    mapControl.getMap().getTrackingLayer().add(geoLine, "line");
                }
                mapControl.getMap().refresh();
            }
            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 刷新路径动画点
     * @param promise
     */
    @ReactMethod
    public void refreshAnimationWayPoint(Promise promise) {
        try {
            sMap = SMap.getInstance();
            MapControl mapControl = sMap.smMapWC.getMapControl();

            if(savePoint2Ds==null||savePoint2Ds.getCount()==0){
                point2Ds=null;
                mapControl.getMap().getTrackingLayer().clear();
                promise.resolve(true);
                return;
            }
            point2Ds=new Point2Ds(savePoint2Ds);

            GeoStyle style = new GeoStyle();
            style.setMarkerSize(new Size2D(10, 10));
            style.setLineColor(new Color(255, 105, 0));
            style.setMarkerSymbolID(3614);
            {
                mapControl.getMap().getTrackingLayer().clear();
                if (point2Ds.getCount() == 1) {
                    GeoPoint geoPoint = new GeoPoint(point2Ds.getItem(0));
                    geoPoint.setStyle(style);
                    mapControl.getMap().getTrackingLayer().add(geoPoint, "point");
                } else if (point2Ds.getCount() > 1) {
                    GeoLine geoLine = new GeoLine(point2Ds);
                    geoLine.setStyle(style);
                    mapControl.getMap().getTrackingLayer().add(geoLine, "line");
                }
                mapControl.getMap().refresh();
            }
            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 结束添加路径动画
     * @param promise
     */
    @ReactMethod
    public void cancelAnimationWayPoint(Promise promise) {
        try {
            sMap = SMap.getInstance();
            MapControl mapControl = sMap.smMapWC.getMapControl();

            mapControl.getMap().getTrackingLayer().clear();
            point2Ds = null;
            savePoint2Ds = null;
            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 结束添加路径动画
     *
     * @param isSave
     * @param promise
     */
    @ReactMethod
    public void endAnimationWayPoint(boolean isSave, Promise promise) {
        try {
            sMap = SMap.getInstance();
            MapControl mapControl = sMap.smMapWC.getMapControl();

            if (!isSave) {
                AnimationManager.getInstance().deleteAll();
                mapControl.getMap().getTrackingLayer().clear();
                point2Ds = null;
                savePoint2Ds = null;
                promise.resolve(true);
                return;
            }

            WritableArray arr = Arguments.createArray();
            if (point2Ds.getCount() > 0) {
                for (int i = 0; i < point2Ds.getCount(); i++) {
                    WritableMap writeMap = Arguments.createMap();
                    Point2D point2D = point2Ds.getItem(i);
                    writeMap.putDouble("x", point2D.getX());
                    writeMap.putDouble("y", point2D.getY());
                    arr.pushMap(writeMap);
                }
            }
            savePoint2Ds=new Point2Ds(point2Ds);
            promise.resolve(arr);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 根据geoId获取对象已设置的动画类型数量
     *
     * @param geoId
     * @param promise
     */
    @ReactMethod
    public void getGeoAnimationTypes(int geoId, Promise promise) {
        try {
            sMap = SMap.getInstance();
            MapControl mapControl = sMap.smMapWC.getMapControl();


            int[] array=new int[7];
            WritableArray arr = Arguments.createArray();
            for (int i = 0; i < array.length; i++) {
                arr.pushInt(0);
            }

            String animationGroupName = "Create_Animation_Instance_#"; //默认创建动画分组的名称，名称特殊一点，保证唯一
            AnimationGroup animationGroup = AnimationManager.getInstance().getGroupByName(animationGroupName);
            if (animationGroup == null) {
                promise.resolve(arr);
                return;
            }

            int size=animationGroup.getAnimationCount();
            for (int i = 0; i < size; i++) {
                AnimationGO animationGO=animationGroup.getAnimationByIndex(i);
                int id=animationGO.getGeometry();
                if(id==geoId){
                    int type=animationGO.getAnimationType().value();
                    array[type]+=1;
                }
            }

            arr = Arguments.createArray();
            for (int i = 0; i < array.length; i++) {
                arr.pushInt(array[i]);
            }
            promise.resolve(arr);
        } catch (Exception e) {
            promise.reject(e);
        }
    }



/************************************** 地图编辑历史操作 BEGIN****************************************/

    /**
     * 把对地图操作记录到历史
     *
     * @param promise
     */
    @ReactMethod
    public void addMapHistory(Promise promise) {
        try {
            sMap = SMap.getInstance();
            MapControl mapControl = sMap.smMapWC.getMapControl();
            mapControl.getEditHistory().addMapHistory();
            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 获取地图操作记录数量
     *
     * @param promise
     */
    @ReactMethod
    public void getMapHistoryCount(Promise promise) {
        try {
            sMap = SMap.getInstance();
            MapControl mapControl = sMap.smMapWC.getMapControl();
            int count = mapControl.getEditHistory().getCount();

            promise.resolve(count);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 获取地图操作记录当前index
     *
     * @param promise
     */
    @ReactMethod
    public void getMapHistoryCurrentIndex(Promise promise) {
        try {
            sMap = SMap.getInstance();
            MapControl mapControl = sMap.smMapWC.getMapControl();
            int index = mapControl.getEditHistory().getCurrentIndex();

            promise.resolve(index);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 地图操作记录重做到index
     *
     * @param index
     * @param promise
     */
    @ReactMethod
    public void redoWithIndex(int index, Promise promise) {
        try {
            sMap = SMap.getInstance();
            MapControl mapControl = sMap.smMapWC.getMapControl();
            boolean result = mapControl.getEditHistory().redo(index);

            promise.resolve(result);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 地图操作记录撤销到index
     *
     * @param index
     * @param promise
     */
    @ReactMethod
    public void undoWithIndex(int index, Promise promise) {
        try {
            sMap = SMap.getInstance();
            MapControl mapControl = sMap.smMapWC.getMapControl();
            boolean result = mapControl.getEditHistory().undo(index);

            promise.resolve(result);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 地图操作记录移除两个index之间的记录
     *
     * @param start
     * @param end
     * @param promise
     */
    @ReactMethod
    public void removeRange(int start, int end, Promise promise) {
        try {
            sMap = SMap.getInstance();
            MapControl mapControl = sMap.smMapWC.getMapControl();
            boolean result = mapControl.getEditHistory().removeRange(start, end);

            promise.resolve(result);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 地图操作记录移除index位置的记录
     *
     * @param promise
     */
    @ReactMethod
    public void remove(int index, Promise promise) {
        try {
            sMap = SMap.getInstance();
            MapControl mapControl = sMap.smMapWC.getMapControl();
            boolean result = mapControl.getEditHistory().remove(index);

            promise.resolve(result);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 清除地图操作记录
     *
     * @param promise
     */
    @ReactMethod
    public void clear(int index, Promise promise) {
        try {
            sMap = SMap.getInstance();
            MapControl mapControl = sMap.smMapWC.getMapControl();
            boolean result = mapControl.getEditHistory().clear();

            promise.resolve(result);
        } catch (Exception e) {
            promise.reject(e);
        }
    }
    /************************************** 地图编辑历史操作 END ****************************************/

    /************************************** 地图设置开始 ****************************************/

    /**
     * 获取图例的宽度和title
     *
     * @param promise
     */
    @ReactMethod
    public void getScaleData(Promise promise) {
        try {
            if (scaleViewHelper == null) {
                getScaleViewHelper();
            }

            scaleViewHelper.mScaleLevel = scaleViewHelper.getScaleLevel();
            scaleViewHelper.mScaleText = scaleViewHelper.getScaleText(scaleViewHelper.mScaleLevel);
            scaleViewHelper.mScaleWidth = scaleViewHelper.getScaleWidth(scaleViewHelper.mScaleLevel);
            WritableMap map = Arguments.createMap();
            map.putDouble("width", scaleViewHelper.mScaleWidth);
            map.putString("title", scaleViewHelper.mScaleText);
            promise.resolve(map);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 获取地图旋转角度
     *
     * @param promise
     */
    @ReactMethod
    public void getMapAngle(Promise promise) {
        try {
            sMap = SMap.getInstance();
            double angle = sMap.smMapWC.getMapControl().getMap().getAngle();
            angle = new BigDecimal(angle).setScale(1, RoundingMode.UP).doubleValue();
            promise.resolve(angle);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 设置地图旋转角度
     *
     * @param angle
     * @param promise
     */
    @ReactMethod
    public void setMapAngle(double angle, Promise promise) {
        try {
            sMap = SMap.getInstance();
            sMap.smMapWC.getMapControl().getMap().setAngle(angle);
            sMap.smMapWC.getMapControl().getMap().refresh();
            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    @ReactMethod
    public void setMapSlantAngle(double angle, Promise promise) {
        try {
            sMap = SMap.getInstance();
            sMap.smMapWC.getMapControl().getMap().SetSlantAngle(angle);
            sMap.smMapWC.getMapControl().getMap().refresh();
            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 获取地图颜色模式
     *
     * @param promise
     */
    @ReactMethod
    public void getMapColorMode(Promise promise) {
        try {
            sMap = SMap.getInstance();
            MapColorMode colorMode = sMap.smMapWC.getMapControl().getMap().getColorMode();
            String color = colorMode.toString();
            promise.resolve(color);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 设置地图颜色模式
     *
     * @param mode
     * @param promise
     */
    @ReactMethod
    public void setMapColorMode(int mode, Promise promise) {
        try {
            sMap = SMap.getInstance();
            MapColorMode colorMode = MapColorMode.DEFAULT;
            switch (mode) {
                case 0:
                    colorMode = MapColorMode.DEFAULT;
                    break;
                case 1:
                    colorMode = MapColorMode.BLACKWHITE;
                    break;
                case 2:
                    colorMode = MapColorMode.GRAY;
                    break;
                case 3:
                    colorMode = MapColorMode.BLACK_WHITE_REVERSE;
                    break;
                case 4:
                    colorMode = MapColorMode.ONLY_BLACK_WHITE_REVERSE;
                    break;
            }
            sMap.smMapWC.getMapControl().getMap().setColorMode(colorMode);
            sMap.smMapWC.getMapControl().getMap().refresh();
            promise.resolve(true);
        } catch (Exception e) {
            promise.resolve(e);
        }
    }

    /**
     * 获取地图背景色
     *
     * @param promise
     */
    @ReactMethod
    public void getMapBackgroundColor(Promise promise) {
        try {
            sMap = SMap.getInstance();
            GeoStyle backgroundStyle = sMap.smMapWC.getMapControl().getMap().getBackgroundStyle();
            Color color = backgroundStyle.getFillForeColor();
            String r = Integer.toHexString(color.getR());
            String g = Integer.toHexString(color.getG());
            String b = Integer.toHexString(color.getB());
            r = r.length() == 1 ? "0" + r : r;
            g = g.length() == 1 ? "0" + g : g;
            b = b.length() == 1 ? "0" + b : b;
            String colorString = "#" + r + g + b;
            promise.resolve(colorString);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 设置地图背景色
     *
     * @param r
     * @param g
     * @param b
     * @param promise
     */
    @ReactMethod
    public void setMapBackgroundColor(int r, int g, int b, Promise promise) {
        try {
            sMap = SMap.getInstance();
            GeoStyle backgroundStyle = sMap.smMapWC.getMapControl().getMap().getBackgroundStyle();
            Color color = new Color(r, g, b);
            backgroundStyle.setFillForeColor(color);
            sMap.smMapWC.getMapControl().getMap().refresh();
            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 获取是否固定符号角度
     */
    @ReactMethod
    public void getMarkerFixedAngle(Promise promise) {
        try {
            sMap = SMap.getInstance();
            Boolean b = sMap.smMapWC.getMapControl().getMap().getIsMarkerFixedAngle();
            promise.resolve(b);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 设置是否固定符号角度
     *
     * @param b
     * @param promise
     */
    @ReactMethod
    public void setMarkerFixedAngle(Boolean b, Promise promise) {
        try {
            sMap = SMap.getInstance();
            sMap.smMapWC.getMapControl().getMap().setIsMarkerFixedAngle(b);
            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 获取是否固定文本角度
     *
     * @param promise
     */
    @ReactMethod
    public void getTextFixedAngle(Promise promise) {
        try {
            sMap = SMap.getInstance();
            Boolean b = sMap.smMapWC.getMapControl().getMap().getIsTextFixedAngle();
            promise.resolve(b);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 获取是否固定文本方向
     *
     * @param promise
     */
    @ReactMethod
    public void getFixedTextOrientation(Promise promise) {
        try {
            sMap = SMap.getInstance();
            Boolean b = sMap.smMapWC.getMapControl().getMap().getIsFixedTextOrientation();
            promise.resolve(b);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 设置是否固定文本角度
     *
     * @param b
     * @param promise
     */
    @ReactMethod
    public void setTextFixedAngle(Boolean b, Promise promise) {
        try {
            sMap = SMap.getInstance();
            sMap.smMapWC.getMapControl().getMap().setIsTextFixedAngle(b);
            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 设置是否固定文本方向
     *
     * @param b
     * @param promise
     */
    @ReactMethod
    public void setFixedTextOrientation(Boolean b, Promise promise) {
        try {
            sMap = SMap.getInstance();
            sMap.smMapWC.getMapControl().getMap().setIsFixedTextOrientation(b);
            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 获取放大镜是否开启
     *
     * @param promise
     */
    @ReactMethod
    public void isMagnifierEnabled(Promise promise) {
        try {
            sMap = SMap.getInstance();
            boolean b = sMap.smMapWC.getMapControl().isMagnifierEnabled();
            promise.resolve(b);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 设置放大镜是否开启
     *
     * @param b
     * @param promise
     */
    @ReactMethod
    public void setIsMagnifierEnabled(boolean b, Promise promise) {
        try {
            sMap = SMap.getInstance();
            sMap.smMapWC.getMapControl().setMagnifierEnabled(b);
            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 获取地图中心点
     *
     * @param promise
     */
    @ReactMethod
    public void getMapCenter(Promise promise) {
        try {
            sMap = SMap.getInstance();
            Point2D center = sMap.smMapWC.getMapControl().getMap().getCenter();
            double x = center.getX();
            double y = center.getY();
            WritableMap writeMap = Arguments.createMap();
            writeMap.putDouble("x", x);
            writeMap.putDouble("y", y);
            promise.resolve(writeMap);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 设置地图中心点
     *
     * @param promise
     */
    @ReactMethod
    public void setMapCenter(double x, double y, Promise promise) {
        try {
            sMap = SMap.getInstance();
            Point2D center = new Point2D(x, y);
            sMap.smMapWC.getMapControl().getMap().setCenter(center);
            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 获取地图比例尺
     *
     * @param promise
     */
    @ReactMethod
    public void getMapScale(Promise promise) {
        try {
            sMap = SMap.getInstance();
            double scale = sMap.smMapWC.getMapControl().getMap().getScale();
            String mscale = "" + 1 / scale;
            promise.resolve(mscale);
        } catch (Exception e) {
            promise.reject(e);
        }
    }


    /**
     * 设置地图比例尺
     *
     * @param promise
     */
    @ReactMethod
    public void setMapScale(double scale, Promise promise) {
        try {
            sMap = SMap.getInstance();
            sMap.smMapWC.getMapControl().getMap().setScale(scale);
            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 获取当前窗口的四至范围 viewBounds
     *
     * @param promise
     */
    @ReactMethod
    public void getMapViewBounds(Promise promise) {
        try {
            sMap = SMap.getInstance();
            Rectangle2D rect = sMap.smMapWC.getMapControl().getMap().getViewBounds();
            double left = rect.getLeft();
            double right = rect.getRight();
            double top = rect.getTop();
            double bottom = rect.getBottom();
            WritableMap map = Arguments.createMap();
            map.putDouble("left", left);
            map.putDouble("bottom", bottom);
            map.putDouble("right", right);
            map.putDouble("top", top);
            promise.resolve(map);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 设置当前窗口四至范围
     *
     * @param left
     * @param bottom
     * @param right
     * @param top
     * @param promise
     */
    @ReactMethod
    public void setMapViewBounds(double left, double bottom, double right, double top, Promise promise) {
        try {
            sMap = SMap.getInstance();
            Rectangle2D rect = new Rectangle2D(left, bottom, right, top);
            sMap.smMapWC.getMapControl().getMap().setViewBounds(rect);
            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 获取地图坐标系
     *
     * @param promise
     */
    @ReactMethod
    public void getPrjCoordSys(Promise promise) {
        try {
            sMap = SMap.getInstance();
            String name = sMap.smMapWC.getMapControl().getMap().getPrjCoordSys().getName();
            promise.resolve(name);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 设置地图坐标系
     *
     * @param xml
     * @param promise
     */
    @ReactMethod
    public void setPrjCoordSys(String xml, Promise promise) {
        try {
            sMap = SMap.getInstance();
            PrjCoordSys prjCoordSys = new PrjCoordSys();
            prjCoordSys.fromXML(xml);
            sMap.smMapWC.getMapControl().getMap().setPrjCoordSys(prjCoordSys);
            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 从数据源复制坐标系
     *
     * @param dataSourcePath
     * @param promise
     */
    @ReactMethod
    public void copyPrjCoordSysFromDatasource(String dataSourcePath, int engineType, Promise promise) {
        try {
            sMap = SMap.getInstance();
            Workspace workspace = new Workspace();
            DatasourceConnectionInfo datasourceConnectionInfo = new DatasourceConnectionInfo();
            EngineType eType = EngineType.newInstance(engineType);
            datasourceConnectionInfo.setEngineType(eType);
            datasourceConnectionInfo.setServer(dataSourcePath);
            datasourceConnectionInfo.setAlias("dataSource");
            Datasource datasource = workspace.getDatasources().open(datasourceConnectionInfo);

            PrjCoordSys prjCoordSys = datasource.getPrjCoordSys();
            sMap.smMapWC.getMapControl().getMap().setPrjCoordSys(prjCoordSys);

            String coordName = sMap.smMapWC.getMapControl().getMap().getPrjCoordSys().getName();

            WritableMap map = Arguments.createMap();
            map.putString("prjCoordSysName", coordName);

            promise.resolve(map);
        } catch (Exception e) {
            promise.reject(e);
        }

    }

    /**
     * 从数据集复制坐标系
     *
     * @param datasourceName
     * @param datasetName
     * @param promise
     */
    @ReactMethod
    public void copyPrjCoordSysFromDataset(String datasourceName, String datasetName, Promise promise) {
        try {
            sMap = SMap.getInstance();
            Datasources datasources = sMap.smMapWC.getWorkspace().getDatasources();

            Datasource datasource = datasources.get(datasourceName);

            if (datasource != null) {
                Dataset dataset = datasource.getDatasets().get(datasetName);
                if (dataset != null) {
                    if (dataset.getPrjCoordSys() != null) {
                        sMap.smMapWC.getMapControl().getMap().setPrjCoordSys(dataset.getPrjCoordSys());
                    } else {
                        sMap.smMapWC.getMapControl().getMap().setPrjCoordSys(datasource.getPrjCoordSys());
                    }
                    String coordName = sMap.smMapWC.getMapControl().getMap().getPrjCoordSys().getName();

                    WritableMap map = Arguments.createMap();
                    map.putString("prjCoordSysName", coordName);

                    promise.resolve(map);
                } else {
                    promise.resolve(false);
                }

            } else {
                promise.resolve(false);
            }
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 从文件复制坐标系
     *
     * @param filePath
     * @param fileType
     * @param promise
     */
    @ReactMethod
    public void copyPrjCoordSysFromFile(String filePath, String fileType, Promise promise) {
        try {
            sMap = SMap.getInstance();
            PrjFileType prjFileType = fileType.equals("xml") ? PrjFileType.SUPERMAP : PrjFileType.ESRI;
            PrjCoordSys prjCoordSys = new PrjCoordSys();
            Boolean isSuccess = prjCoordSys.fromFile(filePath, prjFileType);

            WritableMap map = Arguments.createMap();

            if (isSuccess) {
                sMap.smMapWC.getMapControl().getMap().setPrjCoordSys(prjCoordSys);
                String coordName = sMap.smMapWC.getMapControl().getMap().getPrjCoordSys().getName();
                map.putString("prjCoordSysName", coordName);
            } else {
                map.putString("error", "ILLEGAL_COORDSYS");
            }

            promise.resolve(map);

        } catch (Exception e) {
            promise.reject(e);
        }

    }

    /**
     * 获取动态投影是否已开启
     *
     * @param promise
     */
    @ReactMethod
    public void getMapDynamicProjection(Promise promise) {
        try {
            sMap = SMap.getInstance();
            Boolean isDynamicprojection = sMap.smMapWC.getMapControl().getMap().isDynamicProjection();
            promise.resolve(isDynamicprojection);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 设置是否开启动态投影
     *
     * @param value
     * @param promise
     */
    @ReactMethod
    public void setMapDynamicProjection(Boolean value, Promise promise) {
        try {
            sMap = SMap.getInstance();
            sMap.smMapWC.getMapControl().getMap().setDynamicProjection(value);
            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 获取当前投影转换方法
     *
     * @param promise
     */
    @ReactMethod
    public void getCoordSysTransMethod(Promise promise) {
        try {
            sMap = SMap.getInstance();
            CoordSysTransMethod method = sMap.smMapWC.getMapControl().getMap().getDynamicPrjTransMethond();
            CoordSysTransMethod[] methods = new CoordSysTransMethod[6];
            int index = 0;
            String name = "";
            methods[0] = CoordSysTransMethod.MTH_GEOCENTRIC_TRANSLATION;
            methods[1] = CoordSysTransMethod.MTH_MOLODENSKY;
            methods[2] = CoordSysTransMethod.MTH_MOLODENSKY_ABRIDGED;
            methods[3] = CoordSysTransMethod.MTH_POSITION_VECTOR;
            methods[4] = CoordSysTransMethod.MTH_COORDINATE_FRAME;
            methods[5] = CoordSysTransMethod.MTH_BURSA_WOLF;
            for (int i = 0; i < methods.length; i++) {
                if (method == methods[i])
                    index = i;
            }
            switch (index) {
                case 0:
                    name = "Geocentric Transalation(3-para)";
                    break;
                case 1:
                    name = "Molodensky(7-para)";
                    break;
                case 2:
                    name = "Abridged Molodensky(7-para)";
                    break;
                case 3:
                    name = "Position Vector(7-para)";
                    break;
                case 4:
                    name = "Coordinate Frame(7-para)";
                    break;
                case 5:
                    name = "Bursa-wolf(7-para)";
                    break;
            }
            promise.resolve(name);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 设置当前投影转换方法和参数
     *
     * @param params
     * @param promise
     */
    @ReactMethod
    public void setCoordSysTransMethodAndParams(ReadableMap params, Promise promise) {
        try {
            sMap = SMap.getInstance();
            com.supermap.mapping.Map map = sMap.smMapWC.getMapControl().getMap();
            String[] coorMethodArray = new String[6];
            CoordSysTransMethod method = CoordSysTransMethod.MTH_GEOCENTRIC_TRANSLATION;
            int index = 0;
            coorMethodArray[0] = "Geocentric Transalation(3-para)";
            coorMethodArray[1] = "Molodensky(7-para)";
            coorMethodArray[2] = "Abridged Molodensky(7-para)";
            coorMethodArray[3] = "Position Vector(7-para)";
            coorMethodArray[4] = "Coordinate Frame(7-para)";
            coorMethodArray[5] = "Bursa-wolf(7-para)";
            for (int i = 0; i < coorMethodArray.length; i++) {
                if (params.getString("coordSysTransMethod").equals(coorMethodArray[i]))
                    index = i;
            }
            switch (index) {
                case 0:
                    method = CoordSysTransMethod.MTH_GEOCENTRIC_TRANSLATION;
                    break;
                case 1:
                    method = CoordSysTransMethod.MTH_MOLODENSKY;
                    break;
                case 2:
                    method = CoordSysTransMethod.MTH_MOLODENSKY_ABRIDGED;
                    break;
                case 3:
                    method = CoordSysTransMethod.MTH_POSITION_VECTOR;
                    break;
                case 4:
                    method = CoordSysTransMethod.MTH_COORDINATE_FRAME;
                    break;
                case 5:
                    method = CoordSysTransMethod.MTH_BURSA_WOLF;
                    break;
            }
            map.setDynamicPrjTransMethond(method);
            map.getDynamicPrjTransParameter().setRotateX(params.getDouble("rotateX"));
            map.getDynamicPrjTransParameter().setRotateY(params.getDouble("rotateY"));
            map.getDynamicPrjTransParameter().setRotateZ(params.getDouble("rotateZ"));
            map.getDynamicPrjTransParameter().setTranslateX(params.getDouble("translateX"));
            map.getDynamicPrjTransParameter().setTranslateY(params.getDouble("translateY"));
            map.getDynamicPrjTransParameter().setTranslateZ(params.getDouble("translateZ"));

            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 添加图例的事件监听
     *
     * @param promise
     */
    @ReactMethod
    public void addLegendListener(Promise promise) {
        try {
            sMap = SMap.getInstance();
            sMap.smMapWC.getMapControl().getMap().getLegend().setContentChangeListener(this);
            sMap.smMapWC.getMapControl().getMap().refresh();
            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    @Override
    public void legendContentChanged(Vector<LegendItem> arrItems) {
        sMap = SMap.getInstance();
        WritableArray arr = Arguments.createArray();
        for (int i = 0; i < arrItems.size(); i++) {
            WritableMap writeMap = Arguments.createMap();
            Bitmap bm = arrItems.get(i).getBitmap();
            String name = arrItems.get(i).getCaption();
            int type = arrItems.get(i).getType();
            String result = null;
            if (bm != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
                byte[] bitmapBytes = baos.toByteArray();
                result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
            }
            writeMap.putString("image", result);
            writeMap.putString("title", name);
            writeMap.putInt("type", type);
            arr.pushMap(writeMap);
        }
        ReadableArray array = sMap.getOtherLegendData(arr);
        context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(EventConst.LEGEND_CONTENT_CHANGE, array);
    }

    /**
     * 获取分段图例数据
     *
     * @return
     */
    public ReadableArray getOtherLegendData(WritableArray array) {
        sMap = SMap.getInstance();
        Layers layers = sMap.smMapWC.getMapControl().getMap().getLayers();
        for (int i = 0; i < layers.getCount(); i++) {
            Layer layer = layers.get(i);
            if (layer.getTheme() != null) {
                if (layer.getTheme().getType() == ThemeType.RANGE) {
                    ThemeRange themeRange = (ThemeRange) layer.getTheme();
                    for (int j = 0; j < themeRange.getCount(); j++) {
                        GeoStyle geoStyle = themeRange.getItem(j).getStyle();
                        Color color = geoStyle.getFillForeColor();
                        String caption = themeRange.getItem(j).getCaption();
                        String r = Integer.toHexString(color.getR());
                        String g = Integer.toHexString(color.getG());
                        String b = Integer.toHexString(color.getB());
                        r = r.length() == 1 ? "0" + r : r;
                        g = g.length() == 1 ? "0" + g : g;
                        b = b.length() == 1 ? "0" + b : b;
                        String colorString = "#" + r + g + b;
                        System.out.print(colorString);
                        WritableMap writableMap = Arguments.createMap();
                        writableMap.putString("color", colorString);
                        writableMap.putString("title", caption);
                        writableMap.putInt("type", 3);
                        array.pushMap(writableMap);
                    }
                }
            }
        }
        return array;
    }

    /**
     * 移除图例的事件监听
     *
     * @param promise
     */
    @ReactMethod
    public void removeLegendListener(Promise promise) {
        try {
            sMap = SMap.getInstance();
            sMap.smMapWC.getMapControl().getMap().getLegend().setContentChangeListener(null);
            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }
    /************************************** 地图设置 END ****************************************/
//
//    /**
//     * 初始化二维POI搜索
//     * @param promise
//     */
//    @ReactMethod
//    public void initPointSearch(Promise promise){
//        try {
//            sMap = SMap.getInstance();
//            sMap.poiSearchHelper2D = POISearchHelper2D.getInstence();
//            MapControl mapControl = sMap.smMapWC.getMapControl();
//            sMap.poiSearchHelper2D.initMapControl(mapControl,context);
//            promise.resolve(true);
//        }catch (Exception e){
//            promise.reject(e);
//        }
//    }
//
//    /**
//     * 二维POI搜索
//     * @param keyword
//     * @param promise
//     */
//    @ReactMethod
//    public void pointSearch(String keyword, Promise promise){
//        try {
//            sMap.poiSearchHelper2D.poiSearch(keyword, new POISearchHelper2D.PoiSearchCallBack() {
//                @Override
//                public void poiSearchInfos(ArrayList<PoiGsonBean.PoiInfos> poiInfos) {
//                    WritableArray array = Arguments.createArray();
//                    int count = poiInfos.size();
//                    for(int i = 0; i < count; i++){
//                        WritableMap map = Arguments.createMap();
//                        PoiGsonBean.PoiInfos poiInfo = poiInfos.get(i);
//                        String name = poiInfo.getName();
//                        map.putString("pointName",name);
//                        map.putDouble("x",poiInfo.getLocation().getX());
//                        map.putDouble("y",poiInfo.getLocation().getY());
//                        array.pushMap(map);
//                    }
//                    context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
//                            .emit(EventConst.POINTSEARCH2D_KEYWORDS, array);
//                }
//            });
//            promise.resolve(true);
//        }catch (Exception e){
//            promise.reject(e);
//        }
//    }

    /**
     * 定位到搜索结果某个点
     *
     * @param map
     * @param promise
     */
    @ReactMethod
    public void toLocationPoint(ReadableMap map, Promise promise) {
        try {
            sMap = SMap.getInstance();
            double x = map.getDouble("x");
            double y = map.getDouble("y");
            String name = map.getString("pointName");
            String tagName = "POISEARCH_2D_POINT";
            clearPoint(tagName);
            Boolean isSuccess = addCallout(x, y, name, tagName, true, false);
            promise.resolve(isSuccess);
        } catch (Exception e) {
            promise.reject(e);
        }
    }


    /**
     * 获取当前定位的经纬度
     *
     * @param promise
     */
    @ReactMethod
    public void getCurrentPosition(Promise promise) {
        try {
            LocationManagePlugin.GPSData gpsDat = SMCollector.getGPSPoint();
            WritableMap map = Arguments.createMap();
            map.putDouble("x", gpsDat.dLongitude);
            map.putDouble("y", gpsDat.dLatitude);
            promise.resolve(map);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 获取地图中心点经纬度
     *
     * @param promise
     */
    @ReactMethod
    public void getMapcenterPosition(Promise promise) {
        try {
            sMap = SMap.getInstance();
            MapControl mapControl = sMap.smMapWC.getMapControl();
            Point2D point = mapControl.getMap().getCenter();
            Point2Ds point2Ds = new Point2Ds();
            point2Ds.add(point);

            PrjCoordSys sourcePrjCoordSys = new PrjCoordSys(PrjCoordSysType.PCS_EARTH_LONGITUDE_LATITUDE);

            CoordSysTransParameter coordSysTransParameter = new CoordSysTransParameter();

            CoordSysTranslator.convert(
                    point2Ds,
                    mapControl.getMap().getPrjCoordSys(),
                    sourcePrjCoordSys,
                    coordSysTransParameter,
                    CoordSysTransMethod.MTH_GEOCENTRIC_TRANSLATION);

            Point2D mapPoint = point2Ds.getItem(0);

            WritableMap map = Arguments.createMap();
            map.putDouble("x", mapPoint.getX());
            map.putDouble("y", mapPoint.getY());

            promise.resolve(map);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 移除POI搜索的callout
     *
     * @param promise
     */
    @ReactMethod
    public void removePOICallout(Promise promise) {
        try {
            sMap = SMap.getInstance();
            String tagName = "POISEARCH_2D_POINT";
            clearPoint(tagName);
            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    @ReactMethod
    public void removeAllCallout(Promise promise) {
        try {
            sMap = SMap.getInstance();
            for (int i = 0; i < 10; i++) {
                String tagName = "POISEARCH_2D_POINTS" + i;
                clearPoint(tagName);
            }
            clearPoint("bigCallout");
            hasBigCallout = false;
            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

//    /**
//     * 当前选中的callout移动到地图中心
//     *
//     * @param item
//     * @param promise
//     */
//    @ReactMethod
//    public void setCalloutToMapCenter(ReadableMap item, Promise promise) {
//        try {
//            MapControl mapControl = SMap.getInstance().smMapWC.getMapControl();
//            double x = item.getDouble("x");
//            double y = item.getDouble("y");
//            Point2D point = new Point2D(x, y);
//            Point2Ds point2Ds = new Point2Ds();
//            point2Ds.add(point);
//
//            PrjCoordSys sourcePrjCoordSys = new PrjCoordSys(PrjCoordSysType.PCS_EARTH_LONGITUDE_LATITUDE);
//            CoordSysTransParameter coordSysTransParameter = new CoordSysTransParameter();
//
//            CoordSysTranslator.convert(
//                    point2Ds,
//                    sourcePrjCoordSys,
//                    mapControl.getMap().getPrjCoordSys(),
//                    coordSysTransParameter,
//                    CoordSysTransMethod.MTH_GEOCENTRIC_TRANSLATION);
//
//            Point2D mapPoint = point2Ds.getItem(0);
//
//            mapControl.getMap().setCenter(mapPoint);
//            mapControl.getMap().refresh();
//            promise.resolve(true);
//        } catch (Exception e) {
//            promise.reject(e);
//        }
//    }
    @ReactMethod
    public void setCenterCallout(ReadableMap item,Promise promise){
        try{
            sMap = SMap.getInstance();
            if(hasBigCallout){
                clearPoint("bigCallout");
            }
            double x = item.getDouble("x");
            double y = item.getDouble("y");
            String name = "";
            String tagName = "bigCallout";
            boolean b = addCallout(x,y,name,tagName,true,true);
            promise.resolve(b);
        }catch (Exception e){
            promise.reject(e);
        }
    }
    /**
     * 添加搜索到的callouts
     *
     * @param pointList
     * @param promise
     */
    @ReactMethod
    public void addCallouts(ReadableArray pointList, Promise promise) {
        try {
            sMap = SMap.getInstance();
            sMap.clearPoint("POISEARCH_2D_POINT");
            Boolean isSuccess = true;
            //最多10个callout
            int len = pointList.size() < 10 ? pointList.size() : 10;

            for (int i = 0; i < len; i++) {
                ReadableMap map = pointList.getMap(i);
                double x = map.getDouble("x");
                double y = map.getDouble("y");
                String name = "";
                String tagName = "POISEARCH_2D_POINTS" + i;
                Boolean b = addCallout(x, y, name, tagName, false,false);
                if (!b) {
                    isSuccess = b;
                }
            }
            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 添加callout
     *
     * @param x            经度
     * @param y            纬度
     * @param name         显示的名字
     * @param tagName      标识名
     * @param changeCenter 是否改变地图中心点
     * @param bigCallout   是否特别标注（绿色、加大）
     */
    public Boolean addCallout(final double x, final double y, final String name, final String tagName, final Boolean changeCenter, final Boolean bigCallout) {
        context.getCurrentActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                sMap = SMap.getInstance();
                MapControl mapControl = sMap.smMapWC.getMapControl();
                Point2D point = new Point2D(x, y);
                Point2Ds point2Ds = new Point2Ds();
                point2Ds.add(point);

                PrjCoordSys sourcePrjCoordSys = new PrjCoordSys(PrjCoordSysType.PCS_EARTH_LONGITUDE_LATITUDE);
                CoordSysTransParameter coordSysTransParameter = new CoordSysTransParameter();

                CoordSysTranslator.convert(
                        point2Ds,
                        sourcePrjCoordSys,
                        mapControl.getMap().getPrjCoordSys(),
                        coordSysTransParameter,
                        CoordSysTransMethod.MTH_GEOCENTRIC_TRANSLATION);

                Point2D mapPoint = point2Ds.getItem(0);

                DisplayMetrics dm = new DisplayMetrics();
                getCurrentActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
                float density = dm.density;

                InfoCallout callout = new InfoCallout(context);
                callout.setStyle(CalloutAlignment.LEFT_BOTTOM);
                callout.setBackground(0, 0);
                ImageView imageView = new ImageView(context);
                imageView.setAdjustViewBounds(true);
                if(bigCallout){
                    hasBigCallout = true;
                    imageView.setImageResource(R.drawable.icon_green);
                    imageView.setMaxWidth((int)(50*density));
                    imageView.setMaxHeight((int)(50*density));
                }else{
                    imageView.setImageResource(R.drawable.icon_red);
                    imageView.setMaxWidth((int)(40*density));
                    imageView.setMaxHeight((int)(40*density));
                    imageView.setPadding((int)(5*density),
                            (int)(5*density),0,0);
                }


                TextView textView = new TextView(context);
                textView.setHeight(180);
                textView.setWidth(180);
                //textView.setShadowLayer(3, 3, -3, android.graphics.Color.WHITE);
                textView.setText(name);

                LinearLayout linearLayout = new LinearLayout(context);
                linearLayout.setLayoutParams(new LinearLayout.LayoutParams(240, 180));
                linearLayout.addView(imageView);
                linearLayout.addView(textView);

                callout.setContentView(linearLayout);
                // 20处理默认callout背景位置偏差
                double x = mapPoint.getX() - 20;
                double y = mapPoint.getY() - 20;
                callout.setLocation(x, y);

                mapControl.getMap().getMapView().addCallout(callout, tagName);
                mapControl.getMap().getMapView().showCallOut();
                if (mapControl.getMap().getScale() < 0.000011947150294723098) {
                    mapControl.getMap().setScale(0.000011947150294723098);
                }
                if (changeCenter) {
                    mapControl.getMap().setCenter(mapPoint);
                }
                mapControl.getMap().refresh();
            }
        });
        return true;
    }

    public void clearPoint(final String tagName) {
        context.getCurrentActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MapControl mapControl = SMap.getInstance().smMapWC.getMapControl();
                mapControl.getMap().getMapView().removeCallOut(tagName);
            }
        });
    }
    /************************************** 导航模块 START ****************************************/
    /**
     * 清除导航路线
     *
     * @param promise
     */
    @ReactMethod
    public void clearTarckingLayer(Promise promise) {
        try {
            sMap = SMap.getInstance();
            sMap.smMapWC.getMapControl().getMap().getTrackingLayer().clear();
            context.getCurrentActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    SMap.getInstance().getSmMapWC().getMapControl().getMap().getMapView().removeAllCallOut();
                }
            });
            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }


    private void setNavigationOnline(NavigationOnlineData data) {
        if (data == null) {
            return;
        }
        sMap = SMap.getInstance();
        sMap.smMapWC.getMapControl().getMap().getTrackingLayer().clear();
//		从data中获取geoline
        GeoLine geoLine = data.getRoute();
        GeoStyle geoLineStyle = new GeoStyle();
        Color color = new Color(255, 0, 0);
        geoLineStyle.setLineColor(color);
//		为geoLine设置风格
        geoLine.setStyle(geoLineStyle);
//		在跟踪图层上显示geoLine
        sMap.smMapWC.getMapControl().getMap().getTrackingLayer().add(geoLine, "线路");
        LocationManagePlugin.GPSData gpsDat = SMCollector.getGPSPoint();
        Point2D pt = new Point2D(gpsDat.dLongitude, gpsDat.dLatitude);
        if (!safeGetType(sMap.smMapWC.getMapControl().getMap().getPrjCoordSys(), PrjCoordSysType.PCS_EARTH_LONGITUDE_LATITUDE)) {
            Point2Ds point2Ds = new Point2Ds();
            point2Ds.add(pt);
            PrjCoordSys prjCoordSys = new PrjCoordSys();
            prjCoordSys.setType(PrjCoordSysType.PCS_EARTH_LONGITUDE_LATITUDE);
            CoordSysTransParameter parameter = new CoordSysTransParameter();

            CoordSysTranslator.convert(point2Ds, prjCoordSys, sMap.smMapWC.getMapControl().getMap().getPrjCoordSys(), parameter, CoordSysTransMethod.MTH_GEOCENTRIC_TRANSLATION);
            pt = point2Ds.getItem(0);
            showMarkerHelper(pt, curLocationTag);
        }
//		得到线路信息的集合
        List<PathInfo> pathInfoList = data.getPathInfos();


        WritableArray array = Arguments.createArray();
        for (int i = 0; i < pathInfoList.size(); i++) {
            WritableMap map = Arguments.createMap();
            PathInfo pathInfo = pathInfoList.get(i);
            String roadName = pathInfo.getRoadName();
            int nextDirection = pathInfo.getNextDirection();
            double roadLength = pathInfo.getLength();

            map.putString("roadName", roadName);
            map.putInt("nextDirection", nextDirection);
            map.putDouble("roadLength", roadLength);
            array.pushMap(map);
        }

        WritableMap map = Arguments.createMap();
        map.putString("Length", data.getLength());

        context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(EventConst.NAVIGATION_WAYS, array);

        context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(EventConst.NAVIGATION_LENGTH, map);
    }

    Point2D endPoint, startPoint;

    @ReactMethod
    public void routeAnalyst(double x, double y, Promise promise) {
        try {
            sMap = SMap.getInstance();
            endPoint = new Point2D(x, y);
            LocationManagePlugin.GPSData gpsDat = SMCollector.getGPSPoint();
            startPoint = new Point2D(gpsDat.dLongitude, gpsDat.dLatitude);
            NavigationOnline navigationOnline = new NavigationOnline();
            navigationOnline.setKey("fvV2osxwuZWlY0wJb8FEb2i5");
            navigationOnline.setNavigationOnlineCallback(new NavigationOnline.NavigationOnlineCallback() {
                @Override
                public void calculateSuccess(NavigationOnlineData data) {
                    setNavigationOnline(data);
                }

                @Override
                public void calculateFailed(String errorInfo) {
                    Log.e("LocationMore", errorInfo);
                }
            });
            NavigationOnlineParameter parameter = new NavigationOnlineParameter();
            parameter.setStartPoint(startPoint);
            parameter.setEndPoint(endPoint);
            parameter.setCoordinateType(CoordinateType.NAVINFO_AMAP_MERCATOR);
            parameter.setRouteType(RouteType.RE_COMMEND);
            navigationOnline.routeAnalyst(parameter);
            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }


    /**
     * 打开二维导航工作空间及地图
     *
     * @param promise
     */
    @ReactMethod
    public void open2DNavigationMap(ReadableMap data, Promise promise) {
        try {
            sMap = getInstance();
            WritableArray array = Arguments.createArray();
            Map params = data.toHashMap();
            boolean result = sMap.smMapWC.openWorkspace(params);
            if (result) {
                if (sMap.getSmMapWC().getMapControl() != null && sMap.getSmMapWC().getMapControl().getMap() != null && !sMap.getSmMapWC().getMapControl().getMap().getName().equals("")) {
                    sMap.getSmMapWC().getMapControl().getMap().setVisibleScalesEnabled(false);
                    sMap.getSmMapWC().getMapControl().getMap().setAntialias(true);
                    sMap.getSmMapWC().getMapControl().getMap().refresh();
                }
                Workspace mWorkspace = SMap.getInstance().getSmMapWC().getWorkspace();
                for (int i = 0; i < mWorkspace.getMaps().getCount(); i++) {
                    String name = mWorkspace.getMaps().get(i);
                    WritableMap map = Arguments.createMap();
                    map.putString("name", name);
                    array.pushMap(map);
                }
            }
            promise.resolve(array);
        } catch (Exception e) {
            promise.reject(e);
        }
    }


    /**
     * 设置行业导航参数
     *
     * @param promise
     */
    @ReactMethod
    public void startNavigation(String networkDatasetName, String netModel, Promise promise) {
        try {
            sMap = SMap.getInstance();

            Workspace mWorkspace = SMap.getInstance().getSmMapWC().getWorkspace();

            for (int i = 0; i < mWorkspace.getDatasources().getCount(); i++) {
                Datasource datasource = mWorkspace.getDatasources().get(i);
                Dataset dataset = datasource.getDatasets().get(networkDatasetName);
                if (dataset != null) {
                    // 初始化行业导航对象
                    DatasetVector networkDataset = (DatasetVector) dataset;
                    Navigation2 m_Navigation2 = sMap.getSmMapWC().getMapControl().getNavigation2();      // 获取行业导航控件，只能通过此方法初始化m_Navigation2
                    m_Navigation2.setNetworkDataset(networkDataset);    // 设置网络数据集
                    m_Navigation2.loadModel(netModel);  // 加载网络模型
                    m_Navigation2.addNaviInfoListener(new NaviListener() {

                        @Override
                        public void onStopNavi() {
                            // TODO Auto-generated method stub
                            Log.e("+++++++++++++", "-------------****************");
                            WritableMap map = Arguments.createMap();
                            map.putBoolean("finsh", true);
                            context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                                    .emit(EventConst.INDUSTRYNAVIAGTION, map);
                        }

                        @Override
                        public void onStartNavi() {
                            // TODO Auto-generated method stub
                            Log.e("+++++++++++++", "-------------****************");
                        }

                        @Override
                        public void onNaviInfoUpdate(NaviInfo arg0) {
                            // TODO Auto-generated method stub
                            Log.e("+++++++++++++", "-------------****************");
                        }

                        @Override
                        public void onAarrivedDestination() {
                            // TODO Auto-generated method stub
                            Log.e("+++++++++++++", "-------------****************");
                            WritableMap map = Arguments.createMap();
                            map.putBoolean("finsh", true);
                            context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                                    .emit(EventConst.INDUSTRYNAVIAGTION, map);
                        }

                        @Override
                        public void onAdjustFailure() {
                            // TODO Auto-generated method stub
                            Log.e("+++++++++++++", "-------------****************");
                        }

                        @Override
                        public void onPlayNaviMessage(String arg0) {
                            // TODO Auto-generated method stub
                            Log.e("+++++++++++++", "-------------****************");
                        }
                    });
                }
            }
            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 开启行业导航
     *
     * @param promise
     */
    @ReactMethod
    public void beginNavigation(double x, double y, double x2, double y2, Promise promise) {
        try {
            Point2D pointstart = getPoint(x, y);
            Point2D pointend = getPoint(x2, y2);
            sMap = SMap.getInstance();
            sMap.getSmMapWC().getMapControl().getNavigation2().setStartPoint(pointstart.getX(), pointstart.getY());        // 设置起点
            sMap.getSmMapWC().getMapControl().getNavigation2().setDestinationPoint(pointend.getX(), pointend.getY());     // 设置终点
            sMap.getSmMapWC().getMapControl().getNavigation2().setPathVisible(true);                                       // 设置路径可见
            boolean isfind = sMap.getSmMapWC().getMapControl().getNavigation2().routeAnalyst();
            Log.e("++++++++++++", "" + isfind);
            if (isfind) {
                context.getCurrentActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        sMap.getSmMapWC().getMapControl().getNavigation2().startGuide(1);
                    }
                });
            }
            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }


    /**
     * 设置室内导航
     *
     * @param promise
     */
    @ReactMethod
    public void startIndoorNavigation(Promise promise) {
        try {
            sMap = SMap.getInstance();
            Navigation3 mNavigation3 = sMap.getSmMapWC().getMapControl().getNavigation3();
            GeoStyle style = new GeoStyle();
            style.setLineSymbolID(964882);
            mNavigation3.setRouteStyle(style);
            GeoStyle styleHint = new GeoStyle();
            styleHint.setLineWidth(2);
            styleHint.setLineColor(new com.supermap.data.Color(82, 198, 223));
            styleHint.setLineSymbolID(2);
            mNavigation3.setHintRouteStyle(styleHint);
            mNavigation3.addNaviInfoListener(new NaviListener() {
                @Override
                public void onStopNavi() {
                    // TODO Auto-generated method stub
                }

                @Override
                public void onStartNavi() {
                    // TODO Auto-generated method stub
                }

                @Override
                public void onPlayNaviMessage(String message) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onNaviInfoUpdate(NaviInfo naviInfo) {
                    // TODO Auto-generated method stub
                    Log.e("++++++++++++++++++", "" + naviInfo.CurRoadName);
                }

                @Override
                public void onAdjustFailure() {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onAarrivedDestination() {
                    // TODO Auto-generated method stub
                }
            });

            mNavigation3.setDatasource(IndoorDatasource);
            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }


    /**
     * 开启室内导航
     *
     * @param promise
     */
    @ReactMethod
    public void beginIndoorNavigation(double x, double y, double x2, double y2, Promise promise) {
        try {
            Point2D pointstart = getPoint(x, y);
            Point2D pointend = getPoint(x2, y2);
            sMap = SMap.getInstance();
            sMap.getSmMapWC().getMapControl().getNavigation3().setStartPoint(pointstart.getX(), pointstart.getY(), sMap.getSmMapWC().getFloorListView().getCurrentFloorId());
            sMap.getSmMapWC().getMapControl().getNavigation3().setDestinationPoint(pointend.getX(), pointend.getY(), sMap.getSmMapWC().getFloorListView().getCurrentFloorId());

            boolean result = sMap.getSmMapWC().getMapControl().getNavigation3().routeAnalyst();
            Log.e("++++++++++++", "" + result);
            if (result) {
                context.getCurrentActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        sMap.getSmMapWC().getMapControl().getNavigation3().startGuide(1);
                    }
                });

            }
            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 获取路网信息
     *
     * @param promise
     */
    @ReactMethod
    public void getNavigationData(Promise promise) {
        sMap = SMap.getInstance();
        WritableArray array = Arguments.createArray();
        Workspace mWorkspace = SMap.getInstance().getSmMapWC().getWorkspace();
        Datasources datasources = mWorkspace.getDatasources();
        for (int i = 0; i < datasources.getCount(); i++) {
            Datasource datasource = datasources.get(i);
            for (int j = 0; j < datasource.getDatasets().getCount(); j++) {
                if (datasource.getDatasets().get(j).getType() == DatasetType.NETWORK) {
                    WritableMap map = Arguments.createMap();
                    map.putString("dataset", datasource.getDatasets().get(j).getName());
                    array.pushMap(map);
                }
            }
        }
        promise.resolve(array);
    }

    /**
     * 获取室内导航数据源
     *
     * @param promise
     */
    @ReactMethod
    public void getIndoorNavigationData(String name, Promise promise) {
        sMap = SMap.getInstance();
        IndoorDatasource = sMap.getSmMapWC().getWorkspace().getDatasources().get(name);
        promise.resolve(true);
    }


    /**
     * 新建路网数据集
     *
     * @param promise
     */
    @ReactMethod
    public void newIncrementRoad(String name, Promise promise) {
        if (IndoorDatasource != null) {
            Datasets datasets = IndoorDatasource.getDatasets();
            String datasetName = datasets.getAvailableDatasetName(name);
            IncrementRoadName = datasetName;
            DatasetVectorInfo datasetVectorInfo = new DatasetVectorInfo();
            datasetVectorInfo.setType(DatasetType.LINE);
            datasetVectorInfo.setEncodeType(EncodeType.NONE);
            datasetVectorInfo.setName(datasetName);
            DatasetVector datasetVector = datasets.create(datasetVectorInfo);
            datasetVector.setPrjCoordSys(sMap.getSmMapWC().getMapControl().getMap().getPrjCoordSys());

            Dataset ds = datasets.get(datasetName);
            com.supermap.mapping.Map map = sMap.smMapWC.getMapControl().getMap();
            Layer layer = map.getLayers().add(ds, true);
            layer.setEditable(true);
            layer.setSnapable(true);
            datasetVectorInfo.dispose();
            datasetVector.close();
        }
        promise.resolve(true);
    }


    /**
     * 获取路网数据集
     *
     * @param promise
     */
    @ReactMethod
    public void getNetWorkDataset(Promise promise) {
        if (IndoorDatasource != null) {
            Datasets datasets = IndoorDatasource.getDatasets();
            WritableArray array = Arguments.createArray();
            for (int i = 0; i < datasets.getCount(); i++) {
                if (datasets.get(i).getType() == DatasetType.NETWORK) {
                    WritableMap map = Arguments.createMap();
                    map.putString("dataset", datasets.get(i).getName());
                    array.pushMap(map);
                }
            }
            promise.resolve(array);
        }
    }


    /**
     * 生成路网
     *
     * @param promise
     */
    @ReactMethod
    public void buildNetwork(String networkdataset, Promise promise) {
        DatasetVector lineDataset = (DatasetVector) IndoorDatasource.getDatasets().get(IncrementRoadName);
        String datasetName = IndoorDatasource.getDatasets().getAvailableDatasetName(IncrementRoadName);
        DatasetVector datasetVector2 = (DatasetVector) IndoorDatasource.copyDataset(
                lineDataset, datasetName, EncodeType.NONE);
        // 构造拓扑处理选项topologyProcessingOptions，各属性设置成false
        TopologyProcessingOptions topologyProcessingOptions = new TopologyProcessingOptions();
        topologyProcessingOptions.setLinesIntersected(true);
        TopologyProcessing.clean(datasetVector2, topologyProcessingOptions);

        IndoorDatasource.getDatasets().delete(networkdataset);

        String[] lineFieldNames = new String[datasetVector2.getFieldInfos().getCount()];
        for (int i = 0; i < datasetVector2.getFieldInfos().getCount(); i++) {
            lineFieldNames[i] = datasetVector2.getFieldInfos().get(i).getCaption();
        }

        DatasetVector datasets[] = {datasetVector2};
        DatasetVector resultDataset = NetworkBuilder.buildNetwork(datasets, null, lineFieldNames, null,
                IndoorDatasource, networkdataset, NetworkSplitMode.LINE_SPLIT_BY_POINT, 0.0000001);

        DatasetVector datasetVector = (DatasetVector) IndoorDatasource.getDatasets().get(networkdataset);
        sMap.getSmMapWC().getMapControl().getMap().getLayers().add(datasetVector.getChildDataset(), true);
        promise.resolve(true);
    }


    /**
     * 判断是否是室内点
     *
     * @param promise
     */
    @ReactMethod
    public void isIndoorPoint(double x, double y, Promise promise) {
        sMap = SMap.getInstance();
        boolean isindoor = false;
        if (IndoorDatasource != null) {
            Recordset recordset = null;
            QueryParameter parameter = new QueryParameter();
            parameter.setCursorType(CursorType.STATIC);
            parameter.setSpatialQueryObject(new Point2D(x, y));
            for (int i = 0; i < IndoorDatasource.getDatasets().getCount(); i++) {
                DatasetVector datasetVector = (DatasetVector) IndoorDatasource.getDatasets().get(i);
                recordset = datasetVector.query(parameter);
                if (recordset != null) {
                    isindoor = true;
                }
            }
        }
        WritableMap map = Arguments.createMap();
        map.putBoolean("isindoor", isindoor);
        promise.resolve(map);
    }

    /**
     * 添加起始点
     *
     * @param promise
     */
    @ReactMethod
    public void getStartPoint(double x, double y, Promise promise) {
        sMap = SMap.getInstance();
        showPointByCallout(x, y, "startpoint");
        promise.resolve(true);
    }

    /**
     * 添加终点
     *
     * @param promise
     */
    @ReactMethod
    public void getEndPoint(double x, double y, Promise promise) {
        sMap = SMap.getInstance();
        showPointByCallout(x, y, "endpoint");
        promise.resolve(true);
    }


    /**
     * 清除起终点
     *
     * @param promise
     */
    @ReactMethod
    public void clearPoint(Promise promise) {
        sMap = SMap.getInstance();
        context.getCurrentActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                SMap.getInstance().getSmMapWC().getMapControl().getMap().getMapView().removeAllCallOut();
                sMap.getSmMapWC().getMapControl().getNavigation2().cleanPath();
                sMap.getSmMapWC().getMapControl().getNavigation2().stopGuide();
                sMap.getSmMapWC().getMapControl().getNavigation3().cleanPath();
                sMap.getSmMapWC().getMapControl().getNavigation3().stopGuide();
            }
        });
        promise.resolve(true);
    }


    /**
     * 将地图上的点转换为经纬坐标点
     *
     * @param
     * @return
     */
    private Point2D getPoint(double x, double y) {
        Point2D point2D = null;
        if (SMap.getInstance().getSmMapWC().getMapControl().getMap().getPrjCoordSys().getType() != PrjCoordSysType.PCS_EARTH_LONGITUDE_LATITUDE) {
            PrjCoordSys srcPrjCoordSys = SMap.getInstance().getSmMapWC().getMapControl().getMap().getPrjCoordSys();
            Point2Ds point2Ds = new Point2Ds();
            point2Ds.add(new Point2D(x, y));
            PrjCoordSys desPrjCoordSys = new PrjCoordSys();
            desPrjCoordSys.setType(PrjCoordSysType.PCS_EARTH_LONGITUDE_LATITUDE);
            // 转换投影坐标
            CoordSysTranslator.convert(point2Ds, srcPrjCoordSys,
                    desPrjCoordSys, new CoordSysTransParameter(),
                    CoordSysTransMethod.MTH_GEOCENTRIC_TRANSLATION);
            point2D = point2Ds.getItem(0);
        } else {
            point2D = new Point2D(x, y);
        }
        return point2D;
    }

    private void showPointByCallout(final double x, final double y, final String pointName) {
        m_callout = new InfoCallout(context);
        m_callout.setStyle(CalloutAlignment.LEFT_BOTTOM);
        m_callout.setBackground(0, 0);
        context.getCurrentActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ImageView imageView = new ImageView(context);
                if (pointName.equals("startpoint")) {
                    imageView.setImageResource(R.drawable.icon_scene_tool_start);
                } else {
                    imageView.setImageResource(R.drawable.icon_scene_tool_end);
                }
                imageView.setAdjustViewBounds(true);
                imageView.setMaxWidth(80);
                imageView.setMaxHeight(80);
                LinearLayout linearLayout = new LinearLayout(context);
                linearLayout.setLayoutParams(new LinearLayout.LayoutParams(80, 80));
                linearLayout.addView(imageView);

                m_callout.setContentView(linearLayout);
                m_callout.setLocation(x, y);
                SMap.getInstance().getSmMapWC().getMapControl().getMap().getMapView().addCallout(m_callout, pointName);
                SMap.getInstance().getSmMapWC().getMapControl().getMap().getMapView().showCallOut();
            }
        });

    }


    /**
     * 打开实时路况信息
     *
     * @param promise
     */
    @ReactMethod
    public void openTrafficMap(Promise promise) {
        sMap = SMap.getInstance();
        Layers layers = sMap.getSmMapWC().getMapControl().getMap().getLayers();
        boolean isadd = false;
        for (int i = 0; i < layers.getCount(); i++) {
            if (layers.get(i).getName().equals("tencent@TrafficRest")) {
                isadd = true;
            }
        }
        if (!isadd) {
            DatasourceConnectionInfo info = new DatasourceConnectionInfo();
            info.setAlias("TrafficRest");
            info.setEngineType(EngineType.Rest);
            String url = "https://www.supermapol.com/iserver/services/traffic/rest/maps/tencent";
            info.setServer(url);
            Datasource datasource = sMap.getSmMapWC().getWorkspace().getDatasources().open(info);
            sMap.getSmMapWC().getMapControl().getMap().getLayers().add(datasource.getDatasets().get(0), true);
        }
        promise.resolve(true);
    }



    /**
     * GPS开始
     *
     * @param promise
     */
    @ReactMethod
    public void gpsBegin(Promise promise) {
        sMap = SMap.getInstance();
        LocationManagePlugin.GPSData gpsDat = SMCollector.getGPSPoint();
        Point2D gpsPoint = new Point2D(gpsDat.dLongitude, gpsDat.dLatitude);
        Log.e("+++++++++++++++++++",""+gpsPoint);
        GpsPoint2Ds.add(gpsPoint);
        promise.resolve(true);
    }



    /**
     * 添加GPS轨迹
     *
     * @param promise
     */
    @ReactMethod
    public void addGPSRecordset(Promise promise) {
        try {
            sMap = SMap.getInstance();
            DatasetVector dataset = (DatasetVector) IndoorDatasource.getDatasets().get(IncrementRoadName);
            if (dataset != null) {
                dataset.setReadOnly(false);
            }
            Recordset recordset = dataset.getRecordset(false, CursorType.DYNAMIC);
            GeoLine geoline = new GeoLine();
            geoline.addPart(GpsPoint2Ds);
            recordset.addNew(geoline);
            recordset.update();
            int id[] = new int[1];
            id[0] = recordset.getID();
            recordset.close();
            geoline.dispose();
            recordset.dispose();
            Recordset recordset1 = dataset.query(id, CursorType.DYNAMIC);
            sMap.smMapWC.getMapControl().getEditHistory().batchBegin();
            sMap.smMapWC.getMapControl().getEditHistory().addHistoryType(EditHistoryType.ADDNEW, recordset1, true);
            sMap.smMapWC.getMapControl().getEditHistory().batchEnd();
            recordset1.close();
            recordset1.dispose();
            sMap.smMapWC.getMapControl().getMap().refresh();
            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }



    /************************************** 导航模块 END ****************************************/


    /**
     * 智能配图
     *
     * @param picPath
     * @param promise
     */
    @ReactMethod
    public void matchPictureStyle(String picPath, Promise promise) {
        try {
            SMap.getInstance().smMapWC.getMapControl().getEditHistory().addMapHistory();
            SMMapRender smMapRender = SMMapRender.getInstance();
            smMapRender.setSmMapRenderListener(new SMMapRender.SMMapRenderListener() {
                @Override
                public void onMatchPictureStyleFinished(boolean bSucssed, String strPath, String error) {
                    WritableMap res = Arguments.createMap();
                    res.putBoolean("result", bSucssed);
                    res.putString("image", strPath);
                    res.putString("error", error);
                    context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                            .emit(EventConst.MATCH_IMAGE_RESULT, res);
                }
            });

            String path = picPath;
            if (picPath.indexOf("content://") == 0) {
                path = FileUtil.getRealFilePath(getReactApplicationContext(), Uri.parse(picPath));
            }
            smMapRender.setCompressMode(2);
            smMapRender.matchPictureStyle(path);
            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 移除智能配图监听
     * @param promise
     */
    @ReactMethod
    public void deleteMatchPictureListener(Promise promise) {
        try {
            SMMapRender smMapRender = SMMapRender.getInstance();
            smMapRender.setSmMapRenderListener(null);
            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }
}
