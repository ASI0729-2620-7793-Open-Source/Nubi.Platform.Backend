# API — Bounded Context Perfil y Personalización

Contrato REST del backend Nubi para el módulo de perfil (US-01 a US-06) y
suscripción (US-46 a US-48).

## Convenciones

| Aspecto | Detalle |
| --- | --- |
| Base | `http://localhost:8080/api/v1` |
| Identidad | Cabecera **`X-Account-Id`** con el id de la cuenta. La autenticación está fuera de este BC; hasta que el BC de Autenticación se integre, esa cabecera sustituye al token. |
| Errores | `application/problem+json` con `title`, `detail`, `code`, y `field` / `errors` cuando el fallo es de un campo concreto. |
| Códigos | `201` creado · `200` ok · `400` dato inválido (con el campo) · `404` no existe o no es accesible · `409` límite de plan, duplicado o transición inválida |

### Significado de `code`

| `code` | HTTP | Cuándo |
| --- | --- | --- |
| `request.validation` | 400 | Falta un campo obligatorio o no cumple `@NotBlank` / `@NotNull` / `@Min` |
| `request.malformed` | 400 | El cuerpo no es JSON válido o un valor no se puede convertir |
| `profile.notFound` | 404 | El perfil no existe |
| `profile.notAccessible` | 404 | Existe pero la cuenta no es cuidador activo (no se revela su existencia) |
| `diagnosis.notFound` | 404 | El perfil no tiene diagnóstico declarado |
| `subscription.profile.limit` | 409 | Se superó el cupo de perfiles del plan |
| `subscription.caregiver.limit` | 409 | Se superó el cupo de cuidadores por perfil |
| `caregiver.email.duplicated` | 409 | Ese correo ya está vinculado al perfil |
| `caregiver.principal.notRevocable` | 409 | Se intentó revocar al cuidador principal |
| `caregiver.invitation.notPending` | 409 | Se intentó aceptar una invitación que ya no está pendiente |
| `contact.priorityOrder.duplicated` | 409 | Dos contactos con la misma prioridad |

## Endpoints

### Perfil

| Método | Ruta | Caso |
| --- | --- | --- |
| `POST` | `/profiles` | US-01 crear perfil → `201` |
| `GET` | `/profiles` | US-01 listar los míos |
| `GET` | `/profiles/accessible` | listar los que cuido (incluye los ajenos) |
| `GET` | `/profiles/{id}` | detalle completo (incluye hijos) |
| `PUT` | `/profiles/{id}` | US-02 editar ficha general |
| `DELETE` | `/profiles/{id}` | archivar (borrado lógico) |
| `GET` | `/profiles/{id}/sensory-profile` | US-03 leer sensibilidades |
| `PUT` | `/profiles/{id}/sensory-profile` | US-03 reemplazar el bloque completo |
| `GET` | `/profiles/{id}/diagnosis` | US-04 leer diagnóstico declarado |
| `PUT` | `/profiles/{id}/diagnosis` | US-04 registrar / reemplazar |
| `PUT` | `/profiles/{id}/photo` | US-05 subir foto |

### Cuidadores (US-06)

| Método | Ruta | Caso |
| --- | --- | --- |
| `GET` | `/profiles/{id}/caregivers` | listar |
| `POST` | `/profiles/{id}/caregivers` | invitar → `201`, estado `PENDING` |
| `PATCH` | `/profiles/{id}/caregivers/{caregiverId}` | el invitado acepta con su cuenta: `{"status": "ACCEPTED"}` |
| `DELETE` | `/profiles/{id}/caregivers/{caregiverId}` | revocar acceso → `REVOKED` |

### Contactos de confianza

| Método | Ruta |
| --- | --- |
| `GET` | `/profiles/{id}/trusted-contacts` |
| `POST` | `/profiles/{id}/trusted-contacts` → `201` |
| `PUT` | `/profiles/{id}/trusted-contacts/order` |
| `DELETE` | `/profiles/{id}/trusted-contacts/{contactId}` |

### Suscripción (US-46 a US-48)

| Método | Ruta | Caso |
| --- | --- | --- |
| `GET` | `/subscriptions/current` | crea implícitamente la FREEMIUM si falta |
| `PUT` | `/subscriptions/current/plan` | cambiar de plan |

### Límites por plan

