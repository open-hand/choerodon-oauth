package io.choerodon.oauth.infra.enums;

/**
 * @author superlee
 * @since 0.16.0
 */
public enum ReturnPage {

    /**
     * 默认返回页
     */
    DEFAULT_PAGE("default", "/templates/index-default.html", "index-default"),
    /**
     * 移动端返回页
     */
    MOBILE_PAGE("mobile", "/templates/index-default.html", "index-default");

    private String profile;
    private String path;
    private String fileName;

    ReturnPage(String profile, String path, String fileName) {
        this.profile = profile;
        this.path = path;
        this.fileName = fileName;
    }

    public String profile() {
        return profile;
    }

    public String path() {
        return path;
    }

    public String fileName() {
        return fileName;
    }

    public static ReturnPage getByProfile(String profile) {
        for (ReturnPage page : ReturnPage.values()) {
            if (page.profile().equalsIgnoreCase(profile)) {
                return page;
            }
        }
        //如果没有对应的profile,返回DEFAULT_PAGE
        return DEFAULT_PAGE;
    }
}
