# 管理后台 WebSocket 集成与部署

## 已实现的边界

- 集成在线通信、用户部门角色变更刷新、API谷歌验证修改后全员刷新，以及强退、禁用和删除后的客户端退出；不接公告、充值、提现，不新增菜单、数据表或公网推送接口。
- 服务内端点为 `GET /ws/admin`；握手不代表登录，首帧通过 JWT 认证后才能接收业务事件。
- 同账号所有有效会话和标签页接收；单个登录会话跨所有 admin 节点最多 10 个连接，可配置。
- 所有业务消息仅发布 Redis，由每个节点订阅后投递自己的连接；发布节点不额外本地发送。
- 不存储离线消息，不补发、不记录已读，不保证浏览器收到；发送方法成功仅表示 Redis 接受发布。
- 真实连接保存在本节点。Redis 只保留消息和有期限的连接名额，不保存 WebSocket Session 或 JWT。
- 心跳、首帧认证和会话检查不刷新登录有效期。强退、禁用和删除主动撤销登录缓存并发送退出事件；发送前校验及周期检查作为失效兜底。

## 后端调用

业务代码在 `zero-admin` 中注入 `com.zero.framework.websocket.AdminWebSocketService`，按需要调用：

| 方法 | 接收范围 |
| --- | --- |
| `sendToUser(userId, message)` | 指定用户在所有 admin 节点上的有效连接 |
| `sendToUsers(userIds, message)` | 多个用户在所有节点上的有效连接，重复用户ID自动合并 |
| `broadcast(message)` | 所有 admin 节点上的有效在线连接 |

`userId` 是 `sys_user.user_id`，不是账号名称或登录会话编号。同一用户打开多个浏览器或标签页，各有效连接都会收到消息。接收人和数据权限由服务端业务层确定，不能直接信任浏览器提交的接收人。

### 统一消息对象与业务枚举

首帧只发送Token；认证成功响应、心跳及业务消息使用统一对象：

- 消息对象：`com.zero.framework.websocket.WebSocketMessageVo`。
- 类型枚举：`com.zero.common.enums.webSocket.WebSocketMessageType`。
- 前端编号：`src/utils/websocketMessageType.js` 导出的同名对象。

| 枚举项 | 编号 | 用途 |
| --- | --- | --- |
| CREATE_CONNECTION | 0 | 服务端认证成功，msgData包含心跳参数 |
| PING_PONG | 1 | 心跳请求及响应；客户端收到后不再次回复 |
| REFRESH_BROWSER | 2 | 全局刷新事件，收到后自动整页刷新，重新获取用户资料、权限和菜单 |
| LOGIN_OUT | 3 | 全局退出事件，停止连接及重连，调用退出接口并清理登录态，回到登录页 |

枚举的 `messageType` 是固定编号，不使用ordinal；`desc` 为中文说明；`displayType` 为展示形式；`messageLevel` 为普通、警告或错误。已移除msgWeight和openSendMsg，不再通过事件开关拦截业务推送。修改枚举后须重新编译发布。

新增业务事件使用唯一整数编号，不限制起点或区间，不能与控制类型编号重复，并同步前端编号。元信息由后端枚举维护，客户端只需要提交编号；伪造的说明、展示形式和级别不能覆盖枚举。业务Service发送入口仅允许业务事件，不能伪造认证、心跳和错误消息。

仅保留 `WebSocketMessageType` 及统一消息对象发送接口，旧枚举、旧消息工厂和旧Java发送重载已移除。浏览器不兼容旧type/event协议。

`WebSocketMessageVo.builder()` 用于组装业务消息；`id`、`sendTime` 发布时由服务端重新生成，调用方通过builder设置msgType、msgData及可选来源、接收人和forwardUrl：

```java
WebSocketMessageVo message = WebSocketMessageVo.builder()
        .msgType(WebSocketMessageType.BUSINESS_NOTICE)
        .msgData(Map.of("orderId", "10001", "status", "FINISHED"))
        .forwardUrl("/orders/detail")
        .build();
webSocketService.sendToUser(userId, message);
webSocketService.sendToUsers(userIds, message);
webSocketService.broadcast(message);
```

以上三种发送按业务场景选择，不要对同一事件全部调用。forwardUrl默认空字符串，只接受站内路由，页面自行决定是否展示跳转入口；通信工具不会自动导航。弹窗由全局展示器统一管理，页面订阅只处理自己的业务逻辑，避免重复调用弹窗。不引入参考项目MQ依赖及未实现的签名字段，senderIp、fromAdminId、toAdminId已补齐，接收用户可由sendMsg读取toAdminId，也可通过显式Service参数指定；登录会话和多用户接收名单不暴露给浏览器。

### 按参考项目的builder发送

```java
WebSocketMessageVo messageVo = WebSocketMessageVo.builder()
        .msgType(WebSocketMessageType.BUSINESS_NOTICE)
        .msgData("你好色彩")
        .senderIp("")
        .toAdminId(123456L)
        .forwardUrl("")
        .fromAdminId(null)
        .build();
webSocketService.sendMsg(messageVo);
```

通过Spring注入AdminWebSocketService。toAdminId对应sys_user.user_id，类型为Long，null时广播，非空必须大于0；错误值直接拒绝，不能回退广播。来源用户可选；节点IP由发送服务自动获取，两者均不参与认证或授权，不记录消息正文。业务方必须先完成接收范围及数据权限校验，不允许把浏览器提交的VO直接转发。

多个用户继续使用sendToUsers(userIds, messageVo)，此时VO的toAdminId留空；显式广播或多用户范围与VO接收人矛盾时拒绝。发送端复制数据并生成id、sendTime，不修改调用方对象。msgData继续支持文本、对象、列表及null（无业务载荷事件），不新增MQ签名或离线机制。

### 普通通知示例的前置配置

下面的普通通知Service及Vue示例使用BUSINESS_NOTICE（10），用于说明业务扩展，需要先在后端枚举及前端编号对象中增加相同项：

