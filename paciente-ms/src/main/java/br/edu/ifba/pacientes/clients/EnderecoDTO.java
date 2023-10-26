package br.edu.ifba.pacientes.clients;

import jakarta.validation.constraints.NotBlank;

public record EnderecoDTO(
		@NotBlank(message = "Logradouro não pode ser nulo") String logradouro,
		String numero,
		String complemento,
		@NotBlank(message = "Bairro não pode ser nulo") String bairro,
		@NotBlank(message = "Cidade não pode ser nulo") String cidade,
		@NotBlank(message = "UF não pode ser nulo") String uf,
		@NotBlank(message = "CEP não pode ser nulo") String cep) {
}