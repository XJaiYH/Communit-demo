$(function(){
    $("#topBtn").click(setTop);
    $("#wonderfulBtn").click(setWonderful);
    $("#deleteBtn").click(setDelete);
});

function like(btn, entityType, entityId, entityUserId, postId){
    // 发送AJAX请求之前，将SCRF令牌设置到请求的消息头中
    var token = $("meta[name='_csrf']").attr("content");
    var header = $("meta[name='_csrf_header']").attr("content");
    $(document).ajaxSend(function(e, xhr, options){
        xhr.setRequestHeader(header, token);
    })

    $.post(
        CONTEXT_PATH + "/like",
        {"entityType":entityType, "entityId":entityId, "entityUserId":entityUserId, "postId":postId},
        function(data){
            data = $.parseJSON(data);
            if(data.code==0){
                $(btn).children("i").text(data.likeCount);
                $(btn).children("b").text(data.likeStatus==1?"已赞":"赞");
            }else{
                alter(data.msg);
            }
        }
    );
}

// 置顶
function setTop(){
    var token = $("meta[name='_csrf']").attr("content");
    var header = $("meta[name='_csrf_header']").attr("content");
    $(document).ajaxSend(function(e, xhr, options){
        xhr.setRequestHeader(header, token);
    })
    $.post(
        CONTEXT_PATH + "/discuss/top",
        {"postId":$("#postId").val()},
        function(data){
            data = $.parseJSON(data);
            if(data.code == 0){
                $("#topBtn").attr("disabled", "disabled");
            }else{
                alert(data.msg);
            }
        }
    );
}

// 加精
function setWonderful(){
    var token = $("meta[name='_csrf']").attr("content");
    var header = $("meta[name='_csrf_header']").attr("content");
    $(document).ajaxSend(function(e, xhr, options){
        xhr.setRequestHeader(header, token);
    })
    $.post(
        CONTEXT_PATH + "/discuss/wonderful",
        {"postId":$("#postId").val()},
        function(data){
            data = $.parseJSON(data);
            if(data.code == 0){
                $("#wonderfulBtn").attr("disabled", "disabled");
            }else{
                alert(data.msg);
            }
        }
    );
}

// 删除
function setDelete(){
    var token = $("meta[name='_csrf']").attr("content");
    var header = $("meta[name='_csrf_header']").attr("content");
    $(document).ajaxSend(function(e, xhr, options){
        xhr.setRequestHeader(header, token);
    })
    $.post(
        CONTEXT_PATH + "/discuss/delete",
        {"postId":$("#postId").val()},
        function(data){
            data = $.parseJSON(data);
            if(data.code == 0){
                location.href=CONTEXT_PATH + "/index";// 跳转
            }else{
                alert(data.msg);
            }
        }
    );
}