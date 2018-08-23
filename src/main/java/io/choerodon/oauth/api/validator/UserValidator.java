package io.choerodon.oauth.api.validator;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author dongfan117@gmail.com
 */
@Component
public class UserValidator {

    public static final String EMAIL_REGULAR_EXPRESSION = "[\\w!#$%&'*+/=?^_`{|}~-]+(?:\\.[\\w!#$%&'*+/=?^_`{|}~-]+)*@(?:[\\w](?:[\\w-]*[\\w])?\\.)+[\\w](?:[\\w-]*[\\w])?";

    public static final String PHONE_REGULAR_EXPRESSION = "^((13[0-9]|14[579]|15[0-3,5-9]|17[0135678]|18[0-9])\\d{8})?$";

    @Value("${choerodon.oauth.login.field:mail,phone}")
    private String[] queryField;

    private Set<String> fieldSet;

    @PostConstruct
    private void init() {
        this.fieldSet = new HashSet(Arrays.asList(queryField));
    }

    public boolean emailValidator(String email) {
        return fieldSet.contains("mail") && Pattern.compile(EMAIL_REGULAR_EXPRESSION).matcher(email).matches();
    }

    public boolean phoneValidator(String phone) {
        return fieldSet.contains("phone") && Pattern.compile(PHONE_REGULAR_EXPRESSION).matcher(phone).matches();
    }
}
