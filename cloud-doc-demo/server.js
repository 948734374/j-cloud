const crypto = require("crypto");
const fs = require("fs");
const http = require("http");
const path = require("path");

const ROOT = __dirname;
const PUBLIC_DIR = path.join(ROOT, "public");
const DATA_DIR = path.join(ROOT, "data");
const DATA_FILE = path.join(DATA_DIR, "documents.json");
const DEFAULT_PORT = Number(process.env.PORT || 5178);

const MIME_TYPES = {
  ".html": "text/html; charset=utf-8",
  ".css": "text/css; charset=utf-8",
  ".js": "application/javascript; charset=utf-8",
  ".json": "application/json; charset=utf-8",
  ".svg": "image/svg+xml; charset=utf-8"
};

const demoUsers = new Map([
  ["alice", { id: "u_alice", username: "alice", nickname: "Alice", color: "#2563eb" }],
  ["bob", { id: "u_bob", username: "bob", nickname: "Bob", color: "#16a34a" }],
  ["charlie", { id: "u_charlie", username: "charlie", nickname: "Charlie", color: "#dc2626" }]
]);

const rooms = new Map();
let store = loadStore();

function loadStore() {
  fs.mkdirSync(DATA_DIR, { recursive: true });

  if (!fs.existsSync(DATA_FILE)) {
    const now = new Date().toISOString();
    const initial = {
      documents: [
        {
          id: "doc-product-plan",
          title: "产品需求文档",
          content:
            "<h2>云文档协同编辑 Demo</h2><p>打开两个浏览器窗口，用不同用户登录，然后进入同一篇文档即可看到实时同步。</p><ul><li>支持在线用户显示</li><li>支持内容自动保存</li><li>支持多用户进入同一文档共同编辑</li></ul>",
          ownerId: "u_alice",
          revision: 1,
          createdAt: now,
          updatedAt: now
        }
      ]
    };
    saveStore(initial);
    return initial;
  }

  try {
    return JSON.parse(fs.readFileSync(DATA_FILE, "utf8"));
  } catch (error) {
    console.error("Failed to read data file:", error);
    return { documents: [] };
  }
}

function saveStore(nextStore = store) {
  fs.mkdirSync(DATA_DIR, { recursive: true });
  const tempFile = `${DATA_FILE}.tmp`;
  fs.writeFileSync(tempFile, JSON.stringify(nextStore, null, 2));
  fs.renameSync(tempFile, DATA_FILE);
}

function createToken(user) {
  const payload = {
    id: user.id,
    username: user.username,
    nickname: user.nickname,
    color: user.color,
    issuedAt: Date.now()
  };
  return Buffer.from(JSON.stringify(payload)).toString("base64url");
}

function parseToken(token) {
  if (!token) return null;
  try {
    const payload = JSON.parse(Buffer.from(token, "base64url").toString("utf8"));
    if (!payload.id || !payload.username || !payload.nickname) return null;
    return payload;
  } catch {
    return null;
  }
}

function sendJson(res, statusCode, body) {
  const payload = JSON.stringify(body);
  res.writeHead(statusCode, {
    "Content-Type": "application/json; charset=utf-8",
    "Content-Length": Buffer.byteLength(payload)
  });
  res.end(payload);
}

function readBody(req) {
  return new Promise((resolve, reject) => {
    let raw = "";
    req.on("data", chunk => {
      raw += chunk;
      if (raw.length > 2 * 1024 * 1024) {
        req.destroy();
        reject(new Error("Request body is too large"));
      }
    });
    req.on("end", () => {
      if (!raw) {
        resolve({});
        return;
      }
      try {
        resolve(JSON.parse(raw));
      } catch {
        reject(new Error("Invalid JSON body"));
      }
    });
    req.on("error", reject);
  });
}

function getBearerUser(req) {
  const authorization = req.headers.authorization || "";
  const token = authorization.startsWith("Bearer ") ? authorization.slice(7) : "";
  return parseToken(token);
}

function getDocument(documentId) {
  return store.documents.find(document => document.id === documentId);
}

function listDocuments() {
  return store.documents
    .map(({ id, title, ownerId, revision, createdAt, updatedAt }) => ({
      id,
      title,
      ownerId,
      revision,
      createdAt,
      updatedAt
    }))
    .sort((left, right) => new Date(right.updatedAt) - new Date(left.updatedAt));
}

