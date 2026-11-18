# CBCAddition

> **CBCAddition** —— Create: The Brass Concerto（黄铜协奏曲）的私货附属模组。
> 目标：**简化大部分材料的制作流程**，并保持与整合包现有模组生态兼容。

| 项目 | 值 |
|---|---|
| 模组 ID | `cbcaddition`（必须小写） |
| 显示名 | `CBCAddition` |
| 版本 | `0.4.0` |
| Minecraft | `1.20.1` |
| Forge | `47.2.20`（依赖范围 `[47,)`） |
| Java | `17` |
| 许可证 | `GPL-3.0-only` |
| 作者 | neolxsky |
| 仓库 | https://github.com/God-Forge/CBCAddition |

---

## 环境

| 项目 | 版本 / 说明 |
|---|---|
| Minecraft | 1.20.1（`minecraft_version_range = [1.20.1,1.21)`） |
| Forge | 47.2.20（`forge_version_range = [47,)`，`loaderVersion = [47,)`） |
| 映射 | `official` |
| Java | 17（编译与运行，`java toolchain` 固定 17） |
| Gradle | 8.8（`gradlew` wrapper，发行包走腾讯云镜像） |
| ForgeGradle | 6.0.24 |
| Create | 0.5.1.j（**可选**依赖，`[0.5.1,)`，`ordering = AFTER`；缺失时机器静默不产出） |
| JEI | 15.20.0.129（**编译期**依赖，仅用于插件编译；运行时由整合包提供，不会打进 jar） |

`mods.toml` 中声明：`forge` / `minecraft` 为强制依赖，`create` 为可选依赖（便于单独测试）。

## 功能

两台「合金炉」机器，均为熔炉外观 + 六面 IO 配置 + GUI。

### 安山合金炉（Andesite Alloy Forge）

| 项 | 内容 |
|---|---|
| 注册 ID | `cbcaddition:andesite_forge` |
| 配方 | `1 铁锭 + 9 安山岩 → 10 安山合金`（`create:andesite_alloy`） |
| 槽位 | `0` 铁锭 / `1` 安山岩 / `2` 产物（输出槽不可手动放入） |
| 节流 | 每 20 tick（1 秒）完成一次合成 |
| 常量 | `ANDESITE_COST = 9`、`OUTPUT_AMOUNT = 10`、`COOLDOWN_TICKS = 20` |

### 黄铜合金炉（Brass Alloy Forge）

| 项 | 内容 |
|---|---|
| 注册 ID | `cbcaddition:brass_forge` |
| 配方 | `1 铜锭 + 1 金锭 → 2 黄铜锭`（`create:brass_ingot`） |
| 槽位 | `0` 铜锭 / `1` 金锭 / `2` 产物（输出槽不可手动放入） |
| 节流 | 每 20 tick（1 秒）完成一次合成 |
| 常量 | `COPPER_COST = 1`、`GOLD_COST = 1`、`OUTPUT_AMOUNT = 2`、`COOLDOWN_TICKS = 20` |

外观：正面 / 侧面引用原版熔炉纹理（`minecraft:block/furnace_front` / `furnace_side`）；
安山炉上下两面使用内置的 Create 安山岩贴图，黄铜炉除正面外五面使用内置的 Create 黄铜机壳贴图。
两张贴图已从 Create 0.5.1.j 复制进 `src/main/resources/assets/cbcaddition/textures/block/`
（16×16 PNG），因此未安装 Create 时也能正确显示。两台机器都有 `facing` 水平朝向属性，放置时朝向玩家。

### 机械核心装配机（Mechanical Core Assembler）

| 项 | 内容 |
|---|---|
| 注册 ID | `cbcaddition:mechanical_core_assembler` |
| 配方 A | `1 安山机壳 + 1 大齿轮 → 3 机械核心`（`create:andesite_casing` + `create:large_cogwheel`） |
| 配方 B | `1 工业镀铁块 + 1 大型工业齿轮 → 10 机械核心`（`design_decor:industrial_plating_block` + `design_decor:industrial_gear_large`） |
| 槽位 | `0` = A / `1` = B / `2` = 产物（两种配方均为无序组合，槽位顺序不限） |
| 节流 | 每 20 tick（1 秒）完成一次合成 |

