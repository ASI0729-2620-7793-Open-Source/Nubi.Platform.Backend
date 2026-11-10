# Smoke test manual del Bounded Context Perfil y Personalizacion.
# Requiere el backend corriendo en http://localhost:8080
#
#   powershell -ExecutionPolicy Bypass -File .\scripts\smoke-profile.ps1
#
# El script es idempotente: al inicio archiva los perfiles de la cuenta de
# prueba para que los cupos del plan arranquen en cero.

$base  = "http://localhost:8080/api/v1"
$acct  = "9001"
$fails = 0

function Api([string]$method, [string]$uri, $body) {
    $out  = Join-Path $env:TEMP ("nubi-smoke-" + [guid]::NewGuid().ToString("N") + ".out")
    $args = @("-s", "-S", "-o", $out, "-w", "%{http_code}",
              "-X", $method, "-H", "X-Account-Id: $acct")

    $tmpBody = $null
    if ($null -ne $body) {
        $json = $body | ConvertTo-Json -Depth 6
        $tmpBody = Join-Path $env:TEMP ("nubi-smoke-" + [guid]::NewGuid().ToString("N") + ".json")
        # UTF-8 sin BOM: el parser JSON del backend lo rechazaria con BOM.
        [System.IO.File]::WriteAllText($tmpBody, $json, (New-Object System.Text.UTF8Encoding($false)))
        $args += @("-H", "Content-Type: application/json", "-d", "@$tmpBody")
    }
    $args += $uri

    $status = 0
    try { $status = [int](& curl.exe @args) } catch { $status = 0 }

    $parsed = $null
    if (Test-Path $out) {
        $raw = Get-Content $out -Raw -Encoding UTF8
        if ($raw) { try { $parsed = $raw | ConvertFrom-Json } catch { $parsed = $null } }
        Remove-Item $out -Force -ErrorAction SilentlyContinue
    }
    if ($tmpBody) { Remove-Item $tmpBody -Force -ErrorAction SilentlyContinue }

    return @{ ok = ($status -ge 200 -and $status -lt 300); status = $status; body = $parsed }
}

function Check([string]$name, $actual, $expected) {
    if ("$actual" -eq "$expected") {
        Write-Host ("  OK   " + $name + " = " + $actual) -ForegroundColor Green
    } else {
        Write-Host ("  FAIL " + $name + " : esperaba '" + $expected + "', llego '" + $actual + "'") -ForegroundColor Red
        $script:fails++
    }
}

# --- limpieza ------------------------------------------------------------
$existantes = Api "GET" "$base/profiles"
if ($existantes.ok -and $existantes.body) {
    foreach ($p in $existantes.body) {
        Api "DELETE" ("$base/profiles/" + $p.id) | Out-Null
    }
}
# Vuelve al plan gratuito para que los cupos arranquen en cero.
Api "PUT" "$base/subscriptions/current/plan" @{ plan = "FREEMIUM" } | Out-Null
Write-Host ""
Write-Host "== US-01 crear perfil ==" -ForegroundColor Cyan

$r = Api "POST" "$base/profiles" @{
  firstName = "Lucia"; lastName = "Fernandez"; nickname = "Luchi"
  age = 8; gender = "FEMALE"; communicativeNeed = "PICTOGRAMS"
  primaryCaregiverEmail = "ana@nubi.app"
}
Check "POST /profiles status" $r.status 201
$perfil = $r.body
$id = $perfil.id
Check "perfil.active" $perfil.active $true
Check "perfil.displayName" $perfil.displayName "Luchi"
Check "sensoryProfile.auditory" $perfil.sensoryProfile.auditory "MEDIUM"
Check "caregivers[0].role" $perfil.caregivers[0].role "PRIMARY"

Write-Host ""
Write-Host "== US-02 editar ficha general ==" -ForegroundColor Cyan
$r = Api "PUT" "$base/profiles/$id" @{
  firstName = "Lucia"; lastName = "Fernandez"; nickname = "Luchi"
  age = 9; gender = "FEMALE"; communicativeNeed = "VOICE"
}
Check "PUT /profiles status" $r.status 200
Check "profile.age" $r.body.age 9
Check "profile.communicativeNeed" $r.body.communicativeNeed "VOICE"