```java
// WebSocketMessageType枚举中新增；编号不得与现有事件重复。
BUSINESS_NOTICE(10, "业务通知", WebSocketDisplayType.NOTIFICATION, WebSocketMessageLevel.NORMAL)
```

```javascript
// 在websocketMessageType.js的Object.freeze对象定义中增加：
BUSINESS_NOTICE: 10
```

该示例类型不会自动加入源码。REFRESH_BROWSER（2）已保留为全局刷新指令，不用于普通卡片弹窗，也不再分发给页面订阅。只需刷新时仍直接发送REFRESH_BROWSER，无需新增通知枚举。

### 后端完整 Service 示例

下面统一使用 `SYSTEM_NOTICE` 事件，载荷包含 `title`、`content`。此类是供业务开发参考的示例，不会自动联动现有通知公告。可按包路径新增为 `zero-admin/src/main/java/com/zero/system/service/AdminMessagePushExampleService.java`，或把相应调用合并进已有业务 Service。

```java
package com.zero.system.service;

import java.util.Collection;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zero.common.utils.StringUtils;
import com.zero.common.enums.webSocket.WebSocketMessageType;
import com.zero.framework.websocket.WebSocketMessageVo;
import com.zero.framework.websocket.AdminWebSocketService;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/17
 * 说明: 管理后台消息推送使用示例，接收用户须由调用方完成业务权限校验
 */
@Service
public class AdminMessagePushExampleService
{
    @Autowired
    private AdminWebSocketService webSocketService;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 向指定用户的所有有效在线连接发送通知
     * @param userId 接收人的sys_user.user_id，必须为正数
     * @param title 通知标题，不能为空
     * @param content 通知正文，不能为空，不得包含登录凭证等敏感内容
     * @return
     * 返回值: String
     * 返回值说明: 已发布消息ID，不代表浏览器已收到
     */
    public String notifyUser(Long userId, String title, String content)
    {
        return webSocketService.sendToUser(
                userId,
                buildMessage(title, content));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 向多个用户的所有有效在线连接发送通知，重复用户ID自动合并
     * @param userIds 接收用户ID集合，不能为空，各ID必须为正数
     * @param title 通知标题，不能为空
     * @param content 通知正文，必须允许所有目标用户查看
     * @return
     * 返回值: String
     * 返回值说明: 已发布消息ID，离线用户不会补发
     */
    public String notifyUsers(Collection<Long> userIds, String title, String content)
    {
        return webSocketService.sendToUsers(
                userIds,
                buildMessage(title, content));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 向所有admin节点的有效在线连接广播通知
     * @param title 通知标题，不能为空
     * @param content 通知正文，必须允许全部登录用户查看
     * @return
     * 返回值: String
     * 返回值说明: 已发布消息ID，不承诺离线补发或可靠送达
     */
    public String notifyAllOnline(String title, String content)
    {
        return webSocketService.broadcast(
                buildMessage(title, content));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 校验并组装通知数据，保证前端收到的标题和正文均为非空字符串
     * @param title 通知标题
     * @param content 通知正文
     * @return
     * 返回值: WebSocketMessageVo
     * 返回值说明: 系统通知消息，msgData包含title、content
     */
    private WebSocketMessageVo buildMessage(String title, String content)
    {
        if (!StringUtils.hasText(title))
        {
            throw new IllegalArgumentException("通知标题不能为空");
        }
        if (!StringUtils.hasText(content))
        {
            throw new IllegalArgumentException("通知内容不能为空");
        }
        return WebSocketMessageVo.builder()
                .msgType(WebSocketMessageType.BUSINESS_NOTICE)
                .msgData(Map.of("title", title.trim(), "content", content.trim()))
                .build();
    }
}
```

### 在已有业务中调用

在已有业务 Service 中注入：

```java
@Autowired
private AdminMessagePushExampleService messagePushService;
```

下面三个调用相互独立，按业务需要选择。ID、标题和正文仅为示例，实际接收人须从已完成权限校验的业务数据中取得。

```java
// 指定一个用户。
String messageId = messagePushService.notifyUser(
        100L,
        "处理提醒",
        "你负责的任务有新的处理结果。");

// 指定多个用户。
messagePushService.notifyUsers(
        java.util.List.of(100L, 101L, 102L),
        "协作提醒",
        "你参与的任务已更新。");

// 所有在线用户，内容必须允许全部登录用户查看。
messagePushService.notifyAllOnline(
        "系统维护通知",
        "系统即将进行维护，请及时保存当前操作。");
```

以上发送入口用于 `zero-admin` 内部，没有接入现有业务，也没有通用 HTTP 推送接口。`zero-report`、`zero-file` 不直接注入 admin 的这个类；后续跨服务推送需另行确定内部调用与鉴权方式。

有数据库写操作时，应在事务提交成功后发送，避免前端收到通知后查询不到已提交的数据。发送失败和业务成功如何反馈，需按具体业务确定，不能把推送失败当成数据库事务已回滚。禁止发送密码、JWT、验证码、密钥等内容。

### 消息信封与业务数据

消息使用JSON，字段为id、sendTime、senderIp、fromAdminId、toAdminId、msgType、msgData、forwardUrl，不包含版本号或旧type/data/event/payload结构。所有admin节点与前端必须同步更新，后端重启后刷新页面。

```json
{
  "id": "服务端生成的消息ID",
  "sendTime": 1794840000000,
  "senderIp": "",
  "fromAdminId": null,
  "toAdminId": 100,
  "msgType": {
    "messageType": 10,
    "desc": "业务通知",
    "displayType": "NOTIFICATION",
    "messageLevel": "NORMAL"
  },
  "msgData": {
    "title": "处理提醒",
    "content": "任务已完成"
  },
  "forwardUrl": ""
}
```

首帧由全局连接工具自动发送，格式为 `{"token":"<当前登录Token>"}`，不再包含msgType或msgData。认证成功响应编号为0，msgData包含heartbeatInterval、idleTimeout（毫秒）；心跳请求和响应编号均为1。认证失败及协议错误只关闭连接，不再返回错误消息。Token只在认证首帧内存中处理，禁止放URL、日志、Redis分发或业务消息。

