package com.aditya.Server_Details.server_details.entities;

import com.aditya.Server_Details.server_monitoring.entities.ServerMonitor;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.function.LongToDoubleFunction;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Data
@Table
public class ServerListDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer pId;



    private Integer processId;

    private String processName;

    private Double cpuUtilizationTotal;

    private Long memoryUtilizationTotal;

    private Double specificDriveStorage;

    private Double specificDriveFreeStorage;

    @JoinColumn(name = "serverMonitorFK")
    @ManyToOne(fetch = FetchType.LAZY)
    private ServerMonitor serverMonitor;


}