产物「机械核心」由整合包的 KubeJS 物品提供（`kubejs:mechanical_core`）；
任一输入模组缺失时对应配方自动失效，不报错。外观：正面为原版熔炉纹理，其余面使用内置的
Create 安山机壳贴图（`andesite_casing.png`，同样从 Create 0.5.1.j 复制）。

### 合成判定

- 输入槽数量不足 → 直接返回（不消耗、不产出）
- 目标物品不存在（未装 Create，`BuiltInRegistries` 查不到）→ 静默跳过，不报错
- 产物槽已有其他物品，或剩余空间放不下本次产量（`getMaxStackSize() - OUTPUT_AMOUNT`）→ 暂停合成
- 达成条件时扣除原料、累加产物，并进入 `COOLDOWN_TICKS` 冷却

### 交互

- **右键**：打开 GUI
- **潜行 + 右键**：直接取出产物
- **手持对应原料 + 右键**：直接投料（安山炉：铁锭 / 安山岩；黄铜炉：铜锭 / 金锭）
- 以上快捷交互通过 `IOConfigurable` 之外的 `interact()` 实现，`Block#use` 会先尝试它，
  未消费才打开 GUI

### GUI

- 右键方块打开（`MenuProvider` + `NetworkHooks.openScreen`，同步方块坐标）
- 布局：`A B → C` —— 两个输入槽横向并排，原版熔炉箭头指向产物槽
- 尺寸 `176 × 166`；背景为自绘面板，槽位框与箭头复用原版熔炉贴图（箭头材质不变）
- 机器槽坐标：输入 A `(32, 36)`、输入 B `(56, 36)`、产物 C `(112, 36)`
- 玩家背包 3×9 位于 `y = 84`，快捷栏位于 `y = 142`
- 坐标距离限制：`distanceToSqr <= 64`（4 格）
- **`IO` 按钮**：右上角 `(152, 5)`，展开 / 收起六面配置面板
- **`弹` 按钮**：`(152, 25)`，切换自动弹出（橙 = 开、灰 = 关）；开启时每 tick
  从所有可输出面中**随机挑一个**把产物弹射进相邻容器

### 六面 IO 配置

每个面有 **4 种**模式，点击按钮循环切换：

`DISABLED` → `INPUT` → `OUTPUT` → `BOTH` → …

- **默认**：六面全部 `INPUT`（方便漏斗直连）
- **操作**：点击 GUI 右上角 `IO` 按钮（16×16，相对 GUI 坐标 `152, 5`）展开/收起面板；
  点击展开的格子把该面切到下一个模式
- **布局**：面板按「相对方块朝向」摆放——上 / 左 / 中（正面）/ 右 / 下 / 后
- **颜色**：灰 = 禁用、绿 = 输入、橙 = 输出、蓝 = 双向

各模式暴露给外部的 `IItemHandler` 行为：

| 模式 | 槽位数 | 可插入 | 可提取 |
|---|---|---|---|
| `DISABLED` | 不暴露能力 | — | — |
| `INPUT` | 2（`0`→第一种原料，`1`→第二种原料） | 仅对应原料 | 否 |
| `OUTPUT` | 1（产物槽） | 否 | 是 |
| `BOTH` | 3（原料 + 原料 + 产物） | 仅原料槽 | 仅产物槽 |

- 自动弹出由 GUI 的 **`弹` 按钮**控制：开启后在 `OUTPUT` / `BOTH` 面中**随机挑一个**，
  用 `ItemHandlerHelper.insertItem` 把产物推入相邻方块实体的容器，推入多少就从本体扣多少；
  没有可输出面时什么都不做
- 持久化：槽位内容、冷却值、六面配置、自动弹出开关全部写入方块实体 NBT
  （`Inventory` / `Cooldown` / `Face0`…`Face5` / `AutoEject`）

### 创造模式标签页

`ModCreativeTabs` 通过 `BuildCreativeModeTabContentsEvent` 把机器加入：

- `FUNCTIONAL_BLOCKS`（功能方块）
- `SEARCH`（保证创造模式搜索能查到）

### JEI 配方查看

安装 JEI 后，三台机器的配方会在 JEI 中以独立分类显示：

- 安山合金炉：铁锭 ×1 + 安山岩 ×9 → 安山合金 ×10
- 黄铜合金炉：铜锭 ×1 + 金锭 ×1 → 黄铜锭 ×2
- 机械核心装配机：安山机壳 ×1 + 大齿轮 ×1 → 机械核心 ×3；
  工业镀铁块 ×1 + 大型工业齿轮 ×1 → 机械核心 ×10
