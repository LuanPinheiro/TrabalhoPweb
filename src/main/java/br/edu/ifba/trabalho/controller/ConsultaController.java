package br.edu.ifba.trabalho.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.edu.ifba.trabalho.dtos.ConsultaCancelar;
import br.edu.ifba.trabalho.dtos.ConsultaEnviar;
import br.edu.ifba.trabalho.dtos.ConsultaListar;
import br.edu.ifba.trabalho.exceptions.DataInvalidaException;
import br.edu.ifba.trabalho.exceptions.RegistroNotFoundException;
import br.edu.ifba.trabalho.services.ConsultaService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/consultas")
public class ConsultaController {
	
	@Autowired
	private ConsultaService consultaService;
	
	@GetMapping
	public List<ConsultaListar> listarConsultas(){
		return consultaService.listarConsultas();
	}
	
	@PostMapping
	public ResponseEntity<?> marcarConsulta(@Valid @RequestBody ConsultaEnviar dados) throws RegistroNotFoundException, DataInvalidaException{
		
		consultaService.marcarConsulta(dados);
		return new ResponseEntity<>(HttpStatus.ACCEPTED);
	}
	
	@DeleteMapping
	public ResponseEntity<?> cancelarConsulta(@RequestBody ConsultaCancelar dados) throws RegistroNotFoundException{
		
		consultaService.cancelarConsulta(dados);
		return new ResponseEntity<>(HttpStatus.ACCEPTED);

	}
	
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public Map<String, String> handleValidationExceptions(
	  MethodArgumentNotValidException ex) {
		
		Map<String, String> errors = new HashMap<String, String>();
	    ex.getBindingResult().getAllErrors().forEach((error) -> {
	        String fieldName = ((FieldError) error).getField();
	        String errorMessage = error.getDefaultMessage();
	        errors.put(fieldName, errorMessage);
	    });
	    return errors;
	}
	
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(RegistroNotFoundException.class)
	public Map<String, String> handleRegistroNotFoundException() {
	    
		Map<String, String> errors = new HashMap<String, String>();
        errors.put("message", "Médico ou paciente não encontrado");
	    return errors;
	}
	
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(DataInvalidaException.class)
	public Map<String, String> handleDataInvalidaException() {
	    
		Map<String, String> errors = new HashMap<String, String>();
        errors.put("message", "Data inválida");
	    return errors;
	}
}
