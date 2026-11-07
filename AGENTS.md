# AGENTS.md — LogiTech 操作宪章

> 本文件是 AI 编程助手与本仓库开发者的**操作契约**。所有改动（代码、构建、文档）均须遵守本文约定；当本文与代码冲突时，先核对代码再更新本文。

## TL;DR（太长了不看）

1. 本仓库是 **Minecraft Paper + Slimefun4 附属插件**（Java 21 / Gradle Kotlin DSL），所有改动必须通过 `./gradlew clean build` 验收后才能提交，禁止提交未编译验证的代码。
2. 玩家可见文案走 `language.yml`（`Language` 类加载），日志走 `Debug.logger` 与 `getLogger()`；**禁止** `System.out.println`、**禁止**改动物品/Slimefun 注册 ID、**禁止** `git push -f`。
3. 构建产物 `build/libs/LogiTech-<version>.jar` 为 shaded fat jar；`plugin.yml` 的 `version: ${version}` 由 `processResources` 展开，**不要**改回 Maven 的 `${project.version}`。

## 1. 项目身份与边界

| 维度 | 事实 |
| --- | --- |
| 定位 | Paper 服务器插件，Slimefun4 附属「逻辑工艺」，含堆叠机器、终极合成、多方块结构、货运、空间存储、辐射机制等子系统 |
| 入口类 | `me.matl114.logitech.MyAddon`（`extends JavaPlugin implements SlimefunAddon`） |
| 版本目标 | Java 21 工具链、Paper 1.20.4 API、Slimefun 2025.1、`api-version: 1.20` |
| 仓库策略 | `origin` 为 `balugaq/LogiTech`，`upstream` 为 `linxinzia/LogiTech`；默认分支 `master` |
| 自动更新 | `MyAddon.tryUpdate()` 通过 GuizhanUpdater 拉取 `m1919810/LogiTech@master`，仅 `Build` 前缀版本生效 |

### 1.1 代码包地图（改代码前先定位）

| 包 | 职责 |
| --- | --- |
| `me.matl114.logitech` | 主类 `MyAddon`、`ConfigLoader`、`Language`（语言文件加载） |
| `core.AddGroups / AddItem / AddSlimefunItems` | 物品组、物品模板、Slimefun 物品注册（17 个分类，见 `AddGroups.registerGroups`） |
| `core.Machines.*` | 机器实现（AutoMachines / Electrics / ManualMachines / SpecialMachines / WorkBenchs，约 78 个类） |
| `core.Blocks.*` | 方块类机器与多方块（MultiBlock / MultiBlockCore、Laser、Spawner 等） |
| `core.Cargo.*` | 货运系统：过滤器/分拣机（Bi/Quar/Octa）、配置卡（ChipCard/ConfigCard）、Singularity、空间存储（SpaceStorage） |
| `core.Depends.*` | 软依赖适配：`DependencyNetwork`（Networks）、`DependencyInfinity`（InfinityExpansion）、`SupportedPluginManager` |
| `core.Registries.*` | 注册表：MultiBlockTypes、CustomEffects、RecipeSupporter、EFUImplements（装备强化） |
| `listeners.*` | 监听器（ListenerManager 注册）与保护模块（ProtectionManager、StorageWorldProtection） |
| `manager.*` | Schedules（计划线程）、RadiationRegionManager、EquipmentFUManager、PostSetupTasks |
| `utils.*` | AddUtils / MenuUtils / CraftUtils / TransportUtils 等工具层 |

## 2. 技术栈与项目结构（事实核对）