async function handleApi(req, res, pathname) {
  if (req.method === "POST" && pathname === "/api/auth/login") {
    const body = await readBody(req);
    const username = String(body.username || "").trim().toLowerCase();
    const nickname = String(body.nickname || "").trim();
    const knownUser = demoUsers.get(username);
    const user =
      knownUser ||
      {
        id: `u_${crypto.randomUUID()}`,
        username: username || `guest-${Math.floor(Math.random() * 10000)}`,
        nickname: nickname || username || "Guest",
        color: pickColor(username || nickname || String(Date.now()))
      };

    sendJson(res, 200, {
      token: createToken(user),
      user
    });
    return;
  }

  const user = getBearerUser(req);
  if (!user) {
    sendJson(res, 401, { message: "Unauthorized" });
    return;
  }

  if (req.method === "GET" && pathname === "/api/me") {
    sendJson(res, 200, { user });
    return;
  }

  if (req.method === "GET" && pathname === "/api/documents") {
    sendJson(res, 200, { documents: listDocuments() });
    return;
  }

  if (req.method === "POST" && pathname === "/api/documents") {
    const body = await readBody(req);
    const now = new Date().toISOString();
    const title = String(body.title || "未命名文档").trim() || "未命名文档";
    const document = {
      id: `doc-${crypto.randomUUID()}`,
      title,
      content: "<h2>新文档</h2><p></p>",
      ownerId: user.id,
      revision: 1,
      createdAt: now,
      updatedAt: now
    };
    store.documents.push(document);
    saveStore();
    sendJson(res, 201, { document });
    return;
  }

  const documentMatch = pathname.match(/^\/api\/documents\/([^/]+)$/);
  if (documentMatch && req.method === "GET") {
    const document = getDocument(documentMatch[1]);
    if (!document) {
      sendJson(res, 404, { message: "Document not found" });
      return;
    }
    sendJson(res, 200, { document });
    return;
  }

  const titleMatch = pathname.match(/^\/api\/documents\/([^/]+)\/title$/);
  if (titleMatch && req.method === "PUT") {
    const document = getDocument(titleMatch[1]);
    if (!document) {
      sendJson(res, 404, { message: "Document not found" });
      return;
    }
    const body = await readBody(req);
    const title = String(body.title || "").trim();
    if (!title) {
      sendJson(res, 400, { message: "Title is required" });
      return;
    }
    document.title = title;
    document.updatedAt = new Date().toISOString();
    saveStore();
    broadcastToRoom(document.id, {
      type: "document:title",
      documentId: document.id,
      title: document.title,
      updatedAt: document.updatedAt
    });
    sendJson(res, 200, { document });
    return;
  }

  sendJson(res, 404, { message: "Not found" });
}

function pickColor(input) {
  const colors = ["#2563eb", "#16a34a", "#dc2626", "#9333ea", "#ea580c", "#0891b2"];
  const hash = crypto.createHash("sha1").update(input || "guest").digest();
  return colors[hash[0] % colors.length];
}

function serveStatic(req, res, pathname) {
  const requestedPath = pathname === "/" ? "/index.html" : pathname;
  const safePath = path
    .normalize(decodeURIComponent(requestedPath))
    .replace(/^(\.\.[/\\])+/, "");
  const filePath = path.join(PUBLIC_DIR, safePath);

  if (!filePath.startsWith(PUBLIC_DIR)) {
    res.writeHead(403);
    res.end("Forbidden");
    return;
  }

  fs.readFile(filePath, (error, content) => {
    if (error) {
      res.writeHead(404);
      res.end("Not found");
      return;
    }
    res.writeHead(200, {
      "Content-Type": MIME_TYPES[path.extname(filePath)] || "application/octet-stream",
      "Cache-Control": "no-store"
    });
    res.end(content);
  });
}

function handleHttp(req, res) {
  const parsed = new URL(req.url || "/", "http://localhost");
  const pathname = parsed.pathname || "/";

  if (pathname.startsWith("/api/")) {
    handleApi(req, res, pathname).catch(error => {
      console.error(error);
      sendJson(res, 500, { message: error.message || "Internal server error" });
    });
    return;
  }

  serveStatic(req, res, pathname);
}

