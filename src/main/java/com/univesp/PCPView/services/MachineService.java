package com.univesp.PCPView.services;

import com.univesp.PCPView.dto.machine.request.MachineRequestDTO;
import com.univesp.PCPView.dto.machine.response.MachineResponseDTO;
import com.univesp.PCPView.exceptions.NonExistentMachineException;
import com.univesp.PCPView.models.MachineModel;
import com.univesp.PCPView.repository.MachineRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MachineService {

    private final MachineRepository machineRepository;

    public MachineService(MachineRepository machineRepository) {
        this.machineRepository = machineRepository;
    }

    @Transactional
    public MachineResponseDTO registrarMaquina(MachineRequestDTO body) {
        MachineModel maquina = new MachineModel(body.id(), body.nome());

        machineRepository.save(maquina);

        return converterMaquinaParaResponseDTO(maquina);
    }

    public MachineResponseDTO buscarMaquinaPorID(String id) {
        MachineModel maquina = machineRepository.findById(id).orElseThrow(NonExistentMachineException::new);

        return converterMaquinaParaResponseDTO(maquina);
    }

    @Transactional(readOnly = true)
    public List<MachineResponseDTO> buscarTodasMaquinas() {
        return machineRepository.findAll().stream().map(this::converterMaquinaParaResponseDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<MachineResponseDTO> buscarMaquinasPorStatusOperacional(Boolean operacional) {
        return machineRepository.findByOperacional(operacional).stream().map(this::converterMaquinaParaResponseDTO).toList();
    }

    @Transactional
    public MachineResponseDTO alternarStatusOperacional(String id) {
        MachineModel maquina = machineRepository.findById(id).orElseThrow(NonExistentMachineException::new);

        maquina.alternarStatusOperacional();

        machineRepository.save(maquina);

        return converterMaquinaParaResponseDTO(maquina);
    }

    @Transactional
    public MachineResponseDTO alterarNome(String id, String nome) {
        MachineModel maquina = machineRepository.findById(id).orElseThrow(NonExistentMachineException::new);

        maquina.setNome(nome);

        machineRepository.save(maquina);

        return converterMaquinaParaResponseDTO(maquina);
    }

    @Transactional
    public void deletarMaquina(String id) {
        MachineModel maquina = machineRepository.findById(id).orElseThrow(NonExistentMachineException::new);

        machineRepository.delete(maquina);
    }

    private MachineResponseDTO converterMaquinaParaResponseDTO(MachineModel maquina) {
        return new MachineResponseDTO(maquina.getId(), maquina.getNome(), maquina.getOperacional());
    }
}
