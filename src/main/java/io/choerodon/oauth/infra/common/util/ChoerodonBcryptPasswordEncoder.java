package io.choerodon.oauth.infra.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.SecureRandom;
import java.util.regex.Pattern;

/**
 * 修改自BCryptPasswordEncoder
 * 数据库oauth_client的secret存储为明文，升级之后要求为秘文存储
 * 因此如果match不到，则先加密再次进行匹配
 *
 * @author flyleft
 */
public class ChoerodonBcryptPasswordEncoder  implements PasswordEncoder {

    private static final Pattern BCRYPT_PATTERN = Pattern.compile("\\A\\$2a?\\$\\d\\d\\$[./0-9A-Za-z]{53}");

    private static final int MIN_LOG_ROUNDS = 4;

    private static final int MAX_LOG_ROUNDS = 31;

    private final Logger log = LoggerFactory.getLogger(ChoerodonBcryptPasswordEncoder.class);

    private final int strength;

    private final SecureRandom random;

    public ChoerodonBcryptPasswordEncoder() {
        this(-1);
    }

    public ChoerodonBcryptPasswordEncoder(int strength) {
        this(strength, null);
    }

    public ChoerodonBcryptPasswordEncoder(int strength, SecureRandom random) {
        if (strength != -1 && (strength < MIN_LOG_ROUNDS || strength > MAX_LOG_ROUNDS)) {
            throw new IllegalArgumentException("Bad strength");
        }
        this.strength = strength;
        this.random = random;
    }

    @Override
    public String encode(CharSequence rawPassword) {
        String salt;
        if (strength > 0) {
            if (random != null) {
                salt = BCrypt.gensalt(strength, random);
            } else {
                salt = BCrypt.gensalt(strength);
            }
        } else {
            salt = BCrypt.gensalt();
        }
        return BCrypt.hashpw(rawPassword.toString(), salt);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (encodedPassword == null || encodedPassword.length() == 0) {
            log.warn("Empty encoded password");
            return false;
        }
        if (BCRYPT_PATTERN.matcher(encodedPassword).matches()) {
            // 匹配不到先加密再匹配
            return BCrypt.checkpw(rawPassword.toString(), encodedPassword);
        } else {
            String ep = this.encode(encodedPassword);
            if (BCRYPT_PATTERN.matcher(this.encode(ep)).matches()) {
                return BCrypt.checkpw(rawPassword.toString(), ep);
            }
            log.warn("Encoded password does not look like BCrypt");
            return false;
        }

    }
}
