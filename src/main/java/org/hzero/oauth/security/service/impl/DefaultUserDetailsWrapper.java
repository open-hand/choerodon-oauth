package org.hzero.oauth.security.service.impl;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.hzero.common.HZeroService;
import org.hzero.core.base.BaseConstants;
import org.hzero.core.redis.safe.SafeRedisHelper;
import org.hzero.mybatis.domian.Language;
import org.hzero.oauth.domain.repository.RoleSecGrpRepository;
import org.hzero.oauth.domain.repository.UserRepository;
import org.hzero.oauth.domain.vo.Role;
import org.hzero.oauth.domain.vo.UserRoleDetails;
import org.hzero.oauth.domain.vo.UserVO;
import org.hzero.oauth.security.constant.SecurityAttributes;
import org.hzero.oauth.security.exception.LoginExceptions;
import org.hzero.oauth.security.service.UserDetailsWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.mybatis.helper.LanguageHelper;

/**
 * 处理 UserDetails
 *
 * @author bojiangzhou 2019/02/27
 */
public class DefaultUserDetailsWrapper implements UserDetailsWrapper {
    private static final Logger logger = LoggerFactory.getLogger(DefaultUserDetailsWrapper.class);
    private static final String ROLE_MERGE_PREFIX = "hpfm:config:ROLE_MERGE.";
    private static final String TENANT_DEFAULT_LANGUAGE = "TENANT_DEFAULT_LANGUAGE";

    private UserRepository userRepository;
    private RoleSecGrpRepository roleSecGrpRepository;

    public DefaultUserDetailsWrapper(UserRepository userRepository, RoleSecGrpRepository roleSecGrpRepository) {
        this.userRepository = userRepository;
        this.roleSecGrpRepository = roleSecGrpRepository;
    }

    @Override
    public void warp(CustomUserDetails details, Long userId, Long tenantId, boolean login) {
        logger.debug(">>>>> Before warp[{},{}] : {}", userId, tenantId, details);
        if (details.getTenantId() != null) {
            tenantId = details.getTenantId();
        }
        List<UserRoleDetails> roleDetailList = selectUserRoles(details, tenantId);
        if (CollectionUtils.isNotEmpty(roleDetailList)) {
            List<Long> tenantIds = roleDetailList.stream().map(UserRoleDetails::getTenantId).distinct().collect(Collectors.toList());
            // 如果是登录
            if (login) {
                UserRoleDetails userRoleDetails = roleDetailList.get(0);
                // 如果有设置默认租户并且默认租户在可访问租户列表中取默认租户
                if (userRoleDetails.getDefaultTenantId() != null && tenantIds.contains(userRoleDetails.getDefaultTenantId())) {
                    tenantId = userRoleDetails.getDefaultTenantId();
                }
                // 如果没有默认租户，有租户访问历史并且最近访问租户再可访问租户列表中，默认登录最近访问租户
                else if (userRoleDetails.getAccessDatetime() != null && tenantIds.contains(userRoleDetails.getTenantId())) {
                    tenantId = userRoleDetails.getTenantId();
                }
            }
            // 如果当前租户不属于可访问租户列表，取可访问租户列表第一条
            if (!tenantIds.contains(tenantId)) {
                tenantId = tenantIds.stream()
                        .findFirst()
                        .orElseThrow(() -> new CommonException(LoginExceptions.ROLE_NONE.value()));
            }
            for (UserRoleDetails roleDetails : roleDetailList) {
                if (Objects.equals(tenantId, roleDetails.getTenantId())) {
                    // 筛选当前租户下可访问的角色（出现冲突时必定是数据问题，这里留一手）
                    Map<Long, Role> roleMap = roleDetails.getRoles().stream().collect(Collectors.toMap(Role::getId, Function.identity(), (v1, v2) -> v1));
                    // 防止加载用户信息时覆盖掉当前用户选择的租户
                    if (details.getRoleId() == null || !roleMap.containsKey(details.getRoleId())) {
                        Role role;
                        if (roleMap.containsKey(roleDetails.getDefaultRoleId())) {
                            role = roleMap.get(roleDetails.getDefaultRoleId());
                        } else {
                            role = roleDetails.getRoles().stream().findFirst().orElse(new Role());
                        }
                        details.setRoleId(role.getId());
                    }
                    details.setRoleIds(new ArrayList<>(roleMap.keySet()));
                    details.setSiteRoleIds(roleMap.values().stream()
                            .filter(item -> "site".equals(item.getLevel())).map(Role::getId)
                            .collect(Collectors.toList()));
                    details.setTenantRoleIds(roleMap.values().stream()
                            .filter(item -> "organization".equals(item.getLevel())).map(Role::getId)
                            .collect(Collectors.toList()));
                    details.setTenantIds(tenantIds);
                    if (details.getTenantId() != null && !tenantIds.contains(details.getTenantId())) {
                        details.setTenantId(null);
                    } else {
                        details.setTenantId(tenantId);
                        details.setTenantNum(roleDetails.getTenantNum());
                        this.initRoleMergeFlag(details, roleDetails);
                        this.initUserLanguage(details, roleDetails);
                    }

                    // 角色对应的角色类型
                    Map<Long, String> roleTypes = roleMap.values().stream()
                            .collect(Collectors.toMap(Role::getId, Role::getRoleTypeCode));
                    details.setRoleTypes(roleTypes);
                    break;
                }
            }
        }

        if (CollectionUtils.isEmpty(details.getRoleIds())) {
            logger.warn("User not assign any role! userId: {}", details.getUserId());
        }


        warpRoleInfo(details, details.getRoleId());

        logger.debug(">>>>> After warp[{},{}] : {}", userId, tenantId, details);
    }

