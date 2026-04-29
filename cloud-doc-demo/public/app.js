const state = {
  token: localStorage.getItem("cloud-doc-token") || "",
  user: readJson(localStorage.getItem("cloud-doc-user")),
  documents: [],
  currentDocument: null,
  socket: null,
  clientId: "",
  suppressInput: false,
  cursors: new Map(),
  saveTimer: null,
  titleTimer: null,
  cursorTimer: null,
  lastCursorKey: ""
};

const nodes = {
  loginPanel: document.getElementById("loginPanel"),
  username: document.getElementById("username"),
  loginBtn: document.getElementById("loginBtn"),
  workspacePanel: document.getElementById("workspacePanel"),
  currentUserAvatar: document.getElementById("currentUserAvatar"),
  currentUserName: document.getElementById("currentUserName"),
  newDocTitle: document.getElementById("newDocTitle"),
  createDocBtn: document.getElementById("createDocBtn"),
  docList: document.getElementById("docList"),
  docTitle: document.getElementById("docTitle"),
  presence: document.getElementById("presence"),
  connectionState: document.getElementById("connectionState"),
  emptyState: document.getElementById("emptyState"),
  editorPanel: document.getElementById("editorPanel"),
  editor: document.getElementById("editor"),
  remoteCursors: document.getElementById("remoteCursors"),
  saveState: document.getElementById("saveState"),
  revisionState: document.getElementById("revisionState")
};

nodes.loginBtn.addEventListener("click", login);
nodes.createDocBtn.addEventListener("click", createDocument);
nodes.newDocTitle.addEventListener("keydown", event => {
  if (event.key === "Enter") createDocument();
});
nodes.editor.addEventListener("input", handleEditorInput);
nodes.editor.addEventListener("focus", sendCursorPosition);
nodes.editor.addEventListener("keyup", sendCursorPosition);
nodes.editor.addEventListener("mouseup", sendCursorPosition);
nodes.docTitle.addEventListener("input", handleTitleInput);
window.document.addEventListener("selectionchange", queueCursorPosition);
window.addEventListener("resize", renderCursors);
window.addEventListener("scroll", renderCursors, true);

bootstrap();

async function bootstrap() {
  if (!state.token || !state.user) {
    showLogin();
    return;
  }

  showWorkspace();
  await loadDocuments();
}

function readJson(value) {
  if (!value) return null;
  try {
    return JSON.parse(value);
  } catch {
    return null;
  }
}

async function login() {
  const username = nodes.username.value;
  const response = await request("/api/auth/login", {
    method: "POST",
    body: JSON.stringify({ username })
  });

  state.token = response.token;
  state.user = response.user;
  localStorage.setItem("cloud-doc-token", state.token);
  localStorage.setItem("cloud-doc-user", JSON.stringify(state.user));

  showWorkspace();
  await loadDocuments();
}

function showLogin() {
  nodes.loginPanel.classList.remove("hidden");
  nodes.workspacePanel.classList.add("hidden");
  renderEmpty();
}

function showWorkspace() {
  nodes.loginPanel.classList.add("hidden");
  nodes.workspacePanel.classList.remove("hidden");
  nodes.currentUserName.textContent = state.user.nickname;
  renderAvatar(nodes.currentUserAvatar, state.user);
}

async function loadDocuments() {
  const response = await request("/api/documents");
  state.documents = response.documents;
  renderDocumentList();

  if (!state.currentDocument && state.documents.length > 0) {
    await openDocument(state.documents[0].id);
  }
}

async function createDocument() {
  const title = nodes.newDocTitle.value.trim() || "未命名文档";
  const response = await request("/api/documents", {
    method: "POST",
    body: JSON.stringify({ title })
  });
  nodes.newDocTitle.value = "";
  await loadDocuments();
  await openDocument(response.document.id);
}

