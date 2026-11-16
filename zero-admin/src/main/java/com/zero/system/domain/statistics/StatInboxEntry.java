package com.zero.system.domain.statistics;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/16 22:02
 * 说明: MySQL回退队列读取记录，以独立于事件正文的游标保证坏事件不会阻塞后续记录。
 */
@Getter
@Setter
public class StatInboxEntry
{
    /** 队列表中的事件编号，必须与正文中的编号一致。 */
    private String eventId;
    /** 不包含业务请求正文的精简事件JSON。 */
    private String payload;
    /** 回退入库时间，与事件编号共同组成稳定扫描游标。 */
    private LocalDateTime createdAt;
}