| Plan | Perfiles | Cuidadores por perfil |
| --- | --- | --- |
| `FREEMIUM` | 3 | 2 (incluye al principal) |
| `FAMILY_PREMIUM` | 10 | 5 |
| `INSTITUTIONAL` | 200 | 50 |

## Ejemplos (PowerShell)

```powershell
$base = "http://localhost:8080/api/v1"
$hdr  = @{ "X-Account-Id" = "100"; "Content-Type" = "application/json" }

# US-01 — crear un perfil
$crear = @{
  firstName = "Lucía"; lastName = "Fernández"; nickname = "Luchi"
  age = 8; gender = "FEMALE"; communicativeNeed = "PICTOGRAMS"
  primaryCaregiverEmail = "ana@nubi.app"
} | ConvertTo-Json
$perfil = Invoke-RestMethod -Method Post -Uri "$base/profiles" -Headers $hdr -Body $crear
$id = $perfil.id

# US-03 — sensibilidades sensoriales
Invoke-RestMethod -Method Put -Uri "$base/profiles/$id/sensory-profile" -Headers $hdr -Body @{
  auditory = "VERY_HIGH"; visual = "LOW"; tactile = "HIGH"
  lowStimulationEnabled = $true; prioritizeVisuals = $true; confirmAudio = $false
} | ConvertTo-Json -Depth 5

# US-04 — diagnóstico declarado (OTHER exige customDescription -> 400 sin ella)
Invoke-RestMethod -Method Put -Uri "$base/profiles/$id/diagnosis" -Headers $hdr -Body @{
  condition = "ASD"; diagnosedBy = "Dra. Gómez"
  diagnosisDate = "2024-05-01"; professionalNotes = "Seguimiento anual"
} | ConvertTo-Json -Depth 5

# US-06 — invitar y revocar un cuidador
$inv = Invoke-RestMethod -Method Post -Uri "$base/profiles/$id/caregivers" -Headers $hdr `
  -Body @{ email = "pedro@nubi.app"; role = "THERAPIST" } | ConvertTo-Json
Invoke-RestMethod -Method Delete -Uri "$base/profiles/$id/caregivers/$($inv.id)" -Headers $hdr

# Contactos de confianza
Invoke-RestMethod -Method Post -Uri "$base/profiles/$id/trusted-contacts" -Headers $hdr -Body @{
  fullName = "Mamá"; phone = "+51 999 000 111"; email = "mama@nubi.app"
  relationship = "Madre"; priorityOrder = 1
} | ConvertTo-Json -Depth 5

# US-46/48 — suscripción y cambio de plan
Invoke-RestMethod -Method Get -Uri "$base/subscriptions/current" -Headers $hdr | ConvertTo-Json
Invoke-RestMethod -Method Put -Uri "$base/subscriptions/current/plan" -Headers $hdr `
  -Body @{ plan = "FAMILY_PREMIUM" } | ConvertTo-Json
```

### Respuesta de error esperada (TS-01)

```http
HTTP/1.1 409 Conflict
Content-Type: application/problem+json

{
  "type": "about:blank",
  "title": "Conflicto con el estado actual del recurso",
  "status": 409,
  "detail": "El plan FREEMIUM permite hasta 3 perfiles.",
  "code": "subscription.profile.limit"
}
```

## Cambios: un solo niño con autismo

- Cada cuenta acompaña a **un único niño**. Crear un segundo perfil activo responde `409` con
  `code = profile.single.child` (archivar el perfil libera el cupo). Todos los planes tienen `maxProfiles = 1`;
  los planes solo cambian el cupo de cuidadores (2 / 5 / 50).
- Nubi es solo para niños con **autismo (TEA)**: todo perfil nace con el diagnóstico `ASD` y
  `PUT /profiles/{id}/diagnosis` ya no exige `condition`. Otra condición responde `400` con
  `code = condition.autism.only`.
- `ProfilesContextFacade` (`profile/interfaces/acl`) expone el perfil sensorial a los demás contextos:
  Autorregulación filtra los estímulos y Gestión de Crisis ordena la guía SOS con datos reales.

## Nuevos Bounded Contexts

### IAM — `/api/v1/authentication`

| Método | Ruta | Detalle |
| --- | --- | --- |
| `POST` | `/sign-up` | `{ email, fullName, password }` → `201 { id, email, fullName }` (contraseña con hash PBKDF2) |
| `POST` | `/sign-in` | `{ email, password }` → `200 { id, email, fullName }` |
| `GET` | `/me` | Con cabecera `X-Account-Id` |

