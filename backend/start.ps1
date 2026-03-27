# Load .env file and set environment variables
Get-Content ".env" | Where-Object { $_ -notmatch '^\s*#' -and $_.Trim() -ne '' } | ForEach-Object {
    $parts = $_ -split '=', 2
    $name  = $parts[0].Trim()
    $value = $parts[1].Trim()
    [System.Environment]::SetEnvironmentVariable($name, $value, 'Process')
    Write-Host "Set $name"
}

Write-Host "Starting Spring Boot backend..."
mvn spring-boot:run
