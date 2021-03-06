package com.aelkz.blueprint.processor.rest;

import com.aelkz.blueprint.model.Beneficiario;
import com.aelkz.blueprint.route.RestResourcesEnum;
import com.aelkz.blueprint.service.dto.BeneficiarioDTO;
import com.aelkz.blueprint.service.dto.rest.BeneficiarioResponseDTO;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.ws.rs.HttpMethod;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class RestEndpointProcessor extends RestBaseProcessor implements Processor {

    private static final transient Logger logger = LoggerFactory.getLogger(RestEndpointProcessor.class);

    @Override
    public void process(Exchange exchange) throws Exception {
        Message inMessage = exchange.getIn();

        Map headers = inMessage.getHeaders();

        String restResource = (String) headers.get("CamelServletContextPath");
        String httpMethod = (String) headers.get("CamelHttpMethod");

        BeneficiarioResponseDTO response = new BeneficiarioResponseDTO();

        List<Beneficiario> result = new ArrayList();

        logger.info(httpMethod + " " +restResource);

        if (restResource.equals(RestResourcesEnum.GET_BENEFICIARIOS.getResourcePath())) {

            if (httpMethod.equals(HttpMethod.GET)) {
                // /--------------------------------------------\
                // | rest resource: GET /beneficiario/{handle}  |
                // \--------------------------------------------/

                String handleParameter = (String) headers.get("handle");

                if (handleParameter != null) {
                    Long handle = 0L;

                    processSingleResponse(response, result, handleParameter);

                }else {
                    // /--------------------------------------------\
                    // | rest resource: GET /beneficiarios          |
                    // \--------------------------------------------/

                    result = getBeneficiarioService().findAll();

                    processListResponse(response, result);
                }

            }else if (httpMethod.equals(HttpMethod.POST)) {
                // /--------------------------------------------\
                // | rest resource: POST /beneficiarios         |
                // \--------------------------------------------/

                BeneficiarioDTO request = exchange.getIn().getBody(BeneficiarioDTO.class);

                result = getBeneficiarioService().findAll(request);

                processListResponse(response, result);
            }

        }else if (restResource.equals(RestResourcesEnum.GET_BENEFICIARIO.getResourcePath())) {
            String handleParameter = (String) headers.get("handle");
            Long handle = 0L;

            processSingleResponse(response, result, handleParameter);
        }

        exchange.getOut().setBody(response);
    }

}
