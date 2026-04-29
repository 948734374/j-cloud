# IDEA Codex Windows 离线安装与 Auth 缓存修复方案

适用场景：

- Windows 台式机上的 IntelliJ IDEA / AI Assistant 选择 Codex 时报下载连接超时。
- 已经从 GitHub Releases 下载了 Codex 的 Windows 压缩包。
- IDEA 里 Codex 可能还读到了旧的 ChatGPT workspace/auth 缓存。

## 核心思路

IDEA 的 AI Assistant 使用 Codex 时，会在 JetBrains 缓存目录下维护一套独立的 Codex 环境，通常包含：

```text
aia\codex\
  bin\
  downloads\
  auth.json
  config.toml
```

下载超时的问题，本质上是 IDEA 没能自动下载 Codex 可执行文件。

离线修复就是：

1. 手动把 Windows 版 Codex 可执行文件放进 IDEA 的 `aia\codex\bin`。
2. 清掉或替换 IDEA 专用的 `auth.json`，避免继续使用旧 workspace。
3. 重启 IDEA，让 AI Assistant 重新加载 Codex。

## 1. 确认下载包是否正确

不要使用 GitHub 页面右上角 `Code -> Download ZIP` 下载的源码包。

应该下载 GitHub Releases 里的 Windows release asset：

```text
https://github.com/openai/codex/releases
```

正确的压缩包解压后，里面应该有 `.exe` 可执行文件，例如：

```text
codex-x86_64-pc-windows-msvc.exe
codex-acp-x86_64-pc-windows-msvc.exe
```

文件名可能随版本变化，判断标准是：

- 必须是 Windows 对应的 `.exe`。
- 至少要有 Codex 主程序。
- 如果有 `codex-acp`，也一起放入 `bin`。

如果解压后看到的是源码文件，例如：

```text
package.json
codex-rs
README.md
```

那说明下载错了，需要重新下载 release asset。

## 2. 找到 IDEA Codex 缓存目录

在 Windows PowerShell 中执行：

```powershell
$codexHome="$env:LOCALAPPDATA\JetBrains\IntelliJIdea2026.1\aia\codex"
mkdir "$codexHome\bin" -Force
mkdir "$codexHome\downloads" -Force
explorer $codexHome
```

如果家里 IDEA 版本不是 `IntelliJIdea2026.1`，把目录名换成实际版本。

常见位置类似：

```text
C:\Users\<用户名>\AppData\Local\JetBrains\IntelliJIdea2026.1\aia\codex
```

可以在 PowerShell 里列出 JetBrains 缓存目录辅助确认：

```powershell
dir "$env:LOCALAPPDATA\JetBrains"
```

## 3. 离线安装 Codex 可执行文件

假设下载好的 zip 在：

```text
D:\Downloads\codex-windows.zip
```

执行：

```powershell
$codexHome="$env:LOCALAPPDATA\JetBrains\IntelliJIdea2026.1\aia\codex"
mkdir "$codexHome\bin" -Force
mkdir "$codexHome\downloads" -Force

Expand-Archive "D:\Downloads\codex-windows.zip" "$codexHome\downloads\codex" -Force
Copy-Item "$codexHome\downloads\codex\*.exe" "$codexHome\bin\" -Force
```

如果 `.exe` 不在 zip 的第一层，而是在子目录里，使用递归复制：

```powershell
Get-ChildItem "$codexHome\downloads\codex" -Recurse -Filter "*.exe" |
  Copy-Item -Destination "$codexHome\bin" -Force
```

确认结果：

```powershell
dir "$codexHome\bin"
```

你应该能看到类似：

```text
codex-x86_64-pc-windows-msvc.exe
codex-acp-x86_64-pc-windows-msvc.exe
```

## 4. 清理 IDEA 的旧 Auth 缓存

先完全退出 IDEA。

然后执行：

```powershell
$codexHome="$env:LOCALAPPDATA\JetBrains\IntelliJIdea2026.1\aia\codex"
Rename-Item "$codexHome\auth.json" "auth.json.bak" -ErrorAction SilentlyContinue
```

这样 IDEA 下次启动 Codex 时会重新登录。

