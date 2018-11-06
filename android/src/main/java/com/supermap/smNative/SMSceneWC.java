package com.supermap.smNative;

import com.supermap.data.Datasource;
import com.supermap.data.DatasourceConnectionInfo;
import com.supermap.data.EngineType;
import com.supermap.data.Enum;
import com.supermap.data.Workspace;
import com.supermap.data.WorkspaceConnectionInfo;
import com.supermap.data.WorkspaceType;
import com.supermap.data.WorkspaceVersion;
import com.supermap.realspace.SceneControl;

import java.util.Map;

public class SMSceneWC {
    Workspace workspace;
    SceneControl sceneControl;
    public Workspace getWorkspace() {
        return workspace;
    }
    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }
    public SceneControl getSceneControl() {
        return sceneControl;
    }
    public void setSceneControl(SceneControl sceneControl) {
        this.sceneControl = sceneControl;
    }
    public boolean openWorkspace(Map data) {
        try {
            WorkspaceConnectionInfo info = new WorkspaceConnectionInfo();
            if (data.containsKey("name")) {
                info.setName(data.get("name").toString());
            }
            if (data.containsKey("password")) {
                info.setPassword(data.get("password").toString());
            }
            if (data.containsKey("server")) {
                info.setServer(data.get("server").toString());
            }
            if (data.containsKey("type")) {
                Double type = Double.parseDouble(data.get("type").toString());
                info.setType((WorkspaceType) Enum.parse(WorkspaceType.class, type.intValue()));
            }
            if (data.containsKey("user")) {
                info.setUser(data.get("user").toString());
            }
            if (data.containsKey("version")) {
                Double version = Double.parseDouble(data.get("version").toString());
                info.setVersion((WorkspaceVersion) Enum.parse(WorkspaceVersion.class, version.intValue()));
            }

            boolean result = this.workspace.open(info);
            info.dispose();
            this.sceneControl.getScene().setWorkspace(this.workspace);
            return result;
        } catch (Exception e) {
            throw e;
        }
    }
    public Datasource openDatasource(Map data) {
        try {
            DatasourceConnectionInfo info = new DatasourceConnectionInfo();
            if (data.containsKey("alias")){
                String alias = data.get("alias").toString();
                info.setAlias(alias);

                if (this.workspace.getDatasources().indexOf(alias) != -1) {
                    this.workspace.getDatasources().close(alias);
                }
            }
            if (data.containsKey("engineType")){
                Double type = Double.parseDouble(data.get("engineType").toString());
                info.setEngineType((EngineType) Enum.parse(EngineType.class, type.intValue()));
            }
            if (data.containsKey("server")){
                info.setServer(data.get("server").toString());
            }


            if (data.containsKey("driver")) info.setDriver(data.get("driver").toString());
            if (data.containsKey("user")) info.setUser(data.get("user").toString());
            if (data.containsKey("readOnly")) info.setReadOnly(Boolean.parseBoolean(data.get("readOnly").toString()));
            if (data.containsKey("password")) info.setPassword(data.get("password").toString());
            if (data.containsKey("webCoordinate")) info.setWebCoordinate(data.get("webCoordinate").toString());
            if (data.containsKey("webVersion")) info.setWebVersion(data.get("webVersion").toString());
            if (data.containsKey("webFormat")) info.setWebFormat(data.get("webFormat").toString());
            if (data.containsKey("webVisibleLayers")) info.setWebVisibleLayers(data.get("webVisibleLayers").toString());
            if (data.containsKey("webExtendParam")) info.setWebExtendParam(data.get("webExtendParam").toString());

            Datasource dataSource = this.workspace.getDatasources().open(info);
            info.dispose();

            return dataSource;
        } catch (Exception e) {
            throw e;
        }
    }
}