- 三个方块都注册为**催化剂**：在 JEI 搜索框里对着方块按 `R`（用途）即可打开对应配方
- JEI 插件读取方块实体的常量与相关物品 ID；未装对应模组时不显示该配方，
  未装 JEI 时插件类不会被加载（编译期依赖，运行时不打包）

### 无限元件配置器（Infinity Cell Configurator）

| 项 | 内容 |
|---|---|
| 物品 ID | `cbcaddition:infinity_cell_configurator` |
| 使用 | 手持右键打开 GUI（主手 / 副手均可） |
| 样品槽 | 任意物品，或装有流体的容器（桶等）；只作参照、**不消耗** |
| 消耗 | 1 个 `ae2:cell_component_16k`（16k 存储组件，从玩家背包扣除） |
| 产物 | 对应物品 / 流体的 `expatternprovider:infinity_cell`（ExtendedAE 无限元件） |
| 依赖 | AE2 + ExtendedAE（通过反射对接；未安装时提示不可用，物品本身仍可获得） |

产物放入输出槽，关闭 GUI 时样品与产物会退回玩家背包。

### 简化配方

| 配方 | 内容 | 说明 |
|---|---|---|
| `iron_nugget_from_ingot` | 1 铁锭 → 9 铁粒 | 与原版一致，作为模板 |
| `iron_block_from_4_ingots` | 4 铁锭 → 1 铁块 | **简化版**（原版需 9 个） |

两台机器破坏后掉落本体（已有掉落表）。

## 目录结构

```
CBCAddition/
├── build.gradle                     # ForgeGradle 6 构建脚本（UTF-8 源码编码）
├── settings.gradle                  # 插件仓库（BMCLAPI 镜像优先）
├── gradle.properties                # 版本 / mod 元数据（版本 0.4.0）
├── LICENSE                          # GNU GPL v3.0 全文
├── build/libs/cbcaddition-0.4.0.jar # 构建产物
├── src/main/java/com/neolxsky/CBCAddition/
│   ├── CBCAddition.java             # 主类：注册表 + 网络 + 客户端初始化
│   ├── block/
│   │   ├── IOConfigurable.java            # 六面 IO 公共接口（FaceMode）
│   │   ├── AndesiteForgeBlock.java        # 安山合金炉方块（facing / 右键开 GUI / 快捷交互）
│   │   ├── AndesiteForgeBlockEntity.java  # 槽位 / 合成 / 六面 IO / NBT
│   │   ├── BrassForgeBlock.java           # 黄铜合金炉方块
│   │   ├── BrassForgeBlockEntity.java
│   │   ├── MechanicalCoreAssemblerBlock.java        # 机械核心装配机方块
│   │   └── MechanicalCoreAssemblerBlockEntity.java  # 两套无序配方 / 六面 IO / NBT
│   ├── item/
│   │   └── InfinityCellConfiguratorItem.java      # 无限元件配置器（手持右键开 GUI）
│   ├── client/
│   │   ├── ClientSetup.java               # 注册 GUI 屏幕（仅客户端）
│   │   ├── AndesiteForgeScreen.java       # 熔炉背景 + IO 方向面板
│   │   ├── BrassForgeScreen.java
│   │   ├── MechanicalCoreAssemblerScreen.java
│   │   └── InfinityCellConfiguratorScreen.java
│   ├── datagen/                           # 数据生成（runData）
│   │   ├── DataGenerators.java            # GatherDataEvent 入口
│   │   ├── ModRecipeProvider.java         # 简化配方生成
│   │   ├── ModBlockStateProvider.java     # blockstate + 方块模型生成
│   │   ├── ModItemModelProvider.java      # 物品模型生成
│   │   ├── ModLanguageProvider.java       # en_us / zh_cn 语言生成
│   │   └── ModLootTableProvider.java      # 方块掉落表生成
│   ├── compat/jei/                        # JEI 集成
│   │   ├── CbcJeiPlugin.java              # 插件入口：RecipeType / 配方 / 催化剂
│   │   ├── ForgeRecipe.java               # JEI 展示用配方数据
│   │   └── ForgeRecipeCategory.java       # 熔炉槽位背景的配方分类
│   ├── compat/extendedae/                 # ExtendedAE 无限元件对接（反射）
│   │   └── EaeInfinityCellHelper.java     # 样品 → 无限元件
│   ├── menu/
│   │   ├── AndesiteForgeMenu.java         # 容器菜单（服务端 + 网络构造）
│   │   ├── BrassForgeMenu.java
│   │   ├── MechanicalCoreAssemblerMenu.java
│   │   └── InfinityCellConfiguratorMenu.java
│   ├── network/
│   │   └── ModNetwork.java                # SimpleChannel：面模式 / 自动弹出 / 配置器制造
│   └── registry/
│       ├── ModItems.java                  # 物品注册表（无限元件配置器）
│       ├── ModBlocks.java                 # 方块 + BlockItem
│       ├── ModBlockEntities.java          # 方块实体类型
│       ├── ModMenus.java                  # MenuType
│       └── ModCreativeTabs.java           # 创造标签页内容注入
├── src/main/resources/
│   ├── META-INF/mods.toml           # mod 元数据与依赖声明
│   ├── pack.mcmeta                  # 资源包声明（pack_format 15）
│   └── assets/cbcaddition/textures/block/   # 内置贴图（2 张，复制自 Create）
└── src/generated/resources/         # datagen 输出（随源码提交，.cache 已忽略）
    ├── assets/cbcaddition/{blockstates,models,lang}/
    └── data/cbcaddition/{recipes,loot_tables,advancements}/
```

