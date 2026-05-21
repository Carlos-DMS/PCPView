package com.univesp.PCPView.repository;

import com.univesp.PCPView.models.OrderModel;
import com.univesp.PCPView.models.enums.StatusProducaoEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<OrderModel, String> {
    List<OrderModel> findByStatusOrderByPrioridadeAsc(StatusProducaoEnum status);

    List<OrderModel> findAllByOrderByPrioridadeAsc();
}
