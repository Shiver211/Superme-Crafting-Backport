# Superme Crafting Backport

添加了 81x81 的至尊工作台、自定义至尊合成配方、CraftTweaker 脚本支持、JEI 联动、可选 AE2 自动合成支持，以及巨型至尊熔炉多方块结构。

由于作者第一次写ae相关代码，难免有bug，望大家理解并向我提issue。

## 主要功能

- 81x81 至尊工作台，支持超大型有序和无序配方，支持滚轮缩放和位置调节
- JEI 配方显示与配方转移支持
- CraftTweaker 支持，可用脚本添加至尊合成配方
- 至尊扳手可将工作台内的配方导出为 CraftTweaker 脚本
- 可选 AE2 联动：
  - 至尊样板终端
  - 至尊接口
  - 至尊分子装配室
  - 空白至尊样板和编码至尊样板
- 32、64、128 三种尺寸的至尊熔炉多方块结构
- 至尊熔炉输入总线、输出总线和燃料总线
- 至尊熔炉终端、至尊熔炉炸弹和辅助工具、

## CRT 支持

开发整合包时，可以使用至尊扳手从至尊工作台中直接导出 CraftTweaker 配方脚本。

步骤为：

  1. 在至尊工作台里放好你想要的配方
  2. 副手放产物
  3. 创造模式下使用至尊扳手右键工作台
  4. 在 `scripts\supreme_crafting_generated.zs` 找到保存好的配方添加脚本

## 可选联动

- JEI
- CraftTweaker
- Applied Energistics 2




# Superme Crafting Backport

Adds an 81x81 Supreme Table, custom Supreme Crafting recipes, CraftTweaker scripting support, JEI integration, optional AE2 autocrafting support, and a massive Supreme Furnace multiblock.

## Features

- 81x81 Supreme Table for huge shaped and shapeless recipes, with mouse wheel zooming and panning
- JEI recipe display and recipe transfer support
- CraftTweaker support for adding Supreme Crafting recipes through scripts
- Supreme Wrench can export the current table recipe as a CraftTweaker script
- Optional AE2 integration:
  - Supreme Pattern Terminal
  - Supreme Interface
  - Supreme Assembler
  - Blank Supreme Pattern and Encoded Supreme Pattern
- Supreme Furnace multiblocks in 32, 64, and 128 cube sizes
- Input, output, and fuel hatches for the Supreme Furnace
- Supreme Furnace Terminal, Supreme Furnace Bombs, and helper tools

## CraftTweaker Support

When developing a modpack, you can use the Supreme Wrench to export a CraftTweaker recipe script directly from the Supreme Table.

Steps:

  1. Place the recipe you want in the Supreme Table
  2. Put the output item in your offhand
  3. In creative mode, right-click the table with the Supreme Wrench
  4. Find the saved recipe script in `scripts\supreme_crafting_generated.zs`

## Optional Integrations

- JEI
- CraftTweaker
- Applied Energistics 2
