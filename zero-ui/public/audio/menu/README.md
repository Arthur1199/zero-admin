# 菜单音效文件说明

## 音效文件列表

本目录应包含以下音效文件：

| 文件名 | 用途 | 建议时长 |
|--------|------|---------|
| `menu-expand.mp3` | 父菜单展开音效 | 200-300ms |
| `menu-collapse.mp3` | 父菜单收起音效 | 200-300ms |
| `menu-click.mp3` | 子菜单点击音效 | 100-200ms |
| `menu-tab-change.mp3` | Tab切换音效 | 100-200ms |
| `menu-tab-close.mp3` | Tab关闭音效 | 100-200ms |

## 音效要求

- **格式**: MP3（推荐）或 WAV
- **音量**: 文件音量应适中，代码中已设置音量控制（0.3-0.4）
- **时长**: 简短清脆，避免过长影响用户体验
- **风格**: 建议使用轻快、现代的UI交互音效

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

如果暂时没有音效文件，系统会优雅降级，不会报错。添加音效文件后刷新页面即可生效。

## 自定义音效

如需更换音效，只需替换对应的MP3文件即可，无需修改代码。
