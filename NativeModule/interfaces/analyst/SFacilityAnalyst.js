import {
  NativeModules,
  DeviceEventEmitter,
  NativeEventEmitter,
  Platform,
} from 'react-native'
import { EventConst } from '../../constains/index'
let Analyst = NativeModules.SFacilityAnalyst

/**
 *
 * 加载设施网络分析模型
 * @param datasourceInfo
 * {
      alias,
      server,
      engineType,
 * }
 * @param setting
 * {
 *  networkDataset / networkLayer,
 *  weightFieldInfos: [ {name, ftWeightField, tfWeightField} ],
 *  barrierEdges: [],
 *  barrierNodes: [],
 *  directionField,
 *  edgeIDField,
 *  fNodeIDField,
 *  nodeIDField,
 *  tNodeIDField,
 *  tolerance,
 * }
 * @returns {Promise.<void>}
 */
async function load (datasourceInfo = {}, setting = {}) {
  return Analyst.load(datasourceInfo, setting)
}

/**
 * 传入节点ID数组，查找与节点相连通的弧段ID数组
 * @param ids
 * @returns {Promise}
 */
async function findConnectedEdgesFromNodes (ids = []) {
  return Analyst.findConnectedEdgesFromNodes(ids)
}

/**
 * 设施网络路径分析，即根据给定的起始和终止结点 ID，查找其间耗费最小的路径，返回该路径包含的弧段、结点及耗费。
 * @param startNodeID
 * @param endNodeID
 * @param weightName
 * @param isUncertainDirectionValid
 * @returns {Promise.<{coast: Promise.coast, edges: Promise.edges, nodes: Promise.nodes}>}
 */
async function findPathFromNodes (startNodeID = -1, endNodeID = -1, weightName = 'Length', isUncertainDirectionValid = true) {
  return Analyst.findPathFromNodes(startNodeID, endNodeID, weightName, isUncertainDirectionValid)
}

/**
 * 设置起点
 * @param point
 * @returns {Promise.<Promise|Promise.<void>>}
 */
async function setStartPoint (point) {
  return Analyst.setStartPoint(point)
}

/**
 * 设置起点
 * @param point
 * @returns {Promise.<Promise|Promise.<void>>}
 */
async function setEndPoint (point) {
  return Analyst.setEndPoint(point)
}

async function clear () {
  return Analyst.clear()
}

export default {
  load,
  findPathFromNodes,
  findConnectedEdgesFromNodes,
  setStartPoint,
  setEndPoint,
  clear,
}