如果 Windows 机器上已经有可用的 Codex CLI 登录态，可以从默认 Codex 目录复制：

```powershell
Copy-Item "$env:USERPROFILE\.codex\auth.json" "$codexHome\auth.json" -Force
```

如果不确定 `~\.codex\auth.json` 是否可用，不要复制，直接删除 IDEA 的 `auth.json` 后重新登录更稳。

## 5. 验证 Auth 状态

不要直接打开 `auth.json` 复制里面的内容，里面有 token。

如果装了 PowerShell 7，可以用：

```powershell
Get-Content "$codexHome\auth.json" | ConvertFrom-Json |
  Select-Object auth_mode,last_refresh
```

也可以只看文件时间：

```powershell
dir "$codexHome\auth.json"
```

判断标准：

- `last_refresh` 是最近登录时间，说明不是旧缓存。
- 如果重新登录后还是旧时间，说明 IDEA 没写入这个目录，需要重新确认 `$codexHome` 路径。

## 6. 重启 IDEA 并验证

重新打开 IDEA，进入 AI Assistant，选择 Codex。

观察结果：

- 如果不再报下载超时，说明离线二进制放置成功。
- 如果要求登录，正常走 ChatGPT 登录流程。
- 如果 Codex 面板能打开，但发送任务时报限制，说明安装和登录已正常。

## 常见错误判断

### 仍然报 download timeout

可能原因：

- `.exe` 没有放到 `$codexHome\bin`。
- 下载的是源码 zip，不是 Windows release asset。
- IDEA 版本目录写错，例如实际是 `IntelliJIdea2025.3`，但你放到了 `IntelliJIdea2026.1`。

处理：

```powershell
dir "$env:LOCALAPPDATA\JetBrains"
dir "$codexHome\bin"
```

确认 IDEA 实际缓存目录和 `bin` 下的 `.exe`。

### 出现 deactivated_workspace 或 402 Payment Required

说明登录态对应的 ChatGPT workspace 不可用。

处理：

```powershell
$codexHome="$env:LOCALAPPDATA\JetBrains\IntelliJIdea2026.1\aia\codex"
Rename-Item "$codexHome\auth.json" "auth.json.bad" -ErrorAction SilentlyContinue
```

然后重新打开 IDEA，重新登录一个可用的 ChatGPT workspace。

### 出现 usage limit

说明安装和登录已经成功，但账号额度到了。

处理：

- 等待额度恢复。
- 或联系 workspace 管理员增加额度。

### 找不到 auth.json

这通常不是问题。

说明 IDEA 还没完成登录，或还没有成功启动过 Codex。先修好 `bin`，再打开 IDEA 重新登录。

## 推荐回家执行顺序

1. 完全退出 IDEA。
2. 找到实际 IDEA 缓存目录：`%LOCALAPPDATA%\JetBrains\<IDEA版本>\aia\codex`。
3. 创建 `bin` 和 `downloads`。
4. 解压 GitHub Releases 的 Windows asset。
5. 把所有 `.exe` 放进 `bin`。
6. 删除或重命名 `$codexHome\auth.json`。
7. 重新打开 IDEA，选择 Codex，重新登录。
8. 如果还失败，看错误是 `download timeout`、`deactivated_workspace` 还是 `usage limit`，按上面的分支处理。

## 可直接复制的完整 PowerShell 模板

按实际情况修改 `$ideaVersion` 和 `$zipPath`：

```powershell
$ideaVersion="IntelliJIdea2026.1"
$zipPath="D:\Downloads\codex-windows.zip"

$codexHome="$env:LOCALAPPDATA\JetBrains\$ideaVersion\aia\codex"

mkdir "$codexHome\bin" -Force
mkdir "$codexHome\downloads" -Force

Expand-Archive "$zipPath" "$codexHome\downloads\codex" -Force

Get-ChildItem "$codexHome\downloads\codex" -Recurse -Filter "*.exe" |
  Copy-Item -Destination "$codexHome\bin" -Force

Rename-Item "$codexHome\auth.json" "auth.json.bak" -ErrorAction SilentlyContinue

dir "$codexHome\bin"
explorer $codexHome
```

执行完后重新打开 IDEA。
