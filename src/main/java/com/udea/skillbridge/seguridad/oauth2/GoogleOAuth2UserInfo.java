package com.udea.skillbridge.seguridad.oauth2;

import java.util.Map;

/**
 * Extrae los atributos que Google envía en el token de identidad.
 */
public class GoogleOAuth2UserInfo {
	
	private final Map<String, Object> attributes;

    public GoogleOAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public String getId()          { return (String) attributes.get("sub"); }
    public String getEmail()       { return (String) attributes.get("email"); }
    public String getNombre()      { return (String) attributes.get("given_name"); }
    public String getApellido()    { return (String) attributes.get("family_name"); }
    public String getAvatarUrl()   { return (String) attributes.get("picture"); }
    public Boolean isVerificado()  { return (Boolean) attributes.getOrDefault("email_verified", false); }

}