function renderDocumentList() {
  nodes.docList.innerHTML = "";
  for (const doc of state.documents) {
    const item = window.document.createElement("button");
    item.className = "doc-item";
    if (state.currentDocument?.id === doc.id) item.classList.add("active");
    item.type = "button";
    item.innerHTML = `<strong></strong><span></span>`;
    item.querySelector("strong").textContent = doc.title;
    item.querySelector("span").textContent = `版本 ${doc.revision} · ${formatTime(doc.updatedAt)}`;
    item.addEventListener("click", () => openDocument(doc.id));
    nodes.docList.appendChild(item);
  }
}

async function openDocument(documentId) {
  const response = await request(`/api/documents/${documentId}`);
  state.currentDocument = response.document;
  renderDocument();
  renderDocumentList();
  connectDocumentSocket(documentId);
}

function renderDocument() {
  nodes.emptyState.classList.add("hidden");
  nodes.editorPanel.classList.remove("hidden");
  nodes.docTitle.disabled = false;
  nodes.docTitle.value = state.currentDocument.title;
  setEditorHtml(state.currentDocument.content);
  nodes.revisionState.textContent = `版本 ${state.currentDocument.revision}`;
  nodes.saveState.textContent = `已保存 · ${formatTime(state.currentDocument.updatedAt)}`;
}

function renderEmpty() {
  nodes.emptyState.classList.remove("hidden");
  nodes.editorPanel.classList.add("hidden");
  nodes.docTitle.disabled = true;
  nodes.docTitle.value = "";
  nodes.presence.innerHTML = "";
  state.clientId = "";
  state.lastCursorKey = "";
  state.cursors.clear();
  renderCursors();
  setConnectionState("未连接", "offline");
}

function connectDocumentSocket(documentId) {
  if (state.socket) {
    state.socket.close();
    state.socket = null;
  }
  state.clientId = "";
  state.lastCursorKey = "";
  state.cursors.clear();
  renderCursors();

  setConnectionState("连接中", "");
  const protocol = window.location.protocol === "https:" ? "wss:" : "ws:";
  const socket = new WebSocket(
    `${protocol}//${window.location.host}/ws/documents/${documentId}?token=${encodeURIComponent(state.token)}`
  );
  state.socket = socket;

  socket.addEventListener("open", () => setConnectionState("已连接", "online"));
  socket.addEventListener("close", () => setConnectionState("已断开", "offline"));
  socket.addEventListener("error", () => setConnectionState("连接异常", "offline"));
  socket.addEventListener("message", event => handleSocketMessage(JSON.parse(event.data)));
}

function handleSocketMessage(message) {
  if (message.type === "document:init") {
    state.clientId = message.clientId;
    state.currentDocument = message.document;
    state.cursors.clear();
    for (const cursor of message.cursors || []) {
      storeCursor(cursor);
    }
    renderDocument();
    renderPresence(message.users);
    renderCursors();
    queueCursorPosition();
    return;
  }

  if (message.type === "document:update") {
    if (!state.currentDocument || message.documentId !== state.currentDocument.id) return;
    state.currentDocument.content = message.content;
    state.currentDocument.revision = message.revision;
    state.currentDocument.updatedAt = message.updatedAt;
    if (message.cursor) storeCursor(message.cursor);
    setEditorHtml(message.content);
    nodes.revisionState.textContent = `版本 ${message.revision}`;
    nodes.saveState.textContent = `${message.author.nickname} 已更新 · ${formatTime(message.updatedAt)}`;
    renderCursors();
    void loadDocuments();
    return;
  }

  if (message.type === "document:saved") {
    if (!state.currentDocument || message.documentId !== state.currentDocument.id) return;
    state.currentDocument.revision = message.revision;
    state.currentDocument.updatedAt = message.updatedAt;
    nodes.revisionState.textContent = `版本 ${message.revision}`;
    nodes.saveState.textContent = `已保存 · ${formatTime(message.updatedAt)}`;
    void loadDocuments();
    return;
  }

  if (message.type === "document:title") {
    if (!state.currentDocument || message.documentId !== state.currentDocument.id) return;
    state.currentDocument.title = message.title;
    nodes.docTitle.value = message.title;
    void loadDocuments();
    return;
  }

  if (message.type === "presence:update") {
    renderPresence(message.users);
    return;
  }

  if (message.type === "cursor:update") {
    storeCursor(message);
    renderCursors();
    return;
  }

  if (message.type === "cursor:remove") {
    state.cursors.delete(message.clientId);
    renderCursors();
  }
}

