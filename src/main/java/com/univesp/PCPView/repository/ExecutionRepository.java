package com.univesp.PCPView.repository;

import com.univesp.PCPView.models.ExecutionModel;
import com.univesp.PCPView.models.MachineModel;
import com.univesp.PCPView.models.SubOrderModel;
import com.univesp.PCPView.models.enums.ExecutionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExecutionRepository extends JpaRepository<ExecutionModel, UUID> {
    @Query("SELECT e FROM ExecutionModel e JOIN FETCH e.maquina JOIN FETCH e.subOrdem JOIN FETCH e.operador")
    List<ExecutionModel> findAllComDetalhes();

    List<ExecutionModel> findAllBySubOrdem(SubOrderModel subOrdem);

    Optional<ExecutionModel> findFirstByMaquinaAndStatusInOrderByDataInicioDesc(MachineModel maquina, List<ExecutionStatus> status);

    boolean existsByMaquinaAndStatusIn(MachineModel maquina, List<ExecutionStatus> status);
}