El `id` devuelto es el que el frontend envía en `X-Account-Id`. Al arrancar se crea la cuenta demo
`demo@nubi.app` / `nubi123` (desactivable con `NUBI_DEMO_ENABLED=false`).

### Rutina Visual — `/api/v1/profiles/{profileId}/routine-steps`

| Método | Ruta | Detalle |
| --- | --- | --- |
| `GET` | `` | Pasos del día; si no hay, crea la rutina sugerida (10 pasos) |
| `POST` | `` | `{ title, icon, scheduledTime: "HH:mm" }` → `201` (máximo 20 pasos) |
| `PUT` | `/{stepId}` | Editar |
| `PUT` | `/order` | `{ order: [ids] }` |
| `DELETE` | `/{stepId}` | `204` |
| `POST` / `DELETE` | `/{stepId}/completion` | Marcar / desmarcar como hecho hoy (`completedToday`) |

## Integraciones nuevas

- Al terminar una sesión SOS (`EpisodeFinishedEvent`), Red de Apoyo registra el episodio automáticamente y
  publica `EpisodeRegisteredEvent` para que Gestión de Crisis actualice sus recomendaciones.
- Catálogo de calma: se suman `BALLOON_BREATHING` (respiración guiada) y `CALM_DRAWING` (dibujo).
- CORS acepta `PATCH` (usado por las selecciones de pictogramas).

## Convención REST: sustantivos en la URL, el verbo es el método HTTP

Las rutas nombran recursos; la acción la da el método (`GET`, `POST`, `PUT`, `PATCH`, `DELETE`).
Un cambio de estado es un `PATCH` sobre el recurso con el nuevo estado en el cuerpo, y un filtro va en
query params. Rutas que cambiaron:

| Antes | Ahora | Cuerpo |
| --- | --- | --- |
| `POST /sos-sessions/{id}/steps/{stepId}/complete` | `PATCH /sos-sessions/{id}/steps/{stepId}` | `{"status": "COMPLETED"}` |
| `POST /sos-sessions/{id}/steps/{stepId}/skip` | `PATCH /sos-sessions/{id}/steps/{stepId}` | `{"status": "SKIPPED", "confirmed": true}` |
| `POST /sos-sessions/{id}/finish` | `PATCH /sos-sessions/{id}` | `{"status": "FINISHED", "observedState": "CALM", "finalIntensity": "CALM"}` |
| `POST /sos-sessions/{id}/steps/{stepId}/alternative` | `POST /sos-sessions/{id}/steps/{stepId}/alternatives` (201) | — |
| `PATCH .../selections/{id}/delivered`, `/understood`, `/not-understood` | `PATCH /profiles/{id}/communication-board/selections/{selectionId}` | `{"status": "DELIVERED"}`, `"UNDERSTOOD"` o `"NOT_UNDERSTOOD"` |
| `POST /profiles/{id}/recommendations/generate` | `POST /profiles/{id}/recommendations` | — |
| `POST /profiles/{id}/caregivers/{caregiverId}/accept` | `PATCH /profiles/{id}/caregivers/{caregiverId}` | `{"status": "ACCEPTED"}` |
| `PUT /episode-reports/{id}/share` | `POST /episode-reports/{id}/shares` | `{"sharedWith": "Dra. Paredes"}` |
| `GET /episode-records/filter?...` | `GET /episode-records?profileId=&startDate=&endDate=` | — |
| `PUT /help-requests/{id}/confirmation`, `/cancellation` | `PATCH /help-requests/{id}` | `{"status": "CONFIRMED"}` o `"CANCELLED"` |
| `PUT /support-network/recommendations/{id}/effective` | `PATCH /support-network/recommendations/{id}` | `{"effective": true}` |
| `POST /calm-sessions/{id}/pause`, `/resume`, `/finish` | `PATCH /calm-sessions/{id}` | `{"status": "PAUSED"}`, `"ACTIVE"` o `"FINISHED"` |
| `POST /calm-sessions/{id}/timer/pause`, `/timer/resume` | `PATCH /calm-sessions/{id}/timer` | `{"running": false}` o `true` |

Se mantienen `POST /authentication/sign-up` y `/sign-in` (convención habitual de autenticación) y las
consultas con adjetivos (`/latest`, `/recent`, `/active`, `/in-progress`, `/quick-access`), que son lecturas,
no acciones.