function handleUpgrade(req, socket) {
  const parsed = new URL(req.url || "/", "http://localhost");
  const match = (parsed.pathname || "").match(/^\/ws\/documents\/([^/]+)$/);
  const user = parseToken(parsed.searchParams.get("token"));
  const documentId = match ? match[1] : "";
  const document = getDocument(documentId);

  if (!match || !user || !document) {
    socket.write("HTTP/1.1 401 Unauthorized\r\n\r\n");
    socket.destroy();
    return;
  }

  const key = req.headers["sec-websocket-key"];
  const accept = crypto
    .createHash("sha1")
    .update(`${key}258EAFA5-E914-47DA-95CA-C5AB0DC85B11`)
    .digest("base64");

  socket.write(
    [
      "HTTP/1.1 101 Switching Protocols",
      "Upgrade: websocket",
      "Connection: Upgrade",
      `Sec-WebSocket-Accept: ${accept}`,
      "\r\n"
    ].join("\r\n")
  );

  joinRoom(documentId, socket, user);
}

function joinRoom(documentId, socket, user) {
  const client = {
    id: crypto.randomUUID(),
    documentId,
    socket,
    user,
    cursor: null,
    buffer: Buffer.alloc(0),
    joinedAt: new Date().toISOString()
  };

  if (!rooms.has(documentId)) {
    rooms.set(documentId, new Set());
  }
  rooms.get(documentId).add(client);

  sendWs(socket, {
    type: "document:init",
    clientId: client.id,
    document: getDocument(documentId),
    users: getRoomUsers(documentId),
    cursors: getRoomCursors(documentId, client)
  });
  broadcastPresence(documentId);

  socket.on("data", chunk => {
    client.buffer = Buffer.concat([client.buffer, chunk]);
    client.buffer = parseFrames(client, client.buffer);
  });
  socket.on("close", () => leaveRoom(client));
  socket.on("end", () => leaveRoom(client));
  socket.on("error", () => leaveRoom(client));
}

function leaveRoom(client) {
  const room = rooms.get(client.documentId);
  if (!room || !room.has(client)) return;
  room.delete(client);
  if (room.size === 0) {
    rooms.delete(client.documentId);
  } else {
    broadcastToRoom(client.documentId, {
      type: "cursor:remove",
      documentId: client.documentId,
      clientId: client.id
    });
    broadcastPresence(client.documentId);
  }
}

function parseFrames(client, buffer) {
  let offset = 0;

  while (buffer.length - offset >= 2) {
    const firstByte = buffer[offset];
    const secondByte = buffer[offset + 1];
    const opcode = firstByte & 0x0f;
    const masked = Boolean(secondByte & 0x80);
    let payloadLength = secondByte & 0x7f;
    let headerLength = 2;

    if (payloadLength === 126) {
      if (buffer.length - offset < 4) break;
      payloadLength = buffer.readUInt16BE(offset + 2);
      headerLength = 4;
    } else if (payloadLength === 127) {
      if (buffer.length - offset < 10) break;
      const high = buffer.readUInt32BE(offset + 2);
      const low = buffer.readUInt32BE(offset + 6);
      payloadLength = high * 2 ** 32 + low;
      headerLength = 10;
    }

    const maskLength = masked ? 4 : 0;
    const frameLength = headerLength + maskLength + payloadLength;
    if (buffer.length - offset < frameLength) break;

    const mask = masked ? buffer.slice(offset + headerLength, offset + headerLength + 4) : null;
    const payloadStart = offset + headerLength + maskLength;
    const payload = Buffer.from(buffer.slice(payloadStart, payloadStart + payloadLength));

    if (masked && mask) {
      for (let index = 0; index < payload.length; index += 1) {
        payload[index] ^= mask[index % 4];
      }
    }

    if (opcode === 0x8) {
      client.socket.end();
      return Buffer.alloc(0);
    }
    if (opcode === 0x9) {
      sendRawFrame(client.socket, payload, 0xA);
    }
    if (opcode === 0x1) {
      handleWsMessage(client, payload.toString("utf8"));
    }

    offset += frameLength;
  }

  return buffer.slice(offset);
}

