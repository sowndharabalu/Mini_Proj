# =====================================================================
# Task Tracker REST API - Automated Endpoint Test Script (PowerShell)
# =====================================================================

$baseUrl = "http://localhost:8081/api/tasks"

Write-Host "=======================================================" -ForegroundColor Cyan
Write-Host " Task Tracker REST API - Endpoint Functional Tests" -ForegroundColor Cyan
Write-Host " Base URL: $baseUrl" -ForegroundColor Cyan
Write-Host "=======================================================" -ForegroundColor Cyan

# Function helper
function Test-Endpoint {
    param(
        [string]$Name,
        [string]$Method,
        [string]$Url,
        [object]$Body = $null,
        [int]$ExpectedStatus = 200
    )
    Write-Host "`n--> [$Method] $Name" -ForegroundColor Yellow
    Write-Host "    URL: $Url" -ForegroundColor DarkGray

    $headers = @{ "Content-Type" = "application/json"; "Accept" = "application/json" }
    try {
        if ($Body) {
            $jsonBody = $Body | ConvertTo-Json -Depth 5
            $response = Invoke-RestMethod -Uri $Url -Method $Method -Headers $headers -Body $jsonBody -ErrorAction Stop
        } else {
            $response = Invoke-RestMethod -Uri $Url -Method $Method -Headers $headers -ErrorAction Stop
        }
        Write-Host "    [PASS] Request succeeded!" -ForegroundColor Green
        Write-Host ($response | ConvertTo-Json -Depth 3) -ForegroundColor White
        return $response
    } catch {
        $ex = $_.Exception
        if ($_.Exception.Response) {
            $statusCode = [int]$_.Exception.Response.StatusCode
            if ($statusCode -eq $ExpectedStatus) {
                Write-Host "    [PASS] Expected HTTP $statusCode received!" -ForegroundColor Green
                try {
                    $stream = $_.Exception.Response.GetResponseStream()
                    $reader = New-Object System.IO.StreamReader($stream)
                    $errText = $reader.ReadToEnd()
                    Write-Host $errText -ForegroundColor DarkYellow
                } catch {}
            } else {
                Write-Host "    [FAIL] Expected $ExpectedStatus but got $statusCode" -ForegroundColor Red
            }
        } else {
            Write-Host "    [ERROR] Connection failed: $($ex.Message)" -ForegroundColor Red
        }
    }
}

Write-Host "`n1. Testing GET /stats"
Test-Endpoint -Name "Retrieve Dashboard Metrics" -Method "GET" -Url "$baseUrl/stats"

Write-Host "`n2. Testing GET /api/tasks"
Test-Endpoint -Name "Retrieve All Tasks" -Method "GET" -Url "$baseUrl"

Write-Host "`n3. Testing GET with Filter (status=IN_PROGRESS)"
Test-Endpoint -Name "Filter by Status" -Method "GET" -Url "$($baseUrl)?status=IN_PROGRESS"

Write-Host "`n4. Testing POST (Create new task)"
$newTask = @{
    title = "Automated Test Task"
    description = "Created via test-api.ps1 script"
    priority = "HIGH"
    status = "PENDING"
    dueDate = (Get-Date).AddDays(3).ToString("yyyy-MM-dd")
}
$created = Test-Endpoint -Name "Create New Task" -Method "POST" -Url "$baseUrl" -Body $newTask -ExpectedStatus 201

if ($created -and $created.data -and $created.data.id) {
    $newId = $created.data.id

    Write-Host "`n5. Testing PATCH /status on Task ID $newId"
    $patchBody = @{ status = "COMPLETED" }
    Test-Endpoint -Name "Quick Status Update" -Method "PATCH" -Url "$baseUrl/$newId/status" -Body $patchBody

    Write-Host "`n6. Testing DELETE on Task ID $newId"
    Test-Endpoint -Name "Delete Task" -Method "DELETE" -Url "$baseUrl/$newId"
}

Write-Host "`n7. Testing POST with Validation Failure (Blank Title)"
$invalidTask = @{
    title = ""
    description = "Should fail with 400"
}
Test-Endpoint -Name "Validation Failure (400)" -Method "POST" -Url "$baseUrl" -Body $invalidTask -ExpectedStatus 400

Write-Host "`n8. Testing GET 404 Not Found"
Test-Endpoint -Name "Resource Not Found (404)" -Method "GET" -Url "$baseUrl/999999" -ExpectedStatus 404

Write-Host "`n=======================================================" -ForegroundColor Cyan
Write-Host " Tests execution finished!" -ForegroundColor Cyan
Write-Host "=======================================================" -ForegroundColor Cyan
