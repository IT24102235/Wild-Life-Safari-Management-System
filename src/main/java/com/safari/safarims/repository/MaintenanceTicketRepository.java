package com.safari.safarims.repository;

import com.safari.safarims.entity.MaintenanceTicket;
import com.safari.safarims.common.enums.TicketStatus;
import com.safari.safarims.common.enums.TicketSeverity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaintenanceTicketRepository extends JpaRepository<MaintenanceTicket, Long> {
    List<MaintenanceTicket> findByVehicleId(Long vehicleId);
    List<MaintenanceTicket> findByFiledByUserId(Long userId);
    List<MaintenanceTicket> findByAssigneeMechanicId(Long mechanicId);
    List<MaintenanceTicket> findByStatus(TicketStatus status);
    List<MaintenanceTicket> findBySeverity(TicketSeverity severity);

    @Query("SELECT t FROM MaintenanceTicket t WHERE t.status IN :statuses ORDER BY t.severity DESC, t.openedAt ASC")
    List<MaintenanceTicket> findByStatusInOrderBySeverityAndDate(@Param("statuses") List<TicketStatus> statuses);
}
