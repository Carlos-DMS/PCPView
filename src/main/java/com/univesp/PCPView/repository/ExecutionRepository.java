package com.univesp.PCPView.repository;

import com.univesp.PCPView.models.ExecutionModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ExecutionRepository extends JpaRepository<ExecutionModel, UUID> {
    @Query("SELECT e FROM ExecutionModel e JOIN FETCH e.maquina JOIN FETCH e.subOrdem JOIN FETCH e.operador")
    List<ExecutionModel> findAllComDetalhes();
}