function handleEditorInput(event) {
  if (state.suppressInput || !state.currentDocument || !state.socket) return;
  const selection = getEditorSelection();
  if (selection) publishCursor(selection);

  nodes.saveState.textContent = "保存中...";
  clearTimeout(state.saveTimer);
  const shouldFlushQuickly =
    event?.inputType === "insertParagraph" || event?.inputType === "insertLineBreak";
  state.saveTimer = setTimeout(sendDocumentUpdate, shouldFlushQuickly ? 30 : 180);
}

function sendDocumentUpdate() {
  if (!state.currentDocument || !state.socket || state.socket.readyState !== WebSocket.OPEN) return;

  const selection = getEditorSelection();
  state.socket.send(
    JSON.stringify({
      type: "document:update",
      documentId: state.currentDocument.id,
      content: nodes.editor.innerHTML,
      selection
    })
  );

  if (selection && state.clientId) {
    storeCursor({
      clientId: state.clientId,
      user: state.user,
      selection
    });
    renderCursors();
  }
}

function handleTitleInput() {
  if (!state.currentDocument) return;
  clearTimeout(state.titleTimer);
  state.titleTimer = setTimeout(async () => {
    const title = nodes.docTitle.value.trim();
    if (!title || title === state.currentDocument.title) return;
    await request(`/api/documents/${state.currentDocument.id}/title`, {
      method: "PUT",
      body: JSON.stringify({ title })
    });
    state.currentDocument.title = title;
    await loadDocuments();
  }, 420);
}

function setEditorHtml(html) {
  state.suppressInput = true;
  const active = window.document.activeElement === nodes.editor;
  const previousSelection = active ? getEditorSelection({ includeRect: false }) : null;
  nodes.editor.innerHTML = html || "<p></p>";
  if (active && previousSelection) restoreEditorSelection(previousSelection);
  state.suppressInput = false;
  if (active) queueCursorPosition();
}

function queueCursorPosition() {
  clearTimeout(state.cursorTimer);
  state.cursorTimer = setTimeout(sendCursorPosition, 80);
}

function sendCursorPosition() {
  if (!state.currentDocument || !state.socket || state.socket.readyState !== WebSocket.OPEN) return;
  if (window.document.activeElement !== nodes.editor) return;

  const selection = getEditorSelection();
  if (!selection) return;
  const cursorKey = getCursorKey(selection);
  if (cursorKey === state.lastCursorKey) return;
  state.lastCursorKey = cursorKey;

  publishCursor(selection);
}

function publishCursor(selection) {
  state.lastCursorKey = getCursorKey(selection);
  state.socket.send(
    JSON.stringify({
      type: "cursor:update",
      documentId: state.currentDocument.id,
      selection
    })
  );

  if (state.clientId) {
    storeCursor({
      clientId: state.clientId,
      user: state.user,
      selection
    });
    renderCursors();
  }
}

function getCursorKey(selection) {
  return JSON.stringify({
    path: selection.path || [],
    offset: selection.offset,
    textOffset: selection.textOffset
  });
}

