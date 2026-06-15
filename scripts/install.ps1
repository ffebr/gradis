# Installs the latest Gradis release binary for Windows.
#
#   irm https://raw.githubusercontent.com/ffebr/gradis/main/scripts/install.ps1 | iex
#
$ErrorActionPreference = "Stop"

$repo    = "ffebr/gradis"
$asset   = "gradis-windows-x64.exe"
$dest    = Join-Path $env:LOCALAPPDATA "Gradis"
$binPath = Join-Path $dest "gradis.exe"

Write-Host "Resolving latest release of $repo..."
$release = Invoke-RestMethod -Uri "https://api.github.com/repos/$repo/releases/latest" `
    -Headers @{ "User-Agent" = "gradis-installer" }
$url = ($release.assets | Where-Object { $_.name -eq $asset } | Select-Object -First 1).browser_download_url

if (-not $url) {
    throw "Could not find asset '$asset' in the latest release."
}

if (-not (Test-Path $dest)) {
    New-Item -ItemType Directory -Path $dest -Force | Out-Null
}

Write-Host "Downloading $asset..."
Invoke-WebRequest -Uri $url -OutFile $binPath

# Add the install dir to the user PATH if it isn't already there.
$userPath = [Environment]::GetEnvironmentVariable("Path", "User")
if ($userPath -notlike "*$dest*") {
    [Environment]::SetEnvironmentVariable("Path", "$userPath;$dest", "User")
    Write-Host "Added $dest to your user PATH (restart the terminal to pick it up)."
}

Write-Host "Installed: $binPath"

# rsvg-convert is required at runtime to render the PNG.
if (-not (Get-Command rsvg-convert -ErrorAction SilentlyContinue)) {
    Write-Host ""
    Write-Host "WARNING: 'rsvg-convert' was not found. Gradis needs it to render PNGs." -ForegroundColor Yellow
    Write-Host "  Install it with: scoop install librsvg   (or: choco install rsvg-convert)"
}

Write-Host "Run 'gradis --help' to get started."