关闭码4401表示登录无效，4429表示达到连接限制，1013表示暂时不可用或认证超时；认证超时只重连，不清理登录态。协议错误沿用标准WebSocket关闭码。

## 前端使用与自动地址

前端使用 `src/utils/websocket.js` 的标签页单例。现有登录和路由初始化成功后启动，普通路由切换和主题切换不会重复连接；主动退出请求期间暂停通信并保留订阅，路由初始化也不能重新建连。主动退出成功后清理连接、定时器和订阅，失败时恢复原订阅及通信。收到LOGIN_OUT或4401时走被动退出，先停止连接，再调用退出接口，无论接口是否成功均清理原身份，不恢复重连。业务页面使用 Options API：

### Vue 页面完整示例

连接、登录认证、心跳和重连由全局流程处理，页面只管理自己的订阅。下面的组件兼容普通页面和 keep-alive 缓存页面：

```vue
<template>
  <div class="app-container">
    <el-button
      type="primary"
      :disabled="subscribed"
      @click="subscribeMessages"
    >
      订阅消息
    </el-button>

    <el-button
      :disabled="!subscribed"
      @click="unsubscribeMessages"
    >
      取消订阅
    </el-button>

    <p>当前页面订阅状态：{{ subscribed ? '已订阅' : '未订阅' }}</p>

    <el-alert
      v-if="lastMessage"
      :title="lastMessage.title"
      :description="lastMessage.content"
      type="info"
      :closable="false"
      show-icon
    />

    <template v-if="lastMessage">
      <p>消息编号：{{ lastMessage.id }}</p>
      <p>发送时间：{{ parseTime(lastMessage.sendTime) }}</p>
    </template>
  </div>
</template>

<script>
import { adminWebSocket } from '@/utils/websocket'
import { WebSocketMessageType } from '@/utils/websocketMessageType'

const NOTICE_EVENT = WebSocketMessageType.BUSINESS_NOTICE

export default {
  name: 'WebSocketMessageExample',

  data() {
    return {
      subscribed: false,
      lastMessage: null,
      disposeNoticeSubscription: null
    }
  },

  mounted() {
    this.subscribeMessages()
  },

  activated() {
    // 缓存页面再次进入时恢复订阅；首次进入重复调用也不会重复注册。
    this.subscribeMessages()
  },

  deactivated() {
    this.unsubscribeMessages()
  },

  beforeUnmount() {
    this.unsubscribeMessages()
  },

  methods: {
    subscribeMessages() {
      if (this.disposeNoticeSubscription) {
        return
      }

      this.disposeNoticeSubscription = adminWebSocket.subscribe(
        NOTICE_EVENT,
        this.handleNotice
      )

      this.subscribed = true
    },

    handleNotice(payload, message) {
      if (
        !payload ||
        typeof payload.title !== 'string' ||
        typeof payload.content !== 'string'
      ) {
        return
      }

      this.lastMessage = {
        id: message.id,
        sendTime: message.sendTime,
        title: payload.title,
        content: payload.content
      }

      // 列表页需要刷新时，可在这里调用并返回查询Promise：
      // return this.getList()
    },

    unsubscribeMessages() {
      const dispose = this.disposeNoticeSubscription

      this.disposeNoticeSubscription = null
      this.subscribed = false

      if (dispose) {
        dispose()
      }
    }
  }
}
</script>
```

页面首次进入或从缓存返回时自动订阅，离开、卸载时取消订阅。按钮控制当前这次页面访问的订阅状态；再次进入页面仍会自动订阅。`mounted` 和首次 `activated` 即使都执行，也只注册一次监听。

取消页面订阅不会断开全局连接，其它页面的订阅继续工作。页面不调用全局 `stop()`；`pause()`、`resume()`、`stop()` 由登录和退出流程统一管理。

`subscribed` 只表示当前页面已注册监听，不代表网络一定连通。网络中断期间由全局工具重连，离线消息不补发，所以列表页进入或重新激活时仍应通过 HTTP 查询当前数据。将该组件接入缓存菜单时，组件 `name` 按项目规则与菜单路由的名称保持一致。

### 回调参数与单独取消订阅

`subscribe(messageType, callback)` 返回取消当前监听的函数。回调第一个参数为后端消息对象中的msgData（下面示例变量名payload），第二个参数 `message` 是完整信封：

```javascript
import { adminWebSocket } from '@/utils/websocket'
import { WebSocketMessageType } from '@/utils/websocketMessageType'

const unsubscribe = adminWebSocket.subscribe(
  WebSocketMessageType.BUSINESS_NOTICE,
  (payload, message) => {
    // 业务数据。这里仅输出示例中的非敏感字段。
    console.log(payload.title)
    console.log(payload.content)

    // 信封中的消息编号和服务端毫秒时间戳。
    console.log(message.id)
    console.log(message.sendTime)
  }
)

// 需要取消时调用，例如组件卸载或缓存页面离开。
unsubscribe()
```

上面最后一行展示取消方式；实际页面按生命周期调用，不要订阅后立即取消。一个页面订阅多个事件时，分别保存各自返回的取消函数并逐一调用。业务事件在后端 `WebSocketMessageType` 中维护，前端使用同名编号常量订阅，例如ORDER_CHANGED、TASK_COMPLETED，编号必须一致。第二个参数message还包含msgType及forwardUrl；页面跳转仍须走自己的路由和权限规则。

### 自动生成连接地址

地址直接由 `VITE_APP_BASE_API` 和浏览器地址生成：

| API 配置 | 页面来源 | 连接地址 |
| --- | --- | --- |
| `/dev-api` | `http://localhost` | `ws://localhost/dev-api/ws/admin` |
| `/prod-api` | `https://admin.example.com` | `wss://admin.example.com/prod-api/ws/admin` |
| `https://api.example.com/admin-api` | `https://admin.example.com` | `wss://api.example.com/admin-api/ws/admin` |

