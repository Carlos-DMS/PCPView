package com.univesp.PCPView.repository;

import com.univesp.PCPView.models.ExecutionModel;
import com.univesp.PCPView.models.enums.ExecutionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ExecutionRepository extends JpaRepository<ExecutionModel, UUID> {
    @Query("SELECT e FROM ExecutionModel e " +
            "JOIN FETCH e.maquina m " +
            "JOIN FETCH e.subOrdem s " +
            "JOIN FETCH s.ordemPrincipal o " +
            "JOIN FETCH e.operador op " +
            "WHERE (:maquinaId IS NULL OR m.id = :maquinaId) " +
            "AND (:status IS NULL OR e.status = :status) " +
            "AND (:ordemId IS NULL OR o.numeroOrdem = :ordemId)")
    List<ExecutionModel> findComFiltros(
            @Param("maquinaId") String maquinaId,
            @Param("status") ExecutionStatus status,
            @Param("ordemId") String ordemId);

    List<ExecutionModel> findByMaquinaIdAndStatusNot(String maquinaId, ExecutionStatus executionStatus);
}