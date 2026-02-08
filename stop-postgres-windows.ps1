# Stop PostgreSQL Windows Service
# Run this script as Administrator

Write-Host "=== Stopping PostgreSQL Windows Service ===" -ForegroundColor Yellow
Write-Host ""

# Check if running as administrator
$isAdmin = ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)

if (-not $isAdmin) {
    Write-Host "ERROR: This script requires Administrator privileges!" -ForegroundColor Red
    Write-Host ""
    Write-Host "Please run PowerShell as Administrator and try again:" -ForegroundColor Yellow
    Write-Host "1. Right-click PowerShell" -ForegroundColor Gray
    Write-Host "2. Select 'Run as Administrator'" -ForegroundColor Gray
    Write-Host "3. Run: .\stop-postgres-windows.ps1" -ForegroundColor Gray
    exit 1
}

# Stop PostgreSQL service
try {
    Write-Host "Stopping PostgreSQL Windows service..." -ForegroundColor Cyan
    Stop-Service -Name "postgresql-x64-17" -Force
    Write-Host "SUCCESS: PostgreSQL Windows service stopped!" -ForegroundColor Green
    Write-Host ""
    
    # Optionally disable auto-start
    $disable = Read-Host "Do you want to disable auto-start on boot? (yes/no)"
    if ($disable -eq "yes") {
        Set-Service -Name "postgresql-x64-17" -StartupType Disabled
        Write-Host "Auto-start disabled" -ForegroundColor Green
    }
    
    Write-Host ""
    Write-Host "Now you can use pgvector container on port 5432" -ForegroundColor Cyan
    Write-Host "Restart your Spring Boot application" -ForegroundColor Yellow
    
} catch {
    Write-Host "ERROR: Failed to stop service" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    exit 1
}
