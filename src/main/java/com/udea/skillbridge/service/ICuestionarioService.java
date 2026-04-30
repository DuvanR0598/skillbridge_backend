package com.udea.skillbridge.service;

import java.util.List;
import com.udea.skillbridge.dto.Cuestionario;

public interface ICuestionarioService {
	
	Cuestionario crearCuestionario (Cuestionario cuestionario);
	
	Cuestionario getById (Long idCuestionario);
	
	List<Cuestionario> listarAllCuestionarios();
	
	List<Cuestionario> listarCuestionariosActivos();

}
