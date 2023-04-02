package com.end.dbtocsv.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OutputFileDto {

    private int serviceId;
    private String serviceName;
    private String serviceType;
    private String sourceSystem;
    private String status;
    private String remark;
    private String createdBy;
    private Date createdOn;
    private String modifiedBy;
    private Date modifiedOn;
}
