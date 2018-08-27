package io.choerodon.oauth.api.dto;

import java.util.Map;

/**
 * @author dongfan117@gmail.com
 */
public class EmailSendDTO {

    private String code;

    private String destinationEmail;

    private Map<String, Object> variables;

    public EmailSendDTO() {
    }

    public EmailSendDTO(String code, String destinationEmail, Map<String, Object> variables) {
        this.code = code;
        this.destinationEmail = destinationEmail;
        this.variables = variables;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDestinationEmail() {
        return destinationEmail;
    }

    public void setDestinationEmail(String destinationEmail) {
        this.destinationEmail = destinationEmail;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }
}
