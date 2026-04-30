package com.udea.skillbridge.validation;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.udea.skillbridge.enums.TipoPregunta;
import com.udea.skillbridge.exception.CuestionarioException;
import com.udea.skillbridge.validation.impl.DescripcionValidador;
import com.udea.skillbridge.validation.impl.LikertValidador;
import com.udea.skillbridge.validation.impl.OpcionMultipleValidador;
import com.udea.skillbridge.validation.impl.OpcionUnicaValidador;
import com.udea.skillbridge.validation.impl.VerdaderoFalsoValidador;

import lombok.RequiredArgsConstructor;

/**
 * Factory que mapea TipoPregunta -> PreguntaValidador.
 *
 * Spring inyecta cada validador automáticamente porque todos
 * son @Component. El factory solo los conecta con su enum.
 *
 * ¿Por qué un factory y no un Map hardcodeado?
 * -> Porque Spring gestiona el ciclo de vida de los validadores.
 *   El factory es el único lugar donde ese mapeo existe.
 */
@Component
@RequiredArgsConstructor
public class PreguntaValidadorFactory {
	
	private final VerdaderoFalsoValidador verdaderoFalsoValidador;
    private final LikertValidador likertValidador;
    private final DescripcionValidador descripcionValidador;
    private final OpcionMultipleValidador opcionMultipleValidador;
    private final OpcionUnicaValidador opcionUnicaValidador;
    
    public PreguntaValidador getValidador(TipoPregunta tipoPregunta) {
        return switch (tipoPregunta) {
            case VERDADERO_FALSO  -> verdaderoFalsoValidador;
            case LIKERT          -> likertValidador;
            case DESCRIPCION     -> descripcionValidador;
            case OPCION_MULTIPLE -> opcionMultipleValidador;
            case OPCION_UNICA    -> opcionUnicaValidador;
            
            // El default nunca debería alcanzarse si el enum está completo,
            // pero Java exige cubrirlo.
            default -> throw new CuestionarioException(
                "Tipo de pregunta no soportado: " + tipoPregunta,
                HttpStatus.BAD_REQUEST
            );
        };
    }

}
