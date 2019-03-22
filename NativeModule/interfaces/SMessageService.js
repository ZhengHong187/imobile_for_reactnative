/**
 * Created by imobile-xzy on 2019/3/13.
 */

import {
  EventConst
} from '../constains/index'
import {NativeModules, Platform, NativeEventEmitter,DeviceEventEmitter} from 'react-native';
let MessageServiceeNative = NativeModules.SMessageService;
const callBackIOS = new NativeEventEmitter(MessageServiceeNative);

function register(handlers) {
  try {

    if (handlers && typeof handlers.callback === "function") {
      if (Platform.OS === 'ios' && handlers){
        callBackIOS.addListener(EventConst.MESSAGE_SERVICE_RECEIVE, function (e) {
          handlers.callback(e)
        })
      } else if (Platform.OS === 'android' ) {

        DeviceEventEmitter.addListener(EventConst.MESSAGE_SERVICE_RECEIVE, function (e) {
          handlers.callback(e);
        });
      }
    }
  } catch (error) {
    console.log(error)
  }
}

// function unRegister() {
//   try {
//
//     if (handlers && typeof handlers.callback === "function") {
//       if (Platform.OS === 'ios' && handlers){
//         NativeEventEmitter.removeListener(EventConst.MESSAGE_SERVICE_RECEIVE);
//       } else if (Platform.OS === 'android' ) {
//
//         DeviceEventEmitter.addListener(EventConst.MESSAGE_SERVICE_RECEIVE, function (e) {
//           handlers.callback(e);
//         });
//       }
//     }
//   } catch (error) {
//     console.log(error)
//   }
// }
// 连接服务
function connectService(ip, port,hostName,userName,passwd,userID) {
  return MessageServiceeNative.connectService(ip, port,hostName,userName,passwd,userID);
}
//断开服务链接
function disconnectionService() {
  return MessageServiceeNative.disconnectionService();
}
//消息发送
function sendMessage(message, targetID) {
  return MessageServiceeNative.sendMessage(message,targetID);
}
//文件发送
function sendFile(filePath, targetID) {
  return MessageServiceeNative.sendFile(filePath,targetID);
}
//声明多人会话
function declareSession(uuid) {
  return MessageServiceeNative.declareSession(uuid);
}

//声明多人会话
function joinSession(uuid) {
  return MessageServiceeNative.joinSession(uuid);
}

//退出多人会话
function exitSession(uuid) {
  return MessageServiceeNative.exitSession(uuid);
}

//开启消息接收
function receiveMessage(uuid) {
  return MessageServiceeNative.receiveMessage(uuid);
}

//开启消息接收
function startReceiveMessage(uuid,handle) {
  register(handle);
  return MessageServiceeNative.startReceiveMessage(uuid);
}

//关闭消息接收
function stopReceiveMessage() {
  return MessageServiceeNative.stopReceiveMessage();
}
export default {
  receiveMessage,
  stopReceiveMessage,
  startReceiveMessage,
  exitSession,
  joinSession,
  declareSession,
  sendFile,
  sendMessage,
  disconnectionService,
  connectService,
}