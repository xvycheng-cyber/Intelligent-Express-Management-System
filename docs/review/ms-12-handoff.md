# MS-12 交接清单

> **执行状态：MS-12 已完成**；四图/TOC/Word/PDF 已交付，见 ms-12-delivery-report.md 与 ms-12-independent-check-report.md。

| 项目 | 内容 |
| --- | --- |
| 交接日期 | 2026-11-15 |
| 当前 HEAD | `ccb52c269569466bdfcd91d4cee5a67f7678251a` |
| MS-11 状态 | **COMPLETED（审计收尾完成）** |
| 项目整体 | **NOT READY**（D08/AC-30） |
| MS-12 状态 | 未开始 |

---

## 1. 当前状态

| 项 | 状态 |
| --- | --- |
| MS-10 | COMPLETED（阶段关闭） |
| MS-11 | COMPLETED（审计收尾） |
| MS-09 | **COMPLETED**（四图/目录/Word/PDF；封面 USER DEFERRED） |
| 论文 Markdown | EXISTS，第1章 1.1～1.4、第2章 2.1～2.21 完整 |
| Word | EXISTS，约 67 页 |
| PDF | EXISTS，约 67 页；TOC 为可更新占位 |
| 2.1～2.20 截图 | EXISTS |
| 2.21 录入/解析/保存确认/测试记录 | EXISTS |
| 2.21 地址列表/编辑/删除/批删 | **MISSING/BLOCKED** |
| D08 | P1 OPEN / USER DEFERRED |
| AC-30 | PARTIAL |
| GitHub Actions CI | NOT RUN / USER DEFERRED（P2 工程化增强，不是硬性验收条件） |
| 项目正式验收 | **NOT READY** |

---

## 2. 缺失的四张业务截图

需要真实运行系统后补：

1. `docs/screenshots/2-21-list.png` — 地址列表；
2. `docs/screenshots/2-21-edit-dialog.png` — 编辑弹窗；
3. `docs/screenshots/2-21-delete-confirm.png` — 单条删除确认；
4. `docs/screenshots/2-21-batch-delete.png` — 当前页批量删除确认。

要求：

- 真实 Chrome/Edge 运行截图；
- 虚构数据，`MS10-DEMO`/`MSDEMO` 等标记；
- 不暴露真实联系人、电话、地址、数据库口令；
- 不用设计稿、静态 HTML 或旧图冒充；
- 截图内容与当前 `AddressListView`、`AddressFormDialog`、删除确认逻辑一致；
- 截图后清理本轮数据，仅删除主键明确的测试记录。

---

## 3. 用户授权范围

用户已明确授权 **MS-12 阶段直接修改论文正文、Word、PDF**，并补充真实业务截图及相关图号、图题、正文引用和目录。

该授权：

- **包括**论文 Markdown、Word、PDF、截图、图号/图题/正文引用/目录；
- **不包括**封面、个人信息、教学系、班号、成员信息（仍忽略）；
- **不包括**D08 配置整改；
- **不包括**GitHub 上传/推送/远程 CI；
- **不包括**业务代码、测试代码、数据库结构变更。

**MS-12 不再向用户重复确认论文修改权限。**

---

## 4. MS-12 推荐执行顺序

1. 启动 MySQL（已运行则直接使用）和后端/前端；
2. 核对数据库 `express_address_book`、表 `express_address`；
3. 创建/复用带 `MSDEMO` 标记的虚构记录；
4. 截取地址列表、编辑弹窗、删除确认、批量删除确认；
5. 核对截图内容与页面/代码一致；
6. 将四图插入 `docs/paper/快递地址簿录入程序设计.md`；
7. 统一图号、图题、正文引用、图表清单；
8. 更新 Word 目录/页码；
9. 重新导出 PDF；
10. 检查 PDF 首页、目录、第2章中段、2.18/2.20/2.21 图、参考文献、附录；
11. 清理本轮演示数据；
12. 更新 MS-09 报告/MD/Word/PDF/证据索引；
13. 生成 MS-12 交付报告。

---

## 5. 完成标准

- 四张截图真实存在且与当前页面一致；
- 图号、图题、正文引用、图目录一致；
- Word/PDF 可打开，目录有条目/页码；
- PDF 图片清晰、表格不截断、页码连续；
- 论文技术内容与当前实现/测试一致；
- 不伪造功能、测试、截图；
- 演示数据按主键清理或保留并记录；
- 不修改 D08/封面/GitHub/CI；
- MS-09 可从 PARTIAL 更新为 COMPLETE 仅当上述全部完成。

---

## 6. MS-12 不得触碰

- MySQL 配置/插件/账号/防火墙；
- D08 风险处置；
- 业务代码/测试代码；
- 数据库结构；
- 论文封面和个人信息；
- GitHub 上传/推送/远程 Actions；
- 未经授权的新功能。

---

## 7. 交接结论

MS-11 审计收尾完成，MS-12 可直接在授权范围内修改论文与截图。  
项目整体仍 **NOT READY**，MS-12 完成论文证据链后才可能进入正式验收评审。
