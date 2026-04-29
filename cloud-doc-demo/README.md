# 云文档协同编辑 Demo

这是一个独立的本地云文档协同编辑 Demo，用于验证第一阶段能力：登录、文档列表、单文档多人协同编辑、自动保存、在线用户展示。

## 运行环境

- Node.js 18+
- 不需要安装第三方依赖

## 启动

```bash
npm start
```

默认地址：

```text
http://localhost:5178
```

如果 `5178` 被占用，服务会自动尝试下一个端口。

## 演示方式

1. 打开 `http://localhost:5178`。
2. 使用 `Alice` 登录。
3. 再打开一个新的浏览器窗口或无痕窗口。
4. 使用 `Bob` 登录。
5. 两个用户打开同一篇文档。
6. 任意一端编辑内容，另一端会实时收到更新。

## 已实现功能

- 演示用户登录
- 文档列表
- 新建文档
- 打开文档
- 编辑文档标题
- 编辑文档正文
- WebSocket 房间广播
- 在线协作者展示
- 远端用户彩色光标，支持换行和空行位置
- 远端光标用户名标签，最多显示 3 个字符
- 多用户同位置编辑时，标签堆叠显示并用小三角连接光标
- 自动保存到 `data/documents.json`

## 项目结构

```text
cloud-doc-demo
├── server.js
├── package.json
├── public
│   ├── index.html
│   ├── app.js
│   └── styles.css
├── data
│   └── documents.json
└── docs
    ├── ARCHITECTURE.md
    └── CORE_TECH.md
```

## 技术说明

第一阶段采用轻量方案：

- Node.js 原生 HTTP 服务
- 原生 WebSocket 协议实现
- 浏览器 `contenteditable`
- 本地 JSON 文件持久化

生产级方案建议升级为：

- Yjs CRDT
- Tiptap / ProseMirror
- Redis Pub/Sub
- MySQL / PostgreSQL
- JWT 签名鉴权
- 文档权限和历史版本

更多说明见：

- [架构设计](docs/ARCHITECTURE.md)
- [核心技术与难点技术](docs/CORE_TECH.md)
