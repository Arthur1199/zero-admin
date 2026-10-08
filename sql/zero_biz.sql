drop table if exists biz_dict_group;
CREATE TABLE `biz_dict_group` (
                                  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                  `group_code` varchar(100) NOT NULL COMMENT '字典组编码，例如：order_status、pay_type',
                                  `group_name` varchar(100) NOT NULL COMMENT '字典组名称，例如：订单状态、支付方式',
                                  `status` char(1) NOT NULL DEFAULT '1' COMMENT '状态：1:正常，0:停用',
                                  `del_flag` char(1) NOT NULL DEFAULT '0' COMMENT '删除标志：0未删除，1:删除',
                                  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
                                  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
                                  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                                  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
                                  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                                  PRIMARY KEY (`id`),
                                  UNIQUE KEY `uk_group_code` (`group_code`),
                                  KEY `idx_status_del` (`status`, `del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='业务字典分组表';

drop table if exists biz_dict_item;
CREATE TABLE `biz_dict_item` (
                                 `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                 `group_id` bigint unsigned NOT NULL COMMENT '字典组ID',
                                 `item_code` varchar(100) NOT NULL COMMENT '字典编码，可用于程序识别',
                                 `item_name` varchar(100) NOT NULL COMMENT '字典项名称，例如：待支付、已支付',
                                 `item_value` varchar(500) NOT NULL COMMENT '字典值，业务表实际存储值',
                                 `extra_json` varchar(2000) DEFAULT NULL COMMENT '扩展配置JSON',
                                 `item_sort` int NOT NULL DEFAULT 0 COMMENT '排序，值越小越靠前',
                                 `status` char(1) NOT NULL DEFAULT '1' COMMENT '状态：1:正常，0:停用',
                                 `del_flag` char(1) NOT NULL DEFAULT '0' COMMENT '删除标志：0未删除，1:删除',
                                 `remark` varchar(500) DEFAULT NULL COMMENT '备注',
                                 `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
                                 `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                                 `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
                                 `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                                 PRIMARY KEY (`id`),
                                 UNIQUE KEY `uk_group_item_value` (`group_id`, `item_value`),
                                 KEY `idx_group_status_sort` (`group_id`, `status`, `del_flag`, `item_sort`),
                                 UNIQUE KEY `uk_group_item_code` (`group_id`, `item_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='业务字典明细表';

drop table if exists biz_file_detail;
CREATE TABLE `biz_file_detail` (
                                   `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '文件id',
                                   `url` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '文件访问地址',
                                   `size` bigint DEFAULT NULL COMMENT '文件大小，单位字节',
                                   `filename` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '文件名称',
                                   `original_filename` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '原始文件名',
                                   `base_path` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '基础存储路径',
                                   `path` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '存储路径',
                                   `ext` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '文件扩展名',
                                   `platform` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '存储平台',
                                   `th_url` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '缩略图访问路径',
                                   `th_filename` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '缩略图名称',
                                   `th_size` bigint DEFAULT NULL COMMENT '缩略图大小，单位字节',
                                   `object_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '文件所属对象id',
                                   `object_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '文件所属对象类型，例如用户头像，评价图片',
                                   `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                                   PRIMARY KEY (`id`) USING BTREE,
                                   KEY `idx_file_url` (`url`),
                                   KEY `idx_file_create_time` (`create_time`),
                                   KEY `idx_file_original_filename` (`original_filename`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='文件记录表';
