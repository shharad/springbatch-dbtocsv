package com.end.dbtocsv.util;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class AppProperties {

    @Value("${files.output.path}")
    String fileOutputPath;

}
