# Скрипт автоматичного перерахунку хешів для manifest.json
$baseDir = $PSScriptRoot
$manifestPath = "$baseDir\manifest.json"

$filesToCheck = @(
    "mods\Lunarix-Client-26.2-1.0.jar",
    "mods\fabric-api-0.161.0+26.2.jar",
    "mods\sodium-fabric-0.9.2+mc26.2.jar",
    "versions\Lunarix Client 26.2\Lunarix Client 26.2.json",
    "versions\Lunarix Client 26.2\Lunarix Client 26.2.jar"
)

if (Test-Path $manifestPath) {
    $manifest = Get-Content $manifestPath -Raw | ConvertFrom-Json
} else {
    $manifest = [PSCustomObject]@{
        client_version = "1.0.0"
        minecraft_version = "26.2"
        fabric_loader = "0.20.1"
        files = @()
    }
}

$fileList = @()
foreach ($relPath in $filesToCheck) {
    $fullPath = "$baseDir\$relPath"
    if (Test-Path $fullPath) {
        $hash = (Get-FileHash -Path $fullPath -Algorithm SHA1).Hash.ToLower()
        $size = (Get-Item $fullPath).Length
        $normalizedRel = $relPath.Replace('\', '/')
        
        # Preserve existing URL if possible
        $existing = $manifest.files | Where-Object { $_.path -eq $normalizedRel } | Select-Object -First 1
        $url = if ($existing -and $existing.url) { $existing.url } else { "https://github.com/SoSohik/lunarix-client/releases/download/v1.0.0/" + [System.IO.Path]::GetFileName($relPath) }
        
        $fileList += [PSCustomObject]@{
            path = $normalizedRel
            size = $size
            sha1 = $hash
            url = $url
        }
    }
}

$manifest.files = $fileList
$manifest | ConvertTo-Json -Depth 10 | Set-Content -Path $manifestPath -Encoding UTF8
Write-Output "manifest.json успішно оновлено! Оброблено $($fileList.Count) файлів."