Write-Host ""
Write-Host "== US-03 sensibilidades sensoriales ==" -ForegroundColor Cyan
$r = Api "PUT" "$base/profiles/$id/sensory-profile" @{
  auditory = "VERY_HIGH"; visual = "LOW"; tactile = "HIGH"
  lowStimulationEnabled = $true; prioritizeVisuals = $true; confirmAudio = $false
}
Check "sensory status" $r.status 200
Check "sensory.auditory" $r.body.auditory "VERY_HIGH"
Check "sensory.lowStimulationEnabled" $r.body.lowStimulationEnabled $true

Write-Host ""
Write-Host "== US-04 diagnostico declarado ==" -ForegroundColor Cyan
$r = Api "PUT" "$base/profiles/$id/diagnosis" @{
  condition = "ASD"; diagnosedBy = "Dra. Gomez"
  diagnosisDate = "2024-05-01"; professionalNotes = "Seguimiento anual"
}
Check "diagnosis status" $r.status 200
Check "diagnosis.condition" $r.body.condition "ASD"

$r = Api "PUT" "$base/profiles/$id/diagnosis" @{ condition = "OTHER" }
Check "OTHER sin descripcion status" $r.status 400
Check "OTHER sin descripcion code" $r.body.code "condition.other.description.required"
Check "OTHER sin descripcion field" $r.body.field "customDescription"

$r = Api "GET" "$base/profiles/$id/diagnosis"
Check "GET diagnosis sin datos" $r.status 200
$r = Api "GET" "$base/profiles/999999/diagnosis"
Check "GET diagnosis ajeno 404" $r.status 404

Write-Host ""
Write-Host "== US-05 foto ==" -ForegroundColor Cyan
$r = Api "PUT" "$base/profiles/$id/photo" @{ photoUrl = "/uploads/lucia.png" }
Check "photo status" $r.status 200
Check "profile.photoUrl" $r.body.photoUrl "/uploads/lucia.png"

Write-Host ""
Write-Host "== US-06 cuidadores asociados ==" -ForegroundColor Cyan
$r = Api "POST" "$base/profiles/$id/caregivers" @{
  email = "pedro@nubi.app"; role = "THERAPIST"
}
Check "invitar status" $r.status 201
Check "invitacion status" $r.body.status "PENDING"
$caregiverId = $r.body.id

$r = Api "POST" "$base/profiles/$id/caregivers" @{
  email = "Pedro@nubi.app"; role = "CAREGIVER"
}
Check "correo duplicado status" $r.status 409
Check "correo duplicado code" $r.body.code "caregiver.email.duplicated"

# Con el principal y pedro ya vinculados el cupo de FREEMIUM (2) esta lleno.
$r = Api "POST" "$base/profiles/$id/caregivers" @{
  email = "maria@nubi.app"; role = "THERAPIST"
}
Check "cuidador extra status" $r.status 409
Check "cuidador extra code" $r.body.code "subscription.caregiver.limit"

