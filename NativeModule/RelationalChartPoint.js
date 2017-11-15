/**
 * Created by will on 2016/7/5.
 */
import {NativeModules} from 'react-native';
let RCP = NativeModules.JSRelationalChartPoint;
import ChartPoint from './ChartPoint.js';

/**
 * @class Layer
 */
export default class RelationalChartPoint extends ChartPoint {
    constructor(){
        super();
        Object.defineProperty(this,'_SMRelationalChartPointId',{
            get:function(){
                return this.chartPointId
            },
            set:function(_SMRelationalChartPointId){
                this.chartPointId = _SMRelationalChartPointId;
            }
        });
    }

    /**
     * 获取关系名称
     * @memberOf RelationalChartPoint
     * @returns {Promise.<String>}
     */
    async getRelationalName(){
        try{
            var {name} = await RCP.getRelationalName(this._SMRelationalChartPointId);
            return name;
        }catch(e){
            console.error(e);
        }
    }

    /**
     * 设置关系名称
     * @memberOf RelationalChartPoint
     * @param {String} name
     * @returns {Promise.<Void>}
     */
    async setRelationalName(name){
        try{
            await RCP.setRelationalName(this._SMRelationalChartPointId,name);
        }catch(e){
            console.error(e);
        }
    }

    /**
     * 设置关系点
     * @memberOf RelationalChartPoint
     * @param {Array} pointsArr
     * @returns {Promise.<Void>}
     */
    async setRelationalPoints(pointsArr){
        try{
            var idArr =[];
            for(var i=0;i<pointsArr.length;i++){
                var point = pointsArr[i];
                idArr.push(point._SMRelationalChartPointId);
            }
            await RCP.setRelationalPoints(this._SMRelationalChartPointId,idArr);
        }catch(e){
            console.error(e);
        }
    }
}
