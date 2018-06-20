
function changeImg() {
    var imgSrc = $("#imgObj");
    var src = imgSrc.attr("src");
    imgSrc.attr("src", chgUrl(src));
}
function chgUrl(url) {
    var timestamp = (new Date()).valueOf();
    return 'public/captcha?code='+timestamp;
}

var keyStr = "ABCDEFGHIJKLMNOP" + "QRSTUVWXYZabcdef" + "ghijklmnopqrstuv"
    + "wxyz0123456789+/" + "=";

$(function() {
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
        var username = document.getElementById("username");
        var password = document.getElementById("password");
        if (username !== '' && password !== '') {
            password.value = encode(password.value);
            $('.login-form').submit();
        }
        $('.login-form').submit();
    }})
    document.onkeydown = function(event) {
        var e = event || window.event;
        if (e && e.keyCode == 13) {
            $('.btn').click();
        }
    }
})
$("#list").hide();

function encode(inputPassword) {
    var output = "";
    var chr1, chr2, chr3 = "";
    var enc1, enc2, enc3, enc4 = "";
    var i = 0;
    do {
        chr1 = input.charCodeAt(i++);
        chr2 = input.charCodeAt(i++);
        chr3 = input.charCodeAt(i++);
        enc1 = chr1 >> 2;
        enc2 = ((chr1 & 3) << 4) | (chr2 >> 4);
        enc3 = ((chr2 & 15) << 2) | (chr3 >> 6);
        enc4 = chr3 & 63;
        if (isNaN(chr2)) {
            enc3 = enc4 = 64;
        } else if (isNaN(chr3)) {
            enc4 = 64;
        }
        output = output + keyStr.charAt(enc1) + keyStr.charAt(enc2)
            + keyStr.charAt(enc3) + keyStr.charAt(enc4);
        chr1 = chr2 = chr3 = "";
        enc1 = enc2 = enc3 = enc4 = "";
    } while (i < input.length);
    return output;
}


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