## 构建与运行

```bat
set JAVA_HOME=C:\Users\neolx\.jdks\zulu17.0.21
gradlew build          :: 产物：build/libs/cbcaddition-0.4.0.jar
gradlew runClient      :: 启动开发客户端（工作目录 run/）
gradlew runServer      :: 启动开发服务端（工作目录 run/）
gradlew runData        :: 重新生成资源（输出到 src/generated/resources）
```

- **必须使用 JDK 17**。请把 `JAVA_HOME` 指向一个可正常读写的 JDK 17；
  `gradle.properties` 中不要写 `org.gradle.java.home`（整合包路径含中文会乱码）。
- 优先使用 `C:\Users\neolx\.jdks\zulu17.0.21`；PCL 内置的 `java17` 运行时在部分机器上
  无法创建文件（Gradle 会报 `native-platform.dll` / `.lck` 拒绝访问）。
- 依赖解析顺序：Forge 官方仓库 → Maven Central → BMCLAPI 镜像。
  BMCLAPI 对部分工件返回 403，若放在最前会导致构建失败。
- 首次运行 `runData` / `runClient` 需要下载 Minecraft 资源（联网一次即可）。
- JEI 依赖来自 `maven.blamejared.com`，仅编译期使用并自动下载；不装 JEI 也能正常构建。

## 数据生成（datagen）

配方、方块状态、方块 / 物品模型、语言文件、掉落表全部由 `datagen` 生成，不再手写 JSON：

```bat
gradlew runData
```

- 输出目录：`src/generated/resources/`（已加入 `sourceSets`，会参与打包）
- 生成器实现见 `com.neolxsky.CBCAddition.datagen` 包
- 修改配方 / 名称后重新跑一次 `runData`，不要直接改生成出来的文件
- 贴图（PNG）不由 datagen 管理，仍放在 `src/main/resources/assets/cbcaddition/textures/`

## Git 与版本

- 远程仓库：<https://github.com/God-Forge/CBCAddition>
- 当前版本：`v0.4.0`（`gradle.properties` 的 `mod_version`）
- 提交注释统一使用**简体中文**
- **未经许可不得推送到 GitHub**：本地提交随意，推送前需先获得确认
- 已忽略：`.gradle/`、`build/`、`run/`、IDE 配置等（见 `.gitignore`）

## 修复记录（v0.4.0）

本轮扫描发现的问题已全部处理：

1. **黄铜炉 IO 按钮服务端不生效** —— 网络包处理改为按 `IOConfigurable` 接口识别，
   两种机器共用同一个包，不再只更新安山炉。
