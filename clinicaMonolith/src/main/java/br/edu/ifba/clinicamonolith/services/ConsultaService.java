package br.edu.ifba.clinicamonolith.services;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.edu.ifba.clinicamonolith.dtos.ConsultaCancelar;
import br.edu.ifba.clinicamonolith.dtos.ConsultaEnviar;
import br.edu.ifba.clinicamonolith.dtos.ConsultaListar;
import br.edu.ifba.clinicamonolith.exceptions.CantCancelConsultaException;
import br.edu.ifba.clinicamonolith.exceptions.ConsultaExistenteException;
import br.edu.ifba.clinicamonolith.exceptions.ConsultaNotFoundException;
import br.edu.ifba.clinicamonolith.exceptions.DataInvalidaException;
import br.edu.ifba.clinicamonolith.exceptions.MedicoUnavailableException;
import br.edu.ifba.clinicamonolith.exceptions.PacienteJaAgendadoException;
import br.edu.ifba.clinicamonolith.exceptions.RegistroNotFoundException;
import br.edu.ifba.clinicamonolith.models.Consulta;
import br.edu.ifba.clinicamonolith.models.ConsultaId;
import br.edu.ifba.clinicamonolith.models.Medico;
import br.edu.ifba.clinicamonolith.models.Paciente;
import br.edu.ifba.clinicamonolith.repositories.ConsultaRepository;

@Service
public class ConsultaService {

	@Autowired
	private ConsultaRepository consultaRepository;
	
	@Autowired
	private MedicoService medicoService;
	
	@Autowired
	private PacienteService pacienteService;
	
	
	public List<ConsultaListar> converteLista(List<Consulta> lista){
		// Convertendo cada registro de uma query para um DTO de listagem
		return lista.stream().map(ConsultaListar::new).collect(Collectors.toList());
	}
	
	public List<ConsultaListar> listarConsultas() {
		// Retorna os registros do banco em forma de DTO
		return this.converteLista(consultaRepository.findAll());
	}
	
	public void marcarConsulta(ConsultaEnviar dados) 
			throws RegistroNotFoundException,
			DataInvalidaException,
			ConsultaExistenteException,
			PacienteJaAgendadoException,
			MedicoUnavailableException{
		
		Medico medico;
		// Caso o usuário indique um id de médico
		if(dados.idMedico() != null) {
			// Verifica se o médico existe e está ativo
			medico = medicoService.encontrarPorId(dados.idMedico());
		}
		else {
			// Verifica se existe algum médico com a especialidade indicada e retorna um aleatório
			medico = medicoService.medicoAleatorioPorEspecialidade(dados.especialidade());
		}
		
		// Verifica se o paciente existe e está ativo
		Paciente paciente = pacienteService.encontrarPorId(dados.idPaciente());
		
		LocalDate data = dados.data();
		LocalTime hora = dados.hora();
		// Valida a data segundo as regras de negócio
		this.validaData(data, hora);
		// Valida a consulta segundo as regras de negócio
		this.validaConsulta(medico, paciente, data, hora);
		
		consultaRepository.save(new Consulta(medico, paciente, data, hora));
	}
	
	private Optional<Consulta> encontrarPorIds(ConsultaId ids){
		return consultaRepository.findByIds(ids);
	}

	public void cancelarConsulta(ConsultaCancelar dados) 
			throws RegistroNotFoundException,
			ConsultaNotFoundException,
			CantCancelConsultaException {
		// Recupera a consulta no banco
		Consulta consulta = this.encontrarPorIds(new ConsultaId(
				dados.idMedico(),
				dados.idPaciente(),
				dados.data(),
				dados.hora())).orElseThrow(() -> new ConsultaNotFoundException("Essa consulta não foi agendada"));
		
		if(consulta.isDesmarcado()) {
			throw new ConsultaNotFoundException("Consulta já foi desmarcada");
		}

		LocalDateTime agora = LocalDateTime.now();
		LocalDateTime cancelamento = LocalDateTime.of(dados.data(), dados.hora());
		Duration diff = Duration.between(agora, cancelamento);
		// Valida se a consulta está sendo desmarcada com no mínimo 24 horas de antecedência
		if(diff.toHours() < 24) {
			throw new CantCancelConsultaException();
		}
		
		// Cancela a consulta
		consulta.setDesmarcado(true);
		consulta.setMotivo(dados.motivo());
		consultaRepository.save(consulta);
		
	}
	
	private void validaData(LocalDate data, LocalTime hora) throws DataInvalidaException {
		LocalTime agora = LocalTime.now();
		
		// Validando se a data é num domingo
		if(data.getDayOfWeek() == DayOfWeek.SUNDAY)
			throw new DataInvalidaException("Clínica não está disponível aos domingos");
		
		// Validando se a data é num domingo ou num horário inválido
		if(
				hora.getHour() < 7
				|| hora.getHour() > 18
				|| hora.getMinute() != 0
				|| hora.getSecond() != 0
				)
			throw new DataInvalidaException("Clínica não está disponível nesse horário");
		
		
		// Valida se a consulta está sendo feita com no mínimo 30min de antecedência
		Duration diff = Duration.between(agora, hora);
		if(diff.toMinutes() <= 30)
			throw new DataInvalidaException("Consulta só pode ser marcada com no mínimo 30 minutos de antecedência");
	}
	
	private void validaConsulta(Medico medico, Paciente paciente, LocalDate data, LocalTime hora) 
			throws ConsultaExistenteException,
			PacienteJaAgendadoException,
			MedicoUnavailableException {
		// Valida se a consulta já existe
		encontrarPorIds(new ConsultaId(medico.getId(), paciente.getId(), data, hora)).orElseThrow(ConsultaExistenteException::new);
		
		// Valida se o médico já tem consulta nessa hora
		consultaRepository.findByIdsMedicoIdAndIdsHora(medico.getId(), hora).orElseThrow(MedicoUnavailableException::new);
		
		// Valida se o paciente já tem consulta no dia
		consultaRepository.findByIdsDataAndIdsPacienteId(data, paciente.getId()).orElseThrow(PacienteJaAgendadoException::new);
	}
}