    @Override
    public void warpRoleInfo(CustomUserDetails details, Long roleId) {
        details.setRoleId(roleId);
        // 设置角色标签
        Set<String> labels = Optional.ofNullable(userRepository.selectRoleLabels(details.roleMergeIds())).orElse(new HashSet<>(0));
        details.setRoleLabels(labels);

        // 初始化安全组
        this.initSecGrpIds(details);
    }

    protected List<UserRoleDetails> selectUserRoles(CustomUserDetails details, Long tenantId) {
        return userRepository.selectUserRoles(details, tenantId);
    }

    /**
     * 初始化角色合并标识
     *
     * @param details     用户详情对象
     * @param roleDetails 角色详情
     */
    protected void initRoleMergeFlag(CustomUserDetails details, UserRoleDetails roleDetails) {
        details.setRoleMergeFlag(BaseConstants.Flag.YES.equals(Optional.ofNullable(roleDetails.getRoleMergeFlag())
                .orElseGet(() -> {
                    String roleMergeFlag = SafeRedisHelper.execute(HZeroService.Platform.REDIS_DB, (helper) -> {
                        String str = helper.strGet(ROLE_MERGE_PREFIX + details.getTenantId());
                        if (StringUtils.isBlank(str)) {
                            str = helper.strGet(ROLE_MERGE_PREFIX + BaseConstants.DEFAULT_TENANT_ID.toString());
                        }
                        return str;
                    });

                    return StringUtils.isBlank(roleMergeFlag) ? BaseConstants.Flag.NO : Integer.valueOf(roleMergeFlag);
                })));
    }

    /**
     * 初始化用户语言
     *
     * @param details     用户详情
     * @param roleDetails 角色详情
     */
    protected void initUserLanguage(CustomUserDetails details, UserRoleDetails roleDetails) {
        // 设置语言
        details.setLanguage(this.getUserInitLanguage(details, roleDetails));
    }

    /**
     * 初始化安全组IDs
     *
     * @param details 用户详情对象
     */
    protected void initSecGrpIds(CustomUserDetails details) {
        details.setSecGrpIds(this.selectRoleSecGrpIds(details.roleMergeIds()));
    }

    /**
     * 查询角色的安全组IDs
     *
     * @param roleIds 角色IDs
     * @return 安全组IDs
     */
    protected Set<Long> selectRoleSecGrpIds(@Nonnull List<Long> roleIds) {
        return this.roleSecGrpRepository.sgIdsOfRoleIds(roleIds);
    }

