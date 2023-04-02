package com.end.dbtocsv.config;


import com.end.dbtocsv.dto.OutputFileDto;
import com.end.dbtocsv.entity.RssService;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Service;

@Service
public class RssServiceProcessor implements ItemProcessor<RssService, OutputFileDto> {

    @Override
    public OutputFileDto process(RssService item) throws Exception {

        OutputFileDto outputFileDto = new OutputFileDto();

        if (item.getServiceType().equals("DCB")) {

            outputFileDto.setServiceId(item.getServiceId());
            outputFileDto.setServiceName(item.getServiceName());
            outputFileDto.setServiceType(item.getServiceType());
            outputFileDto.setSourceSystem(item.getSourceSystem());
            outputFileDto.setStatus(item.getStatus());
            outputFileDto.setRemark("File exported using Spring Batch job - exportDataFromDbToCsv"); // The additional info is added in remark field during processing
            outputFileDto.setCreatedBy(item.getCreatedBy());
            outputFileDto.setCreatedOn(item.getCreatedOn());
            outputFileDto.setModifiedBy(item.getModifiedBy());
            outputFileDto.setModifiedOn(item.getModifiedOn());
        }

        return outputFileDto;
    }

}
