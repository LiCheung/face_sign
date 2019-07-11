var video = document.getElementById('video');
var canvas = document.getElementById('canvas');
var ctx = canvas.getContext('2d');
var width = video.width;
var height = video.height;
canvas.width = width;
canvas.height = height;
function liveVideo(){
    var URL = window.URL || window.webkitURL;   // 获取到window.URL对象
    navigator.getUserMedia({
        video: true
    }, function(stream){
        /*video.src = URL.createObjectURL(stream);   // 将获取到的视频流对象转换为地址*/
        try {
            video.src =  window.URL.createObjectURL(stream);
        }catch (e){
            video.srcObject = stream;
        }
        video.play();   // 播放
        //点击截图     
        document.getElementById("snap").addEventListener('click', function() {
            ctx.drawImage(video, 0, 0, width, height);
            var url = canvas.toDataURL('image/png');
            document.getElementById('snap').href = url;

            var dlLink = document.createElement('a');
            dlLink.download = "123";
            dlLink.href = url;
            //dlLink.src = "G:\\test";
            dlLink.dataset.downloadurl = [url, dlLink.download, dlLink.href].join(':');
            document.body.appendChild(dlLink);
            dlLink.click();
            document.body.removeChild(dlLink);
        });
    }, function(error){
        console.log(error.name || error);
    });
}
/*
function exportCanvasAsPNG(id, fileName) {

    var canvasElement = document.getElementById(id);
    var MIME_TYPE = "image/png";
    var imgURL = canvasElement.toDataURL(MIME_TYPE);
    var dlLink = document.createElement('a');
    dlLink.download = fileName;
    dlLink.href = imgURL;
    dlLink.dataset.downloadurl = [MIME_TYPE, dlLink.download, dlLink.href].join(':');

    document.body.appendChild(dlLink);
    dlLink.click();
    document.body.removeChild(dlLink);
}*/

/*function downloadCanvasIamge(selector, name) {
    // 通过选择器获取canvas元素
    var canvas = document.querySelector(selector)
    // 使用toDataURL方法将图像转换被base64编码的URL字符串
    var url = canvas.toDataURL('image/png')
    // 生成一个a元素
    var a = document.createElement('a')
    // 创建一个单击事件
    var event = new MouseEvent('click')

    // 将a的download属性设置为我们想要下载的图片名称，若name不存在则使用‘下载图片名称’作为默认名称
    a.download = name || '下载图片名称'
    // 将生成的URL设置为a.href属性
    a.href = url

    // 触发a的单击事件
    a.dispatchEvent(event)
}

// 调用方式
// 参数一： 选择器，代表canvas
// 参数二： 图片名称，可选
downloadCanvasIamge('canvas', '图片名称')*/

document.getElementById("live").addEventListener('click',function(){
    liveVideo();
});

