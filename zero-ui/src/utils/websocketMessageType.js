/**
 * 与后端对齐的 WebSocket 消息编号及业务消息判定；说明、展示形式和级别继续以服务端消息为准。
 */
// 与后端枚举编号保持一致；说明、展示形式和消息级别以服务端为准。
export const WebSocketMessageType = Object.freeze({
  CREATE_CONNECTION: 0,
  PING_PONG: 1,
  REFRESH_BROWSER: 2,
  LOGIN_OUT: 3,
  SYSTEM_NOTICE: 10,
  ORDER_CHANGED: 101,
  TASK_COMPLETED: 102
})

// 排除通信控制类型，业务编号只要求唯一整数，不设置起点或区间。
const controlTypes = new Set([
  WebSocketMessageType.CREATE_CONNECTION,
  WebSocketMessageType.PING_PONG,
  WebSocketMessageType.LOGIN_OUT,
])

export function isWebSocketBusinessType(value) {
  return Number.isInteger(value) && !controlTypes.has(value)
}
