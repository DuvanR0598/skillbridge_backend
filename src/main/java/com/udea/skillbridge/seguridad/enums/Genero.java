package com.udea.skillbridge.seguridad.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Genero {
	
    MASCULINO("Masculino"),
    FEMENINO("Femenino"),
    NO_BINARIO("No binario"),
    PREFIERO_NO_DECIRLO("Prefiero no decirlo");

    private final String displayName;

}