function getEditorSelection(options = {}) {
  const includeRect = options.includeRect !== false;
  const selection = window.getSelection();
  if (!selection || selection.rangeCount === 0) return null;
  if (!nodes.editor.contains(selection.focusNode)) return null;

  const range = window.document.createRange();
  range.setStart(selection.focusNode, selection.focusOffset);
  range.collapse(true);

  const snapshot = {
    path: getNodePath(nodes.editor, selection.focusNode),
    offset: selection.focusOffset,
    textOffset: getLogicalTextOffset(nodes.editor, range)
  };

  if (includeRect) {
    const rect = getRangeClientRect(range);
    if (rect) {
      const editorRect = nodes.editor.getBoundingClientRect();
      snapshot.rect = {
        left: rect.left - editorRect.left,
        top: rect.top - editorRect.top,
        height: rect.height || 24
      };
    }
  }

  return snapshot;
}

function getNodePath(root, node) {
  const path = [];
  let current = node;

  while (current && current !== root) {
    const parent = current.parentNode;
    if (!parent) return [];
    path.unshift(Array.prototype.indexOf.call(parent.childNodes, current));
    current = parent;
  }

  return path;
}

function resolveNodePath(root, path) {
  if (!Array.isArray(path)) return null;
  let node = root;

  for (const segment of path) {
    if (!node || !node.childNodes) return null;
    node = node.childNodes[segment];
  }

  return node || null;
}

function getLogicalTextOffset(root, range) {
  const prefix = range.cloneRange();
  prefix.selectNodeContents(root);
  prefix.setEnd(range.endContainer, range.endOffset);
  return getLogicalTextLength(prefix.cloneContents());
}

function getLogicalTextLength(node) {
  if (node.nodeType === Node.TEXT_NODE) return node.textContent.length;
  if (node.nodeType !== Node.ELEMENT_NODE && node.nodeType !== Node.DOCUMENT_FRAGMENT_NODE) return 0;

  let length = 0;
  for (const child of node.childNodes) {
    if (child.nodeType === Node.ELEMENT_NODE && child.tagName === "BR") {
      length += 1;
      continue;
    }

    length += getLogicalTextLength(child);
    if (child.nodeType === Node.ELEMENT_NODE && isBlockElement(child)) {
      length += 1;
    }
  }
  return length;
}

function isBlockElement(node) {
  return /^(ADDRESS|ARTICLE|ASIDE|BLOCKQUOTE|DD|DIV|DL|DT|FIGCAPTION|FIGURE|FOOTER|H[1-6]|HEADER|HR|LI|MAIN|NAV|OL|P|PRE|SECTION|TABLE|UL)$/i.test(
    node.tagName || ""
  );
}

function storeCursor(message) {
  if (!message.clientId || !message.user) return;
  if (!isValidCursorSelection(message.selection)) return;

  state.cursors.set(message.clientId, {
    clientId: message.clientId,
    user: message.user,
    selection: message.selection
  });
}

function isValidCursorSelection(selection) {
  if (!selection || typeof selection !== "object") return false;
  return (
    Array.isArray(selection.path) ||
    Number.isFinite(Number(selection.textOffset)) ||
    Number.isFinite(Number(selection.offset)) ||
    Boolean(selection.rect)
  );
}

function renderCursors() {
  const active = window.document.activeElement === nodes.editor;
  const previousSelection = active ? getEditorSelection({ includeRect: false }) : null;

  nodes.remoteCursors.innerHTML = "";
  if (!state.currentDocument || nodes.editorPanel.classList.contains("hidden")) {
    if (active && previousSelection) restoreEditorSelection(previousSelection);
    return;
  }

  const groups = new Map();
  const panelRect = nodes.editorPanel.getBoundingClientRect();

  for (const cursor of state.cursors.values()) {
    const rect = getCursorRect(cursor.selection);
    if (!rect) continue;

    const left = rect.left - panelRect.left;
    const top = rect.top - panelRect.top;
    const key = getCursorGroupKey(left, top);
    const group =
      groups.get(key) ||
      {
        left,
        top,
        height: Math.max(18, rect.height || 24),
        cursors: []
      };

    group.left = Math.min(group.left, left);
    group.top = Math.min(group.top, top);
    group.height = Math.max(group.height, Math.max(18, rect.height || 24));
    group.cursors.push(cursor);
    groups.set(key, group);
  }

  for (const group of groups.values()) {
    renderCursorGroup(group);
  }

  if (active && previousSelection) restoreEditorSelection(previousSelection);
}