不增加系统参数或 WebSocket 域名配置。HTTPS 页面不允许连接 HTTP API。配置错误仅停止 WebSocket 并输出固定提示，不阻断 HTTP 页面初始化。

## 集群配置

默认配置在 admin 的 `application.yml`，前缀 `zero.websocket`：

| 配置 | 默认值 | 用途 |
| --- | --- | --- |
| `channel` | 必填；Nacos示例为 `zero:${spring.profiles.active:default}:admin:ws` | 环境隔离频道，所有节点一致；缺失时拒绝启动 |
| `allowed-origins` | `[]` | 默认同源；独立 API 域名需列出页面源 |
| `auth-timeout` | `5000` | 首帧认证期限，毫秒 |
| `heartbeat-interval` | `30000` | 心跳间隔，毫秒 |
| `idle-timeout` | `90000` | 无有效客户端消息的关闭期限，毫秒，须大于心跳间隔 |
| `session-check-interval` | `30000` | 集中核对登录会话间隔，毫秒 |
| `max-text-bytes` | `65536` | 单条 UTF-8 文本字节上限 |
| `max-connections-per-session` | `10` | 单登录会话跨节点合计连接上限 |
| `max-pending-connections` | `100` | 单节点未认证连接上限 |
| `send-timeout` | `10000` | 单连接发送期限，毫秒 |
| `send-buffer-bytes` | `262144` | 单连接发送缓冲上限 |
| `send-threads` | `4` | 专用发送线程数 |
| `send-queue-capacity` | `1000` | 等待发送任务上限，满时关闭对应连接 |

跨域示例（仅作为配置说明，不自动写入 Nacos）：

```yaml
zero:
  websocket:
    channel: zero:prod:admin:ws
    allowed-origins:
      - https://admin.example.com
```

来源只能是明确的 HTTP/HTTPS 源，不包含路径、通配符、用户信息或查询参数。

所有节点必须共享登录 Redis、相同的 JWT 签名配置和环境消息频道。Redis Pub/Sub 不按数据库编号隔离，测试和生产必须使用不同频道。连接名额使用相同频道前缀下的 Redis 有序集合和原子脚本，申请、续租和过期判断统一使用 Redis 服务端时间，避免节点时钟偏差误回收其它节点名额；节点宕机后旧名额通过租约回收。默认租约 90 秒，长时间停顿后旧连接不再复用过期名额。

Redis 故障时停止业务投递并关闭相关连接，前端退避重连。Redis 恢复后重新订阅，仅接收新消息。按既有运维方式监测 Redis 可用性和固定错误日志，不记录消息正文。

## 代理部署

Vite 已在现有 API 代理开启 WebSocket，并在升级请求中保留页面 Host，以满足默认同源校验。

生产 Nginx 示例，域名及端口均为示例值；只添加匹配当前 API 前缀的 WebSocket 入口，保留现有其它路由：

```nginx
# 放在http上下文。
map $http_upgrade $zero_ws_connection {
    default upgrade;
    '' close;
}

upstream zero_admin_nodes {
    server 127.0.0.1:9099;
    server 127.0.0.1:19099;
}

# 放在现有server上下文；TLS沿用该server现有配置。
location = /prod-api/ws/admin {
    proxy_pass http://zero_admin_nodes/ws/admin;
    proxy_http_version 1.1;
    proxy_set_header Upgrade $http_upgrade;
    proxy_set_header Connection $zero_ws_connection;
    # 保留浏览器原始域名及端口，避免同源校验因内部节点Host而失败。
    proxy_set_header Host $http_host;
    proxy_set_header X-Forwarded-Proto $scheme;
    proxy_set_header X-Forwarded-Port $server_port;
    proxy_read_timeout 120s;
    proxy_send_timeout 15s;
    proxy_buffering off;
}
```

不需要会话粘滞。真实部署如果使用其它 API 前缀、Servlet context-path 或多层网关，需要同步保留前缀重写、Host、Origin 和 Upgrade 转发。WebSocket 地址自动生成不会自动修改代理配置。尚未修改线上 Nginx、Nacos 或任何数据库。

HTTPS 在 Nginx 终止、节点内部使用 HTTP 时，仅保留 Host 不足以完成同源检查。此时还需让 Tomcat 识别可信代理传入的协议和外部端口。针对上面的本机 Nginx 示例，在实际生效的部署配置中合并以下设置（不自动修改）：

```yaml
server:
  forward-headers-strategy: native
  tomcat:
    remoteip:
      protocol-header: X-Forwarded-Proto
      port-header: X-Forwarded-Port
      internal-proxies: '127\.0\.0\.1|::1|0:0:0:0:0:0:0:1'
```