$aceptarOk = $false
$out = Join-Path $env:TEMP "nubi-accept.out"
$code = & curl.exe -s -S -o $out -w "%{http_code}" -X POST `
        -H "X-Account-Id: 9002" "$base/profiles/$id/caregivers/$caregiverId/accept"
$aceptarBody = $null
if (Test-Path $out) {
    $raw = Get-Content $out -Raw -Encoding UTF8
    if ($raw) { try { $aceptarBody = $raw | ConvertFrom-Json } catch {} }
    Remove-Item $out -Force -ErrorAction SilentlyContinue
}
Check "aceptar invitacion status" ([int]$code) 200
Check "aceptar invitacion estado" $aceptarBody.status "ACTIVE"
Check "aceptar invitacion accountId" $aceptarBody.accountId 9002

$principal = ($perfil.caregivers | Where-Object { $_.role -eq "PRIMARY" }).id
$r = Api "DELETE" "$base/profiles/$id/caregivers/$principal"
Check "revocar principal status" $r.status 409
Check "revocar principal code" $r.body.code "caregiver.principal.notRevocable"

$r = Api "DELETE" "$base/profiles/$id/caregivers/$caregiverId"
Check "revocar invitado status" $r.status 200
Check "revocar invitado status" $r.body.status "REVOKED"

Write-Host ""
Write-Host "== Contactos de confianza ==" -ForegroundColor Cyan
$r1 = Api "POST" "$base/profiles/$id/trusted-contacts" @{
  fullName = "Mama"; phone = "+51 999 000 111"; email = "mama@nubi.app"
  relationship = "Madre"; priorityOrder = 1
}
Check "contacto 1 status" $r1.status 201
$r2 = Api "POST" "$base/profiles/$id/trusted-contacts" @{
  fullName = "Papa"; phone = "+51 999 000 222"; relationship = "Padre"
  priorityOrder = 2
}
Check "contacto 2 status" $r2.status 201

$r = Api "POST" "$base/profiles/$id/trusted-contacts" @{
  fullName = "Abuela"; phone = "+51 999 000 333"; priorityOrder = 1
}
Check "prioridad duplicada status" $r.status 409
Check "prioridad duplicada code" $r.body.code "contact.priorityOrder.duplicated"

$r = Api "PUT" "$base/profiles/$id/trusted-contacts/order" @{
  order = @($r2.body.id, $r1.body.id)
}
Check "reordenar status" $r.status 200
Check "reordenar [0]" $r.body[0].fullName "Papa"
Check "reordenar [0].priorityOrder" $r.body[0].priorityOrder 1

$r = Api "DELETE" "$base/profiles/$id/trusted-contacts/$($r2.body.id)"
Check "desvincular status" $r.status 200
Check "desvincular queda 1" $r.body.Count 1
Check "desvincular renumera" $r.body[0].priorityOrder 1

Write-Host ""
Write-Host "== US-46/48 suscripcion y limites ==" -ForegroundColor Cyan
$r = Api "GET" "$base/subscriptions/current"
Check "subscription plan" $r.body.plan "FREEMIUM"
Check "subscription maxProfiles" $r.body.maxProfiles 3
Check "subscription maxCaregivers" $r.body.maxCaregiversPerProfile 2

$r = Api "POST" "$base/profiles" @{
  firstName = "Mateo"; lastName = "Fernandez"; nickname = "Mati"
  age = 5; gender = "MALE"; communicativeNeed = "PICTOGRAMS"
}
Check "perfil 2 status" $r.status 201
$r = Api "POST" "$base/profiles" @{
  firstName = "Sofia"; lastName = "Fernandez"; nickname = "Sofi"
  age = 3; gender = "FEMALE"; communicativeNeed = "PICTOGRAMS"
}
Check "perfil 3 status" $r.status 201

$r = Api "POST" "$base/profiles" @{
  firstName = "Leo"; lastName = "Fernandez"; nickname = "Leo"
  age = 2; gender = "MALE"; communicativeNeed = "PICTOGRAMS"
}
Check "perfil 4 status" $r.status 409
Check "perfil 4 code" $r.body.code "subscription.profile.limit"
Check "perfil 4 detail" ($r.body.detail -like "*3 perfiles*") $true

$r = Api "PUT" "$base/subscriptions/current/plan" @{ plan = "FAMILY_PREMIUM" }
Check "cambiar plan status" $r.status 200
Check "cambiar plan" $r.body.plan "FAMILY_PREMIUM"
Check "nuevos maxProfiles" $r.body.maxProfiles 10

$r = Api "POST" "$base/profiles" @{
  firstName = "Leo"; lastName = "Fernandez"; nickname = "Leo"
  age = 2; gender = "MALE"; communicativeNeed = "PICTOGRAMS"
}
Check "perfil 4 tras subir de plan" $r.status 201

$r = Api "PUT" "$base/subscriptions/current/plan" @{ plan = "GOLD" }
Check "plan invalido status" $r.status 400
Check "plan invalido detail" ($r.body.detail -like "*GOLD*") $true

$r = Api "POST" "$base/profiles" @{ lastName = "Fernandez"; age = 4 }
Check "datos invalidos status" $r.status 400
Check "datos invalidos code" $r.body.code "request.validation"
Check "datos invalidos field" $r.body.field "firstName"

$r = Api "GET" "$base/profiles/999999"
Check "perfil inexistente" $r.status 404

$r = Api "GET" "$base/profiles/$id" 
Check "lectura final status" $r.status 200

$r = Api "DELETE" "$base/profiles/$id"
Check "archivar status" $r.status 200
Check "archivar active" $r.body.active $false

Write-Host ""
if ($fails -eq 0) {
    Write-Host "TODAS LAS COMPROBACIONES PASARON" -ForegroundColor Green
    exit 0
} else {
    Write-Host ("COMPROBACIONES FALLIDAS: " + $fails) -ForegroundColor Red
    exit 1
}
