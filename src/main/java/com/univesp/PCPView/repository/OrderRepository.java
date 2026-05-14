package com.univesp.PCPView.repository;

import com.univesp.PCPView.models.OrderModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<OrderModel, String> {
}