- **构建**：Gradle Kotlin DSL + Shadow 插件（`minimize()` + relocation：`com.jeff_media.morepersistentdatatypes`→`me.matl114.libs.morepersistentdatatypes`、`me.matl114.matlib`→`me.matl114.libs.matlib`）；版本号唯一来源是 `gradle.properties` 的 `version=`。
- **依赖仓库**：Maven Central、Paper、JitPack（Slimefun4 / InfinityExpansion / Networks / matlib）、bg-software（WildStackerAPI）、rosewooddev（rosestacker）。GuizhanLibPlugin 在 Maven Central（`net.guizhanss`）。
- **Lombok**：JDK 23+ 要求显式 `annotationProcessorPaths`（Maven 时代）或 `annotationProcessor(libs.lombok)`（Gradle）；本机 JDK 26 需 Lombok ≥1.18.46。
- **配置资源**：`plugin.yml`、`config.yml`、`item-settings.yml`、`machines.yml`、`addon-machines.yml`、`space-storage.yml`、`language.yml`（约 330 个条目，物品名与描述的主要来源）。
- **日志**：`Debug.logger(...)` 为主要输出；启动日志按阶段打印（物品组加载/软依赖检测/监听器注册等）。
- **启动顺序**（`MyAddon.onEnable()`，新增初始化须插入对应阶段）：Paper 检测 → `AddonInitialization`（matlib，displayName「逻辑工艺」）→ `checkVersion()`（缺 GuizhanLibPlugin 直接抛错禁用）→ `tryUpdate()` → `ConfigLoader.load` → `Language.loadConfig` → `DependencyNetwork/Infinity.init` → `SupportedPluginManager` → `AddGroups.registerGroups` → `AddItem.registerItemStack` → `AddSlimefunItems.registerSlimefunItems` → `StorageSpace.setup` → `AddDepends.setup` → `Schedules.setupSchedules` → `ListenerManager` / `ProtectionManager` → `Storages` / `MultiBlockService` / `MultiBlockTypes` / `CustomEffects` / `RadiationRegionManager` / `EquipmentFUManager` → 各 Utils.setup → `LogitechMain().registerCommand`。
- **CI**：`build.yml`（JDK 21，push/PR 到 master）、`release.yml`（版本号读 `gradle.properties`，打 `v<version>` tag 自动发 Release）、`test-compatibility.yml`（Paper 1.20.1 / 26.2 启动测试，装 Slimefun4 + GuizhanLibPlugin）。

## 3. 决策树：当遇到 A 时，执行 B

### 3.1 新增/修改物品或机器

- 新增物品模板 → `core.AddItem`；新增 Slimefun 物品注册 → `core.AddSlimefunItems`；新分类 → `core.AddGroups`（`registerGroups` + `ROOT` 布局）。
- 文案（名称/描述）→ 写入 `language.yml` 对应条目，通过 `Language` 机制读取；不要在代码里硬编码显示名（物品组标题等历史遗留硬编码除外，保持原样风格）。
- 机器注册完成后需要延后初始化的，挂 `PostSetupTasks.addPostRegisterTask(...)`，不要在 register 阶段直接依赖其他注册表。

### 3.2 软依赖适配

- 涉及 Networks / InfinityExpansion 的功能放 `core.Depends`，初始化必须 try-catch Throwable（版本不匹配时仅禁用相关内容，不能炸主流程）——现有代码即此模式。
- 堆叠插件兼容走 `SupportedPluginManager`，不要直接引用 WildStacker/RoseStacker API 而绕过它。

### 3.3 构建与依赖

- 升级第三方库前，先确认其上游坐标可解析（JitPack 依赖看 `build.log` 区分「未构建」与「构建失败」）。
- `gradle.properties` 只放可移植属性；**禁止**写入本机 `org.gradle.java.home` 或工具链绝对路径。
- 本机无 JDK 21 时构建用：`./gradlew clean build -Dorg.gradle.java.installations.paths="C:/Program Files/Java/jbr-21.0.9"`（不落盘）。

## 4. 验收

- 代码改动：`./gradlew clean build` 成功 + 无新增编译警告块即过。
- CI 配置改动：YAML 语法解析通过；三个 workflow 均 `chmod +x gradlew` 且用 `./gradlew`；`git ls-files -s gradlew` 为 `100755`。
- 文档改动：与代码事实一致（包名、类名、启动顺序、配置键）。

## 5. 红线

1. 禁止改 `plugin.yml` 的 `name`、`main`、`depend` 顺序（GuizhanLibPlugin 缺失会直接禁用插件，这是有意为之）。
2. 禁止移除 shade relocation——matlib 与 MorePersistentDataTypes 与宿主类路径冲突。
3. 禁止把 `version: ${version}` 改成字面量或恢复 `${project.version}`。
4. 禁止在 onEnable 启动序列中途插入重 IO 操作（读世界/反序列化大文件）——先看 `Schedules` 的异步设施。
5. 禁止提交未构建验证的代码。

## 6. 已知债务

- `AddGroups` 中存在大量硬编码中文文案与彩蛋（含对个别玩家的不当措辞），历史遗留，未走语言文件。
- 原 Maven 构建的 git-commit-id jar 命名（`LogiTech-<commit>`）未迁移到 Gradle，产物现为 `LogiTech-<version>.jar`。
- `Unittest.java` 存在于主源码集，测试覆盖率低；`build.yml` 仍跑 `gradlew test`。
