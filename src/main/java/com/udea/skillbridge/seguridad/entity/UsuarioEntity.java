package com.udea.skillbridge.seguridad.entity;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.udea.skillbridge.seguridad.enums.AuthProvider;
import com.udea.skillbridge.seguridad.enums.TipoIdentificacion;
import com.udea.skillbridge.seguridad.enums.TipoRol;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "usuarios", uniqueConstraints = { @UniqueConstraint(name = "uk_usuarios_email", columnNames = "email"),
		@UniqueConstraint(name = "uk_id_usuarios_google", columnNames = "id_google"),
		@UniqueConstraint(name = "uk_usuarios_numero_identificacion", columnNames = "numero_identificacion") })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioEntity implements UserDetails {

	private static final long serialVersionUID = 1L;

	/**
	 * Clave interna autoincremental (clave subrogada). NO es el documento del
	 * usuario; sirve para las relaciones (FKs), el JWT, etc. El documento se
	 * guarda aparte en {@link #numeroIdentificacion}.
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// ── Documento de identificación ─────────────────────────────────

	/**
	 * Tipo de documento (CC, TI, CE, PA). Null en usuarios creados vía Google
	 * que aún no lo han registrado.
	 */
	@Enumerated(EnumType.STRING)
	@Column(name = "tipo_identificacion", length = 5)
	private TipoIdentificacion tipoIdentificacion;

	/**
	 * Número de identificación del usuario. ÚNICO: no pueden existir dos usuarios
	 * con el mismo documento. Es String para soportar pasaportes/cédulas de
	 * extranjería alfanuméricos.
	 */
	@Column(name = "numero_identificacion", length = 30)
	private String numeroIdentificacion;

	// ── Identidad ───────────────────────────────────────────────────

	@NotBlank
	@Size(max = 100)
	@Column(nullable = false, length = 100)
	private String nombre;

	@NotBlank
	@Size(max = 100)
	@Column(nullable = false, length = 100)
	private String apellido;

	@Email
	@NotBlank
	@Column(nullable = false, length = 200)
	private String email;

	/**
	 * Hash de la contraseña. Null si el usuario se registró con Google.
	 */
	@Column(name = "password_hash")
	private String passwordHash;

	// ── OAuth2 ──────────────────────────────────────────────────────

	@Enumerated(EnumType.STRING)
	@Column(name = "auth_provider", nullable = false, length = 20)
	@Builder.Default
	private AuthProvider authProvider = AuthProvider.LOCAL;

	/**
	 * ID único de Google. Null para usuarios LOCAL.
	 */
	@Column(name = "id_google", length = 200)
	private String idGoogle;

	@Column(name = "avatar_url", length = 500)
	private String avatarUrl;

	// ── Estado ──────────────────────────────────────────────────────

	@Builder.Default
	@Column(nullable = false)
	private Boolean activado = true;

	@Builder.Default
	@Column(name = "email_verificado", nullable = false)
	private Boolean emailVerificado = false;

	// ── Trazabilidad ────────────────────────────────────────────────

	@CreationTimestamp
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@Column(name = "ultimo_login_at")
	private LocalDateTime ultimoLoginAt;
	
	/**
	 * true  → el usuario completó los campos mínimos del perfil.
	 * false → el frontend debe redirigir a /perfil-completo.
	 *
	 * Se calcula automáticamente al actualizar el perfil.
	 */
	@Builder.Default
	@Column(name = "profile_completed", nullable = false)
	private Boolean perfilCompleto = false;
	
	/**
	 * Perfil extendido del usuario.
	 * Se crea vacío en el registro y se completa en /usuarios/me/perfil.
	 */
	@OneToOne(
	    mappedBy = "usuarioEnt",
	    cascade = CascadeType.ALL,
	    orphanRemoval = true,
	    fetch = FetchType.LAZY
	)
	private UsuarioPerfilEntity perfil;

	// ── Roles ───────────────────────────────────────────────────────

	@ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "usuario_roles",
        joinColumns        = @JoinColumn(name = "id_usuario"),
        inverseJoinColumns = @JoinColumn(name = "id_rol")
    )
    @Builder.Default
    private Set<RolEntity> roles = new HashSet<>();

	// ── UserDetails — Spring Security ───────────────────────────────

	/**
     * Devuelve roles + permisos como GrantedAuthority.
     * Spring Security usa esto en cada decisión de autorización.
     *
     * Formato:
     *   Roles      → "ROLE_ADMIN", "ROLE_TEACHER"
     *   Permisos   → "QUESTIONNAIRE_CREATE", "ASSESSMENT_START"
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = new HashSet<>();

        // Agregar roles
        roles.forEach(rol ->
            authorities.add(new SimpleGrantedAuthority(rol.getNombre().name()))
        );

        // Agregar permisos individuales
        roles.forEach(rol ->
            rol.getPermisos().forEach(permission ->
                authorities.add(new SimpleGrantedAuthority(
                    permission.getNombre().name()
                ))
            )
        );

        return authorities;
    }

    @Override public String getPassword()   { return passwordHash; }
    @Override public String getUsername()   { return email; }
    @Override public boolean isEnabled()    { return Boolean.TRUE.equals(activado); }
    @Override public boolean isAccountNonExpired()     { return true; }
    @Override public boolean isAccountNonLocked()      { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }

    // ── Helper ──────────────────────────────────────────────────────

    public String getFullName() {
        return nombre + " " + apellido;
    }

    public boolean hasRole(TipoRol tipoRol) {
        return roles.stream()
                .anyMatch(r -> tipoRol.equals(r.getNombre()));
    }
}