    /**
     * 获取用户初始化语言
     *
     * @param details     用户详情
     * @param roleDetails 角色详情
     * @return 用户初始化语言
     */
    private String getUserInitLanguage(CustomUserDetails details, UserRoleDetails roleDetails) {
        Long tenantId = details.getTenantId();

        // 获取当前租户可用语言列表
        Set<String> languageCodes = this.getTenantLanguages(tenantId);

        String language;
        // 1、取用户界面选择的语言
        language = this.getUserPageLanguage();
        logger.info("========================getUserPageLanguage:{},userId:{}", language, details.getUserId());
        if (languageCodes.contains(language)) {
            return language;
        }

        // 2、取用户配置默认语言 (hiam_user_config [default_language])
        language = roleDetails.getDefaultLanguage();
        logger.info("========================getDefaultLanguage:{},userId:{}", language, details.getUserId());
        if (languageCodes.contains(language)) {
            return language;
        }

        // 2、取用户全局默认语言配置 (iam_user [language])
        language = roleDetails.getUserLanguage();
        logger.info("========================getUserLanguage:{},userId:{}", language, details.getUserId());
        if (languageCodes.contains(language)) {
            return language;
        }

        // 3、取租户默认语言
        language = this.getTenantDefaultLanguage(tenantId);
        if (languageCodes.contains(language)) {
            return language;
        }

        // 4、取租户可用语言中的一条，如果租户可用语言也为空，就取系统默认语言
        return languageCodes.stream().findFirst().orElse(LanguageHelper.getDefaultLanguage());
    }

    /**
     * 获取租户语言
     *
     * @param tenantId 租户ID
     * @return 租户语言
     */
    @NonNull
    private Set<String> getTenantLanguages(@NonNull Long tenantId) {
        // 获取当前租户可用语言列表
        List<Language> languages = LanguageHelper.languages(tenantId);
        return Optional.ofNullable(languages).orElse(Collections.emptyList())
                .stream().map(Language::getCode).collect(Collectors.toSet());
    }

    /**
     * 取用户界面选择的语言
     *
     * @return 用户界面选择的语言
     */
    @Nullable
    private String getUserPageLanguage() {
        // 从session中获取数据
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (Objects.isNull(requestAttributes)) {
            return null;
        }

        Object initLangFlagAttribute = requestAttributes.getAttribute(SecurityAttributes.FIELD_INIT_LANG_FLAG,
                RequestAttributes.SCOPE_SESSION);
        // 如果初始化标识不为false，就说明是初始化操作，此时不从session中取语言
        if (initLangFlagAttribute instanceof Boolean && BooleanUtils.isNotFalse((Boolean) initLangFlagAttribute)) {
            return null;
        }

        Object attribute = requestAttributes.getAttribute(SecurityAttributes.FIELD_LANG, RequestAttributes.SCOPE_SESSION);
        if (Objects.isNull(attribute)) {
            return null;
        }

        if (attribute instanceof String) {
            return (String) attribute;
        } else if (attribute instanceof Language) {
            return ((Language) attribute).getCode();
        } else if (attribute instanceof org.hzero.oauth.domain.entity.Language) {
            return ((org.hzero.oauth.domain.entity.Language) attribute).getCode();
        }

        return null;
    }

    /**
     * 获取租户默认语言
     * <p>
     * 如果租户默认语言为空，就获取0租户的默认语言
     *
     * @param tenantId 租户ID
     * @return 租户默认语言
     */
    @Nullable
    private String getTenantDefaultLanguage(@NonNull Long tenantId) {
        return SafeRedisHelper.execute(HZeroService.Platform.REDIS_DB, (helper) -> {
            // 3.1、当前租户默认配置
            String lang = helper.strGet(UserVO.generateCacheKey(TENANT_DEFAULT_LANGUAGE, tenantId));
            if (StringUtils.isBlank(lang) && !Objects.equals(tenantId, BaseConstants.DEFAULT_TENANT_ID)) {
                // 3.2、取0租户默认配置
                lang = helper.strGet(UserVO.generateCacheKey(TENANT_DEFAULT_LANGUAGE, BaseConstants.DEFAULT_TENANT_ID));
            }

            return lang;
        });
    }
}
