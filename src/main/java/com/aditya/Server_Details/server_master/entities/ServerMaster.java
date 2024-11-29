package com.aditya.Server_Details.server_master.entities;


import jakarta.persistence.*;
import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table
public class ServerMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String processName;

    private Double cpuUtilMax;

    private Double cpuUtilMin;

    private Long memUtilMax;

    private Long memUtilMin;

    @Column(columnDefinition = "TEXT")
    private String emailBodyModel;

}