function handleWsMessage(client, rawMessage) {
  let message;
  try {
    message = JSON.parse(rawMessage);
  } catch {
    sendWs(client.socket, { type: "error", message: "Invalid JSON message" });
    return;
  }

  if (message.type === "document:update") {
    const document = getDocument(client.documentId);
    if (!document) return;

    client.cursor = normalizeCursor(message.selection) || client.cursor;
    document.content = String(message.content || "");
    document.revision += 1;
    document.updatedAt = new Date().toISOString();
    saveStore();

    const payload = {
      type: "document:update",
      documentId: document.id,
      content: document.content,
      revision: document.revision,
      updatedAt: document.updatedAt,
      author: client.user,
      cursor: client.cursor
        ? {
            clientId: client.id,
            user: client.user,
            selection: client.cursor
          }
        : null
    };
    broadcastToRoom(client.documentId, payload, client);
    sendWs(client.socket, {
      type: "document:saved",
      documentId: document.id,
      revision: document.revision,
      updatedAt: document.updatedAt
    });
    return;
  }

  if (message.type === "cursor:update") {
    client.cursor = normalizeCursor(message.selection);
    broadcastToRoom(client.documentId, {
      type: "cursor:update",
      documentId: client.documentId,
      clientId: client.id,
      user: client.user,
      selection: client.cursor
    });
    return;
  }

  if (message.type === "ping") {
    sendWs(client.socket, { type: "pong", time: Date.now() });
  }
}

function getRoomUsers(documentId) {
  const room = rooms.get(documentId) || new Set();
  const usersById = new Map();
  for (const client of room) {
    usersById.set(client.user.id, {
      id: client.user.id,
      username: client.user.username,
      nickname: client.user.nickname,
      color: client.user.color
    });
  }
  return [...usersById.values()];
}

function getRoomCursors(documentId, exceptClient = null) {
  const room = rooms.get(documentId) || new Set();
  const cursors = [];
  for (const client of room) {
    if (client === exceptClient || !client.cursor) continue;
    cursors.push({
      clientId: client.id,
      user: {
        id: client.user.id,
        username: client.user.username,
        nickname: client.user.nickname,
        color: client.user.color
      },
      selection: client.cursor
    });
  }
  return cursors;
}

function normalizeCursor(selection) {
  if (!selection || typeof selection !== "object") return null;

  const offset = Number(selection.offset);
  const textOffset = Number(selection.textOffset ?? selection.offset);
  const path = Array.isArray(selection.path)
    ? selection.path.map(segment => Number(segment)).filter(Number.isInteger)
    : null;
  const rect =
    selection.rect &&
    Number.isFinite(Number(selection.rect.left)) &&
    Number.isFinite(Number(selection.rect.top))
      ? {
          left: Number(selection.rect.left),
          top: Number(selection.rect.top),
          height: Math.max(12, Number(selection.rect.height) || 24)
        }
      : null;

  if (!path && !Number.isFinite(textOffset)) return null;
  return {
    path,
    offset: Number.isFinite(offset) ? Math.max(0, Math.floor(offset)) : 0,
    textOffset: Number.isFinite(textOffset) ? Math.max(0, Math.floor(textOffset)) : 0,
    rect
  };
}

function broadcastPresence(documentId) {
  broadcastToRoom(documentId, {
    type: "presence:update",
    documentId,
    users: getRoomUsers(documentId)
  });
}

function broadcastToRoom(documentId, message, exceptClient = null) {
  const room = rooms.get(documentId);
  if (!room) return;
  for (const client of room) {
    if (client === exceptClient) continue;
    sendWs(client.socket, message);
  }
}

function sendWs(socket, message) {
  sendRawFrame(socket, Buffer.from(JSON.stringify(message)), 0x1);
}

function sendRawFrame(socket, payload, opcode = 0x1) {
  if (socket.destroyed) return;

  let header;
  if (payload.length < 126) {
    header = Buffer.from([0x80 | opcode, payload.length]);
  } else if (payload.length < 65536) {
    header = Buffer.alloc(4);
    header[0] = 0x80 | opcode;
    header[1] = 126;
    header.writeUInt16BE(payload.length, 2);
  } else {
    header = Buffer.alloc(10);
    header[0] = 0x80 | opcode;
    header[1] = 127;
    header.writeUInt32BE(0, 2);
    header.writeUInt32BE(payload.length, 6);
  }

  socket.write(Buffer.concat([header, payload]));
}

function startServer(port) {
  const server = http.createServer(handleHttp);
  server.on("upgrade", handleUpgrade);
  server.on("error", error => {
    if (error.code === "EADDRINUSE") {
      startServer(port + 1);
      return;
    }
    console.error(error);
    process.exitCode = 1;
  });
  server.listen(port, () => {
    console.log(`Cloud document demo is running at http://localhost:${port}`);
  });
}

startServer(DEFAULT_PORT);