多机代理时将 `internal-proxies` 改为实际可信代理地址，不能配置为信任所有来源；节点入口应限制为可信代理可访问，代理负责覆盖转发头。已有转发头配置的部署先核对其实际效果，不重复增加另一套过滤器。配置依据见 [Spring Boot 3.5 代理部署说明](https://docs.spring.io/spring-boot/3.5/how-to/webserver.html) 和 [转发端口配置](https://docs.spring.io/spring-boot/3.5/appendix/application-properties/)。

## 验证命令

```powershell
# 仓库根目录，JDK17。
mvn -pl zero-admin -am compile -DskipTests
mvn -pl zero-admin -am test '-Dtest=AdminWebSocketTest,OnlineAuthorizationTokenTest,SpringDocCompatibilityTest' '-Dsurefire.failIfNoSpecifiedTests=false'

# 仅在显式提供本地隔离Redis端口时运行真实网络集成测试，默认跳过该类。
$env:ZERO_WS_TEST_REDIS_PORT = '<隔离Redis端口>'
mvn -pl zero-admin -am test '-Dtest=AdminWebSocketIntegrationTest' '-Dsurefire.failIfNoSpecifiedTests=false'

# zero-ui目录。
npm test
npm run build:prod
```

集成测试只装配真实 WebSocket、Security、JWT 和 Redis 组件，启动独立 Tomcat 节点；它不等于完整 admin 服务与 Nacos、数据库、生产反向代理联调。真实浏览器生产域名、TLS 和负载均衡部署仍需在目标环境验收。

## 首次集成验证记录（2026-11-17）

- Windows Java 17：admin 编译通过；9 项 WebSocket 隔离测试、3 项原有在线会话回归、2 项原有文档安全链回归通过。
- Linux Java 17 Docker + 隔离 Redis 7.2.5：2 项真实网络集成测试通过，覆盖跨节点定向/广播、不重复投递、多连接限额、错误来源、首帧超时、伪造凭证、会话过期、心跳不续期、订阅停止恢复及节点重建。
- Windows 本机 Java 的嵌入 Tomcat 曾因 JDK 回环连接异常无法启动；真实网络结果来自 Linux Docker，未把该环境失败标为业务通过。
- Node 24.21.0：35 项前端单元测试和生产构建通过；保留原有 Browserslist、CSS `:export` 和大包提示，未改框架依赖。
- Chromium + 模拟 HTTP/WebSocket 响应：登录后自动连接、认证帧、心跳、业务订阅、四主题切换、路由切换、正常退出及认证失效返回登录页均检查通过；主题和路由切换保持一个业务连接。此项不代表完整后台真实登录联调。
- 使用真实 Vite 代理和隔离升级服务检查：路径重写为 `/ws/admin`、HTTP 101 升级及保留同源 Host 均通过。
- 尚未执行生产 TLS/Nginx、多机 Nacos/数据库联调、Redis 服务重启及大规模并发/慢网络压测。

## 复查修复记录（2026-11-17）

- 认证超时改为可重试关闭码，避免 Redis 或网络延迟误清理正常登录态；在名额申请和最终会话校验结束后重新检查期限。申请期间退出的会话不能收到 AUTH_OK。
- 同一连接并发 AUTH 只能有一次认证成功，不能重复占名额或同时注册两个身份；节点停止后拒绝新连接。
- Redis 租约改用 Redis TIME，增加两节点 20 次并发申请测试，确保总连接数不突破同一会话上限；过期、释放的名额不能续租复活。
- 退出流程增加可恢复暂停；HTTP 失败保留登录态和原订阅，成功及 FedLogOut 才彻底清理。并发退出请求未全部失败时不恢复，迟到失败不能复活已清理的连接。
- 只有收到 PONG 才重置重连退避，避免服务端反复认证成功后立即断开导致一秒重连循环；重复 start 不覆盖原认证失败回调。
- 消费异步订阅的 Promise 拒绝；某个订阅回调退出后，同条消息不再送给其它旧观察者。
- 本轮后端 23 项测试通过：15 项 WebSocket 隔离测试、3 项真实 Redis 多节点集成测试、3 项在线会话回归及 2 项文档安全链回归。包含并发认证、两节点 20 次并发申请、认证阶段会话失效、两处阻塞校验超时、节点停止时拒绝新连接。
- 本轮前端 44 项单元测试及 Node 24.21.0 生产构建通过，覆盖退出成功/失败、并发暂停、旧回调清理、异步订阅错误、认证成功后立即断开的退避。生产环境验证边界与上文一致。

## 消息字段调整（2026-11-17）

- 已移除协议版本常量、消息中的 `version` 字段以及前端、后端、Redis订阅的版本判断，继续使用 JSON 消息及现有类型、认证、心跳和事件处理。
- 示例和测试均改用不带版本号的消息，并验证认证响应、心跳请求和多节点广播不包含该字段。
- 后端 20 项相关测试、3 项真实 Redis 多节点集成测试、前端 44 项单元测试及 Node 24.21.0 生产构建通过。
- 发布时全部 admin 节点同步更新并重启，前端同步更新后刷新页面，避免新旧消息格式混用。

## 消息类型与事件枚举调整（2026-11-17，历史记录，已被下述统一协议替代）

- 新增 `WebSocketMessageType`、`WebSocketEvent` 两个独立枚举，消息类型字段和后端发送接口使用枚举，类型常量不再放在握手路径常量类中。
- JSON仍输出 `"type": "EVENT"`、`"event": "order.changed"`，前端订阅方式保持一致。新增业务事件在 `WebSocketEvent` 中维护，不需要修改消息封装和Redis分发逻辑。
- 后端22项相关测试及3项真实Redis多节点集成测试通过，覆盖枚举序列化/反序列化、原JSON字符串值、空枚举拒绝、认证、心跳和跨节点投递；前端44项单元测试通过。前端源码未改动。


## 统一参考项目消息结构（2026-11-17）

- 认证、心跳、错误和业务事件全部使用WebSocketMessageType枚举及WebSocketMessageVo对象，统一为id、sendTime、msgType、msgData、forwardUrl；不再发送旧type/data/event/payload字段，不含协议版本号。
- messageType输出messageType、desc、msgWeight、openSendMsg；客户端及Redis反序列化按编号还原后端枚举，不能覆盖中文说明、权重和开关。编号缺失、非整数或未知编号的客户端帧拒绝。
- Service统一使用消息对象发送方法，旧Java适配方法已移除；公开业务入口拒绝控制类型，发送开关关闭时不发布Redis，forwardUrl仅接受站内路由。元数据由服务端生成，业务数据序列化为快照。
- 前端按数字枚举订阅，业务回调接收msgData和完整消息；全局工具不自动导航、不自动刷新、不产生默认弹窗。认证失败停止重连，通信错误继续退避，心跳响应不触发回发循环。
- 原JWT验签、Redis会话读取、跨节点租约、多标签清理、退出暂停与恢复流程继续保留；本次未接入实际业务推送、未修改数据库及线上配置。
- 部署必须同步更新全部admin节点与前端。旧浏览器协议不兼容，不能混用新旧节点；完成后重启后端并刷新前端页面。
- 本轮验证：Windows Java17后端25项测试通过；Linux Java17与隔离Redis的3项真实多节点集成测试通过，包含定向、多个用户、广播、用户去重、统一元信息、跳转字段、心跳、会话失效及节点恢复；Node24.21.0前端50项测试及生产构建通过。生产TLS/Nginx与完整Nacos/数据库联调仍未执行。

## 旧枚举清理（2026-11-17）

- 已删除旧消息类型与旧业务事件枚举，同时移除对应的消息工厂和发送重载；认证、心跳、业务及测试统一使用WebSocketMessageType。上面的旧枚举调整仅为历史记录。
- 消息JSON结构、JWT认证、Redis多节点投递和前端生命周期保持不变。
- 删除后验证：后端25项相关测试、3项隔离Redis真实多节点集成测试通过；首次集成测试启动期间未找到测试类，同期target产物被重新生成，产物稳定后重试通过。前端源码未改动。

## VO字段与builder发送（2026-11-17）

- 消息类统一为WebSocketMessageVo，补齐senderIp、fromAdminId、toAdminId；保留id、sendTime、msgType、msgData、forwardUrl。sendMsg按toAdminId决定定向或广播，继续经Redis分发到全部admin节点。
- 来源字段可选，仅为元数据，不用于认证；不增加MQ或签名功能。业务发送由builder构造并生成独立快照，接收人非法或与显式发送范围矛盾时拒绝。认证及心跳身份仍由现有JWT与服务端会话索引决定。
- 本轮验证：后端编译及26项相关测试通过，3项隔离Redis真实多节点集成测试通过，验证builder消息、来源及接收人字段、sendMsg跨节点定向/广播和原认证、租约、心跳流程；未向真实用户推送。前端源码未改动。

## 展示形式和级别（2026-11-17）

已移除msgWeight、openSendMsg及对应发布、订阅开关判断。消息元信息由服务端枚举确定，客户端无法覆盖；认证、心跳和通信错误使用NONE，不弹业务提示。

| displayType | 展示形式 |
| --- | --- |
| NONE | 不弹窗，页面仍可订阅事件 |
| NOTIFICATION | 右上角卡片，6秒自动关闭 |
| MESSAGE | 顶部提示，4秒自动关闭 |
| MESSAGE_BOX | 居中消息弹窗，逐条确认；内部使用独立ElDialog避免干扰其它确认框 |

messageLevel为NORMAL普通（info样式）、WARNING警告（warning样式）、ERROR错误（error样式）。使用Element Plus原生样式和现有主题，不固定某一种主题颜色。

例如，将现有事件声明改为：

```java
REFRESH_BROWSER(2, "刷新浏览器", WebSocketDisplayType.MESSAGE_BOX, WebSocketMessageLevel.WARNING)
```

仍按原builder方式发送，无需额外设置展示参数：

```java
webSocketService.sendMsg(WebSocketMessageVo.builder()
        .msgType(WebSocketMessageType.REFRESH_BROWSER)
        .msgData(Map.of("title", "维护提醒", "content", "请及时保存当前操作。"))
        .toAdminId(123456L)
        .build());
```

msgData为字符串时直接展示；为对象时仅读取title、content文本。其它对象或列表仍交给页面订阅处理，不直接转换成弹窗内容。不执行HTML、不自动打开forwardUrl、不自动刷新浏览器。

WebSocket展示器按消息ID去重，非模态提示同时最多3条，居中弹窗等待队列最多50条（满后移除最早的等待项），避免消息风暴；不代表可靠送达或已读。退出暂停、退出成功、登录失效和身份切换清理自身弹窗及队列，不关闭HTTP流程的确认框。断线重连保留现有提示及去重状态。生产多主题实际显示仍应在目标环境验收。

- 本轮验证：后端26项测试及3项隔离Redis真实多节点集成测试通过，前端58项测试及生产构建通过；覆盖新元信息往返、控制消息不弹窗、级别映射、重复消息、数量限制、居中排队及退出清理。未向真实用户推送，未完成浏览器多主题外观验收。

## 枚举改名与编号规则（2026-11-17）

- Java统一使用com.zero.common.enums.webSocket.WebSocketMessageType，VO、处理器、发送服务及测试已同步；前端编号定义改为src/utils/websocketMessageType.js。
- 移除BUSINESS_TYPE_START及前端100起点判断。业务事件编号只要求为唯一整数，不能与认证、心跳、连接响应、通信错误等控制类型重复。新增控制类型时须同时维护后端isBusinessEvent与前端controlTypes；普通业务事件无需配置编号范围。
- @Getter生成只读getter，可替换为手写getMessageType、getDesc、getDisplayType、getMessageLevel。@JsonFormat(OBJECT)使枚举输出完整元信息对象，当前保留。若改用@JsonValue返回DTO或Map也能保持对象格式；不能直接改成STRING或NUMBER，否则前端无法读取展示配置，Redis反序列化也须同步更改。
- 消息字段仍为messageType，不因Java枚举改名而修改已有JSON字段。

## 用户ID类型（2026-11-17）

fromAdminId、toAdminId统一为Long，对应管理后台sys_user.user_id。接收人默认null，sendMsg仅在null时广播，0和负数拒绝；来源ID可空，非空必须大于0。多个用户发送时toAdminId保持null，来源信息不用于认证授权。Java调用使用123456L，不再填写空字符串或字符串ID；JSON字段为数字或null。全部节点需同步更新并重启。
- 字段已统一为Long类型fromAdminId、toAdminId，发送入口、来源校验、Redis信封及示例同步。验证：后端26项测试及3项隔离Redis真实双节点测试通过；枚举改名及取消编号区间后前端70项测试和生产构建通过。一次测试遇到target中租约编译类暂时缺失，产物稳定后重跑通过，未向真实用户推送。

## 认证失败只关闭连接（2026-11-17）

已删除AUTHORIZATION_INVALID、AUTHORIZATION_EMPTY、AUTHORIZATION_VERIFY_ERROR三个枚举及前端分支。首帧JWT认证成功才返回CREATE_CONNECTION（0）及心跳参数；缺少凭证、伪造凭证直接使用4401关闭，不发送错误消息。未完成认证的协议错误也只关闭，不返回消息；Redis临时异常或认证超时仍使用1013，允许退避重连，不误清理HTTP登录。会话失效仍以4401关闭，由前端清理登录态。认证后的心跳、多节点投递及业务展示保持不变，所有节点与前端须同步发布。

## 自动填充发送节点IP（2026-11-17）

senderIp由服务端IpUtils自动读取并覆盖调用方输入，业务builder可以省略。优先本机启用网卡的内网IPv4，没有时取其它有效IPv4，无可用地址或读取失败回退回环地址；不查询外部网站，不能识别NAT后的公网出口。容器部署得到的是容器网卡地址，不保证是宿主机IP。节点地址按JVM缓存，网卡变化后重启重新读取。业务发布节点在进入Redis前填写，其它订阅节点转发时不覆盖；认证成功及心跳响应由当前连接节点填写。不输出IP日志，不修改调用方业务对象。
- 验证：28项后端测试、3项隔离Redis真实双节点测试通过，覆盖自动覆盖调用方senderIp、网卡地址选择、无候选兜底以及认证失败直接关闭。前端认证调整后的65项测试和生产构建通过；未向真实用户推送。

## IP工具合并（2026-11-17）

本机网卡地址选择、异常兜底和JVM缓存已合并到IpUtils，通过IpUtils.getLocalIpAddress()调用；独立的本机IP工具类已删除，WebSocket调用及测试同步调整。原IpUtils其它方法保持不变。28项后端相关测试及编译通过，本次纯工具合并未重复真实双节点联调。

## 首帧Token协议简化（2026-11-17）

删除AUTHORIZATION和MESSAGE_ERROR类型。浏览器在WebSocket onopen后只发送包含token字符串字段的JSON对象，后端检查首帧期限、JWT及Redis登录会话，认证成功才返回CREATE_CONNECTION（0）和心跳参数，再加入业务连接索引；握手完成本身不代表认证成功。缺少或非法凭证以4401直接关闭；认证超时或Redis临时故障以1013关闭并允许重连；坏JSON、非法协议及不支持的认证后消息使用标准关闭码直接断开，不发送错误报文。

认证后仅接受PING_PONG（1）心跳；再次发送Token帧直接按协议错误关闭，不切换身份。心跳、会话检查不续期HTTP登录，Redis名额和多节点业务投递不变。前端统一通过onclose处理：4401清理登录态，其它异常退避重连。控制编号只保留0、1；新增业务编号仍须唯一。旧首帧格式不兼容，全部节点和前端必须同步发布。

- 本轮验证：29项后端测试、3项隔离Redis真实双节点集成测试、65项前端测试及生产构建通过。覆盖首帧仅Token、非法及伪造凭证、认证后重复Token及协议错误无报文关闭、心跳、多节点投递和退出清理。未向真实用户推送，生产TLS及完整后台联调仍未执行。

## 用户部门、角色变更后刷新（2026-11-17）

- 用户编辑：先读取原部门和角色，只有部门或角色ID集合实际变化、保存成功且事务提交后，向该用户发送REFRESH_BROWSER（2）。角色顺序或重复ID不算变化；仅修改昵称、手机等资料不发送。
- 用户授权角色：角色集合发生变化后提交通知；角色管理的添加用户、取消单用户或批量取消，在写入成功并提交后通知选中的相关用户。角色定义、菜单授权及数据范围定义修改不属于本次自动通知范围。
- AdminUserRefreshService统一注册事务afterCommit回调，回滚不发送；Redis失败只记录固定提示，不让已提交的修改显示成事务失败。调用方权限、操作日志和原关系表写入保留。
- 不查询单节点在线表，消息统一通过Redis按用户ID分发，因此同用户跨节点、多会话、多标签页的有效在线连接均可收到；离线连接不接收、不落库、不补发。只发送定向消息，不广播。
- 全局连接工具收到2号事件后调用页面刷新，每页只触发一次，不同时弹通知卡片；普通路由跳转不会重复注册，退出及旧连接回调不会触发刷新。刷新会重载当前页面，尚未保存的页面输入会丢失。
- /getInfo读取最新用户及角色快照；部门、角色ID或菜单权限变化时更新当前登录会话缓存，随后重新生成菜单路由。该更新发生在HTTP初始化请求中，WebSocket心跳和推送仍不续期登录。
- 推送为在线尽力通知，Pub/Sub丢失或发布失败不保证页面立即刷新；重新进入页面或主动刷新时仍通过getInfo获取最新数据。未增加离线消息、权限版本或可靠投递机制。

- 验证：52项后端测试通过（含真实Spring事务回调+模拟JDBC的提交/回滚、用户编辑与授权入口、最新登录快照、安全权限回归）；3项隔离Redis真实双节点测试通过，包含用户变更通知跨节点定向投递；67项前端测试及生产构建通过。尚未执行真实数据库账号修改到浏览器刷新的完整业务联调，未操作真实用户、角色授权或线上配置。

## 链路复查及最终字段命名（2026-11-17）

- VO类型字段为msgType（WebSocketMessageType），业务载荷为msgData（Object）；枚举内编号字段为messageType（int）。首帧仍只发送token，不包装为VO。心跳和业务消息示例：

```json
{
  "msgType": {
    "messageType": 2,
    "desc": "刷新浏览器",
    "displayType": "NOTIFICATION",
    "messageLevel": "NORMAL"
  },
  "msgData": { "content": "部门或角色已变更" }
}
```

- 修复认证成功响应与连接注册之间的竞态：验签和会话核对通过后先登记连接索引，发送任务按连接串行等待认证成功响应完成，避免刷新消息漏投或早于成功响应送达。关闭和超时仍可清理该连接。
- Redis接收端拒绝广播标记、接收集合及VO接收人冲突的信封，不把异常定向消息扩成广播；空目标、空消息ID不投递。
- 批量取消角色时，在同一事务内使用参数化查询锁定实际存在的目标关系，提交后只通知实际关联用户；不修改原增删SQL或接口权限。新增查询通过MyBatis动态SQL解析和空集合边界验证，尚未在真实业务数据库执行联调。
- 前端因Cookie变化重连到新身份时，重置该身份的刷新去重状态，旧身份回调不能清理或阻止新身份刷新；重复初始化和退出清理保持原规则。
- zero.websocket.channel现在必须显式配置，不再回退固定dev频道。请确认实际Nacos或启动配置中存在该项；缺失或空白将拒绝启动，防止跨环境共用频道。现有Nacos配置源文件及规划参数未改动，也未修改线上Nacos。
- 字段改名不兼容旧JSON，全部admin节点与前端必须同步更新并重启、刷新。部署时频道、登录缓存及JWT配置仍须在同环境各节点保持一致。

- 本轮最终验证：57项后端测试、3项隔离Redis真实双节点集成测试、68项前端测试及生产构建通过。覆盖msgType/msgData与枚举messageType的新JSON、认证响应窗口不丢事件且响应先于业务消息、环境频道缺失拒绝启动、异常收件人信封拒收、批量取消角色只通知实际关联用户及身份切换后的刷新恢复。新增关系查询已通过真实MyBatis动态SQL解析；未在真实业务MySQL执行角色变更联调，未修改真实账号或线上Nacos。

## API谷歌验证修改后全员刷新（2026-11-17）

修改保存入口PUT /system/apiAuth成功后，SysApiAuthServiceImpl在事务提交后调用AdminUserRefreshService.afterApiAuthChanged()，经现有Redis频道广播REFRESH_BROWSER（2），覆盖所有节点的有效在线用户、会话和标签页，包括当前操作者。

前端复用现有全局刷新处理，页面重载后按原初始化流程重新请求/system/apiAuth/googleAuthApis，更新或清理localStorage中的google_auth_apis。不在GET查询接口中触发通知，避免刷新循环；不发送配置详情、验证码或Token。

- 保留原配置规范化、唯一性校验、状态校验、修改权限及操作日志。
- 影响行数为0、校验失败或事务回滚不广播；推送异常仅记固定提示，不让已提交的保存操作返回失败。
- 本次接入修改保存入口，新增、软删除及系统参数总开关修改未额外接入全员刷新。
- 仍为在线尽力通知，不保存离线消息，不保证每个浏览器收到；离线客户端下次初始化时按原接口加载规则。
- 验证：58项后端相关测试、3项隔离Redis真实双节点测试、68项前端回归通过。覆盖修改提交后广播、回滚和校验失败不发送、推送失败不影响保存，以及两个节点三个在线连接收到相同消息ID且不重复。尚未执行真实数据库配置编辑到浏览器刷新的完整联调，未修改真实API谷歌规则或线上配置。

## 强退、禁用和删除后的客户端退出（2026-11-17）

- 在线用户“强退”：撤销所选登录会话，在Redis信封内限定用户ID和会话编号，跨节点通知该会话的所有标签页。同账号其它独立登录会话保留。
- 用户管理“禁用”和“删除”：数据库成功提交后，使用SCAN分批读取登录缓存，撤销目标账号的全部登录会话，定向发送LOGIN_OUT（3）；事务回滚、零行更新或启用操作不触发退出。编辑用户时提交禁用状态也执行相同流程。
- 退出事件仍只经过Redis Pub/Sub；服务端会话编号仅用于内部筛选，不作为浏览器消息字段，JWT不会进入频道。退出事件允许投递给首帧认证绑定过、但登录缓存刚被撤销的连接；普通业务消息仍必须通过发送前的登录有效性校验。
- 客户端收到LOGIN_OUT后先停止心跳和重连，再调用POST /logout；调用成功或失败都清理原身份的Token、权限、路由和标签页缓存，并返回/login。重复通知或后续4401不会重复退出。业务页面无需订阅LOGIN_OUT，也不能覆盖全局退出处理。
- 如果发送前会话检查已经关闭连接，客户端通过4401执行同样的退出流程。Pub/Sub仍不保证通知必达，离线不补发；Redis故障会产生固定告警，不能把通知成功当成会话撤销或浏览器确认。
- 退出请求固定使用触发退出时的Token；请求等待期间如果其它标签页已登录新身份，旧请求不能清除新登录态或把新身份导航回登录页。
- 后端入口为AdminUserLogoutService.forceLogout(sessionId)和afterUsersUnavailable(userIds)。业务禁用、删除应调用后者，不要只发送LOGIN_OUT后依赖浏览器撤销会话。它们属于服务端业务能力，不提供公网通用推送接口。
- 新增Redis信封sessionId字段及客户端退出处理，部署时所有admin节点和前端应同步更新。未更新的节点可能忽略会话范围，不支持混用旧版分发代码进行单会话强退。
- 本轮验证：64项后端回归、4项隔离Redis真实双节点集成、73项前端测试及生产构建通过。覆盖跨节点单会话强退、账号全部会话退出、其它用户保留、提交/回滚边界、编辑禁用优先退出、批量删除成功与零行结果、缓存或推送异常、退出接口失败清理及新身份保护。未操作真实在线账号，未进行真实业务数据库和完整浏览器操作联调。

## 强退退出流程复查（2026-11-17）

- 修复LOGIN_OUT与主动退出并发时，pause改变连接代次导致强退回调被取消的问题。强退回调现在核对失效身份标记；暂停不取消强退，彻底停止或新身份启动仍能阻止旧回调。
- 主动退出接口等待期间若已登录新身份，旧请求成功返回只释放暂停，不删除新Token、权限、路由和标签页缓存。普通主动退出失败仍沿用原恢复通信规则。
- 多节点分发范围保持不变：强退使用用户ID与登录会话编号共同筛选；禁用、删除提交后覆盖目标账号全部会话，普通业务发送仍校验会话有效性。
- 本轮验证：64项后端测试、4项隔离Redis真实双节点测试、75项前端测试及生产构建通过。新增覆盖强退后紧接主动退出暂停，以及主动退出旧请求成功返回时保护新身份。未操作真实在线账号，真实业务数据库和完整浏览器联调未执行。
