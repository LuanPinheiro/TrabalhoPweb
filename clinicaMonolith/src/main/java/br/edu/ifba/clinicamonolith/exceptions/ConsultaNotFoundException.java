package br.edu.ifba.clinicamonolith.exceptions;

@SuppressWarnings("serial")
public class ConsultaNotFoundException extends Exception {
	public ConsultaNotFoundException(String message) {
		super(message);
	}
}
