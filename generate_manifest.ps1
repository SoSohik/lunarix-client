# Скрипт автоматичного перерахунку хешів для manifest.json
$baseDir = $PSScriptRoot
$manifestPath = "$baseDir\manifest.json"

$filesToCheck = @(
    "mods\Lunarix-Client-v1.2.1.jar",
    "mods\fabric-api-0.141.6-1.21.11.jar",
    "mods\sodium-fabric-0.8.15-beta.1+mc1.21.11.jar",
    "versions\Lunarix Client 1.21.11\Lunarix Client 1.21.11.json",
    "versions\Lunarix Client 1.21.11\Lunarix Client 1.21.11.jar"
)

if (Test-Path $manifestPath) {
    $manifest = Get-Content $manifestPath -Raw | ConvertFrom-Json
} else {
    $manifest = [PSCustomObject]@{
        client_version = "1.3.5"
        minecraft_version = "1.21.11"
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
