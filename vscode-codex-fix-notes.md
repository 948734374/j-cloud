# VSCode Codex 插件卡住修复记录

日期：2026-04-28

## 现象

VSCode 右侧 Codex 面板一直停在加载动画，无法进入正常聊天界面。

截图表现：

- `CODEX` 标签页已打开。
- 中间只有 Codex 加载图标。
- 没有明显的前端报错提示。

## 核心结论

真正的问题不是网络，也不是项目代码问题，而是 VSCode Codex 插件正常启动时读取了默认路径 `~/.codex/auth.json` 里的旧登录态。这个旧登录态对应的 ChatGPT workspace 已被服务端判定为不可用：

```text
402 Payment Required
deactivated_workspace
```

之前临时修好，是因为从当前 JetBrains/Codex 会话里启动 VSCode 时，VSCode 继承了环境变量 `CODEX_HOME`，读取到了另一个目录里的有效登录态：

```text
/Users/zhangyichi/Library/Caches/JetBrains/IntelliJIdea2026.1/aia/codex/auth.json
```

但从 Dock/Finder 正常重开 VSCode 时，不会继承这个 `CODEX_HOME`，所以又回到默认的：

```text
/Users/zhangyichi/.codex/auth.json
```

这就是“当时修好了，关掉 VSCode 再打开又卡住”的原因。

## 有效排查步骤

1. 查看最新 VSCode 日志目录：

```bash
ls -lt ~/Library/Application\ Support/Code/logs | head -10
```

2. 查看 Codex 插件日志：

```bash
sed -n '1,260p' ~/Library/Application\ Support/Code/logs/<latest>/window1/exthost/openai.chatgpt/Codex.log
```

或者：

```bash
tail -200 ~/Library/Application\ Support/Code/logs/<latest>/window1/exthost/openai.chatgpt/Codex.log
```

3. 如果看到下面这些错误，说明是旧 workspace/token 问题：

```text
402 Payment Required
deactivated_workspace
/wham/accounts/check
/backend-api/codex/models
```

4. 检查 Codex CLI 当前登录状态：

```bash
/Users/zhangyichi/.vscode/extensions/openai.chatgpt-26.422.62136-darwin-arm64/bin/macos-aarch64/codex login status
```

5. 不打印 token，只查看默认登录文件的元信息：

```bash
jq '{auth_mode,last_refresh,has_api_key:(.OPENAI_API_KEY != null), has_tokens:(.tokens != null), has_account_id:(.tokens.account_id != null)}' ~/.codex/auth.json
```

6. 查看当前 shell 是否有 `CODEX_HOME`：

```bash
printenv CODEX_HOME
```

昨天有效的 `CODEX_HOME` 是：

```text
/Users/zhangyichi/Library/Caches/JetBrains/IntelliJIdea2026.1/aia/codex
```

7. 对比有效登录态的元信息：

```bash
jq '{auth_mode,last_refresh,has_api_key:(.OPENAI_API_KEY != null), has_tokens:(.tokens != null), has_account_id:(.tokens.account_id != null)}' /Users/zhangyichi/Library/Caches/JetBrains/IntelliJIdea2026.1/aia/codex/auth.json
```

昨天关键差异是：

- `~/.codex/auth.json` 的 `last_refresh` 停在 `2026-04-27`。
- JetBrains/Codex 缓存目录里的 `auth.json` 是 `2026-04-28` 的新登录态。

## 持久修复步骤

1. 关闭所有 VSCode 窗口，避免插件进程继续使用旧状态。

2. 备份默认登录文件：

```bash
cp -p ~/.codex/auth.json ~/.codex/auth.json.backup.$(date +%Y%m%dT%H%M)
```

3. 用已验证可用的登录态覆盖默认路径：

```bash
cp -p /Users/zhangyichi/Library/Caches/JetBrains/IntelliJIdea2026.1/aia/codex/auth.json ~/.codex/auth.json
```

4. 验证默认路径已经变成新登录态：

```bash
jq '{auth_mode,last_refresh,has_api_key:(.OPENAI_API_KEY != null), has_tokens:(.tokens != null), has_account_id:(.tokens.account_id != null)}' ~/.codex/auth.json
```

昨天修复后的结果中，`last_refresh` 变成：

```text
2026-04-28T10:47:44.605835Z
```

5. 模拟 Dock/Finder 正常启动 VSCode 时的 Codex 认证路径，取消 `CODEX_HOME` 后测试：

```bash
env -u CODEX_HOME /Users/zhangyichi/.vscode/extensions/openai.chatgpt-26.422.62136-darwin-arm64/bin/macos-aarch64/codex exec "Reply with OK only."
```

判断标准：

- 如果不再出现 `deactivated_workspace`，说明默认 `~/.codex/auth.json` 已修好。
- 如果返回 `usage limit`，说明认证已通过，只是当前账号临时达到用量限制。

昨天验证时返回的是：

```text
You've hit your usage limit. To get more access now, send a request to your admin or try again at 11:25 PM.
```

这说明 `deactivated_workspace` 问题已经修复，剩余问题是账号用量限制。

6. 重新打开 VSCode。

如果当前 VSCode 里还有旧 Codex app-server，可以先定位并结束它：

```bash
pgrep -af codex
ps -p <pid> -o pid,ppid,command
kill <vscode-codex-app-server-pid>
```

VSCode 插件进程一般长这样：

```text
/Users/zhangyichi/.vscode/extensions/openai.chatgpt-26.422.62136-darwin-arm64/bin/macos-aarch64/codex app-server --analytics-default-enabled
```

## 修复后验证

重新打开 VSCode 后，再看新的 Codex 日志：

```bash
tail -180 ~/Library/Application\ Support/Code/logs/<latest>/window*/exthost/openai.chatgpt/Codex.log
```

修复成功的关键判断：

- 不再出现 `402 Payment Required`。
- 不再出现 `deactivated_workspace`。
- 可以看到 WebView 正常 mounted：

```text
[startup][renderer] app routes mounted
```

昨天最后一次新窗口日志中，`deactivated_workspace` 已消失，只剩一个非阻断错误：

```text
unsupported feature enablement `workspace_dependencies`
```

这个是扩展和 app-server 功能开关不匹配导致的日志噪声，不是导致面板卡住的根因。

## 注意事项

- 不要只从当前 Codex/JetBrains 环境里启动 VSCode 来判断是否修好；那可能只是继承了 `CODEX_HOME` 的临时效果。
- 真正的持久修复必须让 `~/.codex/auth.json` 本身变成有效登录态。
- 不要把 `auth.json` 内容贴到聊天或日志里；里面有 token。
- 如果以后再次出现同样问题，优先检查 `~/.codex/auth.json` 的 `last_refresh` 和插件日志里的 `deactivated_workspace`。
- 如果认证已正常但提示 `usage limit`，那不是本机配置问题，需要等额度恢复或联系管理员。
