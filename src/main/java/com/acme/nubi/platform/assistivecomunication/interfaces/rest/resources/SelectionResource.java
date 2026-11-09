package com.acme.nubi.platform.assistivecomunication.interfaces.rest.resources;

import java.time.LocalDateTime;

public record SelectionResource(Long id, Long pictogramId, String status, LocalDateTime selectedAt,
                                LocalDateTime understoodAt) {
}
