$(function() {
    // 如果是手机端跳转
    if(/Android|webOS|iPhone|iPod|BlackBerry/i.test(navigator.userAgent)) {
        window.location.href = "/oauth/login?device=mobile";
    }
    $('.btn').click(function() {
        $("#usernameIsNullMsg").css('display','none');
        $("#passwordIsNullMsg").css('display','none');
        $("#usernameOrPasswordNotFoundMsg").html("");
        if ($("#username").val() == '') {
            $("#usernameIsNullMsg").css('display','block');
            return;
        }
        if ($("#password").val() == '') {
            $("#passwordIsNullMsg").css('display','block');
            return;
        }
        $('.login-form').submit();
    })
    document.onkeydown = function(event) {
        var e = event || window.event;
        if (e && e.keyCode == 13) {
            $('.btn').click();
        }
    }
})
$("#list").hide();


function changeLang(){
    var url = window.location.href, lang=$("#lang").val();
    window.location.href = url.substring(0, url.indexOf('?'))+"?lang="+lang;

}
function hide() {
    $("#content").hide();
    $("#list").show();
}
function show() {
    $("#content").show();
    $("#list").hide();
}

function changeImg() {
    var imgSrc = $("#imgObj");
    var src = imgSrc.attr("src");
    imgSrc.attr("src", chgUrl(src));
}
function chgUrl(url) {
    var timestamp = (new Date()).valueOf();
    return 'public/captcha?code='+timestamp;
}