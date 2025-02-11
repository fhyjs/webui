function usersidebar(enable){
    let width = $("#usersidebar").outerWidth(true);
    $("#usersidebar").css("right", (enable==1?0:-1*width)+"px");
    // 创建自定义事件
    const event = new CustomEvent("usersidebarEvent", {
        detail: { open: enable} // 可选，传递数据
    });
    document.dispatchEvent(event);
}
function getGreeting() {
    var now = new Date();
    var hour = now.getHours();
    var greeting;

    if (hour >= 5 && hour < 12) {
        greeting = "早上好！";
    } else if (hour >= 12 && hour < 18) {
        greeting = "下午好！";
    } else if (hour >= 18 && hour < 22) {
        greeting = "晚上好！";
    } else {
        greeting = "夜深了，早点休息哦！";
    }
    
    return greeting;
}
function executeEverySecond() {
    currentTime = new Date();
    formattedTime = formatDate(currentTime);
    $("#time").html(formattedTime);
}
// 每秒执行一次
const intervalId1s = setInterval(executeEverySecond, 1000);
function synchronousPostRequest(url, data,type="application/x-www-form-urlencoded",method='POST') {
    const xhr = new XMLHttpRequest();

    // 设置为同步请求
    xhr.open(method, url, false); // 第三个参数设为 false 表示同步请求

    // 设置请求头
    xhr.setRequestHeader("Content-Type", type);
    if (method==="POST"){
        // 发送请求并等待响应
        xhr.send(data.toString()); // 将数据转换为 JSON 字符串
    }

    // 检查响应状态
    if (xhr.status === 200) {
        console.log("响应数据:", xhr.responseText);
        return xhr.responseText; // 返回响应数据
    } else {
        console.error("请求失败，状态码:", xhr.status);
        return null; // 返回 null 表示请求失败
    }
}
// 解析 HTTP 响应头
function parseHttpResponseHeaders(headers) {
    const cleanHeaders = headers.replace(/\r/g, '');
    const headerLines = cleanHeaders.split('\n'); // 按行拆分
    const result = {};
    
    // 解析状态行
    const statusLine = headerLines[0];
    const [httpVersion, statusCode, statusText] = statusLine.split(' ');
    result.statusLine = { httpVersion, statusCode, statusText };
    
    // 解析头部
    for (let i = 1; i < headerLines.length; i++) {
        const line = headerLines[i].trim();
        if (line) {
            const [key, ...valueParts] = line.split(':');
            const keyTrimmed = key.trim();
            const valueTrimmed = valueParts.join(':').trim();
            result[keyTrimmed] = valueTrimmed; // 存储到结果对象中
        }
    }

    return result;
}
function addNavButton(url,name){
    let e = $(`<a onclick='setPage("${url}");' one-link-mark="yes">
        <div class="sidebar-list">TEXT</div>
    </a>`);
    e.find("div").html(name);
    $("#sidebar #nav-btn-ps").before(e);
}
// 解析 Set-Cookie
function parseSetCookie(setCookie) {
    const cookies = {};
    
    // 按分号拆分 Cookie 字符串
    const cookieParts = setCookie.split(';');

    // 第一个部分是 Cookie 的键值对
    const [cookieKeyValue] = cookieParts[0].split('=');
    const cookieValue = cookieParts[0].substring(cookieParts[0].indexOf('=') + 1);
    cookies[cookieKeyValue.trim()] = cookieValue.trim();

    // 解析其他属性
    for (let i = 1; i < cookieParts.length; i++) {
        const part = cookieParts[i].trim();
        if (part) {
            const [key, value] = part.split('=');
            const keyTrimmed = key.trim().toLowerCase(); // 转为小写以方便处理

            // 处理没有值的属性
            if (value === undefined) {
                cookies[keyTrimmed] = true; // 如果没有值，设为 true
            } else {
                cookies[keyTrimmed] = value.trim();
            }
        }
    }

    return cookies;
}
function debounce(func, delay) {
    let timeout;
    return function() {
        clearTimeout(timeout);
        timeout = setTimeout(func, delay);
    };
}

