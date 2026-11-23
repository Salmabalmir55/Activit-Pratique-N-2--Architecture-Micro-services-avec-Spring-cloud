package net.salma.ecom.billingservice.repository;

import net.salma.ecom.billingservice.entities.Bill;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillRepository  extends JpaRepository<Bill, Long> {
}