2. **`interact()` 是死代码** —— `Block#use` 现在先尝试潜行取产物 / 手持投料，未消费才开 GUI。
3. **GUI 未使用常量** —— 删除 `FACES`、`BUTTON_X`、`BUTTON_Y`。
4. **没有自带贴图** —— 安山岩 / 黄铜机壳贴图复制自 Create 0.5.1.j 并内置（MIT），
   熔炉正 / 侧面引用原版纹理，不再依赖 Create 运行时。
5. **缺少 LICENSE** —— 补齐 GNU GPL v3.0 全文，`mod_license` 改为 `GPL-3.0-only`。
6. **没有 datagen** —— 引入 `GatherDataEvent` 生成器，配方 / 模型 / 语言 / 掉落表全部自动生成。
7. **`Level.getCapability(Capability, BlockPos, Direction)` 编译错误** —— Forge 47.2.20 无此重载，
   改为先取相邻 `BlockEntity` 再查能力。
8. **方块没有掉落表** —— 生存模式破坏机器原本不掉落，现由 datagen 生成 `loot_tables`。
9. **JEI 无法查看机器配方** —— 配方原本硬编码在 `tick()`，JEI 抓不到；新增
   `compat/jei` 插件，以独立分类展示两台机器的配方，并把方块注册为催化剂。
10. **GUI 布局调整** —— 改为 `A B → C` 横向结构，槽位框与箭头沿用原版熔炉贴图。
11. **新增机械核心装配机** —— 支持 `安山机壳 + 大齿轮 → 3 机械核心` 与
    `工业镀铁块 + 大型工业齿轮 → 10 机械核心` 两套无序配方，含 GUI、六面 IO 与 JEI 分类。
12. **自动弹出按钮** —— 新增 `弹` 按钮，开启后每 tick 从可输出面随机选一个弹射产物。

## 扩展指南

### 添加简化配方

在 `ModRecipeProvider#buildRecipes` 中用 `ShapelessRecipeBuilder` / `ShapedRecipeBuilder` 添加，
然后运行 `gradlew runData`。引用其他模组的物品直接写 ID（如 `create:brass_ingot`）；
若该模组非必装依赖，请保证物品存在或放在有条件加载的位置。

### 注册新物品

在 `ModItems` 的 `DeferredRegister` 中注册：

```java
public static final RegistryObject<Item> EXAMPLE =
        ITEMS.register("example", () -> new Item(new Item.Properties()));
```

物品要出现在创造模式物品栏，还需在 `ModCreativeTabs#buildContents` 里 `event.accept(...)`，
并在 `ModItemModelProvider` / `ModLanguageProvider` 中补模型与翻译。

### 新增一台机器

1. 继承 `BaseEntityBlock`（参考 `BrassForgeBlock`），实现 `newBlockEntity` / `getTicker` / `use`
2. 方块实体实现 `IOConfigurable` + `MenuProvider`（参考 `BrassForgeBlockEntity`），
   实现 `getFaceMode` / `setFaceMode`、`serverTick` 合成逻辑与 NBT 读写
3. 在 `ModBlocks`、`ModBlockEntities`、`ModMenus` 中登记（方块名必须与 GUI 的
   `getDisplayName()` 翻译键一致）
4. 在 `ClientSetup` 注册屏幕，`ModCreativeTabs` 加入创造标签页
5. 在 `ModBlockStateProvider` / `ModItemModelProvider` / `ModLanguageProvider` /
   `ModLootTableProvider` 中补生成，然后 `gradlew runData`
6. 需要客户端→服务端配置同步时，在 `ModNetwork` 增加数据包
   （按 `IOConfigurable` 的方式处理，天然支持所有机器）

### 调整合成参数

数值集中在方块实体顶部的常量里（`ANDESITE_COST`、`OUTPUT_AMOUNT`、`COOLDOWN_TICKS` 等），
改常量比复制整段 `tick()` 更稳妥；配方的数量请同步改 `ModRecipeProvider`。

## 许可

本项目基于 **GNU General Public License v3.0** 发布，完整文本见 [LICENSE](LICENSE)。

### 第三方资源

- `assets/cbcaddition/textures/block/andesite_block.png`、`brass_casing.png`：复制自
  [Create](https://github.com/Creators-of-Create/Create)（MIT License，© simibubi 及贡献者）。
- 方块正面 / 侧面纹理直接引用原版 `minecraft:block/furnace_front` / `furnace_side`，
  不随本模组分发。

```
CBCAddition
Copyright (C) 2026 neolxsky

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
```
