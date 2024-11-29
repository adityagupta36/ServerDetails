package com.aditya.Server_Details.server_monitoring.repo;

import com.aditya.Server_Details.server_monitoring.entities.ServerMonitor;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServerMonitorRepo extends JpaRepository<ServerMonitor, Integer> {


}
