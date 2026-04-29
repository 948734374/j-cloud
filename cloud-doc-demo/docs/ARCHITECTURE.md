# 云文档协同编辑 Demo 架构设计

## 阶段范围

本项目实现第一阶段能力：

- 登录演示用户
- 文档列表
- 创建文档
- 打开单篇文档
- 多用户进入同一文档共同编辑
- 在线用户展示
- 编辑内容自动保存到本地 JSON 文件

## 本地架构

```text
Browser
  | HTTP API
  v
Node HTTP Server
  | read/write
  v
data/documents.json

Browser
  | WebSocket
  v
Document Room
  | broadcast
  v
Other Browsers
```

## 模块说明

- `server.js`：HTTP API、静态资源服务、WebSocket 握手、房间广播、本地持久化。
- `public/index.html`：应用页面骨架。
- `public/app.js`：登录、文档列表、编辑器、WebSocket 客户端逻辑。
- `public/styles.css`：应用样式。
- `data/documents.json`：本地文档数据，首次启动时自动生成。

## 第一阶段协同策略

第一阶段为了保持 Demo 可直接运行，采用“整篇 HTML 内容同步”：

1. 用户在 `contenteditable` 编辑器输入。
2. 前端防抖后通过 WebSocket 发送当前文档 HTML。
3. 服务端更新文档内容、递增版本号、写入 `data/documents.json`。
4. 服务端向同一文档房间内其他用户广播最新内容。
5. 其他浏览器收到更新后刷新编辑器内容。

该方案适合演示多人实时同步、房间广播、在线用户和自动保存，不适合作为生产级冲突合并方案。

## 第二阶段升级方向

生产级协同编辑建议升级为：

- Yjs 或 Automerge CRDT
- ProseMirror / Tiptap 富文本编辑器
- Redis Pub/Sub 支持多服务实例广播
- MySQL / PostgreSQL 保存文档元数据
- 对象存储保存附件
- 文档权限、分享链接、历史版本恢复
