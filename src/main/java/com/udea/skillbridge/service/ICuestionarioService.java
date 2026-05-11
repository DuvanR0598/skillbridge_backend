package com.udea.skillbridge.service;

import java.util.List;
import com.udea.skillbridge.dto.Cuestionario;
import com.udea.skillbridge.dto.PreguntaCuestionario;

public interface ICuestionarioService {
	
	Cuestionario crearCuestionario (Cuestionario cuestionario);
	
	Cuestionario getById (Long idCuestionario);
	
	List<Cuestionario> listarAllCuestionarios();
	
	List<Cuestionario> listarCuestionariosActivos();
	
	void addPretuntaToCuestinario(Long idCuestionario, PreguntaCuestionario preguntaCuestionario);
	
	Cuestionario cuestionarioCompleto(Long idCuestionario);
	
	void borradoLogico(Long idCuestionario);

}
