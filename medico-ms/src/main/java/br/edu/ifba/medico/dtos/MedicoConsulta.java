package br.edu.ifba.medico.dtos;

import br.edu.ifba.medico.models.Especialidade;
import br.edu.ifba.medico.models.Medico;

public record MedicoConsulta(String crm, String nome, String email, Especialidade especialidade) {
	public MedicoConsulta(Medico medico) {
		this(
				medico.getCrm(),
				medico.getDadosPessoais().getNome(),
				medico.getDadosPessoais().getNome(),
				medico.getEspecialidade()
		);
	}
}
