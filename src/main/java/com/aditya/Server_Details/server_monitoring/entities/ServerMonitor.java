package com.aditya.Server_Details.server_monitoring.entities;

import com.aditya.Server_Details.server_details.entities.ServerListDetail;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Data
@Table
public class ServerMonitor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    LocalDateTime localDateTime;

    @OneToMany(mappedBy = "serverMonitor", cascade = CascadeType.ALL, orphanRemoval = true)
    List<ServerListDetail> serverListDetails;

}
