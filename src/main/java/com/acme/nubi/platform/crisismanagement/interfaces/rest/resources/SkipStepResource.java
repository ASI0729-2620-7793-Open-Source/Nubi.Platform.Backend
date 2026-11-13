package com.acme.nubi.platform.crisismanagement.interfaces.rest.resources;

/**
 * @param confirmed el cuidador confirma que omite el paso; es obligatorio en los pasos obligatorios
 */
public record SkipStepResource(Boolean confirmed) {
}
