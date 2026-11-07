# LogiTech（逻辑工艺）

> 新增 300 余件物品、机器、多方块结构与空间存储系统，并扩展原版合成与货运物流机制，内容量庞大。

LogiTech 是一款 Slimefun4 附属插件（作者 matl114），以「高生产力 + 模组风格多方块」为核心：堆叠机器、无上限终极合成、直接对接量子存储的大规模吞吐、懒狗工作台一键放置，让服务器在推进度的同时获得高产能。

## 前置

| 依赖 | 必需性 |
| --- | --- |
| [Slimefun4](https://github.com/SlimefunGuguProject/Slimefun4) | 必需（`depend`） |
| [GuizhanLibPlugin](https://50l.cc/gzlib) | 必需（`depend`），缺失时插件拒绝启动 |
| [InfinityExpansion](https://github.com/SlimefunGuguProject/InfinityExpansion) | 软依赖，安装后启用相关特性 |
| [Networks](https://github.com/SlimefunGuguProject/Networks) | 软依赖，安装后启用相关特性 |
| WildStacker / RoseStacker | 软依赖，刷怪笼堆叠兼容 |

- 服务端要求：**Paper**（含分支）+ MC 1.20+（`api-version: 1.20`）；不支持 Spigot。
- Java：21+。

## 安装

1. 安装 Paper 服务端与前置（Slimefun4、GuizhanLibPlugin）。
2. 将 `LogiTech-<版本>.jar` 放入 `plugins/` 目录。
3. 重启服务器。`config.yml` 中 `options.auto-update: true` 时，Build 版会通过 GuizhanUpdater 自动更新。

## 命令

主命令 `/logitech`（别名 `/lc`），子命令面向管理员调试与实验性内容：

| 权限 | 说明 | 默认 |
| --- | --- | --- |
| `logitech.command.op` | 管理员命令 | OP |
| `logitech.shell.root` | 免密进入 LogiTech shell | OP |
| `logitech.shell.test` | 测试员工具 | OP |
| `logitech.experimental.test` | 实验性物品 | OP |

游戏内指南可在 Slimefun 指南的「逻辑工艺」分类中查看（含更新日志与 wiki 链接）。

## 配置文件

| 文件 | 用途 |
| --- | --- |
| `config.yml` | 全局选项（自动更新等） |
| `item-settings.yml` / `machines.yml` / `addon-machines.yml` | 物品与机器参数、机器堆叠列表导入 |
| `space-storage.yml` | 空间存储世界配置 |
| `language.yml` | 文本与物品显示名 |

## 构建

```bash
./gradlew clean build
```

产物在 `build/libs/LogiTech-<版本>.jar`。需要 JDK 21（无本地 JDK 时 Gradle 工具链会自动下载）。

## 兼容性

CI 会定期在 Paper 1.20.1 与最新版本（26.2 线）上做启动兼容性测试（见 `.github/workflows/test-compatibility.yml`）。

## 反馈

- Issue：https://github.com/balugaq/LogiTech/issues
- 上游仓库：https://github.com/linxinzia/LogiTech （原仓库 https://github.com/m1919810/LogiTech ）
- Wiki：https://github.com/m1919810/LogiTech/wiki

## 许可

仓库内未声明许可证，版权保留。
