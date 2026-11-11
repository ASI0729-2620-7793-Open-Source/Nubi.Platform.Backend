package com.acme.nubi.platform.profile.domain.model.entities;

import com.acme.nubi.platform.profile.domain.errors.DomainRuleViolation;

/**
 * Entidad. Contacto de confianza que recibe las alertas del usuario.
 * priorityOrder define el orden de escalamiento cuando un contacto no
 * confirma la recepción (menor número = se avisa primero).
 */
public class TrustedContact {

    private final Long id;
    private final String fullName;
    private final String phone;
    private final String email;
    private final String relationship;
    private final int priorityOrder;

    public TrustedContact(
            Long id, String fullName, String phone, String email, String relationship,
            int priorityOrder) {
        if (fullName == null || fullName.isBlank()) {
            throw new DomainRuleViolation(
                    "contact.fullName.required", "El nombre del contacto es obligatorio.",
                    "fullName");
        }
        if (phone == null || phone.isBlank()) {
            throw new DomainRuleViolation(
                    "contact.phone.required", "El teléfono del contacto es obligatorio.", "phone");
        }
        if (priorityOrder <= 0) {
            throw new DomainRuleViolation(
                    "contact.priorityOrder.invalid",
                    "La prioridad del contacto debe ser mayor que cero.",
                    "priorityOrder");
        }
        this.id = id;
        this.fullName = fullName.trim();
        this.phone = phone.trim();
        this.email = email == null || email.isBlank() ? null : email.trim();
        this.relationship = relationship;
        this.priorityOrder = priorityOrder;
    }

    public Long id() {
        return id;
    }

    public String fullName() {
        return fullName;
    }

    public String phone() {
        return phone;
    }

    public String email() {
        return email;
    }

    public String relationship() {
        return relationship;
    }

    public int priorityOrder() {
        return priorityOrder;
    }

    public TrustedContact withPriorityOrder(int order) {
        return new TrustedContact(id, fullName, phone, email, relationship, order);
    }

    public TrustedContact withId(Long persistedId) {
        if (this.id != null) {
            return this;
        }
        return new TrustedContact(
                persistedId, fullName, phone, email, relationship, priorityOrder);
    }

    /** Este contacto se avisa antes que otro (número menor = mayor prioridad). */
    public boolean hasHigherPriorityThan(TrustedContact other) {
        return this.priorityOrder < other.priorityOrder;
    }
}
