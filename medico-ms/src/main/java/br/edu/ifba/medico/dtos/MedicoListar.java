package br.edu.ifba.medico.dtos;

import br.edu.ifba.medico.models.Especialidade;
import br.edu.ifba.medico.models.Medico;

public record MedicoListar(
		String nome,
		String email,
		String crm,
		Especialidade especialidade) {

	public MedicoListar(Medico medico) {
		this(medico.getDadosPessoais().getNome(), medico.getDadosPessoais().getEmail(), medico.getCrm(), medico.getEspecialidade());
	}
}