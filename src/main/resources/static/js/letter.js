$(function(){
	$("#sendBtn").click(send_letter);
	$(".close").click(delete_msg);
});

function send_letter() {
	$("#sendModal").modal("hide");
	var username = $("#recipient-name").val();
	var content = $("#message-text").val();
	$.post(
	    CONTEXT_PATH + "/letter/send",
	    {"username":username, "content":content},
	    function(data) {
            data = $.parseJSON(data);
            $("#hintBody").text(data.msg);
            $("#hintModal").modal("show");
            setTimeout(function(){
                $("#hintModal").modal("hide");
                if(data.code == 0){
                    // 成功发布
                    window.location.reload();
                }
            }, 2000);
	    }
	)
}

function delete_msg() {
	// TODO 删除数据
	$(this).parents(".media").remove();
}