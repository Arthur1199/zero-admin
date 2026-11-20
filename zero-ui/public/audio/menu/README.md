# 菜单音效文件说明

## 音效文件列表

前端通过 `/audio/menu/` 读取以下音效文件，文件名与[播放配置](../../../src/utils/audio.js)对应。

| 文件名 | 用途 | 建议时长 |
|--------|------|---------|
| `menu-expand.mp3` | 父菜单展开音效 | 200-300ms |
| `menu-collapse.mp3` | 父菜单收起音效 | 200-300ms |
| `menu-click.mp3` | 子菜单点击音效 | 100-200ms |
| `menu-tab-change.mp3` | Tab切换音效 | 100-200ms |
| `menu-tab-close.mp3` | Tab关闭音效 | 100-200ms |

## 音效要求

- 使用表中的 MP3 文件名。更换格式时同时更新播放路径。
- 展开、收起和关闭标签页的音量为 0.3，点击菜单和切换标签页为 0.4。
- 按表中的建议时长选择短音效，确认音效授权允许项目分发。

## 获取音效资源

可以从以下渠道获取合适的音效：

1. **免费音效网站**:
   - [Freesound](https://freesound.org/) - 需注册
   - [Zapsplat](https://www.zapsplat.com/) - 免费下载
   - [Mixkit](https://mixkit.co/free-sound-effects/) - 无需注册

2. **推荐搜索关键词**:
   - "UI click"
   - "button click"
   - "interface beep"
   - "menu open"
   - "tab switch"

## 临时使用

音效随设置中的菜单音效开关生效。资源缺失或解码失败会输出控制台警告，菜单和标签页操作仍可继续。补齐资源后刷新页面并测试播放。

## 自定义音效

如需更换音效，只需替换对应的MP3文件即可，无需修改代码。
