addNavButton("pages/message.html","消息");
addNavButton("pages/user.html","用户");
var msgLink=null;
$("#sidebar a").each(function(index, element) {
    if($(this).attr("onclick").includes("pages/message.html")){
        msgLink=$(this);
    }
});
document.addEventListener("message",function(e){
    if (e.detail.type==="new"){
        msgLink.find("div").css("background-color","orangered");
    }
});
document.addEventListener("setPage",function(e){
    if (e.detail.url.includes("pages/message.html")){
        msgLink.find("div").css("background-color","");
    }
});
document.addEventListener("app_ws",function(e){
    console.log(e);
    var detail=e.detail;
    if (detail.type=="receive"){
        var message = detail.message;
        console.log(message);
        var data=message.data;
        if (message.op=="sendMessage"){
            app.sendMessage(data.level,data.msg);
        }
    }
});