function getCursorGroupKey(left, top) {
  const bucketSize = 6;
  return `${Math.round(left / bucketSize) * bucketSize}:${Math.round(top / bucketSize) * bucketSize}`;
}

function renderCursorGroup(group) {
  const groupNode = window.document.createElement("div");
  groupNode.className = group.cursors.some(cursor => cursor.clientId === state.clientId)
    ? "remote-cursor-group local-cursor"
    : "remote-cursor-group";
  groupNode.style.left = `${group.left}px`;
  groupNode.style.top = `${group.top}px`;
  groupNode.style.height = `${group.height}px`;
  groupNode.title = group.cursors
    .map(cursor => cursor.user.nickname || cursor.user.username || "")
    .filter(Boolean)
    .join(" / ");

  const line = window.document.createElement("div");
  line.className = "remote-cursor-line";
  line.style.background = createCursorLineBackground(group.cursors);
  groupNode.appendChild(line);

  const stack = window.document.createElement("div");
  stack.className = "remote-cursor-label-stack";

  for (const cursor of group.cursors) {
    const label = window.document.createElement("span");
    label.className = cursor.clientId === state.clientId
      ? "remote-cursor-label local-cursor-label"
      : "remote-cursor-label";
    label.style.setProperty("--cursor-color", cursor.user.color || "#2563eb");
    label.textContent = truncateUserLabel(cursor.user);
    stack.appendChild(label);
  }

  groupNode.appendChild(stack);
  nodes.remoteCursors.appendChild(groupNode);
}

function createCursorLineBackground(cursors) {
  const colors = cursors.map(cursor => cursor.user.color || "#2563eb");
  if (colors.length <= 1) return colors[0] || "#2563eb";

  const step = 100 / colors.length;
  const stops = colors.flatMap((color, index) => [
    `${color} ${Math.round(index * step)}%`,
    `${color} ${Math.round((index + 1) * step)}%`
  ]);
  return `linear-gradient(to bottom, ${stops.join(", ")})`;
}

function getCursorRect(selection) {
  const range = createRangeFromSelection(nodes.editor, selection);
  const rect = range ? getRangeClientRect(range) : null;
  if (rect) return rect;

  if (selection.rect) {
    const editorRect = nodes.editor.getBoundingClientRect();
    return {
      left: editorRect.left + selection.rect.left,
      top: editorRect.top + selection.rect.top,
      height: selection.rect.height || 24
    };
  }

  return null;
}

function getRangeClientRect(range) {
  const rect = range.getClientRects()[0] || range.getBoundingClientRect();
  if (isUsableRect(rect)) return rect;

  const marker = window.document.createElement("span");
  marker.className = "cursor-measure";
  marker.textContent = "\u200b";
  marker.setAttribute("data-cursor-measure", "true");
  marker.style.display = "inline-block";
  marker.style.width = "0";
  marker.style.height = "1.2em";

  const measureRange = range.cloneRange();
  measureRange.insertNode(marker);
  const markerRect = marker.getBoundingClientRect();
  const parent = marker.parentNode;
  marker.remove();
  if (parent && parent.normalize) parent.normalize();

  return isUsableRect(markerRect) ? markerRect : null;
}

function isUsableRect(rect) {
  return Boolean(rect && Number.isFinite(rect.left) && Number.isFinite(rect.top) && (rect.width || rect.height));
}

function createRangeFromSelection(root, selection) {
  const pathRange = createRangeFromPath(root, selection.path, selection.offset);
  if (pathRange) return pathRange;

  const fallbackOffset = Number(selection.textOffset ?? selection.offset);
  if (Number.isFinite(fallbackOffset)) {
    return createRangeFromTextOffset(root, fallbackOffset);
  }

  return null;
}

