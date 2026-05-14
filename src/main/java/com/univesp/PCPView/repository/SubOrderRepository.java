package com.univesp.PCPView.repository;

import com.univesp.PCPView.models.OrderModel;
import com.univesp.PCPView.models.SubOrderModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubOrderRepository extends JpaRepository<SubOrderModel, String> {
    List<SubOrderModel> findAllByOrdemPrincipal(OrderModel ordemPrincipal);
}
