package com.aditya.Server_Details.server_master.repo;

import com.aditya.Server_Details.server_master.entities.ServerMaster;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.w3c.dom.Text;

import java.util.List;

@Repository
public interface ServerMasterRepo extends JpaRepository<ServerMaster, Integer> {
    @Query(value = "SELECT process_name FROM server_master", nativeQuery = true)
    List<String> findListOfSpecificProcess();

    @Query(value = "SELECT cpu_util_min FROM server_master", nativeQuery = true)
    List<Double> findCpuUtilMin();

    @Query(value = "SELECT cpu_util_max FROM server_master", nativeQuery = true)
    List<Double> findCpuUtilMax();

    @Query(value = "SELECT mem_util_min FROM server_master", nativeQuery = true)
    List<Long> findMemUtilMin();

    @Query(value = "SELECT mem_util_max FROM server_master", nativeQuery = true)
    List<Long> findMemUtilMax();
    @Query(value = "SELECT email_body_model FROM server_master", nativeQuery = true)
    List<String> findByEmailBodyModel();





}