function createRangeFromPath(root, path, offset) {
  const node = resolveNodePath(root, path);
  if (!node) return null;

  const range = window.document.createRange();
  const maxOffset =
    node.nodeType === Node.TEXT_NODE ? node.textContent.length : node.childNodes?.length || 0;
  range.setStart(node, Math.max(0, Math.min(Number(offset) || 0, maxOffset)));
  range.collapse(true);
  return range;
}

function createRangeFromTextOffset(root, offset) {
  const range = window.document.createRange();
  const targetOffset = Math.max(0, Number(offset) || 0);
  let currentOffset = 0;
  let found = false;

  function setRange(node, offsetValue) {
    range.setStart(node, offsetValue);
    range.collapse(true);
    found = true;
  }

  function visit(node) {
    if (found) return;

    if (node.nodeType === Node.TEXT_NODE) {
      const nextOffset = currentOffset + node.textContent.length;
      if (targetOffset <= nextOffset) {
        setRange(node, targetOffset - currentOffset);
      }
      currentOffset = nextOffset;
      return;
    }

    if (node.nodeType !== Node.ELEMENT_NODE && node.nodeType !== Node.DOCUMENT_FRAGMENT_NODE) {
      return;
    }

    if (node.nodeType === Node.ELEMENT_NODE && node.tagName === "BR") {
      const nextOffset = currentOffset + 1;
      if (targetOffset <= nextOffset) {
        const parent = node.parentNode;
        setRange(parent, Array.prototype.indexOf.call(parent.childNodes, node) + 1);
      }
      currentOffset = nextOffset;
      return;
    }

    if (node !== root && node.childNodes.length === 0 && isBlockElement(node)) {
      if (targetOffset <= currentOffset) {
        setRange(node, 0);
        return;
      }
    }

    for (const child of node.childNodes) {
      visit(child);
      if (found) return;
    }

    if (node !== root && node.nodeType === Node.ELEMENT_NODE && isBlockElement(node)) {
      const nextOffset = currentOffset + 1;
      if (targetOffset <= nextOffset) {
        const parent = node.parentNode;
        setRange(parent, Array.prototype.indexOf.call(parent.childNodes, node) + 1);
      }
      currentOffset = nextOffset;
    }
  }

  visit(root);
  if (found) return range;

  range.selectNodeContents(root);
  range.collapse(false);
  return range;
}

function restoreEditorSelection(selection) {
  const range = createRangeFromSelection(nodes.editor, selection);
  if (!range) return false;

  const browserSelection = window.getSelection();
  browserSelection.removeAllRanges();
  browserSelection.addRange(range);
  return true;
}

function truncateUserLabel(user) {
  const name = user.username || user.nickname || "?";
  return Array.from(name).slice(0, 3).join("");
}

function renderPresence(users) {
  nodes.presence.innerHTML = "";
  for (const user of users) {
    const avatar = window.document.createElement("span");
    avatar.className = "avatar";
    avatar.title = user.nickname;
    renderAvatar(avatar, user);
    nodes.presence.appendChild(avatar);
  }
}

function renderAvatar(node, user) {
  node.style.background = user.color;
  node.textContent = (user.nickname || user.username || "?").slice(0, 1).toUpperCase();
}

function setConnectionState(text, className) {
  nodes.connectionState.textContent = text;
  nodes.connectionState.className = `status ${className || ""}`.trim();
}

async function request(path, options = {}) {
  const headers = {
    "Content-Type": "application/json",
    ...(options.headers || {})
  };
  if (state.token) headers.Authorization = `Bearer ${state.token}`;

  const response = await fetch(path, {
    ...options,
    headers
  });

  const data = await response.json().catch(() => ({}));
  if (!response.ok) {
    throw new Error(data.message || `Request failed: ${response.status}`);
  }
  return data;
}

function formatTime(value) {
  if (!value) return "";
  return new Intl.DateTimeFormat("zh-CN", {
    hour: "2-digit",
    minute: "2-digit",
    second: "2-digit"
  }).format(new Date(value));
}
