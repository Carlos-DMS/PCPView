package com.univesp.PCPView.repository;

import com.univesp.PCPView.models.MachineModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MachineRepository extends JpaRepository<MachineModel, String> {
    List<MachineModel> findByOperacional(Boolean operacional);
}
