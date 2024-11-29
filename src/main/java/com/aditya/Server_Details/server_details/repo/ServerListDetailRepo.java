package com.aditya.Server_Details.server_details.repo;

import com.aditya.Server_Details.server_details.entities.ServerListDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServerListDetailRepo extends JpaRepository< ServerListDetail  , Integer > {


}
