package com.supermap.map3D.toolKit;

import android.view.MotionEvent;
import android.view.View;

import com.supermap.data.FieldInfos;
import com.supermap.data.GeometryType;
import com.supermap.data.Workspace;
import com.supermap.realspace.Feature3D;
import com.supermap.realspace.Feature3DSearchOption;
import com.supermap.realspace.Feature3Ds;
import com.supermap.realspace.Layer3D;
import com.supermap.realspace.Layer3DType;
import com.supermap.realspace.Layer3Ds;
import com.supermap.realspace.Scene;
import com.supermap.realspace.SceneControl;
import com.supermap.realspace.Selection3D;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zym on 2018/11/8.
 */

public class TouchUtil {

    /**
     * 触摸单体化建筑获取属性
     * Desc:会和SceneControl的触摸事件冲突，确认没有其他方法是可以调用这个方法
     *
     * @param sceneControl
     * @param osgbAttribute 回调
     */
    public static void getAttribute(final SceneControl sceneControl, final OsgbAttribute osgbAttribute) {
        sceneControl.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case (MotionEvent.ACTION_UP):
                        Map<String, String> attributeMap = TouchUtil.getAttribute(sceneControl, event);
                        osgbAttribute.attributeInfo(attributeMap);
                        break;
                    default:
                        break;

                }
                return false;
            }
        });
    }

    /**
     * 触摸单体化建筑获取属性
     *
     * @param mSceneControl
     * @param event         触摸事件，虽然没有用还是要传MotionEvent，否则没有意义
     */
    public static Map<String, String> getAttribute(SceneControl mSceneControl, MotionEvent event) {
        Layer3Ds layer3ds = mSceneControl.getScene().getLayers();
        Map<String, String> attributeMap = new HashMap<>();

        // 返回给定的三维图层集合中三维图层对象的总数。
        int count = layer3ds.getCount();
        if (count > 0) {
            for (int i = 0; i < count; i++) {
                Layer3D layer = layer3ds.get(i);
                // 遍历count之后，得到三维图层对象
                // 返回三维图层的选择集。
                if (layer == null) {
                    continue;
                }
                final Selection3D selection = layer.getSelection();
                if (selection == null) {
                    continue;
                }
                if (layer.getName() == null) {
                    continue;
                }
                // 获取选择集中对象的总数
                if (selection.getCount() > 0) {
                    // 返回选择集中指定几何对象的系统 ID
                    int _nID = selection.get(0);
                    Scene tempScene = mSceneControl.getScene();
                    String sceneUrl = tempScene.getUrl();
                    // 本地数据获取

                    // 不管KML是在线和本地都是一样的方式。
                    // 不管矢量数据是在线和本地都是一样
                    // 只有倾斜数据的时候，本地从UDB中取，在线用json.
                    attributeMap.clear();
                    if (layer.getType() == Layer3DType.KML) {
                        Utils.KMLData(layer, _nID, attributeMap);
//                        layer.getSelection().clear();
                        return attributeMap;
                    }
                    FieldInfos fieldInfos = layer.getFieldInfos();
                    int FieldInfosCount = fieldInfos.getCount();
                    if (FieldInfosCount > 0) {
                        Utils.vect(selection, layer, fieldInfos, attributeMap);
                    } else {
                        // 在线和本地是不一样 本地是UDB 在线是Json
                        if (sceneUrl == null || sceneUrl.isEmpty() || sceneUrl.equals("")) {
                            Workspace mWorkspace = null;
                            mWorkspace = new Workspace();
                            Utils.urlNUll(tempScene, _nID, mWorkspace, attributeMap);
                        }
                        Utils.urlNoNULL(mSceneControl, sceneUrl, _nID, attributeMap);

                    }
                    return attributeMap;
                }
            }
        }
        attributeMap.clear();
        return attributeMap;
    }

    /**
     * 获取被选中的兴趣点Feature3D
     *
     * @param mSceneControl
     * @param event
     * @return
     */
    public static Feature3D getSelectFeature3D(SceneControl mSceneControl, MotionEvent event) {
        Layer3Ds layer3ds = mSceneControl.getScene().getLayers();
        // 返回给定的三维图层集合中三维图层对象的总数。
        int count = layer3ds.getCount();
        if (count <= 0) {
            return null;
        }
        // 遍历count之后，得到三维图层对象
        for (int i = 0; i < count; i++) {
            Layer3D layer = layer3ds.get(i);
            if (layer == null) {
                continue;
            }
            final Selection3D selection = layer.getSelection();
            if (selection == null) {
                continue;
            }
            if (layer.getName() == null) {
                continue;
            }
            // 获取选择集中对象的总数
            if (selection.getCount() <= 0) {
                continue;
            }
            // 返回选择集中指定几何对象的系统 ID
            int _nID = selection.get(0);
            //是否是kml图层
            if (layer.getType() == Layer3DType.KML) {
                return null;
            }
            Feature3Ds fer = layer.getFeatures();
            //Feature3Ds是否为空
            if (fer == null && fer.getCount() <= 0) {
                return null;
            }
            Feature3D fer3d = fer.findFeature(_nID, Feature3DSearchOption.ALLFEATURES);
            if (fer3d != null && fer3d.getGeometry().getType() == GeometryType.GEOPLACEMARK) {
                return fer3d;
            }else {
                return null;
            }

        }
        return null;
    }

    /**
     * 设置Feature3D的name
     */
    public static void setFeature3DName(Feature3D feature3D,String name){
        feature3D.setName(name);
    }

    /**
     * 设置Feature3D的Description
     */
    public static void setDescription(Feature3D feature3D,String description){
        feature3D.setDescription(description);
    }


    /**
     * 清除选择集
     *
     * @param sceneControl
     */
    public static void clearSelect(SceneControl sceneControl) {
        Layer3Ds layer3ds = sceneControl.getScene().getLayers();
        int count = layer3ds.getCount();
        for (int i = 0; i < count; i++) {
            Selection3D selection3d = layer3ds.get(i).getSelection();
            selection3d.clear();
        }
    }


    public interface OsgbAttribute {
        void attributeInfo(Map<String, String> attributeMap);
    }
}