window.addEventListener('resize', debounce(function() {
    console.log('防抖: 窗口大小已变化');
    let bodyHeight = $("body").height();
    let userbarHeight = $("#userbar").outerHeight(true); // 包括外部边距
    let footerbarHeight = $("#footer").outerHeight(true); // 包括外部边距
    let newHeight = bodyHeight - userbarHeight - footerbarHeight; // 去掉 * 0.95 以确保 iframe 填满剩余空间

    $("#content iframe").height(newHeight); // 设置 iframe 高度

    console.log(`设置 iframe 高度为: ${newHeight}px`);
    if (newHeight<5){
        window.dispatchEvent(new Event("resize"));
    }
}, 200));
function setPage(url){
    $("#content iframe").attr("src",url);
    $("#path-loc").html(url);
    const event = new CustomEvent("setPage", {
        detail: { url:url} // 可选，传递数据
    });
    document.dispatchEvent(event);
}
function getAvatar(url){
    let data = new URLSearchParams(); // 使用 URLSearchParams 构造表单数据
    data.append('x-url', url); // 添加数据项
    data.append('x-method', 'GET'); // 添加数据项
    let byteCharacters = synchronousPostRequest("util/oh_proxy.php?show=body&type=base64",data);
    const binaryString = atob(byteCharacters);

    // 将二进制字符串转换为字节数组
    const byteNumbers = new Uint8Array(binaryString.length);
    for (let i = 0; i < binaryString.length; i++) {
        byteNumbers[i] = binaryString.charCodeAt(i);
    }

    // 创建 Blob 对象
    const blob = new Blob([byteNumbers], { type: 'image/jpeg' }); // 根据图片类型设置 MIME 类型
    return URL.createObjectURL(blob);
}
function loadJs(url){
    var scriptElement = $('<script>');
    scriptElement.attr('src', url);
    // 将 script 元素添加到 head 标签中
    $('head').append(scriptElement);
}
function loadCss(url){
    var linkElement = $('<link>');
    linkElement.attr({
        rel: 'stylesheet',
        type: 'text/css',
        href: url // 将样式文件路径替换为你实际的CSS文件路径
    });

    // 将 <link> 标签添加到 head 标签中
    $('head').append(linkElement);
}
function formatDate() {
    const date = new Date(); // 获取当前时间
    const year = date.getFullYear(); // 获取年份
    const month = String(date.getMonth() + 1).padStart(2, '0'); // 获取月份，注意需要 +1
    const day = String(date.getDate()).padStart(2, '0'); // 获取日期

    const weekdays = ['日', '一', '二', '三', '四', '五', '六']; // 星期数组
    const weekDay = weekdays[date.getDay()]; // 获取星期几

    const hours = String(date.getHours()).padStart(2, '0'); // 获取小时
    const minutes = String(date.getMinutes()).padStart(2, '0'); // 获取分钟
    const seconds = String(date.getSeconds()).padStart(2, '0'); // 获取秒数

    // 格式化为所需的字符串
    return `${year}/${month}/${day} 周${weekDay}<br/> ${hours}:${minutes}:${seconds}`;
}
class appClass{
    models=[];
    userData=null;
    messages=[];
    getUserData(){
        this.userData=$.parseJSON(synchronousPostRequest("../../../data/user_data.json",""));
        if (typeof this.userData.data.username === "undefined"){
            $("#username").html("未登录");
        }else{
            $("#username").html(this.userData.data.username);
        }
    }
    constructor(){
        var tip = Swal.fire({
            title: 'Loading',
            text: '加载中',
            icon: 'info',
            allowOutsideClick: false,
            allowEscapeKey: false,
            showConfirmButton: false
        })
        setPage("pages/index.html");
        this.models=$.parseJSON(synchronousPostRequest("../../../data/entries.json",""));
        this.models.forEach(function(name){
            console.log(name);
            loadJs(`../../../data/${name}/index.js`);
        });
        window.dispatchEvent(new Event("resize"));
        // 监听事件
        document.addEventListener("usersidebarEvent", (event) => {
            console.log(event.detail.open);
        });
        this.getUserData();
        // 创建 WebSocket 连接
        this.socket = new WebSocket('/ws'); // 替换为你的 WebSocket 服务器地址
        // 当 WebSocket 连接成功时触发
        this.socket.onopen = function(event) {

        };

        // 当接收到来自 WebSocket 服务器的消息时触发
        this.socket.onmessage = function(event1) {
            const event = new CustomEvent("app_ws", {
                detail: { type: "receive",message:$.parseJSON(event1.data)} // 可选，传递数据
            });
            document.dispatchEvent(event);
            console.log(event1.data);
        };

        // 当 WebSocket 连接关闭时触发
        this.socket.onclose = function(event) {
            console.log('WebSocket连接已关闭。');
        };

        // 当 WebSocket 出现错误时触发
        this.socket.onerror = function(error) {
            console.error('WebSocket错误:', error);
        };
        tip.close();
    }
    sendMessage(level,content){
        var msg = new AppMessage(level,content);
        this.messages.push(msg);
        const event = new CustomEvent("message", {
            detail: { type: "new",message:msg} // 可选，传递数据
        });
        document.dispatchEvent(event);
    }
}
class AppMessage{
    constructor(level,content){
        this.level=level;
        this.content=content;
    }
}